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

/**
 * 简单的测试运行器，用于验证TaskExecutor的基本功能
 */
public class TaskExecutorRunner {
    public static void main(String[] args) {
        System.out.println("开始测试 TaskExecutor...");
        
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
            AgentOptions options = AgentOptions.builder().build();
            TaskExecutor taskExecutor = new TaskExecutor(mockPlatformInterface, insightEngine, mockAiModelService, options);
            
            System.out.println("TaskExecutor 创建成功");
            
            // 测试executeAiAction方法
            Action action = new Action(ActionType.TAP, "点击按钮");
            action.setTargetDescription("button");
            
            CompletableFuture<TaskResult> resultFuture = taskExecutor.executeAiAction(action.getDescription());
            
            // 等待结果
            TaskResult result = resultFuture.get();
            
            System.out.println("执行结果: " + result.getStatus());
            if (result.getStatus() == TaskStatus.COMPLETED) {
                System.out.println("测试通过！TaskExecutor.executeAiAction 方法正常工作");
            } else {
                System.out.println("测试失败: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("测试完成");
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
            // 模拟点击操作
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> inputText(String text, int x, int y) {
            // 模拟输入操作
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> scroll(String direction, int distance) {
            // 模拟滚动操作
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> navigate(String url) {
            // 模拟导航操作
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
            // 模拟等待页面加载
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public void close() {
            // 模拟关闭连接
        }
        
        @Override
        public boolean isConnected() {
            // 模拟连接状态
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