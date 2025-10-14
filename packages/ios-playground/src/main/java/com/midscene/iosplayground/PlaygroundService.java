package com.midscene.iosplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * iOS Playground服务类，整合设备管理、会话管理和屏幕镜像功能，提供统一的服务接口
 */
public class PlaygroundService {
    private static final Logger logger = LoggerFactory.getLogger(PlaygroundService.class);
    private final IOSPlayground iosPlayground;
    private final ScreenMirrorService screenMirrorService;
    private boolean isInitialized = false;

    /**
     * 构造函数
     */
    public PlaygroundService() {
        this.iosPlayground = new IOSPlayground();
        this.screenMirrorService = new ScreenMirrorService();
        logger.info("PlaygroundService initialized");
    }

    /**
     * 初始化服务
     */
    public boolean initialize() {
        if (isInitialized) {
            logger.warn("PlaygroundService already initialized");
            return true;
        }

        logger.info("Initializing PlaygroundService");
        try {
            // 初始化iOS Playground
            boolean playgroundInit = iosPlayground.initialize();
            if (!playgroundInit) {
                logger.error("Failed to initialize iOS Playground");
                return false;
            }

            // 启动屏幕镜像服务
            screenMirrorService.start();

            isInitialized = true;
            logger.info("PlaygroundService initialized successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize PlaygroundService", e);
            return false;
        }
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        if (!isInitialized) {
            logger.warn("PlaygroundService not initialized");
            return;
        }

        logger.info("Shutting down PlaygroundService");
        try {
            // 停止屏幕镜像服务
            screenMirrorService.stop();

            // 关闭iOS Playground
            iosPlayground.close();

            isInitialized = false;
            logger.info("PlaygroundService shut down successfully");
        } catch (Exception e) {
            logger.error("Error shutting down PlaygroundService", e);
        }
    }

    /**
     * 获取已连接设备列表
     */
    public List<IOSDevice> getConnectedDevices() {
        ensureInitialized();
        return iosPlayground.getConnectedDevices();
    }

    /**
     * 刷新设备列表
     */
    public List<IOSDevice> refreshDeviceList() {
        ensureInitialized();
        return iosPlayground.refreshDeviceList();
    }

    /**
     * 获取设备信息
     */
    public Optional<IOSDevice> getDevice(String deviceId) {
        ensureInitialized();
        return iosPlayground.getDevice(deviceId);
    }

    /**
     * 创建设备会话
     */
    public Optional<PlaygroundSession> createSession(String deviceId) {
        ensureInitialized();
        return iosPlayground.createSession(deviceId);
    }

    /**
     * 获取会话
     */
    public Optional<PlaygroundSession> getSession(String sessionId) {
        ensureInitialized();
        return iosPlayground.getSession(sessionId);
    }

    /**
     * 关闭会话
     */
    public boolean closeSession(String sessionId) {
        ensureInitialized();
        return iosPlayground.closeSession(sessionId);
    }

    /**
     * 开始设备屏幕镜像
     */
    public String startScreenMirror(String deviceId) {
        ensureInitialized();
        return screenMirrorService.startMirrorForDevice(deviceId);
    }

    /**
     * 停止设备屏幕镜像
     */
    public boolean stopScreenMirror(String deviceId) {
        ensureInitialized();
        return screenMirrorService.stopMirrorForDevice(deviceId);
    }

    /**
     * 发送触摸事件到设备
     */
    public boolean sendTouchEvent(String deviceId, int x, int y, String action) {
        ensureInitialized();
        return screenMirrorService.sendTouchEvent(deviceId, x, y, action);
    }

    /**
     * 发送键盘事件到设备
     */
    public boolean sendKeyEvent(String deviceId, String key, String action) {
        ensureInitialized();
        return screenMirrorService.sendKeyEvent(deviceId, key, action);
    }

    /**
     * 连接设备（WiFi）
     */
    public boolean connectDevice(String deviceId) {
        ensureInitialized();
        return iosPlayground.connectDevice(deviceId);
    }

    /**
     * 断开设备连接
     */
    public boolean disconnectDevice(String deviceId) {
        ensureInitialized();
        return iosPlayground.disconnectDevice(deviceId);
    }

    /**
     * 重启设备
     */
    public boolean restartDevice(String deviceId) {
        ensureInitialized();
        return iosPlayground.restartDevice(deviceId);
    }

    /**
     * 获取iOS Playground实例
     */
    public IOSPlayground getIOSPlayground() {
        return iosPlayground;
    }

    /**
     * 获取屏幕镜像服务实例
     */
    public ScreenMirrorService getScreenMirrorService() {
        return screenMirrorService;
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
            throw new IllegalStateException("PlaygroundService not initialized");
        }
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        ensureInitialized();
        return iosPlayground.getActiveSessionCount();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        ensureInitialized();
        iosPlayground.cleanupExpiredSessions();
    }
}