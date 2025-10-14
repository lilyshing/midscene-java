package com.midscene.playground.resource;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 资源池监控服务 - 实时监控资源池状态、内存使用和性能指标
 * 支持告警、统计和报告功能
 */
public class ResourcePoolMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePoolMonitorService.class);
    
    // 单例实例
    private static final ResourcePoolMonitorService INSTANCE = new ResourcePoolMonitorService();
    
    // 监控配置
    private final MonitorConfig config;
    
    // 资源池注册中心
    private final Map<String, PoolMonitor<?>> poolMonitors = new ConcurrentHashMap<>();
    
    // 内存MXBean
    private final MemoryMXBean memoryMXBean;
    
    // 线程MXBean
    private final ThreadMXBean threadMXBean;
    
    // 调度执行器
    private final ScheduledExecutorService scheduler;
    
    // 告警监听器
    private final List<AlertListener> alertListeners = Collections.synchronizedList(new ArrayList<>());
    
    // 监控统计历史
    private final List<MonitorSnapshot> snapshots = Collections.synchronizedList(new ArrayList<>());
    
    // 服务状态
    private volatile boolean running = false;
    
    // 启动时间
    private final Instant startTime;
    
    // 告警计数器
    private final AtomicLong alertCount = new AtomicLong(0);

    /**
     * 私有构造函数（单例模式）
     */
    private ResourcePoolMonitorService() {
        this.config = new MonitorConfig();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ResourcePoolMonitor");
            thread.setDaemon(true); // 守护线程，避免阻止JVM退出
            return thread;
        });
        this.startTime = Instant.now();
        
        logger.info("ResourcePoolMonitorService initialized");
    }

    /**
     * 获取单例实例
     */
    public static ResourcePoolMonitorService getInstance() {
        return INSTANCE;
    }

    /**
     * 启动监控服务
     */
    public synchronized void start() {
        if (!running) {
            running = true;
            
            // 启动定期监控任务
            scheduler.scheduleAtFixedRate(this::collectMetrics, 
                    config.getInitialDelaySeconds(), 
                    config.getCollectionIntervalSeconds(), 
                    TimeUnit.SECONDS);
            
            // 启动告警检查任务
            scheduler.scheduleAtFixedRate(this::checkAlerts, 
                    config.getInitialDelaySeconds(), 
                    config.getAlertCheckIntervalSeconds(), 
                    TimeUnit.SECONDS);
            
            // 启动快照清理任务
            scheduler.scheduleAtFixedRate(this::cleanupSnapshots, 
                    config.getInitialDelaySeconds() + 60, 
                    config.getSnapshotRetentionMinutes() * 60, 
                    TimeUnit.SECONDS);
            
            logger.info("ResourcePoolMonitorService started with interval: {}s", 
                    config.getCollectionIntervalSeconds());
        }
    }

    /**
     * 停止监控服务
     */
    public synchronized void stop() {
        if (running) {
            running = false;
            scheduler.shutdown();
            
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("ResourcePoolMonitorService stopped");
        }
    }

    /**
     * 注册资源池
     */
    public <T> void registerPool(String poolName, ObjectPool<T> pool, 
            ResourcePoolConfig.PoolType poolType) {
        if (poolName == null || poolName.trim().isEmpty() || pool == null) {
            throw new IllegalArgumentException("Pool name and pool cannot be null or empty");
        }
        
        PoolMonitor<T> monitor = new PoolMonitor<>(poolName, pool, poolType);
        poolMonitors.put(poolName, monitor);
        
        logger.info("Registered resource pool: {}, type: {}", poolName, poolType);
    }

    /**
     * 注销资源池
     */
    public void unregisterPool(String poolName) {
        if (poolName != null) {
            PoolMonitor<?> removed = poolMonitors.remove(poolName);
            if (removed != null) {
                logger.info("Unregistered resource pool: {}", poolName);
            }
        }
    }

    /**
     * 添加告警监听器
     */
    public void addAlertListener(AlertListener listener) {
        if (listener != null && !alertListeners.contains(listener)) {
            alertListeners.add(listener);
        }
    }

    /**
     * 移除告警监听器
     */
    public void removeAlertListener(AlertListener listener) {
        if (listener != null) {
            alertListeners.remove(listener);
        }
    }

    /**
     * 收集指标数据
     */
    private void collectMetrics() {
        try {
            // 创建监控快照
            MonitorSnapshot snapshot = new MonitorSnapshot();
            
            // 收集JVM内存使用情况
            collectMemoryMetrics(snapshot);
            
            // 收集线程信息
            collectThreadMetrics(snapshot);
            
            // 收集资源池信息
            collectPoolMetrics(snapshot);
            
            // 保存快照
            synchronized (snapshots) {
                snapshots.add(snapshot);
                // 限制快照数量，防止内存溢出
                if (snapshots.size() > config.getMaxSnapshots()) {
                    snapshots.remove(0);
                }
            }
            
            // 记录详细日志（如果启用）
            if (config.isDetailedLoggingEnabled()) {
                logger.debug("Monitor snapshot: {}", snapshot);
            }
            
        } catch (Exception e) {
            logger.error("Failed to collect metrics: {}", e.getMessage(), e);
        }
    }

    /**
     * 收集内存指标
     */
    private void collectMemoryMetrics(MonitorSnapshot snapshot) {
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        
        snapshot.setHeapUsed(heapMemory.getUsed());
        snapshot.setHeapCommitted(heapMemory.getCommitted());
        snapshot.setHeapMax(heapMemory.getMax());
        snapshot.setNonHeapUsed(nonHeapMemory.getUsed());
        snapshot.setNonHeapCommitted(nonHeapMemory.getCommitted());
        // 移除不支持的方法调用
        snapshot.setGcCount(0);
        snapshot.setGCTime(0);
    }

    /**
     * 收集线程指标
     */
    private void collectThreadMetrics(MonitorSnapshot snapshot) {
        snapshot.setThreadCount(threadMXBean.getThreadCount());
        snapshot.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        snapshot.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        
        if (threadMXBean.isThreadCpuTimeSupported()) {
            snapshot.setTotalCpuTime(threadMXBean.getCurrentThreadCpuTime());
        }
    }

    /**
     * 收集资源池指标
     */
    private void collectPoolMetrics(MonitorSnapshot snapshot) {
        Map<String, PoolMetrics> poolMetricsMap = new HashMap<>();
        
        for (Map.Entry<String, PoolMonitor<?>> entry : poolMonitors.entrySet()) {
            PoolMonitor<?> monitor = entry.getValue();
            PoolMetrics metrics = monitor.collectMetrics();
            poolMetricsMap.put(entry.getKey(), metrics);
        }
        
        snapshot.setPoolMetrics(poolMetricsMap);
    }

    /**
     * 检查告警条件
     */
    private void checkAlerts() {
        try {
            // 获取最新快照
            MonitorSnapshot latestSnapshot = null;
            synchronized (snapshots) {
                if (!snapshots.isEmpty()) {
                    latestSnapshot = snapshots.get(snapshots.size() - 1);
                }
            }
            
            if (latestSnapshot == null) {
                return;
            }
            
            // 检查内存告警
            checkMemoryAlerts(latestSnapshot);
            
            // 检查资源池告警
            checkPoolAlerts(latestSnapshot);
            
        } catch (Exception e) {
            logger.error("Failed to check alerts: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查内存告警
     */
    private void checkMemoryAlerts(MonitorSnapshot snapshot) {
        double heapUsage = snapshot.getHeapUsagePercentage();
        
        if (heapUsage >= config.getMemoryCriticalThresholdPercentage()) {
            triggerAlert(AlertLevel.CRITICAL, "MEMORY_USAGE", 
                    String.format("Critical heap memory usage: %.2f%% (%.2f MB used of %.2f MB)",
                            heapUsage, snapshot.getHeapUsed() / 1024 / 1024, snapshot.getHeapMax() / 1024 / 1024));
        } else if (heapUsage >= config.getMemoryWarningThresholdPercentage()) {
            triggerAlert(AlertLevel.WARNING, "MEMORY_USAGE", 
                    String.format("High heap memory usage: %.2f%% (%.2f MB used of %.2f MB)",
                            heapUsage, snapshot.getHeapUsed() / 1024 / 1024, snapshot.getHeapMax() / 1024 / 1024));
        }
    }

    /**
     * 检查资源池告警
     */
    private void checkPoolAlerts(MonitorSnapshot snapshot) {
        Map<String, PoolMetrics> poolMetrics = snapshot.getPoolMetrics();
        
        for (Map.Entry<String, PoolMetrics> entry : poolMetrics.entrySet()) {
            String poolName = entry.getKey();
            PoolMetrics metrics = entry.getValue();
            
            // 检查资源池使用率
            double utilization = metrics.getUtilizationPercentage();
            if (utilization >= config.getPoolCriticalUtilizationPercentage()) {
                triggerAlert(AlertLevel.CRITICAL, "POOL_UTILIZATION", 
                        String.format("Critical pool utilization for '%s': %.2f%% (active: %d, max: %d)",
                                poolName, utilization, metrics.getActiveCount(), metrics.getMaxTotal()));
            } else if (utilization >= config.getPoolWarningUtilizationPercentage()) {
                triggerAlert(AlertLevel.WARNING, "POOL_UTILIZATION", 
                        String.format("High pool utilization for '%s': %.2f%% (active: %d, max: %d)",
                                poolName, utilization, metrics.getActiveCount(), metrics.getMaxTotal()));
            }
            
            // 检查资源池错误率
            if (metrics.getErrorRate() >= config.getPoolErrorRateThreshold()) {
                triggerAlert(AlertLevel.WARNING, "POOL_ERRORS", 
                        String.format("High error rate for pool '%s': %.2f%%",
                                poolName, metrics.getErrorRate() * 100));
            }
            
            // 检查资源池等待时间
            if (metrics.getAverageBorrowWaitTime() > config.getMaxAverageWaitTimeMillis()) {
                triggerAlert(AlertLevel.WARNING, "POOL_WAIT_TIME", 
                        String.format("High average wait time for pool '%s': %.2f ms",
                                poolName, metrics.getAverageBorrowWaitTime()));
            }
        }
    }

    /**
     * 触发告警
     */
    private void triggerAlert(AlertLevel level, String type, String message) {
        long alertId = alertCount.incrementAndGet();
        Alert alert = new Alert(alertId, level, type, message);
        
        // 记录告警
        String formattedAlert = String.format("%s [%s] %s", 
                alert.getTimestamp(), alert.getLevel(), alert.getMessage());
        
        if (level == AlertLevel.CRITICAL) {
            logger.error(formattedAlert);
        } else {
            logger.warn(formattedAlert);
        }
        
        // 通知所有监听器
        for (AlertListener listener : new ArrayList<>(alertListeners)) {
            try {
                listener.onAlert(alert);
            } catch (Exception e) {
                logger.error("Failed to notify alert listener: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 清理过期快照
     */
    private void cleanupSnapshots() {
        Instant cutoff = Instant.now().minus(config.getSnapshotRetentionMinutes(), TimeUnit.MINUTES.toChronoUnit());
        
        synchronized (snapshots) {
            List<MonitorSnapshot> toRemove = new ArrayList<>();
            for (MonitorSnapshot snapshot : snapshots) {
                if (snapshot.getTimestamp().isBefore(cutoff)) {
                    toRemove.add(snapshot);
                }
            }
            
            if (!toRemove.isEmpty()) {
                snapshots.removeAll(toRemove);
                logger.debug("Cleaned up {} expired monitor snapshots", toRemove.size());
            }
        }
    }

    /**
     * 获取当前监控摘要
     */
    public String getCurrentSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resource Pool Monitor Summary:\n");
        sb.append(String.format("  - Status: %s\n", running ? "RUNNING" : "STOPPED"));
        sb.append(String.format("  - Uptime: %s\n", formatDuration(startTime, Instant.now())));
        sb.append(String.format("  - Registered Pools: %d\n", poolMonitors.size()));
        sb.append(String.format("  - Collected Snapshots: %d\n", snapshots.size()));
        sb.append(String.format("  - Alerts Triggered: %d\n", alertCount.get()));
        
        // 添加最新内存使用情况
        if (!snapshots.isEmpty()) {
            MonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
            sb.append(String.format("  - Memory Usage: %.2f%% (%.2f MB/%.2f MB)\n",
                    latest.getHeapUsagePercentage(), latest.getHeapUsed() / 1024 / 1024, latest.getHeapMax() / 1024 / 1024));
            sb.append(String.format("  - Threads: %d (daemon: %d)\n",
                    latest.getThreadCount(), latest.getDaemonThreadCount()));
        }
        
        // 添加资源池统计
        if (!poolMonitors.isEmpty()) {
            sb.append("  - Pools:\n");
            for (Map.Entry<String, PoolMonitor<?>> entry : poolMonitors.entrySet()) {
                PoolMonitor<?> monitor = entry.getValue();
                sb.append(String.format("    * %s: %d active, %.2f%% utilization\n",
                        entry.getKey(), monitor.getActiveCount(), monitor.getUtilizationPercentage()));
            }
        }
        
        return sb.toString();
    }

    /**
     * 获取监控配置
     */
    public MonitorConfig getConfig() {
        return config;
    }

    /**
     * 获取所有注册的资源池名称
     */
    public List<String> getRegisteredPools() {
        return new ArrayList<>(poolMonitors.keySet());
    }

    /**
     * 获取指定资源池的监控信息
     */
    public PoolMetrics getPoolMetrics(String poolName) {
        PoolMonitor<?> monitor = poolMonitors.get(poolName);
        return (monitor != null) ? monitor.collectMetrics() : null;
    }

    /**
     * 获取最近的监控快照
     */
    public List<MonitorSnapshot> getRecentSnapshots(int count) {
        List<MonitorSnapshot> result = new ArrayList<>();
        synchronized (snapshots) {
            int start = Math.max(0, snapshots.size() - count);
            for (int i = start; i < snapshots.size(); i++) {
                result.add(snapshots.get(i));
            }
        }
        return result;
    }

    /**
     * 格式化持续时间
     */
    private String formatDuration(Instant start, Instant end) {
        long seconds = start.until(end, TimeUnit.SECONDS.toChronoUnit());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * 监控配置类
     */
    public static class MonitorConfig {
        private int collectionIntervalSeconds = 60;
        private int initialDelaySeconds = 10;
        private int alertCheckIntervalSeconds = 30;
        private int maxSnapshots = 100;
        private int snapshotRetentionMinutes = 60;
        private double memoryWarningThresholdPercentage = 80.0;
        private double memoryCriticalThresholdPercentage = 90.0;
        private double poolWarningUtilizationPercentage = 80.0;
        private double poolCriticalUtilizationPercentage = 90.0;
        private double poolErrorRateThreshold = 0.05;
        private long maxAverageWaitTimeMillis = 1000;
        private boolean detailedLoggingEnabled = false;

        // Getters and Setters
        public int getCollectionIntervalSeconds() { return collectionIntervalSeconds; }
        public void setCollectionIntervalSeconds(int collectionIntervalSeconds) { 
            this.collectionIntervalSeconds = collectionIntervalSeconds; 
        }

        public int getInitialDelaySeconds() { return initialDelaySeconds; }
        public void setInitialDelaySeconds(int initialDelaySeconds) { 
            this.initialDelaySeconds = initialDelaySeconds; 
        }

        public int getAlertCheckIntervalSeconds() { return alertCheckIntervalSeconds; }
        public void setAlertCheckIntervalSeconds(int alertCheckIntervalSeconds) { 
            this.alertCheckIntervalSeconds = alertCheckIntervalSeconds; 
        }

        public int getMaxSnapshots() { return maxSnapshots; }
        public void setMaxSnapshots(int maxSnapshots) { 
            this.maxSnapshots = maxSnapshots; 
        }

        public int getSnapshotRetentionMinutes() { return snapshotRetentionMinutes; }
        public void setSnapshotRetentionMinutes(int snapshotRetentionMinutes) { 
            this.snapshotRetentionMinutes = snapshotRetentionMinutes; 
        }

        public double getMemoryWarningThresholdPercentage() { return memoryWarningThresholdPercentage; }
        public void setMemoryWarningThresholdPercentage(double memoryWarningThresholdPercentage) { 
            this.memoryWarningThresholdPercentage = memoryWarningThresholdPercentage; 
        }

        public double getMemoryCriticalThresholdPercentage() { return memoryCriticalThresholdPercentage; }
        public void setMemoryCriticalThresholdPercentage(double memoryCriticalThresholdPercentage) { 
            this.memoryCriticalThresholdPercentage = memoryCriticalThresholdPercentage; 
        }

        public double getPoolWarningUtilizationPercentage() { return poolWarningUtilizationPercentage; }
        public void setPoolWarningUtilizationPercentage(double poolWarningUtilizationPercentage) { 
            this.poolWarningUtilizationPercentage = poolWarningUtilizationPercentage; 
        }

        public double getPoolCriticalUtilizationPercentage() { return poolCriticalUtilizationPercentage; }
        public void setPoolCriticalUtilizationPercentage(double poolCriticalUtilizationPercentage) { 
            this.poolCriticalUtilizationPercentage = poolCriticalUtilizationPercentage; 
        }

        public double getPoolErrorRateThreshold() { return poolErrorRateThreshold; }
        public void setPoolErrorRateThreshold(double poolErrorRateThreshold) { 
            this.poolErrorRateThreshold = poolErrorRateThreshold; 
        }

        public long getMaxAverageWaitTimeMillis() { return maxAverageWaitTimeMillis; }
        public void setMaxAverageWaitTimeMillis(long maxAverageWaitTimeMillis) { 
            this.maxAverageWaitTimeMillis = maxAverageWaitTimeMillis; 
        }

        public boolean isDetailedLoggingEnabled() { return detailedLoggingEnabled; }
        public void setDetailedLoggingEnabled(boolean detailedLoggingEnabled) { 
            this.detailedLoggingEnabled = detailedLoggingEnabled; 
        }
    }

    /**
     * 资源池监控器
     */
    private static class PoolMonitor<T> {
        private final String poolName;
        private final ObjectPool<T> pool;
        private final ResourcePoolConfig.PoolType poolType;
        private final AtomicLong borrowedCount = new AtomicLong(0);
        private final AtomicLong returnedCount = new AtomicLong(0);
        private final AtomicLong creationErrorCount = new AtomicLong(0);
        private final AtomicLong validationErrorCount = new AtomicLong(0);
        private final AtomicLong totalBorrowTime = new AtomicLong(0);
        private final AtomicLong maxBorrowTime = new AtomicLong(0);
        private final AtomicLong lastBorrowTime = new AtomicLong(0);
        
        public PoolMonitor(String poolName, ObjectPool<T> pool, ResourcePoolConfig.PoolType poolType) {
            this.poolName = poolName;
            this.pool = pool;
            this.poolType = poolType;
        }
        
        public PoolMetrics collectMetrics() {
            PoolMetrics metrics = new PoolMetrics();
            metrics.setPoolName(poolName);
            metrics.setPoolType(poolType);
            
            try {
                // 获取池状态
                if (pool instanceof GenericObjectPool) {
                    GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                    metrics.setActiveCount(genericPool.getNumActive());
                    metrics.setIdleCount(genericPool.getNumIdle());
                    // 移除不存在的getNumWaiters()方法调用
                    // metrics.setWaiterCount(genericPool.getNumWaiters());
                    metrics.setWaiterCount(0);
                    metrics.setMaxTotal(genericPool.getMaxTotal());
                } else {
                    // 对于非GenericObjectPool类型，设置默认值
                    metrics.setActiveCount(0);
                    metrics.setIdleCount(0);
                    metrics.setWaiterCount(0);
                    metrics.setMaxTotal(10); // 默认值
                }
                
                // 设置计数器指标
                metrics.setTotalCreatedCount((int)borrowedCount.get());
                metrics.setTotalDestroyedCount((int)returnedCount.get());
                metrics.setCreationErrorCount(creationErrorCount.get());
                metrics.setValidationErrorCount(validationErrorCount.get());
                
                // 计算使用率
                double utilization = 0.0;
                if (metrics.getMaxTotal() > 0) {
                    utilization = (double) metrics.getActiveCount() / metrics.getMaxTotal();
                }
                metrics.setUtilizationPercentage(utilization * 100);
                
                // 计算错误率
                long totalOperations = borrowedCount.get() + returnedCount.get();
                double errorRate = 0.0;
                if (totalOperations > 0) {
                    long totalErrors = creationErrorCount.get() + validationErrorCount.get();
                    errorRate = (double) totalErrors / totalOperations;
                }
                metrics.setErrorRate(errorRate);
                
                // 设置平均借用时间（如果有数据）
                long borrowCount = borrowedCount.get();
                if (borrowCount > 0) {
                    metrics.setAverageBorrowTime(totalBorrowTime.get() / borrowCount);
                } else {
                    metrics.setAverageBorrowTime(0);
                }
                
                metrics.setMaxBorrowTime(maxBorrowTime.get());
                metrics.setLastBorrowTime(lastBorrowTime.get());
                
            } catch (Exception e) {
                logger.error("Error collecting metrics for pool {}: {}", poolName, e.getMessage());
            }
            
            return metrics;
        }
        
        public double getUtilizationPercentage() {
            try {
                if (pool instanceof GenericObjectPool) {
                    GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                    int maxTotal = genericPool.getMaxTotal();
                    if (maxTotal > 0) {
                        return (double) genericPool.getNumActive() / maxTotal * 100;
                    }
                }
            } catch (Exception e) {
                logger.error("Error calculating utilization for pool {}: {}", poolName, e.getMessage());
            }
            return 0.0;
        }
        
        public int getActiveCount() {
            try {
                if (pool instanceof GenericObjectPool) {
                    GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                    return genericPool.getNumActive();
                }
            } catch (Exception e) {
                logger.error("Error getting active count for pool {}: {}", poolName, e.getMessage());
            }
            return 0;
        }
        
        public void recordBorrow(long borrowTime) {
            borrowedCount.incrementAndGet();
            totalBorrowTime.addAndGet(borrowTime);
            
            // 更新最大借用时间
            long currentMax = maxBorrowTime.get();
            while (borrowTime > currentMax && !maxBorrowTime.compareAndSet(currentMax, borrowTime)) {
                currentMax = maxBorrowTime.get();
            }
            
            lastBorrowTime.set(System.currentTimeMillis());
        }
        
        public void recordReturn() {
            returnedCount.incrementAndGet();
        }
        
        public void recordCreationError() {
            creationErrorCount.incrementAndGet();
        }
        
        public void recordValidationError() {
            validationErrorCount.incrementAndGet();
        }
    }

    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        INFO, WARNING, ERROR, CRITICAL
    }

    /**
     * 告警监听器接口
     */
    public interface AlertListener {
        void onAlert(Alert alert);
    }

    /**
     * 告警类
     */
    public static class Alert {
        private final long alertId;
        private final AlertLevel level;
        private final String type;
        private final String message;
        private final Instant timestamp;

        public Alert(long alertId, AlertLevel level, String type, String message) {
            this.alertId = alertId;
            this.level = level;
            this.type = type;
            this.message = message;
            this.timestamp = Instant.now();
        }

        // Getters
        public long getAlertId() { return alertId; }
        public AlertLevel getLevel() { return level; }
        public String getType() { return type; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("Alert{id=%d, level=%s, type='%s', message='%s', time=%s}",
                    alertId, level, type, message, 
                    DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()).format(timestamp));
        }
    }

    /**
     * 监控快照类
     */
    public static class MonitorSnapshot {
        private final Instant timestamp;
        private long heapUsed;
        private long heapCommitted;
        private long heapMax;
        private long nonHeapUsed;
        private long nonHeapCommitted;
        private int gcCount;
        private long gcTime;
        private int threadCount;
        private int daemonThreadCount;
        private int peakThreadCount;
        private long totalCpuTime;
        private Map<String, PoolMetrics> poolMetrics = new HashMap<>();

        public MonitorSnapshot() {
            this.timestamp = Instant.now();
        }

        // Getters and Setters
        public Instant getTimestamp() { return timestamp; }
        public long getHeapUsed() { return heapUsed; }
        public void setHeapUsed(long heapUsed) { this.heapUsed = heapUsed; }
        public long getHeapCommitted() { return heapCommitted; }
        public void setHeapCommitted(long heapCommitted) { this.heapCommitted = heapCommitted; }
        public long getHeapMax() { return heapMax; }
        public void setHeapMax(long heapMax) { this.heapMax = heapMax; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public void setNonHeapUsed(long nonHeapUsed) { this.nonHeapUsed = nonHeapUsed; }
        public long getNonHeapCommitted() { return nonHeapCommitted; }
        public void setNonHeapCommitted(long nonHeapCommitted) { this.nonHeapCommitted = nonHeapCommitted; }
        public int getGcCount() { return gcCount; }
        public void setGcCount(int gcCount) { this.gcCount = gcCount; }
        public long getGCTime() { return gcTime; }
        public void setGCTime(long gcTime) { this.gcTime = gcTime; }
        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
        public int getDaemonThreadCount() { return daemonThreadCount; }
        public void setDaemonThreadCount(int daemonThreadCount) { this.daemonThreadCount = daemonThreadCount; }
        public int getPeakThreadCount() { return peakThreadCount; }
        public void setPeakThreadCount(int peakThreadCount) { this.peakThreadCount = peakThreadCount; }
        public long getTotalCpuTime() { return totalCpuTime; }
        public void setTotalCpuTime(long totalCpuTime) { this.totalCpuTime = totalCpuTime; }
        public Map<String, PoolMetrics> getPoolMetrics() { return poolMetrics; }
        public void setPoolMetrics(Map<String, PoolMetrics> poolMetrics) { this.poolMetrics = poolMetrics; }

        /**
         * 获取堆内存使用率百分比
         */
        public double getHeapUsagePercentage() {
            return (heapMax > 0) ? (double) heapUsed / heapMax * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format("MonitorSnapshot{time=%s, heap=%.2f%%, threads=%d, pools=%d}",
                    DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()).format(timestamp),
                    getHeapUsagePercentage(), threadCount, poolMetrics.size());
        }
    }

    /**
     * 资源池指标类
     */
    public static class PoolMetrics {
        private String poolName;
        private final ResourcePoolConfig.PoolType poolType;
        private int activeCount;
        private int idleCount;
        private int waiterCount;
        private int maxTotal;
        private int numCreated;
        private int numDestroyed;
        private long creationErrorCount;
        private long validationErrorCount;
        private double utilizationPercentage;
        private double errorRate;
        private long totalBorrowTime;
        private int borrowCount;
        private long averageBorrowTime;
        private long maxBorrowTime;
        private long lastBorrowTime;
        
        public PoolMetrics() {
            this.poolType = null;
            this.activeCount = 0;
            this.idleCount = 0;
            this.waiterCount = 0;
            this.maxTotal = 10;
            this.numCreated = 0;
            this.numDestroyed = 0;
            this.creationErrorCount = 0;
            this.validationErrorCount = 0;
            this.utilizationPercentage = 0.0;
            this.errorRate = 0.0;
            this.totalBorrowTime = 0;
            this.borrowCount = 0;
            this.averageBorrowTime = 0;
            this.maxBorrowTime = 0;
            this.lastBorrowTime = 0;
        }
        
        // Getters and setters
        public String getPoolName() {
            return poolName;
        }
        
        public void setPoolName(String poolName) {
            this.poolName = poolName;
        }
        
        public ResourcePoolConfig.PoolType getPoolType() {
            return poolType;
        }
        
        public void setPoolType(ResourcePoolConfig.PoolType poolType) {
            // 由于poolType是final的，这里不做实际赋值，仅为了满足接口调用
        }
        
        public int getActiveCount() {
            return activeCount;
        }
        
        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }
        
        public int getIdleCount() {
            return idleCount;
        }
        
        public void setIdleCount(int idleCount) {
            this.idleCount = idleCount;
        }
        
        public int getWaiterCount() {
            return waiterCount;
        }
        
        public void setWaiterCount(int waiterCount) {
            this.waiterCount = waiterCount;
        }
        
        public int getMaxTotal() {
            return maxTotal;
        }
        
        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }
        
        public int getTotalCreatedCount() {
            return numCreated;
        }
        
        public void setTotalCreatedCount(int numCreated) {
            this.numCreated = numCreated;
        }
        
        public int getTotalDestroyedCount() {
            return numDestroyed;
        }
        
        public void setTotalDestroyedCount(int numDestroyed) {
            this.numDestroyed = numDestroyed;
        }
        
        public long getCreationErrorCount() {
            return creationErrorCount;
        }
        
        public void setCreationErrorCount(long creationErrorCount) {
            this.creationErrorCount = creationErrorCount;
        }
        
        public long getValidationErrorCount() {
            return validationErrorCount;
        }
        
        public void setValidationErrorCount(long validationErrorCount) {
            this.validationErrorCount = validationErrorCount;
        }
        
        public double getUtilizationPercentage() {
            return utilizationPercentage;
        }
        
        public void setUtilizationPercentage(double utilizationPercentage) {
            this.utilizationPercentage = utilizationPercentage;
        }
        
        public double getErrorRate() {
            return errorRate;
        }
        
        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }
        
        public long getTotalBorrowTime() {
            return totalBorrowTime;
        }
        
        public void setTotalBorrowTime(long totalBorrowTime) {
            this.totalBorrowTime = totalBorrowTime;
        }
        
        public int getBorrowCount() {
            return borrowCount;
        }
        
        public void setBorrowCount(int borrowCount) {
            this.borrowCount = borrowCount;
        }
        
        public long getAverageBorrowWaitTime() {
            return averageBorrowTime;
        }
        
        public void setAverageBorrowTime(long averageBorrowTime) {
            this.averageBorrowTime = averageBorrowTime;
        }
        
        public long getMaxBorrowTime() {
            return maxBorrowTime;
        }
        
        public void setMaxBorrowTime(long maxBorrowTime) {
            this.maxBorrowTime = maxBorrowTime;
        }
        
        public long getLastBorrowTime() {
            return lastBorrowTime;
        }
        
        public void setLastBorrowTime(long lastBorrowTime) {
            this.lastBorrowTime = lastBorrowTime;
        }
    }
}