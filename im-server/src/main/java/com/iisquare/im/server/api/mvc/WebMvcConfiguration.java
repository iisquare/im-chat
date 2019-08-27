package com.iisquare.im.server.api.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

/**
 * WebMvcConfigurationSupport只能有一个实例
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Value("${spring.http.encoding.charset}")
    private String charset;
    @Autowired
    private StringRedisTemplate redis;

    /**
     * 线程之间默认共享Redis连接句柄
     * 暂不支持通过配置文件定义shareNativeConnection参数：https://github.com/spring-projects/spring-boot/pull/14217
     * 目前只有此处用到Redis，可通过自定义Factory重写该处逻辑：https://github.com/spring-projects/spring-boot/issues/14196
     */
    @PostConstruct
    public void initialize() {
        LettuceConnectionFactory factory = (LettuceConnectionFactory) redis.getConnectionFactory();
        factory.setShareNativeConnection(false);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/", "classpath:/public/");
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        StringHttpMessageConverter convert = new StringHttpMessageConverter(Charset.forName(charset));
        convert.setWriteAcceptCharset(false);
        converters.add(convert);
        converters.add(new MappingJackson2HttpMessageConverter());
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        super.addCorsMappings(registry);
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "DELETE", "PUT")
            .maxAge(3600);
    }

}
