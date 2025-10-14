package com.midscene.playground;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PlaygroundException类的单元测试
 */
class PlaygroundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // 测试只有消息的构造函数
        String errorMessage = "Test playground exception message";
        PlaygroundException exception = new PlaygroundException(errorMessage);
        
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // 测试带有消息和原因的构造函数
        String errorMessage = "Test playground exception with cause";
        Throwable cause = new IllegalArgumentException("Cause exception");
        
        PlaygroundException exception = new PlaygroundException(errorMessage, cause);
        
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals("Cause exception", exception.getCause().getMessage());
    }

    @Test
    void testExceptionHierarchy() {
        // 验证异常的层次结构
        PlaygroundException exception = new PlaygroundException("Test");
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testNullMessageHandling() {
        // 测试空消息处理
        PlaygroundException exception = new PlaygroundException(null);
        assertNull(exception.getMessage());
        
        // 测试带有null原因的构造函数
        exception = new PlaygroundException("Message", null);
        assertEquals("Message", exception.getMessage());
        assertNull(exception.getCause());
    }
}