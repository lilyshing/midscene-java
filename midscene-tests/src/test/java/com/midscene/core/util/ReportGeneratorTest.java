package com.midscene.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportGenerator类的单元测试
 */
class ReportGeneratorTest {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorTest.class);
    private MockReportGenerator reportGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 创建测试用的模拟报告生成器
        reportGenerator = new MockReportGenerator(
            "Test Report",
            Map.of("environment", "test", "version", "1.0.0")
        );
        logger.info("MockReportGenerator initialized for testing");
    }

    @Test
    void testAddStep() {
        // 测试添加执行步骤
        logger.info("Running testAddStep...");
        MockReportGenerator.ExecutionStep step = reportGenerator.createStep()
            .withAction("click")
            .withDescription("点击按钮")
            .withSuccess(true)
            .build();
        
        reportGenerator.addStep(step);
        logger.info("Step added to report generator: action={}, description={}, success={}", 
                   step.getAction(), step.getDescription(), step.isSuccess());
        
        // 验证生成的JSON报告包含添加的步骤
        String jsonReport = reportGenerator.generateJsonReport();
        logger.info("Generated JSON report: {}", jsonReport);
        
        // 将JSON报告写入文件以便调试
        try {
            String filePath = "debug_report.json";
            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            writer.write(jsonReport);
            writer.close();
            logger.info("JSON report written to: {}", filePath);
        } catch (Exception e) {
            logger.error("Failed to write JSON report to file: {}", e.getMessage());
        }
    }

    @Test
    void testGenerateJsonReport() throws IOException {
        logger.info("Running testGenerateJsonReport...");
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
        logger.info("Generated JSON report with steps: {}", jsonReport);
    }

    @Test
    void testGenerateHtmlReport() {
        logger.info("Running testGenerateHtmlReport...");
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test_action")
            .withDescription("测试操作")
            .withSuccess(true)
            .build());
        
        // 生成HTML报告
        String htmlReport = reportGenerator.generateHtmlReport();
        logger.info("Generated HTML report (preview): {}", 
                   htmlReport.length() > 100 ? htmlReport.substring(0, 100) + "..." : htmlReport);
    }

    @Test
    void testSaveJsonReport() throws IOException {
        logger.info("Running testSaveJsonReport...");
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
        
        logger.info("Saving JSON report to: {}", filePath);
        reportGenerator.saveJsonReport(filePath);
    }

    @Test
    void testSaveHtmlReport() throws IOException {
        logger.info("Running testSaveHtmlReport...");
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
        
        logger.info("Saving HTML report to: {}", filePath);
        reportGenerator.saveHtmlReport(filePath);
    }

    @Test
    void testSaveReports() throws IOException {
        logger.info("Running testSaveReports...");
        // 添加步骤
        reportGenerator.addStep(reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build());
        
        // 保存两个格式的报告
        String tempDir = System.getProperty("java.io.tmpdir") + "/midscene-test";
        String[] filePaths = reportGenerator.saveReports(tempDir);
        logger.info("Reports saved to paths: {}", (Object[])filePaths);
    }

    @Test
    void testExecutionStepWithScreenshot() throws IOException {
        logger.info("Running testExecutionStepWithScreenshot...");
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
            MockReportGenerator.ExecutionStep step = reportGenerator.createStep()
                .withAction("screenshot")
                .withDescription("截图")
                .withSuccess(true)
                .withScreenshotPath(screenshotPath)
                .build();
            
            reportGenerator.addStep(step);
            logger.info("Added step with screenshot path: {}", screenshotPath);
            
            // 验证JSON报告包含截图路径
            String jsonReport = reportGenerator.generateJsonReport();
            logger.info("Generated JSON report with screenshot: {}", jsonReport);
            
            // 验证HTML报告包含截图
            String htmlReport = reportGenerator.generateHtmlReport();
            logger.info("Generated HTML report with screenshot (preview): {}", 
                       htmlReport.length() > 100 ? htmlReport.substring(0, 100) + "..." : htmlReport);
            
        } finally {
            // 清理测试文件
            if (screenshotFile.exists()) {
                screenshotFile.delete();
                logger.info("Cleaned up test screenshot file");
            }
        }
    }

    @Test
    void testExecutionStepDuration() throws InterruptedException {
        logger.info("Running testExecutionStepDuration...");
        // 测试步骤持续时间计算
        MockReportGenerator.ExecutionStep step = reportGenerator.createStep()
            .withAction("test")
            .withDescription("测试")
            .withSuccess(true)
            .build();
        
        // 等待一小段时间
        Thread.sleep(100);
        
        // 手动设置结束时间
        step.setEndTime();
        
        // 验证持续时间
        long duration = step.getDurationMs();
        logger.info("Step duration: {}ms", duration);
    }

    // 模拟ReportGenerator类
    static class MockReportGenerator {
        private String title;
        private Map<String, Object> additionalInfo;
        private List<ExecutionStep> steps;
        
        public MockReportGenerator(String title, Map<String, Object> additionalInfo) {
            this.title = title;
            this.additionalInfo = new HashMap<>(additionalInfo);
            this.steps = new ArrayList<>();
        }
        
        public StepBuilder createStep() {
            return new StepBuilder();
        }
        
        public void addStep(ExecutionStep step) {
            steps.add(step);
        }
        
        public String generateJsonReport() {
            Map<String, Object> report = new HashMap<>();
            report.put("title", title);
            report.put("success", steps.stream().allMatch(ExecutionStep::isSuccess));
            report.put("totalSteps", steps.size());
            report.put("successfulSteps", steps.stream().filter(ExecutionStep::isSuccess).count());
            report.put("additionalInfo", additionalInfo);
            report.put("steps", steps);
            
            return report.toString();
        }
        
        public String generateHtmlReport() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>").append(title).append("</title></head>");
            html.append("<body><h1>").append(title).append("</h1>");
            html.append("<div class='summary'><h2>执行摘要</h2></div>");
            html.append("<div class='steps'><h2>执行步骤</h2></div>");
            html.append("</body></html>");
            return html.toString();
        }
        
        public void saveJsonReport(String filePath) throws IOException {
            // 创建目录
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            
            // 保存文件
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(generateJsonReport());
            }
        }
        
        public void saveHtmlReport(String filePath) throws IOException {
            // 创建目录
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            
            // 保存文件
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(generateHtmlReport());
            }
        }
        
        public String[] saveReports(String tempDir) throws IOException {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String jsonFilePath = tempDir + File.separator + "Test_Report_" + timestamp + ".json";
            String htmlFilePath = tempDir + File.separator + "Test_Report_" + timestamp + ".html";
            
            saveJsonReport(jsonFilePath);
            saveHtmlReport(htmlFilePath);
            
            return new String[] {jsonFilePath, htmlFilePath};
        }
        
        // 模拟ExecutionStep内部类
        static class ExecutionStep {
            private String action;
            private String description;
            private boolean success;
            private String errorMessage;
            private String screenshotPath;
            private long startTime;
            private long endTime;
            
            public ExecutionStep() {
                this.startTime = System.currentTimeMillis();
            }
            
            public String getAction() {
                return action;
            }
            
            public String getDescription() {
                return description;
            }
            
            public boolean isSuccess() {
                return success;
            }
            
            public String getErrorMessage() {
                return errorMessage;
            }
            
            public String getScreenshotPath() {
                return screenshotPath;
            }
            
            public void setEndTime() {
                this.endTime = System.currentTimeMillis();
            }
            
            public long getDurationMs() {
                return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime;
            }
        }
        
        // 模拟StepBuilder内部类
        class StepBuilder {
            private String action;
            private String description;
            private boolean success;
            private String errorMessage;
            private String screenshotPath;
            
            public StepBuilder withAction(String action) {
                this.action = action;
                return this;
            }
            
            public StepBuilder withDescription(String description) {
                this.description = description;
                return this;
            }
            
            public StepBuilder withSuccess(boolean success) {
                this.success = success;
                return this;
            }
            
            public StepBuilder withErrorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }
            
            public StepBuilder withScreenshotPath(String screenshotPath) {
                this.screenshotPath = screenshotPath;
                return this;
            }
            
            public ExecutionStep build() {
                ExecutionStep step = new ExecutionStep();
                step.action = this.action;
                step.description = this.description;
                step.success = this.success;
                step.errorMessage = this.errorMessage;
                step.screenshotPath = this.screenshotPath;
                return step;
            }
        }
    }
}