package com.iisquare.im.server.logic;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iisquare.im.protobuf.User;
import com.iisquare.im.server.core.Logic;
import org.springframework.stereotype.Component;

@Component
public class UserLogic extends Logic {

    public void authAction(ByteString parameter) {
        try {
            User.Auth auth = User.Auth.parseFrom(parameter);
            System.out.println("user.auth->" + auth.getToken());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}
