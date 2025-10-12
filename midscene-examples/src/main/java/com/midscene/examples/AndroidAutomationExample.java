package com.midscene.examples;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.TaskResult;
import com.midscene.android.AndroidDevice;
import com.midscene.examples.AndroidPlatformInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Android自动化示例
 * 演示如何使用Midscene Java框架进行AI驱动的Android设备自动化
 */
public class AndroidAutomationExample {
    private static final Logger logger = LoggerFactory.getLogger(AndroidAutomationExample.class);
    
    public static void main(String[] args) {
        AndroidDevice device = null;
        Agent agent = null;
        
        try {
            // 1. 初始化Android设备
            logger.info("📱 初始化Android设备...");
            // 使用真实的Android设备，而不是模拟器
            device = new AndroidDevice();
            
            // 2. 连接到设备（在构造函数中自动完成）
            logger.info("已连接到Android设备: {}", device.getDeviceId());
            
            // 3. 创建平台接口实现
            AndroidPlatformInterface platformInterface = new AndroidPlatformInterface(device);
            
            // 4. 创建Agent实例
            logger.info("创建Agent实例...");
            AgentOptions options = AgentOptions.builder()
                .timeout(30)
                .retryCount(3)
                .screenshotOnError(true)
                .cacheEnabled(true)
                .build();
            
            agent = new Agent(platformInterface, options);
            
            // 5. 执行AI驱动的Android操作
            logger.info("\n=== 执行AI驱动Android操作示例 ===");
            
            // 启动设置应用
            CompletableFuture<TaskResult> launchResult = agent.aiAction("启动设置应用");
            TaskResult result = launchResult.get(30, TimeUnit.SECONDS);
            logger.info("启动应用操作结果: {}", result.getStatus());
            
            // 等待应用加载
            Thread.sleep(2000);
            
            // 点击WLAN设置
            CompletableFuture<TaskResult> wlanResult = agent.aiAction("点击WLAN设置");
            result = wlanResult.get(30, TimeUnit.SECONDS);
            logger.info("点击WLAN设置操作结果: {}", result.getStatus());
            
            // 等待页面加载
            Thread.sleep(2000);
            
            // 滑动到底部
            CompletableFuture<TaskResult> scrollResult = agent.aiAction("滑动到底部");
            result = scrollResult.get(30, TimeUnit.SECONDS);
            logger.info("滑动操作结果: {}", result.getStatus());
            
            // 使用AI提取网络信息
            logger.info("\n=== 使用AI提取网络信息 ===");
            CompletableFuture<TaskResult> extractResult = agent.aiAction("提取可用WiFi网络列表");
            result = extractResult.get(30, TimeUnit.SECONDS);
            logger.info("提取网络信息操作结果: {}", result.getStatus());
            
            // 返回主屏幕
            CompletableFuture<TaskResult> homeResult = agent.aiAction("返回主屏幕");
            result = homeResult.get(30, TimeUnit.SECONDS);
            logger.info("返回主屏幕操作结果: {}", result.getStatus());
            
            logger.info("✅ Android自动化示例执行成功!");
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("❌ Android自动化示例执行失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Android设备连接失败: {}", e.getMessage(), e);
        } finally {
            // 清理资源
            if (agent != null) {
                try {
                    agent.close();
                } catch (Exception e) {
                    logger.error("关闭Agent时出错", e);
                }
            }
            
            if (device != null) {
                try {
                    device.close();
                } catch (Exception e) {
                    logger.error("关闭Android设备连接时出错", e);
                }
            }
            
            logger.info("示例执行完成");
        }
    }
}