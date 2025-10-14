package com.midscene.iosplayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * iOS设备列表管理器，负责维护和管理已连接的iOS设备
 */
public class DeviceList {
    private final List<IOSDevice> devices = new CopyOnWriteArrayList<>();

    /**
     * 添加设备到列表
     */
    public void addDevice(IOSDevice device) {
        if (device != null && !devices.contains(device)) {
            devices.add(device);
        }
    }

    /**
     * 从列表中移除设备
     */
    public void removeDevice(String deviceId) {
        devices.removeIf(device -> device.getDeviceId().equals(deviceId));
    }

    /**
     * 更新设备信息
     */
    public boolean updateDevice(IOSDevice updatedDevice) {
        if (updatedDevice == null) return false;
        
        for (int i = 0; i < devices.size(); i++) {
            IOSDevice device = devices.get(i);
            if (device.getDeviceId().equals(updatedDevice.getDeviceId())) {
                devices.set(i, updatedDevice);
                return true;
            }
        }
        return false;
    }

    /**
     * 根据设备ID获取设备
     */
    public Optional<IOSDevice> getDeviceById(String deviceId) {
        return devices.stream()
                .filter(device -> device.getDeviceId().equals(deviceId))
                .findFirst();
    }

    /**
     * 根据UDID获取设备
     */
    public Optional<IOSDevice> getDeviceByUdid(String udid) {
        return devices.stream()
                .filter(device -> device.getUdid().equals(udid))
                .findFirst();
    }

    /**
     * 获取所有设备列表
     */
    public List<IOSDevice> getAllDevices() {
        return new ArrayList<>(devices);
    }

    /**
     * 获取已连接的设备列表
     */
    public List<IOSDevice> getConnectedDevices() {
        return devices.stream()
                .filter(IOSDevice::isConnected)
                .collect(Collectors.toList());
    }

    /**
     * 检查设备是否已连接
     */
    public boolean isDeviceConnected(String deviceId) {
        return getDeviceById(deviceId)
                .map(IOSDevice::isConnected)
                .orElse(false);
    }

    /**
     * 清空设备列表
     */
    public void clear() {
        devices.clear();
    }

    /**
     * 获取设备数量
     */
    public int size() {
        return devices.size();
    }

    /**
     * 检查列表是否为空
     */
    public boolean isEmpty() {
        return devices.isEmpty();
    }
}