package com.midscene.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存工具类，用于管理AI响应、UI上下文等数据的缓存
 * 支持基于时间的缓存过期策略
 */
public class CacheUtil {
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRY_TIME_MS = 300000; // 默认5分钟过期
    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);
    
    /**
     * 缓存条目，包含缓存值和过期时间
     */
    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;
        
        public CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
    
    /**
     * 放入缓存，使用默认过期时间
     * @param key 缓存键
     * @param value 缓存值
     */
    public static void put(String key, Object value) {
        put(key, value, DEFAULT_EXPIRY_TIME_MS);
    }
    
    /**
     * 放入缓存，使用指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param expiryTimeMs 过期时间（毫秒）
     */
    public static void put(String key, Object value, long expiryTimeMs) {
        if (key == null || value == null) {
            return;
        }
        
        long expiryTime = System.currentTimeMillis() + expiryTimeMs;
        CACHE.put(key, new CacheEntry(value, expiryTime));
        LoggerUtil.debug(logger, "Added to cache: {}", key);
    }
    
    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        if (key == null) {
            return null;
        }
        
        CacheEntry entry = CACHE.get(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            CACHE.remove(key);
            LoggerUtil.debug(logger, "Cache expired and removed: {}", key);
            return null;
        }
        
        LoggerUtil.debug(logger, "Retrieved from cache: {}", key);
        return (T) entry.getValue();
    }
    
    /**
     * 从缓存中移除指定键
     * @param key 缓存键
     */
    public static void remove(String key) {
        if (key != null) {
            CACHE.remove(key);
            LoggerUtil.debug(logger, "Removed from cache: {}", key);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public static void clear() {
        CACHE.clear();
        LoggerUtil.info(logger, "Cache cleared");
    }
    
    /**
     * 获取当前缓存大小
     * @return 缓存条目数量
     */
    public static int size() {
        // 先清理过期项
        cleanupExpiredEntries();
        return CACHE.size();
    }
    
    /**
     * 清理过期的缓存条目
     */
    public static void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 生成缓存键（用于AI请求）
     * @param prompt 提示文本
     * @param modelName 模型名称
     * @return 缓存键
     */
    public static String generateCacheKeyForAI(String prompt, String modelName) {
        return "ai:" + modelName + ":" + prompt.hashCode();
    }
    
    /**
     * 生成缓存键（用于UI上下文）
     * @param pageUrl 页面URL或设备信息
     * @return 缓存键
     */
    public static String generateCacheKeyForUI(String pageUrl) {
        return "ui:" + pageUrl.hashCode();
    }
}