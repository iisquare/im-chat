package com.iisquare.im.server.broker.logic;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMMessage;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.Scatter;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.MessageService;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MessageLogic extends Logic {

    @Autowired
    private UserLogic userLogic;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private StringRedisTemplate redis;

    public long increase(String userId) {
        return redis.opsForValue().increment("im:chat:version:" + userId).longValue();
    }

    public IM.Result pushAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        User sender = userLogic.info(ctx);
        if (null == sender) return result(directive, 404, "用户信息异常", null);
        IMMessage.Push push = IMMessage.Push.parseFrom(directive.getParameter());
        if (!"person".equals(push.getReception())) {
            return result(directive, 1001, "接收类型异常", null);
        }
        User receiver = userService.info(push.getReceiver());
        if (null == receiver) return result(directive, 1002, "接收用户异常", null);
        if (!"txt".equals(push.getType())) {
            return result(directive, 1003, "消息类型异常", null);
        }
        Message message = Message.builder().sender(sender.getId())
            .version(increase(sender.getId())).reception(push.getReception())
            .receiver(receiver.getId()).sequence(directive.getSequence())
            .type(push.getType()).content(push.getContent()).time(new Date()).build();
        message = messageService.save(message);
        Scatter scatter = Scatter.builder().messageId(message.getId())
            .receiver(receiver.getId()).version(increase(receiver.getId())).build();
        scatter = messageService.save(scatter);
        IMMessage.PushACK.Builder ack = IMMessage.PushACK.newBuilder();
        ack.setId(message.getId()).setVersion(message.getVersion()).setTime(message.getTime().getTime());
        userLogic.sync(sender, message, receiver, scatter);
        return result(directive, 0, null, ack.build());
    }

}
