package com.midscene.shared.platform;

/**
 * Agent操作异常
 * 用于表示Agent执行过程中发生的错误
 */
public class AgentException extends RuntimeException {
    public AgentException(String message) {
        super(message);
    }
    
    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}