package com.midscene.core.service;

import com.midscene.shared.model.ai.AIActionRequest;
import com.midscene.shared.model.ai.AIActionResult;
import com.midscene.shared.model.ai.AIInputRequest;
import com.midscene.shared.model.ai.AIInputResult;
import com.midscene.shared.model.ai.AITapRequest;
import com.midscene.shared.model.ai.AITapResult;
import com.midscene.shared.model.ai.ExtractDataRequest;
import com.midscene.shared.model.ai.ExtractDataResult;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI模型服务接口
 * 负责与AI模型的交互，提供推理、理解等功能
 */
public interface AIModelService {
    /**
     * 执行文本生成
     * @param prompt 提示文本
     * @param options 生成选项
     * @return 生成的文本
     */
    CompletableFuture<String> generate(String prompt, Map<String, Object> options);
    
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
     * 初始化AI模型服务
     * @param config 配置信息
     */
    void initialize(Map<String, Object> config);
    
    /**
     * 关闭AI模型服务
     */
    void shutdown();
    
    /**
     * 获取缓存的AI动作结果
     * @param request AI动作请求
     * @return 缓存的结果，如果没有则返回null
     */
    AIActionResult getCachedResult(AIActionRequest request);
    
    /**
     * 缓存AI动作结果
     * @param request AI动作请求
     * @param result AI动作结果
     */
    void cacheResult(AIActionRequest request, AIActionResult result);
    
    /**
     * 获取缓存的AI点击结果
     * @param request AI点击请求
     * @return 缓存的结果，如果没有则返回null
     */
    AITapResult getCachedResult(AITapRequest request);
    
    /**
     * 缓存AI点击结果
     * @param request AI点击请求
     * @param result AI点击结果
     */
    void cacheResult(AITapRequest request, AITapResult result);
    
    /**
     * 获取缓存的AI输入结果
     * @param request AI输入请求
     * @return 缓存的结果，如果没有则返回null
     */
    AIInputResult getCachedResult(AIInputRequest request);
    
    /**
     * 缓存AI输入结果
     * @param request AI输入请求
     * @param result AI输入结果
     */
    void cacheResult(AIInputRequest request, AIInputResult result);
    
    /**
     * 获取缓存的数据提取结果
     * @param request 数据提取请求
     * @return 缓存的结果，如果没有则返回null
     */
    ExtractDataResult getCachedResult(ExtractDataRequest request);
    
    /**
     * 缓存数据提取结果
     * @param request 数据提取请求
     * @param result 数据提取结果
     */
    void cacheResult(ExtractDataRequest request, ExtractDataResult result);
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计信息字符串
     */
    String getCacheStatistics();
}