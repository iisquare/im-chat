package com.iisquare.im.server.core;

import com.google.protobuf.ByteString;
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

    public void invoke(ByteString parameter) throws InvocationTargetException, IllegalAccessException {
        method.invoke(instance, parameter);
    }

}
