package com.midscene.core.agent;

import com.midscene.core.model.UiContext;
import com.midscene.core.model.Rect;
import com.midscene.core.model.Point;
import com.midscene.core.util.LoggerUtil;
import com.midscene.core.exception.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.Map;

/**
 * Insight引擎，负责UI理解和分析
 * 处理元素定位、页面分析等功能
 */
public class InsightEngine {
    private static final Logger logger = LoggerFactory.getLogger(InsightEngine.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Supplier<CompletableFuture<UiContext>> contextProvider;
    private final AIModelService aiService;
    private UiContext cachedContext;
    private long cacheTimestamp;
    
    /**
     * 构造函数
     * @param contextProvider UI上下文提供者
     * @param aiService AI服务实例
     */
    public InsightEngine(
            Supplier<CompletableFuture<UiContext>> contextProvider,
            AIModelService aiService) {
        
        if (contextProvider == null) {
            throw new IllegalArgumentException("Context provider cannot be null");
        }
        if (aiService == null) {
            throw new IllegalArgumentException("AI service cannot be null");
        }
        
        this.contextProvider = contextProvider;
        this.aiService = aiService;
        this.cachedContext = null;
        this.cacheTimestamp = 0;
    }
    
    /**
     * 定位元素
     * @param elementDescription 元素描述
     * @return 定位结果
     */
    public CompletableFuture<LocateResult> locate(String elementDescription) {
        if (elementDescription == null || elementDescription.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    LocateResult.createFailure("Element description cannot be empty"));
        }
        
        logger.info("Locating element: {}", elementDescription);
        
        // 获取UI上下文
        return getCurrentUiContext()
                .thenCompose(context -> {
                    // 调用AI服务进行元素定位
                    return aiService.locateElement(context, elementDescription)
                            .thenApply(jsonResult -> {
                                try {
                                    // 解析JSON结果
                                    return parseLocateResult(jsonResult, elementDescription);
                                } catch (Exception e) {
                                    logger.error("Failed to parse locate result: {}", e.getMessage(), e);
                                    return LocateResult.createFailure("Failed to parse locate result: " + e.getMessage());
                                }
                            });
                })
                .exceptionally(ex -> {
                    String errorMsg = ExceptionUtil.getRootCause(ex).getMessage();
                    logger.error("Failed to locate element: {}", errorMsg, ex);
                    return LocateResult.createFailure("Failed to locate element: " + errorMsg);
                });
    }
    
    /**
     * 分析页面内容
     * @param prompt 分析提示
     * @return 分析结果
     */
    public CompletableFuture<String> analyzePage(String prompt) {
        logger.info("Analyzing page with prompt: {}", prompt);
        
        return getCurrentUiContext()
                .thenCompose(context -> aiService.analyzeUiContext(context, prompt))
                .exceptionally(ex -> {
                    String errorMsg = ExceptionUtil.getRootCause(ex).getMessage();
                    logger.error("Failed to analyze page: {}", errorMsg, ex);
                    return "Failed to analyze page: " + errorMsg;
                });
    }
    
    /**
     * 规划操作步骤
     * @param userIntent 用户意图
     * @return 操作计划JSON
     */
    public CompletableFuture<String> planActions(String userIntent) {
        logger.info("Planning actions for intent: {}", userIntent);
        
        return getCurrentUiContext()
                .thenCompose(context -> aiService.planActions(context, userIntent))
                .exceptionally(ex -> {
                    String errorMsg = ExceptionUtil.getRootCause(ex).getMessage();
                    logger.error("Failed to plan actions: {}", errorMsg, ex);
                    return "{\"error\": \"Failed to plan actions: " + errorMsg + "\"}";
                });
    }
    
    /**
     * 获取当前UI上下文
     * @return UI上下文
     */
    private CompletableFuture<UiContext> getCurrentUiContext() {
        // 检查缓存是否有效（这里简单处理，实际可以设置缓存过期时间）
        if (cachedContext != null && System.currentTimeMillis() - cacheTimestamp < 1000) {
            logger.debug("Using cached UI context");
            return CompletableFuture.completedFuture(cachedContext);
        }
        
        // 获取新的UI上下文
        return contextProvider.get()
                .thenApply(context -> {
                    this.cachedContext = context;
                    this.cacheTimestamp = System.currentTimeMillis();
                    logger.debug("Retrieved new UI context");
                    return context;
                });
    }
    
    /**
     * 解析定位结果JSON
     * @param jsonResult JSON格式的定位结果
     * @param elementDescription 元素描述
     * @return 定位结果对象
     * @throws Exception 解析异常
     */
    @SuppressWarnings("unchecked")
    private LocateResult parseLocateResult(String jsonResult, String elementDescription) throws Exception {
        Map<String, Object> resultMap = objectMapper.readValue(jsonResult, Map.class);
        
        boolean success = Boolean.TRUE.equals(resultMap.get("success"));
        
        if (!success) {
            String error = (String) resultMap.getOrDefault("error", "Element not found");
            return LocateResult.createFailure(error);
        }
        
        // 提取置信度
        double confidence = ((Number) resultMap.getOrDefault("confidence", 0.0)).doubleValue();
        
        // 提取边界框信息
        Map<String, Number> boundingBoxMap = (Map<String, Number>) resultMap.get("bounding_box");
        Rect boundingBox = null;
        
        if (boundingBoxMap != null) {
            int left = boundingBoxMap.get("left").intValue();
            int top = boundingBoxMap.get("top").intValue();
            int width = boundingBoxMap.get("width").intValue();
            int height = boundingBoxMap.get("height").intValue();
            
            boundingBox = new Rect(left, top, width, height);
        }
        
        // 创建定位结果
        LocateResult result = LocateResult.createSuccess(null, boundingBox, confidence);
        
        // 设置元素信息
        String elementInfo = (String) resultMap.get("element_info");
        if (elementInfo != null) {
            result.setElementInfo(elementInfo);
        } else {
            result.setElementInfo(elementDescription);
        }
        
        logger.debug("Located element with confidence: {}", confidence);
        return result;
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        this.cachedContext = null;
        this.cacheTimestamp = 0;
        logger.debug("Cleared UI context cache");
    }
    
    /**
     * 获取AI服务实例
     * @return AI服务实例
     */
    public AIModelService getAiService() {
        return aiService;
    }
}