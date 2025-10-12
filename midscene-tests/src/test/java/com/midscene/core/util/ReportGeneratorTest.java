package com.midscene.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ReportGenerator类的单元测试
 */
class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 创建测试用的报告生成器
        reportGenerator = new ReportGenerator(
            "Test Report",
            Map.of("environment", "test", "version", "1.0.0")
        );
    }

    @Test
    void testAddStep() {
        // 测试添加执行步骤
        ReportGenerator.ExecutionStep step = reportGenerator.createStep()
            .withAction("click")
            .withDescription("点击按钮")
            .withSuccess(true)
            .build();
        
        reportGenerator.addStep(step);
        
        // 验证生成的JSON报告包含添加的步骤
        String jsonReport = reportGenerator.generateJsonReport();
        System.out.println("JSON Report: " + jsonReport);
        
        // 将JSON报告写入文件以便调试
        try {
            String filePath = "debug_report.json";
            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            writer.write(jsonReport);
            writer.close();
            System.out.println("JSON report written to: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to write JSON report to file: " + e.getMessage());
        }
        
        // 添加调试信息
        System.out.println("Generated JSON report:");
        System.out.println(jsonReport);
        System.out.println("JSON length: " + jsonReport.length());
        
        System.out.println("Checking for \"action\":\"click\"");
        boolean hasActionClick = jsonReport.contains("\"action\":\"click\"");
        System.out.println("Found \"action\":\"click\": " + hasActionClick);
        assertTrue(jsonReport.contains("\"action\":\"click\""));
        
        System.out.println("Checking for \"description\":\"点击按钮\"");
        boolean hasDescription = jsonReport.contains("\"description\":\"点击按钮\"");
        System.out.println("Found \"description\":\"点击按钮\": " + hasDescription);
        
        // 检查Unicode编码的中文
        System.out.println("Checking for Unicode encoded description");
        boolean hasUnicodeDescription = jsonReport.contains("\"description\":\"\\u70b9\\u51fb\\u6309\\u94ae\"");
        System.out.println("Found Unicode description: " + hasUnicodeDescription);
        
        // 任一格式匹配即可
        assertTrue(hasDescription || hasUnicodeDescription);
        
        // 检查success字段的确切格式
        System.out.println("Checking for \"success\":true");
        boolean foundSuccessTrue = jsonReport.contains("\"success\":true");
        System.out.println("Found \"success\":true: " + foundSuccessTrue);
        
        System.out.println("Checking for \"success\": true");
        boolean foundSuccessWithSpace = jsonReport.contains("\"success\": true");
        System.out.println("Found \"success\": true: " + foundSuccessWithSpace);
        
        if (foundSuccessTrue) {
            System.out.println("Found \"success\":true");
            assertTrue(true);
        } else if (foundSuccessWithSpace) {
            System.out.println("Found \"success\": true");
            assertTrue(true);
        } else {
            System.out.println("Could not find success field with true value");
            System.out.println("Looking for any success field...");
            int successIndex = jsonReport.indexOf("success");
            if (successIndex >= 0) {
                System.out.println("Found 'success' at index " + successIndex);
                System.out.println("Context: " + jsonReport.substring(Math.max(0, successIndex-10), Math.min(jsonReport.length(), successIndex+20)));
            }
            
            // 尝试解析JSON并检查success字段
            try {
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonReport);
                boolean successValue = rootNode.get("success").asBoolean();
                System.out.println("Parsed success value: " + successValue);
                if (successValue) {
                    System.out.println("Success field is true in parsed JSON");
                    assertTrue(true);
                } else {
                    System.out.println("Success field is false in parsed JSON");
                    fail("Success field is false in parsed JSON");
                }
            } catch (Exception e) {
                System.out.println("Failed to parse JSON: " + e.getMessage());
                fail("Could not find success field with true value");
            }
        }
    }

    @Test
    void testGenerateJsonReport() throws IOException {
        // 添加成功和失败的步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("login")
            .withDescription("用户登录")
            .withSuccess(true)
            .build());
        
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("click")
            .withDescription("点击提交按钮")
            .withSuccess(false)
            .withErrorMessage("按钮未找到")
            .build());
        
        // 生成并解析JSON报告
        String jsonReport = reportGenerator.generateJsonReport();
        JsonNode rootNode = objectMapper.readTree(jsonReport);
        
        // 验证报告基本信息
        assertEquals("Test Report", rootNode.get("title").asText());
        assertFalse(rootNode.get("success").asBoolean()); // 因为有失败步骤
        assertEquals(2, rootNode.get("totalSteps").asInt());
        assertEquals(1, rootNode.get("successfulSteps").asInt());
        
        // 验证额外信息
        JsonNode additionalInfo = rootNode.get("additionalInfo");
        assertEquals("test", additionalInfo.get("environment").asText());
        assertEquals("1.0.0", additionalInfo.get("version").asText());
        
        // 验证步骤信息
        assertEquals(2, rootNode.get("steps").size());
        assertEquals("login", rootNode.get("steps").get(0).get("action").asText());
        assertTrue(rootNode.get("steps").get(0).get("success").asBoolean());
        assertEquals("click", rootNode.get("steps").get(1).get("action").asText());
        assertFalse(rootNode.get("steps").get(1).get("success").asBoolean());
        assertEquals("按钮未找到", rootNode.get("steps").get(1).get("errorMessage").asText());
    }

    @Test
    void testGenerateHtmlReport() {
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test_action")
            .withDescription("测试操作")
            .withSuccess(true)
            .build());
        
        // 生成HTML报告
        String htmlReport = reportGenerator.generateHtmlReport();
        
        // 验证HTML报告包含必要的元素
        assertTrue(htmlReport.contains("<!DOCTYPE html>"));
        assertTrue(htmlReport.contains("<title>Test Report</title>"));
        assertTrue(htmlReport.contains("test_action"));
        assertTrue(htmlReport.contains("测试操作"));
        assertTrue(htmlReport.contains("step-success"));
        assertTrue(htmlReport.contains("执行摘要"));
        assertTrue(htmlReport.contains("执行步骤"));
    }

    @Test
    void testSaveJsonReport() throws IOException {
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build());
        
        // 保存到临时目录
        String tempDir = System.getProperty("java.io.tmpdir") + "/midscene-test";
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String jsonFileName = "Test_Report_" + timestamp + ".json";
        String filePath = tempDir + File.separator + jsonFileName;
        
        reportGenerator.saveJsonReport(filePath);
        
        // 验证文件存在
        File file = new File(filePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertTrue(file.length() > 0);
        
        // 验证文件内容
        JsonNode rootNode = objectMapper.readTree(file);
        assertEquals("Test Report", rootNode.get("title").asText());
        
        // 清理测试文件
        file.delete();
    }

    @Test
    void testSaveHtmlReport() throws IOException {
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build());
        
        // 保存到临时目录
        String tempDir = System.getProperty("java.io.tmpdir") + "/midscene-test";
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String htmlFileName = "Test_Report_" + timestamp + ".html";
        String filePath = tempDir + File.separator + htmlFileName;
        
        reportGenerator.saveHtmlReport(filePath);
        
        // 验证文件存在
        File file = new File(filePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertTrue(file.length() > 0);
        
        // 清理测试文件
        file.delete();
    }

    @Test
    void testSaveReports() throws IOException {
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build());
        
        // 保存两个格式的报告
        String tempDir = System.getProperty("java.io.tmpdir") + "/midscene-test";
        String[] filePaths = reportGenerator.saveReports(tempDir);
        
        // 验证返回两个文件路径
        assertEquals(2, filePaths.length);
        
        // 验证文件存在
        for (String filePath : filePaths) {
            File file = new File(filePath);
            assertTrue(file.exists());
            assertTrue(file.isFile());
            assertTrue(file.length() > 0);
            
            // 清理测试文件
            file.delete();
        }
    }

    @Test
    void testExecutionStepWithScreenshot() throws IOException {
        // 创建临时截图文件
        String tempDir = System.getProperty("java.io.tmpdir");
        String screenshotPath = tempDir + File.separator + "test.png";
        File screenshotFile = new File(screenshotPath);
        
        try {
            // 创建一个简单的PNG文件（1x1像素的透明PNG）
            byte[] pngData = {
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, // IHDR chunk length
                0x49, 0x48, 0x44, 0x52, // IHDR
                0x00, 0x00, 0x00, 0x01, // width: 1
                0x00, 0x00, 0x00, 0x01, // height: 1
                0x08, 0x06, 0x00, 0x00, 0x00, // bit depth, color type, compression, filter, interlace
                0x1F, 0x15, (byte)0xC4, (byte)0x89, // CRC
                0x00, 0x00, 0x00, 0x0A, // IDAT chunk length
                0x49, 0x44, 0x41, 0x54, // IDAT
                0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, // compressed data
                0x0D, 0x0A, 0x2D, (byte)0xB4, // CRC
                0x00, 0x00, 0x00, 0x00, // IEND chunk length
                0x49, 0x45, 0x4E, 0x44, // IEND
                (byte)0xAE, 0x42, 0x60, (byte)0x82 // CRC
            };
            
            try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                fos.write(pngData);
            }
            
            // 测试带截图的步骤
            ReportGenerator.ExecutionStep step = reportGenerator.createStep()
                .withAction("screenshot")
                .withDescription("截图")
                .withSuccess(true)
                .withScreenshotPath(screenshotPath)
                .build();
            
            reportGenerator.addStep(step);
            
            // 验证JSON报告包含截图路径
            String jsonReport = reportGenerator.generateJsonReport();
            System.out.println("JSON Report with screenshot: " + jsonReport);
            assertTrue(jsonReport.contains("\"screenshotPath\":\"" + screenshotPath.replace("\\", "\\\\") + "\""));
            
            // 验证HTML报告包含截图
            String htmlReport = reportGenerator.generateHtmlReport();
            System.out.println("HTML Report with screenshot: " + htmlReport);
            
            // 检查HTML中是否包含img标签和截图路径
            boolean hasImgTag = htmlReport.contains("<img");
            boolean hasScreenshotPath = htmlReport.contains(screenshotPath);
            
            System.out.println("Has img tag: " + hasImgTag);
            System.out.println("Has screenshot path: " + hasScreenshotPath);
            
            if (hasImgTag && hasScreenshotPath) {
                assertTrue(true);
            } else {
                // 查找img标签的具体内容
                int imgIndex = htmlReport.indexOf("<img");
                if (imgIndex >= 0) {
                    System.out.println("Found img at index " + imgIndex);
                    System.out.println("Context: " + htmlReport.substring(imgIndex, Math.min(htmlReport.length(), imgIndex + 200)));
                }
                fail("HTML report does not contain img tag with screenshot path");
            }
        } finally {
            // 清理测试文件
            if (screenshotFile.exists()) {
                screenshotFile.delete();
            }
        }
    }

    @Test
    void testExecutionStepDuration() throws InterruptedException {
        // 测试步骤持续时间计算
        ReportGenerator.ExecutionStep step = reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build();
        
        // 等待一小段时间
        Thread.sleep(100);
        
        // 手动设置结束时间
        step.setEndTime();
        
        // 验证持续时间大于0
        assertTrue(step.getDurationMs() > 0);
    }
}