package com.midscene.compatibility;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;
import com.midscene.shared.platform.PlatformInterface as NewPlatformInterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 兼容性工厂类
 * 提供统一的对象创建接口，支持旧系统和新系统之间的无缝切换
 */
public class CompatibilityFactory {
    private static final CompatibilityFactory instance = new CompatibilityFactory();
    private final Map<String, CompatibilityService.PlatformAdapter> platformAdapters = new ConcurrentHashMap<>();
    
    private CompatibilityFactory() {
        // 注册默认的平台适配器
        registerDefaultAdapters();
    }
    
    /**
     * 获取工厂实例
     */
    public static CompatibilityFactory getInstance() {
        return instance;
    }
    
    /**
     * 注册平台适配器
     */
    public void registerPlatformAdapter(CompatibilityService.PlatformAdapter adapter) {
        if (adapter != null) {
            platformAdapters.put(adapter.getPlatformType().toUpperCase(), adapter);
        }
    }
    
    /**
     * 注册默认的平台适配器
     */
    private void registerDefaultAdapters() {
        registerPlatformAdapter(new CompatibilityService.WebPlatformAdapter());
        registerPlatformAdapter(new CompatibilityService.AndroidPlatformAdapter());
        registerPlatformAdapter(new CompatibilityService.iOSPlatformAdapter());
    }
    
    /**
     * 创建兼容的Agent实例
     */
    public Agent createCompatibleAgent(String platformType, Map<String, Object> options,
                                      AIModelService aiModelService,
                                      InsightEngine insightEngine,
                                      TaskExecutor taskExecutor) {
        String normalizedType = CompatibilityUtils.getCompatiblePlatformName(platformType);
        
        // 尝试使用新平台创建
        NewPlatformInterface newPlatform = createNewPlatform(normalizedType, options);
        if (newPlatform != null) {
            PlatformInterface oldPlatform = CompatibilityAdapter.adaptNewToOldPlatform(newPlatform);
            return new Agent(oldPlatform, aiModelService, insightEngine, taskExecutor);
        }
        
        // 如果新平台创建失败，尝试使用旧平台
        PlatformInterface oldPlatform = createOldPlatform(normalizedType, options);
        if (oldPlatform != null) {
            return new Agent(oldPlatform, aiModelService, insightEngine, taskExecutor);
        }
        
        throw new IllegalArgumentException("Failed to create agent for platform: " + platformType);
    }
    
    /**
     * 创建新平台实例
     */
    private NewPlatformInterface createNewPlatform(String platformType, Map<String, Object> options) {
        CompatibilityService.PlatformAdapter adapter = platformAdapters.get(platformType);
        if (adapter != null) {
            return adapter.createNewPlatform(options);
        }
        
        // 尝试直接创建新平台实例
        try {
            switch (platformType) {
                case "WEB":
                    return (NewPlatformInterface) Class.forName("com.midscene.webdriver.WebDriverPlatform")
                            .getDeclaredConstructor().newInstance();
                case "ANDROID":
                    return (NewPlatformInterface) Class.forName("com.midscene.android.AndroidPlatform")
                            .getDeclaredConstructor().newInstance();
                case "IOS":
                    return (NewPlatformInterface) Class.forName("com.midscene.ios.iOSPlatform")
                            .getDeclaredConstructor().newInstance();
                default:
                    return null;
            }
        } catch (Exception e) {
            // 创建失败，返回null
            return null;
        }
    }
    
    /**
     * 创建旧平台实例
     */
    private PlatformInterface createOldPlatform(String platformType, Map<String, Object> options) {
        CompatibilityService.PlatformAdapter adapter = platformAdapters.get(platformType);
        if (adapter != null) {
            return adapter.createOldPlatform(options);
        }
        
        // 这里需要根据旧平台的实际实现进行创建
        // 暂时返回null，实际使用时需要实现
        return null;
    }
    
    /**
     * 创建兼容的平台接口
     * 根据系统配置自动选择使用旧平台或新平台
     */
    public PlatformInterface createCompatiblePlatform(String platformType, Map<String, Object> options) {
        boolean useNewPlatform = options != null && Boolean.TRUE.equals(options.get("useNewPlatform"));
        
        if (useNewPlatform) {
            NewPlatformInterface newPlatform = createNewPlatform(platformType, options);
            if (newPlatform != null) {
                return CompatibilityAdapter.adaptNewToOldPlatform(newPlatform);
            }
        }
        
        // 默认使用旧平台
        PlatformInterface oldPlatform = createOldPlatform(platformType, options);
        if (oldPlatform != null) {
            return oldPlatform;
        }
        
        throw new IllegalArgumentException("Failed to create platform interface for: " + platformType);
    }
    
    /**
     * 获取支持的平台类型列表
     */
    public String[] getSupportedPlatforms() {
        return platformAdapters.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查平台是否支持
     */
    public boolean isPlatformSupported(String platformType) {
        String normalizedType = CompatibilityUtils.getCompatiblePlatformName(platformType);
        return platformAdapters.containsKey(normalizedType);
    }
    
    /**
     * 创建兼容性配置
     */
    public CompatibilityConfig createCompatibilityConfig() {
        return new CompatibilityConfig();
    }
    
    /**
     * 兼容性配置类
     */
    public static class CompatibilityConfig {
        private boolean preferNewPlatform = true;
        private boolean fallbackToOldPlatform = true;
        private Map<String, Object> platformSpecificOptions = new ConcurrentHashMap<>();
        
        public CompatibilityConfig preferNewPlatform(boolean prefer) {
            this.preferNewPlatform = prefer;
            return this;
        }
        
        public CompatibilityConfig fallbackToOldPlatform(boolean fallback) {
            this.fallbackToOldPlatform = fallback;
            return this;
        }
        
        public CompatibilityConfig withPlatformOption(String platform, String key, Object value) {
            Map<String, Object> options = (Map<String, Object>) platformSpecificOptions.computeIfAbsent(
                    platform, k -> new ConcurrentHashMap<>());
            options.put(key, value);
            return this;
        }
        
        public boolean isPreferNewPlatform() {
            return preferNewPlatform;
        }
        
        public boolean isFallbackToOldPlatform() {
            return fallbackToOldPlatform;
        }
        
        public Map<String, Object> getPlatformSpecificOptions(String platform) {
            return (Map<String, Object>) platformSpecificOptions.getOrDefault(platform, new ConcurrentHashMap<>());
        }
    }
}