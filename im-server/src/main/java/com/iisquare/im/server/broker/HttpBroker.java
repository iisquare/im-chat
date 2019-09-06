package com.iisquare.im.server.broker;

import com.iisquare.im.server.api.mvc.Configuration;
import com.iisquare.im.server.broker.core.Broker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class HttpBroker extends Broker implements Runnable {

    protected final static Logger logger = LoggerFactory.getLogger(HttpBroker.class);
    @Autowired
    private Configuration configuration;
    @Autowired
    private ServerHandler handler;
    private ServerBootstrap bootstrap;

    public HttpBroker() {
        this.bootstrap = new ServerBootstrap();
        this.bootstrap.channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("http-codec", new HttpServerCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                    pipeline.addLast(handler);
                }
            }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @PostConstruct
    public void backend() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(configuration.getHttpThread());
        try {
            ChannelFuture future = bootstrap.group(bossGroup, workerGroup)
                .bind(configuration.getServerAddress(), configuration.getHttpPort()).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.debug("http broker catch exception", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
