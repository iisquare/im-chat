package com.iisquare.im.server.api.service;

import com.iisquare.im.server.api.dao.UserDao;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.mvc.ServiceBase;
import com.iisquare.util.DPUtil;
import com.iisquare.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
public class UserService extends ServiceBase {

    @Autowired
    private UserDao userDao;
    @Autowired
    private StringRedisTemplate redis;

    public Map<String, Object> search(Map<?, ?> param, Map<?, ?> config) {
        Map<String, Object> result = new LinkedHashMap<>();
        int page = ValidateUtil.filterInteger(param.get("page"), true, 1, null, 1);
        int pageSize = ValidateUtil.filterInteger(param.get("pageSize"), true, 1, 500, 15);
        Sort sort = sort(DPUtil.parseString(param.get("sort")), Arrays.asList("id"));
        if (null == sort) sort = Sort.by(Sort.Order.asc("id"));
        Page<User> data = userDao.findAll((Specification) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String id = DPUtil.trim(DPUtil.parseString(param.get("id")));
            if(!DPUtil.empty(id)) {
                predicates.add(cb.like(root.get("id"), "%" + id + "%"));
            }
            String except = DPUtil.trim(DPUtil.parseString(param.get("except")));
            if(!DPUtil.empty(except)) {
                predicates.add(cb.notEqual(root.get("id"), except));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        }, PageRequest.of(page - 1, pageSize, sort));
        List<User> rows = data.getContent();
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", data.getTotalElements());
        result.put("rows", rows);
        return result;
    }

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

    public String userKey(String id) {
        return "im:chat:user:" + id;
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
        redis.opsForHash().putAll(tokenKey(user.getToken()), DPUtil.buildMap("id", user.getId()));
        return user.getToken();
    }

    public String userId(String token) {
        if (null == token) return null;
        List<Object> list = redis.opsForHash().multiGet(tokenKey(token), Arrays.asList("id"));
        return DPUtil.parseString(list.get(0));
    }

    public boolean block(String id, Date block) {
        User user = info(id);
        if (null == user) return false;
        user.setBlock(block);
        user = userDao.save(user);
        redis.opsForHash().putAll(userKey(user.getId()), DPUtil.buildMap("id", user.getId(), "block", user.block()));
        return true;
    }

    public boolean pushable(String id, boolean enable) {
        User user = info(id);
        if (null == user) return false;
        user.setPushable(enable ? 1 : 0);
        user = userDao.save(user);
        redis.opsForHash().putAll(userKey(user.getId()), DPUtil.buildMap("id", user.getId(), "pushable", user.pushable()));
        return true;
    }

    public boolean version(String id, long version) {
        User user = info(id);
        if (null == user) return false;
        redis.opsForHash().putAll(userKey(user.getId()), DPUtil.buildMap("id", user.getId(), "version", version));
        return true;
    }

    public long version(String id) {
        return DPUtil.parseLong(redis.opsForHash().get(userKey(id), "version"));
    }

}
