package com.midscene.core.service;

import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 洞察引擎接口
 * 负责分析UI上下文，提供元素识别、意图理解等功能
 */
public interface InsightEngine {
    /**
     * 分析UI上下文，识别元素和理解结构
     * @param uiContext UI上下文
     * @return 分析结果
     */
    CompletableFuture<Map<String, Object>> analyzeUiContext(UiContext uiContext);
    
    /**
     * 根据描述找到匹配的UI元素
     * @param uiContext UI上下文
     * @param description 元素描述
     * @return 匹配的元素列表
     */
    CompletableFuture<List<UiElement>> findElementsByDescription(UiContext uiContext, String description);
    
    /**
     * 理解用户意图
     * @param instruction 用户指令
     * @param context 上下文信息
     * @return 理解结果
     */
    CompletableFuture<Map<String, Object>> understandIntent(String instruction, Map<String, Object> context);
    
    /**
     * 生成操作计划
     * @param intent 意图信息
     * @param uiContext UI上下文
     * @return 操作计划
     */
    CompletableFuture<List<Map<String, Object>>> generateActionPlan(Map<String, Object> intent, UiContext uiContext);
    
    /**
     * 初始化洞察引擎
     * @param config 配置信息
     */
    void initialize(Map<String, Object> config);
    
    /**
     * 关闭洞察引擎
     */
    void shutdown();
}