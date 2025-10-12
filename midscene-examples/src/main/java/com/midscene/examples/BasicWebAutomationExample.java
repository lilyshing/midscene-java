package com.midscene.examples;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.TaskResult;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.examples.PlaywrightPlatformInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基本Web自动化示例
 * 演示如何使用Midscene Java框架进行AI驱动的网页自动化
 */
public class BasicWebAutomationExample {
    private static final Logger logger = LoggerFactory.getLogger(BasicWebAutomationExample.class);
    
    public static void main(String[] args) {
        Agent agent = null;
        
        try {
            // 1. 初始化Playwright页面
            logger.info("🌐 初始化Playwright页面...");
            // 使用新的工厂方法创建非headless模式的PlaywrightPage
            try (PlaywrightPage page = PlaywrightPage.createWithHeadlessMode(false)) {
            
            // 2. 导航到示例网站
            String url = "https://example.com";
            logger.info("导航到网站: {}", url);
            page.navigate(url).get(10, TimeUnit.SECONDS);
            
            // 3. 创建平台接口实现
            PlaywrightPlatformInterface platformInterface = new PlaywrightPlatformInterface(page);
            
            // 4. 创建Agent实例
            logger.info("创建Agent实例...");
            AgentOptions options = new AgentOptions();
            options.setTimeout(30);
            agent = new Agent(platformInterface, options);
            
            // 5. 执行AI驱动的操作
            logger.info("\n=== 执行AI操作示例 ===");
            
            // 点击页面中的链接
            CompletableFuture<TaskResult> clickResult = agent.aiAction("点击页面中的第一个链接");
            TaskResult result = clickResult.get(30, TimeUnit.SECONDS);
            logger.info("点击操作结果: {}", result.getStatus());
            
            // 等待页面加载
            Thread.sleep(2000);
            
            // 返回上一页
            CompletableFuture<TaskResult> backResult = agent.aiAction("返回上一页");
            result = backResult.get(30, TimeUnit.SECONDS);
            logger.info("返回操作结果: {}", result.getStatus());
            
            logger.info("✅ Web自动化示例执行成功!");
            
            } // PlaywrightPage会自动关闭
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("❌ Web自动化示例执行失败: {}", e.getMessage(), e);
        } finally {
            // 清理资源
            if (agent != null) {
                try {
                    agent.close();
                } catch (Exception e) {
                    logger.error("关闭Agent时出错", e);
                }
            }
            
            logger.info("示例执行完成");
        }
    }
}