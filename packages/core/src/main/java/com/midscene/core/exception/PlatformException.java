package com.midscene.core.exception;

/**
 * 平台操作异常
 * 用于表示与平台交互时发生的错误
 */
public class PlatformException extends MidsceneException {
    public PlatformException(String message) {
        super(message, "PLATFORM_ERROR", true);
    }
    
    public PlatformException(String message, String errorCode) {
        super(message, errorCode, true);
    }
    
    public PlatformException(String message, Throwable cause) {
        super(message, "PLATFORM_ERROR", cause, true);
    }
    
    public PlatformException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause, true);
    }
}