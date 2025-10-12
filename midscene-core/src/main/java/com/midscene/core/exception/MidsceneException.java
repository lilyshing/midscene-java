package com.midscene.core.exception;

/**
 * 框架基础异常类
 */
public class MidsceneException extends RuntimeException {
    public MidsceneException(String message) {
        super(message);
    }

    public MidsceneException(String message, Throwable cause) {
        super(message, cause);
    }
}