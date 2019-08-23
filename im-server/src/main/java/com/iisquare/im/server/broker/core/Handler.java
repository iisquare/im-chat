package com.iisquare.im.server.broker.core;

import com.iisquare.im.protobuf.IM;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;

public abstract class Handler extends ChannelInboundHandlerAdapter {

    public static final String MESSAGE_FROM_TYPE_WS = "ws";
    public static final String MESSAGE_FROM_TYPE_COMET = "comet";
    public static final String MESSAGE_FROM_TYPE_SOCKET = "socket";

    @Autowired
    protected Dispatcher dispatcher;

    public void onAccept(ChannelHandlerContext ctx) {

    }

    public void onClose(ChannelHandlerContext ctx) {

    }

    public void onReceive(String fromType, ChannelHandlerContext ctx, ByteBuf message) {
        IM.Result result = dispatcher.dispatch(ctx, message);
        if (null == result) return;
        switch (fromType) {
            case MESSAGE_FROM_TYPE_WS:
                ctx.channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(result.toByteArray())));
                return;
            case MESSAGE_FROM_TYPE_COMET:
                return;
            case MESSAGE_FROM_TYPE_SOCKET:
                return;
        }
    }

    public void sendHttpContent(ChannelHandlerContext ctx, FullHttpRequest req, String content) throws UnsupportedEncodingException {
        if (null == content) content = "";
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(content.getBytes("UTF-8")));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,DELETE");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        this.sendHttpResponse(ctx, req, response);
    }

    public void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            try {
                res.content().writeBytes(buf);
            } finally {
                ReferenceCountUtil.release(buf);
            }
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
