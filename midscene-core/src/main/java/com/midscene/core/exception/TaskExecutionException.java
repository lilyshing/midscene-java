package com.midscene.core.exception;

/**
 * 任务执行异常
 */
public class TaskExecutionException extends MidsceneException {
    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}