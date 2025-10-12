package com.midscene.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行结果
 */
public class TaskResult {
    private TaskStatus status;
    private List<StepResult> steps = new ArrayList<>();
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double executionTimeMs;

    public TaskResult() {
        this.status = TaskStatus.PENDING;
        this.startTime = LocalDateTime.now();
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<StepResult> getSteps() {
        return steps;
    }

    public void setSteps(List<StepResult> steps) {
        this.steps = steps;
    }

    public void addStep(StepResult step) {
        this.steps.add(step);
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

    /**
     * 完成任务
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        calculateExecutionTime();
    }

    /**
     * 失败任务
     */
    public void fail(String errorMessage) {
        this.status = TaskStatus.FAILED;
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
        return "TaskResult{" +
                "status=" + status +
                ", stepCount=" + steps.size() +
                ", executionTimeMs=" + executionTimeMs +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}