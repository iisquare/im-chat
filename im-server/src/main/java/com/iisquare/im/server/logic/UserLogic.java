package com.iisquare.im.server.logic;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iisquare.im.protobuf.Im;
import com.iisquare.im.protobuf.User;
import com.iisquare.im.server.core.Logic;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class UserLogic extends Logic {

    public void authAction(ChannelHandlerContext ctx, Im.Directive directive) {
        try {
            User.Auth auth = User.Auth.parseFrom(directive.getParameter());
            System.out.println("user.auth->" + auth.getToken());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}
