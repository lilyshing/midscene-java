package com.midscene.playground.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

/**
 * 执行结果模型
 * 存储命令执行的结果信息
 */
public class ExecutionResult {
    private String command;
    private boolean success;
    private String message;
    private JsonNode resultData;
    private String errorDetails;
    private LocalDateTime timestamp;
    private long executionTimeMs;

    public ExecutionResult() {
        this.timestamp = LocalDateTime.now();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getResultData() {
        return resultData;
    }

    public void setResultData(JsonNode resultData) {
        this.resultData = resultData;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * 创建成功的执行结果
     */
    public static ExecutionResult success(String command, String message, JsonNode resultData, long executionTimeMs) {
        ExecutionResult result = new ExecutionResult();
        result.setCommand(command);
        result.setSuccess(true);
        result.setMessage(message);
        result.setResultData(resultData);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    /**
     * 创建失败的执行结果
     */
    public static ExecutionResult failure(String command, String message, String errorDetails) {
        ExecutionResult result = new ExecutionResult();
        result.setCommand(command);
        result.setSuccess(false);
        result.setMessage(message);
        result.setErrorDetails(errorDetails);
        return result;
    }
}