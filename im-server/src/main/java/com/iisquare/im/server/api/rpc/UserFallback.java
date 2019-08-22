package com.iisquare.im.server.api.rpc;

import com.iisquare.util.ApiUtil;
import org.springframework.stereotype.Component;

@Component
public class UserFallback implements UserRpc {
    @Override
    public String info(String tocken) {
        return ApiUtil.echoResult(500, "user info fallback", tocken);
    }
}
