package com.midscene.evaluation;

import com.midscene.core.model.TaskResult;
import com.midscene.shared.platform.PlatformInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 评估结果类
 * 记录单个测试用例的执行结果和详细信息
 */
public class EvaluationResult {
    private final String testId;
    private final String testName;
    private Instant startTime;
    private Instant endTime;
    private long duration = 0;
    private boolean passed = false;
    private String errorMessage;
    private String expectedResult;
    private String actualResult;
    private String extractedData;
    private TaskResult taskResult;
    private PlatformInfo platformInfo;
    private Map<String, Object> performanceMetrics = new HashMap<>();
    private Map<String, Object> additionalInfo = new HashMap<>();
    
    /**
     * 构造函数
     */
    public EvaluationResult(String testId, String testName) {
        this.testId = Objects.requireNonNull(testId, "测试ID不能为空");
        this.testName = Objects.requireNonNull(testName, "测试名称不能为空");
    }
    
    /**
     * 设置开始时间
     */
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    /**
     * 设置结束时间
     */
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    /**
     * 设置执行时长(毫秒)
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    /**
     * 设置测试是否通过
     */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    
    /**
     * 设置错误信息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * 设置预期结果
     */
    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }
    
    /**
     * 设置实际结果
     */
    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }
    
    /**
     * 设置提取的数据
     */
    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }
    
    /**
     * 设置任务结果
     */
    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }
    
    /**
     * 设置平台信息
     */
    public void setPlatformInfo(PlatformInfo platformInfo) {
        this.platformInfo = platformInfo;
    }
    
    /**
     * 设置性能指标
     */
    public void setPerformanceMetrics(Map<String, Object> metrics) {
        this.performanceMetrics = Objects.requireNonNullElse(metrics, new HashMap<>());
    }
    
    /**
     * 添加性能指标
     */
    public void addPerformanceMetric(String key, Object value) {
        this.performanceMetrics.put(key, value);
    }
    
    /**
     * 设置额外信息
     */
    public void setAdditionalInfo(Map<String, Object> info) {
        this.additionalInfo = Objects.requireNonNullElse(info, new HashMap<>());
    }
    
    /**
     * 添加额外信息
     */
    public void addAdditionalInfo(String key, Object value) {
        this.additionalInfo.put(key, value);
    }
    
    /**
     * 获取详细的失败信息描述
     */
    public String getDetailedFailureMessage() {
        if (passed) {
            return null;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("测试失败: ").append(testName).append(" (ID: ").append(testId).append(")\n");
        
        if (errorMessage != null) {
            message.append("错误信息: ").append(errorMessage).append("\n");
        }
        
        if (expectedResult != null && actualResult != null) {
            message.append("预期结果: ").append(expectedResult).append("\n");
            message.append("实际结果: ").append(actualResult).append("\n");
        }
        
        if (taskResult != null && taskResult.getError() != null) {
            message.append("任务错误: ").append(taskResult.getError()).append("\n");
        }
        
        return message.toString();
    }
    
    /**
     * 导出结果为JSON格式的字符串
     */
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"testId\":\"").append(testId).append("\",");
        json.append("\"testName\":\"").append(testName).append("\",");
        json.append("\"passed\":").append(passed).append(",");
        json.append("\"duration\":").append(duration);
        
        if (errorMessage != null) {
            json.append(",\"errorMessage\":\"").append(escapeJson(errorMessage)).append("\"");
        }
        
        if (startTime != null) {
            json.append(",\"startTime\":\"").append(startTime).append("\"");
        }
        
        if (endTime != null) {
            json.append(",\"endTime\":\"").append(endTime).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * JSON转义辅助方法
     */
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                  .replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Getters
    public String getTestId() {
        return testId;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public boolean isPassed() {
        return passed;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getExpectedResult() {
        return expectedResult;
    }
    
    public String getActualResult() {
        return actualResult;
    }
    
    public String getExtractedData() {
        return extractedData;
    }
    
    public TaskResult getTaskResult() {
        return taskResult;
    }
    
    public PlatformInfo getPlatformInfo() {
        return platformInfo;
    }
    
    public Map<String, Object> getPerformanceMetrics() {
        return new HashMap<>(performanceMetrics);
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return new HashMap<>(additionalInfo);
    }
    
    @Override
    public String toString() {
        return "EvaluationResult{" +
                "testId='" + testId + '\'' +
                ", testName='" + testName + '\'' +
                ", passed=" + passed +
                ", duration=" + duration + "ms" +
                (errorMessage != null ? ", errorMessage='" + errorMessage + "'" : "") +
                '}';
    }
}