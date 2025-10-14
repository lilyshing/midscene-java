package com.midscene.androidplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置类，用于管理Android Playground的配置信息
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    
    // 默认配置值
    private static final int DEFAULT_ADB_SERVER_PORT = 5037;
    private static final String DEFAULT_ADB_PATH = "adb";
    private static final int DEFAULT_DEVICE_POLLING_INTERVAL = 5000; // 5秒
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000; // 30秒
    
    // 配置项
    private final Map<String, Object> configMap;
    
    /**
     * 创建默认配置
     */
    public Configuration() {
        this.configMap = new HashMap<>();
        setDefaults();
        logger.info("Configuration initialized with default values");
    }
    
    /**
     * 从Properties创建配置
     * @param properties Properties对象
     */
    public Configuration(Properties properties) {
        this.configMap = new HashMap<>();
        setDefaults();
        
        // 从Properties加载配置
        if (properties != null) {
            for (String key : properties.stringPropertyNames()) {
                configMap.put(key, properties.getProperty(key));
            }
        }
        
        logger.info("Configuration initialized from properties");
    }
    
    /**
     * 设置默认配置值
     */
    private void setDefaults() {
        setAdbServerPort(DEFAULT_ADB_SERVER_PORT);
        setAdbPath(DEFAULT_ADB_PATH);
        setDevicePollingInterval(DEFAULT_DEVICE_POLLING_INTERVAL);
        setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
    }
    
    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = configMap.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    public void set(String key, Object value) {
        configMap.put(key, value);
        logger.debug("Set config: {} = {}", key, value);
    }
    
    /**
     * 获取ADB服务器端口
     * @return ADB服务器端口
     */
    public int getAdbServerPort() {
        return get("adb.server.port", DEFAULT_ADB_SERVER_PORT);
    }
    
    /**
     * 设置ADB服务器端口
     * @param port 端口号
     */
    public void setAdbServerPort(int port) {
        set("adb.server.port", port);
    }
    
    /**
     * 获取ADB可执行文件路径
     * @return ADB路径
     */
    public String getAdbPath() {
        return get("adb.path", DEFAULT_ADB_PATH);
    }
    
    /**
     * 设置ADB可执行文件路径
     * @param path ADB路径
     */
    public void setAdbPath(String path) {
        set("adb.path", path);
    }
    
    /**
     * 获取设备轮询间隔（毫秒）
     * @return 轮询间隔
     */
    public int getDevicePollingInterval() {
        return get("device.polling.interval", DEFAULT_DEVICE_POLLING_INTERVAL);
    }
    
    /**
     * 设置设备轮询间隔（毫秒）
     * @param interval 轮询间隔
     */
    public void setDevicePollingInterval(int interval) {
        set("device.polling.interval", interval);
    }
    
    /**
     * 获取连接超时时间（毫秒）
     * @return 超时时间
     */
    public int getConnectionTimeout() {
        return get("connection.timeout", DEFAULT_CONNECTION_TIMEOUT);
    }
    
    /**
     * 设置连接超时时间（毫秒）
     * @param timeout 超时时间
     */
    public void setConnectionTimeout(int timeout) {
        set("connection.timeout", timeout);
    }
    
    /**
     * 检查是否启用调试模式
     * @return 如果启用调试模式，返回 true
     */
    public boolean isDebugEnabled() {
        return get("debug.enabled", false);
    }
    
    /**
     * 启用或禁用调试模式
     * @param enabled 是否启用
     */
    public void setDebugEnabled(boolean enabled) {
        set("debug.enabled", enabled);
    }
    
    /**
     * 将配置转换为Properties对象
     * @return Properties对象
     */
    public Properties toProperties() {
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue().toString());
        }
        return properties;
    }
    
    @Override
    public String toString() {
        return "Configuration{size=" + configMap.size() + "}";
    }
}