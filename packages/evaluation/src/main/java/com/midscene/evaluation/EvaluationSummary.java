package com.midscene.evaluation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评估摘要类
 * 汇总所有测试用例的执行结果，提供整体评估统计信息
 */
public class EvaluationSummary {
    private Instant startTime;
    private Instant endTime;
    private long totalExecutionTime = 0;
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private double passRate = 0.0;
    private long averageExecutionTime = 0;
    private List<EvaluationResult> results = new ArrayList<>();
    private Map<String, Integer> testTypeDistribution;
    private Map<String, Double> typePassRates;
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private String platformName;
    private String platformVersion;
    
    public EvaluationSummary() {
        this.startTime = Instant.now();
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
        if (startTime != null) {
            this.totalExecutionTime = Duration.between(startTime, endTime).toMillis();
        }
    }
    
    /**
     * 设置总测试数
     */
    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }
    
    /**
     * 设置通过测试数
     */
    public void setPassedTests(int passedTests) {
        this.passedTests = passedTests;
    }
    
    /**
     * 设置失败测试数
     */
    public void setFailedTests(int failedTests) {
        this.failedTests = failedTests;
    }
    
    /**
     * 设置通过率
     */
    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }
    
    /**
     * 设置平均执行时间
     */
    public void setAverageExecutionTime(long averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }
    
    /**
     * 设置测试结果列表
     */
    public void setResults(List<EvaluationResult> results) {
        this.results = new ArrayList<>(results != null ? results : new ArrayList<>());
        calculateDerivedMetrics();
    }
    
    /**
     * 添加单个测试结果
     */
    public void addResult(EvaluationResult result) {
        if (result != null) {
            results.add(result);
            totalTests++;
            if (result.isPassed()) {
                passedTests++;
            } else {
                failedTests++;
            }
            calculateDerivedMetrics();
        }
    }
    
    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            warnings.add(warning);
        }
    }
    
    /**
     * 添加错误信息
     */
    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            errors.add(error);
        }
    }
    
    /**
     * 设置平台名称
     */
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
    
    /**
     * 设置平台版本
     */
    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }
    
    /**
     * 计算派生指标
     */
    private void calculateDerivedMetrics() {
        // 计算通过率
        if (totalTests > 0) {
            passRate = (double) passedTests / totalTests * 100;
        }
        
        // 计算平均执行时间
        if (!results.isEmpty()) {
            long totalDuration = results.stream()
                .mapToLong(EvaluationResult::getDuration)
                .sum();
            averageExecutionTime = totalDuration / results.size();
        }
        
        // 计算测试类型分布
        testTypeDistribution = results.stream()
            .collect(Collectors.groupingBy(
                r -> {
                    TestCase testCase = findTestCaseById(r.getTestId());
                    return testCase != null ? testCase.getType().name() : "UNKNOWN";
                },
                Collectors.summingInt(r -> 1)
            ));
        
        // 计算各类型测试的通过率
        typePassRates = new java.util.HashMap<>();
        testTypeDistribution.forEach((type, count) -> {
            long passed = results.stream()
                .filter(r -> {
                    TestCase testCase = findTestCaseById(r.getTestId());
                    return testCase != null && testCase.getType().name().equals(type) && r.isPassed();
                })
                .count();
            typePassRates.put(type, (double) passed / count * 100);
        });
    }
    
    /**
     * 根据ID查找测试用例
     * 注意：这里需要改进，实际应该从TestRepository或其他地方获取完整的测试用例信息
     */
    private TestCase findTestCaseById(String testId) {
        // 临时实现，实际应该从测试用例仓库中查找
        return null;
    }
    
    /**
     * 获取所有失败的测试结果
     */
    public List<EvaluationResult> getFailedResults() {
        return results.stream()
            .filter(r -> !r.isPassed())
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有通过的测试结果
     */
    public List<EvaluationResult> getPassedResults() {
        return results.stream()
            .filter(EvaluationResult::isPassed)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取最慢的N个测试结果
     */
    public List<EvaluationResult> getSlowestResults(int limit) {
        return results.stream()
            .sorted((a, b) -> Long.compare(b.getDuration(), a.getDuration()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 导出摘要为JSON格式的字符串
     */
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"startTime\":\"").append(startTime).append("\",");
        json.append("\"endTime\":\"").append(endTime != null ? endTime : "").append("\",");
        json.append("\"totalExecutionTime\":").append(totalExecutionTime).append(",");
        json.append("\"totalTests\":").append(totalTests).append(",");
        json.append("\"passedTests\":").append(passedTests).append(",");
        json.append("\"failedTests\":").append(failedTests).append(",");
        json.append("\"passRate\":") .append(String.format("%.2f", passRate)).append(",");
        json.append("\"averageExecutionTime\":").append(averageExecutionTime);
        
        if (platformName != null) {
            json.append(",\"platformName\":\"").append(platformName).append("\"");
        }
        
        if (platformVersion != null) {
            json.append(",\"platformVersion\":\"").append(platformVersion).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 生成简短的摘要报告文本
     */
    public String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("===== 评估报告摘要 =====\n");
        report.append("执行时间: ").append(startTime).append(" - ")
              .append(endTime != null ? endTime : "进行中").append("\n");
        report.append("总耗时: ").append(totalExecutionTime).append("ms\n");
        report.append("测试总数: ").append(totalTests).append("\n");
        report.append("通过数: ").append(passedTests).append("\n");
        report.append("失败数: ").append(failedTests).append("\n");
        report.append("通过率: ").append(String.format("%.2f", passRate)).append("%\n");
        report.append("平均执行时间: ").append(averageExecutionTime).append("ms\n");
        
        if (platformName != null) {
            report.append("平台: ").append(platformName);
            if (platformVersion != null) {
                report.append(" (v").append(platformVersion).append(")");
            }
            report.append("\n");
        }
        
        if (!warnings.isEmpty()) {
            report.append("\n警告: ").append(warnings.size()).append("\n");
            warnings.forEach(w -> report.append("  - ").append(w).append("\n"));
        }
        
        if (!errors.isEmpty()) {
            report.append("\n错误: ").append(errors.size()).append("\n");
            errors.forEach(e -> report.append("  - ").append(e).append("\n"));
        }
        
        return report.toString();
    }
    
    // Getters
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public int getTotalTests() {
        return totalTests;
    }
    
    public int getPassedTests() {
        return passedTests;
    }
    
    public int getFailedTests() {
        return failedTests;
    }
    
    public double getPassRate() {
        return passRate;
    }
    
    public long getAverageExecutionTime() {
        return averageExecutionTime;
    }
    
    public List<EvaluationResult> getResults() {
        return new ArrayList<>(results);
    }
    
    public Map<String, Integer> getTestTypeDistribution() {
        return testTypeDistribution != null ? new java.util.HashMap<>(testTypeDistribution) : new java.util.HashMap<>();
    }
    
    public Map<String, Double> getTypePassRates() {
        return typePassRates != null ? new java.util.HashMap<>(typePassRates) : new java.util.HashMap<>();
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public String getPlatformName() {
        return platformName;
    }
    
    public String getPlatformVersion() {
        return platformVersion;
    }
    
    @Override
    public String toString() {
        return generateSummaryReport();
    }
}