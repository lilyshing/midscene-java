package com.midscene.androidplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Android Playground 核心类，提供与 Android 设备交互的主要功能
 * 对应原 TypeScript 项目中的 android-playground 模块
 */
public class AndroidPlayground {
    private static final Logger logger = LoggerFactory.getLogger(AndroidPlayground.class);
    
    // 存储活跃的设备会话
    private final Map<String, PlaygroundSession> activeSessions;
    
    // 设备管理器
    private final DeviceManager deviceManager;
    
    // 屏幕镜像服务
    private final ScreenMirrorService screenMirrorService;
    
    /**
     * 创建新的 AndroidPlayground 实例
     */
    public AndroidPlayground() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.deviceManager = new DeviceManager();
        this.screenMirrorService = new ScreenMirrorService();
        logger.info("AndroidPlayground initialized");
    }
    
    /**
     * 初始化 Playground 环境
     */
    public void initialize() {
        logger.info("Initializing Android Playground environment");
        deviceManager.initialize();
        screenMirrorService.initialize();
    }
    
    /**
     * 获取已连接的 Android 设备列表
     * @return 设备列表
     */
    public DeviceList getConnectedDevices() {
        logger.debug("Retrieving connected devices");
        return deviceManager.getConnectedDevices();
    }
    
    /**
     * 为指定设备创建新的 Playground 会话
     * @param deviceId 设备ID
     * @return 新创建的会话
     * @throws PlaygroundException 当会话创建失败时抛出
     */
    public PlaygroundSession createSession(String deviceId) throws PlaygroundException {
        logger.info("Creating session for device: {}", deviceId);
        
        if (!deviceManager.isDeviceConnected(deviceId)) {
            throw new PlaygroundException("Device not connected: " + deviceId);
        }
        
        // 如果会话已存在，返回现有会话
        if (activeSessions.containsKey(deviceId)) {
            logger.debug("Session already exists for device: {}", deviceId);
            return activeSessions.get(deviceId);
        }
        
        // 创建新会话
        String sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
        PlaygroundSession session = new PlaygroundSession(
                sessionId,
                deviceManager.getDevice(deviceId)
        );
        
        // 存储会话
        activeSessions.put(deviceId, session);
        logger.debug("Created new session for device: {}", deviceId);
        return session;
    }
    
    /**
     * 获取指定设备的活动会话
     * @param deviceId 设备ID
     * @return 会话对象，如果不存在则返回null
     */
    public PlaygroundSession getSession(String deviceId) {
        return activeSessions.get(deviceId);
    }
    
    /**
     * 关闭指定设备的会话
     * @param deviceId 设备ID
     */
    public void closeSession(String deviceId) {
        logger.info("Closing session for device: {}", deviceId);
        PlaygroundSession session = activeSessions.remove(deviceId);
        if (session != null) {
            session.close();
            logger.debug("Session closed for device: {}", deviceId);
        }
    }
    
    /**
     * 关闭所有活动会话并清理资源
     */
    public void shutdown() {
        logger.info("Shutting down Android Playground");
        
        // 关闭所有会话
        for (String deviceId : activeSessions.keySet()) {
            closeSession(deviceId);
        }
        
        // 关闭服务
        screenMirrorService.shutdown();
        deviceManager.shutdown();
        
        logger.info("Android Playground shutdown complete");
    }
}