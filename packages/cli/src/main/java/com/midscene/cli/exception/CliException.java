package com.midscene.cli.exception;

/**
 * CLI异常类
 * 用于表示CLI执行过程中的各种错误情况
 */
public class CliException extends RuntimeException {
    
    /**
     * 构造函数
     */
    public CliException(String message) {
        super(message);
    }
    
    /**
     * 构造函数，包含原因
     */
    public CliException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数，仅包含原因
     */
    public CliException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 带有详细信息的构造函数
     */
    public CliException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}