package com.iisquare.im.server.broker.logic;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMMessage;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.service.MessageService;
import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.broker.core.Logic;
import com.iisquare.util.DPUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public long increase() {
        return redis.opsForValue().increment("im:chat:version:message").longValue();
    }

    public IM.Result pullAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userLogic.userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        IMMessage.Pull pull = IMMessage.Pull.parseFrom(directive.getParameter());
        Map<Object, Object> param = DPUtil.buildMap(
            "page", pull.getPage(), "pageSize", pull.getPageSize(),
            "sort", pull.getSort(), "reception", pull.getReception(),
            "minVersion", pull.getMinVersion(), "maxVersion", pull.getMaxVersion(),
            "minVersion", pull.getMinVersion(), "maxVersion", pull.getMaxVersion(),
            "minTime", pull.getMinTime(), "maxTime", pull.getMaxTime());
        if (DPUtil.empty(pull.getReceiver())) {
            param.put("ca", userId);
        } else  {
            param.put("cs", userId);
            param.put("cr", pull.getReceiver());
        }
        Map<String, Object> search = messageService.search(param, DPUtil.buildMap());
        IMMessage.PullResult.Builder result = IMMessage.PullResult.newBuilder();
        result.setPage((Integer) search.get("page"));
        result.setPageSize((Integer) search.get("pageSize"));
        result.setTotal((Long) search.get("total"));
        List rows = new ArrayList();
        for (Message item : (List<Message>) search.get("rows")) {
            IMMessage.Body.Builder body = IMMessage.Body.newBuilder();
            body.setId(item.getId()).setVersion(item.getVersion());
            body.setSender(item.getSequence()).setSender(item.getSender());
            body.setReception(item.getReception()).setReceiver(item.getReceiver());
            body.setType(item.getType()).setContent(item.getContent());
            body.setTime(item.getTime()).setWithdraw(item.getWithdraw());
            rows.add(body.build());
        }
        result.addAllRows(rows);
        return result(directive, 0, null, result.build());
    }

    public IM.Result syncAction(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        String userId = userLogic.userId(ctx);
        if (DPUtil.empty(userId)) return result(directive, 404, "用户信息异常", null);
        long version = userService.version(userId);
        IMMessage.Sync sync = IMMessage.Sync.newBuilder().setVersion(version).build();
        return result(directive, 0, null, sync);
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
            .version(increase()).reception(push.getReception())
            .receiver(receiver.getId()).sequence(directive.getSequence())
            .type(push.getType()).content(push.getContent()).time(System.currentTimeMillis()).build();
        message = messageService.save(message);
        IMMessage.PushResult.Builder ack = IMMessage.PushResult.newBuilder();
        ack.setId(message.getId()).setVersion(message.getVersion()).setTime(message.getTime());
        userLogic.sync(sender, receiver, message);
        return result(directive, 0, null, ack.build());
    }

}
