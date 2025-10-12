package com.midscene.examples;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.TaskResult;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.android.AndroidDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 综合自动化示例
 * 演示如何在一个应用中同时使用Web和Android自动化
 */
public class CombinedAutomationExample {
    private static final Logger logger = LoggerFactory.getLogger(CombinedAutomationExample.class);
    
    public static void main(String[] args) {
        PlaywrightPage webPage = null;
        Agent webAgent = null;
        AndroidDevice androidDevice = null;
        Agent androidAgent = null;
        
        try {
            // 1. 初始化Web自动化
            logger.info("🌐 初始化Web自动化...");
            webPage = new PlaywrightPage();
            webPage.navigate("https://example.com").get(10, TimeUnit.SECONDS);
            
            PlaywrightPlatformInterface webPlatformInterface = new PlaywrightPlatformInterface(webPage);
            webAgent = new Agent(webPlatformInterface);
            
            // 2. 初始化Android自动化
            logger.info("📱 初始化Android自动化...");
            try {
                androidDevice = new AndroidDevice();
                logger.info("已连接到Android设备: {}", androidDevice.getDeviceId());
                
                AndroidPlatformInterface androidPlatformInterface = new AndroidPlatformInterface(androidDevice);
                androidAgent = new Agent(androidPlatformInterface);
            } catch (Exception e) {
                logger.warn("Android设备初始化失败，将继续仅使用Web自动化: {}", e.getMessage());
            }
            
            // 3. 执行Web自动化操作
            logger.info("\n=== 执行Web自动化操作 ===");
            CompletableFuture<TaskResult> webResult = webAgent.aiAction("点击页面中的第一个链接");
            TaskResult result = webResult.get(30, TimeUnit.SECONDS);
            logger.info("Web操作结果: {}", result.getStatus());
            
            // 等待页面加载
            Thread.sleep(2000);
            
            // 返回上一页
            CompletableFuture<TaskResult> backResult = webAgent.aiAction("返回上一页");
            result = backResult.get(30, TimeUnit.SECONDS);
            logger.info("Web返回操作结果: {}", result.getStatus());
            
            // 4. 如果Android设备可用，执行Android自动化操作
            if (androidAgent != null) {
                logger.info("\n=== 执行Android自动化操作 ===");
                
                // 启动设置应用
                CompletableFuture<TaskResult> launchResult = androidAgent.aiAction("启动设置应用");
                result = launchResult.get(30, TimeUnit.SECONDS);
                logger.info("Android启动应用操作结果: {}", result.getStatus());
                
                // 等待应用加载
                Thread.sleep(2000);
                
                // 返回主屏幕
                CompletableFuture<TaskResult> homeResult = androidAgent.aiAction("返回主屏幕");
                result = homeResult.get(30, TimeUnit.SECONDS);
                logger.info("Android返回主屏幕操作结果: {}", result.getStatus());
            }
            
            // 5. 执行跨平台协调操作示例
            logger.info("\n=== 执行跨平台协调操作示例 ===");
            
            // 在Web上执行操作
            CompletableFuture<TaskResult> webExtractResult = webAgent.aiAction("提取页面标题和主要内容");
            result = webExtractResult.get(30, TimeUnit.SECONDS);
            logger.info("Web信息提取结果: {}", result.getStatus());
            
            // 如果Android设备可用，在Android上执行相关操作
            if (androidAgent != null) {
                // 模拟在Android设备上执行与Web内容相关的操作
                CompletableFuture<TaskResult> androidActionResult = androidAgent.aiAction("打开浏览器应用");
                result = androidActionResult.get(30, TimeUnit.SECONDS);
                logger.info("Android浏览器操作结果: {}", result.getStatus());
            }
            
            logger.info("✅ 综合自动化示例执行成功!");
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("❌ 综合自动化示例执行失败: {}", e.getMessage(), e);
        } finally {
            // 清理Web资源
            if (webAgent != null) {
                try {
                    webAgent.close();
                } catch (Exception e) {
                    logger.error("关闭Web Agent时出错", e);
                }
            }
            
            if (webPage != null) {
                try {
                    webPage.close();
                } catch (Exception e) {
                    logger.error("关闭Playwright页面时出错", e);
                }
            }
            
            // 清理Android资源
            if (androidAgent != null) {
                try {
                    androidAgent.close();
                } catch (Exception e) {
                    logger.error("关闭Android Agent时出错", e);
                }
            }
            
            if (androidDevice != null) {
                try {
                    androidDevice.close();
                } catch (Exception e) {
                    logger.error("关闭Android设备连接时出错", e);
                }
            }
            
            logger.info("示例执行完成");
        }
    }
}