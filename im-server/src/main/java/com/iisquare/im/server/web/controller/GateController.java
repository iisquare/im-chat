package com.iisquare.im.server.web.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iisquare.im.server.api.mvc.Configuration;
import com.iisquare.im.server.broker.HttpHandler;
import com.iisquare.util.ApiUtil;
import com.iisquare.util.DPUtil;
import com.iisquare.im.server.web.mvc.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/gate")
@RestController
public class GateController extends WebController {

    @Autowired
    private Configuration config;

    @GetMapping("/route")
    public String routeAction() {
        ObjectNode servers = DPUtil.objectNode();
        String node = config.address();
        servers.putArray("nodes").add(node);
        ObjectNode routes = servers.putObject("routes");
        ObjectNode route = routes.putObject(node);
        route.put("ws", "ws://" + node + ":" + config.getHttpPort() + HttpHandler.WEB_SOCKET);
        route.putObject("comet")
            .put("push", "http://" + node + ":" + config.getHttpPort() + HttpHandler.COMET_PUSH)
            .put("pull", "http://" + node + ":" + config.getHttpPort() + HttpHandler.COMET_PULL);
        route.putObject("tcp").put("server", node).put("port", config.getSocketPort());
        return ApiUtil.echoResult(0, null, servers);
    }

}
