package com.midscene.core.exception;

/**
 * AI模型调用异常
 */
public class AIModelException extends MidsceneException {
    public AIModelException(String message) {
        super(message);
    }

    public AIModelException(String message, Throwable cause) {
        super(message, cause);
    }
}