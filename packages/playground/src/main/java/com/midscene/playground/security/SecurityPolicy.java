package com.midscene.playground.security;

import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.PropertyPermission;
import java.util.ReflectionPermission;
import java.util.RuntimePermission;

/**
 * 安全策略管理器
 * 负责管理和执行代码的安全策略
 */
public class SecurityPolicy {
    private static final Logger logger = Logger.getLogger(SecurityPolicy.class.getName());
    private static final SecurityPolicy INSTANCE = new SecurityPolicy();
    
    // 系统安全策略配置
    private static final Map<String, Set<Permission>> DEFAULT_PERMISSIONS = new ConcurrentHashMap<>();
    private static final Set<String> SUSPICIOUS_PACKAGES = new HashSet<>();
    private static final Set<String> SUSPICIOUS_CLASSES = new HashSet<>();
    
    static {
        initializeDefaultPermissions();
        initializeSuspiciousPackages();
    }
    
    private SecurityPolicy() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 获取单例实例
     */
    public static SecurityPolicy getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化默认权限
     */
    private static void initializeDefaultPermissions() {
        // 基本运行时权限
        Set<Permission> basicPermissions = new HashSet<>();
        basicPermissions.add(new RuntimePermission("accessDeclaredMembers"));
        basicPermissions.add(new RuntimePermission("createClassLoader"));
        basicPermissions.add(new RuntimePermission("getClassLoader"));
        basicPermissions.add(new RuntimePermission("setContextClassLoader"));
        basicPermissions.add(new RuntimePermission("getProtectionDomain"));
        basicPermissions.add(new RuntimePermission("modifyThread"));
        basicPermissions.add(new RuntimePermission("modifyThreadGroup"));
        basicPermissions.add(new RuntimePermission("stopThread"));
        basicPermissions.add(new RuntimePermission("setFactory"));
        basicPermissions.add(new RuntimePermission("getenv"));
        
        // 反射权限
        basicPermissions.add(new ReflectionPermission("suppressAccessChecks"));
        
        // 属性权限
        basicPermissions.add(new PropertyPermission("*", "read"));
        
        DEFAULT_PERMISSIONS.put("basic", basicPermissions);
        
        // 网络权限 - 默认禁用
        Set<Permission> networkPermissions = new HashSet<>();
        // networkPermissions.add(new SocketPermission("*", "connect,accept,listen,resolve"));
        DEFAULT_PERMISSIONS.put("network", networkPermissions);
        
        // 文件权限 - 默认只允许访问临时目录
        Set<Permission> filePermissions = new HashSet<>();
        try {
            Path tempDir = Files.createTempDirectory("playground");
            String tempPath = tempDir.toAbsolutePath().toString();
            filePermissions.add(new FilePermission(tempPath + "/-", "read,write,delete"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create temporary directory for default permissions", e);
        }
        DEFAULT_PERMISSIONS.put("file", filePermissions);
    }
    
    /**
     * 初始化可疑包列表
     */
    private static void initializeSuspiciousPackages() {
        // 系统安全相关包
        SUSPICIOUS_PACKAGES.add("java.security");
        SUSPICIOUS_PACKAGES.add("javax.security");
        SUSPICIOUS_PACKAGES.add("sun.security");
        SUSPICIOUS_PACKAGES.add("com.sun.security");
        
        // 系统管理相关包
        SUSPICIOUS_PACKAGES.add("java.lang.reflect");
        SUSPICIOUS_PACKAGES.add("java.lang.instrument");
        SUSPICIOUS_PACKAGES.add("java.lang.classfile");
        SUSPICIOUS_PACKAGES.add("java.lang.module");
        SUSPICIOUS_PACKAGES.add("java.lang.invoke");
        
        // 系统IO相关包
        SUSPICIOUS_PACKAGES.add("java.io.FileOutputStream");
        SUSPICIOUS_PACKAGES.add("java.io.FileWriter");
        SUSPICIOUS_PACKAGES.add("java.io.RandomAccessFile");
        
        // 系统进程相关类
        SUSPICIOUS_CLASSES.add("java.lang.ProcessBuilder");
        SUSPICIOUS_CLASSES.add("java.lang.Runtime");
        SUSPICIOUS_CLASSES.add("java.util.concurrent.Executors");
        
        // 网络相关类
        SUSPICIOUS_PACKAGES.add("java.net");
        SUSPICIOUS_PACKAGES.add("javax.net");
        
        // JVM相关类
        SUSPICIOUS_PACKAGES.add("java.lang.management");
        SUSPICIOUS_PACKAGES.add("javax.management");
        SUSPICIOUS_PACKAGES.add("sun.misc");
        SUSPICIOUS_PACKAGES.add("sun.reflect");
    }
    
    /**
     * 创建会话安全权限集合
     */
    public PermissionCollection createSessionPermissions(String sessionId, boolean allowNetwork, String tempDirPath) {
        Permissions permissions = new Permissions();
        
        // 添加基本权限
        DEFAULT_PERMISSIONS.getOrDefault("basic", new HashSet<>()).forEach(permissions::add);
        
        // 添加文件权限
        if (tempDirPath != null) {
            permissions.add(new FilePermission(tempDirPath + "/-", "read,write,delete"));
        }
        
        // 添加网络权限（如果允许）
        if (allowNetwork) {
            permissions.add(new SocketPermission("*", "connect,resolve"));
        }
        
        // 添加会话特定的权限
        permissions.add(new RuntimePermission("playground.session." + sessionId));
        
        return permissions;
    }
    
    /**
     * 检查类是否安全
     */
    public boolean isClassSafe(String className) {
        // 检查可疑类
        if (SUSPICIOUS_CLASSES.contains(className)) {
            logger.warning("Detected suspicious class: " + className);
            return false;
        }
        
        // 检查可疑包
        for (String suspiciousPackage : SUSPICIOUS_PACKAGES) {
            if (className.startsWith(suspiciousPackage + ".")) {
                logger.warning("Detected suspicious package: " + className);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 执行特权操作
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        return AccessController.doPrivileged(action);
    }
    
    /**
     * 执行特权操作（可能抛出异常）
     */
    public static <T> T doPrivilegedException(PrivilegedExceptionAction<T> action) throws Exception {
        return AccessController.doPrivileged(action);
    }
    
    /**
     * 检查当前上下文是否有权限
     */
    public static void checkPermission(Permission permission) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(permission);
        }
    }
    
    /**
     * 获取当前访问控制上下文
     */
    public static AccessControlContext getContext() {
        return AccessController.getContext();
    }
    
    /**
     * 在指定上下文中执行操作
     */
    public static <T> T doPrivilegedWithContext(PrivilegedAction<T> action, AccessControlContext context) {
        return AccessController.doPrivileged(action, context);
    }
    
    /**
     * 检查是否为危险操作
     */
    public boolean isDangerousOperation(String operation, String target) {
        // 危险操作检查
        switch (operation) {
            case "file.write":
            case "file.delete":
                return target != null && (target.contains("/etc/") || 
                                          target.contains("C:\\Windows") ||
                                          target.contains("C:/Windows"));
            
            case "network.connect":
                return target != null && (target.contains("localhost") || 
                                          target.contains("127.0.0.1") ||
                                          target.contains("169.254") ||
                                          target.contains("0.0.0.0"));
            
            case "process.create":
                return true; // 任何进程创建都是危险的
            
            case "class.load":
                return target != null && !isClassSafe(target);
            
            default:
                return false;
        }
    }
    
    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String eventType, String message, String className, String sessionId) {
        logger.log(Level.INFO, String.format("[SECURITY] [%s] [%s] Session: %s, Class: %s, Message: %s", 
                                            eventType, 
                                            Thread.currentThread().getName(), 
                                            sessionId, 
                                            className, 
                                            message));
    }
    
    /**
     * 记录安全违规
     */
    public void logSecurityViolation(String permission, String className, String sessionId) {
        logger.log(Level.WARNING, String.format("[VIOLATION] Permission denied: %s, Class: %s, Session: %s", 
                                               permission, 
                                               className, 
                                               sessionId));
    }
    
    /**
     * 创建自定义安全管理器
     */
    public SecurityManager createSecurityManager(final Map<String, Set<String>> allowedClasses) {
        return new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                // 获取调用栈
                Class<?>[] context = getClassContext();
                
                // 检查是否为系统类调用
                for (Class<?> caller : context) {
                    String callerName = caller.getName();
                    
                    // 跳过Playground自身的类
                    if (callerName.startsWith("com.midscene.playground")) {
                        continue;
                    }
                    
                    // 检查是否为允许的类
                    boolean isAllowed = false;
                    if (allowedClasses != null) {
                        for (String pkg : allowedClasses.keySet()) {
                            if (callerName.startsWith(pkg + ".")) {
                                isAllowed = true;
                                break;
                            }
                        }
                        
                        for (Set<String> classes : allowedClasses.values()) {
                            if (classes.contains(callerName)) {
                                isAllowed = true;
                                break;
                            }
                        }
                    }
                    
                    // 检查是否为危险权限
                    if (!isAllowed && isDangerousPermission(perm)) {
                        logSecurityViolation(perm.toString(), callerName, "unknown");
                        throw new AccessControlException("Permission denied: " + perm);
                    }
                    
                    break;
                }
            }
            
            private boolean isDangerousPermission(Permission perm) {
                // 检查危险权限
                return perm instanceof FilePermission && !perm.getActions().contains("read") ||
                       perm instanceof SocketPermission ||
                       perm instanceof RuntimePermission && perm.getName().contains("exitVM") ||
                       perm instanceof RuntimePermission && perm.getName().contains("shutdownHooks") ||
                       perm instanceof RuntimePermission && perm.getName().contains("setSecurityManager") ||
                       perm instanceof SecurityPermission;
            }
        };
    }
    
    /**
     * 获取允许的包列表
     */
    public Set<String> getAllowedPackages() {
        Set<String> allowed = new HashSet<>();
        // 默认允许的包
        allowed.add("java.lang");
        allowed.add("java.util");
        allowed.add("java.math");
        allowed.add("java.text");
        allowed.add("java.io");
        allowed.add("java.util.concurrent");
        allowed.add("java.util.function");
        allowed.add("java.util.stream");
        return allowed;
    }
    
    /**
     * 验证代码片段的安全性
     */
    public boolean validateCodeSafety(String code) {
        if (code == null || code.trim().isEmpty()) {
            return true;
        }
        
        // 检查危险模式
        String[] dangerousPatterns = {
            "Runtime.getRuntime()",
            "ProcessBuilder",
            "System.exit",
            "setSecurityManager",
            "ClassLoader.defineClass",
            "Class.forName",
            "Method.invoke",
            "Field.set",
            "java.lang.reflect",
            "sun.misc.Unsafe",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "javax.xml.transform.Templates",
            "javax.swing.JFrame",
            "java.awt.Robot"
        };
        
        for (String pattern : dangerousPatterns) {
            if (code.contains(pattern)) {
                logger.warning("Detected dangerous code pattern: " + pattern);
                return false;
            }
        }
        
        return true;
    }
}