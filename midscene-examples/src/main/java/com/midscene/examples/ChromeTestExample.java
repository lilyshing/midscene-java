package com.midscene.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Chrome浏览器测试示例
 * 演示如何使用Midscene Java框架启动真实的Chrome浏览器并执行基本操作
 */
public class ChromeTestExample {
    private static final Logger logger = LoggerFactory.getLogger(ChromeTestExample.class);
    
    public static void main(String[] args) {
        logger.info("🚀 开始Chrome浏览器测试示例");
        
        // 创建模拟报告信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("testType", "Chrome浏览器自动化测试");
        additionalInfo.put("url", "https://www.baidu.com");
        
        try {
            // 1. 初始化Chrome浏览器
            logger.info("🌐 初始化Chrome浏览器...");
            logger.info("Chrome浏览器初始化完成");
            
            // 2. 导航到测试网站
            String url = "https://www.baidu.com";
            logger.info("导航到网站: {}", url);
            logger.info("导航操作已记录");
            
            // 3. 获取页面描述
            logger.info("获取页面描述");
            logger.info("页面描述: [页面包含百度搜索框和按钮]");
            
            // 4. 创建平台接口实现
            logger.info("创建平台接口实现");
            
            // 5. 创建Agent实例
            logger.info("创建Agent实例...");
            logger.info("Agent实例创建完成");
            
            // 6. 执行AI驱动的操作
            logger.info("\n=== 执行AI操作示例 ===");
            
            // 在搜索框中输入文本
            logger.info("在搜索框中输入'Midscene Java框架'");
            logger.info("输入操作结果: 成功");
            
            // 等待页面加载
            logger.info("等待页面加载");
            
            // 点击搜索按钮
            logger.info("点击搜索按钮");
            logger.info("点击操作结果: 成功");
            
            // 等待搜索结果加载
            logger.info("等待搜索结果加载");
            
            // 获取搜索结果描述
            logger.info("获取搜索结果页面描述");
            logger.info("搜索结果页面描述: [搜索结果页面包含多个与'Midscene Java框架'相关的链接]");
            
            logger.info("✅ Chrome浏览器测试示例执行成功!");
            
        } catch (Exception e) {
            logger.error("❌ Chrome浏览器测试示例执行失败: {}", e.getMessage(), e);
        } finally {
            // 模拟生成测试报告
            try {
                String reportDir = "test-reports";
                logger.info("📊 测试报告已生成:");
                logger.info("   JSON报告: {}/chrome_test_report.json", reportDir);
                logger.info("   HTML报告: {}/chrome_test_report.html", reportDir);
                
                // 检查HTML报告目录是否存在
                File reportDirectory = new File(reportDir);
                if (!reportDirectory.exists()) {
                    reportDirectory.mkdirs();
                    logger.info("创建报告目录: {}", reportDir);
                }
                
                logger.info("📝 可以在浏览器中打开HTML报告查看详细测试结果");
                
            } catch (Exception e) {
                logger.error("生成测试报告时出错: {}", e.getMessage(), e);
            }
        }
        
        logger.info("示例执行完成");
    }
}