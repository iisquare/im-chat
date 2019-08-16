package com.iisquare.im.server.web.controller;

import com.iisquare.im.server.api.service.UserService;
import com.iisquare.im.server.core.util.ApiUtil;
import com.iisquare.im.server.core.util.DPUtil;
import com.iisquare.im.server.web.mvc.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserController extends WebController {

    @Autowired
    private UserService userService;

    @PostMapping("/token")
    public String tokenAction(@RequestBody Map<String, Object> param) {
        String userId = userService.validate(DPUtil.parseString(param.get("userId")));
        if (null == userId) return ApiUtil.echoResult(1001, "用户标识不满足要求", userId);
        String token = userService.token(userId, !DPUtil.empty(param.get("renew")));
        return ApiUtil.echoResult(0, null, DPUtil.buildMap("userId", userId, "token", token));
    }

}
