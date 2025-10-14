package com.midscene.visualizer.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试执行类
 * 记录单个测试操作的执行信息
 */
public class TestExecution {
    private String id;
    private String actionName;
    private String target;
    private String targetType;
    private String command;
    private Map<String, Object> parameters;
    private boolean success;
    private String result;
    private String errorMessage;
    private long executionTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String screenshotId;
    private Map<String, Object> metadata;
    private String threadId;
    private String sessionId;
    
    /**
     * 构造函数
     */
    public TestExecution() {
        this.id = "execution-" + System.currentTimeMillis();
        this.parameters = new HashMap<>();
        this.metadata = new HashMap<>();
        this.startTime = LocalDateTime.now();
        this.threadId = String.valueOf(Thread.currentThread().getId());
    }
    
    /**
     * 构造函数
     */
    public TestExecution(String actionName, String target) {
        this();
        this.actionName = actionName;
        this.target = target;
    }
    
    /**
     * 获取执行ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取操作名称
     */
    public String getActionName() {
        return actionName;
    }
    
    /**
     * 设置操作名称
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    /**
     * 获取目标
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * 设置目标
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * 获取目标类型
     */
    public String getTargetType() {
        return targetType;
    }
    
    /**
     * 设置目标类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    /**
     * 获取命令
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * 设置命令
     */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /**
     * 获取参数
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    /**
     * 设置参数
     */
    public void setParameter(String key, Object value) {
        this.parameters.put(key, value);
    }
    
    /**
     * 获取参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key) {
        return (T) this.parameters.get(key);
    }
    
    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 设置成功状态
     */
    public void setSuccess(boolean success) {
        this.success = success;
        if (this.endTime == null) {
            endExecution();
        }
    }
    
    /**
     * 获取结果
     */
    public String getResult() {
        return result;
    }
    
    /**
     * 设置结果
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 设置错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        if (this.endTime == null) {
            endExecution();
        }
    }
    
    /**
     * 获取执行时间（毫秒）
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * 获取开始时间
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * 获取结束时间
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * 结束执行
     */
    public void endExecution() {
        this.endTime = LocalDateTime.now();
        this.executionTimeMs = calculateExecutionTime();
    }
    
    /**
     * 计算执行时间
     */
    private long calculateExecutionTime() {
        if (endTime == null) {
            return 0;
        }
        return startTime.until(endTime, java.time.temporal.ChronoUnit.MILLIS);
    }
    
    /**
     * 获取截图ID
     */
    public String getScreenshotId() {
        return screenshotId;
    }
    
    /**
     * 设置截图ID
     */
    public void setScreenshotId(String screenshotId) {
        this.screenshotId = screenshotId;
    }
    
    /**
     * 获取元数据
     */
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    /**
     * 设置元数据
     */
    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * 获取线程ID
     */
    public String getThreadId() {
        return threadId;
    }
    
    /**
     * 设置线程ID
     */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
    
    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 设置会话ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * 检查是否有错误
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
    
    /**
     * 获取操作摘要
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(actionName).append(" on ").append(target);
        if (!success) {
            sb.append(" (FAILED)");
        }
        sb.append(" - ").append(executionTimeMs).append("ms");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TestExecution{\n");
        sb.append("  id='").append(id).append("'\n");
        sb.append("  actionName='").append(actionName).append("'\n");
        sb.append("  target='").append(target).append("'\n");
        sb.append("  success=").append(success).append("\n");
        sb.append("  executionTimeMs=").append(executionTimeMs).append("\n");
        if (hasError()) {
            sb.append("  errorMessage='").append(errorMessage).append("'\n");
        }
        sb.append("  startTime=").append(startTime).append("\n");
        sb.append("  endTime=").append(endTime).append("\n");
        sb.append("}");
        return sb.toString();
    }
}