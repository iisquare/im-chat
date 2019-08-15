package com.iisquare.im.server.api.mvc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Configuration {

    @Value("${server.address:0.0.0.0}")
    private String serverAddress;
    @Value("${server.port:8080}")
    private Integer serverPort;
    @Value("${socket.port:7002}")
    private Integer socketPort;
    @Value("${socket.thread:0}")
    private Integer socketThread;
    @Value("${internal.port:7003}")
    private Integer internalPort;
    @Value("${internal.thread:0}")
    private Integer internalThread;
    @Value("${http.port:7005}")
    private Integer httpPort;
    @Value("${http.thread:0}")
    private Integer httpThread;

}
