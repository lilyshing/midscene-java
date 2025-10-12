package com.midscene.core.executor;

import com.midscene.core.agent.AgentOptions;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.agent.TaskExecutor;
import com.midscene.core.agent.AIModelService;
import com.midscene.core.agent.InsightEngine;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.model.UiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * TaskExecutor类的简单单元测试
 */
@ExtendWith(MockitoExtension.class)
class SimpleTaskExecutorTest {

    @Mock
    private PlatformInterface mockPlatformInterface;
    
    @Mock
    private InsightEngine mockInsightEngine;
    
    @Mock
    private AIModelService mockAiModelService;
    
    private AgentOptions agentOptions;
    private TaskExecutor taskExecutor;

    @BeforeEach
    void setUp() {
        agentOptions = new AgentOptions();
        
        // 创建TaskExecutor实例
        taskExecutor = new TaskExecutor(mockPlatformInterface, mockInsightEngine, mockAiModelService, agentOptions);
    }

    @Test
    void testConstructor() {
        // 测试构造函数
        assertNotNull(taskExecutor);
    }

    @Test
    void testExecuteAiAction() throws Exception {
        // 准备测试数据
        String taskDescription = "点击提交按钮";
        UiContext mockUiContext = new UiContext();
        
        // 模拟依赖行为
        when(mockPlatformInterface.getUiContext()).thenReturn(CompletableFuture.completedFuture(mockUiContext));
        
        // 模拟AI模型服务返回JSON
        when(mockAiModelService.planActions(any(UiContext.class), eq(taskDescription)))
            .thenReturn(CompletableFuture.completedFuture(
                "{\"type\": \"action_plan\", \"description\": \"Generated plan for: " + taskDescription + "\", \"steps\": []}"
            ));
        
        // 执行测试
        CompletableFuture<TaskResult> result = taskExecutor.executeAiAction(taskDescription);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, result.get().getStatus());
        verify(mockPlatformInterface).getUiContext();
        verify(mockAiModelService).planActions(any(UiContext.class), eq(taskDescription));
    }
}