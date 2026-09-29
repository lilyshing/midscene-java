package com.midscene.core.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试AIModelCache类的基本功能，但不使用具体的模型类
 */
public class AIModelCacheTest {

    private AIModelCache cache;
    private static final long TEST_EXPIRY_MS = 100;
    private static final int TEST_MAX_SIZE = 5;

    @BeforeEach
    public void setUp() {
        cache = new AIModelCache(TEST_EXPIRY_MS, TEST_MAX_SIZE);
    }

    @Test
    public void testConstructor() {
        assertEquals(TEST_EXPIRY_MS, cache.getExpiryMs());
        assertEquals(TEST_MAX_SIZE, cache.getMaxEntries());
    }

    @Test
    public void testCacheBasicOperations() {
        // 测试缓存统计功能 - 我们不直接测试缓存内容，而是测试统计信息
        AIModelCache.CacheStatistics initialStats = cache.getStatistics();
        assertEquals(0, initialStats.totalRequests.get());
        assertEquals(0, initialStats.itemsAdded.get());
        assertEquals(0, initialStats.misses.get());
    }

    @Test
    public void testCacheSizeMethods() {
        // 测试初始缓存大小
        assertEquals(0, cache.getTotalCacheSize());
    }

    @Test
    public void testCacheClear() {
        // 测试清除缓存功能
        cache.clear();
        assertEquals(0, cache.getTotalCacheSize());
        
        // 检查清除统计
        AIModelCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(1, stats.totalClears.get());
    }

    @Test
    public void testInvalidateByType() {
        // 测试按类型使缓存失效
        cache.invalidateByType(AIModelCache.CacheType.ALL);
        assertEquals(0, cache.getTotalCacheSize());
        
        // 检查失效统计
        AIModelCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(1, stats.invalidations.get());
    }

    @Test
    public void testHitRateCalculation() {
        // 测试初始命中率
        assertEquals(0.0, cache.getHitRate(), 0.01);
    }

    @Test
    public void testCacheStatisticsCopy() {
        // 测试统计信息复制功能
        AIModelCache.CacheStatistics stats1 = cache.getStatistics();
        AIModelCache.CacheStatistics stats2 = cache.getStatistics();
        
        // 确认是不同的对象
        assertNotSame(stats1, stats2);
        
        // 确认内容相同
        assertEquals(stats1.totalRequests.get(), stats2.totalRequests.get());
        assertEquals(stats1.itemsAdded.get(), stats2.itemsAdded.get());
    }

    @Test
    public void testCacheTypeEnum() {
        // 测试缓存类型枚举
        assertNotNull(AIModelCache.CacheType.ACTION);
        assertNotNull(AIModelCache.CacheType.TAP);
        assertNotNull(AIModelCache.CacheType.INPUT);
        assertNotNull(AIModelCache.CacheType.EXTRACT);
        assertNotNull(AIModelCache.CacheType.ALL);
    }

    @Test
    public void testInvalidateByDifferentTypes() {
        // 测试各种缓存类型的失效
        cache.invalidateByType(AIModelCache.CacheType.ACTION);
        cache.invalidateByType(AIModelCache.CacheType.TAP);
        cache.invalidateByType(AIModelCache.CacheType.INPUT);
        cache.invalidateByType(AIModelCache.CacheType.EXTRACT);
        
        // 检查失效统计
        AIModelCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(4, stats.invalidations.get());
    }


}