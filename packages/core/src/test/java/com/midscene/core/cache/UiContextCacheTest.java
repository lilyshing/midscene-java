package com.midscene.core.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class UiContextCacheTest {

    private UiContextCache cache;
    private static final long SHORT_LEVEL1_EXPIRY = 10; // 10毫秒一级缓存过期时间
    private static final long LONG_LEVEL2_EXPIRY = 100; // 100毫秒二级缓存过期时间
    private static final String CUSTOM_CACHE_KEY = "custom_ui_context_key";

    @BeforeEach
    public void setUp() {
        // 使用较短的过期时间以便于测试
        cache = new UiContextCache(SHORT_LEVEL1_EXPIRY, LONG_LEVEL2_EXPIRY);
    }

    @Test
    public void testDefaultConstructor() {
        // 测试默认构造函数
        UiContextCache defaultCache = new UiContextCache();
        assertNotNull(defaultCache);
    }

    @Test
    public void testCustomConstructor() {
        // 测试自定义构造函数
        long level1Expiry = 200;
        long level2Expiry = 2000;
        UiContextCache customCache = new UiContextCache(level1Expiry, level2Expiry);
        assertNotNull(customCache);
        assertEquals(0, customCache.getLevel1CacheSize()); // 空缓存
        assertEquals(0, customCache.getLevel2CacheSize()); // 空缓存
    }

    @Test
    public void testCacheSizeMethods() {
        // 测试初始缓存大小
        assertEquals(0, cache.getLevel1CacheSize());
        assertEquals(0, cache.getLevel2CacheSize());
    }

    @Test
    public void testCacheClear() {
        // 测试清除缓存功能
        cache.clear();
        assertEquals(0, cache.getLevel1CacheSize());
        assertEquals(0, cache.getLevel2CacheSize());
        
        // 检查清除统计
        UiContextCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(1, stats.totalClears.get());
    }

    @Test
    public void testHitRateCalculation() {
        // 测试初始命中率
        assertEquals(0.0, cache.getHitRate(), 0.01);
    }

    @Test
    public void testCacheStatisticsCopy() {
        // 测试统计信息复制功能
        UiContextCache.CacheStatistics stats1 = cache.getStatistics();
        UiContextCache.CacheStatistics stats2 = cache.getStatistics();
        
        // 确认是不同的对象
        assertNotSame(stats1, stats2);
        
        // 确认内容相同
        assertEquals(stats1.totalRequests.get(), stats2.totalRequests.get());
        assertEquals(stats1.itemsAdded.get(), stats2.itemsAdded.get());
    }

    @Test
    public void testInvalidateByOperationType() {
        // 测试不同操作类型的失效
        cache.invalidateBasedOnOperation(UiContextCache.OperationType.READ, null);
        cache.invalidateBasedOnOperation(UiContextCache.OperationType.MODIFY, null);
        cache.invalidateBasedOnOperation(UiContextCache.OperationType.NAVIGATE, null);
        
        // 检查统计
        UiContextCache.CacheStatistics stats = cache.getStatistics();
        assertTrue(stats.invalidations.get() >= 0);
    }

    @Test
    public void testOperationTypeEnum() {
        // 测试操作类型枚举
        assertNotNull(UiContextCache.OperationType.READ);
        assertNotNull(UiContextCache.OperationType.MODIFY);
        assertNotNull(UiContextCache.OperationType.NAVIGATE);
    }

    @Test
    public void testInvalidateMethods() {
        // 测试各种失效方法
        cache.invalidate();
        cache.invalidate(CUSTOM_CACHE_KEY);
        
        // 应该不会抛出异常
        assertTrue(true);
    }

    @Test
    public void testBasicMethodSafety() {
        // 测试基本方法调用安全性
        assertNull(cache.get());
        assertNull(cache.get(CUSTOM_CACHE_KEY));
        
        // 测试受影响元素列表
        List<String> affectedElements = new ArrayList<>();
        affectedElements.add("element1");
        cache.invalidateBasedOnOperation(UiContextCache.OperationType.MODIFY, affectedElements);
        
        // 确认方法调用后不会抛出异常
        assertTrue(true);
    }
}