package com.iisquare.im.server.broker.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMMessage;
import com.iisquare.im.server.broker.core.Handler;
import com.iisquare.im.server.broker.logic.MessageLogic;
import com.iisquare.im.server.broker.logic.UserLogic;
import com.iisquare.util.DPUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
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
public class SyncJob implements MessageListener {

    public static final String CHANNEL_SYNC = "im:chat:sync";
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
        container.addMessageListener(messageListener(), new ChannelTopic(CHANNEL_SYNC));
        return container;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        JsonNode json = DPUtil.parseJSON(new String(message.getBody()));
        if (null == json) return;
        IMMessage.Sync sync = IMMessage.Sync.newBuilder().setVersion(json.get("v").asLong()).build();
        IM.Result result = IM.Result.newBuilder().setSequence(MessageLogic.SEQUENCE_SYNC).setData(sync.toByteString()).build();
        for (String fromType : fromTypes) {
            ChannelGroup group = userLogic.channelGroup(fromType, json.get("u").asText());
            if (null == group) continue;
            switch (fromType) {
                case Handler.MESSAGE_FROM_TYPE_WS:
                    group.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(result.toByteArray())));
                    break;
                case Handler.MESSAGE_FROM_TYPE_COMET:
                    break;
                case Handler.MESSAGE_FROM_TYPE_SOCKET:
                    break;
            }
        }
    }
}
