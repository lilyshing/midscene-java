package com.midscene.androidplayground;

/**
 * Android Playground 异常类，用于表示 Playground 操作中的错误
 */
public class PlaygroundException extends Exception {
    
    /**
     * 创建新的 PlaygroundException
     * @param message 错误消息
     */
    public PlaygroundException(String message) {
        super(message);
    }
    
    /**
     * 创建新的 PlaygroundException，包含导致错误的原因
     * @param message 错误消息
     * @param cause 原始异常
     */
    public PlaygroundException(String message, Throwable cause) {
        super(message, cause);
    }
}