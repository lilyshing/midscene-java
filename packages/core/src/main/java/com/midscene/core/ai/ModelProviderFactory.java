package com.midscene.core.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型提供商工厂类，负责管理和动态选择不同的模型提供商实现
 */
public class ModelProviderFactory {
    private static final ModelProviderFactory INSTANCE = new ModelProviderFactory();
    private final Map<String, ModelProvider> providerInstances = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends ModelProvider>> providerClasses = new ConcurrentHashMap<>();
    
    private ModelProviderFactory() {
        // 注册默认的模型提供商
        registerProvider("openai", OpenAIModelProvider.class);
        registerProvider("azure", AzureOpenAIModelProvider.class);
        registerProvider("default", DefaultModelProvider.class);
    }
    
    /**
     * 获取工厂实例
     */
    public static ModelProviderFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册一个新的模型提供商
     */
    public void registerProvider(String name, Class<? extends ModelProvider> providerClass) {
        if (name == null || providerClass == null) {
            throw new IllegalArgumentException("Provider name and class cannot be null");
        }
        providerClasses.put(name.toLowerCase(), providerClass);
    }
    
    /**
     * 获取指定名称的模型提供商实例
     */
    public ModelProvider getProvider(String name, Map<String, Object> config) {
        if (name == null || name.trim().isEmpty()) {
            name = "default"; // 默认使用default提供商
        }
        
        String normalizedName = name.toLowerCase();
        return providerInstances.computeIfAbsent(normalizedName, n -> createProvider(n, config));
    }
    
    /**
     * 获取指定名称的模型提供商实例（简化版本）
     */
    public ModelProvider getProvider(String name) {
        return getProvider(name, null);
    }
    
    /**
     * 获取任意可用的模型提供商实例
     */
    public ModelProvider getAnyProvider() {
        if (providerClasses.isEmpty()) {
            return null;
        }
        String firstProviderName = providerClasses.keySet().iterator().next();
        return getProvider(firstProviderName);
    }
    
    /**
     * 创建模型提供商实例
     */
    private ModelProvider createProvider(String name, Map<String, Object> config) {
        try {
            Class<? extends ModelProvider> providerClass = providerClasses.get(name);
            if (providerClass == null) {
                // 如果找不到指定的提供商，则使用默认提供商
                providerClass = DefaultModelProvider.class;
            }
            
            // 创建实例
            ModelProvider provider = providerClass.getDeclaredConstructor().newInstance();
            
            // 初始化提供商
            if (config != null) {
                provider.initialize(config);
            }
            
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create model provider: " + name, e);
        }
    }
    
    /**
     * 根据配置自动选择合适的模型提供商
     */
    public ModelProvider autoSelectProvider(Map<String, Object> config) {
        if (config == null) {
            return getProvider("default", null);
        }
        
        // 根据配置中的提供商名称选择
        String providerName = (String) config.get("provider");
        if (providerName != null && !providerName.trim().isEmpty()) {
            return getProvider(providerName, config);
        }
        
        // 根据配置特征自动判断
        if (config.containsKey("azureEndpoint") || config.containsKey("AZURE_OPENAI_ENDPOINT")) {
            return getProvider("azure", config);
        }
        
        if (config.containsKey("apiKey") || config.containsKey("OPENAI_API_KEY")) {
            return getProvider("openai", config);
        }
        
        // 默认返回default提供商
        return getProvider("default", config);
    }
    
    /**
     * 清理所有已创建的提供商实例
     */
    public void clearProviders() {
        shutdownAllProviders();
    }
    
    /**
     * 关闭所有模型提供商实例
     */
    public void shutdownAllProviders() {
        for (ModelProvider provider : providerInstances.values()) {
            try {
                provider.shutdown();
            } catch (Exception e) {
                // 忽略关闭过程中的异常
                e.printStackTrace();
            }
        }
        providerInstances.clear();
    }
}