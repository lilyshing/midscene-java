package com.midscene.core.exception;

/**
 * UI元素未找到异常
 */
public class ElementNotFoundException extends MidsceneException {
    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}