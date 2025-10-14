package com.midscene.core.service;

import com.midscene.core.model.TaskResult;
import com.midscene.shared.platform.PlatformInterface;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 任务执行器接口
 * 负责执行各种自动化操作任务
 */
public interface TaskExecutor {
    /**
     * 执行通用任务
     * @param taskType 任务类型
     * @param parameters 任务参数
     * @return 任务执行结果
     */
    CompletableFuture<TaskResult> executeTask(String taskType, Map<String, Object> parameters);
    
    /**
     * 执行点击操作
     * @param target 目标信息
     * @return 任务执行结果
     */
    CompletableFuture<TaskResult> executeTap(Map<String, Object> target);
    
    /**
     * 执行输入操作
     * @param target 目标信息
     * @param text 输入文本
     * @return 任务执行结果
     */
    CompletableFuture<TaskResult> executeInput(Map<String, Object> target, String text);
    
    /**
     * 执行滑动操作
     * @param start 起始位置
     * @param end 结束位置
     * @param duration 持续时间(毫秒)
     * @return 任务执行结果
     */
    CompletableFuture<TaskResult> executeSwipe(Map<String, Object> start, Map<String, Object> end, int duration);
    
    /**
     * 执行等待操作
     * @param condition 等待条件
     * @param timeout 超时时间(毫秒)
     * @return 任务执行结果
     */
    CompletableFuture<TaskResult> executeWait(Map<String, Object> condition, long timeout);
    
    /**
     * 设置平台接口
     * @param platform 平台接口实例
     */
    void setPlatform(PlatformInterface platform);
    
    /**
     * 初始化任务执行器
     * @param config 配置信息
     */
    void initialize(Map<String, Object> config);
    
    /**
     * 关闭任务执行器
     */
    void shutdown();
}