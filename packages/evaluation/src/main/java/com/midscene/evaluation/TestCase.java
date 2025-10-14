package com.midscene.evaluation;

import java.util.Objects;
import java.util.UUID;

/**
 * 测试用例类
 * 定义不同类型的测试场景和测试参数
 */
public class TestCase {
    /**
     * 测试用例类型枚举
     */
    public enum TestType {
        ACTION,        // 通用AI动作测试
        TAP,           // 点击操作测试
        INPUT,         // 输入操作测试
        DATA_EXTRACTION, // 数据提取测试
        PERFORMANCE    // 性能测试
    }
    
    private final String id;
    private final String name;
    private final TestType type;
    private String instruction;
    private String targetDescription;
    private String inputText;
    private String expectedResult;
    private int performanceIterations = 5;
    private long performanceThresholdMs = 5000;
    private boolean enabled = true;
    
    /**
     * 创建通用AI动作测试用例
     */
    public static TestCase createActionTest(String name, String instruction) {
        return new TestCase(name, TestType.ACTION).withInstruction(instruction);
    }
    
    /**
     * 创建点击操作测试用例
     */
    public static TestCase createTapTest(String name, String targetDescription) {
        return new TestCase(name, TestType.TAP).withTargetDescription(targetDescription);
    }
    
    /**
     * 创建输入操作测试用例
     */
    public static TestCase createInputTest(String name, String targetDescription, String inputText) {
        return new TestCase(name, TestType.INPUT)
            .withTargetDescription(targetDescription)
            .withInputText(inputText);
    }
    
    /**
     * 创建数据提取测试用例
     */
    public static TestCase createDataExtractionTest(String name, String targetDescription, String expectedResult) {
        return new TestCase(name, TestType.DATA_EXTRACTION)
            .withTargetDescription(targetDescription)
            .withExpectedResult(expectedResult);
    }
    
    /**
     * 创建性能测试用例
     */
    public static TestCase createPerformanceTest(String name, String instruction, int iterations, long thresholdMs) {
        return new TestCase(name, TestType.PERFORMANCE)
            .withInstruction(instruction)
            .withPerformanceIterations(iterations)
            .withPerformanceThresholdMs(thresholdMs);
    }
    
    /**
     * 构造函数
     */
    private TestCase(String name, TestType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
    }
    
    /**
     * 自定义测试ID
     */
    public TestCase withId(String id) {
        this.id = Objects.requireNonNull(id, "测试ID不能为空");
        return this;
    }
    
    /**
     * 设置测试指令
     */
    public TestCase withInstruction(String instruction) {
        this.instruction = instruction;
        return this;
    }
    
    /**
     * 设置目标描述
     */
    public TestCase withTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
        return this;
    }
    
    /**
     * 设置输入文本
     */
    public TestCase withInputText(String inputText) {
        this.inputText = inputText;
        return this;
    }
    
    /**
     * 设置预期结果
     */
    public TestCase withExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
        return this;
    }
    
    /**
     * 设置性能测试迭代次数
     */
    public TestCase withPerformanceIterations(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("迭代次数必须大于0");
        }
        this.performanceIterations = iterations;
        return this;
    }
    
    /**
     * 设置性能测试阈值(毫秒)
     */
    public TestCase withPerformanceThresholdMs(long thresholdMs) {
        if (thresholdMs < 0) {
            throw new IllegalArgumentException("阈值不能为负数");
        }
        this.performanceThresholdMs = thresholdMs;
        return this;
    }
    
    /**
     * 设置测试是否启用
     */
    public TestCase withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public TestType getType() {
        return type;
    }
    
    public String getInstruction() {
        return instruction;
    }
    
    public String getTargetDescription() {
        return targetDescription;
    }
    
    public String getInputText() {
        return inputText;
    }
    
    public String getExpectedResult() {
        return expectedResult;
    }
    
    public int getPerformanceIterations() {
        return performanceIterations;
    }
    
    public long getPerformanceThresholdMs() {
        return performanceThresholdMs;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return id.equals(testCase.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TestCase{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", enabled=" + enabled +
                '}';
    }
}