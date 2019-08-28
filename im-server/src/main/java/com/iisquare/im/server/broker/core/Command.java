package com.iisquare.im.server.broker.core;

import com.iisquare.im.protobuf.IM;
import io.netty.channel.ChannelHandlerContext;
import lombok.*;

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

    public IM.Result invoke(String fromType, ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        return (IM.Result) method.invoke(instance, fromType, ctx, directive);
    }

}
