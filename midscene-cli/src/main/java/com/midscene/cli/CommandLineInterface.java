package com.midscene.cli;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.model.InterfaceType;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.util.CacheUtil;
import com.midscene.core.util.LoggerUtil;
import com.midscene.core.util.ReportGenerator;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Midscene Java命令行接口
 * 提供自动化操作的命令行入口
 */
@Command(
    name = "midscene",
    description = "Midscene Java自动化工具 - AI驱动的UI自动化框架",
    mixinStandardHelpOptions = true,
    version = "1.0.0"
)
public class CommandLineInterface implements Callable<Integer> {
    private static final Logger logger = LoggerUtil.getLogger(CommandLineInterface.class);
    
    @Option(names = {"-p", "--platform"}, description = "平台类型: web/selenium, web/playwright, android", required = true)
    private String platform;
    
    @Option(names = {"-u", "--url"}, description = "目标URL (Web平台)")
    private String url;
    
    @Option(names = {"-d", "--device"}, description = "设备ID (Android平台)")
    private String deviceId;
    
    @Option(names = {"-a", "--action"}, description = "执行的动作")
    private String action;
    
    @Option(names = {"-f", "--file"}, description = "从文件读取动作")
    private File actionFile;
    
    @Option(names = {"--headless"}, description = "无头模式 (Web平台)", defaultValue = "false")
    private boolean headless;
    
    @Option(names = {"--output", "-o"}, description = "输出目录", defaultValue = "./output")
    private String outputDir;
    
    @Option(names = {"--generate-report"}, description = "生成报告", defaultValue = "true")
    private boolean generateReport;
    
    @Option(names = {"--cache-ttl"}, description = "缓存过期时间(秒)", defaultValue = "300")
    private long cacheTtlSeconds;
    
    @Option(names = {"--timeout"}, description = "操作超时时间(秒)", defaultValue = "60")
    private long timeoutSeconds;
    
    @Option(names = {"--verbose", "-v"}, description = "详细日志", defaultValue = "false")
    private boolean verbose;
    
    @Override
    public Integer call() throws Exception {
        // 初始化日志
        initLogger();
        
        LoggerUtil.info(logger, "Starting Midscene CLI");
        LoggerUtil.info(logger, "Platform: {}", platform);
        
        // 初始化缓存
        CacheUtil.clear();
        
        // 创建报告生成器
        ReportGenerator reportGenerator = null;
        if (generateReport) {
            reportGenerator = new ReportGenerator(
                "Midscene Execution Report",
                Map.of(
                    "platform", platform,
                    "url", url,
                    "deviceId", deviceId,
                    "headless", headless
                )
            );
        }
        
        PlatformInterface platformInterface = null;
        Agent agent = null;
        
        try {
            // 创建平台接口
            platformInterface = createPlatformInterface();
            
            // 创建Agent
            agent = new Agent(platformInterface);
            
            // 执行动作
            if (actionFile != null && actionFile.exists()) {
                // 从文件读取动作
                executeActionsFromFile(actionFile, agent, reportGenerator);
            } else if (action != null) {
                // 执行单个动作
                executeSingleAction(action, agent, reportGenerator);
            } else {
            LoggerUtil.error(logger, "No action specified. Use --action or --file");
            return 1;
        }
            
            // 保存报告
            if (generateReport && reportGenerator != null) {
                reportGenerator.saveReports(outputDir);
            }
            
            LoggerUtil.info(logger, "Execution completed successfully");
            return 0;
            
        } catch (Exception e) {
            LoggerUtil.error(logger, "Execution failed", e);
            
            // 在报告中记录错误
            if (generateReport && reportGenerator != null) {
                var errorStep = reportGenerator.createStep()
                    .withAction("cli_execution")
                    .withDescription("CLI执行")
                    .withSuccess(false)
                    .withErrorMessage(e.getMessage())
                    .build();
                reportGenerator.addStep(errorStep);
                reportGenerator.saveReports(outputDir);
            }
            
            return 1;
            
        } finally {
            // 清理资源
            if (agent != null) {
                try {
                    agent.close();
                } catch (Exception e) {
                    LoggerUtil.error(logger, "Failed to close agent", e);
                }
            }
            
            if (platformInterface != null) {
                try {
                    platformInterface.close();
                } catch (Exception e) {
                    LoggerUtil.error(logger, "Failed to close platform interface", e);
                }
            }
            
            CacheUtil.clear();
        }
    }
    
    private void initLogger() {
        if (verbose) {
            // 设置为debug级别
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        } else {
            // 设置为info级别
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        }
    }
    
    private PlatformInterface createPlatformInterface() throws Exception {
        String[] platformParts = platform.split("/");
        String platformType = platformParts[0];
        String platformImpl = platformParts.length > 1 ? platformParts[1] : null;
        
        switch (platformType.toLowerCase()) {
            case "web":
                return createWebPlatformInterface(platformImpl);
            case "android":
                return createAndroidPlatformInterface();
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
    
    private PlatformInterface createWebPlatformInterface(String impl) throws Exception {
        if ("selenium".equals(impl)) {
            // 这里应该导入并使用Selenium实现
            // 由于具体实现类不确定，暂时使用反射或占位符
            throw new UnsupportedOperationException("Selenium implementation not fully implemented yet");
        } else if ("playwright".equals(impl) || impl == null) {
            // 这里应该导入并使用Playwright实现
            throw new UnsupportedOperationException("Playwright implementation not fully implemented yet");
        } else {
            throw new IllegalArgumentException("Unsupported web implementation: " + impl);
        }
    }
    
    private PlatformInterface createAndroidPlatformInterface() throws Exception {
        // 这里应该导入并使用Android实现
        throw new UnsupportedOperationException("Android implementation not fully implemented yet");
    }
    
    private void executeSingleAction(String action, Agent agent, ReportGenerator reportGenerator) throws Exception {
        LoggerUtil.info(logger, "Executing action: {}", action);
        
        long startTime = System.currentTimeMillis();
        ReportGenerator.StepBuilder stepBuilder = null;
        
        if (reportGenerator != null) {
            stepBuilder = reportGenerator.createStep()
                .withAction("ai_action")
                .withDescription(action);
        }
        
        try {
            // 执行AI动作
            var result = agent.aiAction(action)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .join();
            
            LoggerUtil.info(logger, "Action result: {}", result);
            
            if (reportGenerator != null && stepBuilder != null) {
                boolean isSuccess = result.getStatus() == TaskStatus.COMPLETED;
                ReportGenerator.ExecutionStep step = stepBuilder.withSuccess(isSuccess).build();
                step.setEndTime(); // 设置结束时间
                reportGenerator.addStep(step);
            }
            
        } catch (Exception e) {
            if (reportGenerator != null && stepBuilder != null) {
                ReportGenerator.ExecutionStep step = stepBuilder.withSuccess(false)
                    .withErrorMessage(e.getMessage())
                    .build();
                step.setEndTime(); // 设置结束时间
                reportGenerator.addStep(step);
            }
            throw e;
        }
    }
    
    private void executeActionsFromFile(File file, Agent agent, ReportGenerator reportGenerator) throws Exception {
        LoggerUtil.info(logger, "Executing actions from file: {}", file.getAbsolutePath());
        
        // 这里应该读取文件内容并执行每个动作
        // 暂时抛出未实现异常
        throw new UnsupportedOperationException("File execution not fully implemented yet");
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandLineInterface()).execute(args);
        System.exit(exitCode);
    }
}