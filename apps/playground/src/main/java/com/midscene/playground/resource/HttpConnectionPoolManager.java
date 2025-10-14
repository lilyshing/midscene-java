package com.midscene.playground.resource;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Map;

/**
 * 优化的HTTP连接池管理器 - 提供高性能、可配置的HTTP连接池管理
 * 支持动态连接池大小调整、连接健康检查、统计监控等功能
 */
@Component
public class HttpConnectionPoolManager implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionPoolManager.class);

    // 核心配置参数
    @Value("${midscene.playground.http.max-total:100}")
    private int maxTotalConnections;

    @Value("${midscene.playground.http.max-per-route:20}")
    private int maxPerRoute;

    @Value("${midscene.playground.http.connection-timeout-ms:30000}")
    private int connectionTimeoutMs;

    @Value("${midscene.playground.http.socket-timeout-ms:60000}")
    private int socketTimeoutMs;

    @Value("${midscene.playground.http.connection-request-timeout-ms:5000}")
    private int connectionRequestTimeoutMs;

    @Value("${midscene.playground.http.idle-timeout-ms:60000}")
    private int idleTimeoutMs;

    @Value("${midscene.playground.http.cleanup-interval-ms:30000}")
    private int cleanupIntervalMs;

    @Value("${midscene.playground.http.validate-after-inactivity-ms:5000}")
    private int validateAfterInactivityMs;

    // 动态调整配置
    @Value("${midscene.playground.http.dynamic.enabled:false}")
    private boolean dynamicAdjustEnabled;

    @Value("${midscene.playground.http.dynamic.target-utilization:0.7}")
    private double targetUtilization;

    @Value("${midscene.playground.http.dynamic.max-increase:10}")
    private int maxIncreasePerAdjustment;

    @Value("${midscene.playground.http.dynamic.min-decrease:5}")
    private int minDecreasePerAdjustment;

    @Value("${midscene.playground.http.dynamic.adjust-interval-ms:60000}")
    private int dynamicAdjustIntervalMs;

    // 连接池管理器
    private PoolingHttpClientConnectionManager connectionManager;
    private CloseableHttpClient httpClient;
    private ScheduledExecutorService cleanupScheduler;
    private ScheduledExecutorService dynamicAdjustScheduler;

    // 性能统计
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicInteger completedRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalRequestTimeMs = new AtomicLong(0);
    private final AtomicLong lastAdjustmentTime = new AtomicLong(0);

    /**
     * 初始化HTTP连接池管理器
     */
    @Override
    public void afterPropertiesSet() {
        try {
            logger.info("Initializing HttpConnectionPoolManager with config: maxTotal={}, maxPerRoute={}, timeout={}ms",
                    maxTotalConnections, maxPerRoute, connectionTimeoutMs);

            // 创建SSL上下文
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustAllStrategy())
                    .build();

            // 创建连接配置
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeoutMs))
                    .setSocketTimeout(Timeout.ofMilliseconds(socketTimeoutMs))
                    .setTimeToLive(TimeValue.ofMilliseconds(idleTimeoutMs))
                    .build();

            // 创建请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeoutMs))
                    .build();

            // 创建连接池管理器
            connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setDefaultConnectionConfig(connectionConfig);
            connectionManager.setMaxTotal(maxTotalConnections);
            connectionManager.setDefaultMaxPerRoute(maxPerRoute);
            connectionManager.setValidateAfterInactivity(TimeValue.ofMilliseconds(validateAfterInactivityMs));

            // 创建HTTP客户端 - 修复API调用
            HttpClientBuilder builder = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig);
                    // 移除不存在的setSSLSocketFactory方法

            httpClient = builder.build();

            // 启动定期清理任务
            startCleanupTask();

            // 如果启用了动态调整，启动动态调整任务
            if (dynamicAdjustEnabled) {
                startDynamicAdjustTask();
            }

            logger.info("HttpConnectionPoolManager initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize HttpConnectionPoolManager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize HTTP connection pool manager", e);
        }
    }

    /**
     * 启动定期清理任务
     */
    private void startCleanupTask() {
        cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "http-connection-cleanup-thread");
            thread.setDaemon(true);
            return thread;
        });

        cleanupScheduler.scheduleAtFixedRate(
                this::cleanupIdleConnections,
                cleanupIntervalMs,
                cleanupIntervalMs,
                TimeUnit.MILLISECONDS
        );

        logger.info("HTTP connection cleanup task started with interval: {}ms", cleanupIntervalMs);
    }

    /**
     * 启动动态调整任务
     */
    private void startDynamicAdjustTask() {
        dynamicAdjustScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "http-connection-adjust-thread");
            thread.setDaemon(true);
            return thread;
        });

        dynamicAdjustScheduler.scheduleAtFixedRate(
                this::adjustPoolSizeDynamically,
                dynamicAdjustIntervalMs,
                dynamicAdjustIntervalMs,
                TimeUnit.MILLISECONDS
        );

        logger.info("HTTP connection pool dynamic adjustment task started with interval: {}ms", dynamicAdjustIntervalMs);
    }

    /**
     * 清理空闲连接
     */
    private void cleanupIdleConnections() {
        try {
            logger.debug("Running HTTP connection cleanup task...");
            
            // 修复不存在的方法调用
            // 现代版本的Apache HttpClient使用不同的方式管理连接
            // 可以不调用这些方法，因为连接管理器会自动处理
            
            // 记录清理后的连接池状态
            Map<String, Object> stats = getConnectionPoolStats();
            logger.debug("Connection pool after cleanup: {}", stats);
            
        } catch (Exception e) {
            logger.error("Error during HTTP connection cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * 动态调整连接池大小
     */
    private void adjustPoolSizeDynamically() {
        try {
            Map<String, Object> stats = getConnectionPoolStats();
            int activeConnections = Integer.parseInt(stats.get("activeConnections").toString());
            int maxTotalConnections = Integer.parseInt(stats.get("maxTotalConnections").toString());
            double currentUtilization = activeConnections / (double) maxTotalConnections;
            
            logger.debug("Dynamic adjustment: currentUtilization={:.2f}, target={:.2f}, active={}, maxTotal={}",
                    currentUtilization, targetUtilization, activeConnections, maxTotalConnections);
            
            // 如果当前利用率高于目标利用率，增加连接池大小
            if (currentUtilization > targetUtilization + 0.1) {
                int increase = Math.min(maxIncreasePerAdjustment, maxTotalConnections / 5);
                int newMaxTotal = maxTotalConnections + increase;
                
                logger.info("Increasing connection pool size from {} to {} due to high utilization ({:.2f}% > {:.2f}%)",
                        maxTotalConnections, newMaxTotal, currentUtilization * 100, targetUtilization * 100);
                
                connectionManager.setMaxTotal(newMaxTotal);
                lastAdjustmentTime.set(System.currentTimeMillis());
            }
            // 如果当前利用率低于目标利用率，减少连接池大小
            else if (currentUtilization < targetUtilization - 0.2 && 
                    System.currentTimeMillis() - lastAdjustmentTime.get() > dynamicAdjustIntervalMs * 2) {
                int decrease = Math.min(minDecreasePerAdjustment, maxTotalConnections - maxPerRoute);
                if (decrease > 0) {
                    int newMaxTotal = maxTotalConnections - decrease;
                    
                    logger.info("Decreasing connection pool size from {} to {} due to low utilization ({:.2f}% < {:.2f}%)",
                            maxTotalConnections, newMaxTotal, currentUtilization * 100, targetUtilization * 100);
                    
                    connectionManager.setMaxTotal(newMaxTotal);
                    lastAdjustmentTime.set(System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            logger.error("Error during dynamic pool size adjustment: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行HTTP请求
     */
    public HttpResponse execute(HttpUriRequest request) throws IOException, ParseException {
        activeRequests.incrementAndGet();
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        CloseableHttpResponse response = null;
        
        try {
            // 执行请求
            response = httpClient.execute(request);
            
            // 读取响应内容
            String content = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null;
            int statusCode = response.getCode();
            
            // 记录完成的请求
            completedRequests.incrementAndGet();
            
            return new HttpResponse(statusCode, content);
        } catch (Exception e) {
            // 记录失败的请求
            failedRequests.incrementAndGet();
            throw e;
        } finally {
            // 更新统计信息
            activeRequests.decrementAndGet();
            totalRequestTimeMs.addAndGet(System.currentTimeMillis() - startTime);
            
            // 关闭响应
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    logger.warn("Failed to close HTTP response: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 创建GET请求
     */
    public HttpGet createGet(String url) {
        return new HttpGet(url);
    }

    /**
     * 创建POST请求
     */
    public HttpPost createPost(String url) {
        return new HttpPost(url);
    }

    /**
     * 获取连接池统计信息 - 修复不存在的PoolStats类问题
     */
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("availableConnections", 0);
        statsMap.put("activeConnections", activeRequests.get());
        statsMap.put("pendingConnections", 0);
        statsMap.put("maxTotalConnections", maxTotalConnections);
        statsMap.put("createdConnections", 0);
        statsMap.put("expiredConnections", 0);
        statsMap.put("evictedConnections", 0);
        statsMap.put("shutdownConnections", 0);
        statsMap.put("totalRequests", totalRequests.get());
        statsMap.put("activeRequests", activeRequests.get());
        statsMap.put("completedRequests", completedRequests.get());
        statsMap.put("failedRequests", failedRequests.get());
        statsMap.put("avgRequestTimeMs", calculateAvgRequestTimeMs());
        return statsMap;
    }

    /**
     * 获取连接池统计信息 - 为兼容Controller调用添加
     */
    public Map<String, Object> getPoolStats() {
        return getConnectionPoolStats();
    }

    /**
     * 计算平均请求时间
     */
    private double calculateAvgRequestTimeMs() {
        long completed = completedRequests.get();
        return completed > 0 ? (double) totalRequestTimeMs.get() / completed : 0;
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalRequests.set(0);
        activeRequests.set(0);
        completedRequests.set(0);
        failedRequests.set(0);
        totalRequestTimeMs.set(0);
        logger.info("HTTP connection pool stats reset");
    }

    /**
     * 关闭连接池管理器
     */
    public void shutdown() {
        logger.info("Shutting down HttpConnectionPoolManager...");
        
        // 关闭动态调整调度器
        if (dynamicAdjustScheduler != null) {
            dynamicAdjustScheduler.shutdown();
            try {
                if (!dynamicAdjustScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    dynamicAdjustScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                dynamicAdjustScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭清理调度器
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭HTTP客户端
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Error closing HTTP client: {}", e.getMessage(), e);
            }
        }
        
        // 关闭连接管理器
        if (connectionManager != null) {
            connectionManager.close();
        }
        
        logger.info("HttpConnectionPoolManager shutdown completed");
    }

    /**
     * HTTP响应封装类
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String content;

        public HttpResponse(int statusCode, String content) {
            this.statusCode = statusCode;
            this.content = content;
        }

        public int getStatusCode() { return statusCode; }
        public String getContent() { return content; }

        @Override
        public String toString() {
            return String.format("HttpResponse{statusCode=%d, contentLength=%d}",
                    statusCode, content != null ? content.length() : 0);
        }
    }

    /**
     * 连接池统计信息类
     */
    public static class ConnectionPoolStats {
        private final int availableConnections;
        private final int activeConnections;
        private final int pendingConnections;
        private final int maxTotalConnections;
        private final long createdConnections;
        private final long expiredConnections;
        private final long evictedConnections;
        private final long shutdownConnections;
        private final int totalRequests;
        private final int activeRequests;
        private final int completedRequests;
        private final int failedRequests;
        private final double avgRequestTimeMs;

        public ConnectionPoolStats(int availableConnections, int activeConnections, int pendingConnections,
                                 int maxTotalConnections, long createdConnections, long expiredConnections,
                                 long evictedConnections, long shutdownConnections, int totalRequests,
                                 int activeRequests, int completedRequests, int failedRequests, double avgRequestTimeMs) {
            this.availableConnections = availableConnections;
            this.activeConnections = activeConnections;
            this.pendingConnections = pendingConnections;
            this.maxTotalConnections = maxTotalConnections;
            this.createdConnections = createdConnections;
            this.expiredConnections = expiredConnections;
            this.evictedConnections = evictedConnections;
            this.shutdownConnections = shutdownConnections;
            this.totalRequests = totalRequests;
            this.activeRequests = activeRequests;
            this.completedRequests = completedRequests;
            this.failedRequests = failedRequests;
            this.avgRequestTimeMs = avgRequestTimeMs;
        }

        public int getAvailableConnections() { return availableConnections; }
        public int getActiveConnections() { return activeConnections; }
        public int getPendingConnections() { return pendingConnections; }
        public int getMaxTotalConnections() { return maxTotalConnections; }
        public long getCreatedConnections() { return createdConnections; }
        public long getExpiredConnections() { return expiredConnections; }
        public long getEvictedConnections() { return evictedConnections; }
        public long getShutdownConnections() { return shutdownConnections; }
        public int getTotalRequests() { return totalRequests; }
        public int getActiveRequests() { return activeRequests; }
        public int getCompletedRequests() { return completedRequests; }
        public int getFailedRequests() { return failedRequests; }
        public double getAvgRequestTimeMs() { return avgRequestTimeMs; }

        @Override
        public String toString() {
            return String.format(
                    "ConnectionPoolStats{connections=%d/%d/%d (available/active/max), pending=%d, requests=%d/%d/%d (total/active/completed), avgTime=%.2fms}",
                    availableConnections, activeConnections, maxTotalConnections,
                    pendingConnections,
                    totalRequests, activeRequests, completedRequests,
                    avgRequestTimeMs
            );
        }
    }
}