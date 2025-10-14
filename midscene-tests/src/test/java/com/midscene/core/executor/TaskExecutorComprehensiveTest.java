package com.midscene.core.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * TaskExecutor综合测试框架，用于演示任务执行器的基本概念
 */
public class TaskExecutorComprehensiveTest {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorComprehensiveTest.class);
    
    public static void main(String[] args) {
        logger.info("开始TaskExecutor框架演示...");
        
        try {
            // 创建模拟的平台接口
            MockPlatformInterface mockPlatformInterface = new MockPlatformInterface();
            
            logger.info("模拟平台接口创建成功");
            logger.info("接口类型: {}", mockPlatformInterface.getInterfaceType());
            
            // 测试各种操作
            testBasicOperations(mockPlatformInterface);
            
            // 测试操作序列
            testActionSequence(mockPlatformInterface);
            
            logger.info("演示完成！");
            
        } catch (Exception e) {
            logger.error("演示过程中发生错误: {}", e.getMessage(), e);
        }
    }
    
    private static void testBasicOperations(MockPlatformInterface platform) throws Exception {
        logger.info("\n=== 基本操作演示 ===");
        
        // 测试点击操作
        boolean tapResult = platform.tap(100, 200).get();
        logger.info("点击操作演示结果: {}", tapResult);
        
        // 测试输入操作
        boolean inputResult = platform.inputText("测试文本", 100, 200).get();
        logger.info("输入操作演示结果: {}", inputResult);
        
        // 测试滚动操作
        boolean scrollResult = platform.scroll("down", 500).get();
        logger.info("滚动操作演示结果: {}", scrollResult);
        
        // 测试导航操作
        platform.navigate("https://example.com");
        logger.info("导航操作演示完成");
        
        // 测试页面加载等待
        boolean waitResult = platform.waitForPageLoad(5000).get();
        logger.info("页面加载等待演示结果: {}", waitResult);
    }
    
    private static void testActionSequence(MockPlatformInterface platform) throws Exception {
        logger.info("\n=== 操作序列演示 ===");
        
        List<String> actions = Arrays.asList(
            "导航到登录页面",
            "输入用户名",
            "输入密码",
            "点击登录按钮",
            "验证登录成功"
        );
        
        for (String actionDesc : actions) {
            logger.info("执行操作: {}", actionDesc);
            
            // 根据操作描述执行不同的模拟操作
            if (actionDesc.contains("导航")) {
                platform.navigate("https://example.com/login");
            } else if (actionDesc.contains("输入")) {
                platform.inputText(actionDesc.contains("用户名") ? "test_user" : "password123", 100, 150).get();
            } else if (actionDesc.contains("点击")) {
                platform.tap(150, 250).get();
            } else if (actionDesc.contains("验证")) {
                platform.waitForPageLoad(3000).get();
            }
            
            logger.info("操作 '{}' 演示完成", actionDesc);
        }
        
        logger.info("操作序列演示完成");
    }
    
    // 简化的模拟平台接口
    static class MockPlatformInterface {
        public String getInterfaceType() {
            return "mock";
        }
        
        public CompletableFuture<Boolean> tap(int x, int y) {
            logger.info("模拟点击操作: 坐标({}, {})", x, y);
            return CompletableFuture.completedFuture(true);
        }
        
        public CompletableFuture<Boolean> inputText(String text, int x, int y) {
            logger.info("模拟输入操作: 文本\"{}\" 坐标({}, {})", text, x, y);
            return CompletableFuture.completedFuture(true);
        }
        
        public CompletableFuture<Boolean> scroll(String direction, int distance) {
            logger.info("模拟滚动操作: 方向\"{}\" 距离{}", direction, distance);
            return CompletableFuture.completedFuture(true);
        }
        
        public void navigate(String url) {
            logger.info("模拟导航操作: URL\"{}", url);
        }
        
        public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
            logger.info("模拟等待页面加载: 超时{}ms", timeout);
            return CompletableFuture.completedFuture(true);
        }
        
        public void close() {
            logger.info("模拟关闭连接");
        }
        
        public boolean isConnected() {
            return true;
        }
    }
}