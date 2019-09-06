package com.iisquare.im.server.test;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.broker.logic.MessageLogic;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ImTester implements Runnable {

    protected final static Logger logger = LoggerFactory.getLogger(ImTester.class);
    private Bootstrap bootstrap;
    public static Channel channel = null;
    public static CountDownLatch startLatch = new CountDownLatch(1);
    public static CountDownLatch stopLatch = new CountDownLatch(1);

    public ImTester() {
        this.bootstrap = new Bootstrap();
        this.bootstrap.channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
                    pipeline.addLast("encoder", new LengthFieldPrepender(2));
                    pipeline.addLast(new ClientHandler());
                }
            }).option(ChannelOption.TCP_NODELAY, true);
    }

    @Before
    public void start() throws InterruptedException {
        new Thread(this).start();
        startLatch.await();
    }

    @After
    public void stop() throws InterruptedException {
        stopLatch.await();
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ChannelFuture future = bootstrap.group(bossGroup).connect("127.0.0.1", 7002).sync();
            channel = future.channel();
            startLatch.countDown();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.debug("socket broker catch exception", e);
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    @Test
    public void authTest() throws InterruptedException {
        IM.Directive.Builder directive = IM.Directive.newBuilder();
        directive.setSequence(MessageLogic.SEQUENCE_AUTH);
        IMUser.Auth.Builder auth = IMUser.Auth.newBuilder();
        auth.setToken("52805e717fce4420a8d6e7e1d9554bef").setWithSyn(false);
        directive.setCommand("user.auth").setParameter(auth.build().toByteString());
        channel.writeAndFlush(Unpooled.wrappedBuffer(directive.build().toByteArray()));
    }

}

class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            IM.Result result = IM.Result.parseFrom(((ByteBuf) msg).nioBuffer());
            System.out.println(result);
            System.out.println(IMUser.AuthResult.parseFrom(result.getData()));
        } else {
            System.out.println(msg);
        }
        ctx.close();
        ImTester.stopLatch.countDown();
    }
}
