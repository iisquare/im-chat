package com.iisquare.im.server.core;

import com.google.protobuf.ByteString;
import com.iisquare.im.protobuf.Im;
import io.netty.channel.ChannelHandlerContext;
import lombok.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Command {

    private String controller;
    private String action;
    private Object instance;
    private Method method;

    public void invoke(ChannelHandlerContext ctx, Im.Directive directive) throws InvocationTargetException, IllegalAccessException {
        method.invoke(instance, ctx, directive);
    }

}
