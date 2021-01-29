package com.iisquare.im.server.test;

import com.iisquare.util.ReflectUtil;
import org.junit.Test;

import java.util.List;

public class JarTester {

    @Test
    public void loadTest() {
        String jarPath = "file:/D:/xampp/htdocs/im-chat/im-server/build/libs/im-server-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/com/iisquare/im/server/broker/logic/";
        List<String> list = ReflectUtil.getClassNameByJar(jarPath, true);
        System.out.println(list);
    }

}
