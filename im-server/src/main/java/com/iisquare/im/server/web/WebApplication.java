package com.iisquare.im.server.web;

import com.iisquare.im.server.api.mvc.BeanNameGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.iisquare.im.server.api.entity"})
@EnableJpaRepositories(basePackages = {"com.iisquare.im.server.api.dao"})
@ComponentScan(basePackages = {
    "com.iisquare.im.server.api",
    "com.iisquare.im.server.broker",
    "com.iisquare.im.server.web"
})
@EnableFeignClients(basePackages = {"com.iisquare.im.server.api.rpc"})
public class WebApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(WebApplication.class).beanNameGenerator(new BeanNameGenerator()).run(args);
    }

}
