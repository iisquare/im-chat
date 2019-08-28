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
    /**
     * @see(https://github.com/protocolbuffers/protobuf/issues/6581)
     */
    public void serializeTest() throws InvalidProtocolBufferException {
        IMUser.Contact.Row.Builder builder = IMUser.Contact.Row.newBuilder();
        long time = new Date().getTime(); // random
        time = 1566988103970L; // ok
        time = 1566988066637L; // bad
        System.out.println("time:" + time);
        builder.setTime(time);
        ByteString b1 = builder.build().toByteString();
        System.out.println("origin:" + Arrays.toString(b1.toByteArray()));
        String encode = Base64.getEncoder().encodeToString(b1.toByteArray());
        System.out.println("base64 encode:" + encode);
        byte[] decode = Base64.getDecoder().decode(encode);
        System.out.println("base64 decode:" + Arrays.toString(decode));
        IMUser.Contact.Row row = IMUser.Contact.Row.parseFrom(decode);
        System.out.println("-------ROW BEGIN-------");
        System.out.println(row);
        System.out.println("-------ROW END-------");
        String data = b1.toStringUtf8(); // same with new String(b1.toByteArray())
        System.out.println("b1 data:" + data);
        ByteString b2 = ByteString.copyFromUtf8(data);
        System.out.println("b2 data:" + b2.toStringUtf8());
        byte[] bytes = b2.toByteArray(); // same with data.getBytes()
        System.out.println("bytes:" + Arrays.toString(bytes));
        IMUser.Contact.Row.parseFrom(b2); // bad time will throw int64 exception
    }

}
