package com.midscene.playground.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Playground配置类
 * 管理Playground的所有配置信息
 */
public class PlaygroundConfig {
    // 默认配置值
    private static final long DEFAULT_EXECUTION_TIMEOUT_MS = 10000; // 10秒
    private static final long DEFAULT_MAX_SESSION_AGE_MS = 3600000; // 1小时
    private static final int DEFAULT_MAX_MEMORY_MB = 256;
    private static final int DEFAULT_MAX_THREADS = 4;
    private static final boolean DEFAULT_ENABLE_SANDBOX = true;
    
    // 核心配置
    private long executionTimeoutMs;
    private long maxSessionAgeMs;
    private int maxMemoryMb;
    private int maxThreads;
    private boolean enableSandbox;
    
    // 安全配置
    private List<String> allowedPackages;
    private List<String> allowedClasses;
    private List<String> deniedPackages;
    private List<String> deniedClasses;
    private Map<String, String> systemProperties;
    
    // 代码配置
    private String sessionInitCode;
    private String sessionCleanupCode;
    private List<String> defaultImports;
    
    /**
     * 构造函数
     */
    public PlaygroundConfig() {
        // 设置默认值
        this.executionTimeoutMs = DEFAULT_EXECUTION_TIMEOUT_MS;
        this.maxSessionAgeMs = DEFAULT_MAX_SESSION_AGE_MS;
        this.maxMemoryMb = DEFAULT_MAX_MEMORY_MB;
        this.maxThreads = DEFAULT_MAX_THREADS;
        this.enableSandbox = DEFAULT_ENABLE_SANDBOX;
        
        // 初始化集合
        this.allowedPackages = new ArrayList<>();
        this.allowedClasses = new ArrayList<>();
        this.deniedPackages = new ArrayList<>();
        this.deniedClasses = new ArrayList<>();
        this.systemProperties = new HashMap<>();
        this.defaultImports = new ArrayList<>();
        
        // 添加默认允许的包
        addDefaultAllowedPackages();
        // 添加默认导入
        addDefaultImports();
    }
    
    /**
     * 添加默认允许的包
     */
    private void addDefaultAllowedPackages() {
        // 添加常用的安全包
        allowedPackages.add("java.lang");
        allowedPackages.add("java.util");
        allowedPackages.add("java.math");
        allowedPackages.add("java.text");
        allowedPackages.add("java.time");
        allowedPackages.add("java.io");
        
        // 添加MidScene相关包
        allowedPackages.add("com.midscene.core");
        allowedPackages.add("com.midscene.shared");
        
        // 添加默认拒绝的包
        deniedPackages.add("java.lang.reflect");
        deniedPackages.add("java.lang.invoke");
        deniedPackages.add("java.lang.ProcessBuilder");
        deniedPackages.add("java.net");
        deniedPackages.add("java.nio.file");
    }
    
    /**
     * 添加默认导入
     */
    private void addDefaultImports() {
        defaultImports.add("java.util.*");
        defaultImports.add("java.util.stream.*");
        defaultImports.add("java.time.*");
        defaultImports.add("java.time.format.*");
    }
    
    // Getters and Setters
    
    public long getExecutionTimeoutMs() {
        return executionTimeoutMs;
    }
    
    public void setExecutionTimeoutMs(long executionTimeoutMs) {
        this.executionTimeoutMs = executionTimeoutMs > 0 ? executionTimeoutMs : DEFAULT_EXECUTION_TIMEOUT_MS;
    }
    
    public long getMaxSessionAgeMs() {
        return maxSessionAgeMs;
    }
    
    public void setMaxSessionAgeMs(long maxSessionAgeMs) {
        this.maxSessionAgeMs = maxSessionAgeMs;
    }
    
    public int getMaxMemoryMb() {
        return maxMemoryMb;
    }
    
    public void setMaxMemoryMb(int maxMemoryMb) {
        this.maxMemoryMb = maxMemoryMb > 0 ? maxMemoryMb : DEFAULT_MAX_MEMORY_MB;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
    
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads > 0 ? maxThreads : DEFAULT_MAX_THREADS;
    }
    
    public boolean isEnableSandbox() {
        return enableSandbox;
    }
    
    public void setEnableSandbox(boolean enableSandbox) {
        this.enableSandbox = enableSandbox;
    }
    
    public List<String> getAllowedPackages() {
        return new ArrayList<>(allowedPackages);
    }
    
    public void setAllowedPackages(List<String> allowedPackages) {
        this.allowedPackages.clear();
        if (allowedPackages != null) {
            this.allowedPackages.addAll(allowedPackages);
        }
    }
    
