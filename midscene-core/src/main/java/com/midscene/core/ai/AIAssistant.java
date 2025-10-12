package com.midscene.core.ai;

import com.midscene.core.model.UiElement;
import com.midscene.core.model.UINode;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI助手抽象类，定义AI模型交互的基本接口
 */
public abstract class AIAssistant {
    /**
     * 根据自然语言提示和UI上下文分析UI元素
     * @param prompt 用户自然语言提示
     * @param uiElements UI元素列表
     * @return 分析结果
     */
    public abstract CompletableFuture<String> analyzeUI(String prompt, List<UiElement> uiElements);

    /**
     * 根据自然语言请求规划执行步骤
     * @param request 用户自然语言请求
     * @param uiContext UI上下文信息
     * @return 生成的执行计划
     */
    public abstract CompletableFuture<String> planSteps(String request, String uiContext);

    /**
     * 根据自然语言条件定位UI元素
     * @param condition 定位条件
     * @param uiElements UI元素列表
     * @return 定位到的UI元素索引
     */
    public abstract CompletableFuture<Integer> locateElement(String condition, List<UiElement> uiElements);

    /**
     * 从UI节点树中提取特定信息
     * @param query 信息提取查询
     * @param uiNode UI节点树
     * @return 提取的信息
     */
    public abstract CompletableFuture<String> extractData(String query, UINode uiNode);

    /**
     * 验证UI是否满足特定条件
     * @param condition 验证条件
     * @param uiElements UI元素列表
     * @return 验证结果
     */
    public abstract CompletableFuture<Boolean> verifyCondition(String condition, List<UiElement> uiElements);

    /**
     * 处理执行过程中的异常情况
     * @param error 错误描述
     * @param context 上下文信息
     * @return 异常处理建议
     */
    public abstract CompletableFuture<String> handleException(String error, String context);
}