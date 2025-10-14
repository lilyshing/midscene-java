package com.midscene.core.exception;

/**
 * Midscene框架的基础异常类
 */
public class MidsceneException extends RuntimeException {
    private final String errorCode;
    private final boolean retryable;
    
    public MidsceneException(String message) {
        this(message, null, false);
    }
    
    public MidsceneException(String message, String errorCode) {
        this(message, errorCode, false);
    }
    
    public MidsceneException(String message, Throwable cause) {
        this(message, null, cause, false);
    }
    
    public MidsceneException(String message, String errorCode, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public MidsceneException(String message, String errorCode, Throwable cause, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
}