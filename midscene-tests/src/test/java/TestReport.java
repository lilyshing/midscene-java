import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * 测试报告生成器的演示类
 */
public class TestReport {
    private static final Logger logger = LoggerFactory.getLogger(TestReport.class);
    
    // 模拟的ExecutionStep类
    static class ExecutionStep {
        private String action;
        private String description;
        private boolean success;
        private String screenshotPath;
        private long durationMs;
        private Date timestamp;
        
        private ExecutionStep() {
            this.timestamp = new Date();
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
        
        public String getScreenshotPath() {
            return screenshotPath;
        }
        
        public long getDurationMs() {
            return durationMs;
        }
        
        public Date getTimestamp() {
            return timestamp;
        }
        
        // Builder内部类
        public static class Builder {
            private ExecutionStep step = new ExecutionStep();
            
            public Builder withAction(String action) {
                step.action = action;
                return this;
            }
            
            public Builder withDescription(String description) {
                step.description = description;
                return this;
            }
            
            public Builder withSuccess(boolean success) {
                step.success = success;
                return this;
            }
            
            public Builder withScreenshotPath(String screenshotPath) {
                step.screenshotPath = screenshotPath;
                return this;
            }
            
            public Builder withDurationMs(long durationMs) {
                step.durationMs = durationMs;
                return this;
            }
            
            public ExecutionStep build() {
                return step;
            }
        }
    }
    
    // 模拟的ReportGenerator类
    static class ReportGenerator {
        private String title;
        private Map<String, String> metadata;
        private List<ExecutionStep> steps;
        private Date startTime;
        
        public ReportGenerator(String title, Map<String, String> metadata) {
            this.title = title;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.steps = new ArrayList<>();
            this.startTime = new Date();
        }
        
        public ExecutionStep.Builder createStep() {
            return new ExecutionStep.Builder();
        }
        
        public void addStep(ExecutionStep step) {
            steps.add(step);
            logger.info("添加执行步骤: {} - {}", step.getAction(), step.getDescription());
        }
        
        public String generateJsonReport() {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");
            jsonBuilder.append("  \"title\": \"").append(title).append("\",\n");
            jsonBuilder.append("  \"metadata\": {\n");
            
            // 添加metadata
            int count = 0;
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                jsonBuilder.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
                if (count++ < metadata.size() - 1) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }
            
            jsonBuilder.append("  },\n");
            jsonBuilder.append("  \"steps\": [\n");
            
            // 添加steps
            for (int i = 0; i < steps.size(); i++) {
                ExecutionStep step = steps.get(i);
                jsonBuilder.append("    {\n");
                jsonBuilder.append("      \"action\": \"").append(step.getAction()).append("\",\n");
                jsonBuilder.append("      \"description\": \"").append(step.getDescription()).append("\",\n");
                jsonBuilder.append("      \"success\": ").append(step.isSuccess()).append(",\n");
                if (step.getScreenshotPath() != null) {
                    jsonBuilder.append("      \"screenshotPath\": \"").append(step.getScreenshotPath()).append("\",\n");
                }
                jsonBuilder.append("      \"timestamp\": \"").append(step.getTimestamp()).append("\"\n");
                jsonBuilder.append("    ");
                if (i < steps.size() - 1) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }
            
            jsonBuilder.append("  ]\n");
            jsonBuilder.append("}");
            
            String json = jsonBuilder.toString();
            logger.info("生成JSON报告");
            return json;
        }
        
        public String generateHtmlReport() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n");
            htmlBuilder.append("<html>\n");
            htmlBuilder.append("<head>\n");
            htmlBuilder.append("  <title>").append(title).append("</title>\n");
            htmlBuilder.append("  <style>\n");
            htmlBuilder.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            htmlBuilder.append("    h1 { color: #333; }\n");
            htmlBuilder.append("    .metadata { background: #f5f5f5; padding: 10px; margin-bottom: 20px; }\n");
            htmlBuilder.append("    table { border-collapse: collapse; width: 100%; }\n");
            htmlBuilder.append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            htmlBuilder.append("    th { background-color: #4CAF50; color: white; }\n");
            htmlBuilder.append("    .success { background-color: #e8f5e9; }\n");
            htmlBuilder.append("    .failure { background-color: #ffebee; }\n");
            htmlBuilder.append("  </style>\n");
            htmlBuilder.append("</head>\n");
            htmlBuilder.append("<body>\n");
            
