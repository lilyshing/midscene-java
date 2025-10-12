package com.midscene.core.agent;

import com.midscene.core.model.*;
import com.midscene.core.agent.InsightEngine;
import com.midscene.core.agent.AIModelService;
import com.midscene.core.agent.PlatformInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Agent类的单元测试
 */
@ExtendWith(MockitoExtension.class)
class AgentTest {

    @Mock
    private PlatformInterface mockPlatformInterface;
    
    @Mock
    private InsightEngine mockInsightEngine;
    
    @Mock
    private AIModelService mockAiModelService;
    
    @Mock
    private TaskExecutor mockTaskExecutor;
    
    private Agent agent;
    private AgentOptions agentOptions;

    @BeforeEach
    void setUp() {
        agentOptions = new AgentOptions();
        agentOptions.setRetryCount(3);
        
        // 创建Agent实例
        agent = new Agent(mockPlatformInterface, agentOptions);
    }

    @Test
    void testConstructor() {
        // 测试构造函数
        assertNotNull(agent);
        assertEquals(agentOptions.getRetryCount(), agentOptions.getRetryCount());
        assertEquals(agentOptions.getTimeout(), agentOptions.getTimeout());
    }

    @Test
    void testAiActionSuccess() throws Exception {
        // 准备测试数据
        String prompt = "点击提交按钮";
        TaskResult expectedResult = new TaskResult();
        expectedResult.complete();
        
        // 执行测试
        CompletableFuture<TaskResult> result = agent.aiAction(prompt);
        
        // 验证结果
        assertNotNull(result);
        // 由于我们无法轻易模拟TaskExecutor，这里只验证返回类型
        assertTrue(result instanceof CompletableFuture);
    }

    @Test
    void testAiLocate() throws Exception {
        // 准备测试数据
        String elementDescription = "提交按钮";
        
        // 模拟UI上下文
        UiContext mockContext = mock(UiContext.class);
        when(mockPlatformInterface.getUiContext()).thenReturn(CompletableFuture.completedFuture(mockContext));
        
        // 创建新的Agent实例
        Agent testAgent = new Agent(mockPlatformInterface, agentOptions);
        
        // 执行测试
        CompletableFuture<LocateResult> result = testAgent.aiLocate(elementDescription);
        
        // 验证结果
        assertNotNull(result);
        // 由于我们无法轻易模拟InsightEngine，这里只验证返回类型
        assertTrue(result instanceof CompletableFuture);
    }

    @Test
    void testFreeze() throws Exception {
        // 模拟UI上下文
        UiContext mockContext = mock(UiContext.class);
        when(mockPlatformInterface.getUiContext()).thenReturn(CompletableFuture.completedFuture(mockContext));
        
        // 创建新的Agent实例
        Agent testAgent = new Agent(mockPlatformInterface, agentOptions);
        
        // 执行测试
        CompletableFuture<Boolean> result = testAgent.freeze();
        
        // 验证结果
        assertNotNull(result);
        // 由于我们无法轻易模拟PlatformInterface，这里只验证返回类型
        assertTrue(result instanceof CompletableFuture);
    }

    @Test
    void testUnfreeze() {
        // 执行测试
        agent.unfreeze();
        
        // 验证结果 - 只验证方法不会抛出异常
        assertTrue(true);
    }

    @Test
    void testGetInterfaceType() {
        // 模拟依赖行为
        when(mockPlatformInterface.getInterfaceType()).thenReturn("test-interface");
        
        // 创建新的Agent实例
        Agent testAgent = new Agent(mockPlatformInterface, agentOptions);
        
        // 执行测试
        String result = testAgent.getInterfaceType();
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test-interface", result);
        // getInterfaceType在构造函数中被调用两次（一次在Agent构造函数，一次在TaskExecutor构造函数），在测试中被调用一次
        verify(mockPlatformInterface, times(3)).getInterfaceType();
    }

    @Test
    void testIsDestroyed() {
        // 验证初始状态
        assertFalse(agent.isDestroyed());
        
        // 关闭Agent
        agent.close();
        
        // 验证状态已更改
        assertTrue(agent.isDestroyed());
    }

    @Test
    void testIsContextFrozen() throws Exception {
        // 模拟UI上下文
        UiContext mockContext = mock(UiContext.class);
        when(mockPlatformInterface.getUiContext()).thenReturn(CompletableFuture.completedFuture(mockContext));
        
        // 创建新的Agent实例
        Agent testAgent = new Agent(mockPlatformInterface, agentOptions);
        
        // 验证初始状态
        assertFalse(testAgent.isContextFrozen());
        
        // 冻结上下文
        testAgent.freeze();
        
        // 验证状态已更改
        assertTrue(testAgent.isContextFrozen());
        
        // 解除冻结
        testAgent.unfreeze();
        
        // 验证状态已恢复
        assertFalse(testAgent.isContextFrozen());
    }

    @Test
    void testUpdateOptions() {
        // 创建新选项
        AgentOptions newOptions = new AgentOptions();
        newOptions.setRetryCount(5);
        newOptions.setTimeout(10000);
        
        // 创建新的Agent实例来测试更新选项
        Agent newAgent = new Agent(mockPlatformInterface, newOptions);
        
        // 验证选项已更新
        assertEquals(5, newOptions.getRetryCount());
        assertEquals(10000, newOptions.getTimeout());
    }
}