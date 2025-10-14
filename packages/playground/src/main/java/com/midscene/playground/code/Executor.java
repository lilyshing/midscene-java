package com.midscene.playground.code;

import java.util.Map;

/**
 * 代码执行器接口
 * 定义代码执行的基本功能
 */
public interface Executor {
    
    /**
     * 执行代码
     * @param code 要执行的代码
     * @return 执行结果
     */
    ExecutionResult execute(String code);
    
    /**
     * 执行代码
     * @param code 要执行的代码
     * @param context 执行上下文
     * @return 执行结果
     */
    ExecutionResult execute(String code, Map<String, Object> context);
    
    /**
     * 执行代码（带有超时设置）
     * @param code 要执行的代码
     * @param timeoutMs 超时时间（毫秒）
     * @return 执行结果
     */
    ExecutionResult executeWithTimeout(String code, long timeoutMs);
    
    /**
     * 执行代码（带有超时设置和上下文）
     * @param code 要执行的代码
     * @param context 执行上下文
     * @param timeoutMs 超时时间（毫秒）
     * @return 执行结果
     */
    ExecutionResult executeWithTimeout(String code, Map<String, Object> context, long timeoutMs);
    
    /**
     * 获取执行器支持的语言
     * @return 支持的语言名称
     */
    String getSupportedLanguage();
    
    /**
     * 检查是否支持指定的语言
     * @param language 语言名称
     * @return 是否支持
     */
    boolean supportsLanguage(String language);
    
    /**
     * 重置执行器状态
     */
    void reset();
    
    /**
     * 关闭执行器，释放资源
     */
    void close();
}