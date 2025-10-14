package com.midscene.playground.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 资源监控服务 - 提供系统资源使用监控、内存泄漏检测和自动优化功能
 * 支持内存使用监控、GC统计、对象生命周期追踪和自动资源回收
 */
@Service
public class MemoryMonitorService implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MemoryMonitorService.class);

    // JVM内存监控配置
    @Value("${midscene.playground.monitor.memory.warning-threshold:80}")
    private int memoryWarningThreshold;

    @Value("${midscene.playground.monitor.memory.critical-threshold:90}")
    private int memoryCriticalThreshold;

    @Value("${midscene.playground.monitor.memory.check-interval-ms:30000}")
    private int memoryCheckIntervalMs;

    @Value("${midscene.playground.monitor.memory.auto-reclaim-enabled:true}")
    private boolean autoReclaimEnabled;

    // 对象跟踪配置
    @Value("${midscene.playground.monitor.object-tracking-enabled:false}")
    private boolean objectTrackingEnabled;

    @Value("${midscene.playground.monitor.object-tracking-threshold:1000}")
    private int objectTrackingThreshold;

    @Value("${midscene.playground.monitor.leak-detection-interval-ms:120000}")
    private int leakDetectionIntervalMs;

    // 采样配置
    @Value("${midscene.playground.monitor.sampling-rate:1000}")
    private int samplingRate;

    // JVM管理接口
    private final MemoryMXBean memoryMXBean;
    private final List<MemoryPoolMXBean> memoryPoolMXBeans;
    private final List<GarbageCollectorMXBean> gcMXBeans;
    private final ThreadMXBean threadMXBean;

    // 监控线程和调度器
    private ScheduledExecutorService monitorScheduler;
    private ScheduledFuture<?> memoryCheckFuture;
    private ScheduledFuture<?> leakDetectionFuture;

    // 对象生命周期跟踪
    private final ConcurrentHashMap<String, ResourceTracker> resourceTrackers;
    private final AtomicLong totalResourcesCreated = new AtomicLong(0);
    private final AtomicLong totalResourcesDestroyed = new AtomicLong(0);
    private final AtomicBoolean isMonitoringActive = new AtomicBoolean(false);
    private final AtomicReference<MemoryStats> lastMemoryStats = new AtomicReference<>();
    private final AtomicReference<GCStats> lastGCStats = new AtomicReference<>();
    private final AtomicReference<ThreadStats> lastThreadStats = new AtomicReference<>();

    // 内存使用历史记录（用于趋势分析）
    private final CircularBuffer<MemorySample> memoryHistory = new CircularBuffer<>(100);
    private final CircularBuffer<GCEvent> gcHistory = new CircularBuffer<>(100);

    public MemoryMonitorService() {
        // 获取JVM管理Bean
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.threadMXBean = ManagementFactory.getThreadMXBean();

        // 初始化资源跟踪器
        this.resourceTrackers = new ConcurrentHashMap<>();
    }

    /**
     * 初始化监控服务
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Initializing MemoryMonitorService with config: warningThreshold={}%, criticalThreshold={}%, checkInterval={}ms",
                memoryWarningThreshold, memoryCriticalThreshold, memoryCheckIntervalMs);

        // 初始化调度器
        monitorScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "memory-monitor-thread");
            thread.setDaemon(true);
            return thread;
        });

        // 启动内存检查任务
        memoryCheckFuture = monitorScheduler.scheduleAtFixedRate(
                this::checkMemoryUsage,
                0, // 立即执行第一次
                memoryCheckIntervalMs,
                TimeUnit.MILLISECONDS
        );

        // 启动泄漏检测任务（如果启用）
        if (objectTrackingEnabled) {
            leakDetectionFuture = monitorScheduler.scheduleAtFixedRate(
                    this::detectPotentialLeaks,
                    leakDetectionIntervalMs,
                    leakDetectionIntervalMs,
                    TimeUnit.MILLISECONDS
            );
        }

        // 记录JVM信息
        logJvmInfo();

        isMonitoringActive.set(true);
        logger.info("MemoryMonitorService initialized successfully");
    }

    /**
     * 检查内存使用情况
     */
    private void checkMemoryUsage() {
        try {
            // 获取当前内存统计
            MemoryStats stats = getCurrentMemoryStats();
            lastMemoryStats.set(stats);

            // 记录内存使用样本
            if (isSamplingRequired()) {
                memoryHistory.add(new MemorySample(
                        System.currentTimeMillis(),
                        stats.getHeapMemoryUsage().getUsed(),
                        stats.getHeapMemoryUsage().getMax(),
                        stats.getNonHeapMemoryUsage().getUsed()
                ));
            }

            // 记录GC统计
            GCStats gcStats = getCurrentGCStats();
            lastGCStats.set(gcStats);

            // 检查内存使用阈值
            checkMemoryThresholds(stats);

            // 记录线程统计
            if (isSamplingRequired()) {
                ThreadStats threadStats = getCurrentThreadStats();
                lastThreadStats.set(threadStats);
            }

            // 记录资源跟踪统计
            logResourceTrackingStats();

        } catch (Exception e) {
            logger.error("Error during memory check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 确定是否需要进行采样
     */
    private boolean isSamplingRequired() {
        return System.currentTimeMillis() % samplingRate == 0;
    }

    /**
     * 检查内存阈值并在必要时触发回收
     */
    private void checkMemoryThresholds(MemoryStats stats) {
        long heapUsed = stats.getHeapMemoryUsage().getUsed();
        long heapMax = stats.getHeapMemoryUsage().getMax();
        int usagePercentage = heapMax > 0 ? (int) ((double) heapUsed / heapMax * 100) : 0;

        // 记录内存使用情况
        logger.debug("Memory usage: {}% ({}MB used of {}MB)", 
                usagePercentage, heapUsed / 1024 / 1024, heapMax / 1024 / 1024);

        // 检查临界阈值
        if (usagePercentage >= memoryCriticalThreshold) {
            logger.warn("CRITICAL Memory usage: {}% - exceeding critical threshold of {}%", 
                    usagePercentage, memoryCriticalThreshold);
            
            // 记录详细内存池信息
            logMemoryPoolDetails();
            
            // 执行强制垃圾回收
            if (autoReclaimEnabled) {
                forceGarbageCollection(true);
            }
        }
        // 检查警告阈值
        else if (usagePercentage >= memoryWarningThreshold) {
            logger.warn("WARNING Memory usage: {}% - exceeding warning threshold of {}%", 
                    usagePercentage, memoryWarningThreshold);
            
            // 记录详细内存池信息
            logMemoryPoolDetails();
            
            // 执行垃圾回收
            if (autoReclaimEnabled) {
                forceGarbageCollection(false);
            }
        }
    }

    /**
     * 执行垃圾回收
     */
    private void forceGarbageCollection(boolean critical) {
        logger.info("Performing {} garbage collection...", critical ? "CRITICAL" : "regular");
        
        long beforeGcUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long startTime = System.currentTimeMillis();
        
        // 执行GC
        System.gc();
        System.runFinalization();
        
        // 等待GC完成
        try {
            Thread.sleep(critical ? 1000 : 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再次执行以确保
        System.gc();
        
        long afterGcUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long duration = System.currentTimeMillis() - startTime;
        long reclaimed = beforeGcUsed - afterGcUsed;
        
        // 记录GC事件
        gcHistory.add(new GCEvent(
                System.currentTimeMillis(),
                duration,
                reclaimed
        ));
        
        logger.info("Garbage collection completed in {}ms: {}MB reclaimed ({}MB -> {}MB)", 
                duration, reclaimed / 1024 / 1024,
                beforeGcUsed / 1024 / 1024, afterGcUsed / 1024 / 1024);
    }

    /**
     * 检测潜在的内存泄漏
     */
    private void detectPotentialLeaks() {
        if (!objectTrackingEnabled) {
            return;
        }

        try {
            logger.debug("Performing potential memory leak detection...");
            
            // 分析各资源跟踪器的状态
            for (Map.Entry<String, ResourceTracker> entry : resourceTrackers.entrySet()) {
                ResourceTracker tracker = entry.getValue();
                ResourceStats stats = tracker.getStats();
                
                // 检查资源增长趋势
                if (stats.getActiveCount() > objectTrackingThreshold && 
                    stats.getActiveCount() > stats.getDestroyCount() * 1.5) {
                    
                    logger.warn("Potential memory leak detected for resource type '{}': {} active, {} created, {} destroyed",
                            entry.getKey(), stats.getActiveCount(), stats.getCreateCount(), stats.getDestroyCount());
                    
                    // 记录详细信息
                    logResourceTrackerDetails(entry.getKey(), tracker);
                }
            }
            
            // 检查长时间未销毁的对象
            checkLongLivedObjects();
            
        } catch (Exception e) {
            logger.error("Error during leak detection: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查长时间存在的对象
     */
    private void checkLongLivedObjects() {
        long thresholdTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5); // 5分钟
        
        for (Map.Entry<String, ResourceTracker> entry : resourceTrackers.entrySet()) {
            ResourceTracker tracker = entry.getValue();
            List<ObjectRecord> longLivedObjects = tracker.getObjectsCreatedBefore(thresholdTime);
            
            if (!longLivedObjects.isEmpty() && longLivedObjects.size() > 100) {
                logger.warn("Found {} long-lived objects of type '{}' created more than 5 minutes ago",
                        longLivedObjects.size(), entry.getKey());
                
                // 记录部分样本
                if (longLivedObjects.size() <= 10) {
                    for (ObjectRecord record : longLivedObjects) {
                        logger.debug("Long-lived object: {} created at {}", 
                                record.getObjectId(), new Date(record.getCreationTime()));
                    }
                }
            }
        }
    }

    /**
     * 开始跟踪资源
     */
    public <T> void trackResource(String resourceType, T resource) {
        if (!objectTrackingEnabled) {
            return;
        }

        try {
            ResourceTracker tracker = resourceTrackers.computeIfAbsent(resourceType, k -> new ResourceTracker(resourceType));
            tracker.trackObject(resource);
            totalResourcesCreated.incrementAndGet();
        } catch (Exception e) {
            logger.warn("Failed to track resource of type '{}': {}", resourceType, e.getMessage());
        }
    }

    /**
     * 停止跟踪资源
     */
    public <T> void untrackResource(String resourceType, T resource) {
        if (!objectTrackingEnabled) {
            return;
        }

        try {
            ResourceTracker tracker = resourceTrackers.get(resourceType);
            if (tracker != null) {
                tracker.untrackObject(resource);
                totalResourcesDestroyed.incrementAndGet();
            }
        } catch (Exception e) {
            logger.warn("Failed to untrack resource of type '{}': {}", resourceType, e.getMessage());
        }
    }

    /**
     * 获取当前内存统计
     */
    public MemoryStats getCurrentMemoryStats() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        // 获取内存池详细信息
        Map<String, MemoryUsage> poolUsage = new HashMap<>();
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            poolUsage.put(pool.getName(), pool.getUsage());
        }
        
        return new MemoryStats(heapUsage, nonHeapUsage, poolUsage);
    }

    /**
     * 获取当前GC统计
     */
    public GCStats getCurrentGCStats() {
        List<GCCollectorInfo> gcCollectors = new ArrayList<>();
        
        for (GarbageCollectorMXBean gc : gcMXBeans) {
            gcCollectors.add(new GCCollectorInfo(
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTime(),
                    Arrays.asList(gc.getMemoryPoolNames())
            ));
        }
        
        return new GCStats(gcCollectors);
    }

    /**
     * 获取当前线程统计
     */
    public ThreadStats getCurrentThreadStats() {
        int threadCount = threadMXBean.getThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        int deadlockedThreadCount = 0;
        
        // 检查死锁
        if (threadMXBean.isSynchronizerUsageSupported()) {
            long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
            deadlockedThreadCount = deadlockedThreads != null ? deadlockedThreads.length : 0;
        }
        
        return new ThreadStats(
                threadCount,
                daemonThreadCount,
                peakThreadCount,
                totalStartedThreadCount,
                deadlockedThreadCount
        );
    }

    /**
     * 获取资源使用摘要
     */
    public ResourceSummary getResourceSummary() {
        return new ResourceSummary(
                lastMemoryStats.get() != null ? lastMemoryStats.get() : getCurrentMemoryStats(),
                lastGCStats.get() != null ? lastGCStats.get() : getCurrentGCStats(),
                lastThreadStats.get() != null ? lastThreadStats.get() : getCurrentThreadStats(),
                getResourceTrackingSummary(),
                System.currentTimeMillis()
        );
    }

    /**
     * 获取资源跟踪摘要
     */
    public ResourceTrackingSummary getResourceTrackingSummary() {
        Map<String, ResourceStats> perTypeStats = new HashMap<>();
        
        for (Map.Entry<String, ResourceTracker> entry : resourceTrackers.entrySet()) {
            perTypeStats.put(entry.getKey(), entry.getValue().getStats());
        }
        
        return new ResourceTrackingSummary(
                totalResourcesCreated.get(),
                totalResourcesDestroyed.get(),
                perTypeStats
        );
    }

    /**
     * 重置资源监控
     */
    public void reset() {
        // 重置资源跟踪
        resourceTrackers.clear();
        totalResourcesCreated.set(0);
        totalResourcesDestroyed.set(0);
        
        // 清空历史记录
        memoryHistory.clear();
        gcHistory.clear();
        
        // 重置最后统计
        lastMemoryStats.set(null);
        lastGCStats.set(null);
        lastThreadStats.set(null);
        
        logger.info("MemoryMonitorService reset completed");
    }

    /**
     * 销毁监控服务
     */
    @Override
    public void destroy() throws Exception {
        logger.info("Shutting down MemoryMonitorService...");
        
        isMonitoringActive.set(false);
        
        // 取消定时任务
        if (memoryCheckFuture != null && !memoryCheckFuture.isCancelled()) {
            memoryCheckFuture.cancel(true);
        }
        
        if (leakDetectionFuture != null && !leakDetectionFuture.isCancelled()) {
            leakDetectionFuture.cancel(true);
        }
        
        // 关闭调度器
        if (monitorScheduler != null) {
            monitorScheduler.shutdown();
            try {
                if (!monitorScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitorScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitorScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 记录最终状态
        if (logger.isInfoEnabled()) {
            ResourceSummary summary = getResourceSummary();
            logger.info("Final resource usage summary: {}", summary);
        }
        
        logger.info("MemoryMonitorService shutdown completed");
    }

    /**
     * 获取内存使用统计信息
     */
    public Map<String, Object> getMemoryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取堆内存使用情况
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        Map<String, Object> heapStats = new HashMap<>();
        heapStats.put("init", heapMemoryUsage.getInit());
        heapStats.put("used", heapMemoryUsage.getUsed());
        heapStats.put("committed", heapMemoryUsage.getCommitted());
        heapStats.put("max", heapMemoryUsage.getMax());
        
        Map<String, Object> nonHeapStats = new HashMap<>();
        nonHeapStats.put("init", nonHeapMemoryUsage.getInit());
        nonHeapStats.put("used", nonHeapMemoryUsage.getUsed());
        nonHeapStats.put("committed", nonHeapMemoryUsage.getCommitted());
        nonHeapStats.put("max", nonHeapMemoryUsage.getMax());
        
        stats.put("heap", heapStats);
        stats.put("nonHeap", nonHeapStats);
        
        // GC统计信息
        Map<String, Object> gcStats = new HashMap<>();
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            Map<String, Object> gcBeanStats = new HashMap<>();
            gcBeanStats.put("collectionCount", gcBean.getCollectionCount());
            gcBeanStats.put("collectionTime", gcBean.getCollectionTime());
            gcStats.put(gcBean.getName(), gcBeanStats);
        }
        stats.put("gc", gcStats);
        
        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        stats.put("threadCount", threadMXBean.getThreadCount());
        stats.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        stats.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        
        return stats;
    }
    
    /**
     * 触发内存清理
     */
    public void triggerMemoryCleanup() {
        try {
            logger.info("Triggering memory cleanup...");
            
            // 手动触发GC（仅在极端情况下使用）
            System.gc();
            
            // 等待GC完成
            Thread.sleep(100);
            
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            logger.info("Memory cleanup completed. Heap memory after cleanup: {}MB / {}MB", 
                heapMemory.getUsed() / 1024 / 1024, 
                heapMemory.getMax() / 1024 / 1024);
        } catch (Exception e) {
            logger.error("Error during memory cleanup: {}", e.getMessage(), e);
        }
    }

    private void logJvmInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        logger.info("JVM Info: maxMemory={}MB, totalMemory={}MB, freeMemory={}MB, processors={}",
                maxMemory / 1024 / 1024, totalMemory / 1024 / 1024, freeMemory / 1024 / 1024,
                runtime.availableProcessors());
    }

    private void logMemoryPoolDetails() {
        StringBuilder sb = new StringBuilder("Memory pool details:\n");
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                int percentUsed = usage.getMax() > 0 ? (int) ((double) usage.getUsed() / usage.getMax() * 100) : 0;
                sb.append(String.format("- %s: %d%% used (%dMB/%dMB)\n",
                        pool.getName(), percentUsed,
                        usage.getUsed() / 1024 / 1024, usage.getMax() / 1024 / 1024));
            }
        }
        logger.info(sb.toString());
    }

    private void logResourceTrackingStats() {
        if (!objectTrackingEnabled || resourceTrackers.isEmpty()) {
            return;
        }
        
        StringBuilder sb = new StringBuilder("Resource tracking stats:\n");
        for (Map.Entry<String, ResourceTracker> entry : resourceTrackers.entrySet()) {
            ResourceStats stats = entry.getValue().getStats();
            sb.append(String.format("- %s: %d active, %d created, %d destroyed\n",
                    entry.getKey(), stats.getActiveCount(), stats.getCreateCount(), stats.getDestroyCount()));
        }
        logger.debug(sb.toString());
    }

    private void logResourceTrackerDetails(String type, ResourceTracker tracker) {
        ResourceStats stats = tracker.getStats();
        
        // 记录最近创建的对象信息
        List<ObjectRecord> recentObjects = tracker.getRecentlyCreatedObjects(5);
        StringBuilder sb = new StringBuilder(String.format("Resource tracker details for '%s':\n", type));
        sb.append(String.format("- Stats: %d active, %d created, %d destroyed\n",
                stats.getActiveCount(), stats.getCreateCount(), stats.getDestroyCount()));
        
        if (!recentObjects.isEmpty()) {
            sb.append("- Recently created objects:\n");
            for (ObjectRecord record : recentObjects) {
                sb.append(String.format("  - ID: %s, Created: %s\n",
                        record.getObjectId(), new Date(record.getCreationTime())));
            }
        }
        
        logger.info(sb.toString());
    }

    // 内部类定义
    
    /**
     * 资源跟踪器 - 跟踪特定类型资源的生命周期
     */
    private static class ResourceTracker {
        private final String resourceType;
        private final ConcurrentHashMap<Object, ObjectRecord> trackedObjects = new ConcurrentHashMap<>();
        private final AtomicLong createCount = new AtomicLong(0);
        private final AtomicLong destroyCount = new AtomicLong(0);
        private final CopyOnWriteArrayList<ObjectRecord> recentCreations = new CopyOnWriteArrayList<>();

        public ResourceTracker(String resourceType) {
            this.resourceType = resourceType;
        }

        public void trackObject(Object obj) {
            if (obj == null) return;
            
            String objectId = Integer.toHexString(System.identityHashCode(obj));
            long creationTime = System.currentTimeMillis();
            
            ObjectRecord record = new ObjectRecord(objectId, creationTime, obj.getClass().getName());
            trackedObjects.put(obj, record);
            createCount.incrementAndGet();
            
            // 记录最近创建的对象
            synchronized (recentCreations) {
                recentCreations.add(0, record);
                // 只保留最近100个
                if (recentCreations.size() > 100) {
                    recentCreations.subList(100, recentCreations.size()).clear();
                }
            }
        }

        public void untrackObject(Object obj) {
            if (obj == null) return;
            
            ObjectRecord removed = trackedObjects.remove(obj);
            if (removed != null) {
                destroyCount.incrementAndGet();
            }
        }

        public ResourceStats getStats() {
            return new ResourceStats(
                    trackedObjects.size(),
                    createCount.get(),
                    destroyCount.get()
            );
        }

        public List<ObjectRecord> getObjectsCreatedBefore(long timestamp) {
            List<ObjectRecord> result = new ArrayList<>();
            for (ObjectRecord record : trackedObjects.values()) {
                if (record.getCreationTime() < timestamp) {
                    result.add(record);
                }
            }
            return result;
        }

        public List<ObjectRecord> getRecentlyCreatedObjects(int count) {
            synchronized (recentCreations) {
                return new ArrayList<>(recentCreations.subList(0, Math.min(count, recentCreations.size())));
            }
        }
    }

    /**
     * 对象记录 - 存储被跟踪对象的信息
     */
    private static class ObjectRecord {
        private final String objectId;
        private final long creationTime;
        private final String className;

        public ObjectRecord(String objectId, long creationTime, String className) {
            this.objectId = objectId;
            this.creationTime = creationTime;
            this.className = className;
        }

        public String getObjectId() { return objectId; }
        public long getCreationTime() { return creationTime; }
        public String getClassName() { return className; }
    }

    /**
     * 资源统计 - 特定类型资源的使用统计
     */
    public static class ResourceStats {
        private final int activeCount;
        private final long createCount;
        private final long destroyCount;

        public ResourceStats(int activeCount, long createCount, long destroyCount) {
            this.activeCount = activeCount;
            this.createCount = createCount;
            this.destroyCount = destroyCount;
        }

        public int getActiveCount() { return activeCount; }
        public long getCreateCount() { return createCount; }
        public long getDestroyCount() { return destroyCount; }

        @Override
        public String toString() {
            return String.format("ResourceStats{active=%d, created=%d, destroyed=%d}",
                    activeCount, createCount, destroyCount);
        }
    }

    /**
     * 内存统计 - 系统内存使用统计
     */
    public static class MemoryStats {
        private final MemoryUsage heapMemoryUsage;
        private final MemoryUsage nonHeapMemoryUsage;
        private final Map<String, MemoryUsage> memoryPoolUsage;

        public MemoryStats(MemoryUsage heapMemoryUsage, MemoryUsage nonHeapMemoryUsage, Map<String, MemoryUsage> memoryPoolUsage) {
            this.heapMemoryUsage = heapMemoryUsage;
            this.nonHeapMemoryUsage = nonHeapMemoryUsage;
            this.memoryPoolUsage = memoryPoolUsage;
        }

        public MemoryUsage getHeapMemoryUsage() { return heapMemoryUsage; }
        public MemoryUsage getNonHeapMemoryUsage() { return nonHeapMemoryUsage; }
        public Map<String, MemoryUsage> getMemoryPoolUsage() { return memoryPoolUsage; }
    }

    /**
     * GC统计 - 垃圾收集器统计
     */
    public static class GCStats {
        private final List<GCCollectorInfo> gcCollectors;

        public GCStats(List<GCCollectorInfo> gcCollectors) {
            this.gcCollectors = gcCollectors;
        }

        public List<GCCollectorInfo> getGcCollectors() { return gcCollectors; }
    }

    /**
     * GC收集器信息
     */
    public static class GCCollectorInfo {
        private final String name;
        private final long collectionCount;
        private final long collectionTime;
        private final List<String> memoryPools;

        public GCCollectorInfo(String name, long collectionCount, long collectionTime, List<String> memoryPools) {
            this.name = name;
            this.collectionCount = collectionCount;
            this.collectionTime = collectionTime;
            this.memoryPools = memoryPools;
        }

        public String getName() { return name; }
        public long getCollectionCount() { return collectionCount; }
        public long getCollectionTime() { return collectionTime; }
        public List<String> getMemoryPools() { return memoryPools; }
    }

    /**
     * 线程统计 - 线程使用统计
     */
    public static class ThreadStats {
        private final int threadCount;
        private final int daemonThreadCount;
        private final int peakThreadCount;
        private final long totalStartedThreadCount;
        private final int deadlockedThreadCount;

        public ThreadStats(int threadCount, int daemonThreadCount, int peakThreadCount, 
                          long totalStartedThreadCount, int deadlockedThreadCount) {
            this.threadCount = threadCount;
            this.daemonThreadCount = daemonThreadCount;
            this.peakThreadCount = peakThreadCount;
            this.totalStartedThreadCount = totalStartedThreadCount;
            this.deadlockedThreadCount = deadlockedThreadCount;
        }

        public int getThreadCount() { return threadCount; }
        public int getDaemonThreadCount() { return daemonThreadCount; }
        public int getPeakThreadCount() { return peakThreadCount; }
        public long getTotalStartedThreadCount() { return totalStartedThreadCount; }
        public int getDeadlockedThreadCount() { return deadlockedThreadCount; }
    }

    /**
     * 资源摘要 - 系统资源使用摘要
     */
    public static class ResourceSummary {
        private final MemoryStats memoryStats;
        private final GCStats gcStats;
        private final ThreadStats threadStats;
        private final ResourceTrackingSummary trackingSummary;
        private final long timestamp;

        public ResourceSummary(MemoryStats memoryStats, GCStats gcStats, ThreadStats threadStats,
                              ResourceTrackingSummary trackingSummary, long timestamp) {
            this.memoryStats = memoryStats;
            this.gcStats = gcStats;
            this.threadStats = threadStats;
            this.trackingSummary = trackingSummary;
            this.timestamp = timestamp;
        }

        public MemoryStats getMemoryStats() { return memoryStats; }
        public GCStats getGcStats() { return gcStats; }
        public ThreadStats getThreadStats() { return threadStats; }
        public ResourceTrackingSummary getTrackingSummary() { return trackingSummary; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            long heapUsed = memoryStats.getHeapMemoryUsage().getUsed() / 1024 / 1024;
            long heapMax = memoryStats.getHeapMemoryUsage().getMax() / 1024 / 1024;
            int usagePercent = heapMax > 0 ? (int) ((double) heapUsed / heapMax * 100) : 0;
            
            return String.format("ResourceSummary{memory=%d%% (%dMB/%dMB), threads=%d, resources=%d/%d}",
                    usagePercent, heapUsed, heapMax,
                    threadStats.getThreadCount(),
                    trackingSummary.getActiveResources(),
                    trackingSummary.getTotalResourcesCreated());
        }
    }

    /**
     * 资源跟踪摘要 - 资源跟踪统计摘要
     */
    public static class ResourceTrackingSummary {
        private final long totalResourcesCreated;
        private final long totalResourcesDestroyed;
        private final Map<String, ResourceStats> perTypeStats;

        public ResourceTrackingSummary(long totalResourcesCreated, long totalResourcesDestroyed,
                                      Map<String, ResourceStats> perTypeStats) {
            this.totalResourcesCreated = totalResourcesCreated;
            this.totalResourcesDestroyed = totalResourcesDestroyed;
            this.perTypeStats = perTypeStats;
        }

        public long getTotalResourcesCreated() { return totalResourcesCreated; }
        public long getTotalResourcesDestroyed() { return totalResourcesDestroyed; }
        public long getActiveResources() { 
            return perTypeStats.values().stream()
                    .mapToInt(ResourceStats::getActiveCount)
                    .sum();
        }
        public Map<String, ResourceStats> getPerTypeStats() { return perTypeStats; }
    }

    /**
     * 内存样本 - 内存使用历史样本
     */
    private static class MemorySample {
        private final long timestamp;
        private final long heapUsed;
        private final long heapMax;
        private final long nonHeapUsed;

        public MemorySample(long timestamp, long heapUsed, long heapMax, long nonHeapUsed) {
            this.timestamp = timestamp;
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
        }
    }

    /**
     * GC事件 - 垃圾收集事件记录
     */
    private static class GCEvent {
        private final long timestamp;
        private final long durationMs;
        private final long reclaimedBytes;

        public GCEvent(long timestamp, long durationMs, long reclaimedBytes) {
            this.timestamp = timestamp;
            this.durationMs = durationMs;
            this.reclaimedBytes = reclaimedBytes;
        }
    }

    /**
     * 循环缓冲区 - 用于存储历史数据
     */
    private static class CircularBuffer<T> {
        private final LinkedList<T> buffer;
        private final int capacity;

        public CircularBuffer(int capacity) {
            this.buffer = new LinkedList<>();
            this.capacity = capacity;
        }

        public synchronized void add(T item) {
            if (buffer.size() >= capacity) {
                buffer.removeFirst();
            }
            buffer.addLast(item);
        }

        public synchronized List<T> getAll() {
            return new ArrayList<>(buffer);
        }

        public synchronized void clear() {
            buffer.clear();
        }

        public synchronized int size() {
            return buffer.size();
        }
    }
}