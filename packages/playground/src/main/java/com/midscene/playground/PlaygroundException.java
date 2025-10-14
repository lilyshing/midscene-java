package com.midscene.playground;

/**
 * Playground 异常类，用于表示 Playground 模块中的错误
 */
public class PlaygroundException extends RuntimeException {

    /**
     * 使用指定的错误消息构造异常
     * @param message 错误消息
     */
    public PlaygroundException(String message) {
        super(message);
    }

    /**
     * 使用指定的错误消息和原因构造异常
     * @param message 错误消息
     * @param cause 异常原因
     */
    public PlaygroundException(String message, Throwable cause) {
        super(message, cause);
    }
}