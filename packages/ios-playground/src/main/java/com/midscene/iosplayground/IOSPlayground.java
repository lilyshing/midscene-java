package com.midscene.iosplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * iOS Playground主入口类，提供iOS设备管理和会话控制的核心功能
 */
public class IOSPlayground {
    private static final Logger logger = LoggerFactory.getLogger(IOSPlayground.class);
    private final DeviceManager deviceManager;
    private final SessionManager sessionManager;
    private boolean isInitialized = false;

    /**
     * 构造函数
     */
    public IOSPlayground() {
        this.deviceManager = new DeviceManager();
        this.sessionManager = new SessionManager();
        logger.info("iOSPlayground initialized");
    }

    /**
     * 初始化iOS Playground
     */
    public boolean initialize() {
        if (isInitialized) {
            logger.warn("iOSPlayground already initialized");
            return true;
        }

        logger.info("Initializing iOS Playground");
        try {
            // 启动设备监控
            deviceManager.startMonitoring();
            
            // 初始刷新设备列表
            deviceManager.refreshDeviceList();
            
            isInitialized = true;
            logger.info("iOS Playground initialized successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize iOS Playground", e);
            return false;
        }
    }

    /**
     * 关闭iOS Playground
     */
    public void close() {
        if (!isInitialized) {
            logger.warn("iOSPlayground not initialized");
            return;
        }

        logger.info("Closing iOS Playground");
        try {
            // 关闭所有会话
            sessionManager.closeAllSessions();
            
            // 停止设备监控
            deviceManager.stopMonitoring();
            
            isInitialized = false;
            logger.info("iOS Playground closed successfully");
        } catch (Exception e) {
            logger.error("Error closing iOS Playground", e);
        }
    }

    /**
     * 获取已连接设备列表
     */
    public List<IOSDevice> getConnectedDevices() {
        ensureInitialized();
        return deviceManager.getConnectedDevices();
    }

    /**
     * 刷新设备列表
     */
    public List<IOSDevice> refreshDeviceList() {
        ensureInitialized();
        return deviceManager.refreshDeviceList();
    }

    /**
     * 检查设备是否已连接
     */
    public boolean isDeviceConnected(String deviceId) {
        ensureInitialized();
        return deviceManager.isDeviceConnected(deviceId);
    }

    /**
     * 获取设备信息
     */
    public Optional<IOSDevice> getDevice(String deviceId) {
        ensureInitialized();
        return deviceManager.getDevice(deviceId);
    }

    /**
     * 创建新会话
     */
    public Optional<PlaygroundSession> createSession(String deviceId) {
        ensureInitialized();
        return sessionManager.createSession(deviceId);
    }

    /**
     * 获取会话
     */
    public Optional<PlaygroundSession> getSession(String sessionId) {
        ensureInitialized();
        return sessionManager.getSession(sessionId);
    }

    /**
     * 关闭会话
     */
    public boolean closeSession(String sessionId) {
        ensureInitialized();
        return sessionManager.closeSession(sessionId);
    }

    /**
     * 连接设备（WiFi）
     */
    public boolean connectDevice(String deviceId) {
        ensureInitialized();
        return deviceManager.connectDevice(deviceId);
    }

    /**
     * 断开设备连接
     */
    public boolean disconnectDevice(String deviceId) {
        ensureInitialized();
        return deviceManager.disconnectDevice(deviceId);
    }

    /**
     * 重启设备
     */
    public boolean restartDevice(String deviceId) {
        ensureInitialized();
        return deviceManager.restartDevice(deviceId);
    }

    /**
     * 获取设备管理器
     */
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    /**
     * 获取会话管理器
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("iOSPlayground not initialized");
        }
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        ensureInitialized();
        return sessionManager.getActiveSessionCount();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        ensureInitialized();
        sessionManager.cleanupExpiredSessions();
    }
}