package com.iisquare.im.server.broker.logic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.Scatter;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import com.iisquare.im.server.broker.job.SyncJob;
import com.iisquare.util.DPUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserLogic extends Logic {

    public static final AttributeKey USER_KEY = AttributeKey.valueOf("userId");
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redis;
    public static final Map<String, ChannelGroup> channels = new ConcurrentHashMap<>();

    public String userId(ChannelHandlerContext ctx) {
        Object value = ctx.channel().attr(USER_KEY).get();
        if (null == value) return null;
        return value.toString();
    }

    public User info(ChannelHandlerContext ctx) {
        User info = userService.info(userId(ctx));
        return null == info || info.isBlocked() ? null : info;
    }

    public void logout(String fromType, ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        ChannelGroup group = channels.get(channelKey(fromType, userId(ctx)));
        if (null == group) return;
        group.remove(channel);
    }

    public ChannelGroup channelGroup(String fromType, String userId) {
        return channels.get(channelKey(fromType, userId));
    }

    public String channelKey (String fromType, String userId) {
        return fromType + "@" + userId;
    }

    public IM.Result authAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
//         IMUser.Auth auth = directive.getParameter().unpack(IMUser.Auth.class);
        IMUser.Auth auth = IMUser.Auth.parseFrom(directive.getParameter());
        User info = userService.info(userService.userId(auth.getToken()));
        if (null == info) return result(directive, 404, "用户信息不存在", null);
        ctx.channel().attr(USER_KEY).set(info.getId());
        String key = this.channelKey(fromType, info.getId());
        ChannelGroup group = channels.get(key);
        if (null == group) {
            synchronized (UserLogic.class) {
                group = channels.get(info.getId());
                if (null == group) {
                    group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                    channels.put(key, group);
                }
            }
        }
        group.add(ctx.channel());
//        return result(directive, 0, null, Any.pack(IMUser.AuthResult.newBuilder().setUserId(info.getId()).build()));
        return result(directive, 0, null, IMUser.AuthResult.newBuilder().setUserId(info.getId()).build());
    }

    public IM.Result contactAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Contact.Builder builder = IMUser.Contact.newBuilder();
        for (Object o : redis.opsForHash().values(contact(userId))) {
            builder.addRows(IMUser.Contact.Row.parseFrom(ByteString.copyFromUtf8(o.toString())));
        }
        return result(directive, 0, null, builder.build());
    }

    public String contact(String userId) {
        return "im:chat:contact:" + userId;
    }

    public IM.Result uncontactAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Uncontact uncontact = IMUser.Uncontact.parseFrom(directive.getParameter());
        redis.opsForHash().delete(contact(userId), uncontact.getUserId());
        return result(directive, 0, null, null);
    }

    public void sync(User sender, Message message, User receiver, Scatter scatter) throws UnsupportedEncodingException {
        // 发送方
        IMUser.Contact.Row.Builder builder = IMUser.Contact.Row.newBuilder();
        builder.setUserId(receiver.getId()).setMessageId(message.getId()).setDirection("send")
            .setContent(message.getContent()).setTime(message.getTime().getTime());
        String data = builder.build().toByteString().toStringUtf8();
        redis.opsForHash().put(contact(sender.getId()), receiver.getId(), data);
        // 接收方
        builder = IMUser.Contact.Row.newBuilder();
        builder.setUserId(sender.getId()).setMessageId(message.getId()).setDirection("receive")
            .setContent(message.getContent()).setTime(message.getTime().getTime());
        data = builder.build().toByteString().toStringUtf8();
        redis.opsForHash().put(contact(receiver.getId()), sender.getId(), data);
        // 同步通知
        ObjectNode sync = DPUtil.objectNode();
        sync.put("u", sender.getId()).put("v", message.getVersion());
        redis.convertAndSend(SyncJob.CHANNEL_SYNC, DPUtil.parseString(sync));
        sync.put("u", receiver.getId()).put("v", scatter.getVersion());
        redis.convertAndSend(SyncJob.CHANNEL_SYNC, DPUtil.parseString(sync));
    }

}
