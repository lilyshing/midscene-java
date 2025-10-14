package com.midscene.androidplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AndroidDeviceTest {
    private AndroidDevice device;

    @BeforeEach
    void setUp() {
        // 根据实际构造函数参数创建设备
        device = new AndroidDevice("device-123", "192.168.1.100", 5555, DeviceConnectionType.TCP_IP);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("device-123", device.getDeviceId());
        assertEquals("192.168.1.100", device.getIpAddress());
        assertEquals(5555, device.getPort());
        assertEquals(DeviceConnectionType.TCP_IP, device.getConnectionType());
        assertNotNull(device.getConnectedAt());
        assertEquals(DeviceStatus.ONLINE, device.getStatus());
    }

    @Test
    void testSetters() {
        device.setDeviceName("Pixel 5");
        device.setModel("Pixel 5");
        device.setAndroidVersion("13");
        device.setApiLevel(33);
        device.setStatus(DeviceStatus.OFFLINE);

        assertEquals("Pixel 5", device.getDeviceName());
        assertEquals("Pixel 5", device.getModel());
        assertEquals("13", device.getAndroidVersion());
        assertEquals(33, device.getApiLevel());
        assertEquals(DeviceStatus.OFFLINE, device.getStatus());
    }

    @Test
    void testDeviceEqualityByFields() {
        // 比较具有相同deviceId的设备的字段值
        AndroidDevice sameDevice = new AndroidDevice("device-123", "192.168.1.200", 5555, DeviceConnectionType.TCP_IP);
        AndroidDevice differentDevice = new AndroidDevice("device-456", "192.168.1.100", 5555, DeviceConnectionType.USB);

        // 比较字段而不是直接使用equals方法
        assertEquals(device.getDeviceId(), sameDevice.getDeviceId());
        assertNotEquals(device.getDeviceId(), differentDevice.getDeviceId());
        
        // 即使deviceId相同，其他字段可能不同
        assertNotEquals(device.getIpAddress(), sameDevice.getIpAddress());
        assertEquals(device.getPort(), sameDevice.getPort());
        assertEquals(device.getConnectionType(), sameDevice.getConnectionType());
    }

    @Test
    void testToString() {
        device.setModel("Pixel 5");
        device.setAndroidVersion("13");
        String toString = device.toString();
        assertTrue(toString.contains("device-123"));
        assertTrue(toString.contains("Pixel 5"));
        assertTrue(toString.contains("13"));
        assertTrue(toString.contains("TCP_IP"));
        assertTrue(toString.contains("ONLINE"));
    }

    @Test
    void testDeviceStatus() {
        // 测试状态变更
        device.setStatus(DeviceStatus.ONLINE);
        assertEquals(DeviceStatus.ONLINE, device.getStatus());
        
        device.setStatus(DeviceStatus.OFFLINE);
        assertEquals(DeviceStatus.OFFLINE, device.getStatus());
    }

    @Test
    void testConnectedAt() {
        // 验证connectedAt不为null且是LocalDateTime类型
        assertNotNull(device.getConnectedAt());
        assertTrue(device.getConnectedAt() instanceof LocalDateTime);
    }

    @Test
    void testConnectionType() {
        // 测试不同的连接类型
        AndroidDevice usbDevice = new AndroidDevice(
                "usb-device", 
                "127.0.0.1", 
                5037, 
                DeviceConnectionType.USB
        );
        
        AndroidDevice tcpIpDevice = new AndroidDevice(
                "tcp-ip-device", 
                "192.168.1.101", 
                5555, 
                DeviceConnectionType.TCP_IP
        );
        
        AndroidDevice emulatorDevice = new AndroidDevice(
                "emulator-5554", 
                "127.0.0.1", 
                5554, 
                DeviceConnectionType.EMULATOR
        );
        
        assertEquals(DeviceConnectionType.TCP_IP, device.getConnectionType());
        assertEquals(DeviceConnectionType.USB, usbDevice.getConnectionType());
        assertEquals(DeviceConnectionType.TCP_IP, tcpIpDevice.getConnectionType());
        assertEquals(DeviceConnectionType.EMULATOR, emulatorDevice.getConnectionType());
    }
}