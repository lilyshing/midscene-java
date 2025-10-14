package com.midscene.core.model;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    /**
     * 任务已完成
     */
    COMPLETED,
    
    /**
     * 任务成功（与COMPLETED同义）
     */
    SUCCESS,
    
    /**
     * 任务失败
     */
    FAILED,
    
    /**
     * 任务正在执行
     */
    IN_PROGRESS,
    
    /**
     * 任务已取消
     */
    CANCELLED,
    
    /**
     * 任务已超时
     */
    TIMEOUT,
    
    /**
     * 任务等待中
     */
    PENDING
}