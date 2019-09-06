package com.iisquare.im.server.broker;

import com.iisquare.im.protobuf.IM;
import com.iisquare.im.protobuf.IMUser;
import com.iisquare.im.server.broker.core.Handler;
import com.iisquare.im.server.broker.logic.MessageLogic;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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

    private IM.Directive auth(String token, boolean withSyn) {
        IM.Directive.Builder directive = IM.Directive.newBuilder();
        directive.setSequence(MessageLogic.SEQUENCE_AUTH);
        IMUser.Auth.Builder auth = IMUser.Auth.newBuilder();
        auth.setToken(token).setWithSyn(withSyn);
        directive.setCommand("user.auth").setParameter(auth.build().toByteString());
        return directive.build();
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
        String[] strings = req.uri().split("\\?");
        String uri = strings[0];
        Map<String, String> param = new LinkedHashMap<>();
        if (strings.length > 1) {
            for (String str : strings[1].split("&")) {
                String[] split = str.split("=");
                param.put(split[0], split.length > 1 ? split[1] : "");
            }
        }
        String token = param.get("token");
        switch (uri) {
            case COMET_PUSH:
            case COMET_PULL:
                boolean isPull = COMET_PULL.endsWith(uri);
                sendCometFirst(ctx, req);
                IM.Result result = userLogic.authAction(MESSAGE_FROM_TYPE_COMET, ctx, auth(token, isPull));
                if (result.getCode() != 0) {
                    sendCometContent(ctx, result.toByteArray(), true);
                    return;
                }
                if (isPull) return;
                this.onReceive(MESSAGE_FROM_TYPE_COMET, ctx, req.content());
                sendCometLast(ctx);
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
                    this.onAccept(MESSAGE_FROM_TYPE_WS, ctx);
                    this.onReceive(MESSAGE_FROM_TYPE_WS, ctx, Unpooled.wrappedBuffer(auth(token, true).toByteArray()));
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
            this.onClose(MESSAGE_FROM_TYPE_WS, ctx);
        } else if (frame instanceof PingWebSocketFrame) { // 判断是否是Ping消息
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof BinaryWebSocketFrame) {
            this.onReceive(MESSAGE_FROM_TYPE_WS, ctx, frame.content());
        }
    }

}
