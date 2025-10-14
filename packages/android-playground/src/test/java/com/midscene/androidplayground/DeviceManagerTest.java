package com.midscene.androidplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeviceManagerTest {
    private DeviceManager deviceManager;

    @BeforeEach
    void setUp() {
        deviceManager = new DeviceManager();
    }

    @Test
    void testRefreshDeviceList() {
        // 测试刷新设备列表方法返回值的基本特性
        Object result = deviceManager.refreshDeviceList();
        assertNotNull(result, "返回值不应为null");
    }

    @Test
    void testGetConnectedDevices() {
        // 测试获取已连接设备方法返回值的基本特性
        Object result = deviceManager.getConnectedDevices();
        assertNotNull(result, "返回值不应为null");
    }

    @Test
    void testGetDevice() {
        // 测试获取不存在设备的情况
        Object result = deviceManager.getDevice("non-existent-device");
        assertNull(result, "获取不存在设备时应返回null");
    }

    @Test
    void testIsDeviceConnected() {
        // 测试检查不存在设备连接状态的情况
        boolean result = deviceManager.isDeviceConnected("non-existent-device");
        assertFalse(result, "检查不存在设备连接状态时应返回false");
    }
}