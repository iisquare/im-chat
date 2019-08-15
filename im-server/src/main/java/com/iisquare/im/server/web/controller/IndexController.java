package com.iisquare.im.server.web.controller;

import com.iisquare.im.server.api.rpc.UserRpc;
import com.iisquare.im.server.core.util.ApiUtil;
import com.iisquare.im.server.core.util.DPUtil;
import com.iisquare.im.server.web.mvc.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/")
@RestController
public class IndexController extends WebController {

    @Autowired
    private UserRpc userRpc;

    @RequestMapping("/")
    public String indexAction(ModelMap model, HttpServletRequest request) {
        return ApiUtil.echoResult(0, "it's work!", null);
    }

}
