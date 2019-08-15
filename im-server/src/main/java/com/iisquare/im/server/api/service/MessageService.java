package com.iisquare.im.server.api.service;

import com.iisquare.im.server.api.dao.MessageDao;
import com.iisquare.im.server.api.dao.ScatterDao;
import com.iisquare.im.server.api.mvc.Configuration;
import com.iisquare.im.server.api.mvc.ServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MessageService extends ServiceBase {

    @Autowired
    private Configuration configuration;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private ScatterDao scatterDao;

    /**
     * 线程之间默认共享Redis连接句柄
     * 暂不支持通过配置文件定义shareNativeConnection参数：https://github.com/spring-projects/spring-boot/pull/14217
     * 目前只有此处用到Redis，可通过自定义Factory重写该处逻辑：https://github.com/spring-projects/spring-boot/issues/14196
     */
    @PostConstruct
    public void initialize() {
        LettuceConnectionFactory factory = (LettuceConnectionFactory) redis.getConnectionFactory();
        factory.setShareNativeConnection(false);
    }

}
