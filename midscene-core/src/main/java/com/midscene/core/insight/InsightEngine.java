package com.midscene.core.insight;

import com.midscene.core.ai.AIModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * AI推理引擎，负责页面分析、元素定位和操作决策
 */
public class InsightEngine {
    private static final Logger logger = LoggerFactory.getLogger(InsightEngine.class);
    
    private final Supplier<CompletableFuture<Object>> contextProvider;
    private final AIModelService aiService;
    private final AIModelConfig modelConfig;
    
    /**
     * 构造函数
     * @param contextProvider 上下文提供者
     * @param aiService AI模型服务
     * @param modelConfig AI模型配置
     */
    public InsightEngine(
            Supplier<CompletableFuture<Object>> contextProvider,
            AIModelService aiService,
            AIModelConfig modelConfig) {
        this.contextProvider = contextProvider;
        this.aiService = aiService;
        this.modelConfig = modelConfig;
    }
    
    /**
     * 分析用户意图并生成操作计划
     * @param prompt 用户提示
     * @return 操作计划
     */
    public CompletableFuture<OperationPlan> analyzeIntent(String prompt) {
        return contextProvider.get()
            .thenCompose(context -> {
                // 调用AI服务生成操作计划
                return aiService.generateOperationPlan(new AIRequest(), modelConfig);
            });
    }
    
    /**
     * 定位UI元素
     * @param description 元素描述
     * @return 元素定位结果
     */
    public CompletableFuture<LocateResult> locateElement(String description) {
        return contextProvider.get()
            .thenCompose(context -> {
                return aiService.locateElement(new AIRequest(), modelConfig);
            });
    }
    
    /**
     * 提取页面数据
     * @param schema 数据提取模式
     * @return 提取的数据
     */
    public CompletableFuture<Map<String, Object>> extractData(Map<String, String> schema) {
        return contextProvider.get()
            .thenCompose(context -> {
                return aiService.extractData(new AIRequest(), modelConfig);
            });
    }
    
    /**
     * 验证断言
     * @param assertion 断言内容
     * @return 断言结果
     */
    public CompletableFuture<AssertResult> verifyAssertion(String assertion) {
        return contextProvider.get()
            .thenCompose(context -> {
                return aiService.verifyAssertion(new AIRequest(), modelConfig);
            });
    }
    
    /**
     * 获取当前配置
     */
    public AIModelConfig getModelConfig() {
        return modelConfig;
    }
}