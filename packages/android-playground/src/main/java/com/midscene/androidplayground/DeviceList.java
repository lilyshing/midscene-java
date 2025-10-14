package com.midscene.androidplayground;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备列表类，封装已连接设备的集合
 */
public class DeviceList implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 设备列表
    private final List<AndroidDevice> devices;
    
    /**
     * 创建新的 DeviceList 实例
     * @param devices 设备列表
     */
    public DeviceList(List<AndroidDevice> devices) {
        this.devices = Collections.unmodifiableList(devices != null ? devices : Collections.emptyList());
    }
    
    /**
     * 获取设备列表
     * @return 不可修改的设备列表
     */
    public List<AndroidDevice> getDevices() {
        return devices;
    }
    
    /**
     * 获取设备数量
     * @return 设备数量
     */
    public int getCount() {
        return devices.size();
    }
    
    /**
     * 检查是否包含指定ID的设备
     * @param deviceId 设备ID
     * @return 如果包含，返回 true
     */
    public boolean containsDevice(String deviceId) {
        return devices.stream().anyMatch(device -> device.getDeviceId().equals(deviceId));
    }
    
    /**
     * 获取指定ID的设备
     * @param deviceId 设备ID
     * @return 设备对象，如果不存在则返回 null
     */
    public AndroidDevice getDeviceById(String deviceId) {
        return devices.stream()
                .filter(device -> device.getDeviceId().equals(deviceId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取在线设备列表
     * @return 在线设备列表
     */
    public List<AndroidDevice> getOnlineDevices() {
        return devices.stream()
                .filter(device -> device.getStatus() == DeviceStatus.ONLINE)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取特定连接类型的设备
     * @param connectionType 连接类型
     * @return 符合条件的设备列表
     */
    public List<AndroidDevice> getDevicesByConnectionType(DeviceConnectionType connectionType) {
        return devices.stream()
                .filter(device -> device.getConnectionType() == connectionType)
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "DeviceList{count=" + devices.size() + "}";
    }
}