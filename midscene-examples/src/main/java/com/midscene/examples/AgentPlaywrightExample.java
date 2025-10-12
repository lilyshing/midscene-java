package com.midscene.examples;

import com.microsoft.playwright.*;
import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.agent.LocateResult;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.util.ReportGenerator;
import com.midscene.web.playwright.PlaywrightPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent与Playwright集成的示例
 */
public class AgentPlaywrightExample {
    private static final Logger logger = LoggerFactory.getLogger(AgentPlaywrightExample.class);

    public static void main(String[] args) {
        PlaywrightPage playwrightPage = null;
        Agent agent = null;
        
        // 创建报告生成器
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("testType", "Agent与Playwright集成测试");
        additionalInfo.put("url", "https://www.baidu.com");
        ReportGenerator reportGenerator = new ReportGenerator("AgentPlaywrightExample测试报告", additionalInfo);

        try {
            logger.info("步骤1: 初始化Playwright页面");
            ReportGenerator.ExecutionStep initStep = reportGenerator.createStep()
                .withAction("init_browser")
                .withDescription("初始化Playwright页面")
                .build();
            
            try {
                playwrightPage = PlaywrightPage.createWithHeadlessMode(false);
                initStep.setEndTime();
                reportGenerator.addStep(initStep);
            } catch (Exception e) {
                initStep.setEndTime();
                initStep.setErrorMessage("初始化Playwright页面失败: " + e.getMessage());
                reportGenerator.addStep(initStep);
                throw e;
            }
            
            logger.info("步骤2: 导航到百度网站");
            ReportGenerator.ExecutionStep navigateStep = reportGenerator.createStep()
                .withAction("navigate")
                .withDescription("导航到百度网站")
                .build();
            
            try {
                playwrightPage.navigate("https://www.baidu.com").get();
                Thread.sleep(2000); // 等待页面加载
                
                // 捕获导航后的截图
                try {
                    String screenshotPath = playwrightPage.captureScreenshot("navigate_to_baidu");
                    navigateStep.setScreenshotPath(screenshotPath);
                } catch (Exception e) {
                    logger.warn("无法捕获导航后的截图: {}", e.getMessage());
                }
                
                navigateStep.setEndTime();
                reportGenerator.addStep(navigateStep);
            } catch (Exception e) {
                navigateStep.setEndTime();
                navigateStep.setErrorMessage("导航到百度网站失败: " + e.getMessage());
                reportGenerator.addStep(navigateStep);
                throw e;
            }
            
            logger.info("步骤3: 创建平台接口");
            PlaywrightPlatformInterface platformInterface = new PlaywrightPlatformInterface(playwrightPage);
            
            logger.info("步骤4: 配置Agent选项");
            AgentOptions options = AgentOptions.builder()
                    .generateReport(true)
                    .reportFileName("AgentPlaywrightExample")
                    .timeout(30)
                    .retryCount(3)
                    .screenshotOnError(true)
                    .cacheEnabled(true)
                    .saveExecutionLogs(true)
                    .build();
            
            logger.info("步骤5: 创建Agent实例");
            agent = new Agent(platformInterface, options);
            
            logger.info("步骤6: 执行AI操作 - 在搜索框中输入关键词并搜索");
            ReportGenerator.ExecutionStep searchStep = reportGenerator.createStep()
                .withAction("ai_search")
                .withDescription("在搜索框中输入'人工智能'关键词并搜索")
                .build();
            
            try {
                CompletableFuture<TaskResult> searchResult = agent.aiAction("在搜索框中输入'人工智能'关键词并点击搜索按钮");
                TaskResult searchTaskResult = searchResult.get();
                logger.info("搜索操作结果: {}", searchTaskResult);
                
                // 捕获搜索后的截图
                try {
                    String screenshotPath = playwrightPage.captureScreenshot("search_ai_keyword");
                    searchStep.setScreenshotPath(screenshotPath);
                } catch (Exception e) {
                    logger.warn("无法捕获搜索后的截图: {}", e.getMessage());
                }
                
                searchStep.setEndTime();
                boolean isSuccess = searchTaskResult.getStatus() == TaskStatus.COMPLETED;
                searchStep.setSuccess(isSuccess);
                if (!isSuccess) {
                    searchStep.setErrorMessage("搜索操作失败: " + searchTaskResult.getErrorMessage());
                }
                reportGenerator.addStep(searchStep);
            } catch (Exception e) {
                searchStep.setEndTime();
                searchStep.setErrorMessage("搜索操作异常: " + e.getMessage());
                reportGenerator.addStep(searchStep);
                throw e;
            }
            
            // 等待页面加载
            Thread.sleep(3000);
            
            logger.info("步骤7: 执行AI操作 - 验证搜索结果页面");
            ReportGenerator.ExecutionStep verifyStep = reportGenerator.createStep()
                .withAction("ai_verify")
                .withDescription("验证搜索结果页面是否包含'人工智能'相关内容")
                .build();
            
            try {
                CompletableFuture<TaskResult> verifyResult = agent.aiAction("验证搜索结果页面是否包含'人工智能'相关内容");
                TaskResult verifyTaskResult = verifyResult.get();
                logger.info("验证操作结果: {}", verifyTaskResult);
                
                // 捕获验证后的截图
                try {
                    String screenshotPath = playwrightPage.captureScreenshot("verify_search_results");
                    verifyStep.setScreenshotPath(screenshotPath);
                } catch (Exception e) {
                    logger.warn("无法捕获验证后的截图: {}", e.getMessage());
                }
                
                verifyStep.setEndTime();
                boolean isSuccess = verifyTaskResult.getStatus() == TaskStatus.COMPLETED;
                verifyStep.setSuccess(isSuccess);
                if (!isSuccess) {
                    verifyStep.setErrorMessage("验证操作失败: " + verifyTaskResult.getErrorMessage());
                }
                reportGenerator.addStep(verifyStep);
            } catch (Exception e) {
                verifyStep.setEndTime();
                verifyStep.setErrorMessage("验证操作异常: " + e.getMessage());
                reportGenerator.addStep(verifyStep);
                throw e;
            }
            
            logger.info("步骤8: 执行AI操作 - 返回百度首页");
            ReportGenerator.ExecutionStep backStep = reportGenerator.createStep()
                .withAction("ai_back")
                .withDescription("返回百度首页")
                .build();
            
            try {
                CompletableFuture<TaskResult> backResult = agent.aiAction("返回百度首页");
                TaskResult backTaskResult = backResult.get();
                logger.info("返回操作结果: {}", backTaskResult);
                
                // 捕获返回后的截图
                try {
                    String screenshotPath = playwrightPage.captureScreenshot("back_to_baidu_homepage");
                    backStep.setScreenshotPath(screenshotPath);
                } catch (Exception e) {
                    logger.warn("无法捕获返回后的截图: {}", e.getMessage());
                }
                
                backStep.setEndTime();
                boolean isSuccess = backTaskResult.getStatus() == TaskStatus.COMPLETED;
                backStep.setSuccess(isSuccess);
                if (!isSuccess) {
                    backStep.setErrorMessage("返回操作失败: " + backTaskResult.getErrorMessage());
                }
                reportGenerator.addStep(backStep);
            } catch (Exception e) {
                backStep.setEndTime();
                backStep.setErrorMessage("返回操作异常: " + e.getMessage());
                reportGenerator.addStep(backStep);
                throw e;
            }
            
            logger.info("所有测试步骤执行完成");
            
        } catch (Exception e) {
            logger.error("测试执行过程中发生错误", e);
        } finally {
            // 清理资源
            if (agent != null) {
                try {
                    agent.close();
                    logger.info("Agent已关闭并释放资源");
                } catch (Exception e) {
                    logger.error("关闭Agent时发生错误", e);
                }
            }
            
            if (playwrightPage != null) {
                try {
                    logger.info("正在关闭 Playwright 页面...");
                    playwrightPage.close();
                } catch (Exception e) {
                    logger.error("关闭页面时发生错误", e);
                }
            }
            
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
            
            logger.info("测试示例执行完成");
        }
    }
}