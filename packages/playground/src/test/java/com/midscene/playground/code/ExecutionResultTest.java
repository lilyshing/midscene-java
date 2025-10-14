package com.midscene.playground.code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ExecutionResult类的单元测试
 */
class ExecutionResultTest {

    @Test
    void testSuccessResultCreation() {
        // 测试创建成功结果
        ExecutionResult result = ExecutionResult.success()
                .setReturnValue("Success value")
                .setOutput("Execution output")
                .setExecutionTimeMs(100)
                .build();
        
        assertTrue(result.isSuccess());
        assertEquals("Success value", result.getReturnValue());
        assertEquals("Execution output", result.getOutput());
        assertNull(result.getError());
        assertEquals(100, result.getExecutionTimeMs());
        assertNull(result.getException());
        assertEquals(0, result.getContext().size());
    }

    @Test
    void testFailureResultCreation() {
        // 测试创建失败结果
        ExecutionResult result = ExecutionResult.failure()
                .setError("Something went wrong")
                .setExecutionTimeMs(50)
                .build();
        
        assertFalse(result.isSuccess());
        assertNull(result.getReturnValue());
        assertNull(result.getOutput());
        assertEquals("Something went wrong", result.getError());
        assertEquals(50, result.getExecutionTimeMs());
        assertNull(result.getException());
    }

    @Test
    void testExceptionHandling() {
        // 测试异常处理
        Exception exception = new RuntimeException("Test exception");
        ExecutionResult result = ExecutionResult.success() // 即使开始为success，设置exception后会变为failure
                .setException(exception)
                .build();
        
        assertFalse(result.isSuccess()); // 设置异常后应该自动变为失败
        assertEquals(exception, result.getException());
        assertEquals("Test exception", result.getError()); // 错误消息自动从异常获取
        
        // 测试堆栈跟踪获取
        String stackTrace = result.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("Test exception"));
        assertTrue(stackTrace.contains("RuntimeException"));
    }

    @Test
    void testContextHandling() {
        // 测试上下文处理
        Map<String, Object> context = new HashMap<>();
        context.put("key1", "value1");
        context.put("key2", 123);
        
        ExecutionResult result = ExecutionResult.success()
                .setContext(context)
                .build();
        
        Map<String, Object> returnedContext = result.getContext();
        assertNotNull(returnedContext);
        assertEquals(2, returnedContext.size());
        assertEquals("value1", returnedContext.get("key1"));
        assertEquals(123, returnedContext.get("key2"));
        
        // 验证返回的上下文是不可修改的
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedContext.put("key3", "value3");
        });
    }

    @Test
    void testNullContext() {
        // 测试空上下文
        ExecutionResult result = ExecutionResult.success()
                .setContext(null)
                .build();
        
        Map<String, Object> returnedContext = result.getContext();
        assertNotNull(returnedContext);
        assertTrue(returnedContext.isEmpty());
    }

    @Test
    void testToStringMethod() {
        // 测试toString方法
        ExecutionResult result = ExecutionResult.success()
                .setReturnValue("test")
                .setOutput("short output")
                .setExecutionTimeMs(42)
                .build();
        
        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ExecutionResult"));
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("returnValue=test"));
        assertTrue(toString.contains("output=\"short output\""));
        assertTrue(toString.contains("executionTimeMs=42"));
        assertTrue(toString.contains("contextSize=0"));
    }

    @Test
    void testStringTruncation() {
        // 测试长字符串截断功能
        StringBuilder longStringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longStringBuilder.append("a");
        }
        String longString = longStringBuilder.toString();
        
        ExecutionResult result = ExecutionResult.success()
                .setOutput(longString)
                .build();
        
        String toString = result.toString();
        assertTrue(toString.contains("output=\"" + longString.substring(0, 100) + "...\""));
    }

    @Test
    void testBuilderChaining() {
        // 测试Builder链式调用
        ExecutionResult result = ExecutionResult.success()
                .setReturnValue(123)
                .setOutput("output")
                .setError("error") // 成功结果也可以设置错误，但通常不应该
                .setExecutionTimeMs(100)
                .build();
        
        assertTrue(result.isSuccess());
        assertEquals(123, result.getReturnValue());
        assertEquals("output", result.getOutput());
        assertEquals("error", result.getError());
        assertEquals(100, result.getExecutionTimeMs());
    }

    @Test
    void testExceptionOverridesSuccess() {
        // 测试设置异常会覆盖success标志
        ExecutionResult result = ExecutionResult.success()
                .setException(new RuntimeException("Test"))
                .build();
        
        assertFalse(result.isSuccess());
    }

    @Test
    void testCustomErrorOverridesExceptionMessage() {
        // 测试自定义错误消息会覆盖异常消息
        ExecutionResult result = ExecutionResult.failure()
                .setError("Custom error message")
                .setException(new RuntimeException("Exception message"))
                .build();
        
        assertEquals("Custom error message", result.getError()); // 应该保留自定义错误消息
    }

    @Test
    void testEmptyValues() {
        // 测试空值处理
        ExecutionResult result = ExecutionResult.success().build();
        
        assertTrue(result.isSuccess());
        assertNull(result.getReturnValue());
        assertNull(result.getOutput());
        assertNull(result.getError());
        assertEquals(0, result.getExecutionTimeMs());
        assertNull(result.getException());
        assertNotNull(result.getContext());
        assertTrue(result.getContext().isEmpty());
    }
}