package com.midscene.androidplayground;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Android 设备类，代表一个连接的 Android 设备
 */
public class AndroidDevice implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 设备唯一标识
    private final String deviceId;
    
    // 设备IP地址
    private final String ipAddress;
    
    // 设备端口
    private final int port;
    
    // 连接类型
    private final DeviceConnectionType connectionType;
    
    // 设备名称（可能为空）
    private String deviceName;
    
    // 设备型号
    private String model;
    
    // Android版本
    private String androidVersion;
    
    // API级别
    private int apiLevel;
    
    // 连接时间
    private final LocalDateTime connectedAt;
    
    // 设备状态
    private DeviceStatus status;
    
    /**
     * 创建新的 AndroidDevice 实例
     * @param deviceId 设备ID
     * @param ipAddress IP地址
     * @param port 端口号
     * @param connectionType 连接类型
     */
    public AndroidDevice(String deviceId, String ipAddress, int port, DeviceConnectionType connectionType) {
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.connectionType = connectionType;
        this.connectedAt = LocalDateTime.now();
        this.status = DeviceStatus.ONLINE;
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取IP地址
     * @return IP地址
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * 获取端口号
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 获取连接类型
     * @return 连接类型
     */
    public DeviceConnectionType getConnectionType() {
        return connectionType;
    }
    
    /**
     * 获取设备名称
     * @return 设备名称
     */
    public String getDeviceName() {
        return deviceName;
    }
    
    /**
     * 设置设备名称
     * @param deviceName 设备名称
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    /**
     * 获取设备型号
     * @return 设备型号
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 设置设备型号
     * @param model 设备型号
     */
    public void setModel(String model) {
        this.model = model;
    }
    
    /**
     * 获取Android版本
     * @return Android版本
     */
    public String getAndroidVersion() {
        return androidVersion;
    }
    
    /**
     * 设置Android版本
     * @param androidVersion Android版本
     */
    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }
    
    /**
     * 获取API级别
     * @return API级别
     */
    public int getApiLevel() {
        return apiLevel;
    }
    
    /**
     * 设置API级别
     * @param apiLevel API级别
     */
    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }
    
    /**
     * 获取连接时间
     * @return 连接时间
     */
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }
    
    /**
     * 获取设备状态
     * @return 设备状态
     */
    public DeviceStatus getStatus() {
        return status;
    }
    
    /**
     * 设置设备状态
     * @param status 设备状态
     */
    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "AndroidDevice{" +
                "deviceId='" + deviceId + '\'' +
                ", model='" + model + '\'' +
                ", androidVersion='" + androidVersion + '\'' +
                ", connectionType=" + connectionType +
                ", status=" + status +
                '}';
    }
}