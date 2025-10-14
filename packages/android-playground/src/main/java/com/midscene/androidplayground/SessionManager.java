package com.midscene.androidplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * 会话管理器，负责创建和管理与设备的交互会话
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // 存储活跃的会话
    private final Map<String, PlaygroundSession> activeSessions;
    // 设备管理器
    private final DeviceManager deviceManager;
    
    /**
     * 创建新的会话管理器
     * @param deviceManager 设备管理器实例
     */
    public SessionManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        this.activeSessions = new ConcurrentHashMap<>();
        logger.info("SessionManager initialized");
    }
    
    /**
     * 初始化会话管理器
     */
    public void initialize() {
        logger.info("Initializing SessionManager");
        // 初始化必要的资源
    }
    
    /**
     * 为指定设备创建新的会话
     * @param deviceId 设备ID
     * @return 新创建的会话
     * @throws PlaygroundException 当设备不可用或创建会话失败时抛出
     */
    public PlaygroundSession createSession(String deviceId) throws PlaygroundException {
        logger.info("Creating new session for device: {}", deviceId);
        
        // 检查设备是否存在并且在线
        AndroidDevice device = deviceManager.getDevice(deviceId);
        if (device == null) {
            throw new PlaygroundException("Device not found: " + deviceId);
        }
        
        if (device.getStatus() != DeviceStatus.ONLINE) {
            throw new PlaygroundException("Device is not online: " + deviceId);
        }
        
        try {
            // 检查是否已经存在会话
            PlaygroundSession existingSession = activeSessions.get(deviceId);
            if (existingSession != null && !existingSession.isClosed()) {
                logger.debug("Session already exists for device: {}", deviceId);
                return existingSession;
            }
            
            // 创建新会话
            String sessionId = generateSessionId();
            PlaygroundSession session = new PlaygroundSession(sessionId, device);
            
            // 存储会话
            activeSessions.put(sessionId, session);
            logger.info("Created new session: {} for device: {}", sessionId, deviceId);
            
            return session;
        } catch (Exception e) {
            logger.error("Failed to create session for device: {}", deviceId, e);
            throw new PlaygroundException("Failed to create session: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成会话ID
     * @return 唯一的会话ID
     */
    private String generateSessionId() {
        return "session-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 获取指定会话ID的会话
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回 null
     */
    public PlaygroundSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * 关闭指定会话
     * @param sessionId 会话ID
     * @return 如果成功关闭，返回 true
     */
    public boolean closeSession(String sessionId) {
        logger.info("Closing session: {}", sessionId);
        
        PlaygroundSession session = activeSessions.remove(sessionId);
        if (session != null) {
            try {
                session.close();
                logger.info("Session closed successfully: {}", sessionId);
                return true;
            } catch (Exception e) {
                logger.error("Error closing session: {}", sessionId, e);
            }
        }
        
        return false;
    }
    
    /**
     * 获取设备对应的会话
     * @param deviceId 设备ID
     * @return 会话对象，如果不存在则返回 null
     */
    public PlaygroundSession getSessionByDeviceId(String deviceId) {
        for (PlaygroundSession session : activeSessions.values()) {
            if (session.getDevice().getDeviceId().equals(deviceId)) {
                return session;
            }
        }
        return null;
    }
    
    /**
     * 关闭与指定设备相关的所有会话
     * @param deviceId 设备ID
     */
    public void closeSessionsForDevice(String deviceId) {
        logger.info("Closing all sessions for device: {}", deviceId);
        
        for (String sessionId : activeSessions.keySet()) {
            PlaygroundSession session = activeSessions.get(sessionId);
            if (session.getDevice().getDeviceId().equals(deviceId)) {
                closeSession(sessionId);
            }
        }
    }
    
    /**
     * 获取活跃会话数量
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * 关闭所有会话并清理资源
     */
    public void shutdown() {
        logger.info("Shutting down SessionManager");
        
        // 关闭所有会话
        for (String sessionId : activeSessions.keySet()) {
            closeSession(sessionId);
        }
        
        activeSessions.clear();
        logger.info("SessionManager shutdown complete");
    }
}