package com.iisquare.im.server.api.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ne-user", url = "${server.address}", fallback = UserFallback.class)
public interface UserRpc {

    @RequestMapping(method = RequestMethod.GET, value = "/user/verify")
    String info(@RequestParam("AUT") String token);

}
