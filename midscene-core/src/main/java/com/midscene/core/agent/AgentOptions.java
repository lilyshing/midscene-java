package com.midscene.core.agent;

/**
 * Agent配置选项类，用于控制Agent的行为和性能
 */
public class AgentOptions {
    // 超时设置
    private int timeout = 30; // 操作超时时间（秒）
    
    // 重试机制
    private int retryCount = 3; // 失败重试次数
    private double retryDelay = 1.0; // 重试间隔（秒）
    
    // 调试选项
    private boolean screenshotOnError = true; // 错误时自动截图
    private boolean saveExecutionLogs = true; // 保存执行日志
    
    // 性能优化
    private boolean cacheEnabled = true; // 启用智能缓存
    private boolean parallelExecution = false; // 并行执行（实验性）
    
    // AI 模型设置
    private Double modelTemperature = 0.1; // AI 响应随机性
    private Integer maxTokens = 1000; // 最大 token 数
    
    // 并发控制
    private int concurrencyLimit = 3; // 并发限制
    
    // 缓存配置
    private String cacheId; // 缓存ID
    
    // 报告配置
    private boolean generateReport = false; // 生成执行报告
    private String reportFileName; // 报告文件名
    
    /**
     * 默认构造函数
     */
    public AgentOptions() {
        // 使用字段默认值初始化
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private final AgentOptions options = new AgentOptions();
        
        /**
         * 设置超时时间
         * @param timeout 超时时间（秒）
         * @return Builder实例
         */
        public Builder timeout(int timeout) {
            options.timeout = timeout;
            return this;
        }
        
        /**
         * 设置重试次数
         * @param retryCount 重试次数
         * @return Builder实例
         */
        public Builder retryCount(int retryCount) {
            options.retryCount = retryCount;
            return this;
        }
        
        /**
         * 设置重试延迟
         * @param retryDelay 重试延迟（秒）
         * @return Builder实例
         */
        public Builder retryDelay(double retryDelay) {
            options.retryDelay = retryDelay;
            return this;
        }
        
        /**
         * 设置错误时是否截图
         * @param screenshotOnError 是否截图
         * @return Builder实例
         */
        public Builder screenshotOnError(boolean screenshotOnError) {
            options.screenshotOnError = screenshotOnError;
            return this;
        }
        
        /**
         * 设置是否保存执行日志
         * @param saveExecutionLogs 是否保存日志
         * @return Builder实例
         */
        public Builder saveExecutionLogs(boolean saveExecutionLogs) {
            options.saveExecutionLogs = saveExecutionLogs;
            return this;
        }
        
        /**
         * 设置是否启用缓存
         * @param cacheEnabled 是否启用缓存
         * @return Builder实例
         */
        public Builder cacheEnabled(boolean cacheEnabled) {
            options.cacheEnabled = cacheEnabled;
            return this;
        }
        
        /**
         * 设置是否并行执行
         * @param parallelExecution 是否并行执行
         * @return Builder实例
         */
        public Builder parallelExecution(boolean parallelExecution) {
            options.parallelExecution = parallelExecution;
            return this;
        }
        
        /**
         * 设置模型温度
         * @param modelTemperature 模型温度
         * @return Builder实例
         */
        public Builder modelTemperature(double modelTemperature) {
            options.modelTemperature = modelTemperature;
            return this;
        }
        
        /**
         * 设置最大Token数
         * @param maxTokens 最大Token数
         * @return Builder实例
         */
        public Builder maxTokens(int maxTokens) {
            options.maxTokens = maxTokens;
            return this;
        }
        
        /**
         * 设置并发限制
         * @param concurrencyLimit 并发限制
         * @return Builder实例
         */
        public Builder concurrencyLimit(int concurrencyLimit) {
            options.concurrencyLimit = concurrencyLimit;
            return this;
        }
        
        /**
         * 设置缓存ID
         * @param cacheId 缓存ID
         * @return Builder实例
         */
        public Builder cacheId(String cacheId) {
            options.cacheId = cacheId;
            return this;
        }
        
        /**
         * 设置是否生成报告
         * @param generateReport 是否生成报告
         * @return Builder实例
         */
        public Builder generateReport(boolean generateReport) {
            options.generateReport = generateReport;
            return this;
        }
        
        /**
         * 设置报告文件名
         * @param reportFileName 报告文件名
         * @return Builder实例
         */
        public Builder reportFileName(String reportFileName) {
            options.reportFileName = reportFileName;
            return this;
        }
        
        /**
         * 构建AgentOptions实例
         * @return AgentOptions实例
         */
        public AgentOptions build() {
            return options;
        }
    }
    
    /**
     * 创建构建器实例
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Getter和Setter方法
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public double getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(double retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public boolean isScreenshotOnError() {
        return screenshotOnError;
    }
    
    public void setScreenshotOnError(boolean screenshotOnError) {
        this.screenshotOnError = screenshotOnError;
    }
    
    public boolean isSaveExecutionLogs() {
        return saveExecutionLogs;
    }
    
    public void setSaveExecutionLogs(boolean saveExecutionLogs) {
        this.saveExecutionLogs = saveExecutionLogs;
    }
    
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
    
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }
    
    public Double getModelTemperature() {
        return modelTemperature;
    }
    
    public void setModelTemperature(Double modelTemperature) {
        this.modelTemperature = modelTemperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public int getConcurrencyLimit() {
        return concurrencyLimit;
    }
    
    public void setConcurrencyLimit(int concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
    }
    
    public String getCacheId() {
        return cacheId;
    }
    
    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }
    
    public boolean isGenerateReport() {
        return generateReport;
    }
    
    public void setGenerateReport(boolean generateReport) {
        this.generateReport = generateReport;
    }
    
    public String getReportFileName() {
        return reportFileName;
    }
    
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }
    
    @Override
    public String toString() {
        return "AgentOptions{" +
               "timeout=" + timeout +
               ", retryCount=" + retryCount +
               ", screenshotOnError=" + screenshotOnError +
               ", cacheEnabled=" + cacheEnabled +
               "}";
    }
}