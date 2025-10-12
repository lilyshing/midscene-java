package com.midscene.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheUtil类的单元测试
 */
class CacheUtilTest {

    @BeforeEach
    void setUp() {
        // 每个测试前清空缓存，确保测试隔离
        CacheUtil.clear();
    }

    @Test
    void testPutAndGet() {
        // 测试基本的缓存放入和获取功能
        String key = "testKey";
        String value = "testValue";
        
        CacheUtil.put(key, value);
        String result = CacheUtil.get(key);
        
        assertEquals(value, result);
    }

    @Test
    void testGetNonExistentKey() {
        // 测试获取不存在的键
        String result = CacheUtil.get("nonExistentKey");
        assertNull(result);
    }

    @Test
    void testNullKeyOrValue() {
        // 测试空键或空值
        CacheUtil.put(null, "value");
        CacheUtil.put("key", null);
        
        assertNull(CacheUtil.get(null));
        assertEquals(0, CacheUtil.size());
    }

    @Test
    void testRemove() {
        // 测试移除缓存
        String key = "testKey";
        String value = "testValue";
        
        CacheUtil.put(key, value);
        assertNotNull(CacheUtil.get(key));
        
        CacheUtil.remove(key);
        assertNull(CacheUtil.get(key));
    }

    @Test
    void testClear() {
        // 测试清空所有缓存
        CacheUtil.put("key1", "value1");
        CacheUtil.put("key2", "value2");
        assertEquals(2, CacheUtil.size());
        
        CacheUtil.clear();
        assertEquals(0, CacheUtil.size());
    }

    @Test
    void testSize() {
        // 测试缓存大小
        assertEquals(0, CacheUtil.size());
        
        CacheUtil.put("key1", "value1");
        CacheUtil.put("key2", "value2");
        CacheUtil.put("key3", "value3");
        
        assertEquals(3, CacheUtil.size());
    }

    @Test
    void testExpiration() throws InterruptedException {
        // 测试缓存过期
        String key = "testKey";
        String value = "testValue";
        
        // 设置100毫秒后过期
        CacheUtil.put(key, value, 100);
        assertNotNull(CacheUtil.get(key));
        
        // 等待过期
        TimeUnit.MILLISECONDS.sleep(150);
        
        // 获取已过期的缓存，应该返回null
        assertNull(CacheUtil.get(key));
        // 此时缓存大小应为0，因为过期项已被自动清理
        assertEquals(0, CacheUtil.size());
    }

    @Test
    void testCleanupExpiredEntries() throws InterruptedException {
        // 测试清理过期条目
        CacheUtil.put("key1", "value1", 100);  // 快速过期
        CacheUtil.put("key2", "value2", 10000);  // 长时间不过期
        
        // 等待第一个过期
        TimeUnit.MILLISECONDS.sleep(150);
        
        // 手动清理
        CacheUtil.cleanupExpiredEntries();
        
        // 验证只有过期的被清理
        assertNull(CacheUtil.get("key1"));
        assertEquals("value2", CacheUtil.get("key2"));
        assertEquals(1, CacheUtil.size());
    }

    @Test
    void testGenerateCacheKeyForAI() {
        // 测试AI缓存键生成
        String prompt = "analyze this UI";  
        String modelName = "gpt-4";
        
        String key1 = CacheUtil.generateCacheKeyForAI(prompt, modelName);
        String key2 = CacheUtil.generateCacheKeyForAI(prompt, modelName);
        String key3 = CacheUtil.generateCacheKeyForAI("different prompt", modelName);
        
        // 相同输入应生成相同的键
        assertEquals(key1, key2);
        // 不同输入应生成不同的键
        assertNotEquals(key1, key3);
        // 键应包含模型名称前缀
        assertTrue(key1.startsWith("ai:" + modelName + ":"));
    }

    @Test
    void testGenerateCacheKeyForUI() {
        // 测试UI缓存键生成
        String pageUrl = "https://example.com";
        
        String key1 = CacheUtil.generateCacheKeyForUI(pageUrl);
        String key2 = CacheUtil.generateCacheKeyForUI(pageUrl);
        String key3 = CacheUtil.generateCacheKeyForUI("https://different.com");
        
        // 相同URL应生成相同的键
        assertEquals(key1, key2);
        // 不同URL应生成不同的键
        assertNotEquals(key1, key3);
        // 键应包含UI前缀
        assertTrue(key1.startsWith("ui:"));
    }
}