package com.iisquare.im.server.broker.core;

import com.google.protobuf.InvalidProtocolBufferException;
import com.iisquare.im.protobuf.IM;
import com.iisquare.im.server.broker.HttpHandler;
import com.iisquare.util.DPUtil;
import com.iisquare.util.ReflectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
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
        List<String> list = ReflectUtil.getClassName(classpath.replace(".core", ".logic"));
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

    public IM.Result dispatch(String fromType, ChannelHandlerContext ctx, ByteBuf message) {
        IM.Directive directive = null;
        try {
            directive = IM.Directive.parseFrom(message.nioBuffer());
        } catch (InvalidProtocolBufferException e) {
            logger.warn("can not parse message", e);
            return null;
        }
        Command command = commands.get(directive.getCommand());
        if (null == command) return Logic.result(directive, -1, "指令异常", null);
        try {
            return command.invoke(fromType, ctx, directive);
        } catch (Exception e) {
            logger.warn("dispatch invoke failed", e);
            return Logic.result(directive, -2, e.getMessage(), null);
        }
    }

}
