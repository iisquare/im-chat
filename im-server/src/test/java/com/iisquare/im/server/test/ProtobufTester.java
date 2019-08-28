package com.iisquare.im.server.test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iisquare.im.protobuf.IMUser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class ProtobufTester {

    @Test
    public void serializeTest() throws InvalidProtocolBufferException {
        IMUser.Contact.Row.Builder builder = IMUser.Contact.Row.newBuilder();
        builder.setUserId("xxx").setDirection("send").setMessageId("hhhhhh");
        builder.setContent("fasfsaf");
        long time = new Date().getTime();
        time = 1566988103970L; // ok
        time = 1566988066637L; // bad
        System.out.println(time);
        builder.setTime(time);
        ByteString b1 = builder.build().toByteString();
        System.out.println(Arrays.toString(b1.toByteArray()));
        String data = Base64.getEncoder().encodeToString(b1.toByteArray());
        System.out.println(data);
        byte[] b2 = Base64.getDecoder().decode(data);
        System.out.println(Arrays.toString(b2));
        String str = new String(b1.toByteArray());
        System.out.println(str);
        System.out.println(b1.toStringUtf8());
        System.out.println(Arrays.toString(str.getBytes()));
        IMUser.Contact.Row row = IMUser.Contact.Row.parseFrom(b2);
        System.out.println(row);
    }

}