    public void addAllowedPackage(String pkg) {
        if (pkg != null && !pkg.isEmpty()) {
            allowedPackages.add(pkg);
        }
    }
    
    public List<String> getAllowedClasses() {
        return new ArrayList<>(allowedClasses);
    }
    
    public void setAllowedClasses(List<String> allowedClasses) {
        this.allowedClasses.clear();
        if (allowedClasses != null) {
            this.allowedClasses.addAll(allowedClasses);
        }
    }
    
    public void addAllowedClass(String className) {
        if (className != null && !className.isEmpty()) {
            allowedClasses.add(className);
        }
    }
    
    public List<String> getDeniedPackages() {
        return new ArrayList<>(deniedPackages);
    }
    
    public void setDeniedPackages(List<String> deniedPackages) {
        this.deniedPackages.clear();
        if (deniedPackages != null) {
            this.deniedPackages.addAll(deniedPackages);
        }
    }
    
    public void addDeniedPackage(String pkg) {
        if (pkg != null && !pkg.isEmpty()) {
            deniedPackages.add(pkg);
        }
    }
    
    public List<String> getDeniedClasses() {
        return new ArrayList<>(deniedClasses);
    }
    
    public void setDeniedClasses(List<String> deniedClasses) {
        this.deniedClasses.clear();
        if (deniedClasses != null) {
            this.deniedClasses.addAll(deniedClasses);
        }
    }
    
    public void addDeniedClass(String className) {
        if (className != null && !className.isEmpty()) {
            deniedClasses.add(className);
        }
    }
    
    public Map<String, String> getSystemProperties() {
        return new HashMap<>(systemProperties);
    }
    
    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties.clear();
        if (systemProperties != null) {
            this.systemProperties.putAll(systemProperties);
        }
    }
    
    public void setSystemProperty(String key, String value) {
        if (key != null) {
            systemProperties.put(key, value);
        }
    }
    
    public String getSessionInitCode() {
        return sessionInitCode;
    }
    
    public void setSessionInitCode(String sessionInitCode) {
        this.sessionInitCode = sessionInitCode;
    }
    
    public String getSessionCleanupCode() {
        return sessionCleanupCode;
    }
    
    public void setSessionCleanupCode(String sessionCleanupCode) {
        this.sessionCleanupCode = sessionCleanupCode;
    }
    
    public List<String> getDefaultImports() {
        return new ArrayList<>(defaultImports);
    }
    
    public void setDefaultImports(List<String> defaultImports) {
        this.defaultImports.clear();
        if (defaultImports != null) {
            this.defaultImports.addAll(defaultImports);
        }
    }
    
    public void addDefaultImport(String importStatement) {
        if (importStatement != null && !importStatement.isEmpty()) {
            defaultImports.add(importStatement);
        }
    }
    
    /**
     * 检查包是否被允许
     */
    public boolean isPackageAllowed(String pkg) {
        // 如果包在拒绝列表中，直接拒绝
        for (String deniedPkg : deniedPackages) {
            if (pkg.startsWith(deniedPkg)) {
                return false;
            }
        }
        
        // 如果包在允许列表中，允许
        for (String allowedPkg : allowedPackages) {
            if (pkg.startsWith(allowedPkg)) {
                return true;
            }
        }
        
        // 默认拒绝
        return false;
    }
    
    /**
     * 检查类是否被允许
     */
    public boolean isClassAllowed(String className) {
        // 如果类在拒绝列表中，直接拒绝
        if (deniedClasses.contains(className)) {
            return false;
        }
        
        // 如果类在允许列表中，允许
        if (allowedClasses.contains(className)) {
            return true;
        }
        
        // 检查类所在的包
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String pkg = className.substring(0, lastDotIndex);
            return isPackageAllowed(pkg);
        }
        
        // 默认拒绝
        return false;
    }
    
    /**
     * 创建默认配置实例
     */
    public static PlaygroundConfig createDefault() {
        return new PlaygroundConfig();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PlaygroundConfig{\n")
          .append("  executionTimeoutMs='")
          .append(executionTimeoutMs)
          .append("'\n")
          .append("  maxSessionAgeMs='")
          .append(maxSessionAgeMs)
          .append("'\n")
          .append("  maxMemoryMb='")
          .append(maxMemoryMb)
          .append("'\n")
          .append("  maxThreads='")
          .append(maxThreads)
          .append("'\n")
          .append("  enableSandbox='")
          .append(enableSandbox)
          .append("'\n")
          .append("  allowedPackagesCount='")
          .append(allowedPackages.size())
          .append("'\n")
          .append("  deniedPackagesCount='")
          .append(deniedPackages.size())
          .append("'\n")
          .append("}");
        return sb.toString();
    }
}