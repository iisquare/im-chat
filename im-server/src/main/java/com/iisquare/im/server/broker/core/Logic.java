package com.iisquare.im.server.broker.core;

import com.google.protobuf.ByteString;
import com.iisquare.im.protobuf.IM;

public abstract class Logic {

    public static IM.Result result(IM.Directive directive, int code, String message, ByteString data) {
        if(null == message) {
            switch (code) {
                case 0:
                    message = "操作成功";
                    break;
                case 403:
                    message = "禁止访问";
                    break;
                case 404:
                    message = "信息不存在";
                    break;
                case 500:
                    message = "操作失败";
                    break;
                default:
                    message = "";
            }
        }
//        if (null == data) data = Any.getDefaultInstance();
        if (null == data) data = ByteString.EMPTY;
        return IM.Result.newBuilder()
            .setSequence(directive.getSequence()).setCode(code).setMessage(message).setData(data).build();
    }

}
