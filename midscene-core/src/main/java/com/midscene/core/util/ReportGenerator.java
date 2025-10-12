package com.midscene.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告生成器，用于生成执行报告
 */
public class ReportGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private String reportTitle = "执行报告";
    private final List<ExecutionStep> executionSteps = new ArrayList<>();
    private Map<String, Object> additionalInfo;
    private boolean isSuccessful = true;
    
    /**
     * 内部类，用于表示执行步骤
     */
    public static class ExecutionStep {
        private final String action;
        private final String description;
        private final long startTime;
        private long endTime;
        private long durationMs;
        private boolean success = true;
        private String errorMessage;
        private String screenshotPath;
        
        /**
         * 构造函数
         * @param action 操作名称
         * @param description 描述
         */
        public ExecutionStep(String action, String description) {
            this.action = action;
            this.description = description;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * 获取操作名称
         * @return 操作名称
         */
        public String getAction() {
            return action;
        }
        
        /**
         * 获取描述
         * @return 描述
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * 获取开始时间戳
         * @return 开始时间戳
         */
        public long getStartTime() {
            return startTime;
        }
        
        /**
         * 获取结束时间戳
         * @return 结束时间戳
         */
        public long getEndTime() {
            return endTime;
        }
        
        /**
         * 设置结束时间
         */
        public void setEndTime() {
            this.endTime = System.currentTimeMillis();
            this.durationMs = this.endTime - this.startTime;
        }
        
        /**
         * 获取持续时间
         * @return 持续时间（毫秒）
         */
        public long getDurationMs() {
            return durationMs;
        }
        
        /**
         * 检查是否成功
         * @return 是否成功
         */
        public boolean isSuccess() {
            return success;
        }
        
        /**
         * 设置成功状态
         * @param success 成功状态
         */
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        /**
         * 获取错误信息
         * @return 错误信息
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        /**
         * 设置错误信息
         * @param errorMessage 错误信息
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            this.success = false;
        }
        
        /**
         * 获取截图路径
         * @return 截图路径
         */
        public String getScreenshotPath() {
            return screenshotPath;
        }
        
        /**
         * 设置截图路径
         * @param screenshotPath 截图路径
         */
        public void setScreenshotPath(String screenshotPath) {
            this.screenshotPath = screenshotPath;
        }
    }
    
    /**
     * 构造函数
     */
    public ReportGenerator() {
        this(null);
    }
    
    /**
     * 构造函数
     * @param reportTitle 报告标题
     */
    public ReportGenerator(String reportTitle) {
        if (reportTitle != null) {
            this.reportTitle = reportTitle;
        }
    }
    
    /**
     * 构造函数
     * @param reportTitle 报告标题
     * @param additionalInfo 额外信息
     */
    public ReportGenerator(String reportTitle, Map<String, Object> additionalInfo) {
        if (reportTitle != null) {
            this.reportTitle = reportTitle;
        }
        this.additionalInfo = additionalInfo;
    }
    
    /**
     * 添加执行步骤
     * @param step 执行步骤
     * @return 当前报告生成器实例
     */
    public ReportGenerator addStep(ExecutionStep step) {
        executionSteps.add(step);
        if (!step.isSuccess()) {
            isSuccessful = false;
        }
        return this;
    }
    
    /**
     * 创建并添加执行步骤
     * @param action 操作名称
     * @param description 描述
     * @return 创建的执行步骤
     */
    public ExecutionStep createStep(String action, String description) {
        ExecutionStep step = new ExecutionStep(action, description);
        executionSteps.add(step);
        return step;
    }
    
    /**
     * 创建执行步骤构建器
     * @return 执行步骤构建器
     */
    public StepBuilder createStep() {
        return new StepBuilder();
    }
    
    /**
     * 执行步骤构建器
     */
    public class StepBuilder {
        private String action;
        private String description;
        private boolean success = true;
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
            this.success = false;
            return this;
        }
        
        public StepBuilder withScreenshotPath(String screenshotPath) {
            this.screenshotPath = screenshotPath;
            return this;
        }
        
        public ExecutionStep build() {
            ExecutionStep step = new ExecutionStep(action, description);
            step.setSuccess(success);
            if (errorMessage != null) {
                step.setErrorMessage(errorMessage);
            }
            if (screenshotPath != null) {
                step.setScreenshotPath(screenshotPath);
            }
            return step;
        }
    }
    
    /**
     * 添加额外信息
     * @param key 键
     * @param value 值
     * @return 当前报告生成器实例
     */
    public ReportGenerator addAdditionalInfo(String key, Object value) {
        if (additionalInfo == null) {
            additionalInfo = new HashMap<>();
        }
        additionalInfo.put(key, value);
        return this;
    }
    
    /**
     * 设置额外信息
     * @param additionalInfo 额外信息
     * @return 当前报告生成器实例
     */
    public ReportGenerator setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }
    
    /**
     * 生成JSON格式的报告
     * @return JSON字符串
     */
    public String generateJsonReport() {
        try {
            StringBuilder jsonBuilder = new StringBuilder();
            
            // 开始JSON对象
            jsonBuilder.append("{");
            
            // 基本信息
            jsonBuilder.append('"').append("title").append('"').append(':');
            jsonBuilder.append('"').append(escapeForJson(reportTitle)).append('"').append(',');
            
            jsonBuilder.append('"').append("generatedAt").append('"').append(':');
            jsonBuilder.append('"').append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append('"').append(',');
            
            jsonBuilder.append('"').append("success").append('"').append(':').append(isSuccessful).append(',');
            
            jsonBuilder.append('"').append("totalSteps").append('"').append(':').append(executionSteps.size()).append(',');
            jsonBuilder.append('"').append("successfulSteps").append('"').append(':').append(getSuccessfulStepsCount()).append(',');
            jsonBuilder.append('"').append("failedSteps").append('"').append(':').append(getFailedStepsCount());
            
            // 额外信息
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                jsonBuilder.append(',');
                jsonBuilder.append('"').append("additionalInfo").append('"').append(":{");
                boolean first = true;
                for (Map.Entry<String, Object> entry : additionalInfo.entrySet()) {
                    if (!first) jsonBuilder.append(',');
                    jsonBuilder.append('"').append(escapeForJson(entry.getKey())).append('"').append(':');
                    if (entry.getValue() == null) {
                        jsonBuilder.append("null");
                    } else if (entry.getValue() instanceof String) {
                        jsonBuilder.append('"').append(escapeForJson((String)entry.getValue())).append('"');
                    } else {
                        jsonBuilder.append(entry.getValue());
                    }
                    first = false;
                }
                jsonBuilder.append('}');
            }
            
            // 步骤信息
            jsonBuilder.append(',');
            jsonBuilder.append('"').append("steps").append('"').append(":[");
            boolean firstStep = true;
            for (ExecutionStep step : executionSteps) {
                if (!firstStep) jsonBuilder.append(',');
                
                jsonBuilder.append("{");
                jsonBuilder.append('"').append("action").append('"').append(':');
                jsonBuilder.append('"').append(escapeForJson(step.getAction() != null ? step.getAction() : "")).append('"').append(',');
                
                jsonBuilder.append('"').append("description").append('"').append(':');
                jsonBuilder.append('"').append(escapeForJson(step.getDescription() != null ? step.getDescription() : "")).append('"').append(',');
                
                jsonBuilder.append('"').append("success").append('"').append(':').append(step.isSuccess()).append(',');
                
                jsonBuilder.append('"').append("startTime").append('"').append(':').append(step.getStartTime()).append(',');
                jsonBuilder.append('"').append("endTime").append('"').append(':').append(step.getEndTime()).append(',');
                jsonBuilder.append('"').append("durationMs").append('"').append(':').append(step.getDurationMs());
                
                if (step.getErrorMessage() != null) {
                    jsonBuilder.append(',');
                    jsonBuilder.append('"').append("errorMessage").append('"').append(':');
                    jsonBuilder.append('"').append(escapeForJson(step.getErrorMessage())).append('"');
                }
                
                if (step.getScreenshotPath() != null) {
                    jsonBuilder.append(',');
                    jsonBuilder.append('"').append("screenshotPath").append('"').append(':');
                    jsonBuilder.append('"').append(escapeForJson(step.getScreenshotPath())).append('"');
                }
                
                jsonBuilder.append("}");
                firstStep = false;
            }
            
            jsonBuilder.append("]");
            jsonBuilder.append("}");
            
            return jsonBuilder.toString();
        } catch (Exception e) {
            LOGGER.error("Error generating JSON report: {}", e.getMessage(), e);
            // 返回一个基本的错误JSON对象
            return "{\"error\":\"Failed to generate report\"}";
        }
    }
    
    /**
     * 为JSON字符串进行转义处理，确保所有字符都是ASCII或Unicode转义
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeForJson(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    // 处理所有非ASCII字符，转换为Unicode转义序列
                    if (c > 127) {
                        result.append("\\u").append(String.format("%04x", (int)c));
                    } else {
                        result.append(c);
                    }
                    break;
            }
        }
        return result.toString();
    }
    
    /**
     * 将图片文件转换为Base64编码
     * @param imagePath 图片文件路径
     * @return Base64编码的图片数据，如果转换失败则返回null
     */
    private String imageToBase64(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                LOGGER.warn("Screenshot file does not exist: {}", imagePath);
                return null;
            }
            
            try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
                byte[] imageData = new byte[(int) imageFile.length()];
                imageInFile.read(imageData);
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
            }
        } catch (IOException e) {
            LOGGER.error("Error converting image to base64: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成HTML格式的报告
     * @return HTML字符串
     */
    public String generateHtmlReport() {
        try {
            StringBuilder htmlBuilder = new StringBuilder();
            
            // HTML头部
            htmlBuilder.append("<!DOCTYPE html>");
            htmlBuilder.append("<html lang=\"zh-CN\">");
            htmlBuilder.append("<head>");
            htmlBuilder.append("<meta charset=\"UTF-8\">");
            htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            htmlBuilder.append("<title>").append(escapeHtml(reportTitle)).append("</title>");
            htmlBuilder.append("<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"https://lf3-static.bytednsdoc.com/obj/eden-cn/vhaeh7vhabf/favicon-32x32.png\" />");
            htmlBuilder.append("<style>");
            
            // 增强版CSS样式
            htmlBuilder.append(generateEnhancedCss());
            htmlBuilder.append("</style>");
            htmlBuilder.append("</head>");
            htmlBuilder.append("<body>");
            
            htmlBuilder.append("<div class=\"container\">");
            
            // 头部信息
            htmlBuilder.append("<div class=\"header\">");
            htmlBuilder.append("<h1>").append(escapeHtml(reportTitle)).append("</h1>");
            htmlBuilder.append("<p>生成时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("</p>");
            htmlBuilder.append("</div>");
            
            // 摘要信息
            htmlBuilder.append("<div class=\"summary\">");
            htmlBuilder.append("<h2>执行摘要</h2>");
            htmlBuilder.append("<div class=\"status-indicator ").append(isSuccessful ? "success" : "failure").append("\">");
            htmlBuilder.append("<span class=\"status-icon\">").append(isSuccessful ? "✓" : "✗").append("</span>");
            htmlBuilder.append("<span class=\"status-text\">").append(isSuccessful ? "执行成功" : "执行失败").append("</span>");
            htmlBuilder.append("</div>");
            
            htmlBuilder.append("<div class=\"stats\">");
            htmlBuilder.append("<div class=\"stat-item\">");
            htmlBuilder.append("<span class=\"stat-value\">").append(executionSteps.size()).append("</span>");
            htmlBuilder.append("<span class=\"stat-label\">总步骤数</span>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("<div class=\"stat-item\">");
            htmlBuilder.append("<span class=\"stat-value success\">").append(executionSteps.stream().filter(ExecutionStep::isSuccess).count()).append("</span>");
            htmlBuilder.append("<span class=\"stat-label\">成功步骤</span>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("<div class=\"stat-item\">");
            htmlBuilder.append("<span class=\"stat-value failure\">").append(executionSteps.stream().filter(step -> !step.isSuccess()).count()).append("</span>");
            htmlBuilder.append("<span class=\"stat-label\">失败步骤</span>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("</div>");
            
            // 额外信息
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                htmlBuilder.append("<div class=\"additional-info\">");
                htmlBuilder.append("<h3>额外信息</h3>");
                htmlBuilder.append("<table>");
                for (Map.Entry<String, Object> entry : additionalInfo.entrySet()) {
                    if (entry.getValue() != null) {
                        htmlBuilder.append("<tr>");
                        htmlBuilder.append("<td class=\"info-key\">").append(escapeHtml(entry.getKey())).append("</td>");
                        htmlBuilder.append("<td class=\"info-value\">").append(escapeHtml(entry.getValue().toString())).append("</td>");
                        htmlBuilder.append("</tr>");
                    }
                }
                htmlBuilder.append("</table>");
                htmlBuilder.append("</div>");
            }
            htmlBuilder.append("</div>");
            
            // 步骤详情
            htmlBuilder.append("<div class=\"steps-container\">");
            htmlBuilder.append("<h2>执行步骤</h2>");
            
            for (int i = 0; i < executionSteps.size(); i++) {
                ExecutionStep step = executionSteps.get(i);
                htmlBuilder.append("<div class=\"step ")
                          .append(step.isSuccess() ? "step-success" : "step-failure")
                          .append("\">").append("\n");
                
                htmlBuilder.append("<div class=\"step-header\">");
                htmlBuilder.append("<div class=\"step-number\">").append(i + 1).append("</div>");
                htmlBuilder.append("<div class=\"step-title\">").append(escapeHtml(step.getAction())).append("</div>");
                htmlBuilder.append("<div class=\"step-status\">").append(step.isSuccess() ? "成功" : "失败").append("</div>");
                htmlBuilder.append("</div>\n");
                
                if (step.getDescription() != null) {
                    htmlBuilder.append("<div class=\"step-description\">")
                              .append(escapeHtml(step.getDescription())).append("</div>\n");
                }
                
                htmlBuilder.append("<div class=\"step-details\">");
                htmlBuilder.append("<div class=\"detail-item\">");
                htmlBuilder.append("<span class=\"detail-label\">开始时间:</span>");
                htmlBuilder.append("<span class=\"detail-value\">").append(formatTimestamp(step.getStartTime())).append("</span>");
                htmlBuilder.append("</div>");
                
                htmlBuilder.append("<div class=\"detail-item\">");
                htmlBuilder.append("<span class=\"detail-label\">结束时间:</span>");
                htmlBuilder.append("<span class=\"detail-value\">").append(formatTimestamp(step.getEndTime())).append("</span>");
                htmlBuilder.append("</div>");
                
                htmlBuilder.append("<div class=\"detail-item\">");
                htmlBuilder.append("<span class=\"detail-label\">持续时间:</span>");
                htmlBuilder.append("<span class=\"detail-value\">").append(step.getDurationMs()).append("ms</span>");
                htmlBuilder.append("</div>");
                htmlBuilder.append("</div>\n");
                
                if (step.getErrorMessage() != null) {
                    htmlBuilder.append("<div class=\"step-error\">")
                              .append("<div class=\"error-title\">错误信息:</div>")
                              .append("<div class=\"error-message\">").append(escapeHtml(step.getErrorMessage())).append("</div>")
                              .append("</div>\n");
                }
                
                if (step.getScreenshotPath() != null) {
                    String base64Image = imageToBase64(step.getScreenshotPath());
                    if (base64Image != null) {
                        htmlBuilder.append("<div class=\"step-screenshot\">")
                                  .append("<div class=\"screenshot-title\">截图</div>")
                                  .append("<div class=\"screenshot-container\">")
                                  .append("<img class=\"screenshot\" src=\"").append(base64Image).append("\" alt=\"截图\">")
                                  .append("</div>")
                                  .append("</div>\n");
                    } else {
                        // 如果转换失败，显示错误信息
                        htmlBuilder.append("<div class=\"step-screenshot\">")
                                  .append("<div class=\"screenshot-title\">截图</div>")
                                  .append("<div class=\"screenshot-container\">")
                                  .append("<div class=\"error-message\">截图加载失败</div>")
                                  .append("</div>")
                                  .append("</div>\n");
                    }
                }
                
                htmlBuilder.append("</div>\n");
            }
            
            htmlBuilder.append("</div>");
            htmlBuilder.append("</div>");
            
            // 添加JavaScript交互
            htmlBuilder.append("<script>");
            htmlBuilder.append(generateInteractiveScript());
            htmlBuilder.append("</script>");
            
            htmlBuilder.append("</body>");
            htmlBuilder.append("</html>");
            
            return htmlBuilder.toString();
        } catch (Exception e) {
            LOGGER.error("Error generating HTML report: {}", e.getMessage(), e);
            return "<html><body><h1>Error generating report</h1><p>" + escapeHtml(e.getMessage()) + "</p></body></html>";
        }
    }
    
    /**
     * 生成增强版CSS样式
     * @return CSS字符串
     */
    private String generateEnhancedCss() {
        StringBuilder css = new StringBuilder();
        
        // 基础样式
        css.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f7fa; color: #333; }");
        css.append(".container { max-width: 1200px; margin: 0 auto; padding: 20px; }");
        
        // 头部样式
        css.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 30px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1); }");
        css.append(".header h1 { margin: 0 0 10px 0; font-size: 2.5rem; font-weight: 600; }");
        css.append(".header p { margin: 0; opacity: 0.9; font-size: 1.1rem; }");
        
        // 摘要样式
        css.append(".summary { background-color: white; padding: 25px; border-radius: 12px; margin-bottom: 30px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }");
        css.append(".summary h2 { margin-top: 0; color: #2c3e50; font-size: 1.8rem; }");
        
        // 状态指示器
        css.append(".status-indicator { display: flex; align-items: center; margin-bottom: 20px; padding: 15px; border-radius: 8px; }");
        css.append(".status-indicator.success { background-color: rgba(40, 167, 69, 0.1); border-left: 4px solid #28a745; }");
        css.append(".status-indicator.failure { background-color: rgba(220, 53, 69, 0.1); border-left: 4px solid #dc3545; }");
        css.append(".status-icon { font-size: 1.5rem; margin-right: 10px; font-weight: bold; }");
        css.append(".status-indicator.success .status-icon { color: #28a745; }");
        css.append(".status-indicator.failure .status-icon { color: #dc3545; }");
        css.append(".status-text { font-size: 1.2rem; font-weight: 500; }");
        css.append(".status-indicator.success .status-text { color: #28a745; }");
        css.append(".status-indicator.failure .status-text { color: #dc3545; }");
        
        // 统计数据
        css.append(".stats { display: flex; justify-content: space-between; margin-bottom: 20px; }");
        css.append(".stat-item { text-align: center; flex: 1; padding: 15px; }");
        css.append(".stat-value { display: block; font-size: 2rem; font-weight: 600; margin-bottom: 5px; }");
        css.append(".stat-value.success { color: #28a745; }");
        css.append(".stat-value.failure { color: #dc3545; }");
        css.append(".stat-label { color: #6c757d; font-size: 0.9rem; }");
        
        // 额外信息
        css.append(".additional-info { margin-top: 20px; }");
        css.append(".additional-info h3 { margin-top: 0; color: #2c3e50; font-size: 1.4rem; }");
        css.append(".additional-info table { width: 100%; border-collapse: collapse; }");
        css.append(".additional-info td { padding: 10px; border-bottom: 1px solid #eee; }");
        css.append(".info-key { font-weight: 600; color: #495057; width: 30%; }");
        css.append(".info-value { color: #6c757d; }");
        
        // 步骤容器
        css.append(".steps-container { background-color: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }");
        css.append(".steps-container h2 { margin-top: 0; color: #2c3e50; font-size: 1.8rem; }");
        
        // 步骤样式
        css.append(".step { border: 1px solid #e9ecef; border-radius: 12px; margin-bottom: 20px; overflow: hidden; transition: all 0.3s ease; }");
        css.append(".step:hover { box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1); transform: translateY(-2px); }");
        css.append(".step-success { border-left: 4px solid #28a745; }");
        css.append(".step-failure { border-left: 4px solid #dc3545; }");
        
        // 步骤头部
        css.append(".step-header { display: flex; align-items: center; padding: 15px 20px; background-color: #f8f9fa; border-bottom: 1px solid #e9ecef; }");
        css.append(".step-number { width: 30px; height: 30px; border-radius: 50%; background-color: #6c757d; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 15px; }");
        css.append(".step-success .step-number { background-color: #28a745; }");
        css.append(".step-failure .step-number { background-color: #dc3545; }");
        css.append(".step-title { font-weight: 600; font-size: 1.1rem; flex: 1; }");
        css.append(".step-status { padding: 5px 10px; border-radius: 20px; font-size: 0.8rem; font-weight: 500; }");
        css.append(".step-success .step-status { background-color: rgba(40, 167, 69, 0.1); color: #28a745; }");
        css.append(".step-failure .step-status { background-color: rgba(220, 53, 69, 0.1); color: #dc3545; }");
        
        // 步骤内容
        css.append(".step-description { padding: 15px 20px; font-size: 1rem; line-height: 1.5; }");
        css.append(".step-details { display: flex; flex-wrap: wrap; padding: 0 20px 15px; }");
        css.append(".detail-item { flex: 1; min-width: 200px; margin-bottom: 10px; }");
        css.append(".detail-label { font-weight: 600; color: #495057; margin-right: 5px; }");
        css.append(".detail-value { color: #6c757d; }");
        
        // 错误信息
        css.append(".step-error { padding: 15px 20px; background-color: rgba(220, 53, 69, 0.05); }");
        css.append(".error-title { font-weight: 600; color: #dc3545; margin-bottom: 5px; }");
        css.append(".error-message { color: #dc3545; font-family: monospace; background-color: rgba(220, 53, 69, 0.1); padding: 10px; border-radius: 6px; white-space: pre-wrap; }");
        
        // 截图
        css.append(".step-screenshot { padding: 0 20px 20px; }");
        css.append(".screenshot-title { font-weight: 600; color: #495057; margin-bottom: 10px; }");
        css.append(".screenshot-container { border: 1px solid #e9ecef; border-radius: 8px; overflow: hidden; }");
        css.append(".screenshot { max-width: 100%; height: auto; display: block; cursor: pointer; transition: transform 0.3s ease; }");
        css.append(".screenshot:hover { transform: scale(1.02); }");
        
        // 响应式设计
        css.append("@media (max-width: 768px) {");
        css.append(".container { padding: 10px; }");
        css.append(".header { padding: 20px; }");
        css.append(".header h1 { font-size: 2rem; }");
        css.append(".stats { flex-direction: column; }");
        css.append(".stat-item { margin-bottom: 10px; }");
        css.append(".step-details { flex-direction: column; }");
        css.append(".detail-item { min-width: auto; }");
        css.append("}");
        
        // 截图模态框
        css.append(".modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.9); }");
        css.append(".modal-content { margin: auto; display: block; max-width: 90%; max-height: 90%; margin-top: 5%; }");
        css.append(".close { position: absolute; top: 15px; right: 35px; color: #f1f1f1; font-size: 40px; font-weight: bold; cursor: pointer; }");
        css.append(".close:hover { color: #bbb; }");
        
        return css.toString();
    }
    
    /**
     * 生成交互式JavaScript
     * @return JavaScript字符串
     */
    private String generateInteractiveScript() {
        StringBuilder js = new StringBuilder();
        
        js.append("// 截图模态框功能\n");
        js.append("document.addEventListener('DOMContentLoaded', function() {\n");
        js.append("  const modal = document.createElement('div');\n");
        js.append("  modal.className = 'modal';\n");
        js.append("  modal.id = 'screenshotModal';\n");
        js.append("  \n");
        js.append("  const modalImg = document.createElement('img');\n");
        js.append("  modalImg.className = 'modal-content';\n");
        js.append("  modalImg.id = 'modalImage';\n");
        js.append("  \n");
        js.append("  const closeBtn = document.createElement('span');\n");
        js.append("  closeBtn.className = 'close';\n");
        js.append("  closeBtn.innerHTML = '&times;';\n");
        js.append("  \n");
        js.append("  modal.appendChild(modalImg);\n");
        js.append("  modal.appendChild(closeBtn);\n");
        js.append("  document.body.appendChild(modal);\n");
        js.append("  \n");
        js.append("  // 为所有截图添加点击事件\n");
        js.append("  const screenshots = document.querySelectorAll('.screenshot');\n");
        js.append("  screenshots.forEach(function(screenshot) {\n");
        js.append("    screenshot.addEventListener('click', function() {\n");
        js.append("      modal.style.display = 'block';\n");
        js.append("      modalImg.src = this.src;\n");
        js.append("    });\n");
        js.append("  });\n");
        js.append("  \n");
        js.append("  // 关闭模态框\n");
        js.append("  closeBtn.onclick = function() {\n");
        js.append("    modal.style.display = 'none';\n");
        js.append("  };\n");
        js.append("  \n");
        js.append("  // 点击模态框外部关闭\n");
        js.append("  modal.onclick = function(event) {\n");
        js.append("    if (event.target == modal) {\n");
        js.append("      modal.style.display = 'none';\n");
        js.append("    }\n");
        js.append("  };\n");
        js.append("  \n");
        js.append("  // ESC键关闭模态框\n");
        js.append("  document.addEventListener('keydown', function(event) {\n");
        js.append("    if (event.key === 'Escape') {\n");
        js.append("      modal.style.display = 'none';\n");
        js.append("    }\n");
        js.append("  });\n");
        js.append("});\n");
        
        return js.toString();
    }
    
    /**
     * 格式化时间戳
     * @param timestamp 时间戳
     * @return 格式化后的时间字符串
     */
    private String formatTimestamp(long timestamp) {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
    
    /**
     * 为HTML字符串进行转义处理
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * 保存JSON报告到文件
     * @param filePath 文件路径
     * @throws IOException 如果保存失败
     */
    public void saveJsonReport(String filePath) throws IOException {
        saveToFile(filePath, generateJsonReport());
    }
    
    /**
     * 保存HTML报告到文件
     * @param filePath 文件路径
     * @throws IOException 如果保存失败
     */
    public void saveHtmlReport(String filePath) throws IOException {
        saveToFile(filePath, generateHtmlReport());
    }
    
    /**
     * 保存JSON和HTML报告到指定目录
     * @param directory 目录路径
     * @return 包含JSON和HTML报告文件路径的数组
     * @throws IOException 如果保存失败
     */
    public String[] saveReports(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String jsonFileName = reportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".json";
        String htmlFileName = reportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".html";
        
        String jsonFilePath = directory + File.separator + jsonFileName;
        String htmlFilePath = directory + File.separator + htmlFileName;
        
        saveJsonReport(jsonFilePath);
        saveHtmlReport(htmlFilePath);
        
        return new String[]{jsonFilePath, htmlFilePath};
    }
    
    /**
     * 保存JSON报告到指定目录
     * @param directory 目录路径
     * @return JSON报告文件路径
     * @throws IOException 如果保存失败
     */
    public String saveJsonReportToDirectory(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = reportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".json";
        String filePath = directory + File.separator + fileName;
        
        saveJsonReport(filePath);
        return filePath;
    }
    
    /**
     * 保存HTML报告到指定目录
     * @param directory 目录路径
     * @return HTML报告文件路径
     * @throws IOException 如果保存失败
     */
    public String saveHtmlReportToDirectory(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = reportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".html";
        String filePath = directory + File.separator + fileName;
        
        saveHtmlReport(filePath);
        return filePath;
    }
    
    /**
     * 保存内容到文件
     * @param filePath 文件路径
     * @param content 内容
     * @throws IOException 如果保存失败
     */
    private void saveToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(filePath), "UTF-8"))) {
            writer.write(content);
            writer.flush();
        }
    }
    
    /**
     * 获取报告标题
     * @return 报告标题
     */
    public String getReportTitle() {
        return reportTitle;
    }
    
    /**
     * 获取执行步骤列表
     * @return 执行步骤列表
     */
    public List<ExecutionStep> getExecutionSteps() {
        return new ArrayList<>(executionSteps);
    }
    
    /**
     * 获取额外信息
     * @return 额外信息
     */
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo != null ? new HashMap<>(additionalInfo) : null;
    }
    
    /**
     * 检查执行是否成功
     * @return 是否成功
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }
    
    /**
     * 获取成功步骤数
     * @return 成功步骤数
     */
    public long getSuccessfulStepsCount() {
        return executionSteps.stream().filter(ExecutionStep::isSuccess).count();
    }
    
    /**
     * 获取失败步骤数
     * @return 失败步骤数
     */
    public long getFailedStepsCount() {
        return executionSteps.stream().filter(step -> !step.isSuccess()).count();
    }
}