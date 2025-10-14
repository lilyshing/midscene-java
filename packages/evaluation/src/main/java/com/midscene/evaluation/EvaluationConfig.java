package com.midscene.evaluation;

import java.time.Duration;

/**
 * 评估配置类
 * 用于自定义评估引擎的行为参数和规则
 */
public class EvaluationConfig {
    private boolean useRegexMatch = false;
    private boolean useContainsMatch = false;
    private long timeoutMs = 30000; // 默认30秒超时
    private int maxRetries = 1;     // 默认重试次数
    private boolean failFast = false;
    private boolean generateDetailedReport = true;
    private boolean saveScreenshots = false;
    private boolean recordPerformanceMetrics = true;
    private boolean parallelExecution = false;
    private int maxParallelTests = 5;
    private String reportDirectory = "./reports";
    private ReportFormat reportFormat = ReportFormat.JSON;
    
    /**
     * 报告格式枚举
     */
    public enum ReportFormat {
        JSON,
        HTML,
        XML,
        CSV,
        TEXT
    }
    
    /**
     * 默认构造函数
     */
    public EvaluationConfig() {
    }
    
    /**
     * 创建自定义配置的构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 设置是否使用正则表达式匹配结果
     */
    public void setUseRegexMatch(boolean useRegexMatch) {
        this.useRegexMatch = useRegexMatch;
    }
    
    /**
     * 设置是否使用包含关系匹配结果
     */
    public void setUseContainsMatch(boolean useContainsMatch) {
        this.useContainsMatch = useContainsMatch;
    }
    
    /**
     * 设置测试执行超时时间(毫秒)
     */
    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = Math.max(1000, timeoutMs); // 最小1秒
    }
    
    /**
     * 设置测试执行超时时间
     */
    public void setTimeout(Duration timeout) {
        if (timeout != null) {
            this.timeoutMs = timeout.toMillis();
        }
    }
    
    /**
     * 设置最大重试次数
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries); // 最小0次
    }
    
    /**
     * 设置是否快速失败
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }
    
    /**
     * 设置是否生成详细报告
     */
    public void setGenerateDetailedReport(boolean generateDetailedReport) {
        this.generateDetailedReport = generateDetailedReport;
    }
    
    /**
     * 设置是否保存截图
     */
    public void setSaveScreenshots(boolean saveScreenshots) {
        this.saveScreenshots = saveScreenshots;
    }
    
    /**
     * 设置是否记录性能指标
     */
    public void setRecordPerformanceMetrics(boolean recordPerformanceMetrics) {
        this.recordPerformanceMetrics = recordPerformanceMetrics;
    }
    
    /**
     * 设置是否并行执行测试
     */
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }
    
    /**
     * 设置最大并行测试数
     */
    public void setMaxParallelTests(int maxParallelTests) {
        this.maxParallelTests = Math.max(1, maxParallelTests); // 最小1个
    }
    
    /**
     * 设置报告保存目录
     */
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory != null ? reportDirectory : "./reports";
    }
    
    /**
     * 设置报告格式
     */
    public void setReportFormat(ReportFormat reportFormat) {
        this.reportFormat = reportFormat != null ? reportFormat : ReportFormat.JSON;
    }
    
    // Getters
    public boolean isUseRegexMatch() {
        return useRegexMatch;
    }
    
    public boolean isUseContainsMatch() {
        return useContainsMatch;
    }
    
    public long getTimeoutMs() {
        return timeoutMs;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public boolean isFailFast() {
        return failFast;
    }
    
    public boolean isGenerateDetailedReport() {
        return generateDetailedReport;
    }
    
    public boolean isSaveScreenshots() {
        return saveScreenshots;
    }
    
    public boolean isRecordPerformanceMetrics() {
        return recordPerformanceMetrics;
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public int getMaxParallelTests() {
        return maxParallelTests;
    }
    
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    public ReportFormat getReportFormat() {
        return reportFormat;
    }
    
    /**
     * 配置构建器类
     */
    public static class Builder {
        private final EvaluationConfig config = new EvaluationConfig();
        
        /**
         * 设置使用正则表达式匹配
         */
        public Builder withRegexMatch() {
            config.setUseRegexMatch(true);
            config.setUseContainsMatch(false); // 互斥
            return this;
        }
        
        /**
         * 设置使用包含关系匹配
         */
        public Builder withContainsMatch() {
            config.setUseContainsMatch(true);
            config.setUseRegexMatch(false); // 互斥
            return this;
        }
        
        /**
         * 设置超时时间
         */
        public Builder withTimeout(long timeoutMs) {
            config.setTimeoutMs(timeoutMs);
            return this;
        }
        
        /**
         * 设置超时时间
         */
        public Builder withTimeout(Duration timeout) {
            config.setTimeout(timeout);
            return this;
        }
        
        /**
         * 设置重试次数
         */
        public Builder withRetries(int maxRetries) {
            config.setMaxRetries(maxRetries);
            return this;
        }
        
        /**
         * 设置快速失败模式
         */
        public Builder withFailFast() {
            config.setFailFast(true);
            return this;
        }
        
        /**
         * 设置生成详细报告
         */
        public Builder withDetailedReport() {
            config.setGenerateDetailedReport(true);
            return this;
        }
        
        /**
         * 设置保存截图
         */
        public Builder withScreenshots() {
            config.setSaveScreenshots(true);
            return this;
        }
        
        /**
         * 设置记录性能指标
         */
        public Builder withPerformanceMetrics() {
            config.setRecordPerformanceMetrics(true);
            return this;
        }
        
        /**
         * 设置并行执行
         */
        public Builder withParallelExecution(int maxParallelTests) {
            config.setParallelExecution(true);
            config.setMaxParallelTests(maxParallelTests);
            return this;
        }
        
        /**
         * 设置报告目录
         */
        public Builder withReportDirectory(String directory) {
            config.setReportDirectory(directory);
            return this;
        }
        
        /**
         * 设置报告格式
         */
        public Builder withReportFormat(ReportFormat format) {
            config.setReportFormat(format);
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public EvaluationConfig build() {
            return config;
        }
    }
}