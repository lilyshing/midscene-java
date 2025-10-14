package com.midscene.core.cache;

import com.midscene.shared.platform.UiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UI上下文多级缓存类
 * 提供高性能的UI上下文缓存机制，支持多级缓存策略和智能失效
 */
public class UiContextCache {
    private static final Logger logger = LoggerFactory.getLogger(UiContextCache.class);
    
    // 一级缓存：快速访问缓存，过期时间短
    private final Map<String, CachedUiContext> level1Cache;
    private final long level1CacheExpiryMs;
    
    // 二级缓存：更持久的缓存，过期时间较长
    private final Map<String, CachedUiContext> level2Cache;
    private final long level2CacheExpiryMs;
    
    // 缓存键前缀
    private static final String DEFAULT_CACHE_KEY = "default_ui_context";
    
    // 缓存统计信息
    private final CacheStatistics statistics;
    
    // 操作类型枚举
    public enum OperationType {
        READ,      // 只读操作，不会改变页面状态
        MODIFY,    // 修改操作，可能改变页面状态
        NAVIGATE,  // 导航操作，肯定改变页面状态
        UNKNOWN    // 未知操作类型
    }
    
    public UiContextCache() {
        this(500, 5000); // 默认：一级缓存500ms，二级缓存5s
    }
    
    public UiContextCache(long level1ExpiryMs, long level2ExpiryMs) {
        this.level1Cache = new ConcurrentHashMap<>();
        this.level2Cache = new ConcurrentHashMap<>();
        this.level1CacheExpiryMs = level1ExpiryMs;
        this.level2CacheExpiryMs = level2ExpiryMs;
        this.statistics = new CacheStatistics();
    }
    
    /**
     * 从缓存中获取UI上下文
     * @param cacheKey 缓存键，默认为"default_ui_context"
     * @return 缓存的UI上下文，如果缓存未命中或已过期则返回null
     */
    public UiContext get(String cacheKey) {
        if (cacheKey == null) {
            cacheKey = DEFAULT_CACHE_KEY;
        }
        
        // 更新统计信息：总请求数
        statistics.totalRequests.incrementAndGet();
        
        // 1. 先检查一级缓存
        CachedUiContext cached = level1Cache.get(cacheKey);
        if (cached != null && !cached.isExpired(level1CacheExpiryMs)) {
            logger.debug("Level 1 cache hit for UI context: {}", cacheKey);
            statistics.level1Hits.incrementAndGet();
            cached.incrementAccessCount();
            return cached.getUiContext();
        }
        
        // 2. 检查二级缓存
        cached = level2Cache.get(cacheKey);
        if (cached != null && !cached.isExpired(level2CacheExpiryMs)) {
            logger.debug("Level 2 cache hit for UI context: {}", cacheKey);
            statistics.level2Hits.incrementAndGet();
            cached.incrementAccessCount();
            // 二级缓存命中时，同时更新一级缓存
            level1Cache.put(cacheKey, new CachedUiContext(cached.getUiContext()));
            return cached.getUiContext();
        }
        
        logger.debug("Cache miss for UI context: {}", cacheKey);
        statistics.misses.incrementAndGet();
        return null;
    }
    
    /**
     * 从缓存中获取UI上下文（使用默认键）
     */
    public UiContext get() {
        return get(DEFAULT_CACHE_KEY);
    }
    
    /**
     * 将UI上下文放入缓存
     * @param uiContext UI上下文对象
     * @param cacheKey 缓存键，默认为"default_ui_context"
     */
    public void put(UiContext uiContext, String cacheKey) {
        if (uiContext == null) {
            logger.warn("Cannot cache null UI context");
            return;
        }
        
        if (cacheKey == null) {
            cacheKey = DEFAULT_CACHE_KEY;
        }
        
        CachedUiContext cached = new CachedUiContext(uiContext);
        level1Cache.put(cacheKey, cached);
        level2Cache.put(cacheKey, cached);
        statistics.itemsAdded.incrementAndGet();
        logger.debug("UI context cached with key: {}", cacheKey);
    }
    
    /**
     * 将UI上下文放入缓存（使用默认键）
     */
    public void put(UiContext uiContext) {
        put(uiContext, DEFAULT_CACHE_KEY);
    }
    
