package com.midscene.playground.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 命令请求模型
 * 接收前端发送的命令请求
 */
public class CommandRequest {
    private String sessionId;
    private String commandType;
    private JsonNode parameters;
    private boolean async = false;
    private int timeoutMs = 30000; // 默认超时30秒

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public JsonNode getParameters() {
        return parameters;
    }

    public void setParameters(JsonNode parameters) {
        this.parameters = parameters;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}