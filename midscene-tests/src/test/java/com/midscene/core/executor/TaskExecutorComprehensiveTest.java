package com.midscene.core.executor;

import com.midscene.core.agent.InsightEngine;
import com.midscene.core.agent.TaskExecutor;
import com.midscene.core.agent.AIModelService;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.Action;
import com.midscene.core.model.ActionType;
import com.midscene.core.model.UiContext;
import com.midscene.core.agent.LocateResult;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.model.Point;

import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;

/**
 * 全面的TaskExecutor测试运行器，验证各种功能
 */
public class TaskExecutorComprehensiveTest {
    public static void main(String[] args) {
        System.out.println("开始全面测试 TaskExecutor...");
        
        try {
            // 创建模拟的依赖项
            MockPlatformInterface mockPlatformInterface = new MockPlatformInterface();
            MockAIModelService mockAiModelService = new MockAIModelService();
            
            // 创建InsightEngine
            InsightEngine insightEngine = new InsightEngine(
                () -> CompletableFuture.completedFuture(new UiContext()),
                mockAiModelService
            );
            
            // 创建TaskExecutor实例
            AgentOptions options = AgentOptions.builder()
                .timeout(10)
                .retryCount(2)
                .screenshotOnError(false)
                .build();
            TaskExecutor taskExecutor = new TaskExecutor(mockPlatformInterface, insightEngine, mockAiModelService, options);
            
            System.out.println("TaskExecutor 创建成功");
            
            // 测试1: 执行点击操作
            testTapOperation(taskExecutor);
            
            // 测试2: 执行输入操作
            testInputOperation(taskExecutor);
            
            // 测试3: 执行滚动操作
            testScrollOperation(taskExecutor);
            
            // 测试4: 执行导航操作
            testNavigateOperation(taskExecutor);
            
            // 测试5: 执行多个操作序列
            testActionSequence(taskExecutor);
            
            System.out.println("\n所有测试完成！");
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testTapOperation(TaskExecutor taskExecutor) throws Exception {
        System.out.println("\n=== 测试1: 点击操作 ===");
        
        Action action = new Action(ActionType.TAP, "点击登录按钮");
        action.setTargetDescription("登录按钮");
        
        CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(action.getDescription());
        TaskResult result = resultFuture.get();
        
        System.out.println("执行结果: " + result.getStatus());
        if (result.getStatus() == TaskStatus.COMPLETED) {
            System.out.println("✓ 点击操作测试通过");
        } else {
            System.out.println("✗ 点击操作测试失败: " + result.getErrorMessage());
        }
    }
    
    private static void testInputOperation(TaskExecutor taskExecutor) throws Exception {
        System.out.println("\n=== 测试2: 输入操作 ===");
        
        Action action = new Action(ActionType.INPUT, "在搜索框输入文本");
        action.setTargetDescription("搜索框");
        
        CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(action.getDescription());
        TaskResult result = resultFuture.get();
        
        System.out.println("执行结果: " + result.getStatus());
        if (result.getStatus() == TaskStatus.COMPLETED) {
            System.out.println("✓ 输入操作测试通过");
        } else {
            System.out.println("✗ 输入操作测试失败: " + result.getErrorMessage());
        }
    }
    
    private static void testScrollOperation(TaskExecutor taskExecutor) throws Exception {
        System.out.println("\n=== 测试3: 滚动操作 ===");
        
        Action action = new Action(ActionType.SCROLL, "向下滚动页面");
        
        CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(action.getDescription());
        TaskResult result = resultFuture.get();
        
        System.out.println("执行结果: " + result.getStatus());
        if (result.getStatus() == TaskStatus.COMPLETED) {
            System.out.println("✓ 滚动操作测试通过");
        } else {
            System.out.println("✗ 滚动操作测试失败: " + result.getErrorMessage());
        }
    }
    
    private static void testNavigateOperation(TaskExecutor taskExecutor) throws Exception {
        System.out.println("\n=== 测试4: 导航操作 ===");
        
        Action action = new Action(ActionType.NAVIGATE, "导航到指定URL");
        
        CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(action.getDescription());
        TaskResult result = resultFuture.get();
        
        System.out.println("执行结果: " + result.getStatus());
        if (result.getStatus() == TaskStatus.COMPLETED) {
            System.out.println("✓ 导航操作测试通过");
        } else {
            System.out.println("✗ 导航操作测试失败: " + result.getErrorMessage());
        }
    }
    
    private static void testActionSequence(TaskExecutor taskExecutor) throws Exception {
        System.out.println("\n=== 测试5: 操作序列 ===");
        
        List<String> actions = Arrays.asList(
            "导航到登录页面",
            "输入用户名",
            "输入密码",
            "点击登录按钮",
            "验证登录成功"
        );
        
        for (String actionDesc : actions) {
            System.out.println("执行操作: " + actionDesc);
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(actionDesc);
            TaskResult result = resultFuture.get();
            
            System.out.println("执行结果: " + result.getStatus());
            if (result.getStatus() != TaskStatus.COMPLETED) {
                System.out.println("✗ 操作序列测试失败: " + result.getErrorMessage());
                return;
            }
        }
        
        System.out.println("✓ 操作序列测试通过");
    }
    
    // 模拟的PlatformInterface实现
    static class MockPlatformInterface implements com.midscene.core.agent.PlatformInterface {
        @Override
        public String getInterfaceType() {
            return "mock";
        }
        
        @Override
        public CompletableFuture<UiContext> getUiContext() {
            UiContext context = new UiContext();
            // 设置模拟的UI上下文数据
            return CompletableFuture.completedFuture(context);
        }
        
        @Override
        public CompletableFuture<Boolean> tap(int x, int y) {
            System.out.println("模拟点击操作: 坐标(" + x + ", " + y + ")");
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> inputText(String text, int x, int y) {
            System.out.println("模拟输入操作: 文本\"" + text + "\" 坐标(" + x + ", " + y + ")");
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> scroll(String direction, int distance) {
            System.out.println("模拟滚动操作: 方向\"" + direction + "\" 距离" + distance);
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> navigate(String url) {
            System.out.println("模拟导航操作: URL\"" + url + "\"");
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
            System.out.println("模拟等待页面加载: 超时" + timeout + "ms");
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public void close() {
            System.out.println("模拟关闭连接");
        }
        
        @Override
        public boolean isConnected() {
            return true;
        }
    }
    
    // 模拟的AIModelService实现
    static class MockAIModelService extends AIModelService {
        public MockAIModelService() {
            super(null);
        }
        
        @Override
        public CompletableFuture<String> locateElement(UiContext context, String elementDescription) {
            // 返回模拟的定位结果
            return CompletableFuture.completedFuture(
                "{" +
                "\"success\": true," +
                "\"confidence\": 0.95," +
                "\"bounding_box\": {\"left\": 100, \"top\": 200, \"width\": 120, \"height\": 40}" +
                "}"
            );
        }
    }
}