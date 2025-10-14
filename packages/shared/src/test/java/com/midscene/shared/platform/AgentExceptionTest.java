package com.midscene.shared.platform;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AgentException类的单元测试
 */
class AgentExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // 测试只有消息的构造函数
        String errorMessage = "Test exception message";
        AgentException exception = new AgentException(errorMessage);
        
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // 测试带有消息和原因的构造函数
        String errorMessage = "Test exception with cause";
        Throwable cause = new IllegalArgumentException("Cause exception");
        
        AgentException exception = new AgentException(errorMessage, cause);
        
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals("Cause exception", exception.getCause().getMessage());
    }

    @Test
    void testExceptionHierarchy() {
        // 验证异常的层次结构
        AgentException exception = new AgentException("Test");
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testNullMessageHandling() {
        // 测试空消息处理
        AgentException exception = new AgentException(null);
        assertNull(exception.getMessage());
        
        // 测试带有null原因的构造函数
        exception = new AgentException("Message", null);
        assertEquals("Message", exception.getMessage());
        assertNull(exception.getCause());
    }
}