package com.iisquare.im.server.api.service;

import com.iisquare.im.server.api.dao.UserDao;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.mvc.ServiceBase;
import com.iisquare.util.DPUtil;
import com.iisquare.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService extends ServiceBase {

    @Autowired
    private UserDao userDao;
    @Autowired
    private StringRedisTemplate redis;

    public String validate(String id) {
        return ValidateUtil.filterSimpleString(id, true, 6, 32, null);
    }

    public String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public User info(String id) {
        if (null == id) return null;
        Optional<User> info = userDao.findById(id);
        return info.isPresent() ? info.get() : null;
    }

    public String tokenKey(String token) {
        return "im:chat:token:" + token;
    }

    public String token(String id, boolean renew) {
        User user = info(id);
        if (!renew && null != user) return user.getToken();
        if (null == user) {
            user = User.builder().id(id).build();
        } else {
            redis.delete(tokenKey(user.getToken()));
        }
        user.setToken(uuid());
        user = userDao.save(user);
        redis.opsForHash().putAll(tokenKey(user.getToken()), DPUtil.buildMap("id", user.getId(), "block", user.block()));
        return user.getToken();
    }

    public String userId(String token) {
        List<Object> list = redis.opsForHash().multiGet(tokenKey(token), Arrays.asList("id", "block"));
        if (DPUtil.parseLong(list.get(1)) > System.currentTimeMillis()) return "";
        return DPUtil.parseString(list.get(0));
    }

    public boolean block(String id, Date block) {
        User user = info(id);
        if (null == user) return false;
        user.setBlock(block);
        user = userDao.save(user);
        redis.opsForHash().putAll(tokenKey(user.getToken()), DPUtil.buildMap("id", user.getId(), "block", user.block()));
        return true;
    }

    public boolean pushable(String id, boolean enable) {
        User user = info(id);
        if (null == user) return false;
        user.setPushable(enable ? 1 : 0);
        user = userDao.save(user);
        redis.opsForHash().putAll(tokenKey(user.getToken()), DPUtil.buildMap("id", user.getId(), "pushable", user.pushable()));
        return true;
    }

}
