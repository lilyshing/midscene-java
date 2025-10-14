package com.midscene.core.service.impl;

import com.midscene.core.ai.ModelProvider;
import com.midscene.core.ai.ModelProviderFactory;
import com.midscene.core.cache.AIModelCache;
import com.midscene.core.exception.MidsceneException;
import com.midscene.core.service.AIModelService;
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
 * AI模型服务的默认实现类
 * 提供基础的AI模型交互功能，集成多种AI模型提供商
 */
public class DefaultAIModelService implements AIModelService {
    
    private ModelProviderFactory factory;
    private ModelProvider defaultProvider;
    private Map<String, Object> globalConfig;
    private AIModelCache aiModelCache;

    public DefaultAIModelService() {
        this.factory = ModelProviderFactory.getInstance();
        this.globalConfig = new java.util.HashMap<>();
        // 初始化AI模型缓存，默认30秒过期，最多1000条缓存
        this.aiModelCache = new AIModelCache(30000, 1000);
    }
    
    @Override
    public CompletableFuture<String> generate(String prompt, Map<String, Object> options) {
        try {
            ModelProvider provider = getModelProvider(options);
            return provider.generateText(prompt, mergeOptions(options));
        } catch (Exception e) {
            throw new MidsceneException("Failed to generate text: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompletableFuture<String> chat(List<Map<String, String>> messages, Map<String, Object> options) {
        try {
            ModelProvider provider = getModelProvider(options);
            return provider.chat(messages, mergeOptions(options));
        } catch (Exception e) {
            throw new MidsceneException("Failed to generate chat: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> analyzeImage(String imageBase64, String prompt, Map<String, Object> options) {
        try {
            ModelProvider provider = getModelProvider(options);
            return provider.analyzeImage(imageBase64, prompt, mergeOptions(options));
        } catch (Exception e) {
            throw new MidsceneException("Failed to analyze image: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompletableFuture<String> extractInformation(String context, String query) {
        try {
            ModelProvider provider = getModelProvider(null);
            return provider.extractInformation(context, query);
        } catch (Exception e) {
            throw new MidsceneException("Failed to extract information: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void initialize(Map<String, Object> config) {
        try {
            // 保存全局配置
            if (config != null) {
                this.globalConfig.putAll(config);
                
                // 从配置中读取缓存相关配置
                if (config.containsKey("aiCacheExpiryMs")) {
                    try {
                        long expiryMs = Long.parseLong(config.get("aiCacheExpiryMs").toString());
                        int maxEntries = aiModelCache.getMaxEntries();
                        this.aiModelCache = new AIModelCache(expiryMs, maxEntries);
                    } catch (Exception e) {
                        // 如果配置无效，保持默认值
                        System.err.println("Invalid aiCacheExpiryMs configuration, using default");
                    }
                }
                if (config.containsKey("aiCacheMaxEntries")) {
                    try {
                        int maxEntries = Integer.parseInt(config.get("aiCacheMaxEntries").toString());
                        long expiryMs = aiModelCache.getExpiryMs();
                        this.aiModelCache = new AIModelCache(expiryMs, maxEntries);
                    } catch (Exception e) {
                        // 如果配置无效，保持默认值
                        System.err.println("Invalid aiCacheMaxEntries configuration, using default");
                    }
                }
            }
            
            // 注册并初始化OpenAI提供商
            Map<String, Object> openAIConfig = extractProviderConfig(config, "openai");
            if (!openAIConfig.isEmpty()) {
                factory.registerProvider("openai", com.midscene.core.ai.OpenAIModelProvider.class);
                // 创建并缓存实例
                factory.getProvider("openai", openAIConfig);
            }
            
            // 注册并初始化Azure OpenAI提供商
            Map<String, Object> azureConfig = extractProviderConfig(config, "azure-openai");
            if (!azureConfig.isEmpty()) {
                factory.registerProvider("azure-openai", com.midscene.core.ai.AzureOpenAIModelProvider.class);
                // 创建并缓存实例
                factory.getProvider("azure-openai", azureConfig);
            }
            
            // 设置默认提供商
            String defaultProviderName = (String) config.getOrDefault("defaultProvider", "openai");
            this.defaultProvider = factory.getProvider(defaultProviderName);
            
            // 如果默认提供商不可用，尝试使用任何可用的提供商
            if (this.defaultProvider == null) {
                this.defaultProvider = factory.getAnyProvider();
            }
        } catch (Exception e) {
            throw new MidsceneException("Failed to initialize AI model service: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void shutdown() {
        // 关闭所有注册的提供商
        factory.shutdownAllProviders();
        // 清理缓存
        if (aiModelCache != null) {
            aiModelCache.clear();
        }
    }
    
    /**
     * 获取指定的模型提供商
     */
    private ModelProvider getModelProvider(Map<String, Object> options) {
        if (options != null && options.containsKey("provider")) {
            String providerName = (String) options.get("provider");
            ModelProvider provider = factory.getProvider(providerName);
            if (provider != null) {
                return provider;
            }
        }
        
        if (defaultProvider == null) {
            throw new MidsceneException("No AI model provider available");
        }
        
        return defaultProvider;
    }
    
    /**
     * 合并全局配置和选项
     */
    private Map<String, Object> mergeOptions(Map<String, Object> options) {
        Map<String, Object> mergedOptions = new java.util.HashMap<>(globalConfig);
        if (options != null) {
            mergedOptions.putAll(options);
        }
        return mergedOptions;
    }
    
    /**
     * 从配置中提取特定提供商的配置
     */
    private Map<String, Object> extractProviderConfig(Map<String, Object> config, String providerName) {
        Map<String, Object> providerConfig = new java.util.HashMap<>();
        
        if (config != null) {
            // 提取特定提供商的配置
            Object providerConfigObj = config.get(providerName);
            if (providerConfigObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedConfig = (Map<String, Object>) providerConfigObj;
                providerConfig.putAll(typedConfig);
            }
            
            // 提取顶级配置中的相关键值对
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String key = entry.getKey();
                // 提取提供商特定的环境变量命名格式的配置
                if (key.toUpperCase().contains(providerName.toUpperCase()) ||
                    key.equals("apiKey") || key.equals("model") || key.equals("timeout")) {
                    providerConfig.put(key, entry.getValue());
                }
            }
        }
        
        return providerConfig;
    }

    @Override
    public AIActionResult getCachedResult(AIActionRequest request) {
        return aiModelCache.getActionResult(request);
    }

    @Override
    public void cacheResult(AIActionRequest request, AIActionResult result) {
        aiModelCache.putActionResult(request, result);
    }

    @Override
    public AITapResult getCachedResult(AITapRequest request) {
        return aiModelCache.getTapResult(request);
    }

    @Override
    public void cacheResult(AITapRequest request, AITapResult result) {
        aiModelCache.putTapResult(request, result);
    }

    @Override
    public AIInputResult getCachedResult(AIInputRequest request) {
        return aiModelCache.getInputResult(request);
    }

    @Override
    public void cacheResult(AIInputRequest request, AIInputResult result) {
        aiModelCache.putInputResult(request, result);
    }

    @Override
    public ExtractDataResult getCachedResult(ExtractDataRequest request) {
        return aiModelCache.getExtractResult(request);
    }

    @Override
    public void cacheResult(ExtractDataRequest request, ExtractDataResult result) {
        aiModelCache.putExtractResult(request, result);
    }

    @Override
    public String getCacheStatistics() {
        return aiModelCache.getStatistics().toString();
    }
}