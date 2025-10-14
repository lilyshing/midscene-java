package com.midscene.playground.controller;

import com.midscene.playground.model.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 * 处理实时通信和事件推送
 */
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理客户端发送的消息
     */
    @MessageMapping("/playground.message")
    @SendTo("/topic/public")
    public String handleMessage(@Payload String message) {
        logger.info("Received message: {}", message);
        return "Server received: " + message;
    }

    /**
     * 广播执行结果到特定会话
     */
    public void broadcastExecutionResult(String sessionId, ExecutionResult result) {
        messagingTemplate.convertAndSend("/queue/sessions/" + sessionId, result);
    }

    /**
     * 广播系统事件
     */
    public void broadcastSystemEvent(String eventType, String message) {
        SystemEvent event = new SystemEvent(eventType, message);
        messagingTemplate.convertAndSend("/topic/system", event);
    }

    /**
     * 系统事件模型
     */
    public static class SystemEvent {
        private String eventType;
        private String message;
        private String timestamp;

        public SystemEvent(String eventType, String message) {
            this.eventType = eventType;
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        // Getters and setters
        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}