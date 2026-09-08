package com.evrental.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * 维护用户连接，支持单播和广播
 *
 * <p>使用方式：</p>
 * <ul>
 *   <li>注入 NotificationHandler</li>
 *   <li>调用 sendToUser(userId, message) 发送给指定用户</li>
 *   <li>调用 broadcast(message) 发送给所有在线用户</li>
 * </ul>
 */
@Slf4j
@org.springframework.stereotype.Component
public class NotificationHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket连接建立, userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("WebSocket连接关闭, userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("收到消息: {}", message.getPayload());
    }

    /** 发送消息给指定用户 */
    public void sendToUser(Long userId, String message) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败, userId={}", userId, e);
            }
        }
    }

    /** 广播消息给所有人 */
    public void broadcast(String message) {
        SESSIONS.forEach((id, s) -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(message));
                } catch (IOException ignored) {
                }
            }
        });
    }

    /** 获取当前在线用户数 */
    public int getOnlineCount() {
        return SESSIONS.size();
    }
}
