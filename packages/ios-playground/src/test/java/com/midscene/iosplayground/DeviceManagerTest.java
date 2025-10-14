package com.midscene.iosplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeviceManagerTest {
    private DeviceManager deviceManager;
    private DeviceList deviceList;

    @BeforeEach
    void setUp() {
        // 使用真实的DeviceList实例
        deviceList = new DeviceList();
        deviceManager = new DeviceManager();
    }

    @Test
    void testRefreshDeviceList() {
        // 测试刷新设备列表的基本功能
        List<IOSDevice> devices = deviceManager.refreshDeviceList();
        assertNotNull(devices);
        // 注意：实际实现中会添加模拟设备，所以可能不为空
    }

    @Test
    void testGetConnectedDevices() {
        // 测试获取已连接设备列表
        List<IOSDevice> devices = deviceManager.getConnectedDevices();
        assertNotNull(devices);
    }

    @Test
    void testGetDevice() {
        // 测试获取不存在的设备
        Optional<IOSDevice> result = deviceManager.getDevice("non-existent-device");
        assertFalse(result.isPresent());
    }

    @Test
    void testConnectDevice() {
        // 测试连接设备的基本逻辑
        boolean result = deviceManager.connectDevice("test-device");
        assertFalse(result); // 不存在的设备应该返回false
    }

    @Test
    void testDisconnectDevice() {
        // 测试断开设备连接
        boolean result = deviceManager.disconnectDevice("test-device");
        assertFalse(result);
    }

    @Test
    void testRestartDevice() {
        // 测试重启设备
        boolean result = deviceManager.restartDevice("test-device");
        assertFalse(result);
    }

    @Test
    void testIsDeviceConnected() {
        // 测试设备连接状态检查
        boolean result = deviceManager.isDeviceConnected("test-device");
        assertFalse(result);
    }

    @Test
    void testStartMonitoring() {
        // 测试开始监控设备连接状态
        deviceManager.startMonitoring();
        // 验证方法能正常调用
    }

    @Test
    void testStopMonitoring() {
        // 测试停止监控设备连接状态
        deviceManager.stopMonitoring();
        // 验证方法能正常调用
    }

    @Test
    void testGetDeviceInfo() {
        // 测试获取设备详细信息
        Optional<IOSDevice> result = deviceManager.getDeviceInfo("test-device");
        assertFalse(result.isPresent());
    }
}