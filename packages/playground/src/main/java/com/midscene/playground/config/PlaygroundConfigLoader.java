package com.midscene.playground.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Playground配置加载器
 * 负责从文件加载配置信息
 */
public class PlaygroundConfigLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_CONFIG_PATH = "playground-config.json";
    private static final String DEFAULT_PROPS_PATH = "playground.properties";
    
    /**
     * 加载默认配置
     */
    public static PlaygroundConfig loadDefault() {
        PlaygroundConfig config = new PlaygroundConfig();
        
        // 尝试从默认路径加载配置文件
        try {
            // 先尝试JSON配置
            if (Files.exists(Paths.get(DEFAULT_CONFIG_PATH))) {
                return loadFromJsonFile(DEFAULT_CONFIG_PATH, config);
            }
            
            // 再尝试Properties配置
            if (Files.exists(Paths.get(DEFAULT_PROPS_PATH))) {
                return loadFromPropertiesFile(DEFAULT_PROPS_PATH, config);
            }
            
            // 尝试从classpath加载默认配置
            try (InputStream is = PlaygroundConfigLoader.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_PATH)) {
                if (is != null) {
                    return loadFromJsonStream(is, config);
                }
            }
            
            try (InputStream is = PlaygroundConfigLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPS_PATH)) {
                if (is != null) {
                    return loadFromPropertiesStream(is, config);
                }
            }
        } catch (Exception e) {
            // 如果加载失败，使用默认配置
            System.err.println("Failed to load default configuration: " + e.getMessage());
        }
        
        return config;
    }
    
    /**
     * 从文件加载配置
     * @param filePath 配置文件路径
     */
    public static PlaygroundConfig loadFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("Config file not found: " + filePath);
        }
        
        PlaygroundConfig config = new PlaygroundConfig();
        
        // 根据文件扩展名选择加载方式
        if (filePath.endsWith(".json")) {
            return loadFromJsonFile(filePath, config);
        } else if (filePath.endsWith(".properties")) {
            return loadFromPropertiesFile(filePath, config);
        } else {
            throw new IOException("Unsupported config file format: " + filePath);
        }
    }
    
    /**
     * 从JSON文件加载配置
     */
    private static PlaygroundConfig loadFromJsonFile(String filePath, PlaygroundConfig config) throws IOException {
        File file = new File(filePath);
        return loadFromJsonStream(Files.newInputStream(file.toPath()), config);
    }
    
    /**
     * 从JSON流加载配置
     */
    private static PlaygroundConfig loadFromJsonStream(InputStream is, PlaygroundConfig config) throws IOException {
        // 使用Jackson读取JSON配置
        // 这里为了简化，假设JSON结构与PlaygroundConfig类一致
        // 实际项目中可能需要自定义反序列化逻辑
        PlaygroundConfig loadedConfig = OBJECT_MAPPER.readValue(is, PlaygroundConfig.class);
        
        // 合并配置
        mergeConfig(config, loadedConfig);
        
        return config;
    }
    
    /**
     * 从Properties文件加载配置
     */
    private static PlaygroundConfig loadFromPropertiesFile(String filePath, PlaygroundConfig config) throws IOException {
        File file = new File(filePath);
        try (InputStream is = Files.newInputStream(file.toPath())) {
            return loadFromPropertiesStream(is, config);
        }
    }
    
    /**
     * 从Properties流加载配置
     */
    private static PlaygroundConfig loadFromPropertiesStream(InputStream is, PlaygroundConfig config) throws IOException {
        Properties props = new Properties();
        props.load(is);
        
        // 加载核心配置
        if (props.containsKey("execution.timeout.ms")) {
            config.setExecutionTimeoutMs(Long.parseLong(props.getProperty("execution.timeout.ms")));
        }
        
        if (props.containsKey("max.session.age.ms")) {
            config.setMaxSessionAgeMs(Long.parseLong(props.getProperty("max.session.age.ms")));
        }
        
        if (props.containsKey("max.memory.mb")) {
            config.setMaxMemoryMb(Integer.parseInt(props.getProperty("max.memory.mb")));
        }
        
        if (props.containsKey("max.threads")) {
            config.setMaxThreads(Integer.parseInt(props.getProperty("max.threads")));
        }
        
        if (props.containsKey("enable.sandbox")) {
            config.setEnableSandbox(Boolean.parseBoolean(props.getProperty("enable.sandbox")));
        }
        
        // 加载安全配置
        loadSecurityProperties(props, config);
        
        // 加载代码配置
        if (props.containsKey("session.init.code")) {
            config.setSessionInitCode(props.getProperty("session.init.code"));
        }
        
        if (props.containsKey("session.cleanup.code")) {
            config.setSessionCleanupCode(props.getProperty("session.cleanup.code"));
        }
        
        return config;
    }
    
    /**
     * 加载安全相关的配置
     */
    private static void loadSecurityProperties(Properties props, PlaygroundConfig config) {
        // 加载允许的包
        String allowedPackages = props.getProperty("allowed.packages");
        if (allowedPackages != null && !allowedPackages.trim().isEmpty()) {
            for (String pkg : allowedPackages.split(",")) {
                config.addAllowedPackage(pkg.trim());
            }
        }
        
        // 加载允许的类
        String allowedClasses = props.getProperty("allowed.classes");
        if (allowedClasses != null && !allowedClasses.trim().isEmpty()) {
            for (String className : allowedClasses.split(",")) {
                config.addAllowedClass(className.trim());
            }
        }
        
        // 加载拒绝的包
        String deniedPackages = props.getProperty("denied.packages");
        if (deniedPackages != null && !deniedPackages.trim().isEmpty()) {
            for (String pkg : deniedPackages.split(",")) {
                config.addDeniedPackage(pkg.trim());
            }
        }
        
        // 加载拒绝的类
        String deniedClasses = props.getProperty("denied.classes");
        if (deniedClasses != null && !deniedClasses.trim().isEmpty()) {
            for (String className : deniedClasses.split(",")) {
                config.addDeniedClass(className.trim());
            }
        }
        
        // 加载默认导入
        String defaultImports = props.getProperty("default.imports");
        if (defaultImports != null && !defaultImports.trim().isEmpty()) {
            for (String importStmt : defaultImports.split(",")) {
                config.addDefaultImport(importStmt.trim());
            }
        }
        
        // 加载系统属性
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("system.property.")) {
                String propName = key.substring("system.property.".length());
                String propValue = props.getProperty(key);
                config.setSystemProperty(propName, propValue);
            }
        }
    }
    
    /**
     * 合并配置
     */
    private static void mergeConfig(PlaygroundConfig target, PlaygroundConfig source) {
        // 这里实现配置合并逻辑
        // 对于集合类型，需要特殊处理
        if (source.allowedPackages != null) {
            for (String pkg : source.allowedPackages) {
                if (!target.allowedPackages.contains(pkg)) {
                    target.allowedPackages.add(pkg);
                }
            }
        }
        
        if (source.allowedClasses != null) {
            for (String className : source.allowedClasses) {
                if (!target.allowedClasses.contains(className)) {
                    target.allowedClasses.add(className);
                }
            }
        }
        
        if (source.deniedPackages != null) {
            for (String pkg : source.deniedPackages) {
                if (!target.deniedPackages.contains(pkg)) {
                    target.deniedPackages.add(pkg);
                }
            }
        }
        
        if (source.deniedClasses != null) {
            for (String className : source.deniedClasses) {
                if (!target.deniedClasses.contains(className)) {
                    target.deniedClasses.add(className);
                }
            }
        }
        
        if (source.systemProperties != null) {
            target.systemProperties.putAll(source.systemProperties);
        }
        
        if (source.defaultImports != null) {
            for (String importStmt : source.defaultImports) {
                if (!target.defaultImports.contains(importStmt)) {
                    target.defaultImports.add(importStmt);
                }
            }
        }
    }
    
    /**
     * 保存配置到JSON文件
     */
    public static void saveToJsonFile(PlaygroundConfig config, String filePath) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        // 确保目录存在
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 保存到文件
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, config);
    }
    
    /**
     * 保存配置到Properties文件
     */
    public static void saveToPropertiesFile(PlaygroundConfig config, String filePath) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        Properties props = new Properties();
        
        // 保存核心配置
        props.setProperty("execution.timeout.ms", String.valueOf(config.getExecutionTimeoutMs()));
        props.setProperty("max.session.age.ms", String.valueOf(config.getMaxSessionAgeMs()));
        props.setProperty("max.memory.mb", String.valueOf(config.getMaxMemoryMb()));
        props.setProperty("max.threads", String.valueOf(config.getMaxThreads()));
        props.setProperty("enable.sandbox", String.valueOf(config.isEnableSandbox()));
        
        // 保存安全配置
        saveSecurityProperties(config, props);
        
        // 保存代码配置
        if (config.getSessionInitCode() != null) {
            props.setProperty("session.init.code", config.getSessionInitCode());
        }
        
        if (config.getSessionCleanupCode() != null) {
            props.setProperty("session.cleanup.code", config.getSessionCleanupCode());
        }
        
        // 确保目录存在
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 保存到文件
        try (java.io.OutputStream os = Files.newOutputStream(file.toPath())) {
            props.store(os, "Playground Configuration");
        }
    }
    
    /**
     * 保存安全相关的配置
     */
    private static void saveSecurityProperties(PlaygroundConfig config, Properties props) {
        // 保存允许的包
        StringBuilder allowedPackages = new StringBuilder();
        for (String pkg : config.getAllowedPackages()) {
            if (allowedPackages.length() > 0) {
                allowedPackages.append(",");
            }
            allowedPackages.append(pkg);
        }
        props.setProperty("allowed.packages", allowedPackages.toString());
        
        // 保存允许的类
        StringBuilder allowedClasses = new StringBuilder();
        for (String className : config.getAllowedClasses()) {
            if (allowedClasses.length() > 0) {
                allowedClasses.append(",");
            }
            allowedClasses.append(className);
        }
        props.setProperty("allowed.classes", allowedClasses.toString());
        
        // 保存拒绝的包
        StringBuilder deniedPackages = new StringBuilder();
        for (String pkg : config.getDeniedPackages()) {
            if (deniedPackages.length() > 0) {
                deniedPackages.append(",");
            }
            deniedPackages.append(pkg);
        }
        props.setProperty("denied.packages", deniedPackages.toString());
        
        // 保存拒绝的类
        StringBuilder deniedClasses = new StringBuilder();
        for (String className : config.getDeniedClasses()) {
            if (deniedClasses.length() > 0) {
                deniedClasses.append(",");
            }
            deniedClasses.append(className);
        }
        props.setProperty("denied.classes", deniedClasses.toString());
        
        // 保存默认导入
        StringBuilder defaultImports = new StringBuilder();
        for (String importStmt : config.getDefaultImports()) {
            if (defaultImports.length() > 0) {
                defaultImports.append(",");
            }
            defaultImports.append(importStmt);
        }
        props.setProperty("default.imports", defaultImports.toString());
        
        // 保存系统属性
        for (Map.Entry<String, String> entry : config.getSystemProperties().entrySet()) {
            props.setProperty("system.property." + entry.getKey(), entry.getValue());
        }
    }
}