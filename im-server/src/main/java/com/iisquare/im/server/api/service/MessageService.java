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
        info.setTime(System.currentTimeMillis());
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
            String sender = DPUtil.trim(DPUtil.parseString(param.get("sender")));
            if(!DPUtil.empty(sender)) predicates.add(cb.equal(root.get("sender"), sender));
            String reception = DPUtil.trim(DPUtil.parseString(param.get("reception")));
            if(!DPUtil.empty(reception)) predicates.add(cb.equal(root.get("reception"), reception));
            String receiver = DPUtil.trim(DPUtil.parseString(param.get("receiver")));
            if(!DPUtil.empty(receiver)) predicates.add(cb.equal(root.get("receiver"), receiver));
            String cs = DPUtil.trim(DPUtil.parseString(param.get("cs")));
            String cr = DPUtil.trim(DPUtil.parseString(param.get("cr")));
            if(!DPUtil.empty(cs) && !DPUtil.empty(cr)) { // 单个会话的记录
                predicates.add(cb.or(
                    cb.and(cb.equal(root.get("sender"), cs), cb.equal(root.get("receiver"), cr)),
                    cb.and(cb.equal(root.get("sender"), cr), cb.equal(root.get("receiver"), cs))
                ));
            }
            String ca = DPUtil.trim(DPUtil.parseString(param.get("ca")));
            if(!DPUtil.empty(ca)) { // 单个用户的记录
                predicates.add(cb.or(cb.equal(root.get("sender"), ca), cb.equal(root.get("receiver"), ca)));
            }
            long minVersion = DPUtil.parseLong(param.containsKey("minVersion"));
            if (minVersion > 0) predicates.add(cb.ge(root.get("version"),minVersion ));
            long maxVersion = DPUtil.parseLong(param.containsKey("maxVersion"));
            if (maxVersion > 0) predicates.add(cb.le(root.get("version"), maxVersion));
            long minTime = DPUtil.parseLong(param.containsKey("minTime"));
            if (minTime > 0) predicates.add(cb.ge(root.get("time"), minTime));
            long maxTime = DPUtil.parseLong(param.containsKey("maxTime"));
            if (maxTime > 0) predicates.add(cb.le(root.get("time"), maxTime));
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
