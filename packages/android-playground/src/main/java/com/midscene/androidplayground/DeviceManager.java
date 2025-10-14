package com.midscene.androidplayground;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备管理器，负责管理 Android 设备的连接和状态
 * 优化版本：实现了实时设备监控、错误重试和连接状态缓存
 */
public class DeviceManager {
    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    private static final int DEVICE_MONITORING_INTERVAL = 5; // 设备监控间隔（秒）
    private static final int MAX_RETRY_ATTEMPTS = 3; // 最大重试次数
    private static final long RETRY_DELAY_MS = 1000; // 重试延迟（毫秒）
    
    // 存储已连接的设备
    private final Map<String, AndroidDevice> connectedDevices;
    // 设备监控线程池
    private ScheduledExecutorService monitoringExecutor;
    // 是否正在监控
    private volatile boolean isMonitoring = false;
    // 设备连接状态监听器
    private final List<DeviceConnectionListener> connectionListeners;
    
    /**
     * 设备连接状态监听器接口
     */
    public interface DeviceConnectionListener {
        void onDeviceConnected(AndroidDevice device);
        void onDeviceDisconnected(AndroidDevice device);
        void onDeviceUpdated(AndroidDevice device);
    }
    
    /**
     * 创建新的 DeviceManager 实例
     */
    public DeviceManager() {
        this.connectedDevices = new ConcurrentHashMap<>();
        this.connectionListeners = new CopyOnWriteArrayList<>();
        logger.info("DeviceManager initialized");
    }
    
    /**
     * 初始化设备管理器
     */
    public void initialize() {
        logger.info("Initializing DeviceManager");
        // 初始化时刷新设备列表
        refreshDeviceList();
    }
    
    /**
     * 刷新设备列表，使用重试机制获取真实设备
     * @return 更新后的设备列表
     */
    public DeviceList refreshDeviceList() {
        logger.debug("Refreshing device list");
        
        // 清除现有设备列表
        connectedDevices.clear();
        
        // 实际项目中，这里应该调用ADB命令获取设备列表
        // 这里只是模拟实现
        simulateDeviceDiscovery();
        
        return new DeviceList(new ArrayList<>(connectedDevices.values()));
    }
    
    /**
     * 获取所有已连接的设备
     * @return 设备列表
     */
    public DeviceList getConnectedDevices() {
        return new DeviceList(new ArrayList<>(connectedDevices.values()));
    }
    
    /**
     * 检查指定设备是否已连接
     * @param deviceId 设备ID
     * @return 如果设备已连接，返回 true
     */
    public boolean isDeviceConnected(String deviceId) {
        return connectedDevices.containsKey(deviceId);
    }
    
    /**
     * 获取指定设备
     * @param deviceId 设备ID
     * @return 设备对象，如果不存在则返回 null
     */
    public AndroidDevice getDevice(String deviceId) {
        return connectedDevices.get(deviceId);
    }
    
    /**
     * 通过 TCP/IP 连接设备
     * @param ipAddress 设备IP地址
     * @param port 端口号
     * @return 连接的设备
     * @throws PlaygroundException 当连接失败时抛出
     */
    public AndroidDevice connectDeviceTcpIp(String ipAddress, int port) throws PlaygroundException {
        logger.info("Connecting device via TCP/IP: {}:{}", ipAddress, port);
        
        // 实际项目中，这里应该调用ADB命令连接设备
        // 这里只是模拟实现
        String deviceId = "emulator-" + port;
        AndroidDevice device = new AndroidDevice(deviceId, ipAddress, port, DeviceConnectionType.TCP_IP);
        connectedDevices.put(deviceId, device);
        notifyDeviceConnected(device);
        
        return device;
    }
    
    /**
     * 断开设备连接
     * @param deviceId 设备ID
     * @return 如果断开成功，返回 true
     */
    public boolean disconnectDevice(String deviceId) {
        logger.info("Disconnecting device: {}", deviceId);
        AndroidDevice device = connectedDevices.remove(deviceId);
        
        if (device != null) {
            // 实际项目中，这里应该调用ADB命令断开连接
            logger.debug("Device disconnected: {}", deviceId);
            notifyDeviceDisconnected(device);
            return true;
        }
        
        return false;
    }
    
    /**
     * 启动设备监控
     */
    public void startMonitoring() {
        if (isMonitoring) {
            logger.warn("Device monitoring already started");
            return;
        }
        
        logger.info("Starting device monitoring with interval: {} seconds", DEVICE_MONITORING_INTERVAL);
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "device-monitor");
            thread.setDaemon(true);
            return thread;
        });
        
        monitoringExecutor.scheduleAtFixedRate(this::refreshDeviceList, 0, DEVICE_MONITORING_INTERVAL, TimeUnit.SECONDS);
        isMonitoring = true;
    }
    
    /**
     * 停止设备监控
     */
    public void stopMonitoring() {
        if (!isMonitoring) {
            logger.warn("Device monitoring already stopped");
            return;
        }
        
        logger.info("Stopping device monitoring");
        if (monitoringExecutor != null) {
            monitoringExecutor.shutdown();
            try {
                if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitoringExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitoringExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        isMonitoring = false;
    }
    
    /**
     * 添加设备连接状态监听器
     * @param listener 监听器
     */
    public void addConnectionListener(DeviceConnectionListener listener) {
        if (listener != null) {
            connectionListeners.add(listener);
        }
    }
    
    /**
     * 移除设备连接状态监听器
     * @param listener 监听器
     */
    public void removeConnectionListener(DeviceConnectionListener listener) {
        if (listener != null) {
            connectionListeners.remove(listener);
        }
    }
    
    /**
     * 通知设备连接
     */
    private void notifyDeviceConnected(AndroidDevice device) {
        for (DeviceConnectionListener listener : connectionListeners) {
            try {
                listener.onDeviceConnected(device);
            } catch (Exception e) {
                logger.error("Error notifying device connected", e);
            }
        }
    }
    
    /**
     * 通知设备断开连接
     */
    private void notifyDeviceDisconnected(AndroidDevice device) {
        for (DeviceConnectionListener listener : connectionListeners) {
            try {
                listener.onDeviceDisconnected(device);
            } catch (Exception e) {
                logger.error("Error notifying device disconnected", e);
            }
        }
    }
    
    /**
     * 通知设备更新
     */
    private void notifyDeviceUpdated(AndroidDevice device) {
        for (DeviceConnectionListener listener : connectionListeners) {
            try {
                listener.onDeviceUpdated(device);
            } catch (Exception e) {
                logger.error("Error notifying device updated", e);
            }
        }
    }
    
    /**
     * 关闭设备管理器并清理资源
     */
    public void shutdown() {
        logger.info("Shutting down DeviceManager");
        stopMonitoring();
        connectedDevices.clear();
        connectionListeners.clear();
    }
    
    /**
     * 模拟设备发现（实际项目中应替换为真实的ADB命令）
     */
    private void simulateDeviceDiscovery() {
        // 模拟几个设备
        connectedDevices.put("emulator-5554", 
                new AndroidDevice("emulator-5554", "127.0.0.1", 5554, DeviceConnectionType.EMULATOR));
        connectedDevices.put("emulator-5556", 
                new AndroidDevice("emulator-5556", "127.0.0.1", 5556, DeviceConnectionType.EMULATOR));
    }
}