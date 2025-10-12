package com.midscene.core.model;

import java.time.LocalDateTime;

/**
 * 步骤执行结果
 */
public class StepResult {
    private String description;
    private StepStatus status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double executionTimeMs;
    private String targetElementInfo;
    private String actionInfo;

    public StepResult(String description) {
        this.description = description;
        this.status = StepStatus.PENDING;
        this.startTime = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getTargetElementInfo() {
        return targetElementInfo;
    }

    public void setTargetElementInfo(String targetElementInfo) {
        this.targetElementInfo = targetElementInfo;
    }

    public String getActionInfo() {
        return actionInfo;
    }

    public void setActionInfo(String actionInfo) {
        this.actionInfo = actionInfo;
    }

    /**
     * 开始执行步骤
     */
    public void start() {
        this.status = StepStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 完成步骤
     */
    public void complete() {
        this.status = StepStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        calculateExecutionTime();
    }

    /**
     * 失败步骤
     */
    public void fail(String errorMessage) {
        this.status = StepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        calculateExecutionTime();
    }

    /**
     * 计算执行时间
     */
    private void calculateExecutionTime() {
        if (this.startTime != null && this.endTime != null) {
            this.executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    @Override
    public String toString() {
        return "StepResult{" +
                "description='" + description + '\'' +
                ", status=" + status +
                ", executionTimeMs=" + executionTimeMs +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}