package com.iisquare.im.server.broker.logic;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMMessage;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageLogic extends Logic {

    @Autowired
    private UserLogic userLogic;
    @Autowired
    private UserService userService;

    public IM.Result pushAction(ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        User user = userLogic.info(ctx);
        if (null == user) return result(directive, 404, "用户信息异常", null);
        IMMessage.Push push = IMMessage.Push.parseFrom(directive.getParameter());
        if (!"person".equals(push.getReception())) {
            return result(directive, 1001, "接收类型异常", null);
        }
        User receiver = userService.info(push.getReceiver());
        if (null == receiver) return result(directive, 1002, "接收用户异常", null);
        if (!"txt".equals(push.getType())) {
            return result(directive, 1003, "消息类型异常", null);
        }
        return null;
    }

}
