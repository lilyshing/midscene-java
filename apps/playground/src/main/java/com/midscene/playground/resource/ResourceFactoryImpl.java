package com.midscene.playground.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 资源工厂实现类 - 提供各种资源的创建、验证和销毁功能
 * 支持HTTP客户端、数据库连接、文件句柄等资源的池化管理
 */
public class ResourceFactoryImpl extends ResourceFactory<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFactoryImpl.class);
    
    // HTTP客户端配置参数
    private static final int HTTP_MAX_TOTAL = 200;
    private static final int HTTP_MAX_PER_ROUTE = 50;
    private static final int HTTP_CONNECTION_TIMEOUT_MS = 10000;
    private static final int HTTP_SOCKET_TIMEOUT_MS = 30000;
    private static final int HTTP_CONNECTION_REQUEST_TIMEOUT_MS = 5000;
    private static final int HTTP_VALIDATE_AFTER_INACTIVITY_MS = 2000;
    
    // 数据库配置参数
    private static final Map<String, String> DB_CONFIGS = new ConcurrentHashMap<>();
    
    // SSL上下文（用于HTTPS连接）
    private static volatile SSLContext sslContext;
    
    // 资源使用统计
    private final Map<String, ResourceMetrics> resourceMetrics = new ConcurrentHashMap<>();

    static {
        // 初始化数据库配置
        initDbConfigs();
    }

    public ResourceFactoryImpl() {
        // 初始化资源使用统计
        initResourceMetrics();
    }

    /**
     * 初始化数据库配置
     */
    private static void initDbConfigs() {
        // 默认数据库配置
        DB_CONFIGS.put("jdbc.url", "jdbc:h2:mem:playground;DB_CLOSE_DELAY=-1");
        DB_CONFIGS.put("jdbc.username", "sa");
        DB_CONFIGS.put("jdbc.password", "");
        DB_CONFIGS.put("jdbc.driver", "org.h2.Driver");
        
        // 尝试从系统属性或环境变量覆盖配置
        try {
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                if (entry.getKey().startsWith("JDBC_")) {
                    String key = "jdbc." + entry.getKey().substring(5).toLowerCase();
                    DB_CONFIGS.put(key, entry.getValue());
                }
            }
            
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith("jdbc.")) {
                    DB_CONFIGS.put((String)entry.getKey(), (String)entry.getValue());
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to load database configs from environment: {}", e.getMessage());
        }
    }

    /**
     * 初始化资源使用统计
     */
    private void initResourceMetrics() {
        resourceMetrics.put("http.client", new ResourceMetrics());
        resourceMetrics.put("db.connection", new ResourceMetrics());
        resourceMetrics.put("file.input", new ResourceMetrics());
        resourceMetrics.put("file.output", new ResourceMetrics());
    }

    /**
     * 获取SSL上下文，支持自签名证书
     */
    private static SSLContext getSslContext() throws KeyManagementException, NoSuchAlgorithmException {
        if (sslContext == null) {
            synchronized (ResourceFactoryImpl.class) {
                if (sslContext == null) {
                    // 创建信任所有证书的SSL上下文（用于测试环境）
                    TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                    };
                    
                    sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                }
            }
        }
        return sslContext;
    }

    /**
     * 创建HTTP客户端
     */
    public CloseableHttpClient createHttpClient() {
        try {
            // 配置HTTP客户端
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(5000))
                    .setResponseTimeout(Timeout.ofMilliseconds(5000))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                    .build();
            
            // 创建连接管理器
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(20);
            connectionManager.setDefaultMaxPerRoute(10);
            
            // 创建HTTP客户端
            CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(config)
                    .build();
            
            // 更新资源统计
            ResourceMetrics metrics = resourceMetrics.get("http.client");
            if (metrics != null) {
                metrics.incrementCreated();
                metrics.incrementActive();
            }
            
            return client;
        } catch (Exception e) {
            logger.error("Failed to create HTTP client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create HTTP client", e);
        }
    }

    /**
     * 创建数据库连接
     */
    public Connection createDatabaseConnection() {
        try {
            logger.debug("Creating new database connection");
            
            // 加载驱动类
            Class.forName(DB_CONFIGS.get("jdbc.driver"));
            
            // 创建连接
            Connection connection = DriverManager.getConnection(
                DB_CONFIGS.get("jdbc.url"),
                DB_CONFIGS.get("jdbc.username"),
                DB_CONFIGS.get("jdbc.password")
            );
            
            // 设置连接参数
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            // 更新统计信息
            updateResourceMetrics("db.connection", true);
            
            return connection;
            
        } catch (Exception e) {
            logger.error("Failed to create database connection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    /**
     * 创建文件输入流
     */
    public InputStream createFileInputStream(File file) {
        try {
            logger.debug("Creating new FileInputStream for: {}", file.getAbsolutePath());
            
            // 检查文件是否存在
            if (!file.exists()) {
                throw new IOException("File not found: " + file.getAbsolutePath());
            }
            
            // 创建文件输入流
            InputStream inputStream = new FileInputStream(file);
            
            // 更新统计信息
            updateResourceMetrics("file.input", true);
            
            return inputStream;
            
        } catch (IOException e) {
            logger.error("Failed to create FileInputStream for {}: {}", 
                    file.getAbsolutePath(), e.getMessage(), e);
            throw new RuntimeException("Failed to create FileInputStream", e);
        }
    }

    /**
     * 创建文件输出流
     */
    public OutputStream createFileOutputStream(File file) {
        try {
            logger.debug("Creating new FileOutputStream for: {}", file.getAbsolutePath());
            
            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 创建文件输出流
            OutputStream outputStream = new FileOutputStream(file, false); // 覆盖模式
            
            // 更新统计信息
            updateResourceMetrics("file.output", true);
            
            return outputStream;
            
        } catch (IOException e) {
            logger.error("Failed to create FileOutputStream for {}: {}", 
                    file.getAbsolutePath(), e.getMessage(), e);
            throw new RuntimeException("Failed to create FileOutputStream", e);
        }
    }

    /**
     * 验证HTTP客户端是否有效
     */
    public boolean validateHttpClient(CloseableHttpClient client) {
        try {
            if (client == null) {
                return false;
            }
            
            // 对于CloseableHttpClient，这里简单返回true
            // 实际生产环境中可能需要更复杂的验证逻辑
            return true;
        } catch (Exception e) {
            logger.warn("HttpClient validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证数据库连接是否有效
     */
    public boolean validateDatabaseConnection(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            
            // 执行简单的查询来验证连接
            connection.createStatement().execute("SELECT 1");
            return true;
            
        } catch (SQLException e) {
            logger.warn("Database connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证输入流是否有效
     */
    public boolean validateInputStream(InputStream inputStream) {
        try {
            if (inputStream == null) {
                return false;
            }
            
            // 对于FileInputStream，检查是否已关闭
            if (inputStream instanceof FileInputStream) {
                // 尝试获取文件描述符来验证
                return true; // 在Java中无法直接检查是否关闭，需要通过状态跟踪
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warn("InputStream validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证输出流是否有效
     */
    public boolean validateOutputStream(OutputStream outputStream) {
        try {
            if (outputStream == null) {
                return false;
            }
            
            // 对于FileOutputStream，检查是否已关闭
            if (outputStream instanceof FileOutputStream) {
                // 在Java中无法直接检查是否关闭，需要通过状态跟踪
                return true;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warn("OutputStream validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 销毁HTTP客户端
     */
    public void destroyHttpClient(CloseableHttpClient client) {
        try {
            if (client != null) {
                logger.debug("Destroying HttpClient instance");
                client.close();
                
                // 更新统计信息
                updateResourceMetrics("http.client", false);
            }
        } catch (Exception e) {
            logger.error("Failed to destroy HttpClient: {}", e.getMessage(), e);
        }
    }

    /**
     * 销毁数据库连接
     */
    public void destroyDatabaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                logger.debug("Destroying database connection");
                connection.close();
                
                // 更新统计信息
                updateResourceMetrics("db.connection", false);
            }
        } catch (SQLException e) {
            logger.error("Failed to destroy database connection: {}", e.getMessage(), e);
        }
    }

    /**
     * 销毁输入流
     */
    public void destroyInputStream(InputStream inputStream) {
        try {
            if (inputStream != null && inputStream instanceof Closeable) {
                logger.debug("Destroying InputStream");
                ((Closeable) inputStream).close();
                
                // 更新统计信息
                updateResourceMetrics("file.input", false);
            }
        } catch (IOException e) {
            logger.error("Failed to destroy InputStream: {}", e.getMessage(), e);
        }
    }

    /**
     * 销毁输出流
     */
    public void destroyOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null && outputStream instanceof Closeable) {
                logger.debug("Destroying OutputStream");
                ((Closeable) outputStream).close();
                
                // 更新统计信息
                updateResourceMetrics("file.output", false);
            }
        } catch (IOException e) {
            logger.error("Failed to destroy OutputStream: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新资源使用统计
     */
    private void updateResourceMetrics(String resourceType, boolean isCreation) {
        ResourceMetrics metrics = resourceMetrics.get(resourceType);
        if (metrics != null) {
            if (isCreation) {
                metrics.incrementCreated();
                metrics.incrementActive();
            } else {
                metrics.incrementDestroyed();
                metrics.decrementActive();
            }
        }
    }

    /**
     * 获取资源使用统计
     */
    public Map<String, ResourceMetrics> getResourceMetrics() {
        return new ConcurrentHashMap<>(resourceMetrics);
    }

    /**
     * 资源使用统计类
     */
    public static class ResourceMetrics {
        private final AtomicLong created = new AtomicLong(0);
        private final AtomicLong destroyed = new AtomicLong(0);
        private final AtomicLong active = new AtomicLong(0);
        private final AtomicLong validationFailures = new AtomicLong(0);
        private final AtomicLong creationTime = new AtomicLong(System.currentTimeMillis());

        public long getCreated() { return created.get(); }
        public long getDestroyed() { return destroyed.get(); }
        public long getActive() { return active.get(); }
        public long getValidationFailures() { return validationFailures.get(); }
        public long getCreationTime() { return creationTime.get(); }

        public void incrementCreated() { created.incrementAndGet(); }
        public void incrementDestroyed() { destroyed.incrementAndGet(); }
        public void incrementActive() { active.incrementAndGet(); }
        public void decrementActive() { 
            long newValue = active.decrementAndGet();
            if (newValue < 0) {
                active.set(0); // 防止负数
                logger.warn("ResourceMetrics: Active count went negative");
            }
        }
        public void incrementValidationFailures() { validationFailures.incrementAndGet(); }

        @Override
        public String toString() {
            return String.format("ResourceMetrics{created=%d, destroyed=%d, active=%d, failures=%d}",
                    created.get(), destroyed.get(), active.get(), validationFailures.get());
        }
    }

    /**
     * 关闭所有资源的静态方法
     */
    public static void closeQuietly(Object resource) {
        if (resource instanceof Closeable) {
            try {
                ((Closeable) resource).close();
            } catch (IOException e) {
                // 静默关闭，不记录错误
            }
        }
    }

    /**
     * 获取当前资源使用摘要
     */
    public String getResourceUsageSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resource Usage Summary:\n");
        
        for (Map.Entry<String, ResourceMetrics> entry : resourceMetrics.entrySet()) {
            ResourceMetrics metrics = entry.getValue();
            sb.append(String.format("- %s: %d active, %d created, %d destroyed\n",
                    entry.getKey(), metrics.getActive(), metrics.getCreated(), metrics.getDestroyed()));
        }
        
        return sb.toString();
    }

    /**
     * 设置数据库配置
     */
    public static void setDatabaseConfig(String key, String value) {
        DB_CONFIGS.put(key, value);
    }

    /**
     * 获取数据库配置
     */
    public static String getDatabaseConfig(String key) {
        return DB_CONFIGS.get(key);
    }

    /**
     * 从属性文件加载数据库配置
     */
    public static void loadDatabaseConfigFromProperties(Properties properties) {
        if (properties != null) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith("jdbc.")) {
                    DB_CONFIGS.put((String)entry.getKey(), (String)entry.getValue());
                }
            }
            logger.info("Loaded database config from properties");
        }
    }

    /**
     * 实现资源工厂的创建方法
     */
    @Override
    protected Object doCreate() throws Exception {
        // 默认创建方法 - 实际应用中可能需要根据类型参数化
        throw new UnsupportedOperationException("Use specific create methods instead");
    }

    /**
     * 实现资源工厂的验证方法
     */
    @Override
    protected boolean doValidate(Object resource) {
        // 默认验证方法 - 实际应用中可能需要根据资源类型进行验证
        return resource != null;
    }

    /**
     * 实现资源工厂的销毁方法
     */
    @Override
    protected void doDestroy(Object resource) {
        // 默认销毁方法 - 实际应用中可能需要根据资源类型进行适当的销毁
        if (resource != null) {
            logger.debug("Destroying generic resource: {}", resource.getClass().getName());
        }
    }
}