package com.midscene.evaluation;

import com.midscene.core.agent.Agent;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.shared.platform.PlatformInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 评估引擎类
 * 提供自动化测试和性能评估的核心功能
 */
public class EvaluationEngine {
    private final Agent agent;
    private final EvaluationConfig config;
    private final Map<String, TestCase> testCases = new ConcurrentHashMap<>();
    private final List<EvaluationResult> results = new ArrayList<>();
    private final AtomicInteger executedTests = new AtomicInteger(0);
    private final AtomicInteger passedTests = new AtomicInteger(0);
    private final AtomicInteger failedTests = new AtomicInteger(0);
    
    public EvaluationEngine(Agent agent) {
        this(agent, new EvaluationConfig());
    }
    
    public EvaluationEngine(Agent agent, EvaluationConfig config) {
        this.agent = agent;
        this.config = config;
    }
    
    /**
     * 注册测试用例
     */
    public void registerTestCase(TestCase testCase) {
        if (testCase != null) {
            testCases.put(testCase.getId(), testCase);
        }
    }
    
    /**
     * 执行所有测试用例
     */
    public CompletableFuture<EvaluationSummary> executeAllTests() {
        List<CompletableFuture<EvaluationResult>> futureResults = new ArrayList<>();
        
        for (TestCase testCase : testCases.values()) {
            futureResults.add(executeTestCase(testCase));
        }
        
        return CompletableFuture.allOf(futureResults.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<EvaluationResult> allResults = new ArrayList<>();
                for (CompletableFuture<EvaluationResult> future : futureResults) {
                    allResults.add(future.join());
                }
                
                return generateSummary(allResults);
            });
    }
    
    /**
     * 执行单个测试用例
     */
    public CompletableFuture<EvaluationResult> executeTestCase(TestCase testCase) {
        if (testCase == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        Instant startTime = Instant.now();
        
        return CompletableFuture.runAsync(() -> {
            result.setStartTime(startTime);
            result.setPlatformInfo(agent.getPlatform().getPlatformInfo());
        })
        .thenCompose(v -> {
            switch (testCase.getType()) {
                case ACTION:
                    return executeActionTest(testCase);
                case TAP:
                    return executeTapTest(testCase);
                case INPUT:
                    return executeInputTest(testCase);
                case DATA_EXTRACTION:
                    return executeDataExtractionTest(testCase);
                case PERFORMANCE:
                    return executePerformanceTest(testCase);
                default:
                    return CompletableFuture.completedFuture(result);
            }
        })
        .thenApply(r -> {
            Instant endTime = Instant.now();
            r.setEndTime(endTime);
            r.setDuration(Duration.between(startTime, endTime).toMillis());
            
            synchronized (this) {
                results.add(r);
                executedTests.incrementAndGet();
                if (r.isPassed()) {
                    passedTests.incrementAndGet();
                } else {
                    failedTests.incrementAndGet();
                }
            }
            
            return r;
        });
    }
    
    /**
     * 执行动作测试
     */
    private CompletableFuture<EvaluationResult> executeActionTest(TestCase testCase) {
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        
        return agent.aiAction(testCase.getInstruction())
            .thenApply(taskResult -> {
                result.setTaskResult(taskResult);
                
                if (taskResult.getStatus() == TaskStatus.SUCCESS) {
                    // 验证结果是否符合预期
                    if (testCase.getExpectedResult() != null) {
                        boolean matches = verifyResult(taskResult.getResult(), testCase.getExpectedResult());
                        result.setPassed(matches);
                        if (!matches) {
                            result.setErrorMessage("实际结果与预期不符");
                            result.setActualResult(taskResult.getResult());
                            result.setExpectedResult(testCase.getExpectedResult());
                        }
                    } else {
                        result.setPassed(true);
                    }
                } else {
                    result.setPassed(false);
                    result.setErrorMessage(taskResult.getError() != null ? taskResult.getError() : "任务执行失败");
                }
                
                return result;
            })
            .exceptionally(e -> {
                result.setPassed(false);
                result.setErrorMessage("测试执行异常: " + e.getMessage());
                return result;
            });
    }
    
    /**
     * 执行点击测试
     */
    private CompletableFuture<EvaluationResult> executeTapTest(TestCase testCase) {
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        
        return agent.aiTap(testCase.getTargetDescription())
            .thenApply(taskResult -> {
                result.setTaskResult(taskResult);
                result.setPassed(taskResult.getStatus() == TaskStatus.SUCCESS);
                
                if (!result.isPassed()) {
                    result.setErrorMessage(taskResult.getError() != null ? taskResult.getError() : "点击操作失败");
                }
                
                return result;
            })
            .exceptionally(e -> {
                result.setPassed(false);
                result.setErrorMessage("测试执行异常: " + e.getMessage());
                return result;
            });
    }
    
    /**
     * 执行输入测试
     */
    private CompletableFuture<EvaluationResult> executeInputTest(TestCase testCase) {
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        
        return agent.aiInput(testCase.getTargetDescription(), testCase.getInputText())
            .thenApply(taskResult -> {
                result.setTaskResult(taskResult);
                result.setPassed(taskResult.getStatus() == TaskStatus.SUCCESS);
                
                if (!result.isPassed()) {
                    result.setErrorMessage(taskResult.getError() != null ? taskResult.getError() : "输入操作失败");
                }
                
                return result;
            })
            .exceptionally(e -> {
                result.setPassed(false);
                result.setErrorMessage("测试执行异常: " + e.getMessage());
                return result;
            });
    }
    
    /**
     * 执行数据提取测试
     */
    private CompletableFuture<EvaluationResult> executeDataExtractionTest(TestCase testCase) {
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        
        return agent.extractData(testCase.getTargetDescription())
            .thenApply(extractedData -> {
                result.setExtractedData(extractedData);
                
                // 验证提取的数据是否符合预期
                if (testCase.getExpectedResult() != null) {
                    boolean matches = verifyResult(extractedData, testCase.getExpectedResult());
                    result.setPassed(matches);
                    if (!matches) {
                        result.setErrorMessage("提取的数据与预期不符");
                        result.setActualResult(extractedData);
                        result.setExpectedResult(testCase.getExpectedResult());
                    }
                } else {
                    result.setPassed(extractedData != null && !extractedData.isEmpty());
                    if (!result.isPassed()) {
                        result.setErrorMessage("未能提取到数据");
                    }
                }
                
                return result;
            })
            .exceptionally(e -> {
                result.setPassed(false);
                result.setErrorMessage("测试执行异常: " + e.getMessage());
                return result;
            });
    }
    
    /**
     * 执行性能测试
     */
    private CompletableFuture<EvaluationResult> executePerformanceTest(TestCase testCase) {
        EvaluationResult result = new EvaluationResult(testCase.getId(), testCase.getName());
        
        int iterations = testCase.getPerformanceIterations() > 0 ? testCase.getPerformanceIterations() : 5;
        List<Long> durations = new ArrayList<>();
        
        // 执行多次以获取平均性能指标
        CompletableFuture<Void>[] futures = new CompletableFuture[iterations];
        for (int i = 0; i < iterations; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                Instant start = Instant.now();
                agent.aiAction(testCase.getInstruction()).join();
                Instant end = Instant.now();
                durations.add(Duration.between(start, end).toMillis());
            });
        }
        
        return CompletableFuture.allOf(futures)
            .thenApply(v -> {
                // 计算性能指标
                long total = durations.stream().mapToLong(Long::longValue).sum();
                long avg = total / iterations;
                long min = durations.stream().mapToLong(Long::longValue).min().orElse(0);
                long max = durations.stream().mapToLong(Long::longValue).max().orElse(0);
                
                result.setPerformanceMetrics(Map.of(
                    "iterations", iterations,
                    "average_ms", avg,
                    "min_ms", min,
                    "max_ms", max,
                    "total_ms", total
                ));
                
                // 检查是否满足性能要求
                long threshold = testCase.getPerformanceThresholdMs() > 0 ? testCase.getPerformanceThresholdMs() : 5000;
                result.setPassed(avg <= threshold);
                
                if (!result.isPassed()) {
                    result.setErrorMessage("性能不达标: 平均执行时间 " + avg + "ms 超过阈值 " + threshold + "ms");
                }
                
                return result;
            })
            .exceptionally(e -> {
                result.setPassed(false);
                result.setErrorMessage("性能测试执行异常: " + e.getMessage());
                return result;
            });
    }
    
    /**
     * 验证结果是否符合预期
     */
    private boolean verifyResult(String actual, String expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        
        // 支持简单的包含关系验证
        if (config.isUseContainsMatch()) {
            return actual.contains(expected);
        }
        
        // 支持正则表达式验证
        if (config.isUseRegexMatch()) {
            return actual.matches(expected);
        }
        
        // 默认精确匹配
        return actual.equals(expected);
    }
    
    /**
     * 生成评估摘要
     */
    private EvaluationSummary generateSummary(List<EvaluationResult> allResults) {
        EvaluationSummary summary = new EvaluationSummary();
        summary.setTotalTests(executedTests.get());
        summary.setPassedTests(passedTests.get());
        summary.setFailedTests(failedTests.get());
        summary.setResults(allResults);
        
        // 计算通过率
        if (executedTests.get() > 0) {
            summary.setPassRate((double) passedTests.get() / executedTests.get() * 100);
        }
        
        // 计算平均执行时间
        long totalDuration = allResults.stream()
            .mapToLong(EvaluationResult::getDuration)
            .sum();
        if (!allResults.isEmpty()) {
            summary.setAverageExecutionTime(totalDuration / allResults.size());
        }
        
        return summary;
    }
    
    /**
     * 获取所有执行结果
     */
    public List<EvaluationResult> getResults() {
        return new ArrayList<>(results);
    }
    
    /**
     * 清除所有结果
     */
    public void clearResults() {
        synchronized (this) {
            results.clear();
            executedTests.set(0);
            passedTests.set(0);
            failedTests.set(0);
        }
    }
}