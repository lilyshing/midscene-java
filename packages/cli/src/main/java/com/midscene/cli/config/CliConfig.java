package com.midscene.cli.config;

import com.midscene.cli.exception.CliException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * CLI配置管理类
 * 负责加载、保存和访问CLI配置信息
 */
public class CliConfig {
    private static final String CONFIG_FILE_NAME = "midscene.config.json";
    private static final String USER_CONFIG_DIR = ".midscene";
    
    private final Map<String, String> properties = new HashMap<>();
    private final String configFilePath;
    private boolean verbose = false;
    
    /**
     * 构造函数
     */
    private CliConfig(String configFilePath) {
        this.configFilePath = configFilePath;
        loadFromFile();
    }
    
    /**
     * 加载配置
     */
    public static CliConfig load() {
        // 首先尝试从当前目录加载配置
        Path currentConfigPath = Paths.get(CONFIG_FILE_NAME);
        if (Files.exists(currentConfigPath)) {
            return new CliConfig(currentConfigPath.toString());
        }
        
        // 然后尝试从用户目录加载配置
        String userHome = System.getProperty("user.home");
        Path userConfigPath = Paths.get(userHome, USER_CONFIG_DIR, CONFIG_FILE_NAME);
        if (Files.exists(userConfigPath)) {
            return new CliConfig(userConfigPath.toString());
        }
        
        // 如果配置文件不存在，创建默认配置并保存到用户目录
        try {
            Files.createDirectories(Paths.get(userHome, USER_CONFIG_DIR));
        } catch (IOException e) {
            throw new CliException("无法创建配置目录", e);
        }
        
        CliConfig config = new CliConfig(userConfigPath.toString());
        config.resetToDefaults();
        try {
            config.save();
        } catch (IOException e) {
            // 静默失败，只记录警告
            System.err.println("警告: 无法保存默认配置文件: " + e.getMessage());
        }
        
        return config;
    }
    
    /**
     * 从文件加载配置
     */
    private void loadFromFile() {
        File configFile = new File(configFilePath);
        if (!configFile.exists() || !configFile.isFile()) {
            resetToDefaults();
            return;
        }
        
        try (InputStream input = new FileInputStream(configFile)) {
            Properties props = new Properties();
            props.load(input);
            
            // 清空现有属性并加载新属性
            properties.clear();
            for (String key : props.stringPropertyNames()) {
                properties.put(key, props.getProperty(key));
            }
            
        } catch (IOException e) {
            // 加载失败，使用默认配置
            resetToDefaults();
            System.err.println("警告: 无法加载配置文件，使用默认配置: " + e.getMessage());
        }
    }
    
    /**
     * 保存配置到文件
     */
    public void save() throws IOException {
        // 确保父目录存在
        File configFile = new File(configFilePath);
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            Files.createDirectories(parentDir.toPath());
        }
        
        try (OutputStream output = new FileOutputStream(configFile)) {
            Properties props = new Properties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            props.store(output, "MidScene CLI Configuration");
        }
    }
    
    /**
     * 重置为默认配置
     */
    public void resetToDefaults() {
        properties.clear();
        
        // 设置默认值
        properties.put("defaultPlatform", "web");
        properties.put("defaultEnvironment", "dev");
        properties.put("browser", "chrome");
        properties.put("timeout", "30000");
        properties.put("retryCount", "1");
        properties.put("reportFormat", "html");
    }
    
    /**
     * 获取配置属性
     */
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * 获取配置属性，如果不存在则返回默认值
     */
    public String getProperty(String key, String defaultValue) {
        String value = properties.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 设置配置属性
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    /**
     * 检查配置属性是否存在
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * 删除配置属性
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    /**
     * 获取所有属性名称
     */
    public Set<String> getPropertyNames() {
        return new HashSet<>(properties.keySet());
    }
    
    /**
     * 获取默认平台
     */
    public String getDefaultPlatform() {
        return getProperty("defaultPlatform", "web");
    }
    
    /**
     * 设置默认平台
     */
    public void setDefaultPlatform(String platform) {
        setProperty("defaultPlatform", platform);
    }
    
    /**
     * 获取默认环境
     */
    public String getDefaultEnvironment() {
        return getProperty("defaultEnvironment", "dev");
    }
    
    /**
     * 设置默认环境
     */
    public void setDefaultEnvironment(String environment) {
        setProperty("defaultEnvironment", environment);
    }
    
    /**
     * 获取默认浏览器
     */
    public String getDefaultBrowser() {
        return getProperty("browser", "chrome");
    }
    
    /**
     * 设置默认浏览器
     */
    public void setDefaultBrowser(String browser) {
        setProperty("browser", browser);
    }
    
    /**
     * 获取超时时间
     */
    public int getTimeout() {
        String timeout = getProperty("timeout", "30000");
        try {
            return Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            return 30000;
        }
    }
    
    /**
     * 设置超时时间
     */
    public void setTimeout(int timeout) {
        setProperty("timeout", String.valueOf(timeout));
    }
    
    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        String retryCount = getProperty("retryCount", "1");
        try {
            return Integer.parseInt(retryCount);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    
    /**
     * 设置重试次数
     */
    public void setRetryCount(int retryCount) {
        setProperty("retryCount", String.valueOf(retryCount));
    }
    
    /**
     * 获取默认报告格式
     */
    public String getDefaultReportFormat() {
        return getProperty("reportFormat", "html");
    }
    
    /**
     * 设置默认报告格式
     */
    public void setDefaultReportFormat(String format) {
        setProperty("reportFormat", format);
    }
    
    /**
     * 获取是否启用详细日志
     */
    public boolean isVerbose() {
        return verbose;
    }
    
    /**
     * 设置是否启用详细日志
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * 获取配置文件路径
     */
    public String getConfigFilePath() {
        return configFilePath;
    }
}