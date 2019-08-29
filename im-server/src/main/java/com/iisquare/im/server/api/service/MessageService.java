package com.iisquare.im.server.api.service;

import com.iisquare.im.server.api.dao.MessageDao;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.mvc.ServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class MessageService extends ServiceBase {

    @Autowired
    private MessageDao messageDao;

    public String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Message save(Message info) {
        info.setTime(new Date());
        if(null == info.getId()) info.setId(uuid());
        return messageDao.save(info);
    }

}
