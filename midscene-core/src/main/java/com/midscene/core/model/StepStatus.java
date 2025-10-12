package com.midscene.core.model;

/**
 * 步骤执行状态枚举
 */
public enum StepStatus {
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
     * 步骤被跳过
     */
    SKIPPED
}