package com.midscene.core.exception;

import com.midscene.core.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * 异常工具类 - 提供统一的异常处理功能
 */
public class ExceptionUtil {
    
    /**
     * 创建AI模型异常
     * @param message 异常消息
     * @return AI模型异常实例
     */
    public static AIModelException createAIModelException(String message) {
        return new AIModelException(message);
    }
    
    /**
     * 创建AI模型异常（带原因）
     * @param message 异常消息
     * @param cause 原始异常
     * @return AI模型异常实例
     */
    public static AIModelException createAIModelException(String message, Throwable cause) {
        return new AIModelException(message, cause);
    }
    
    /**
     * 创建元素未找到异常
     * @param selector 选择器
     * @return 元素未找到异常实例
     */
    public static ElementNotFoundException createElementNotFoundException(String selector) {
        return new ElementNotFoundException("Element not found: " + selector);
    }
    
    /**
     * 创建元素未找到异常（带原因）
     * @param selector 选择器
     * @param cause 原始异常
     * @return 元素未找到异常实例
     */
    public static ElementNotFoundException createElementNotFoundException(String selector, Throwable cause) {
        return new ElementNotFoundException("Element not found: " + selector, cause);
    }
    
    /**
     * 创建平台连接异常
     * @param platform 平台名称
     * @param message 异常消息
     * @return 平台连接异常实例
     */
    public static PlatformConnectionException createPlatformConnectionException(String platform, String message) {
        return new PlatformConnectionException("Failed to connect to " + platform + ": " + message);
    }
    
    /**
     * 创建平台连接异常（带原因）
     * @param platform 平台名称
     * @param message 异常消息
     * @param cause 原始异常
     * @return 平台连接异常实例
     */
    public static PlatformConnectionException createPlatformConnectionException(String platform, String message, Throwable cause) {
        return new PlatformConnectionException("Failed to connect to " + platform + ": " + message, cause);
    }
    
    /**
     * 创建任务执行异常
     * @param taskId 任务ID
     * @param message 异常消息
     * @return 任务执行异常实例
     */
    public static TaskExecutionException createTaskExecutionException(String taskId, String message) {
        return new TaskExecutionException("Task " + taskId + " failed: " + message);
    }
    
    /**
     * 创建任务执行异常（带原因）
     * @param taskId 任务ID
     * @param message 异常消息
     * @param cause 原始异常
     * @return 任务执行异常实例
     */
    public static TaskExecutionException createTaskExecutionException(String taskId, String message, Throwable cause) {
        return new TaskExecutionException("Task " + taskId + " failed: " + message, cause);
    }
    
    /**
     * 创建超时异常
     * @param operation 操作名称
     * @param timeoutMs 超时时间（毫秒）
     * @return 超时异常实例
     */
    public static TimeoutException createTimeoutException(String operation, long timeoutMs) {
        return new TimeoutException("Operation timed out after " + timeoutMs + "ms: " + operation);
    }
    
    /**
     * 创建超时异常（带原因）
     * @param operation 操作名称
     * @param timeoutMs 超时时间（毫秒）
     * @param cause 原始异常
     * @return 超时异常实例
     */
    public static TimeoutException createTimeoutException(String operation, long timeoutMs, Throwable cause) {
        return new TimeoutException("Operation timed out after " + timeoutMs + "ms: " + operation, cause);
    }
    
    /**
     * 记录异常并抛出
     * @param logger Logger实例
     * @param exception 要抛出的异常
     * @param <T> 异常类型
     * @return 异常实例（不会实际返回，因为会被抛出）
     * @throws T 抛出指定类型的异常
     */
    public static <T extends Throwable> T logAndThrow(Logger logger, T exception) throws T {
        if (exception instanceof MidsceneException) {
            LoggerUtil.error(logger, exception.getMessage(), exception);
        } else {
            LoggerUtil.error(logger, "Unexpected error occurred", exception);
        }
        throw exception;
    }
    
    /**
     * 处理并转换异常
     * @param logger Logger实例
     * @param originalException 原始异常
     * @param message 新异常消息
     * @return 转换后的MidsceneException
     */
    public static MidsceneException handleException(Logger logger, Throwable originalException, String message) {
        LoggerUtil.error(logger, message, originalException);
        
        if (originalException instanceof MidsceneException) {
            return (MidsceneException) originalException;
        } else {
            return new MidsceneException(message, originalException);
        }
    }
    
    /**
     * 获取异常链中的根本原因
     * @param throwable 异常对象
     * @return 根本原因异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        Throwable cause = throwable;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }
    
    /**
     * 格式化异常信息为字符串
     * @param throwable 异常对象
     * @return 格式化的异常信息
     */
    public static String formatException(Throwable throwable) {
        if (throwable == null) {
            return "No exception";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        sb.append(": ");
        sb.append(throwable.getMessage());
        
        // 添加栈跟踪的前几行
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int maxLines = Math.min(5, stackTrace.length);
        for (int i = 0; i < maxLines; i++) {
            sb.append("\n    at ");
            sb.append(stackTrace[i].toString());
        }
        
        // 如果有更多栈帧
        if (stackTrace.length > maxLines) {
            sb.append("\n    ... ");
            sb.append(stackTrace.length - maxLines);
            sb.append(" more");
        }
        
        return sb.toString();
    }
    
    /**
     * 检查异常是否是某种类型或其原因链中包含某种类型
     * @param throwable 要检查的异常
     * @param exceptionClass 目标异常类
     * @return 是否包含目标类型的异常
     */
    public static boolean containsException(Throwable throwable, Class<? extends Throwable> exceptionClass) {
        if (throwable == null || exceptionClass == null) {
            return false;
        }
        
        Throwable current = throwable;
        while (current != null) {
            if (exceptionClass.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        
        return false;
    }
}