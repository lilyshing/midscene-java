package com.midscene.examples;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.TaskResult;
import com.midscene.web.playwright.PlaywrightPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 高级Web自动化示例
 * 演示如何使用Midscene Java框架进行复杂的AI驱动网页自动化
 */
public class AdvancedWebAutomationExample {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedWebAutomationExample.class);
    
    public static void main(String[] args) {
        PlaywrightPage page = null;
        Agent agent = null;
        
        try {
            // 1. 初始化Playwright页面
            logger.info("🌐 初始化Playwright页面...");
            page = new PlaywrightPage();
            
            // 2. 导航到示例网站
            String url = "https://github.com";
            logger.info("导航到网站: {}", url);
            page.navigate(url).get(10, TimeUnit.SECONDS);
            
            // 3. 创建平台接口实现
            PlaywrightPlatformInterface platformInterface = new PlaywrightPlatformInterface(page);
            
            // 4. 创建Agent实例，配置高级选项
            logger.info("创建Agent实例...");
            AgentOptions options = AgentOptions.builder()
                .timeout(30)
                .retryCount(3)
                .retryDelay(1.0)
                .screenshotOnError(true)
                .cacheEnabled(true)
                .build();
            
            agent = new Agent(platformInterface, options);
            
            // 5. 执行复杂的AI驱动操作
            logger.info("\n=== 执行高级AI操作示例 ===");
            
            // 搜索仓库
            CompletableFuture<TaskResult> searchResult = agent.aiAction("在搜索框中输入 'midscene' 并点击搜索按钮");
            TaskResult result = searchResult.get(30, TimeUnit.SECONDS);
            logger.info("搜索操作结果: {}", result.getStatus());
            
            // 等待搜索结果加载
            Thread.sleep(2000);
            
            // 点击第一个搜索结果
            CompletableFuture<TaskResult> clickResult = agent.aiAction("点击第一个搜索结果");
            result = clickResult.get(30, TimeUnit.SECONDS);
            logger.info("点击结果操作: {}", result.getStatus());
            
            // 等待页面加载
            Thread.sleep(2000);
            
            // 使用AI提取页面信息
            logger.info("\n=== 使用AI提取页面信息 ===");
            CompletableFuture<TaskResult> extractResult = agent.aiAction("提取仓库名称、描述和星标数量");
            result = extractResult.get(30, TimeUnit.SECONDS);
            logger.info("提取信息操作: {}", result.getStatus());
            
            // 使用AI验证页面状态
            logger.info("\n=== 使用AI验证页面状态 ===");
            CompletableFuture<TaskResult> assertResult = agent.aiAction("验证页面是否包含代码文件列表");
            result = assertResult.get(30, TimeUnit.SECONDS);
            logger.info("验证操作结果: {}", result.getStatus());
            
            // 冻结页面状态，进行批量操作
            logger.info("\n=== 冻结页面状态进行批量操作 ===");
            agent.freeze().get(5, TimeUnit.SECONDS);
            
            // 在冻结状态下执行多个操作
            CompletableFuture<TaskResult> scrollResult = agent.aiAction("向下滚动页面");
            result = scrollResult.get(30, TimeUnit.SECONDS);
            logger.info("滚动操作结果: {}", result.getStatus());
            
            // 解除页面状态冻结
            agent.unfreeze();
            logger.info("页面状态已解除冻结");
            
            logger.info("✅ 高级Web自动化示例执行成功!");
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("❌ 高级Web自动化示例执行失败: {}", e.getMessage(), e);
        } finally {
            // 清理资源
            if (agent != null) {
                try {
                    agent.close();
                } catch (Exception e) {
                    logger.error("关闭Agent时出错", e);
                }
            }
            
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    logger.error("关闭Playwright页面时出错", e);
                }
            }
            
            logger.info("示例执行完成");
        }
    }
}