    /**
     * 基于操作类型的智能缓存失效策略
     * @param operationType 操作类型
     * @param affectedElements 受影响的元素ID列表（可选）
     */
    public void invalidateBasedOnOperation(OperationType operationType, List<String> affectedElements) {
        switch (operationType) {
            case READ:
                // 只读操作通常不需要使缓存失效，但可以选择性地进行部分刷新
                if (logger.isDebugEnabled()) {
                    logger.debug("Read operation performed, cache remains valid");
                }
                break;
                
            case MODIFY:
                // 修改操作可能改变页面状态，采用智能部分失效策略
                if (affectedElements != null && !affectedElements.isEmpty()) {
                    // 如果有具体的受影响元素，只对这些元素相关的缓存进行处理
                    // 这里简化实现，实际应用中可以基于元素关联性进行更精细的失效控制
                    logger.debug("Modifying selective cache entries for {} elements", affectedElements.size());
                    // 对于修改操作，我们仍然完全失效缓存以确保一致性
                    // 在更高级的实现中，可以只更新与受影响元素相关的部分
                    invalidate();
                } else {
                    // 没有具体元素信息时，完全失效缓存
                    invalidate();
                }
                break;
                
            case NAVIGATE:
                // 导航操作肯定会改变整个页面，完全失效缓存
                logger.debug("Navigation operation performed, invalidating all UI context caches");
                clear(); // 导航操作清除所有缓存
                break;
                
            case UNKNOWN:
            default:
                // 未知操作类型，为安全起见完全失效缓存
                logger.debug("Unknown operation type, invalidating UI context cache");
                invalidate();
                break;
        }
        
        statistics.invalidations.incrementAndGet();
    }
    
    /**
     * 使特定键的缓存失效
     * @param cacheKey 缓存键
     */
    public void invalidate(String cacheKey) {
        if (cacheKey == null) {
            cacheKey = DEFAULT_CACHE_KEY;
        }
        
        level1Cache.remove(cacheKey);
        level2Cache.remove(cacheKey);
        logger.debug("UI context cache invalidated for key: {}", cacheKey);
        statistics.invalidations.incrementAndGet();
    }
    
    /**
     * 使默认键的缓存失效
     */
    public void invalidate() {
        invalidate(DEFAULT_CACHE_KEY);
    }
    
    /**
     * 清除所有缓存
     */
    public void clear() {
        level1Cache.clear();
        level2Cache.clear();
        logger.debug("All UI context caches cleared");
        statistics.totalClears.incrementAndGet();
    }
    
    /**
     * 获取一级缓存大小
     */
    public int getLevel1CacheSize() {
        return level1Cache.size();
    }
    
    /**
     * 获取二级缓存大小
     */
    public int getLevel2CacheSize() {
        return level2Cache.size();
    }
    
    /**
     * 获取缓存命中率
     * @return 命中率百分比
     */
    public double getHitRate() {
        long totalRequests = statistics.totalRequests.get();
        if (totalRequests == 0) {
            return 0.0;
        }
        long totalHits = statistics.level1Hits.get() + statistics.level2Hits.get();
        return (double) totalHits / totalRequests * 100;
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(statistics);
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        public final AtomicInteger totalRequests = new AtomicInteger(0);
        public final AtomicInteger level1Hits = new AtomicInteger(0);
        public final AtomicInteger level2Hits = new AtomicInteger(0);
        public final AtomicInteger misses = new AtomicInteger(0);
        public final AtomicInteger itemsAdded = new AtomicInteger(0);
        public final AtomicInteger invalidations = new AtomicInteger(0);
        public final AtomicInteger totalClears = new AtomicInteger(0);
        
        public CacheStatistics() {
        }
        
        public CacheStatistics(CacheStatistics other) {
            // 创建统计信息的副本
            this.totalRequests.set(other.totalRequests.get());
            this.level1Hits.set(other.level1Hits.get());
            this.level2Hits.set(other.level2Hits.get());
            this.misses.set(other.misses.get());
            this.itemsAdded.set(other.itemsAdded.get());
            this.invalidations.set(other.invalidations.get());
            this.totalClears.set(other.totalClears.get());
        }
        
        @Override
        public String toString() {
            long total = totalRequests.get();
            long hits = level1Hits.get() + level2Hits.get();
            double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;
            
            return String.format(
                "CacheStatistics{requests=%d, hits=%d (%.2f%%), misses=%d, level1Hits=%d, level2Hits=%d, " +
                "itemsAdded=%d, invalidations=%d, clears=%d}",
                total, hits, hitRate, misses.get(), level1Hits.get(), level2Hits.get(),
                itemsAdded.get(), invalidations.get(), totalClears.get()
            );
        }
    }
    
    /**
     * 缓存的UI上下文包装类
     */
    private static class CachedUiContext {
        private final UiContext uiContext;
        private final long timestamp;
        private final AtomicInteger accessCount;
        
        public CachedUiContext(UiContext uiContext) {
            this.uiContext = uiContext;
            this.timestamp = System.currentTimeMillis();
            this.accessCount = new AtomicInteger(0);
        }
        
        public UiContext getUiContext() {
            return uiContext;
        }
        
        public boolean isExpired(long expiryMs) {
            return System.currentTimeMillis() - timestamp > expiryMs;
        }
        
        public long getAge() {
            return System.currentTimeMillis() - timestamp;
        }
        
        public void incrementAccessCount() {
            accessCount.incrementAndGet();
        }
        
        public int getAccessCount() {
            return accessCount.get();
        }
    }
}