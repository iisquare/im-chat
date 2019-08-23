package com.iisquare.im.server.broker;

import com.iisquare.im.server.broker.core.Handler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
public class HttpHandler extends Handler {

    public static final String COMET_PUSH = "/push";
    public static final String COMET_PULL = "/pull";
    public static final String WEB_SOCKET = "/block";
    private static Map<Channel, WebSocketServerHandshaker> handshakers = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        if (req.method().equals(HttpMethod.OPTIONS)) {
            sendHttpResponse(ctx, req, null);
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
                    this.onAccept(ctx);
                }
                return;
            default:
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) { // 判断是否是关闭链路的指令
            WebSocketServerHandshaker handshaker = handshakers.remove(ctx.channel());
            if (null != handshaker) handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            this.onClose(ctx);
        } else if (frame instanceof PingWebSocketFrame) { // 判断是否是Ping消息
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof BinaryWebSocketFrame) {
            this.onReceive(MESSAGE_FROM_TYPE_WS, ctx, frame.content());
        }
    }

}
