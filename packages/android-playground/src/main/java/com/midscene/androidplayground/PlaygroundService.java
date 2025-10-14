package com.midscene.androidplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Playground服务类，作为整个模块的主要服务入口
 */
public class PlaygroundService {
    private static final Logger logger = LoggerFactory.getLogger(PlaygroundService.class);
    
    // 服务实例（单例模式）
    private static volatile PlaygroundService instance;
    
    // 配置
    private final Configuration configuration;
    // 设备管理器
    private final DeviceManager deviceManager;
    // 会话管理器
    private final SessionManager sessionManager;
    // 屏幕镜像服务
    private final ScreenMirrorService screenMirrorService;
    // 服务是否已初始化
    private volatile boolean initialized;
    // 服务是否已关闭
    private volatile boolean closed;
    
    /**
     * 创建新的PlaygroundService实例
     * @param configuration 配置对象
     */
    private PlaygroundService(Configuration configuration) {
        this.configuration = configuration;
        this.deviceManager = new DeviceManager();
        this.sessionManager = new SessionManager(deviceManager);
        this.screenMirrorService = new ScreenMirrorService();
        this.initialized = false;
        this.closed = false;
        
        logger.info("PlaygroundService instance created");
    }
    
    /**
     * 获取PlaygroundService的单例实例
     * @param configuration 配置对象
     * @return PlaygroundService实例
     */
    public static synchronized PlaygroundService getInstance(Configuration configuration) {
        if (instance == null || instance.isClosed()) {
            instance = new PlaygroundService(configuration);
        }
        return instance;
    }
    
    /**
     * 获取默认配置的PlaygroundService单例实例
     * @return PlaygroundService实例
     */
    public static synchronized PlaygroundService getInstance() {
        return getInstance(new Configuration());
    }
    
    /**
     * 初始化服务
     * @throws PlaygroundException 当初始化失败时抛出
     */
    public synchronized void initialize() throws PlaygroundException {
        if (closed) {
            throw new IllegalStateException("Service is already closed");
        }
        
        if (!initialized) {
            logger.info("Initializing PlaygroundService");
            
            try {
                // 初始化各个组件
                deviceManager.initialize();
                sessionManager.initialize();
                screenMirrorService.initialize();
                
                initialized = true;
                logger.info("PlaygroundService initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize PlaygroundService", e);
                throw new PlaygroundException("Failed to initialize service: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 关闭服务
     */
    public synchronized void close() {
        if (!closed) {
            logger.info("Closing PlaygroundService");
            
            try {
                // 关闭各个组件
                screenMirrorService.shutdown();
                sessionManager.shutdown();
                deviceManager.shutdown();
                
                closed = true;
                initialized = false;
                logger.info("PlaygroundService closed successfully");
            } catch (Exception e) {
                logger.error("Error closing PlaygroundService", e);
            }
        }
    }
    
    /**
     * 获取设备管理器
     * @return 设备管理器实例
     */
    public DeviceManager getDeviceManager() {
        ensureServiceAvailable();
        return deviceManager;
    }
    
    /**
     * 获取会话管理器
     * @return 会话管理器实例
     */
    public SessionManager getSessionManager() {
        ensureServiceAvailable();
        return sessionManager;
    }
    
    /**
     * 获取屏幕镜像服务
     * @return 屏幕镜像服务实例
     */
    public ScreenMirrorService getScreenMirrorService() {
        ensureServiceAvailable();
        return screenMirrorService;
    }
    
    /**
     * 获取配置
     * @return 配置对象
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    /**
     * 检查服务是否已初始化
     * @return 如果已初始化，返回 true
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 检查服务是否已关闭
     * @return 如果已关闭，返回 true
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * 确保服务可用（已初始化且未关闭）
     * @throws IllegalStateException 当服务不可用时抛出
     */
    private void ensureServiceAvailable() {
        if (closed) {
            throw new IllegalStateException("Service is closed");
        }
        
        if (!initialized) {
            throw new IllegalStateException("Service is not initialized");
        }
    }
}