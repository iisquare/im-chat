package com.iisquare.im.server.broker.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.iisquare.util.DPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class SyncJob implements MessageListener {

    public static final String CHANNEL_SYNC = "im:chat:sync";
    @Autowired
    private StringRedisTemplate redis;

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
        JsonNode sync = DPUtil.parseJSON(new String(message.getBody()));
        System.out.println(sync);
    }
}
