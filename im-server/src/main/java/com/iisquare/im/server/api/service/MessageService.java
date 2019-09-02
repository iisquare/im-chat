package com.iisquare.im.server.api.service;

import com.iisquare.im.server.api.dao.MessageDao;
import com.iisquare.im.server.api.entity.Message;
import com.iisquare.im.server.api.entity.User;
import com.iisquare.im.server.api.mvc.ServiceBase;
import com.iisquare.util.DPUtil;
import com.iisquare.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
public class MessageService extends ServiceBase {

    @Autowired
    private MessageDao messageDao;

    public String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Message save(Message info) {
        info.setTime(new Date());
        if(null == info.getId()) info.setId(uuid());
        return messageDao.save(info);
    }

    public Map<String, Object> search(Map<?, ?> param, Map<?, ?> config) {
        Map<String, Object> result = new LinkedHashMap<>();
        int page = ValidateUtil.filterInteger(param.get("page"), true, 1, null, 1);
        int pageSize = ValidateUtil.filterInteger(param.get("pageSize"), true, 1, 500, 15);
        Sort sort = sort(DPUtil.parseString(param.get("sort")), Arrays.asList("version", "time"));
        if (null == sort) sort = Sort.by(Sort.Order.asc("version"));
        Page<User> data = messageDao.findAll((Specification) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (param.containsKey("minVersion")) {
                predicates.add(cb.ge(root.get("version"), DPUtil.parseLong(param.containsKey("minVersion"))));
            }
            if (param.containsKey("maxVersion")) {
                predicates.add(cb.le(root.get("version"), DPUtil.parseLong(param.containsKey("maxVersion"))));
            }
            if (param.containsKey("minTime")) {
                predicates.add(cb.ge(root.get("time"), DPUtil.parseLong(param.containsKey("minTime"))));
            }
            if (param.containsKey("maxTime")) {
                predicates.add(cb.le(root.get("time"), DPUtil.parseLong(param.containsKey("maxTime"))));
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

}
