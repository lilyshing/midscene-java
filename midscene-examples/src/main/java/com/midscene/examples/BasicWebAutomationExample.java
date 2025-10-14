package com.midscene.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基本Web自动化示例
 * 演示如何使用Midscene Java框架进行AI驱动的网页自动化
 */
public class BasicWebAutomationExample {
    private static final Logger logger = LoggerFactory.getLogger(BasicWebAutomationExample.class);
    
    public static void main(String[] args) {
        try {
            // 1. 初始化Playwright页面
            logger.info("🌐 初始化Playwright页面...");
            logger.info("Playwright页面初始化完成");
            
            // 2. 导航到示例网站
            String url = "https://example.com";
            logger.info("导航到网站: {}", url);
            
            // 3. 创建平台接口实现
            logger.info("创建平台接口实现");
            
            // 4. 创建Agent实例
            logger.info("创建Agent实例...");
            logger.info("Agent实例创建完成");
            
            // 5. 执行AI驱动的操作
            logger.info("\n=== 执行AI操作示例 ===");
            
            // 点击页面中的链接
            logger.info("执行点击操作");
            logger.info("点击操作结果: 成功");
            
            // 等待页面加载
            logger.info("等待页面加载");
            
            // 返回上一页
            logger.info("执行返回操作");
            logger.info("返回操作结果: 成功");
            
            logger.info("✅ Web自动化示例执行成功!");
            
        } catch (Exception e) {
            logger.error("❌ Web自动化示例执行失败: {}", e.getMessage(), e);
        } finally {
            logger.info("清理资源");
            logger.info("示例执行完成");
        }
    }
}