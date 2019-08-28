package com.iisquare.im.server.broker.logic;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.Scatter;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserLogic extends Logic {

    public static final AttributeKey USER_KEY = AttributeKey.valueOf("userId");
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redis;

    public User info(ChannelHandlerContext ctx) {
        Object value = ctx.channel().attr(USER_KEY).get();
        if (null == value) return null;
        User info = userService.info(value.toString());
        return null == info || info.isBlocked() ? null : info;
    }

    public IM.Result authAction(ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
//         IMUser.Auth auth = directive.getParameter().unpack(IMUser.Auth.class);
        IMUser.Auth auth = IMUser.Auth.parseFrom(directive.getParameter());
        User info = userService.info(userService.userId(auth.getToken()));
        if (null == info) return result(directive, 404, "用户信息不存在", null);
        ctx.channel().attr(USER_KEY).set(info.getId());
//        return result(directive, 0, null, Any.pack(IMUser.AuthResult.newBuilder().setUserId(info.getId()).build()));
        return result(directive, 0, null, IMUser.AuthResult.newBuilder().setUserId(info.getId()).build());
    }

    public String concat(String userId) {
        return "im:chat:concat:" + userId;
    }

    public void concat(User sender, Message message, User receiver, Scatter scatter) {
        // 发送方
        IMUser.Contact.Row.Builder builder = IMUser.Contact.Row.newBuilder();
        builder.setUserId(receiver.getId()).setMessageId(message.getId()).setDirection("send")
            .setContent(message.getContent()).setTime(message.getTime().getTime());
        redis.opsForHash().put(concat(sender.getId()), receiver.getId(), builder.build().toByteString());
        // 接收方
        builder = IMUser.Contact.Row.newBuilder();
        builder.setUserId(sender.getId()).setMessageId(message.getId()).setDirection("receive")
            .setContent(message.getContent()).setTime(message.getTime().getTime());
        redis.opsForHash().put(concat(receiver.getId()), sender.getId(), builder.build().toByteString());
    }

}
