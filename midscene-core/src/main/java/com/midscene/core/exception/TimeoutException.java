package com.midscene.core.exception;

/**
 * 操作超时异常
 */
public class TimeoutException extends MidsceneException {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}