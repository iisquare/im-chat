package com.iisquare.im.server.broker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class HttpHandler extends ChannelInboundHandlerAdapter {

    protected final static Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    private static Map<Channel, WebSocketServerHandshaker> handshakers = new ConcurrentHashMap<>();
    public static final String COMET_PUSH = "/push";
    public static final String COMET_PULL = "/pull";
    public static final String WEB_SOCKET = "/block";
    @Autowired
    private Dispatcher dispatcher;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    public void write(ChannelHandlerContext ctx, FullHttpRequest req, String content) throws UnsupportedEncodingException {
        if (null == content) content = "";
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(content.getBytes("UTF-8")));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,DELETE");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        sendHttpResponse(ctx, req, response);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        if (req.method().equals(HttpMethod.OPTIONS)) {
            write(ctx, req, null);
            return;
        }
        switch (req.uri()) {
            case COMET_PUSH:
                return;
            case COMET_PULL:
                return;
            case WEB_SOCKET:
                if (!"websocket".equals(req.headers().get("Upgrade"))) {
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                    return;
                }
                // 构造握手响应返回，本机测试
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET, null, false);
                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), req);
                    handshakers.put(ctx.channel(), handshaker);
                }
                return;
            default:
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) { // 判断是否是关闭链路的指令
            handshakers.remove(ctx.channel()).close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        } else if (frame instanceof PingWebSocketFrame) { // 判断是否是Ping消息
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        } else {
            if (frame instanceof BinaryWebSocketFrame) {
                dispatcher.dispatch(ctx, (BinaryWebSocketFrame) frame);
            }
            String message = (frame instanceof TextWebSocketFrame) ? ((TextWebSocketFrame) frame).text() : null;
            if (!ctx.channel().hasAttr(AttributeKey.valueOf("xxx"))) {
                ctx.channel().attr(AttributeKey.valueOf("xxx")).set(message);
            }
            String name = ctx.channel().attr(AttributeKey.valueOf("xxx")).get().toString();
            for (Map.Entry<Channel, WebSocketServerHandshaker> entry : handshakers.entrySet()) {
                entry.getKey().write(new TextWebSocketFrame(name + "@" + message));
            }
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
