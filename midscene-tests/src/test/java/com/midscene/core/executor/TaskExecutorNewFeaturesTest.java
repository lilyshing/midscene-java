package com.midscene.core.executor;

import com.midscene.core.agent.TaskExecutor;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.model.Action;
import com.midscene.core.model.ActionType;
import com.midscene.core.model.Point;
import com.midscene.core.model.ScrollDirection;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.UiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * TaskExecutor新功能测试类
 * 测试重试机制和新操作类型（WAIT、NAVIGATE、SCREENSHOT、EXIT）
 */
@DisplayName("TaskExecutor新功能测试")
public class TaskExecutorNewFeaturesTest {

    @Mock
    private PlatformInterface platformInterface;

    private TaskExecutor taskExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskExecutor = new TaskExecutor(platformInterface);
        
        // 设置平台接口的基本行为
        when(platformInterface.getInterfaceType()).thenReturn("TEST");
        when(platformInterface.isConnected()).thenReturn(true);
        when(platformInterface.getUiContext()).thenReturn(CompletableFuture.completedFuture(new UiContext()));
        when(platformInterface.tap(anyInt(), anyInt())).thenReturn(CompletableFuture.completedFuture(true));
        when(platformInterface.inputText(anyString(), anyInt(), anyInt())).thenReturn(CompletableFuture.completedFuture(true));
        when(platformInterface.scroll(anyString(), anyInt())).thenReturn(CompletableFuture.completedFuture(true));
        when(platformInterface.navigate(anyString())).thenReturn(CompletableFuture.completedFuture(true));
        when(platformInterface.waitForPageLoad(anyLong())).thenReturn(CompletableFuture.completedFuture(true));
    }

    @Nested
    @DisplayName("重试机制测试")
    class RetryMechanismTests {

        @Test
        @DisplayName("测试操作失败时的重试机制")
        void testRetryMechanismOnFailure() throws Exception {
            // 创建一个会失败的操作
            Action action = new Action(ActionType.TAP, "点击按钮");
            action.setCoordinates(new Point(100, 100));
            action.setMaxRetries(2);
            action.setRetryOnFailure(true);
            
            // 设置平台接口前两次调用失败，第三次成功
            when(platformInterface.tap(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(false))
                .thenReturn(CompletableFuture.completedFuture(false))
                .thenReturn(CompletableFuture.completedFuture(true));
            
            // 执行操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("点击按钮");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(1, result.getSteps().size());
            
            // 验证平台接口被调用了3次（1次初始调用 + 2次重试）
            verify(platformInterface, times(3)).tap(100, 100);
        }

        @Test
        @DisplayName("测试禁用重试时的行为")
        void testNoRetryWhenDisabled() throws Exception {
            // 创建一个会失败的操作，但禁用重试
            Action action = new Action(ActionType.TAP, "点击按钮");
            action.setCoordinates(new Point(100, 100));
            action.setMaxRetries(2);
            action.setRetryOnFailure(false);
            
            // 设置平台接口调用失败
            when(platformInterface.tap(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(false));
            
            // 执行操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("点击按钮");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.FAILED, result.getStatus());
            
            // 验证平台接口只被调用了1次（没有重试）
            verify(platformInterface, times(1)).tap(100, 100);
        }

        @Test
        @DisplayName("测试超过最大重试次数时的行为")
        void testExceedMaxRetries() throws Exception {
            // 创建一个会失败的操作
            Action action = new Action(ActionType.TAP, "点击按钮");
            action.setCoordinates(new Point(100, 100));
            action.setMaxRetries(1); // 只允许1次重试
            action.setRetryOnFailure(true);
            
            // 设置平台接口总是失败
            when(platformInterface.tap(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(false));
            
            // 执行操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("点击按钮");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.FAILED, result.getStatus());
            
            // 验证平台接口被调用了2次（1次初始调用 + 1次重试）
            verify(platformInterface, times(2)).tap(100, 100);
        }
    }

    @Nested
    @DisplayName("新操作类型测试")
    class NewActionTypeTests {

        @Test
        @DisplayName("测试WAIT操作")
        void testWaitAction() throws Exception {
            // 执行等待操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("等待3秒");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(1, result.getSteps().size());
            assertEquals("等待3秒", result.getSteps().get(0).getDescription());
        }

        @Test
        @DisplayName("测试NAVIGATE操作")
        void testNavigateAction() throws Exception {
            // 执行导航操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("导航到https://example.com");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(1, result.getSteps().size());
            
            // 验证平台接口的navigateTo方法被调用
            verify(platformInterface, times(1)).navigateTo("https://example.com");
        }

        @Test
        @DisplayName("测试SCREENSHOT操作")
        void testScreenshotAction() throws Exception {
            // 执行截图操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("截图test.png");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(1, result.getSteps().size());
            
            // 验证平台接口的takeScreenshot方法被调用
            verify(platformInterface, times(1)).takeScreenshot("test.png");
        }

        @Test
        @DisplayName("测试EXIT操作")
        void testExitAction() throws Exception {
            // 执行退出操作
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction("退出应用");
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(1, result.getSteps().size());
            
            // 验证平台接口的exitApplication方法被调用
            verify(platformInterface, times(1)).exitApplication();
        }
    }

    @Nested
    @DisplayName("操作序列测试")
    class ActionSequenceTests {

        @Test
        @DisplayName("测试包含新操作类型的序列")
        void testActionSequenceWithNewTypes() throws Exception {
            // 创建操作序列
            List<Action> actions = Arrays.asList(
                new Action(ActionType.NAVIGATE, "导航到登录页面"),
                new Action(ActionType.INPUT, "输入用户名"),
                new Action(ActionType.INPUT, "输入密码"),
                new Action(ActionType.TAP, "点击登录按钮"),
                new Action(ActionType.VERIFY, "验证登录成功"),
                new Action(ActionType.SCREENSHOT, "截图保存结果"),
                new Action(ActionType.WAIT, "等待3秒"),
                new Action(ActionType.EXIT, "退出应用")
            );
            
            // 设置输入操作的行为
            when(platformInterface.inputText(anyString(), anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(true));
            
            // 执行操作序列
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeActions(actions);
            TaskResult result = resultFuture.get(15, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(8, result.getSteps().size());
            
            // 验证各种操作都被执行
            verify(platformInterface, times(1)).navigateTo(anyString());
            verify(platformInterface, times(2)).inputText(anyString(), anyInt(), anyInt());
            verify(platformInterface, times(1)).tap(anyInt(), anyInt());
            verify(platformInterface, times(1)).takeScreenshot(anyString());
            verify(platformInterface, times(1)).exitApplication();
        }
    }

    @Nested
    @DisplayName("JSON解析测试")
    class JsonParsingTests {

        @Test
        @DisplayName("测试解析新操作类型的JSON")
        void testParseNewActionTypesFromJson() throws Exception {
            // 创建包含新操作类型的JSON
            String json = "[{" +
                "\"action\": \"navigate\"," +
                "\"description\": \"导航到登录页面\"," +
                "\"url\": \"https://example.com/login\"" +
                "},{" +
                "\"action\": \"wait\"," +
                "\"description\": \"等待3秒\"," +
                "\"duration\": \"3000\"" +
                "},{" +
                "\"action\": \"screenshot\"," +
                "\"description\": \"截图保存\"," +
                "\"filename\": \"login.png\"" +
                "},{" +
                "\"action\": \"exit\"," +
                "\"description\": \"退出应用\"" +
                "}]";
            
            // 执行JSON解析
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeJsonActions(json);
            TaskResult result = resultFuture.get(10, TimeUnit.SECONDS);
            
            // 验证结果
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
            assertEquals(4, result.getSteps().size());
            
            // 验证各种操作都被执行
            verify(platformInterface, times(1)).navigateTo("https://example.com/login");
            verify(platformInterface, times(1)).takeScreenshot("login.png");
            verify(platformInterface, times(1)).exitApplication();
        }
    }
}