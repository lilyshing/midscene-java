package com.midscene.evaluation;

import com.midscene.core.agent.Agent;
import com.midscene.core.model.TaskResult;
import com.midscene.shared.platform.PlatformInterface;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * 评估工具示例类
 * 展示如何集成和使用评估引擎进行自动化测试和性能评估
 */
public class EvaluationToolExample {
    private final EvaluationEngine engine;
    private final ReportGenerator reportGenerator;
    
    /**
     * 构造函数
     */
    public EvaluationToolExample(Agent agent) {
        // 创建评估配置
        EvaluationConfig config = EvaluationConfig.builder()
            .withTimeout(30000)       // 30秒超时
            .withRetries(1)           // 失败重试1次
            .withDetailedReport()     // 生成详细报告
            .withPerformanceMetrics() // 记录性能指标
            .withReportFormat(EvaluationConfig.ReportFormat.HTML)
            .build();
        
        // 创建评估引擎
        this.engine = new EvaluationEngine(agent, config);
        
        // 创建报告生成器
        this.reportGenerator = new ReportGenerator("./evaluation-reports");
    }
    
    /**
     * 配置常用测试用例
     */
    public void configureStandardTestCases() {
        // 注册基础功能测试用例
        registerBasicFunctionalityTests();
        
        // 注册性能测试用例
        registerPerformanceTests();
        
        // 注册数据提取测试用例
        registerDataExtractionTests();
    }
    
    /**
     * 注册基础功能测试用例
     */
    private void registerBasicFunctionalityTests() {
        // 点击操作测试
        engine.registerTestCase(TestCase.createTapTest(
            "测试点击按钮",
            "点击页面上的登录按钮"
        ));
        
        // 输入操作测试
        engine.registerTestCase(TestCase.createInputTest(
            "测试表单输入",
            "用户名字段",
            "testuser123"
        ));
        
        // 通用AI动作测试
        engine.registerTestCase(TestCase.createActionTest(
            "测试完整登录流程",
            "输入用户名'test@example.com'，密码'password123'，然后点击登录按钮"
        ).withExpectedResult("登录成功"));
    }
    
    /**
     * 注册性能测试用例
     */
    private void registerPerformanceTests() {
        // 页面加载性能测试
        engine.registerTestCase(TestCase.createPerformanceTest(
            "页面加载性能测试",
            "等待页面完全加载并验证主要元素可见",
            10,  // 执行10次
            3000 // 平均时间不超过3秒
        ));
        
        // 交互响应性能测试
        engine.registerTestCase(TestCase.createPerformanceTest(
            "交互响应性能测试",
            "快速点击导航菜单中的各个选项并返回",
            5,   // 执行5次
            2000 // 平均时间不超过2秒
        ));
    }
    
    /**
     * 注册数据提取测试用例
     */
    private void registerDataExtractionTests() {
        // 提取产品信息
        engine.registerTestCase(TestCase.createDataExtractionTest(
            "提取产品价格信息",
            "页面上的产品价格",
            "¥"
        ));
        
        // 提取用户信息
        engine.registerTestCase(TestCase.createDataExtractionTest(
            "提取用户信息",
            "用户个人资料页面上的用户名、邮箱和注册日期",
            "@"
        ));
    }
    
    /**
     * 执行所有测试并生成报告
     */
    public void runFullEvaluation() {
        System.out.println("开始执行评估测试...");
        
        try {
            // 执行所有测试用例
            EvaluationSummary summary = engine.executeAllTests().join();
            
            // 打印摘要报告
            System.out.println("\n" + summary.generateSummaryReport());
            
            // 保存报告
            System.out.println("\n保存评估报告...");
            
            // 生成多种格式的报告
            reportGenerator.generateMultiFormatReports(summary, Arrays.asList(
                EvaluationConfig.ReportFormat.JSON,
                EvaluationConfig.ReportFormat.HTML,
                EvaluationConfig.ReportFormat.CSV
            ));
            
            System.out.println("评估完成！报告已保存到 evaluation-reports 目录");
            
        } catch (Exception e) {
            System.err.println("评估执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行特定测试用例
     */
    public void runSingleTest(String testId) {
        System.out.println("执行单个测试用例: " + testId);
        
        try {
            // 创建一个简单的测试用例
            TestCase testCase = TestCase.createActionTest(
                "临时测试",
                "执行简单的UI验证"
            ).withId(testId);
            
            // 执行测试
            EvaluationResult result = engine.executeTestCase(testCase).join();
            
            // 打印结果
            System.out.println("\n测试结果: " + (result.isPassed() ? "通过" : "失败"));
            System.out.println("执行时间: " + result.getDuration() + "ms");
            
            if (!result.isPassed() && result.getErrorMessage() != null) {
                System.out.println("错误信息: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前配置的测试用例统计信息
     */
    public void printTestStatistics() {
        // 这里可以添加获取和打印测试用例统计信息的逻辑
        System.out.println("当前已配置的测试用例信息...");
    }
    
    /**
     * 示例：如何使用这个评估工具
     * 注意：这是一个静态方法，仅作为使用示例
     */
    public static void demonstrateUsage(Agent agent) {
        // 创建评估工具实例
        EvaluationToolExample evaluator = new EvaluationToolExample(agent);
        
        // 配置标准测试用例
        evaluator.configureStandardTestCases();
        
        // 打印测试统计
        evaluator.printTestStatistics();
        
        // 执行完整评估
        evaluator.runFullEvaluation();
    }
    
    /**
     * 主方法 - 仅作为使用示例
     * 实际使用时应从应用程序中调用demonstrateUsage方法
     */
    public static void main(String[] args) {
        System.out.println("评估工具示例程序");
        System.out.println("注意：这是一个示例，实际使用时需要提供有效的Agent实例");
        
        // 在实际应用中，这里应该传入一个已初始化的Agent实例
        // Agent agent = new YourAgentImplementation();
        // demonstrateUsage(agent);
    }
}