package com.midscene.core.cache;

import com.midscene.shared.model.ai.AIActionRequest;
import com.midscene.shared.model.ai.AIActionResult;
import com.midscene.shared.model.ai.AIInputRequest;
import com.midscene.shared.model.ai.AIInputResult;
import com.midscene.shared.model.ai.AITapRequest;
import com.midscene.shared.model.ai.AITapResult;
import com.midscene.shared.model.ai.ExtractDataRequest;
import com.midscene.shared.model.ai.ExtractDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * AI模型结果缓存类
 * 提供AI模型调用结果的缓存功能，避免相同请求的重复计算
 */
public class AIModelCache {
    private static final Logger logger = LoggerFactory.getLogger(AIModelCache.class);
    
    // 不同类型的缓存
    private final Map<String, CachedModelResult<?>> actionCache;
    private final Map<String, CachedModelResult<?>> tapCache;
    private final Map<String, CachedModelResult<?>> inputCache;
    private final Map<String, CachedModelResult<?>> extractCache;
    
    // 缓存过期时间（毫秒）
    private final long cacheExpiryMs;
    
    // 最大缓存条目数
    private final int maxCacheSize;
    
    // 缓存统计信息
    private final CacheStatistics statistics;
    
    public AIModelCache() {
        this(30000, 1000); // 默认：30秒过期，最多1000条
    }
    
    public AIModelCache(long expiryMs, int maxSize) {
        this.actionCache = new ConcurrentHashMap<>();
        this.tapCache = new ConcurrentHashMap<>();
        this.inputCache = new ConcurrentHashMap<>();
        this.extractCache = new ConcurrentHashMap<>();
        this.cacheExpiryMs = expiryMs;
        this.maxCacheSize = maxSize;
        this.statistics = new CacheStatistics();
    }
    
    /**
     * 获取最大缓存条目数
     */
    public int getMaxEntries() {
        return maxCacheSize;
    }
    
    /**
     * 获取缓存过期时间（毫秒）
     */
    public long getExpiryMs() {
        return cacheExpiryMs;
    }
    
    /**
     * 为AI动作请求创建缓存键
     */
    private String createActionCacheKey(AIActionRequest request) {
        if (request == null) return null;
        return String.format("action:%s:%s:%d", 
                request.getTask(),
                request.getAgentType() != null ? request.getAgentType() : "default",
                request.hashCode());
    }
    
    /**
     * 为AI点击请求创建缓存键
     */
    private String createTapCacheKey(AITapRequest request) {
        if (request == null) return null;
        return String.format("tap:%s:%s:%d", 
                request.getTask(),
                request.getAgentType() != null ? request.getAgentType() : "default",
                request.hashCode());
    }
    
    /**
     * 为AI输入请求创建缓存键
     */
    private String createInputCacheKey(AIInputRequest request) {
        if (request == null) return null;
        return String.format("input:%s:%s:%s:%d", 
                request.getTask(),
                request.getInputText(),
                request.getAgentType() != null ? request.getAgentType() : "default",
                request.hashCode());
    }
    
    /**
     * 为数据提取请求创建缓存键
     */
    private String createExtractCacheKey(ExtractDataRequest request) {
        if (request == null) return null;
        return String.format("extract:%s:%s:%d", 
                request.getTask(),
                request.getAgentType() != null ? request.getAgentType() : "default",
                request.hashCode());
    }
    
    /**
     * 从缓存获取AI动作结果
     */
    @SuppressWarnings("unchecked")
    public AIActionResult getActionResult(AIActionRequest request) {
        if (request == null) return null;
        
        statistics.totalRequests.incrementAndGet();
        String key = createActionCacheKey(request);
        
        CachedModelResult<?> cached = actionCache.get(key);
        if (cached != null && !cached.isExpired(cacheExpiryMs)) {
            logger.debug("AI action cache hit for key: {}", key);
            statistics.actionHits.incrementAndGet();
            return (AIActionResult) cached.getResult();
        }
        
        statistics.misses.incrementAndGet();
        return null;
    }
    
    /**
     * 存储AI动作结果到缓存
     */
    public void putActionResult(AIActionRequest request, AIActionResult result) {
        if (request == null || result == null) return;
        
        checkCacheSize(actionCache);
        String key = createActionCacheKey(request);
        actionCache.put(key, new CachedModelResult<>(result));
        statistics.itemsAdded.incrementAndGet();
        logger.debug("AI action result cached with key: {}", key);
    }
    
