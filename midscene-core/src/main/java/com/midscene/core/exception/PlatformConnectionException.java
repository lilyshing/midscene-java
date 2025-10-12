package com.midscene.core.exception;

/**
 * 平台连接异常
 */
public class PlatformConnectionException extends MidsceneException {
    public PlatformConnectionException(String message) {
        super(message);
    }

    public PlatformConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}