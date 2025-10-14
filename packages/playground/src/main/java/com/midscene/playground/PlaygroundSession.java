package com.midscene.playground;

import com.midscene.playground.code.ExecutionResult;
import com.midscene.playground.code.Executor;
import com.midscene.playground.code.JavaExecutor;
import com.midscene.playground.config.PlaygroundConfig;
import com.midscene.playground.security.SandboxManager;
import com.midscene.playground.security.SecureEnvironment;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Playground会话类
 * 管理单个代码执行会话
 */
public class PlaygroundSession {
    private final String id;
    private final long creationTime;
    private final PlaygroundConfig config;
    private final Executor executor;
    private final Map<String, Object> sharedContext;
    private final SecureEnvironment secureEnvironment;
    
    /**
     * 构造函数
     */
    public PlaygroundSession(PlaygroundConfig config, SandboxManager sandboxManager) {
        this.id = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.config = config;
        this.sharedContext = new ConcurrentHashMap<>();
        
        // 创建安全环境
        this.secureEnvironment = sandboxManager.createSecureEnvironment();
        
        // 创建执行器
        this.executor = new JavaExecutor(config, secureEnvironment);
        
        // 初始化会话
        initialize();
    }
    
    /**
     * 初始化会话
     */
    private void initialize() {
        // 设置默认上下文
        sharedContext.put("sessionId", id);
        sharedContext.put("creationTime", creationTime);
        
        // 执行初始化代码
        String initCode = config.getSessionInitCode();
        if (initCode != null && !initCode.trim().isEmpty()) {
            try {
                executor.execute(initCode, sharedContext);
            } catch (Exception e) {
                throw new PlaygroundException("Failed to initialize session: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 执行代码
     * @param code 要执行的代码
     */
    public ExecutionResult executeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        
        try {
            // 使用共享上下文执行代码
            ExecutionResult result = executor.execute(code, sharedContext);
            
            // 更新上下文（如果需要）
            if (result.getContext() != null) {
                sharedContext.putAll(result.getContext());
            }
            
            return result;
        } catch (Exception e) {
            throw new PlaygroundException("Failed to execute code: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行代码（带有超时设置）
     * @param code 要执行的代码
     * @param timeoutMs 超时时间（毫秒）
     */
    public ExecutionResult executeCode(String code, long timeoutMs) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        
        try {
            // 临时设置超时
            long originalTimeout = config.getExecutionTimeoutMs();
            config.setExecutionTimeoutMs(timeoutMs);
            
            try {
                return executeCode(code);
            } finally {
                // 恢复原始超时设置
                config.setExecutionTimeoutMs(originalTimeout);
            }
        } catch (Exception e) {
            throw new PlaygroundException("Failed to execute code with timeout: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取会话ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取创建时间
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * 获取会话年龄（毫秒）
     */
    public long getAge() {
        return System.currentTimeMillis() - creationTime;
    }
    
    /**
     * 检查会话是否过期
     */
    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }
    
    /**
     * 检查会话是否过期
     * @param currentTime 当前时间
     */
    public boolean isExpired(long currentTime) {
        long maxSessionAge = config.getMaxSessionAgeMs();
        return maxSessionAge > 0 && (currentTime - creationTime) > maxSessionAge;
    }
    
    /**
     * 获取共享上下文
     */
    public Map<String, Object> getSharedContext() {
        return new ConcurrentHashMap<>(sharedContext); // 返回副本
    }
    
    /**
     * 设置上下文变量
     * @param key 键
     * @param value 值
     */
    public void setContextVariable(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        sharedContext.put(key, value);
    }
    
    /**
     * 获取上下文变量
     * @param key 键
     */
    public Object getContextVariable(String key) {
        return sharedContext.get(key);
    }
    
    /**
     * 清除上下文变量
     * @param key 键
     */
    public void clearContextVariable(String key) {
        if (key != null) {
            sharedContext.remove(key);
        }
    }
    
    /**
     * 重置上下文（保留基本会话信息）
     */
    public void resetContext() {
        sharedContext.clear();
        sharedContext.put("sessionId", id);
        sharedContext.put("creationTime", creationTime);
    }
    
    /**
     * 获取执行器
     */
    public Executor getExecutor() {
        return executor;
    }
    
    /**
     * 获取安全环境
     */
    public SecureEnvironment getSecureEnvironment() {
        return secureEnvironment;
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        try {
            // 执行清理代码
            String cleanupCode = config.getSessionCleanupCode();
            if (cleanupCode != null && !cleanupCode.trim().isEmpty()) {
                try {
                    executor.execute(cleanupCode, sharedContext);
                } catch (Exception e) {
                    // 记录但不抛出，因为这是清理阶段
                    System.err.println("Error during session cleanup: " + e.getMessage());
                }
            }
            
            // 清理资源
            sharedContext.clear();
            secureEnvironment.close();
        } catch (Exception e) {
            System.err.println("Error closing session: " + e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        return "PlaygroundSession{" +
               "id='" + id + '\'' +
               ", age=" + getAge() + "ms" +
               ", contextSize=" + sharedContext.size() +
               '}';
    }
}