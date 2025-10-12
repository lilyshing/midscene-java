package com.midscene.core.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LoggerUtil类的单元测试
 */
class LoggerUtilTest {

    @Test
    void testGetLoggerByClass() {
        // 测试通过类获取Logger
        Logger logger = LoggerUtil.getLogger(LoggerUtilTest.class);
        assertNotNull(logger);
        assertEquals(LoggerUtilTest.class.getName(), logger.getName());
    }

    @Test
    void testGetLoggerByName() {
        // 测试通过名称获取Logger
        String loggerName = "TestLogger";
        Logger logger = LoggerUtil.getLogger(loggerName);
        assertNotNull(logger);
        assertEquals(loggerName, logger.getName());
    }

    @Test
    void testIsEnabled() {
        // 测试日志级别检查
        Logger logger = LoggerUtil.getLogger(LoggerUtilTest.class);
        
        // 测试各种日志级别
        assertTrue(LoggerUtil.isEnabled(logger, Level.DEBUG));
        assertTrue(LoggerUtil.isEnabled(logger, Level.INFO));
        assertTrue(LoggerUtil.isEnabled(logger, Level.WARN));
        assertTrue(LoggerUtil.isEnabled(logger, Level.ERROR));
        // TRACE级别可能未启用，所以不强制要求
    }

    @Test
    void testLogException() {
        // 测试异常日志记录
        Logger logger = LoggerUtil.getLogger(LoggerUtilTest.class);
        Exception testException = new RuntimeException("Test exception");
        
        // 这些方法不应该抛出异常
        assertDoesNotThrow(() -> LoggerUtil.logException(logger, testException, Level.DEBUG));
        assertDoesNotThrow(() -> LoggerUtil.logException(logger, testException, Level.INFO));
        assertDoesNotThrow(() -> LoggerUtil.logException(logger, testException, Level.WARN));
        assertDoesNotThrow(() -> LoggerUtil.logException(logger, testException, Level.ERROR));
        
        // 测试null异常
        assertDoesNotThrow(() -> LoggerUtil.logException(logger, null, Level.ERROR));
    }

    @Test
    void testLogLevels() {
        // 测试各种日志级别的方法
        Logger logger = LoggerUtil.getLogger(LoggerUtilTest.class);
        
        // 这些方法不应该抛出异常
        assertDoesNotThrow(() -> LoggerUtil.debug(logger, "Debug message"));
        assertDoesNotThrow(() -> LoggerUtil.debug(logger, "Debug message with exception", new RuntimeException("Test")));
        assertDoesNotThrow(() -> LoggerUtil.debug(logger, "Debug message with param {}", "param1"));
        
        assertDoesNotThrow(() -> LoggerUtil.info(logger, "Info message"));
        assertDoesNotThrow(() -> LoggerUtil.info(logger, "Info message with exception", new RuntimeException("Test")));
        assertDoesNotThrow(() -> LoggerUtil.info(logger, "Info message with param {}", "param1"));
        
        assertDoesNotThrow(() -> LoggerUtil.warn(logger, "Warning message"));
        assertDoesNotThrow(() -> LoggerUtil.warn(logger, "Warning message with exception", new RuntimeException("Test")));
        assertDoesNotThrow(() -> LoggerUtil.warn(logger, "Warning message with param {}", "param1"));
        
        assertDoesNotThrow(() -> LoggerUtil.error(logger, "Error message"));
        assertDoesNotThrow(() -> LoggerUtil.error(logger, "Error message with exception", new RuntimeException("Test")));
        assertDoesNotThrow(() -> LoggerUtil.error(logger, "Error message with param {}", "param1"));
        
        assertDoesNotThrow(() -> LoggerUtil.trace(logger, "Trace message"));
        assertDoesNotThrow(() -> LoggerUtil.trace(logger, "Trace message with exception", new RuntimeException("Test")));
        assertDoesNotThrow(() -> LoggerUtil.trace(logger, "Trace message with param {}", "param1"));
    }
}