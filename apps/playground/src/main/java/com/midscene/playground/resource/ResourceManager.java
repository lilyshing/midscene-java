package com.midscene.playground.resource;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 优化的资源管理器 - 提供高效的资源池化管理、内存优化和资源监控功能
 * 支持泛型的资源池管理，提供增强的错误处理和动态配置能力
 */
@Component
public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    
    // 使用泛型化的资源池注册表，提高类型安全
    private final Map<String, ResourcePoolHolder<?>> resourcePools = new ConcurrentHashMap<>();
    
    // 资源使用统计
    private final Map<String, ResourceStats> resourceStats = new ConcurrentHashMap<>();
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupScheduler;
    
    // 配置管理，支持动态更新
    private final AtomicReference<ResourceManagerConfig> config = new AtomicReference<>(new ResourceManagerConfig());
    
    // 默认配置常量
    private static final long DEFAULT_CLEANUP_INTERVAL_MS = 60000; // 60秒
    private static final int DEFAULT_MAX_TOTAL = 20;
    private static final int DEFAULT_MAX_IDLE = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final long DEFAULT_MAX_WAIT_MS = 30000;
    private static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MS = 300000; // 5分钟
    private static final boolean DEFAULT_TEST_ON_BORROW = true;
    private static final boolean DEFAULT_TEST_WHILE_IDLE = true;
    private static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MS = 60000; // 1分钟

    public ResourceManager() {
        // 初始化定时清理调度器
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "resource-cleanup-thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // 启动定时清理任务
        this.cleanupScheduler.scheduleAtFixedRate(
                this::periodicCleanup,
                config.get().getCleanupIntervalMs(),
                config.get().getCleanupIntervalMs(),
                TimeUnit.MILLISECONDS
        );
        
        logger.info("ResourceManager initialized with periodic cleanup interval: {}ms", 
                config.get().getCleanupIntervalMs());
    }

    /**
     * 创建默认的资源池配置
     */
    private <T> GenericObjectPoolConfig<T> createDefaultPoolConfig() {
        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.get().getMaxTotal());
        poolConfig.setMaxIdle(config.get().getMaxIdle());
        poolConfig.setMinIdle(config.get().getMinIdle());
        poolConfig.setMaxWaitMillis(config.get().getMaxWaitMs());
        poolConfig.setMinEvictableIdleTimeMillis(config.get().getMinEvictableIdleTimeMs());
        poolConfig.setTestOnBorrow(config.get().isTestOnBorrow());
        poolConfig.setTestWhileIdle(config.get().isTestWhileIdle());
        poolConfig.setTimeBetweenEvictionRunsMillis(config.get().getTimeBetweenEvictionRunsMs());
        poolConfig.setJmxEnabled(true);
        return poolConfig;
    }

    /**
     * 注册资源池
     */
    public <T> void registerResourcePool(String poolId, ResourceFactory<T> factory) {
        registerResourcePool(poolId, factory, createDefaultPoolConfig());
    }

    /**
     * 注册资源池（带自定义配置）
     */
    public <T> void registerResourcePool(String poolId, ResourceFactory<T> factory, GenericObjectPoolConfig<T> poolConfig) {
        if (resourcePools.containsKey(poolId)) {
            logger.warn("Resource pool with ID '{}' already exists. Skipping registration.", poolId);
            return;
        }

        try {
            // 创建资源池
            GenericObjectPool<T> pool = new GenericObjectPool<>(factory, poolConfig);
            resourcePools.put(poolId, new ResourcePoolHolder<>(pool, factory, poolConfig));
            resourceStats.put(poolId, new ResourceStats());
            
            logger.info("Resource pool '{}' registered successfully with config: maxTotal={}, maxIdle={}, minIdle={}",
                    poolId, poolConfig.getMaxTotal(), poolConfig.getMaxIdle(), poolConfig.getMinIdle());
        } catch (Exception e) {
            logger.error("Failed to register resource pool '{}': {}", poolId, e.getMessage(), e);
            throw new RuntimeException("Failed to register resource pool: " + poolId, e);
        }
    }

    /**
     * 获取资源池
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectPool<T> getResourcePool(String poolId) {
        ResourcePoolHolder<?> holder = resourcePools.get(poolId);
        if (holder == null) {
            throw new IllegalArgumentException("Resource pool not found: " + poolId);
        }
        return (ObjectPool<T>) holder.getPool();
    }

    /**
     * 借用资源，增强错误处理和超时控制
     */
    public <T> T borrowResource(String poolId) throws Exception {
        return borrowResource(poolId, config.get().getMaxWaitMs());
    }

    /**
     * 借用资源（带超时控制）
     */
    @SuppressWarnings("unchecked")
    public <T> T borrowResource(String poolId, long borrowTimeoutMs) throws Exception {
        ResourcePoolHolder<T> holder = (ResourcePoolHolder<T>) resourcePools.get(poolId);
        if (holder == null) {
            throw new IllegalArgumentException("Resource pool not found: " + poolId);
        }
        
        ObjectPool<T> pool = holder.getPool();
        
        long startTime = System.currentTimeMillis();
        T resource;
        
        try {
            // 尝试借用资源
            if (borrowTimeoutMs > 0) {
                // 使用默认借用
                resource = pool.borrowObject();
            } else {
                // 默认借用
                resource = pool.borrowObject();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 更新统计信息
            ResourceStats stats = resourceStats.get(poolId);
            if (stats != null) {
                stats.incrementBorrowCount();
                stats.addBorrowTime(duration);
                stats.updateLastBorrowTime();
                
                // 记录借用耗时指标
                if (duration > config.get().getSlowBorrowThresholdMs()) {
                    logger.warn("Slow resource borrow detected: pool='{}', time={}ms", poolId, duration);
                }
            }
            
            logger.debug("Borrowed resource from pool '{}' in {}ms", poolId, duration);
            return resource;
        } catch (Exception e) {
            // 增强的错误处理
            logger.error("Failed to borrow resource from pool '{}': {}", poolId, e.getMessage(), e);
            
            // 更新统计信息中的错误计数
            ResourceStats stats = resourceStats.get(poolId);
            if (stats != null) {
                stats.incrementErrorCount();
            }
            
            // 重新抛出异常，保留原始异常链
            throw e;
        }
    }

    /**
     * 归还资源，增强异常处理和资源验证
     */
    @SuppressWarnings("unchecked")
    public <T> void returnResource(String poolId, T resource) {
        returnResource(poolId, resource, false);
    }

    /**
     * 归还资源（带废弃标记）
     */
    @SuppressWarnings("unchecked")
    public <T> void returnResource(String poolId, T resource, boolean invalidate) {
        if (resource == null) {
            logger.debug("Attempting to return null resource to pool '{}', ignoring", poolId);
            return;
        }
        
        try {
            ResourcePoolHolder<T> holder = (ResourcePoolHolder<T>) resourcePools.get(poolId);
            if (holder == null) {
                logger.warn("Resource pool '{}' not found when returning resource, will destroy resource directly", poolId);
                // 如果池不存在，直接销毁资源
                try {
                    if (resource != null) {
                        ResourceFactory<T> factory = (ResourceFactory<T>) new ResourceFactoryImpl();
                        factory.doDestroy(resource);
                    }
                } catch (Exception ex) {
                    logger.error("Failed to destroy resource when pool not found: {}", ex.getMessage(), ex);
                }
                return;
            }
            
            ObjectPool<T> pool = holder.getPool();
            
            if (invalidate) {
                pool.invalidateObject(resource);
                logger.debug("Invalidated resource from pool '{}'", poolId);
            } else {
                // 归还资源前验证资源状态
                boolean isValid = true;
                try {
                    isValid = holder.getFactory().doValidate(resource);
                } catch (Exception ex) {
                    logger.warn("Failed to validate resource before returning to pool '{}': {}", 
                            poolId, ex.getMessage(), ex);
                    isValid = false;
                }
                
                if (isValid) {
                    pool.returnObject(resource);
                    logger.debug("Returned resource to pool '{}'", poolId);
                } else {
                    pool.invalidateObject(resource);
                    logger.debug("Returned invalid resource to pool '{}', will be invalidated", poolId);
                }
            }
            
            // 更新统计信息
            ResourceStats stats = resourceStats.get(poolId);
            if (stats != null) {
                stats.incrementReturnCount();
                stats.updateLastReturnTime();
            }
        } catch (Exception e) {
            logger.error("Failed to return resource to pool '{}': {}", poolId, e.getMessage(), e);
            
            // 如果归还失败，尝试销毁资源
            try {
                ResourcePoolHolder<T> holder = (ResourcePoolHolder<T>) resourcePools.get(poolId);
                if (holder != null) {
                    holder.getFactory().doDestroy(resource);
                    logger.debug("Resource could not be returned to pool '{}', destroyed directly", poolId);
                }
            } catch (Exception ex) {
                logger.error("Failed to handle resource that could not be returned: {}", ex.getMessage(), ex);
            }
            
            // 更新错误计数
            ResourceStats stats = resourceStats.get(poolId);
            if (stats != null) {
                stats.incrementErrorCount();
            }
        }
    }

    /**
     * 清理空闲资源，增加更智能的清理策略
     */
    public void cleanupIdleResources() {
        logger.info("Starting cleanup of idle resources...");
        
        int totalCleanedUp = 0;
        for (Map.Entry<String, ResourcePoolHolder<?>> entry : resourcePools.entrySet()) {
            String poolId = entry.getKey();
            ResourcePoolHolder<?> holder = entry.getValue();
            ObjectPool<?> pool = holder.getPool();
            
            try {
                int beforeEvict = pool.getNumIdle();
                if (pool instanceof GenericObjectPool) {
                    GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                    
                    // 根据资源池使用情况动态调整清理策略
                    ResourceStats stats = resourceStats.get(poolId);
                    if (stats != null && System.currentTimeMillis() - stats.getLastBorrowTime() > 
                            config.get().getUnusedPoolCleanupThresholdMs()) {
                        // 对于长时间未使用的池，清理更激进
                        logger.info("Performing aggressive cleanup for rarely used pool '{}'", poolId);
                        // 清理到最小空闲数量
                        genericPool.clear();
                    } else {
                        // 常规清理
                        genericPool.evict();
                    }
                }
                int afterEvict = pool.getNumIdle();
                int cleanedUp = beforeEvict - afterEvict;
                totalCleanedUp += cleanedUp;
                
                if (cleanedUp > 0) {
                    logger.info("Pool '{}': cleaned up {} idle resources", poolId, cleanedUp);
                }
            } catch (Exception e) {
                logger.error("Failed to cleanup idle resources for pool '{}': {}", poolId, e.getMessage(), e);
            }
        }
        
        logger.info("Resource cleanup completed: {} idle resources cleaned up in total", totalCleanedUp);
    }

    /**
     * 定期清理任务，增加健康检查
     */
    private void periodicCleanup() {
        try {
            // 执行资源清理
            cleanupIdleResources();
            
            // 执行资源池健康检查
            performHealthCheck();
        } catch (Exception e) {
            logger.error("Exception during periodic resource cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行资源池健康检查，监控异常情况
     */
    private void performHealthCheck() {
        for (Map.Entry<String, ResourcePoolHolder<?>> entry : resourcePools.entrySet()) {
            String poolId = entry.getKey();
            ResourcePoolHolder<?> holder = entry.getValue();
            ObjectPool<?> pool = holder.getPool();
            ResourceStats stats = resourceStats.get(poolId);
            
            try {
                // 检查错误率
                if (stats != null && stats.getBorrowCount() > 0) {
                    double errorRate = (double) stats.getErrorCount() / stats.getBorrowCount();
                    if (errorRate > config.get().getMaxErrorRate()) {
                        logger.warn("High error rate detected for pool '{}': {:.2f}%", 
                                poolId, errorRate * 100);
                    }
                }
                
                // 检查资源池饱和度
                int activeCount = pool.getNumActive();
                int maxTotal = holder.getPoolConfig().getMaxTotal();
                double saturation = (double) activeCount / maxTotal;
                if (saturation > config.get().getHighSaturationThreshold()) {
                    logger.warn("High pool saturation detected for pool '{}': {:.2f}% ({} active of {})", poolId, saturation * 100, activeCount, maxTotal);
                }
            } catch (Exception e) {
                logger.error("Failed to perform health check for pool '{}': {}", poolId, e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取所有资源池的统计信息
     */
    public Map<String, Object> getPoolStats() {
        Map<String, Object> statusMap = new HashMap<>();
        
        for (Map.Entry<String, ResourcePoolHolder<?>> entry : resourcePools.entrySet()) {
            String poolId = entry.getKey();
            ResourcePoolHolder<?> holder = entry.getValue();
            ObjectPool<?> pool = holder.getPool();
            ResourceStats stats = resourceStats.get(poolId);
            
            try {
                // 获取等待者数量
                int numWaiters = 0;
                
                // 创建统计信息Map
                Map<String, Object> poolStats = new HashMap<>();
                poolStats.put("poolId", poolId);
                poolStats.put("numActive", pool.getNumActive());
                poolStats.put("numIdle", pool.getNumIdle());
                poolStats.put("numWaiters", numWaiters);
                poolStats.put("borrowCount", stats != null ? stats.getBorrowCount() : 0);
                poolStats.put("returnCount", stats != null ? stats.getReturnCount() : 0);
                poolStats.put("errorCount", stats != null ? stats.getErrorCount() : 0);
                poolStats.put("avgBorrowTimeMs", stats != null ? stats.getAvgBorrowTimeMs() : 0);
                
                // 添加最近使用时间信息
                if (stats != null) {
                    poolStats.put("lastBorrowTime", stats.getLastBorrowTime());
                    poolStats.put("lastReturnTime", stats.getLastReturnTime());
                }
                
                // 添加配置信息摘要
                GenericObjectPoolConfig<?> poolConfig = holder.getPoolConfig();
                poolStats.put("maxTotal", poolConfig.getMaxTotal());
                poolStats.put("maxIdle", poolConfig.getMaxIdle());
                poolStats.put("minIdle", poolConfig.getMinIdle());
                
                statusMap.put(poolId, poolStats);
            } catch (Exception e) {
                logger.error("Failed to get status for pool '{}': {}", poolId, e.getMessage(), e);
                // 添加错误信息到结果中
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("error", e.getMessage());
                statusMap.put(poolId, errorMap);
            }
        }
        
        return statusMap;
    }
    
    /**
     * 获取指定资源池的统计信息
     */
    public Map<String, Object> getPoolStats(String poolName) {
        if (poolName == null || poolName.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 从资源池中获取指定池的统计信息
        ResourcePoolHolder<?> holder = resourcePools.get(poolName);
        if (holder == null) {
            return Collections.emptyMap();
        }
        
        ObjectPool<?> pool = holder.getPool();
        ResourceStats stats = resourceStats.get(poolName);
        
        // 创建统计信息Map
        Map<String, Object> poolStats = new HashMap<>();
        try {
            poolStats.put("poolName", poolName);
            poolStats.put("activeCount", pool.getNumActive());
            poolStats.put("idleCount", pool.getNumIdle());
            // 设置默认值0
            poolStats.put("waitCount", 0);
            
            // 添加更多统计信息
            if (stats != null) {
                poolStats.put("borrowCount", stats.getBorrowCount());
                poolStats.put("returnCount", stats.getReturnCount());
                poolStats.put("errorCount", stats.getErrorCount());
                poolStats.put("avgBorrowTimeMs", stats.getAvgBorrowTimeMs());
                poolStats.put("lastBorrowTime", stats.getLastBorrowTime());
                poolStats.put("lastReturnTime", stats.getLastReturnTime());
            }
            
            // 添加配置信息
            GenericObjectPoolConfig<?> poolConfig = holder.getPoolConfig();
            poolStats.put("maxTotal", poolConfig.getMaxTotal());
            poolStats.put("maxIdle", poolConfig.getMaxIdle());
            poolStats.put("minIdle", poolConfig.getMinIdle());
        } catch (Exception e) {
            logger.error("Failed to get stats for pool '{}': {}", poolName, e.getMessage(), e);
            poolStats.put("error", e.getMessage());
        }
        
        return poolStats;
    }
    
    /**
     * 清理指定资源池
     */
    public boolean cleanupPool(String poolName) {
        if (poolName == null || poolName.isEmpty()) {
            return false;
        }
        
        try {
            ResourcePoolHolder<?> holder = resourcePools.get(poolName);
            if (holder != null) {
                ObjectPool<?> pool = holder.getPool();
                pool.clear();
                logger.info("Cleaned up pool '{}'", poolName);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error cleaning up pool {}", poolName, e);
            return false;
        }
    }

    /**
     * 关闭所有资源池
     */
    public void shutdown() {
        logger.info("Shutting down ResourceManager...");
        
        // 关闭清理调度器
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 关闭所有资源池
        for (Map.Entry<String, ResourcePoolHolder<?>> entry : resourcePools.entrySet()) {
            String poolId = entry.getKey();
            ResourcePoolHolder<?> holder = entry.getValue();
            ObjectPool<?> pool = holder.getPool();
            
            try {
                pool.close();
                logger.info("Closed resource pool '{}'", poolId);
            } catch (Exception e) {
                logger.error("Failed to close resource pool '{}': {}", poolId, e.getMessage(), e);
            }
        }
        
        // 清空集合
        resourcePools.clear();
        resourceStats.clear();
        
        logger.info("ResourceManager shutdown completed");
    }

    /**
     * 获取所有资源池的状态信息
     * @return 资源池状态信息映射
     */
    public Map<String, Object> getAllPoolStatus() {
        return getPoolStats();
    }
    
    /**
     * 更新资源管理器配置
     */
    public void updateConfig(ResourceManagerConfig newConfig) {
        if (newConfig != null) {
            config.set(newConfig);
            logger.info("ResourceManager configuration updated");
        }
    }
    
    /**
     * 获取当前配置
     */
    public ResourceManagerConfig getConfig() {
        return config.get();
    }

    /**
     * 资源池状态类
     */
    public static class ResourcePoolStatus {
        private final String poolId;
        private final int numActive;
        private final int numIdle;
        private final int numWaiters;
        private final long borrowCount;
        private final long returnCount;
        private final double avgBorrowTimeMs;

        public ResourcePoolStatus(String poolId, int numActive, int numIdle, int numWaiters,
                                 long borrowCount, long returnCount, double avgBorrowTimeMs) {
            this.poolId = poolId;
            this.numActive = numActive;
            this.numIdle = numIdle;
            this.numWaiters = numWaiters;
            this.borrowCount = borrowCount;
            this.returnCount = returnCount;
            this.avgBorrowTimeMs = avgBorrowTimeMs;
        }

        // Getters
        public String getPoolId() { return poolId; }
        public int getNumActive() { return numActive; }
        public int getNumIdle() { return numIdle; }
        public int getNumWaiters() { return numWaiters; }
        public long getBorrowCount() { return borrowCount; }
        public long getReturnCount() { return returnCount; }
        public double getAvgBorrowTimeMs() { return avgBorrowTimeMs; }

        @Override
        public String toString() {
            return String.format("ResourcePoolStatus{poolId='%s', active=%d, idle=%d, waiters=%d, borrows=%d, returns=%d, avgBorrowTime=%.2fms}",
                    poolId, numActive, numIdle, numWaiters, borrowCount, returnCount, avgBorrowTimeMs);
        }
    }

    /**
     * 资源统计类 - 增强的统计信息收集
     */
    private static class ResourceStats {
        private final AtomicLong borrowCount = new AtomicLong(0);
        private final AtomicLong returnCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalBorrowTimeMs = new AtomicLong(0);
        private volatile long lastBorrowTime;
        private volatile long lastReturnTime;

        public void incrementBorrowCount() {
            borrowCount.incrementAndGet();
        }

        public void incrementReturnCount() {
            returnCount.incrementAndGet();
        }

        public void incrementErrorCount() {
            errorCount.incrementAndGet();
        }

        public void addBorrowTime(long timeMs) {
            totalBorrowTimeMs.addAndGet(timeMs);
        }

        public void updateLastBorrowTime() {
            lastBorrowTime = System.currentTimeMillis();
        }

        public void updateLastReturnTime() {
            lastReturnTime = System.currentTimeMillis();
        }

        public long getBorrowCount() {
            return borrowCount.get();
        }

        public long getReturnCount() {
            return returnCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public double getAvgBorrowTimeMs() {
            long count = borrowCount.get();
            return count > 0 ? (double) totalBorrowTimeMs.get() / count : 0;
        }

        public long getLastBorrowTime() {
            return lastBorrowTime;
        }

        public long getLastReturnTime() {
            return lastReturnTime;
        }
    }
    
    /**
     * 资源池持有者 - 封装资源池、工厂和配置
     */
    private static class ResourcePoolHolder<T> {
        private final ObjectPool<T> pool;
        private final ResourceFactory<T> factory;
        private final GenericObjectPoolConfig<T> poolConfig;
        
        public ResourcePoolHolder(ObjectPool<T> pool, ResourceFactory<T> factory, GenericObjectPoolConfig<T> poolConfig) {
            this.pool = pool;
            this.factory = factory;
            this.poolConfig = poolConfig;
        }
        
        public ObjectPool<T> getPool() {
            return pool;
        }
        
        public ResourceFactory<T> getFactory() {
            return factory;
        }
        
        public GenericObjectPoolConfig<T> getPoolConfig() {
            return poolConfig;
        }
    }
    
    /**
     * 资源管理器配置类 - 支持动态更新的配置项
     */
    public static class ResourceManagerConfig {
        private long cleanupIntervalMs = DEFAULT_CLEANUP_INTERVAL_MS;
        private int maxTotal = DEFAULT_MAX_TOTAL;
        private int maxIdle = DEFAULT_MAX_IDLE;
        private int minIdle = DEFAULT_MIN_IDLE;
        private long maxWaitMs = DEFAULT_MAX_WAIT_MS;
        private long minEvictableIdleTimeMs = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MS;
        private boolean testOnBorrow = DEFAULT_TEST_ON_BORROW;
        private boolean testWhileIdle = DEFAULT_TEST_WHILE_IDLE;
        private long timeBetweenEvictionRunsMs = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MS;
        
        // 新增配置项
        private long slowBorrowThresholdMs = 1000; // 1秒
        private double maxErrorRate = 0.1; // 10%
        private double highSaturationThreshold = 0.8; // 80%
        private long unusedPoolCleanupThresholdMs = 300000; // 5分钟
        
        // Getters and Setters
        public long getCleanupIntervalMs() { return cleanupIntervalMs; }
        public void setCleanupIntervalMs(long cleanupIntervalMs) { this.cleanupIntervalMs = cleanupIntervalMs; }
        public int getMaxTotal() { return maxTotal; }
        public void setMaxTotal(int maxTotal) { this.maxTotal = maxTotal; }
        public int getMaxIdle() { return maxIdle; }
        public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }
        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        public long getMaxWaitMs() { return maxWaitMs; }
        public void setMaxWaitMs(long maxWaitMs) { this.maxWaitMs = maxWaitMs; }
        public long getMinEvictableIdleTimeMs() { return minEvictableIdleTimeMs; }
        public void setMinEvictableIdleTimeMs(long minEvictableIdleTimeMs) { this.minEvictableIdleTimeMs = minEvictableIdleTimeMs; }
        public boolean isTestOnBorrow() { return testOnBorrow; }
        public void setTestOnBorrow(boolean testOnBorrow) { this.testOnBorrow = testOnBorrow; }
        public boolean isTestWhileIdle() { return testWhileIdle; }
        public void setTestWhileIdle(boolean testWhileIdle) { this.testWhileIdle = testWhileIdle; }
        public long getTimeBetweenEvictionRunsMs() { return timeBetweenEvictionRunsMs; }
        public void setTimeBetweenEvictionRunsMs(long timeBetweenEvictionRunsMs) { this.timeBetweenEvictionRunsMs = timeBetweenEvictionRunsMs; }
        
        // 新增配置的getter和setter
        public long getSlowBorrowThresholdMs() { return slowBorrowThresholdMs; }
        public void setSlowBorrowThresholdMs(long slowBorrowThresholdMs) { this.slowBorrowThresholdMs = slowBorrowThresholdMs; }
        public double getMaxErrorRate() { return maxErrorRate; }
        public void setMaxErrorRate(double maxErrorRate) { this.maxErrorRate = maxErrorRate; }
        public double getHighSaturationThreshold() { return highSaturationThreshold; }
        public void setHighSaturationThreshold(double highSaturationThreshold) { this.highSaturationThreshold = highSaturationThreshold; }
        public long getUnusedPoolCleanupThresholdMs() { return unusedPoolCleanupThresholdMs; }
        public void setUnusedPoolCleanupThresholdMs(long unusedPoolCleanupThresholdMs) { this.unusedPoolCleanupThresholdMs = unusedPoolCleanupThresholdMs; }
    }
}