    /**
     * 从缓存获取AI点击结果
     */
    @SuppressWarnings("unchecked")
    public AITapResult getTapResult(AITapRequest request) {
        if (request == null) return null;
        
        statistics.totalRequests.incrementAndGet();
        String key = createTapCacheKey(request);
        
        CachedModelResult<?> cached = tapCache.get(key);
        if (cached != null && !cached.isExpired(cacheExpiryMs)) {
            logger.debug("AI tap cache hit for key: {}", key);
            statistics.tapHits.incrementAndGet();
            return (AITapResult) cached.getResult();
        }
        
        statistics.misses.incrementAndGet();
        return null;
    }
    
    /**
     * 存储AI点击结果到缓存
     */
    public void putTapResult(AITapRequest request, AITapResult result) {
        if (request == null || result == null) return;
        
        checkCacheSize(tapCache);
        String key = createTapCacheKey(request);
        tapCache.put(key, new CachedModelResult<>(result));
        statistics.itemsAdded.incrementAndGet();
        logger.debug("AI tap result cached with key: {}", key);
    }
    
    /**
     * 从缓存获取AI输入结果
     */
    @SuppressWarnings("unchecked")
    public AIInputResult getInputResult(AIInputRequest request) {
        if (request == null) return null;
        
        statistics.totalRequests.incrementAndGet();
        String key = createInputCacheKey(request);
        
        CachedModelResult<?> cached = inputCache.get(key);
        if (cached != null && !cached.isExpired(cacheExpiryMs)) {
            logger.debug("AI input cache hit for key: {}", key);
            statistics.inputHits.incrementAndGet();
            return (AIInputResult) cached.getResult();
        }
        
        statistics.misses.incrementAndGet();
        return null;
    }
    
    /**
     * 存储AI输入结果到缓存
     */
    public void putInputResult(AIInputRequest request, AIInputResult result) {
        if (request == null || result == null) return;
        
        checkCacheSize(inputCache);
        String key = createInputCacheKey(request);
        inputCache.put(key, new CachedModelResult<>(result));
        statistics.itemsAdded.incrementAndGet();
        logger.debug("AI input result cached with key: {}", key);
    }
    
    /**
     * 从缓存获取数据提取结果
     */
    @SuppressWarnings("unchecked")
    public ExtractDataResult getExtractResult(ExtractDataRequest request) {
        if (request == null) return null;
        
        statistics.totalRequests.incrementAndGet();
        String key = createExtractCacheKey(request);
        
        CachedModelResult<?> cached = extractCache.get(key);
        if (cached != null && !cached.isExpired(cacheExpiryMs)) {
            logger.debug("Extract data cache hit for key: {}", key);
            statistics.extractHits.incrementAndGet();
            return (ExtractDataResult) cached.getResult();
        }
        
        statistics.misses.incrementAndGet();
        return null;
    }
    
    /**
     * 存储数据提取结果到缓存
     */
    public void putExtractResult(ExtractDataRequest request, ExtractDataResult result) {
        if (request == null || result == null) return;
        
        checkCacheSize(extractCache);
        String key = createExtractCacheKey(request);
        extractCache.put(key, new CachedModelResult<>(result));
        statistics.itemsAdded.incrementAndGet();
        logger.debug("Extract data result cached with key: {}", key);
    }
    
