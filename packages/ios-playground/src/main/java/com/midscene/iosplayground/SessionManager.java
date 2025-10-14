package com.midscene.iosplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * iOS Playground会话管理器，负责创建、管理和关闭与iOS设备的会话
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private final DeviceManager deviceManager;
    private final Map<String, PlaygroundSession> sessions = new ConcurrentHashMap<>();

    /**
     * 构造函数
     */
    public SessionManager() {
        this.deviceManager = new DeviceManager();
        logger.info("SessionManager initialized");
    }

    /**
     * 创建新会话
     */
    public Optional<PlaygroundSession> createSession(String deviceId) {
        logger.info("Creating session for device: {}", deviceId);
        
        // 获取设备
        Optional<IOSDevice> deviceOpt = deviceManager.getDevice(deviceId);
        if (!deviceOpt.isPresent()) {
            logger.error("Device not found: {}", deviceId);
            return Optional.empty();
        }

        IOSDevice device = deviceOpt.get();
        
        // 检查设备是否已连接
        if (!device.isConnected()) {
            logger.error("Device not connected: {}", deviceId);
            return Optional.empty();
        }

        // 检查是否已存在会话
        PlaygroundSession existingSession = findSessionByDeviceId(deviceId);
        if (existingSession != null && existingSession.isActive()) {
            logger.warn("Session already exists for device: {}", deviceId);
            return Optional.of(existingSession);
        }

        // 创建新会话
        PlaygroundSession session = new PlaygroundSession(device);
        sessions.put(session.getSessionId(), session);
        logger.info("Session created successfully: {} for device {}", session.getSessionId(), deviceId);
        
        return Optional.of(session);
    }

    /**
     * 获取会话
     */
    public Optional<PlaygroundSession> getSession(String sessionId) {
        PlaygroundSession session = sessions.get(sessionId);
        if (session != null && session.isActive()) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * 关闭会话
     */
    public boolean closeSession(String sessionId) {
        logger.info("Closing session: {}", sessionId);
        
        PlaygroundSession session = sessions.get(sessionId);
        if (session != null) {
            session.close();
            sessions.remove(sessionId);
            logger.info("Session closed: {}", sessionId);
            return true;
        }
        
        logger.warn("Session not found: {}", sessionId);
        return false;
    }

    /**
     * 获取设备管理器
     */
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    /**
     * 通过设备ID查找会话
     */
    private PlaygroundSession findSessionByDeviceId(String deviceId) {
        for (PlaygroundSession session : sessions.values()) {
            if (session.isActive() && session.getDevice().getDeviceId().equals(deviceId)) {
                return session;
            }
        }
        return null;
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(PlaygroundSession::isActive)
                .count();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        logger.info("Cleaning up expired sessions");
        long currentTime = System.currentTimeMillis();
        
        sessions.entrySet().removeIf(entry -> {
            PlaygroundSession session = entry.getValue();
            if (!session.isActive()) {
                logger.info("Removing inactive session: {}", entry.getKey());
                return true;
            }
            // 可以添加超时逻辑
            return false;
        });
    }

    /**
     * 关闭所有会话
     */
    public void closeAllSessions() {
        logger.info("Closing all sessions");
        for (String sessionId : sessions.keySet()) {
            closeSession(sessionId);
        }
    }
}