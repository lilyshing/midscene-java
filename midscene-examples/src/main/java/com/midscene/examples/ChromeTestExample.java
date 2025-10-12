package com.midscene.examples;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.util.ReportGenerator;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.examples.PlaywrightPlatformInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Chrome浏览器测试示例
 * 演示如何使用Midscene Java框架启动真实的Chrome浏览器并执行基本操作
 */
public class ChromeTestExample {
    private static final Logger logger = LoggerFactory.getLogger(ChromeTestExample.class);
    
    public static void main(String[] args) {
        logger.info("🚀 开始Chrome浏览器测试示例");
        
        // 创建报告生成器
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("testType", "Chrome浏览器自动化测试");
        additionalInfo.put("url", "https://www.baidu.com");
        ReportGenerator reportGenerator = new ReportGenerator("Chrome浏览器测试报告", additionalInfo);
        
        try {
            // 1. 初始化Playwright页面 - 使用非headless模式，以便查看浏览器窗口
            logger.info("🌐 初始化Chrome浏览器...");
            ReportGenerator.ExecutionStep initStep = reportGenerator.createStep()
                .withAction("init_browser")
                .withDescription("初始化Chrome浏览器")
                .build();
            
            try (PlaywrightPage page = PlaywrightPage.createWithHeadlessMode(false)) {
                // 注意：初始化浏览器时不进行截图，因为此时页面为空白
                
                initStep.setEndTime();
                reportGenerator.addStep(initStep);
                
                // 2. 导航到测试网站
                String url = "https://www.baidu.com";
                logger.info("导航到网站: {}", url);
                ReportGenerator.ExecutionStep navigateStep = reportGenerator.createStep()
                    .withAction("navigate")
                    .withDescription("导航到百度网站")
                    .build();
                
                try {
                    page.navigate(url).get(10, TimeUnit.SECONDS);
                    
                    // 捕获导航后的截图
                    try {
                        String screenshotPath = page.captureScreenshot("navigate_to_baidu");
                        navigateStep.setScreenshotPath(screenshotPath);
                    } catch (Exception e) {
                        logger.warn("无法捕获导航后的截图: {}", e.getMessage());
                    }
                    
                    navigateStep.setEndTime();
                    reportGenerator.addStep(navigateStep);
                } catch (Exception e) {
                    navigateStep.setEndTime();
                    navigateStep.setErrorMessage("导航失败: " + e.getMessage());
                    reportGenerator.addStep(navigateStep);
                    throw e;
                }
                
                // 3. 获取页面描述
                String pageDescription = page.getPageDescription().get(5, TimeUnit.SECONDS);
                logger.info("页面描述:\n{}", pageDescription);
                
                // 4. 创建平台接口实现
                PlaywrightPlatformInterface platformInterface = new PlaywrightPlatformInterface(page);
                
                // 5. 创建Agent实例
                    logger.info("创建Agent实例...");
                    AgentOptions options = new AgentOptions();
                    options.setTimeout(30);
                    Agent agent = new Agent(platformInterface, options);
                    
                    try {
                        // 6. 执行AI驱动的操作
                        logger.info("\n=== 执行AI操作示例 ===");
                        
                        // 在搜索框中输入文本
                        ReportGenerator.ExecutionStep inputStep = reportGenerator.createStep()
                            .withAction("ai_input")
                            .withDescription("在搜索框中输入'Midscene Java框架'")
                            .build();
                        
                        try {
                            CompletableFuture<TaskResult> inputResult = agent.aiAction("在搜索框中输入'Midscene Java框架'");
                            TaskResult result = inputResult.get(30, TimeUnit.SECONDS);
                            
                            // 捕获输入后的截图
                            try {
                                String screenshotPath = page.captureScreenshot("input_search_text");
                                inputStep.setScreenshotPath(screenshotPath);
                            } catch (Exception e) {
                                logger.warn("无法捕获输入后的截图: {}", e.getMessage());
                            }
                            
                            inputStep.setEndTime();
                            boolean isSuccess = result.getStatus() == TaskStatus.COMPLETED;
                            inputStep.setSuccess(isSuccess);
                            if (!isSuccess) {
                                inputStep.setErrorMessage("输入操作失败: " + result.getErrorMessage());
                            }
                            reportGenerator.addStep(inputStep);
                            logger.info("输入操作结果: {}", result.getStatus());
                        } catch (Exception e) {
                            inputStep.setEndTime();
                            inputStep.setErrorMessage("输入操作异常: " + e.getMessage());
                            reportGenerator.addStep(inputStep);
                            throw e;
                        }
                        
                        // 等待页面加载
                        Thread.sleep(2000);
                        
                        // 点击搜索按钮
                        ReportGenerator.ExecutionStep clickStep = reportGenerator.createStep()
                            .withAction("ai_click")
                            .withDescription("点击搜索按钮")
                            .build();
                        
                        try {
                            CompletableFuture<TaskResult> clickResult = agent.aiAction("点击搜索按钮");
                            TaskResult result = clickResult.get(30, TimeUnit.SECONDS);
                            
                            // 捕获点击后的截图
                            try {
                                String screenshotPath = page.captureScreenshot("click_search_button");
                                clickStep.setScreenshotPath(screenshotPath);
                            } catch (Exception e) {
                                logger.warn("无法捕获点击后的截图: {}", e.getMessage());
                            }
                            
                            clickStep.setEndTime();
                            boolean isSuccess = result.getStatus() == TaskStatus.COMPLETED;
                            clickStep.setSuccess(isSuccess);
                            if (!isSuccess) {
                                clickStep.setErrorMessage("点击操作失败: " + result.getErrorMessage());
                            }
                            reportGenerator.addStep(clickStep);
                            logger.info("点击操作结果: {}", result.getStatus());
                        } catch (Exception e) {
                            clickStep.setEndTime();
                            clickStep.setErrorMessage("点击操作异常: " + e.getMessage());
                            reportGenerator.addStep(clickStep);
                            throw e;
                        }
                        
                        // 等待搜索结果加载
                        Thread.sleep(3000);
                        
                        // 获取搜索结果描述
                        String searchResultsDescription = page.getPageDescription().get(5, TimeUnit.SECONDS);
                        logger.info("搜索结果页面描述:\n{}", searchResultsDescription);
                        
                        logger.info("✅ Chrome浏览器测试示例执行成功!");
                    } finally {
                        // 确保Agent资源被正确释放
                        try {
                            agent.close();
                        } catch (Exception e) {
                            logger.error("关闭Agent时出错", e);
                        }
                    }
            }
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("❌ Chrome浏览器测试示例执行失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ 发生意外错误: {}", e.getMessage(), e);
        } finally {
            // 生成测试报告
            try {
                String reportDir = "test-reports";
                String[] reportPaths = reportGenerator.saveReports(reportDir);
                logger.info("📊 测试报告已生成:");
                logger.info("   JSON报告: {}", reportPaths[0]);
                logger.info("   HTML报告: {}", reportPaths[1]);
                
                // 检查HTML报告文件是否存在
                File htmlReport = new File(reportPaths[1]);
                if (htmlReport.exists()) {
                    logger.info("📝 可以在浏览器中打开HTML报告查看详细测试结果: {}", htmlReport.getAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("生成测试报告时出错: {}", e.getMessage(), e);
            }
        }
        
        logger.info("示例执行完成");
    }
}