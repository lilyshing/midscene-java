package com.midscene.playground.resource;

// 修复导入语句，确保使用正确的接口
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.hc.client5.http.impl.classic.HttpClients;

// 移除未使用的导入
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceManagementExample {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagementExample.class);
    
    // 资源池实例
    private static ObjectPool<CloseableHttpClient> httpClientPool;
    private static ObjectPool<Connection> dbConnectionPool;
    private static ObjectPool<FileResource> fileResourcePool;
    
    // 资源工厂
    private static ResourceFactoryImpl resourceFactory;
    
    // 监控服务
    private static ResourcePoolMonitorService monitorService;

    /**
     * 主方法 - 运行资源管理示例
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Resource Management Example");
            
            // 初始化资源管理组件
            initResourceManagement();
            
            // 运行基本示例
            runBasicExamples();
            
            // 运行并发测试示例
            runConcurrentTests();
            
            // 打印监控统计信息
            printMonitoringStats();
            
        } catch (Exception e) {
            logger.error("Error in Resource Management Example: {}", e.getMessage(), e);
        } finally {
            // 清理资源
            cleanupResources();
        }
    }

    /**
     * 初始化资源管理组件
     */
    private static void initResourceManagement() throws Exception {
        logger.info("Initializing resource management components");
        
        // 创建资源工厂
        resourceFactory = new ResourceFactoryImpl();
        
        // 初始化监控服务
        monitorService = ResourcePoolMonitorService.getInstance();
        
        // 配置监控服务
        ResourcePoolMonitorService.MonitorConfig monitorConfig = monitorService.getConfig();
        monitorConfig.setCollectionIntervalSeconds(10); // 10秒收集一次指标
        monitorConfig.setAlertCheckIntervalSeconds(5);  // 5秒检查一次告警
        monitorConfig.setDetailedLoggingEnabled(false); // 禁用详细日志
        
        // 启动监控服务
        monitorService.start();
        
        // 创建HTTP客户端连接池
        createHttpClientPool();
        
        // 创建数据库连接池
        createDatabaseConnectionPool();
        
        // 创建文件资源池
        createFileResourcePool();
        
        logger.info("Resource management components initialized successfully");
    }

    /**
     * 创建HTTP客户端连接池
     */
    // 修改createHttpClientPool方法
    private static void createHttpClientPool() {
        logger.info("Creating HTTP client connection pool");
        
        // 获取HTTP客户端池配置
        ResourcePoolConfig httpConfig = ResourcePoolConfig.getConfig(ResourcePoolConfig.PoolType.HTTP_CLIENT);
        
        // 创建HTTP客户端工厂
        HttpClientFactory httpClientFactory = new HttpClientFactory(resourceFactory);
        
        // 直接创建并配置GenericObjectPoolConfig，确保正确的泛型参数
        GenericObjectPoolConfig<CloseableHttpClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(httpConfig.getMaxTotal());
        poolConfig.setMaxIdle(httpConfig.getMaxIdle());
        poolConfig.setMinIdle(httpConfig.getMinIdle());
        poolConfig.setMaxWaitMillis(httpConfig.getMaxWaitMillis());
        poolConfig.setTestOnBorrow(httpConfig.isTestOnBorrow());
        poolConfig.setTestOnReturn(httpConfig.isTestOnReturn());
        poolConfig.setTestWhileIdle(httpConfig.isTestWhileIdle());
        
        // 创建对象池
        httpClientPool = new GenericObjectPool<>(httpClientFactory, poolConfig);
        
        // 注册到监控服务
        monitorService.registerPool("httpClientPool", httpClientPool, ResourcePoolConfig.PoolType.HTTP_CLIENT);
        
        logger.info("HTTP client connection pool created with maxTotal={}, maxIdle={}, minIdle={}",
                httpConfig.getMaxTotal(), httpConfig.getMaxIdle(), httpConfig.getMinIdle());
    }

    // 修改createDatabaseConnectionPool方法
    private static void createDatabaseConnectionPool() {
        logger.info("Creating database connection pool");
        
        // 获取数据库连接池配置
        ResourcePoolConfig dbConfig = ResourcePoolConfig.getConfig(ResourcePoolConfig.PoolType.DATABASE_CONNECTION);
        
        // 创建数据库连接工厂
        DatabaseConnectionFactory dbFactory = new DatabaseConnectionFactory(resourceFactory);
        
        // 直接创建并配置GenericObjectPoolConfig，确保正确的泛型参数
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(dbConfig.getMaxTotal());
        poolConfig.setMaxIdle(dbConfig.getMaxIdle());
        poolConfig.setMinIdle(dbConfig.getMinIdle());
        poolConfig.setMaxWaitMillis(dbConfig.getMaxWaitMillis());
        poolConfig.setTestOnBorrow(dbConfig.isTestOnBorrow());
        poolConfig.setTestOnReturn(dbConfig.isTestOnReturn());
        poolConfig.setTestWhileIdle(dbConfig.isTestWhileIdle());
        
        // 创建对象池
        dbConnectionPool = new GenericObjectPool<>(dbFactory, poolConfig);
        
        // 注册到监控服务
        monitorService.registerPool("databaseConnectionPool", dbConnectionPool, 
                ResourcePoolConfig.PoolType.DATABASE_CONNECTION);
        
        logger.info("Database connection pool created with maxTotal={}, maxIdle={}, minIdle={}",
                dbConfig.getMaxTotal(), dbConfig.getMaxIdle(), dbConfig.getMinIdle());
    }

    // 修改createFileResourcePool方法
    private static void createFileResourcePool() {
        logger.info("Creating file resource pool");
        
        // 获取文件资源池配置
        ResourcePoolConfig fileConfig = ResourcePoolConfig.getConfig(ResourcePoolConfig.PoolType.FILE_INPUT_STREAM);
        
        // 创建文件资源工厂
        FileResourceFactory fileFactory = new FileResourceFactory(resourceFactory);
        
        // 直接创建并配置GenericObjectPoolConfig，确保正确的泛型参数
        GenericObjectPoolConfig<FileResource> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(fileConfig.getMaxTotal());
        poolConfig.setMaxIdle(fileConfig.getMaxIdle());
        poolConfig.setMinIdle(fileConfig.getMinIdle());
        poolConfig.setMaxWaitMillis(fileConfig.getMaxWaitMillis());
        poolConfig.setTestOnBorrow(fileConfig.isTestOnBorrow());
        poolConfig.setTestOnReturn(fileConfig.isTestOnReturn());
        poolConfig.setTestWhileIdle(fileConfig.isTestWhileIdle());
        
        // 创建对象池
        fileResourcePool = new GenericObjectPool<>(fileFactory, poolConfig);
        
        // 注册到监控服务
        monitorService.registerPool("fileResourcePool", fileResourcePool, 
                ResourcePoolConfig.PoolType.FILE_INPUT_STREAM);
        
        logger.info("File resource pool created with maxTotal={}, maxIdle={}, minIdle={}",
                fileConfig.getMaxTotal(), fileConfig.getMaxIdle(), fileConfig.getMinIdle());
    }

    /**
     * 运行基本示例
     */
    private static void runBasicExamples() throws Exception {
        logger.info("Running basic examples");
        
        // 测试HTTP客户端池
        testHttpClientPool();
        
        // 测试数据库连接池
        testDatabaseConnectionPool();
        
        // 测试文件资源池
        testFileResourcePool();
        
        logger.info("Basic examples completed successfully");
    }

    /**
     * 测试HTTP客户端池
     */
    private static void testHttpClientPool() {
        System.out.println("\n===== Testing HTTP Client Pool =====");
        
        try {
            // 创建资源配置
            ResourcePoolConfig config = new ResourcePoolConfig();
            config.setMaxTotal(5);
            config.setMaxIdle(3);
            config.setMinIdle(1);
            config.setMaxWaitMillis(3000L); // 使用正确的方法名
            config.setTestOnBorrow(true);
            // 移除不存在的setPoolType方法
            
            // 创建HTTP客户端工厂
            HttpClientFactory httpClientFactory = new HttpClientFactory(new ResourceFactoryImpl());
            
            // 创建对象池配置
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(config.getMaxTotal());
            poolConfig.setMaxIdle(config.getMaxIdle());
            poolConfig.setMinIdle(config.getMinIdle());
            poolConfig.setMaxWaitMillis(config.getMaxWaitMillis()); // 使用正确的方法名
            poolConfig.setTestOnBorrow(config.isTestOnBorrow());
            
            // 创建GenericObjectPool并指定泛型参数
            GenericObjectPool<CloseableHttpClient> pool = new GenericObjectPool<>(httpClientFactory, poolConfig);
            
            // 注册到监控服务
            ResourcePoolMonitorService monitorService = ResourcePoolMonitorService.getInstance();
            monitorService.registerPool("http-client-pool", pool, ResourcePoolConfig.PoolType.HTTP_CLIENT);
            
            // 运行并发测试
            int threadCount = 10;
            int iterationsPerThread = 2;
            runConcurrentTests(pool, threadCount, iterationsPerThread, (client) -> {
                try {
                    // 发送HTTP请求测试
                    HttpGet httpGet = new HttpGet("https://httpbin.org/get");
                    try (CloseableHttpResponse response = client.execute(httpGet);
                         BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(response.getEntity().getContent()))) {
                        
                        // 读取响应状态
                        int statusCode = response.getCode();
                        System.out.println("HTTP Response Status: " + statusCode);
                        
                        // 读取并打印响应内容（前100个字符）
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                            if (content.length() > 100) break;
                        }
                        System.out.println("Response Content Preview: " + 
                                (content.length() > 100 ? content.substring(0, 100) + "..." : content.toString()));
                    }
                    return true;
                } catch (Exception e) {
                    System.err.println("HTTP request failed: " + e.getMessage());
                    return false;
                }
            });
            
            // 打印池统计信息
            printPoolStats(pool);
            
            // 关闭池
            pool.close();
            monitorService.unregisterPool("http-client-pool");
            
        } catch (Exception e) {
            System.err.println("Error in HTTP client pool test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试数据库连接池
     */
    private static void testDatabaseConnectionPool() throws Exception {
        logger.info("Testing database connection pool");
        
        // 从池中获取数据库连接
        Connection connection = dbConnectionPool.borrowObject();
        
        try {
            // 验证连接是否有效
            boolean isValid = resourceFactory.validateDatabaseConnection(connection);
            logger.info("Database connection validation: {}", isValid ? "SUCCESS" : "FAILED");
            
            // 执行简单查询
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1 AS TEST")) {
                
                if (rs.next()) {
                    int result = rs.getInt("TEST");
                    logger.info("Database query test result: {}", result);
                }
            }
            
        } finally {
            // 归还到池中
            dbConnectionPool.returnObject(connection);
        }
    }
    
    /**
     * 测试文件资源池
     */
    private static void testFileResourcePool() throws Exception {
        logger.info("Testing file resource pool");
        
        // 从池中获取文件资源
        FileResource fileResource = fileResourcePool.borrowObject();
        
        try {
            // 创建临时文件进行测试
            File testFile = File.createTempFile("test", ".txt");
            testFile.deleteOnExit();
            
            // 使用文件资源
            fileResource.setFile(testFile);
            
            // 写入测试数据
            try (OutputStream out = resourceFactory.createFileOutputStream(testFile)) {
                out.write("Hello, Resource Pool!".getBytes());
            }
            
            // 读取测试数据
            try (InputStream in = resourceFactory.createFileInputStream(testFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                String content = new String(buffer, 0, bytesRead);
                logger.info("File content: '{}'", content);
            }
            
        } finally {
            // 清空资源状态
            fileResource.reset();
            // 归还到池中
            fileResourcePool.returnObject(fileResource);
        }
    }
    
    /**
     * 运行并发测试
     */
    private static void runConcurrentTests() throws Exception {
        logger.info("Running concurrent tests");
        
        // 创建线程池
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // 提交并发任务
        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    // 随机选择资源类型进行测试
                    int resourceType = taskId % 3;
                    
                    switch (resourceType) {
                        case 0:
                            concurrentHttpClientTest(taskId);
                            break;
                        case 1:
                            concurrentDatabaseTest(taskId);
                            break;
                        case 2:
                            concurrentFileTest(taskId);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Error in concurrent task {}: {}", taskId, e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        
        // 关闭线程池
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        logger.info("Concurrent tests completed");
    }
    
    /**
     * 并发测试HTTP客户端池
     */
    private static void concurrentHttpClientTest(int taskId) throws Exception {
        try {
            logger.debug("Task {}: Borrowing HTTP client", taskId);
            CloseableHttpClient client = httpClientPool.borrowObject();
            try {
                // 模拟工作
                Thread.sleep(100);
                logger.debug("Task {}: Using HTTP client", taskId);
            } finally {
                httpClientPool.returnObject(client);
                logger.debug("Task {}: Returned HTTP client", taskId);
            }
        } catch (Exception e) {
            logger.error("Task {}: HTTP client test failed", taskId, e);
        }
    }
    
    /**
     * 并发测试数据库连接池
     */
    private static void concurrentDatabaseTest(int taskId) throws Exception {
        try {
            logger.debug("Task {}: Borrowing database connection", taskId);
            Connection conn = dbConnectionPool.borrowObject();
            try {
                // 模拟工作
                Thread.sleep(100);
                logger.debug("Task {}: Using database connection", taskId);
            } finally {
                dbConnectionPool.returnObject(conn);
                logger.debug("Task {}: Returned database connection", taskId);
            }
        } catch (Exception e) {
            logger.error("Task {}: Database connection test failed", taskId, e);
        }
    }
    
    /**
     * 并发测试文件资源池
     */
    private static void concurrentFileTest(int taskId) throws Exception {
        try {
            logger.debug("Task {}: Borrowing file resource", taskId);
            FileResource fileResource = fileResourcePool.borrowObject();
            try {
                // 模拟工作
                Thread.sleep(100);
                logger.debug("Task {}: Using file resource", taskId);
            } finally {
                fileResource.reset();
                fileResourcePool.returnObject(fileResource);
                logger.debug("Task {}: Returned file resource", taskId);
            }
        } catch (Exception e) {
            logger.error("Task {}: File resource test failed", taskId, e);
        }
    }
    
    /**
     * 打印监控统计信息
     */
    private static void printMonitoringStats() throws Exception {
        // 等待一些监控数据收集
        Thread.sleep(2000);
        
        logger.info("\n=== Resource Monitoring Statistics ===");
        logger.info(monitorService.getCurrentSummary());
        
        // 打印资源工厂统计
        logger.info("\n=== Resource Factory Statistics ===");
        logger.info(resourceFactory.getResourceUsageSummary());
        
        // 打印各资源池的详细指标
        for (String poolName : monitorService.getRegisteredPools()) {
            ResourcePoolMonitorService.PoolMetrics metrics = monitorService.getPoolMetrics(poolName);
            if (metrics != null) {
                logger.info("\n--- Pool: {} ---", poolName);
                logger.info("  Type: {}", metrics.getPoolType());
                logger.info("  Active Count: {}", metrics.getActiveCount());
                logger.info("  Idle Count: {}", metrics.getIdleCount());
                logger.info("  Utilization: {:.2f}%", metrics.getUtilizationPercentage());
                logger.info("  Error Rate: {:.4f}%", metrics.getErrorRate() * 100);
                logger.info("  Total Created: {}", metrics.getTotalCreatedCount());
                logger.info("  Total Destroyed: {}", metrics.getTotalDestroyedCount());
            }
        }
        
        logger.info("\n=== End of Statistics ===");
    }
    
    /**
     * 清理资源
     */
    private static void cleanupResources() {
        logger.info("Cleaning up resources");
        
        try {
            // 关闭资源池
            if (httpClientPool != null) {
                httpClientPool.close();
                monitorService.unregisterPool("httpClientPool");
            }
            
            if (dbConnectionPool != null) {
                dbConnectionPool.close();
                monitorService.unregisterPool("databaseConnectionPool");
            }
            
            if (fileResourcePool != null) {
                fileResourcePool.close();
                monitorService.unregisterPool("fileResourcePool");
            }
            
            // 停止监控服务
            monitorService.stop();
            
        } catch (Exception e) {
            logger.error("Error during resource cleanup: {}", e.getMessage(), e);
        }
        
        logger.info("Resource cleanup completed");
    }
    
    // 创建HTTP客户端池
    private static ObjectPool<CloseableHttpClient> createHttpClientPool(ResourcePoolConfig config) {
        // 创建HTTP客户端工厂
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        
        // 配置Apache Commons Pool2的GenericObjectPoolConfig
        GenericObjectPoolConfig<CloseableHttpClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.getMaxTotal());
        poolConfig.setMaxIdle(config.getMaxIdle());
        poolConfig.setMinIdle(config.getMinIdle());
        poolConfig.setMaxWaitMillis(config.getMaxWaitMillis());
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());
        poolConfig.setTestOnReturn(config.isTestOnReturn());
        poolConfig.setTestWhileIdle(config.isTestWhileIdle());
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        
        // 创建并返回对象池
        return new GenericObjectPool<>(httpClientFactory, poolConfig);
    }
    
    // HTTP客户端工厂类
    // 替换整个HttpClientFactory类
    private static class HttpClientFactory implements PooledObjectFactory<CloseableHttpClient> {
        private final ResourceFactoryImpl resourceFactory;
        
        // 默认构造函数
        public HttpClientFactory() {
            this.resourceFactory = null;
        }
        
        // 接受ResourceFactory参数的构造函数
        public HttpClientFactory(ResourceFactoryImpl resourceFactory) {
            this.resourceFactory = resourceFactory;
        }
        
        @Override
        public PooledObject<CloseableHttpClient> makeObject() throws Exception {
            // 创建连接池管理器
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(100);  // 连接池最大连接数
            connectionManager.setDefaultMaxPerRoute(20);  // 每个路由默认最大连接数
            
            // 创建请求配置 - 使用正确的方法名
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(3000))
                    .setResponseTimeout(Timeout.ofMilliseconds(5000))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(2000))
                    .build();
            
            // 创建HTTP客户端 - 移除StandardHttpRequestRetryHandler
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            
            return new DefaultPooledObject<>(httpClient);
        }
        
        @Override
        public void destroyObject(PooledObject<CloseableHttpClient> p) throws Exception {
            CloseableHttpClient httpClient = p.getObject();
            httpClient.close();
        }
        
        @Override
        public boolean validateObject(PooledObject<CloseableHttpClient> p) {
            try {
                CloseableHttpClient httpClient = p.getObject();
                CloseableHttpResponse response = httpClient.execute(new HttpGet("https://httpbin.org/status/200"));
                int statusCode = response.getCode();
                response.close();
                return statusCode == 200;
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public void activateObject(PooledObject<CloseableHttpClient> p) throws Exception {
            // 激活对象时的处理，这里不需要特殊处理
        }
        
        @Override
        public void passivateObject(PooledObject<CloseableHttpClient> p) throws Exception {
            // 钝化对象时的处理，这里不需要特殊处理
        }
    }

    /**
     * 数据库连接工厂类
     */
    private static class DatabaseConnectionFactory implements PooledObjectFactory<Connection> {
        private final ResourceFactoryImpl resourceFactory;

        public DatabaseConnectionFactory(ResourceFactoryImpl resourceFactory) {
            this.resourceFactory = resourceFactory;
        }

        @Override
        public PooledObject<Connection> makeObject() throws Exception {
            Connection connection = resourceFactory.createDatabaseConnection();
            return new DefaultPooledObject<>(connection);
        }

        @Override
        public void destroyObject(PooledObject<Connection> p) throws Exception {
            resourceFactory.destroyDatabaseConnection(p.getObject());
        }

        @Override
        public boolean validateObject(PooledObject<Connection> p) {
            return resourceFactory.validateDatabaseConnection(p.getObject());
        }

        @Override
        public void activateObject(PooledObject<Connection> p) throws Exception {
            // 激活连接时重置自动提交设置
            Connection conn = p.getObject();
            conn.setAutoCommit(false);
        }

        @Override
        public void passivateObject(PooledObject<Connection> p) throws Exception {
            // 钝化连接时回滚未提交的事务
            Connection conn = p.getObject();
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
        }
    }

    /**
     * 文件资源类
     */
    private static class FileResource {
        private File file;

        public File getFile() { return file; }
        public void setFile(File file) { this.file = file; }
        
        public void reset() {
            this.file = null;
        }
    }

    /**
     * 文件资源工厂类
     */
    private static class FileResourceFactory implements PooledObjectFactory<FileResource> {
        private final ResourceFactoryImpl resourceFactory;

        public FileResourceFactory(ResourceFactoryImpl resourceFactory) {
            this.resourceFactory = resourceFactory;
        }

        @Override
        public PooledObject<FileResource> makeObject() throws Exception {
            return new DefaultPooledObject<>(new FileResource());
        }

        @Override
        public void destroyObject(PooledObject<FileResource> p) throws Exception {
            // 文件资源不需要额外的销毁逻辑，因为文件句柄是单独管理的
            p.getObject().reset();
        }

        @Override
        public boolean validateObject(PooledObject<FileResource> p) {
            return p.getObject() != null;
        }

        @Override
        public void activateObject(PooledObject<FileResource> p) throws Exception {
            // 文件资源不需要额外激活
        }

        @Override
        public void passivateObject(PooledObject<FileResource> p) throws Exception {
            // 钝化时重置文件引用
            p.getObject().reset();
        }
    }

    /**
     * 运行资源池的并发测试
     */
    private static <T> void runConcurrentTests(ObjectPool<T> pool, int threadCount, int iterationsPerThread, ResourceTest<T> test) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        T resource = null;
                        try {
                            long startTime = System.currentTimeMillis();
                            // 移除超时参数，使用无参版本的borrowObject
                            resource = pool.borrowObject();
                            long borrowTime = System.currentTimeMillis() - startTime;
                            System.out.printf("Thread %d: Borrowed resource in %d ms\n", threadId, borrowTime);
                            
                            if (test.execute(resource)) {
                                successCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            if (e.getMessage().contains("Timeout")) {
                                timeoutCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                            System.err.printf("Thread %d: Error - %s\n", threadId, e.getMessage());
                        } finally {
                            if (resource != null) {
                                try {
                                    pool.returnObject(resource);
                                } catch (Exception e) {
                                    System.err.printf("Thread %d: Error returning resource - %s\n", threadId, e.getMessage());
                                }
                            }
                        }
                        
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    System.err.printf("Thread %d: Unhandled exception - %s\n", threadId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n=== Test Results ===");
        System.out.println("Success: " + successCount.get());
        System.out.println("Failure: " + failureCount.get());
        System.out.println("Timeout: " + timeoutCount.get());
        System.out.println("Total: " + (successCount.get() + failureCount.get() + timeoutCount.get()));
    }
    
    /**
     * 打印资源池统计信息
     */
    private static <T> void printPoolStats(ObjectPool<T> pool) {
        try {
            if (pool instanceof GenericObjectPool) {
                GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;
                System.out.println("\n=== Pool Statistics ===");
                System.out.println("Active Count: " + genericPool.getNumActive());
                System.out.println("Idle Count: " + genericPool.getNumIdle());
                System.out.println("Total Created: " + genericPool.getCreatedCount());
                System.out.println("Total Destroyed: " + genericPool.getDestroyedCount());
                System.out.println("Mean Active Time: " + genericPool.getMeanActiveTimeMillis() + " ms");
                System.out.println("Mean Idle Time: " + genericPool.getMeanIdleTimeMillis() + " ms");
                
                int maxTotal = genericPool.getMaxTotal();
                int active = genericPool.getNumActive();
                double utilization = maxTotal > 0 ? (double) active / maxTotal * 100 : 0;
                System.out.printf("Pool Utilization: %.2f%%\n", utilization);
            }
        } catch (Exception e) {
            System.err.println("Error getting pool statistics: " + e.getMessage());
        }
    }
    
    /**
     * 资源测试接口
     */
    @FunctionalInterface
    private interface ResourceTest<T> {
        boolean execute(T resource);
    }
}