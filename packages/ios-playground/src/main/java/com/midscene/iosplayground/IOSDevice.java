package com.midscene.iosplayground;

import java.util.Objects;

/**
 * iOS设备模型类，代表一个连接的iOS设备
 */
public class IOSDevice {
    private String deviceId; // 设备唯一标识符
    private String name; // 设备名称
    private String udid; // 设备UDID
    private String version; // iOS版本
    private boolean isConnected; // 连接状态
    private boolean isUsbConnected; // USB连接状态
    private boolean isWifiConnected; // WiFi连接状态

    public IOSDevice(String deviceId, String udid) {
        this.deviceId = deviceId;
        this.udid = udid;
        this.isConnected = true;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isUsbConnected() {
        return isUsbConnected;
    }

    public void setUsbConnected(boolean usbConnected) {
        isUsbConnected = usbConnected;
    }

    public boolean isWifiConnected() {
        return isWifiConnected;
    }

    public void setWifiConnected(boolean wifiConnected) {
        isWifiConnected = wifiConnected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IOSDevice iosDevice = (IOSDevice) o;
        return Objects.equals(deviceId, iosDevice.deviceId) && Objects.equals(udid, iosDevice.udid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, udid);
    }

    @Override
    public String toString() {
        return "IOSDevice{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", udid='" + udid + '\'' +
                ", version='" + version + '\'' +
                ", isConnected=" + isConnected +
                '}';
    }
}