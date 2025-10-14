package com.midscene.playground.security;

import com.midscene.playground.config.PlaygroundConfig;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 沙箱管理器
 * 负责创建和管理代码执行的安全环境
 */
public class SandboxManager {
    private static final Logger logger = Logger.getLogger(SandboxManager.class.getName());
    private static final Map<String, SandboxEnvironment> sandboxEnvironments = new ConcurrentHashMap<>();
    private final PlaygroundConfig config;
    private final Map<String, ClassLoader> classLoaderCache = new ConcurrentHashMap<>();
    private final File tempDir;
    
    /**
     * 构造函数
     */
    public SandboxManager(PlaygroundConfig config) {
        this.config = config;
        this.tempDir = createTempDirectory();
        initializeSecurityManager();
    }
    
    /**
     * 创建临时目录
     */
    private File createTempDirectory() {
        try {
            Path tempPath = Files.createTempDirectory("playground-sandbox-");
            tempPath.toFile().deleteOnExit();
            logger.info("Created temporary directory for sandbox: " + tempPath);
            return tempPath.toFile();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create temporary directory for sandbox", e);
            throw new RuntimeException("Failed to initialize sandbox environment", e);
        }
    }
    
    /**
     * 初始化安全管理器
     */
    private void initializeSecurityManager() {
        if (config.isEnableSandbox()) {
            // 设置自定义安全策略
            Policy.setPolicy(new CustomSecurityPolicy());
            
            // 启用安全管理器
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
                logger.info("Security manager enabled");
            }
        }
    }
    
    /**
     * 获取会话的沙箱环境
     */
    public SandboxEnvironment getSandboxEnvironment(String sessionId) {
        return sandboxEnvironments.computeIfAbsent(sessionId, this::createNewSandbox);
    }
    
    /**
     * 创建新的沙箱环境
     */
    private SandboxEnvironment createNewSandbox(String sessionId) {
        try {
            // 创建会话专属的临时目录
            File sessionDir = new File(tempDir, sessionId);
            if (!sessionDir.mkdirs()) {
                logger.warning("Failed to create session directory: " + sessionDir);
            }
            sessionDir.deleteOnExit();
            
            // 创建沙箱环境
            SandboxEnvironment sandbox = new SandboxEnvironment(sessionId, sessionDir);
            logger.info("Created new sandbox environment for session: " + sessionId);
            return sandbox;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create sandbox environment for session: " + sessionId, e);
            throw new RuntimeException("Failed to create sandbox environment", e);
        }
    }
    
    /**
     * 清理会话的沙箱环境
     */
    public void cleanupSandbox(String sessionId) {
        SandboxEnvironment sandbox = sandboxEnvironments.remove(sessionId);
        if (sandbox != null) {
            sandbox.cleanup();
            logger.info("Cleaned up sandbox environment for session: " + sessionId);
        }
    }
    
    /**
     * 获取安全的类加载器
     */
    public ClassLoader getSecureClassLoader(String sessionId) {
        return classLoaderCache.computeIfAbsent(sessionId, id -> {
            SandboxEnvironment sandbox = getSandboxEnvironment(id);
            return new SecureSandboxClassLoader(sandbox);
        });
    }
    
    /**
     * 验证类是否允许加载
     */
    public boolean isClassAllowed(String className) {
        // 检查是否在拒绝列表中
        if (isInDeniedList(className)) {
            return false;
        }
        
        // 检查是否在允许列表中
        return isInAllowedList(className) || isSystemClass(className);
    }
    
    /**
     * 检查类是否在拒绝列表中
     */
    private boolean isInDeniedList(String className) {
        // 检查拒绝的类
        for (String deniedClass : config.getDeniedClasses()) {
            if (className.equals(deniedClass) || className.startsWith(deniedClass + ".")) {
                return true;
            }
        }
        
        // 检查拒绝的包
        for (String deniedPackage : config.getDeniedPackages()) {
            if (className.startsWith(deniedPackage + ".")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查类是否在允许列表中
     */
    private boolean isInAllowedList(String className) {
        // 检查允许的类
        for (String allowedClass : config.getAllowedClasses()) {
            if (className.equals(allowedClass) || className.startsWith(allowedClass + ".")) {
                return true;
            }
        }
        
        // 检查允许的包
        for (String allowedPackage : config.getAllowedPackages()) {
            if (className.startsWith(allowedPackage + ".")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为系统类
     */
    private boolean isSystemClass(String className) {
        // 允许基本的Java语言和核心库类
        return className.startsWith("java.lang.") ||
               className.startsWith("java.util.") ||
               className.startsWith("java.math.") ||
               className.startsWith("java.text.") ||
               className.startsWith("java.io.") && !className.equals("java.io.FileOutputStream") && !className.equals("java.io.FileWriter") ||
               className.equals("java.lang.Object") ||
               className.equals("java.lang.String") ||
               className.equals("java.lang.Integer") ||
               className.equals("java.lang.Long") ||
               className.equals("java.lang.Boolean") ||
               className.equals("java.lang.Double") ||
               className.equals("java.lang.Float") ||
               className.equals("java.lang.Short") ||
               className.equals("java.lang.Byte") ||
               className.equals("java.lang.Character") ||
               className.equals("java.lang.Void") ||
               className.equals("java.lang.Number") ||
               className.equals("java.lang.Comparable") ||
               className.equals("java.lang.Cloneable") ||
               className.equals("java.lang.Serializable");
    }
    
    /**
     * 获取安全权限集合
     */
    public PermissionCollection getSecurityPermissions(String sessionId) {
        Permissions permissions = new Permissions();
        
        // 基本运行时权限
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        permissions.add(new RuntimePermission("createClassLoader"));
        permissions.add(new RuntimePermission("getClassLoader"));
        permissions.add(new RuntimePermission("setContextClassLoader"));
        permissions.add(new RuntimePermission("getProtectionDomain"));
        
        // 线程权限
        permissions.add(new RuntimePermission("modifyThread"));
        permissions.add(new RuntimePermission("modifyThreadGroup"));
        
        // IO权限 - 只允许访问会话目录
        try {
            SandboxEnvironment sandbox = getSandboxEnvironment(sessionId);
            String sessionDir = sandbox.getSessionDirectory().getAbsolutePath();
            permissions.add(new FilePermission(sessionDir + File.separator + "*", "read,write,delete"));
            permissions.add(new FilePermission(sessionDir + File.separator + "-*," + "read,write,delete"));
        } catch (Exception e) {
            logger.warning("Failed to add file permissions for session: " + sessionId);
        }
        
        // 如果禁用沙箱，给予所有权限
        if (!config.isEnableSandbox()) {
            permissions.add(new AllPermission());
        }
        
        return permissions;
    }
    
    /**
     * 清理所有资源
     */
    public void shutdown() {
        // 清理所有沙箱环境
        for (String sessionId : new HashSet<>(sandboxEnvironments.keySet())) {
            cleanupSandbox(sessionId);
        }
        
        // 清理临时目录
        try {
            if (tempDir.exists()) {
                deleteDirectory(tempDir);
                logger.info("Cleaned up sandbox temporary directory: " + tempDir);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to cleanup temporary directory: " + tempDir, e);
        }
        
        // 清空缓存
        classLoaderCache.clear();
    }
    
    /**
     * 递归删除目录
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
    
    /**
     * 自定义安全策略
     */
    private class CustomSecurityPolicy extends Policy {
        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            Permissions permissions = new Permissions();
            // 默认权限集合
            return permissions;
        }
        
        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            // 获取类加载器信息，确定会话ID
            ClassLoader loader = domain.getClassLoader();
            if (loader instanceof SecureSandboxClassLoader) {
                SecureSandboxClassLoader sandboxLoader = (SecureSandboxClassLoader) loader;
                String sessionId = sandboxLoader.getSessionId();
                return getSecurityPermissions(sessionId);
            }
            
            // 非沙箱加载器，返回最小权限
            Permissions permissions = new Permissions();
            return permissions;
        }
        
        @Override
        public boolean implies(ProtectionDomain domain, java.security.Permission permission) {
            PermissionCollection permissions = getPermissions(domain);
            return permissions.implies(permission);
        }
    }
    
    /**
     * 沙箱类加载器
     */
    private class SecureSandboxClassLoader extends SecureClassLoader {
        private final SandboxEnvironment sandbox;
        
        public SecureSandboxClassLoader(SandboxEnvironment sandbox) {
            this.sandbox = sandbox;
        }
        
        public String getSessionId() {
            return sandbox.getSessionId();
        }
        
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // 检查类是否允许加载
            if (!isClassAllowed(name)) {
                logger.warning("Blocked loading of disallowed class: " + name);
                throw new ClassNotFoundException("Class not allowed: " + name);
            }
            
            // 尝试从已加载的类中获取
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }
            
            // 优先从父类加载器加载（系统类）
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                // 父类加载器无法加载，尝试从沙箱加载
            }
            
            // 从沙箱加载自定义类
            byte[] classData = sandbox.getClassData(name);
            if (classData != null) {
                Class<?> clazz = defineClass(name, classData, 0, classData.length);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
            
            throw new ClassNotFoundException(name);
        }
    }
    
    /**
     * 沙箱环境类
     */
    public class SandboxEnvironment {
        private final String sessionId;
        private final File sessionDirectory;
        private final Map<String, byte[]> classDataCache = new HashMap<>();
        private long creationTime = System.currentTimeMillis();
        
        public SandboxEnvironment(String sessionId, File sessionDirectory) {
            this.sessionId = sessionId;
            this.sessionDirectory = sessionDirectory;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public File getSessionDirectory() {
            return sessionDirectory;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public void setClassData(String className, byte[] classData) {
            classDataCache.put(className, classData);
        }
        
        public byte[] getClassData(String className) {
            return classDataCache.get(className);
        }
        
        public void cleanup() {
            classDataCache.clear();
            
            // 清理会话目录
            try {
                deleteDirectory(sessionDirectory);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to cleanup session directory: " + sessionDirectory, e);
            }
        }
        
        public boolean isExpired(long maxAgeMs) {
            return System.currentTimeMillis() - creationTime > maxAgeMs;
        }
    }
}