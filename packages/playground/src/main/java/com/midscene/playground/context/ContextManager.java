package com.midscene.playground.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 上下文管理器
 * 负责管理代码执行的上下文环境
 */
public class ContextManager {
    private static final Logger logger = Logger.getLogger(ContextManager.class.getName());
    private final Map<String, Object> contextObjects = new ConcurrentHashMap<>();
    private final Map<String, Object> sharedObjects = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String sessionId;
    
    /**
     * 构造函数
     */
    public ContextManager(String sessionId) {
        this.sessionId = sessionId;
        logger.info("Initialized context manager for session: " + sessionId);
    }
    
    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 设置上下文对象
     */
    public void set(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Setting context object: " + key + " in session: " + sessionId);
        contextObjects.put(key, value);
    }
    
    /**
     * 获取上下文对象
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Getting context object: " + key + " from session: " + sessionId);
        return (T) contextObjects.get(key);
    }
    
    /**
     * 获取上下文对象，如果不存在则创建
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String key, Supplier<T> supplier) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        
        // 尝试获取现有对象
        T value = (T) contextObjects.get(key);
        if (value == null) {
            // 使用写锁创建新对象
            lock.writeLock().lock();
            try {
                // 双重检查
                value = (T) contextObjects.get(key);
                if (value == null) {
                    value = supplier.get();
                    contextObjects.put(key, value);
                    logger.fine("Created new context object: " + key + " in session: " + sessionId);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to create context object: " + key, e);
                throw new RuntimeException("Failed to create context object: " + key, e);
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        return value;
    }
    
    /**
     * 检查上下文对象是否存在
     */
    public boolean contains(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        return contextObjects.containsKey(key);
    }
    
    /**
     * 移除上下文对象
     */
    public void remove(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Removing context object: " + key + " from session: " + sessionId);
        contextObjects.remove(key);
    }
    
    /**
     * 清空上下文
     */
    public void clear() {
        logger.info("Clearing context for session: " + sessionId);
        contextObjects.clear();
    }
    
    /**
     * 获取上下文对象的键集合
     */
    public Set<String> getKeys() {
        return contextObjects.keySet();
    }
    
    /**
     * 获取上下文对象的数量
     */
    public int size() {
        return contextObjects.size();
    }
    
    /**
     * 设置共享对象
     */
    public void setShared(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Setting shared object: " + key);
        sharedObjects.put(key, value);
    }
    
    /**
     * 获取共享对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getShared(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Getting shared object: " + key);
        return (T) sharedObjects.get(key);
    }
    
    /**
     * 移除共享对象
     */
    public void removeShared(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        logger.fine("Removing shared object: " + key);
        sharedObjects.remove(key);
    }
    
    /**
     * 清空共享对象
     */
    public void clearShared() {
        logger.info("Clearing shared objects");
        sharedObjects.clear();
    }
    
    /**
     * 获取所有上下文对象的快照
     */
    public Map<String, Object> getSnapshot() {
        lock.readLock().lock();
        try {
            return new HashMap<>(contextObjects);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 合并另一个上下文管理器的内容
     */
    public void merge(ContextManager other) {
        if (other == null) {
            throw new IllegalArgumentException("Context manager cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            Map<String, Object> otherSnapshot = other.getSnapshot();
            contextObjects.putAll(otherSnapshot);
            logger.info("Merged context from session: " + other.getSessionId() + " into session: " + sessionId);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 创建上下文副本
     */
    public ContextManager copy() {
        ContextManager copy = new ContextManager(sessionId + ":copy");
        copy.merge(this);
        return copy;
    }
    
    /**
     * 执行上下文绑定的操作
     */
    public <T> T doWithContext(ContextualAction<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        try {
            logger.fine("Executing contextual action in session: " + sessionId);
            return action.execute(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing contextual action in session: " + sessionId, e);
            throw new RuntimeException("Error executing contextual action", e);
        }
    }
    
    /**
     * 上下文操作接口
     */
    @FunctionalInterface
    public interface ContextualAction<T> {
        T execute(ContextManager context) throws Exception;
    }
    
    /**
     * 注册上下文清理钩子
     */
    public void registerCleanupHook(String key, AutoCloseable cleanupAction) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        
        if (cleanupAction == null) {
            throw new IllegalArgumentException("Cleanup action cannot be null");
        }
        
        // 创建清理包装器
        CleanupWrapper wrapper = new CleanupWrapper(key, cleanupAction);
        
        // 存储清理钩子
        set("__cleanup_" + key, wrapper);
        logger.fine("Registered cleanup hook for: " + key + " in session: " + sessionId);
    }
    
    /**
     * 执行所有清理钩子
     */
    public void executeCleanup() {
        logger.info("Executing cleanup hooks for session: " + sessionId);
        
        // 查找所有清理钩子并执行
        for (String key : contextObjects.keySet()) {
            if (key.startsWith("__cleanup_")) {
                Object value = contextObjects.get(key);
                if (value instanceof CleanupWrapper) {
                    CleanupWrapper wrapper = (CleanupWrapper) value;
                    try {
                        wrapper.cleanup();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error executing cleanup hook for: " + wrapper.getKey(), e);
                    }
                }
            }
        }
    }
    
    /**
     * 清理包装器
     */
    private static class CleanupWrapper implements AutoCloseable {
        private final String key;
        private final AutoCloseable action;
        private boolean closed = false;
        
        public CleanupWrapper(String key, AutoCloseable action) {
            this.key = key;
            this.action = action;
        }
        
        public String getKey() {
            return key;
        }
        
        public void cleanup() throws Exception {
            if (!closed) {
                closed = true;
                action.close();
            }
        }
        
        @Override
        public void close() throws Exception {
            cleanup();
        }
    }
    
    /**
     * 检查上下文是否为空
     */
    public boolean isEmpty() {
        return contextObjects.isEmpty();
    }
    
    /**
     * 冻结上下文，使其不可修改
     */
    public ReadOnlyContextManager freeze() {
        return new ReadOnlyContextManager(this);
    }
    
    /**
     * 只读上下文管理器
     */
    public static class ReadOnlyContextManager {
        private final ContextManager delegate;
        
        public ReadOnlyContextManager(ContextManager delegate) {
            this.delegate = delegate;
        }
        
        /**
         * 获取上下文对象
         */
        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return delegate.get(key);
        }
        
        /**
         * 检查上下文对象是否存在
         */
        public boolean contains(String key) {
            return delegate.contains(key);
        }
        
        /**
         * 获取上下文对象的键集合
         */
        public Set<String> getKeys() {
            return delegate.getKeys();
        }
        
        /**
         * 获取上下文对象的数量
         */
        public int size() {
            return delegate.size();
        }
        
        /**
         * 获取共享对象
         */
        @SuppressWarnings("unchecked")
        public <T> T getShared(String key) {
            return delegate.getShared(key);
        }
        
        /**
         * 获取所有上下文对象的快照
         */
        public Map<String, Object> getSnapshot() {
            return delegate.getSnapshot();
        }
        
        /**
         * 检查上下文是否为空
         */
        public boolean isEmpty() {
            return delegate.isEmpty();
        }
        
        /**
         * 获取会话ID
         */
        public String getSessionId() {
            return delegate.getSessionId();
        }
    }
}