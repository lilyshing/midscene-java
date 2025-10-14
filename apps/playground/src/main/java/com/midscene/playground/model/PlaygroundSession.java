package com.midscene.playground.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Playground会话模型
 * 表示一个Playground的执行会话
 */
public class PlaygroundSession {
    private String sessionId;
    private String platformType;
    private String serverUrl;
    private LocalDateTime startTime;
    private LocalDateTime lastActivityTime;
    private SessionStatus status;
    private Map<String, Object> contextData = new HashMap<>();
    private String currentCommand;
    private ExecutionResult lastResult;

    public PlaygroundSession() {
        this.sessionId = java.util.UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.status = SessionStatus.IDLE;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData;
    }

    public String getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(String currentCommand) {
        this.currentCommand = currentCommand;
    }

    public ExecutionResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(ExecutionResult lastResult) {
        this.lastResult = lastResult;
    }

    public enum SessionStatus {
        IDLE, RUNNING, PAUSED, COMPLETED, ERROR
    }
}