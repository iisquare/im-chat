package com.iisquare.im.server.broker.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import com.iisquare.im.server.broker.job.TransmitJob;
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

    public static final AttributeKey ATTR_USER = AttributeKey.valueOf("userId");
    public static final AttributeKey ATTR_FROM = AttributeKey.valueOf("fromType");
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redis;
    public static final Map<String, ChannelGroup> channels = new ConcurrentHashMap<>();

    public String userId(ChannelHandlerContext ctx) {
        Object value = ctx.channel().attr(ATTR_USER).get();
        if (null == value) return null;
        return value.toString();
    }

    public String fromType(ChannelHandlerContext ctx) {
        Object value = ctx.channel().attr(ATTR_FROM).get();
        if (null == value) return null;
        return value.toString();
    }

    public String channelKey (ChannelHandlerContext ctx) {
        return fromType(ctx) + "@" + userId(ctx);
    }

    public User info(ChannelHandlerContext ctx) {
        User info = userService.info(userId(ctx));
        return null == info || info.isBlocked() ? null : info;
    }

    public ChannelGroup channelGroup(String fromType, String userId) {
        return channels.get(channelKey(fromType, userId));
    }

    public String channelKey (String fromType, String userId) {
        return fromType + "@" + userId;
    }

    public void syn(String fromType, ChannelHandlerContext ctx, String userId) {
        String key = this.channelKey(fromType, userId);
        ChannelGroup group = channels.get(key);
        if (null == group) {
            synchronized (UserLogic.class) {
                group = channels.get(userId);
                if (null == group) {
                    group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                    channels.put(key, group);
                }
            }
        }
        group.add(ctx.channel());
    }

    public void fin( ChannelHandlerContext ctx) {
        ChannelGroup group = channels.get(channelKey(ctx));
        if (null == group) return;
        group.remove(ctx.channel());
    }

    public IM.Result authAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
//         IMUser.Auth auth = directive.getParameter().unpack(IMUser.Auth.class);
        IMUser.Auth auth = IMUser.Auth.parseFrom(directive.getParameter());
        User info = userService.info(userService.userId(auth.getToken()));
        if (null == info) return result(directive, 404, "用户信息不存在", null);
        ctx.channel().attr(ATTR_USER).set(info.getId());
        ctx.channel().attr(ATTR_FROM).set(fromType);
        if (auth.getWithSyn()) this.syn(fromType, ctx, info.getId());
//        return result(directive, 0, null, Any.pack(IMUser.AuthResult.newBuilder().setUserId(info.getId()).build()));
        IMUser.AuthResult.Builder result = IMUser.AuthResult.newBuilder();
        result.setHeartbeat(30000);
        result.setUserId(info.getId()).setVersion(userService.version(info.getId()));
        return result(directive, 0, null, result.build());
    }

    public String contactKey(String userId) {
        return "im:chat:contact:" + userId;
    }

    public String contactHash(String reception, String id) {
        return reception + "@" + id;
    }

    public String unreadKey(String userId) {
        return "im:chat:unread:" + userId;
    }

    public String unreadHash(String reception, String id) {
        return reception + "@" + id;
    }

    /**
     * 获取联系人列表
     */
    public IM.Result contactAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Contact.Builder builder = IMUser.Contact.newBuilder();
        for (Object o : redis.opsForHash().values(contactKey(userId))) {
            JsonNode json = DPUtil.parseJSON(o.toString());
            if (null == json) continue;
            IMUser.Contact.Row.Builder contact = IMUser.Contact.Row.newBuilder();
            contact.setDirection(json.at("/direction").asText("")).setMessageId(json.at("/messageId").asText(""));
            contact.setReception(json.at("/reception").asText("")).setReceiver(json.at("/receiver").asText(""));
            contact.setType(json.at("/type").asText("")).setContent(json.at("/content").asText(""));
            contact.setTime(json.at("/time").asLong(0)).setVersion(json.at("/version").asLong(0));
            builder.addRows(contact.build());
        }
        return result(directive, 0, null, builder.build());
    }

    /**
     * 清除联系人
     */
    public IM.Result detachAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Detach fin = IMUser.Detach.parseFrom(directive.getParameter());
        redis.opsForHash().delete(contactKey(userId), contactHash(fin.getReception(), fin.getReceiver()));
        return result(directive, 0, null, null);
    }

    /**
     * 获取未读消息数
     */
    public IM.Result unreadAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Unread.Builder builder = IMUser.Unread.newBuilder();
        Map<Object, Object> entries = redis.opsForHash().entries(contactKey(userId));
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            IMUser.Unread.Row.Builder unread = IMUser.Unread.Row.newBuilder();
            String[] strings = entry.getKey().toString().split("@");
            if (strings.length != 2) continue;
            unread.setReception(strings[0]).setReceiver(strings[1]).setCount(DPUtil.parseLong(entry.getValue()));
            builder.addRows(unread.build());
        }
        return result(directive, 0, null, builder.build());
    }

    /**
     * 清除未读计数
     */
    public IM.Result deliveryAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMUser.Delivery delivery = IMUser.Delivery.parseFrom(directive.getParameter());
        redis.opsForHash().delete(unreadKey(userId), unreadHash(delivery.getReception(), delivery.getReceiver()));
        // 推送通知
        TransmitJob.delivery(redis, userId, delivery);
        return result(directive, 0, null, null);
    }

    /**
     * 群组和公众号方式需要转换收发方数据
     * 采用下述方式进行序列化前后结果可能不一致
     * ByteString.toStringUtf8()->new String(byte[])
     * ByteString.copyFromUtf8()->string.getBytes()
     */
    public void sync(User sender, User receiver, Message message) throws UnsupportedEncodingException {
        // 发送方
        ObjectNode contact = DPUtil.objectNode();
        contact.put("direction", MessageLogic.DIRECTION_SEND).put("messageId", message.getId());
        contact.put("reception", message.getReception()).put("receiver", message.getReceiver());
        contact.put("type", message.getType()).put("content", message.getContent());
        contact.put("time", message.getTime()).put("version", message.getVersion());
        redis.opsForHash().put(contactKey(sender.getId()),
            contactHash(message.getReception(), receiver.getId()), DPUtil.stringify(contact));
        userService.version(sender.getId(), message.getVersion());
        // 接收方
        contact.put("direction", MessageLogic.DIRECTION_RECEIVE);
        contact.put("reception", message.getReception()).put("receiver", message.getSender());
        redis.opsForHash().put(contactKey(receiver.getId()),
            contactHash(message.getReception(), sender.getId()), DPUtil.stringify(contact));
        userService.version(receiver.getId(), message.getVersion());
        // 未读计数
        redis.opsForHash().increment(
            unreadKey(receiver.getId()), unreadHash(message.getReception(), sender.getId()), 1);
        // 同步通知
        TransmitJob.sync(redis, sender.getId(), message.getVersion());
        TransmitJob.sync(redis, receiver.getId(), message.getVersion());
    }

}
