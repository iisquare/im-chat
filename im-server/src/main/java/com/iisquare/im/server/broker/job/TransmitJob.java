package com.iisquare.im.server.broker.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMMessage;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.broker.HttpBroker;
import com.iisquare.im.server.broker.core.Handler;
import com.iisquare.im.server.broker.logic.MessageLogic;
import com.iisquare.im.server.broker.logic.UserLogic;
import com.iisquare.util.DPUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class TransmitJob implements MessageListener {

    protected final static Logger logger = LoggerFactory.getLogger(TransmitJob.class);
    public static final String CHANNEL_TRANSMIT = "im:chat:topic:transmit";
    public static final String TRANSMIT_SYNC = "sync";
    public static final String TRANSMIT_DELIVERY = "delivery";
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    protected UserLogic userLogic;
    private List<String> fromTypes = Arrays.asList(
        Handler.MESSAGE_FROM_TYPE_SOCKET, Handler.MESSAGE_FROM_TYPE_WS, Handler.MESSAGE_FROM_TYPE_COMET);

    @Bean
    MessageListenerAdapter messageListener( ) {
        return new MessageListenerAdapter(this);
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container
            = new RedisMessageListenerContainer();
        container.setConnectionFactory(redis.getConnectionFactory());
        container.addMessageListener(messageListener(), new ChannelTopic(CHANNEL_TRANSMIT));
        return container;
    }

    public static void sync(StringRedisTemplate redis, String userId, long version) {
        ObjectNode transmit = DPUtil.objectNode();
        ObjectNode parameter = transmit.put("transmit", TRANSMIT_SYNC).putObject("parameter");
        parameter.put("userId", userId).put("version", version);
        redis.convertAndSend(TransmitJob.CHANNEL_TRANSMIT, DPUtil.parseString(transmit));
    }

    public IM.Result sync(JsonNode parameter) {
        IMMessage.Sync sync = IMMessage.Sync.newBuilder().setVersion(parameter.get("version").asLong()).build();
        return IM.Result.newBuilder().setSequence(MessageLogic.SEQUENCE_SYNC).setData(sync.toByteString()).build();
    }

    public static void delivery(StringRedisTemplate redis, String userId, IMUser.Delivery delivery) {
        ObjectNode transmit = DPUtil.objectNode();
        ObjectNode parameter = transmit.put("transmit", TRANSMIT_DELIVERY).putObject("parameter");
        parameter.put("userId", userId).put("base64", DPUtil.encode(delivery.toByteArray()));
        redis.convertAndSend(TransmitJob.CHANNEL_TRANSMIT, DPUtil.parseString(transmit));
    }

    public IM.Result delivery(JsonNode parameter) {
        try {
            IMUser.Delivery delivery = IMUser.Delivery.parseFrom(DPUtil.decode(parameter.get("base64").asText()));
            return IM.Result.newBuilder().setSequence(MessageLogic.SEQUENCE_DELIVERY).setData(delivery.toByteString()).build();
        } catch (InvalidProtocolBufferException e) {
            logger.warn("decode delivery parameter failed", e);
            return null;
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        JsonNode json = DPUtil.parseJSON(new String(message.getBody()));
        if (null == json) return;
        IM.Result result;
        String userId;
        JsonNode parameter = json.get("parameter");
        switch (json.get("transmit").asText("")) {
            case TRANSMIT_SYNC:
                result = sync(parameter);
                userId = parameter.get("userId").asText();
                break;
            case TRANSMIT_DELIVERY:
                result = delivery(parameter);
                userId = parameter.get("userId").asText();
                break;
            default: return;
        }
        if (null == result || DPUtil.empty(userId)) return;
        for (String fromType : fromTypes) {
            ChannelGroup group = userLogic.channelGroup(fromType, userId);
            if (null == group) continue;
            switch (fromType) {
                case Handler.MESSAGE_FROM_TYPE_WS:
                    group.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(result.toByteArray())));
                    break;
                case Handler.MESSAGE_FROM_TYPE_COMET:
                    group.writeAndFlush(new DefaultHttpContent(Unpooled.wrappedBuffer(result.toByteArray())));
                    group.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    break;
                case Handler.MESSAGE_FROM_TYPE_SOCKET:
                    group.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(result.toByteArray())));
                    break;
            }
        }
    }
}