    /**
     * 缓存大小检查，超过限制时清理过期条目
     */
    private void checkCacheSize(Map<String, CachedModelResult<?>> cache) {
        if (cache.size() >= maxCacheSize) {
            // 清理过期条目
            long now = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> 
                now - entry.getValue().getTimestamp() > cacheExpiryMs);
            
            // 如果清理后仍然超过限制，删除最旧的条目
            if (cache.size() >= maxCacheSize) {
                logger.warn("Cache size exceeded, removing oldest entries");
                statistics.evictions.incrementAndGet();
                
                // 找出最旧的条目并删除
                String oldestKey = null;
                long oldestTime = Long.MAX_VALUE;
                
                for (Map.Entry<String, CachedModelResult<?>> entry : cache.entrySet()) {
                    if (entry.getValue().getTimestamp() < oldestTime) {
                        oldestTime = entry.getValue().getTimestamp();
                        oldestKey = entry.getKey();
                    }
                }
                
                if (oldestKey != null) {
                    cache.remove(oldestKey);
                }
            }
        }
    }
    
    /**
     * 使特定类型的缓存失效
     */
    public void invalidateByType(CacheType type) {
        switch (type) {
            case ACTION:
                actionCache.clear();
                break;
            case TAP:
                tapCache.clear();
                break;
            case INPUT:
                inputCache.clear();
                break;
            case EXTRACT:
                extractCache.clear();
                break;
            case ALL:
                clear();
                break;
        }
        statistics.invalidations.incrementAndGet();
        logger.debug("Invalidated {} cache", type);
    }
    
    /**
     * 清理所有缓存
     */
    public void clear() {
        actionCache.clear();
        tapCache.clear();
        inputCache.clear();
        extractCache.clear();
        statistics.totalClears.incrementAndGet();
        logger.debug("All AI model caches cleared");
    }
    
    /**
     * 获取总缓存大小
     */
    public int getTotalCacheSize() {
        return actionCache.size() + tapCache.size() + inputCache.size() + extractCache.size();
    }
    
    /**
     * 获取缓存命中率
     */
    public double getHitRate() {
        long totalRequests = statistics.totalRequests.get();
        if (totalRequests == 0) {
            return 0.0;
        }
        
        long totalHits = statistics.actionHits.get() + statistics.tapHits.get() + 
                        statistics.inputHits.get() + statistics.extractHits.get();
        
        return (double) totalHits / totalRequests * 100;
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(statistics);
    }
    
    /**
     * 缓存类型枚举
     */
    public enum CacheType {
        ACTION,
        TAP,
        INPUT,
        EXTRACT,
        ALL
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        public final AtomicInteger totalRequests = new AtomicInteger(0);
        public final AtomicInteger actionHits = new AtomicInteger(0);
        public final AtomicInteger tapHits = new AtomicInteger(0);
        public final AtomicInteger inputHits = new AtomicInteger(0);
        public final AtomicInteger extractHits = new AtomicInteger(0);
        public final AtomicInteger misses = new AtomicInteger(0);
        public final AtomicInteger itemsAdded = new AtomicInteger(0);
        public final AtomicInteger invalidations = new AtomicInteger(0);
        public final AtomicInteger evictions = new AtomicInteger(0);
        public final AtomicInteger totalClears = new AtomicInteger(0);
        
        public CacheStatistics() {
        }
        
        public CacheStatistics(CacheStatistics other) {
            // 创建统计信息的副本
            this.totalRequests.set(other.totalRequests.get());
            this.actionHits.set(other.actionHits.get());
            this.tapHits.set(other.tapHits.get());
            this.inputHits.set(other.inputHits.get());
            this.extractHits.set(other.extractHits.get());
            this.misses.set(other.misses.get());
            this.itemsAdded.set(other.itemsAdded.get());
            this.invalidations.set(other.invalidations.get());
            this.evictions.set(other.evictions.get());
            this.totalClears.set(other.totalClears.get());
        }
        
        @Override
        public String toString() {
            long total = totalRequests.get();
            long hits = actionHits.get() + tapHits.get() + inputHits.get() + extractHits.get();
            double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;
            
            return String.format(
                "AIModelCacheStatistics{requests=%d, hits=%d (%.2f%%), misses=%d, " +
                "actionHits=%d, tapHits=%d, inputHits=%d, extractHits=%d, " +
                "itemsAdded=%d, invalidations=%d, evictions=%d, clears=%d}",
                total, hits, hitRate, misses.get(), 
                actionHits.get(), tapHits.get(), inputHits.get(), extractHits.get(),
                itemsAdded.get(), invalidations.get(), evictions.get(), totalClears.get()
            );
        }
    }
    
    /**
     * 缓存的模型结果包装类
     */
    private static class CachedModelResult<T> {
        private final T result;
        private final long timestamp;
        
        public CachedModelResult(T result) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getResult() {
            return result;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired(long expiryMs) {
            return System.currentTimeMillis() - timestamp > expiryMs;
        }
    }
}