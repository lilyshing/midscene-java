package com.midscene.core.insight;

import com.midscene.core.ai.AIModelConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI模型服务接口，定义了与AI模型交互的方法
 */
public interface AIModelService {
    
    /**
     * 生成操作计划
     * @param request AI请求
     * @param config 模型配置
     * @return 操作计划
     */
    CompletableFuture<OperationPlan> generateOperationPlan(AIRequest request, AIModelConfig config);
    
    /**
     * 定位UI元素
     * @param request AI请求
     * @param config 模型配置
     * @return 定位结果
     */
    CompletableFuture<LocateResult> locateElement(AIRequest request, AIModelConfig config);
    
    /**
     * 提取页面数据
     * @param request AI请求
     * @param config 模型配置
     * @return 提取的数据
     */
    CompletableFuture<Map<String, Object>> extractData(AIRequest request, AIModelConfig config);
    
    /**
     * 验证断言
     * @param request AI请求
     * @param config 模型配置
     * @return 断言结果
     */
    CompletableFuture<AssertResult> verifyAssertion(AIRequest request, AIModelConfig config);
}