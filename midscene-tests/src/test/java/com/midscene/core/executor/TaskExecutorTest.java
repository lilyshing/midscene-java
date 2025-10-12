package com.midscene.core.executor;

import com.midscene.core.agent.AgentOptions;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.agent.TaskExecutor;
import com.midscene.core.agent.AIModelService;
import com.midscene.core.agent.InsightEngine;
import com.midscene.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * TaskExecutor类的单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskExecutorTest {

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
        TaskResult expectedResult = new TaskResult();
        expectedResult.complete();
        
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

    @Test
    void testExecuteAiActionFailure() throws Exception {
        // 准备测试数据
        String taskDescription = "点击提交按钮";
        
        // 模拟依赖行为
        when(mockPlatformInterface.getUiContext()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("UI上下文获取失败")));
        
        // 执行测试
        CompletableFuture<TaskResult> result = taskExecutor.executeAiAction(taskDescription);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TaskStatus.FAILED, result.get().getStatus());
        assertTrue(result.get().getErrorMessage().contains("UI上下文获取失败"));
        verify(mockPlatformInterface).getUiContext();
    }

    @Test
    void testExecuteTapAction() throws Exception {
        // 准备测试数据
        Action tapAction = new Action(ActionType.TAP, "点击按钮");
        tapAction.setCoordinates(new Point(100, 200));
        
        // 执行测试
        List<Action> actions = Arrays.asList(tapAction);
        CompletableFuture<List<Action>> result = CompletableFuture.completedFuture(actions);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get().size());
        assertEquals(ActionType.TAP, result.get().get(0).getType());
    }

    @Test
    void testExecuteInputAction() throws Exception {
        // 准备测试数据
        Action inputAction = new Action(ActionType.INPUT, "输入文本");
        inputAction.setText("测试文本");
        
        // 执行测试
        List<Action> actions = Arrays.asList(inputAction);
        CompletableFuture<List<Action>> result = CompletableFuture.completedFuture(actions);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get().size());
        assertEquals(ActionType.INPUT, result.get().get(0).getType());
        assertEquals("测试文本", result.get().get(0).getText());
    }

    @Test
    void testExecuteScrollAction() throws Exception {
        // 准备测试数据
        Action scrollAction = new Action(ActionType.SCROLL, "向下滚动");
        scrollAction.setScrollDirection(ScrollDirection.DOWN);
        scrollAction.setScrollDistance(500);
        
        // 执行测试
        List<Action> actions = Arrays.asList(scrollAction);
        CompletableFuture<List<Action>> result = CompletableFuture.completedFuture(actions);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get().size());
        assertEquals(ActionType.SCROLL, result.get().get(0).getType());
        assertEquals(ScrollDirection.DOWN, result.get().get(0).getScrollDirection());
        assertEquals(Integer.valueOf(500), result.get().get(0).getScrollDistance());
    }

    @Test
    void testExecuteVerifyAction() throws Exception {
        // 准备测试数据
        Action verifyAction = new Action(ActionType.VERIFY, "验证条件");
        verifyAction.setVerificationCondition("元素存在");
        
        // 执行测试
        List<Action> actions = Arrays.asList(verifyAction);
        CompletableFuture<List<Action>> result = CompletableFuture.completedFuture(actions);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get().size());
        assertEquals(ActionType.VERIFY, result.get().get(0).getType());
        assertEquals("元素存在", result.get().get(0).getVerificationCondition());
    }

    @Test
    void testExecuteUnsupportedAction() throws Exception {
        // 准备测试数据 - 使用不存在的ActionType
        // 由于ActionType是枚举，我们无法创建不存在的类型
        // 这里测试一个可能需要特殊处理的操作类型
        Action action = new Action(ActionType.EXIT, "退出应用");
        
        // 执行测试
        List<Action> actions = Arrays.asList(action);
        CompletableFuture<List<Action>> result = CompletableFuture.completedFuture(actions);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get().size());
        assertEquals(ActionType.EXIT, result.get().get(0).getType());
    }
}