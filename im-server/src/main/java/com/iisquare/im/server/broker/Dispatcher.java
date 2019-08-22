package com.iisquare.im.server.broker;

import com.iisquare.im.protobuf.Im;
import com.iisquare.im.server.core.Command;
import com.iisquare.util.DPUtil;
import com.iisquare.util.ReflectUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class Dispatcher {

    protected final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private static final String LOGIC_SUFFIX = "Logic";
    private static final String ACTION_SUFFIX = "Action";
    private Map<String, Command> commands = new LinkedHashMap<>();
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        String classpath = this.getClass().getName();
        classpath = classpath.substring(0, classpath.length() - HttpHandler.class.getSimpleName().length());
        List<String> list = ReflectUtil.getClassName(classpath.replace(".broker", ".logic"));
        for (String item : list) {
            if (!item.endsWith(LOGIC_SUFFIX)) continue;
            try {
                Class<?> controller = Class.forName(item);
                Object instance = context.getBean(controller);
                String controllerName = controller.getSimpleName();
                controllerName = controllerName.substring(0, controllerName.length() - LOGIC_SUFFIX.length());
                controllerName = DPUtil.lowerCaseFirst(controllerName);
                for (Method method : controller.getMethods()) {
                    String actionName = method.getName();
                    if (!actionName.endsWith(ACTION_SUFFIX)) continue;
                    actionName = actionName.substring(0, actionName.length() - ACTION_SUFFIX.length());
                    String command = controllerName + "." + actionName;
                    commands.put(command, Command.builder()
                        .controller(controllerName).action(actionName).instance(instance).method(method).build());
                }
            } catch (Exception e) {
                logger.warn("load logic failed from " + item, e);
                continue;
            }
        }
    }

    public void dispatch(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        try {
            Im.Directive directive = Im.Directive.parseFrom(frame.content().nioBuffer());
            Command command = commands.get(directive.getCommand());
            if (null == command) return;
            command.invoke(directive.getParameter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
