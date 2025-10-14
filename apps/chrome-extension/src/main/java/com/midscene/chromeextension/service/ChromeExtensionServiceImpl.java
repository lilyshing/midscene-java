package com.midscene.chromeextension.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChromeExtensionServiceImpl implements ChromeExtensionService {

    @Value("${chrome.extension.id}")
    private String extensionId;

    @Value("${chrome.extension.version}")
    private String extensionVersion;

    @Value("${chrome.extension.name}")
    private String extensionName;

    // 存储录制会话
    private final Map<String, RecordingSession> recordingSessions = new ConcurrentHashMap<>();
    
    // 存储扩展配置
    private final Map<String, String> extensionConfig = new ConcurrentHashMap<>();

    @Override
    public void initializeBridgeService() {
        // 初始化扩展桥接服务
        System.out.println("Initializing Chrome Extension Bridge Service...");
        
        // 加载默认配置
        extensionConfig.put("bridgeTimeout", "30000");
        extensionConfig.put("maxRecordingTime", "3600000"); // 1 hour
        extensionConfig.put("captureScreenshots", "true");
        extensionConfig.put("captureNetwork", "false");
        
        System.out.println("Chrome Extension Bridge Service initialized");
    }

    @Override
    public CompletableFuture<Map<String, Object>> processExtensionMessage(Map<String, Object> message) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> response = new HashMap<>();
            String messageType = message.getOrDefault("type", "").toString();
            
            switch (messageType) {
                case "recording_start":
                    String recordingId = startRecording(message);
                    response.put("status", "success");
                    response.put("recordingId", recordingId);
                    break;
                case "recording_stop":
                    String stopRecordingId = message.getOrDefault("recordingId", "").toString();
                    Map<String, Object> recordingResult = stopRecording(stopRecordingId);
                    response.put("status", "success");
                    response.put("result", recordingResult);
                    break;
                case "ping":
                    response.put("status", "success");
                    response.put("message", "pong");
                    break;
                default:
                    response.put("status", "error");
                    response.put("message", "Unknown message type: " + messageType);
            }
            
            response.put("timestamp", System.currentTimeMillis());
            return response;
        });
    }

    @Override
    public String startRecording(Map<String, Object> options) {
        String recordingId = UUID.randomUUID().toString();
        RecordingSession session = new RecordingSession(recordingId, options);
        recordingSessions.put(recordingId, session);
        
        System.out.println("Started recording session: " + recordingId);
        return recordingId;
    }

    @Override
    public Map<String, Object> stopRecording(String recordingId) {
        RecordingSession session = recordingSessions.remove(recordingId);
        Map<String, Object> result = new HashMap<>();
        
        if (session != null) {
            session.stop();
            result.put("status", "completed");
            result.put("recordingId", recordingId);
            result.put("duration", session.getDuration());
            result.put("eventsCaptured", session.getEventsCaptured());
            System.out.println("Stopped recording session: " + recordingId);
        } else {
            result.put("status", "error");
            result.put("message", "Recording session not found: " + recordingId);
        }
        
        return result;
    }

    @Override
    public Map<String, String> getExtensionConfig() {
        return new HashMap<>(extensionConfig);
    }

    @Override
    public boolean updateExtensionConfig(Map<String, String> config) {
        if (config != null) {
            extensionConfig.putAll(config);
            return true;
        }
        return false;
    }

    @Override
    public CompletableFuture<Boolean> sendMessageToExtension(Map<String, Object> message) {
        return CompletableFuture.supplyAsync(() -> {
            // 模拟向Chrome扩展发送消息
            System.out.println("Sending message to extension: " + message);
            // 实际实现中需要通过WebSocket或其他方式与浏览器扩展通信
            return true;
        });
    }

    // 录制会话内部类
    private static class RecordingSession {
        private final String id;
        private final Map<String, Object> options;
        private final long startTime;
        private long stopTime;
        private boolean isActive;
        private int eventsCaptured;

        public RecordingSession(String id, Map<String, Object> options) {
            this.id = id;
            this.options = new HashMap<>(options);
            this.startTime = System.currentTimeMillis();
            this.isActive = true;
            this.eventsCaptured = 0;
        }

        public void stop() {
            if (isActive) {
                this.stopTime = System.currentTimeMillis();
                this.isActive = false;
            }
        }

        public long getDuration() {
            if (isActive) {
                return System.currentTimeMillis() - startTime;
            }
            return stopTime - startTime;
        }

        public int getEventsCaptured() {
            return eventsCaptured;
        }

        public void incrementEvents() {
            this.eventsCaptured++;
        }
    }
}