            htmlBuilder.append("<h1>").append(title).append("</h1>\n");
            
            // 添加metadata
            htmlBuilder.append("<div class=\"metadata\">\n");
            htmlBuilder.append("  <h3>环境信息</h3>\n");
            htmlBuilder.append("  <ul>\n");
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                htmlBuilder.append("    <li><strong>").append(entry.getKey()).append(":</strong> ")
                          .append(entry.getValue()).append("</li>\n");
            }
            htmlBuilder.append("  </ul>\n");
            htmlBuilder.append("</div>\n");
            
            // 添加步骤表格
            htmlBuilder.append("<h2>执行步骤</h2>\n");
            htmlBuilder.append("<table>\n");
            htmlBuilder.append("  <tr>\n");
            htmlBuilder.append("    <th>操作</th>\n");
            htmlBuilder.append("    <th>描述</th>\n");
            htmlBuilder.append("    <th>状态</th>\n");
            htmlBuilder.append("    <th>截图</th>\n");
            htmlBuilder.append("    <th>时间</th>\n");
            htmlBuilder.append("  </tr>\n");
            
            for (ExecutionStep step : steps) {
                String statusClass = step.isSuccess() ? "success" : "failure";
                htmlBuilder.append("  <tr class=\"").append(statusClass).append("\">\n");
                htmlBuilder.append("    <td>").append(step.getAction()).append("</td>\n");
                htmlBuilder.append("    <td>").append(step.getDescription()).append("</td>\n");
                htmlBuilder.append("    <td>").append(step.isSuccess() ? "成功" : "失败").append("</td>\n");
                
                if (step.getScreenshotPath() != null) {
                    htmlBuilder.append("    <td><img src=\"").append(step.getScreenshotPath())
                              .append("\" alt=\"Screenshot\" width=\"200\" /></td>\n");
                } else {
                    htmlBuilder.append("    <td>-</td>\n");
                }
                
                htmlBuilder.append("    <td>").append(step.getTimestamp()).append("</td>\n");
                htmlBuilder.append("  </tr>\n");
            }
            
            htmlBuilder.append("</table>\n");
            htmlBuilder.append("</body>\n");
            htmlBuilder.append("</html>");
            
            String html = htmlBuilder.toString();
            logger.info("生成HTML报告");
            return html;
        }
    }
    
    public static void main(String[] args) {
        logger.info("启动测试报告生成器演示");
        
        // 创建测试报告生成器
        ReportGenerator reportGenerator = new ReportGenerator(
            "测试报告",
            Map.of("environment", "test", "version", "1.0.0")
        );
        
        // 测试添加执行步骤
        ExecutionStep step = reportGenerator.createStep()
            .withAction("click")
            .withDescription("点击按钮")
            .withSuccess(true)
            .build();
        
        reportGenerator.addStep(step);
        
        // 验证生成的JSON报告包含添加的步骤
        String jsonReport = reportGenerator.generateJsonReport();
        logger.info("JSON报告: {}", jsonReport);
        
        // 测试带截图的步骤
        ExecutionStep screenshotStep = reportGenerator.createStep()
            .withAction("screenshot")
            .withDescription("截图")
            .withSuccess(true)
            .withScreenshotPath("test.png")
            .build();
        
        reportGenerator.addStep(screenshotStep);
        
        // 验证JSON报告包含截图路径
        String jsonReportWithScreenshot = reportGenerator.generateJsonReport();
        logger.info("带截图的JSON报告: {}", jsonReportWithScreenshot);
        
        // 验证HTML报告包含截图
        String htmlReport = reportGenerator.generateHtmlReport();
        logger.info("带截图的HTML报告: {}", htmlReport);
        
        logger.info("测试报告生成器演示完成");
    }
}