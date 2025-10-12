package com.midscene.core.model;

/**
 * 任务执行状态枚举
 */
public enum TaskStatus {
    /**
     * 等待执行
     */
    PENDING,
    
    /**
     * 执行中
     */
    RUNNING,
    
    /**
     * 执行完成
     */
    COMPLETED,
    
    /**
     * 执行失败
     */
    FAILED,
    
    /**
     * 执行被取消
     */
    CANCELLED
}