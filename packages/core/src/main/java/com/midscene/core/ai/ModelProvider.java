package com.midscene.core.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模型提供商接口
 * 定义与不同AI模型提供商交互的标准方法
 */
public interface ModelProvider {
    
    /**
     * 执行文本生成
     * @param prompt 提示文本
     * @param options 生成选项
     * @return 生成的文本
     */
    CompletableFuture<String> generateText(String prompt, Map<String, Object> options);
    
    /**
     * 执行多轮对话
     * @param messages 对话消息列表
     * @param options 生成选项
     * @return 生成的响应
     */
    CompletableFuture<String> chat(List<Map<String, String>> messages, Map<String, Object> options);
    
    /**
     * 执行图像分析
     * @param imageBase64 图像的Base64编码
     * @param prompt 提示文本
     * @param options 分析选项
     * @return 分析结果
     */
    CompletableFuture<Map<String, Object>> analyzeImage(String imageBase64, String prompt, Map<String, Object> options);
    
    /**
     * 从上下文中提取信息
     * @param context 上下文信息
     * @param query 查询内容
     * @return 提取的信息
     */
    CompletableFuture<String> extractInformation(String context, String query);
    
    /**
     * 初始化模型提供商
     * @param config 配置信息
     * @return 是否初始化成功
     */
    boolean initialize(Map<String, Object> config);
    
    /**
     * 关闭模型提供商
     */
    void shutdown();
    
    /**
     * 获取提供商名称
     * @return 提供商名称
     */
    String getProviderName();
}