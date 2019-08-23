package com.iisquare.im.server.core;

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

    public IM.Result invoke(ChannelHandlerContext ctx, IM.Directive directive) throws Exception {
        Object result = method.invoke(instance, ctx, directive);
        if (null == result) return null;
        return (IM.Result) result;
    }

}
