package com.midscene.playground.resource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 资源池化配置类 - 统一管理各种资源池的配置参数
 * 支持HTTP连接池、数据库连接池、文件句柄池等配置
 */
public class ResourcePoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePoolConfig.class);
    
    // 资源池类型枚举
    public enum PoolType {
        HTTP_CLIENT,       // HTTP客户端连接池
        DATABASE_CONNECTION, // 数据库连接池
        FILE_INPUT_STREAM,  // 文件输入流池
        FILE_OUTPUT_STREAM, // 文件输出流池
        CUSTOM              // 自定义资源池
    }
    
    // 全局默认配置
    private static final ResourcePoolConfig DEFAULT_CONFIG = new ResourcePoolConfig();
    
    // 类型特定的配置映射
    private static final Map<PoolType, ResourcePoolConfig> TYPE_CONFIGS = new ConcurrentHashMap<>();
    
    // 池配置参数
    private int maxTotal = 8;                    // 最大连接数
    private int maxIdle = 8;                     // 最大空闲连接数
    private int minIdle = 0;                     // 最小空闲连接数
    private boolean lifo = true;                 // 后进先出策略
    private boolean fairness = false;            // 是否公平获取资源
    private long maxWaitMillis = -1L;            // 最大等待时间（-1表示无限等待）
    private long minEvictableIdleTimeMillis = 60000L; // 可驱逐的最小空闲时间
    private long softMinEvictableIdleTimeMillis = -1L; // 软可驱逐空闲时间
    private int numTestsPerEvictionRun = 3;      // 每次驱逐检查的数量
    private boolean testOnCreate = false;        // 创建时测试
    private boolean testOnBorrow = true;         // 借用时测试
    private boolean testOnReturn = false;        // 归还时测试
    private boolean testWhileIdle = true;        // 空闲时测试
    private long timeBetweenEvictionRunsMillis = 30000L; // 驱逐运行间隔
    private long evictorShutdownTimeoutMillis = 10000L; // 驱逐器关闭超时
    private boolean blockWhenExhausted = true;   // 资源耗尽时是否阻塞
    private long durationBetweenEvictionRuns = 30; // 驱逐运行间隔（秒）
    
    // 动态调整配置
    private boolean enableDynamicScaling = true; // 是否启用动态扩展
    private double targetUtilization = 0.75;     // 目标利用率
    private double minUtilizationThreshold = 0.3; // 最小利用率阈值
    private double maxUtilizationThreshold = 0.9; // 最大利用率阈值
    private int maxScaleUpAmount = 10;           // 最大扩容数量
    private int maxScaleDownAmount = 5;          // 最大缩容数量
    private long scalingIntervalSeconds = 60;    // 动态调整间隔
    
    // 资源验证配置
    private int validationTimeoutMillis = 5000;  // 验证超时时间
    private boolean validateConnections = true;  // 是否验证连接
    
    // 监控配置
    private boolean enableMetrics = true;        // 是否启用指标收集
    private long metricsCollectionIntervalSeconds = 30; // 指标收集间隔
    
    static {
        // 初始化类型特定的配置
        initTypeConfigs();
    }

    /**
     * 初始化类型特定的配置
     */
    private static void initTypeConfigs() {
        // HTTP客户端连接池配置
        ResourcePoolConfig httpConfig = new ResourcePoolConfig();
        httpConfig.setMaxTotal(50);
        httpConfig.setMaxIdle(25);
        httpConfig.setMinIdle(5);
        httpConfig.setMaxWaitMillis(10000L);
        httpConfig.setMinEvictableIdleTimeMillis(120000L);
        httpConfig.setTestOnBorrow(true);
        httpConfig.setTestWhileIdle(true);
        httpConfig.setTimeBetweenEvictionRunsMillis(60000L);
        httpConfig.setTargetUtilization(0.8);
        TYPE_CONFIGS.put(PoolType.HTTP_CLIENT, httpConfig);
        
        // 数据库连接池配置
        ResourcePoolConfig dbConfig = new ResourcePoolConfig();
        dbConfig.setMaxTotal(20);
        dbConfig.setMaxIdle(10);
        dbConfig.setMinIdle(5);
        dbConfig.setMaxWaitMillis(30000L);
        dbConfig.setMinEvictableIdleTimeMillis(180000L);
        dbConfig.setTestOnBorrow(true);
        dbConfig.setTestOnReturn(false);
        dbConfig.setTestWhileIdle(true);
        dbConfig.setTimeBetweenEvictionRunsMillis(60000L);
        dbConfig.setTargetUtilization(0.7);
        TYPE_CONFIGS.put(PoolType.DATABASE_CONNECTION, dbConfig);
        
        // 文件输入流池配置
        ResourcePoolConfig fileInConfig = new ResourcePoolConfig();
        fileInConfig.setMaxTotal(50);
        fileInConfig.setMaxIdle(10);
        fileInConfig.setMinIdle(0);
        fileInConfig.setMaxWaitMillis(5000L);
        fileInConfig.setMinEvictableIdleTimeMillis(30000L);
        fileInConfig.setTestOnBorrow(false);
        fileInConfig.setTestOnReturn(false);
        fileInConfig.setTestWhileIdle(false);
        fileInConfig.setEnableDynamicScaling(false);
        TYPE_CONFIGS.put(PoolType.FILE_INPUT_STREAM, fileInConfig);
        
        // 文件输出流池配置
        ResourcePoolConfig fileOutConfig = new ResourcePoolConfig();
        fileOutConfig.setMaxTotal(30);
        fileOutConfig.setMaxIdle(5);
        fileOutConfig.setMinIdle(0);
        fileOutConfig.setMaxWaitMillis(3000L);
        fileOutConfig.setMinEvictableIdleTimeMillis(15000L);
        fileOutConfig.setTestOnBorrow(false);
        fileOutConfig.setTestOnReturn(false);
        fileOutConfig.setTestWhileIdle(false);
        fileOutConfig.setEnableDynamicScaling(false);
        TYPE_CONFIGS.put(PoolType.FILE_OUTPUT_STREAM, fileOutConfig);
    }

    /**
     * 获取默认配置
     */
    public static ResourcePoolConfig getDefaultConfig() {
        return DEFAULT_CONFIG.clone();
    }

    /**
     * 获取特定类型的配置
     */
    public static ResourcePoolConfig getConfig(PoolType type) {
        ResourcePoolConfig config = TYPE_CONFIGS.get(type);
        return (config != null) ? config.clone() : DEFAULT_CONFIG.clone();
    }

    /**
     * 更新特定类型的配置
     */
    public static void updateConfig(PoolType type, ResourcePoolConfig config) {
        if (config != null) {
            TYPE_CONFIGS.put(type, config.clone());
            logger.info("Updated configuration for pool type: {}", type);
        }
    }

    /**
     * 从属性文件加载配置
     */
    public static void loadConfigFromProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        
        try {
            // 加载默认配置
            DEFAULT_CONFIG.loadFromProperties(properties, "resource.pool.default.");
            
            // 加载类型特定配置
            for (PoolType type : PoolType.values()) {
                if (type != PoolType.CUSTOM) {
                    String prefix = "resource.pool." + type.name().toLowerCase().replace('_', '.') + ".";
                    ResourcePoolConfig config = TYPE_CONFIGS.get(type);
                    if (config != null) {
                        config.loadFromProperties(properties, prefix);
                    }
                }
            }
            
            logger.info("Loaded resource pool configurations from properties");
            
        } catch (Exception e) {
            logger.error("Failed to load resource pool configurations: {}", e.getMessage(), e);
        }
    }

    /**
     * 从系统属性加载配置
     */
    public static void loadConfigFromSystemProperties() {
        Properties properties = System.getProperties();
        loadConfigFromProperties(properties);
    }

    /**
     * 从属性对象加载配置（针对特定前缀）
     */
    private void loadFromProperties(Properties properties, String prefix) {
        // 基本池配置
        this.maxTotal = getIntProperty(properties, prefix + "max.total", this.maxTotal);
        this.maxIdle = getIntProperty(properties, prefix + "max.idle", this.maxIdle);
        this.minIdle = getIntProperty(properties, prefix + "min.idle", this.minIdle);
        this.lifo = getBooleanProperty(properties, prefix + "lifo", this.lifo);
        this.fairness = getBooleanProperty(properties, prefix + "fairness", this.fairness);
        this.maxWaitMillis = getLongProperty(properties, prefix + "max.wait", this.maxWaitMillis);
        
        // 驱逐配置
        this.minEvictableIdleTimeMillis = getLongProperty(properties, 
                prefix + "min.evictable.idle.time", this.minEvictableIdleTimeMillis);
        this.softMinEvictableIdleTimeMillis = getLongProperty(properties, 
                prefix + "soft.min.evictable.idle.time", this.softMinEvictableIdleTimeMillis);
        this.numTestsPerEvictionRun = getIntProperty(properties, 
                prefix + "num.tests.per.eviction.run", this.numTestsPerEvictionRun);
        
        // 测试配置
        this.testOnCreate = getBooleanProperty(properties, prefix + "test.on.create", this.testOnCreate);
        this.testOnBorrow = getBooleanProperty(properties, prefix + "test.on.borrow", this.testOnBorrow);
        this.testOnReturn = getBooleanProperty(properties, prefix + "test.on.return", this.testOnReturn);
        this.testWhileIdle = getBooleanProperty(properties, prefix + "test.while.idle", this.testWhileIdle);
        this.timeBetweenEvictionRunsMillis = getLongProperty(properties, 
                prefix + "time.between.eviction.runs", this.timeBetweenEvictionRunsMillis);
        
        // 动态调整配置
        this.enableDynamicScaling = getBooleanProperty(properties, 
                prefix + "dynamic.scaling.enabled", this.enableDynamicScaling);
        this.targetUtilization = getDoubleProperty(properties, 
                prefix + "target.utilization", this.targetUtilization);
        this.minUtilizationThreshold = getDoubleProperty(properties, 
                prefix + "min.utilization.threshold", this.minUtilizationThreshold);
        this.maxUtilizationThreshold = getDoubleProperty(properties, 
                prefix + "max.utilization.threshold", this.maxUtilizationThreshold);
        this.maxScaleUpAmount = getIntProperty(properties, 
                prefix + "max.scale.up.amount", this.maxScaleUpAmount);
        this.maxScaleDownAmount = getIntProperty(properties, 
                prefix + "max.scale.down.amount", this.maxScaleDownAmount);
        this.scalingIntervalSeconds = getLongProperty(properties, 
                prefix + "scaling.interval", this.scalingIntervalSeconds);
        
        // 监控配置
        this.enableMetrics = getBooleanProperty(properties, 
                prefix + "metrics.enabled", this.enableMetrics);
        this.metricsCollectionIntervalSeconds = getLongProperty(properties, 
                prefix + "metrics.interval", this.metricsCollectionIntervalSeconds);
    }

    /**
     * 获取整数属性
     */
    private static int getIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value '{}' for property '{}', using default: {}", 
                        value, key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取长整型属性
     */
    private static long getLongProperty(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value '{}' for property '{}', using default: {}", 
                        value, key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取双精度属性
     */
    private static double getDoubleProperty(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value '{}' for property '{}', using default: {}", 
                        value, key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔属性
     */
    private static boolean getBooleanProperty(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }

    /**
     * 转换为Apache Commons Pool2的配置
     */
    public GenericObjectPoolConfig<?> toGenericObjectPoolConfig() {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setLifo(lifo);
        config.setFairness(fairness);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
        config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        config.setTestOnCreate(testOnCreate);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setTestWhileIdle(testWhileIdle);
        config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        config.setEvictorShutdownTimeoutMillis(evictorShutdownTimeoutMillis);
        config.setBlockWhenExhausted(blockWhenExhausted);
        
        return config;
    }

    /**
     * 克隆配置对象
     */
    @Override
    public ResourcePoolConfig clone() {
        ResourcePoolConfig cloned = new ResourcePoolConfig();
        
        // 基本池配置
        cloned.maxTotal = this.maxTotal;
        cloned.maxIdle = this.maxIdle;
        cloned.minIdle = this.minIdle;
        cloned.lifo = this.lifo;
        cloned.fairness = this.fairness;
        cloned.maxWaitMillis = this.maxWaitMillis;
        
        // 驱逐配置
        cloned.minEvictableIdleTimeMillis = this.minEvictableIdleTimeMillis;
        cloned.softMinEvictableIdleTimeMillis = this.softMinEvictableIdleTimeMillis;
        cloned.numTestsPerEvictionRun = this.numTestsPerEvictionRun;
        
        // 测试配置
        cloned.testOnCreate = this.testOnCreate;
        cloned.testOnBorrow = this.testOnBorrow;
        cloned.testOnReturn = this.testOnReturn;
        cloned.testWhileIdle = this.testWhileIdle;
        cloned.timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRunsMillis;
        cloned.evictorShutdownTimeoutMillis = this.evictorShutdownTimeoutMillis;
        cloned.blockWhenExhausted = this.blockWhenExhausted;
        cloned.durationBetweenEvictionRuns = this.durationBetweenEvictionRuns;
        
        // 动态调整配置
        cloned.enableDynamicScaling = this.enableDynamicScaling;
        cloned.targetUtilization = this.targetUtilization;
        cloned.minUtilizationThreshold = this.minUtilizationThreshold;
        cloned.maxUtilizationThreshold = this.maxUtilizationThreshold;
        cloned.maxScaleUpAmount = this.maxScaleUpAmount;
        cloned.maxScaleDownAmount = this.maxScaleDownAmount;
        cloned.scalingIntervalSeconds = this.scalingIntervalSeconds;
        
        // 资源验证配置
        cloned.validationTimeoutMillis = this.validationTimeoutMillis;
        cloned.validateConnections = this.validateConnections;
        
        // 监控配置
        cloned.enableMetrics = this.enableMetrics;
        cloned.metricsCollectionIntervalSeconds = this.metricsCollectionIntervalSeconds;
        
        return cloned;
    }

    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResourcePoolConfig Summary:\n");
        sb.append(String.format("  - Pool Size: %d (min) - %d (max), Max Idle: %d\n", 
                minIdle, maxTotal, maxIdle));
        sb.append(String.format("  - Eviction: every %dms, Min Idle Time: %dms\n", 
                timeBetweenEvictionRunsMillis, minEvictableIdleTimeMillis));
        sb.append(String.format("  - Testing: create=%s, borrow=%s, return=%s, idle=%s\n", 
                testOnCreate, testOnBorrow, testOnReturn, testWhileIdle));
        sb.append(String.format("  - Dynamic Scaling: %s (target: %.2f, interval: %ds)\n", 
                enableDynamicScaling, targetUtilization, scalingIntervalSeconds));
        sb.append(String.format("  - Metrics: %s (interval: %ds)\n", 
                enableMetrics, metricsCollectionIntervalSeconds));
        return sb.toString();
    }

    // Getters and Setters
    public int getMaxTotal() { return maxTotal; }
    public void setMaxTotal(int maxTotal) { this.maxTotal = maxTotal; }
    
    public int getMaxIdle() { return maxIdle; }
    public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }
    
    public int getMinIdle() { return minIdle; }
    public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
    
    public boolean isLifo() { return lifo; }
    public void setLifo(boolean lifo) { this.lifo = lifo; }
    
    public boolean isFairness() { return fairness; }
    public void setFairness(boolean fairness) { this.fairness = fairness; }
    
    public long getMaxWaitMillis() { return maxWaitMillis; }
    public void setMaxWaitMillis(long maxWaitMillis) { this.maxWaitMillis = maxWaitMillis; }
    
    public long getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) { 
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis; 
    }
    
    public long getSoftMinEvictableIdleTimeMillis() { return softMinEvictableIdleTimeMillis; }
    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) { 
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis; 
    }
    
    public int getNumTestsPerEvictionRun() { return numTestsPerEvictionRun; }
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) { 
        this.numTestsPerEvictionRun = numTestsPerEvictionRun; 
    }
    
    public boolean isTestOnCreate() { return testOnCreate; }
    public void setTestOnCreate(boolean testOnCreate) { this.testOnCreate = testOnCreate; }
    
    public boolean isTestOnBorrow() { return testOnBorrow; }
    public void setTestOnBorrow(boolean testOnBorrow) { this.testOnBorrow = testOnBorrow; }
    
    public boolean isTestOnReturn() { return testOnReturn; }
    public void setTestOnReturn(boolean testOnReturn) { this.testOnReturn = testOnReturn; }
    
    public boolean isTestWhileIdle() { return testWhileIdle; }
    public void setTestWhileIdle(boolean testWhileIdle) { this.testWhileIdle = testWhileIdle; }
    
    public long getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) { 
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis; 
    }
    
    public long getEvictorShutdownTimeoutMillis() { return evictorShutdownTimeoutMillis; }
    public void setEvictorShutdownTimeoutMillis(long evictorShutdownTimeoutMillis) { 
        this.evictorShutdownTimeoutMillis = evictorShutdownTimeoutMillis; 
    }
    
    public boolean isBlockWhenExhausted() { return blockWhenExhausted; }
    public void setBlockWhenExhausted(boolean blockWhenExhausted) { 
        this.blockWhenExhausted = blockWhenExhausted; 
    }
    
    public boolean isEnableDynamicScaling() { return enableDynamicScaling; }
    public void setEnableDynamicScaling(boolean enableDynamicScaling) { 
        this.enableDynamicScaling = enableDynamicScaling; 
    }
    
    public double getTargetUtilization() { return targetUtilization; }
    public void setTargetUtilization(double targetUtilization) { 
        this.targetUtilization = targetUtilization; 
    }
    
    public double getMinUtilizationThreshold() { return minUtilizationThreshold; }
    public void setMinUtilizationThreshold(double minUtilizationThreshold) { 
        this.minUtilizationThreshold = minUtilizationThreshold; 
    }
    
    public double getMaxUtilizationThreshold() { return maxUtilizationThreshold; }
    public void setMaxUtilizationThreshold(double maxUtilizationThreshold) { 
        this.maxUtilizationThreshold = maxUtilizationThreshold; 
    }
    
    public int getMaxScaleUpAmount() { return maxScaleUpAmount; }
    public void setMaxScaleUpAmount(int maxScaleUpAmount) { 
        this.maxScaleUpAmount = maxScaleUpAmount; 
    }
    
    public int getMaxScaleDownAmount() { return maxScaleDownAmount; }
    public void setMaxScaleDownAmount(int maxScaleDownAmount) { 
        this.maxScaleDownAmount = maxScaleDownAmount; 
    }
    
    public long getScalingIntervalSeconds() { return scalingIntervalSeconds; }
    public void setScalingIntervalSeconds(long scalingIntervalSeconds) { 
        this.scalingIntervalSeconds = scalingIntervalSeconds; 
    }
    
    public int getValidationTimeoutMillis() { return validationTimeoutMillis; }
    public void setValidationTimeoutMillis(int validationTimeoutMillis) { 
        this.validationTimeoutMillis = validationTimeoutMillis; 
    }
    
    public boolean isValidateConnections() { return validateConnections; }
    public void setValidateConnections(boolean validateConnections) { 
        this.validateConnections = validateConnections; 
    }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { 
        this.enableMetrics = enableMetrics; 
    }
    
    public long getMetricsCollectionIntervalSeconds() { return metricsCollectionIntervalSeconds; }
    public void setMetricsCollectionIntervalSeconds(long metricsCollectionIntervalSeconds) { 
        this.metricsCollectionIntervalSeconds = metricsCollectionIntervalSeconds; 
    }
}