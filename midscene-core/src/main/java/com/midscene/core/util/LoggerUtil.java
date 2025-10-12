package com.midscene.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * 日志工具类 - 提供统一的日志功能
 */
public class LoggerUtil {
    
    /**
     * 获取指定类的Logger实例
     * @param clazz 目标类
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 获取指定名称的Logger实例
     * @param name Logger名称
     * @return Logger实例
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
    /**
     * 记录DEBUG级别日志（带异常信息）
     * @param logger Logger实例
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void debug(Logger logger, String message, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, throwable);
        }
    }
    
    /**
     * 记录DEBUG级别格式化日志
     * @param logger Logger实例
     * @param format 格式字符串
     * @param args 格式化参数
     */
    public static void debug(Logger logger, String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, args);
        }
    }
    
    /**
     * 记录INFO级别日志（带异常信息）
     * @param logger Logger实例
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void info(Logger logger, String message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(message, throwable);
        }
    }
    
    /**
     * 记录INFO级别格式化日志
     * @param logger Logger实例
     * @param format 格式字符串
     * @param args 格式化参数
     */
    public static void info(Logger logger, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(format, args);
        }
    }
    
    /**
     * 记录WARN级别日志（带异常信息）
     * @param logger Logger实例
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void warn(Logger logger, String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, throwable);
        }
    }
    
    /**
     * 记录WARN级别格式化日志
     * @param logger Logger实例
     * @param format 格式字符串
     * @param args 格式化参数
     */
    public static void warn(Logger logger, String format, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, args);
        }
    }
    
    /**
     * 记录ERROR级别日志（带异常信息）
     * @param logger Logger实例
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(Logger logger, String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(message, throwable);
        }
    }
    
    /**
     * 记录ERROR级别格式化日志
     * @param logger Logger实例
     * @param format 格式字符串
     * @param args 格式化参数
     */
    public static void error(Logger logger, String format, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(format, args);
        }
    }
    
    /**
     * 记录TRACE级别日志（带异常信息）
     * @param logger Logger实例
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void trace(Logger logger, String message, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(message, throwable);
        }
    }
    
    /**
     * 记录TRACE级别格式化日志
     * @param logger Logger实例
     * @param format 格式字符串
     * @param args 格式化参数
     */
    public static void trace(Logger logger, String format, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, args);
        }
    }
    
    /**
     * 检查Logger是否启用了指定级别的日志
     * @param logger Logger实例
     * @param level 日志级别
     * @return 是否启用
     */
    public static boolean isEnabled(Logger logger, Level level) {
        switch (level) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                return false;
        }
    }
    
    /**
     * 记录异常堆栈信息
     * @param logger Logger实例
     * @param throwable 异常对象
     * @param level 日志级别
     */
    public static void logException(Logger logger, Throwable throwable, Level level) {
        if (throwable == null) {
            return;
        }
        
        String message = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName();
        
        switch (level) {
            case TRACE:
                debug(logger, message, throwable);
                break;
            case DEBUG:
                debug(logger, message, throwable);
                break;
            case INFO:
                info(logger, message, throwable);
                break;
            case WARN:
                warn(logger, message, throwable);
                break;
            case ERROR:
                error(logger, message, throwable);
                break;
        }
    }
}