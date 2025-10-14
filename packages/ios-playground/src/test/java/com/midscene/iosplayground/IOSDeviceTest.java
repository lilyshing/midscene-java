package com.midscene.iosplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IOSDeviceTest {
    private IOSDevice device;

    @BeforeEach
    void setUp() {
        device = new IOSDevice("device-123", "ABCDEF1234567890ABCDEF1234567890ABCDEF12");
        device.setName("iPhone 14");
        device.setVersion("iOS 17.0");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("device-123", device.getDeviceId());
        assertEquals("iPhone 14", device.getName());
        assertEquals("iOS 17.0", device.getVersion());
        assertEquals("ABCDEF1234567890ABCDEF1234567890ABCDEF12", device.getUdid());
        assertTrue(device.isConnected()); // 构造函数中默认设置为已连接
    }

    @Test
    void testSetters() {
        device.setConnected(false);
        device.setName("iPhone 15 Pro");
        device.setVersion("iOS 17.2");
        device.setUdid("1234567890ABCDEF1234567890ABCDEF12ABCDEF");
        device.setUsbConnected(true);
        device.setWifiConnected(false);

        assertFalse(device.isConnected());
        assertEquals("iPhone 15 Pro", device.getName());
        assertEquals("iOS 17.2", device.getVersion());
        assertEquals("1234567890ABCDEF1234567890ABCDEF12ABCDEF", device.getUdid());
        assertTrue(device.isUsbConnected());
        assertFalse(device.isWifiConnected());
    }

    @Test
    void testEqualsAndHashCode() {
        IOSDevice sameDevice = new IOSDevice("device-123", "ABCDEF1234567890ABCDEF1234567890ABCDEF12");
        IOSDevice differentDevice = new IOSDevice("device-456", "DifferentUDID");

        assertEquals(device, sameDevice);
        assertNotEquals(device, differentDevice);
        assertEquals(device.hashCode(), sameDevice.hashCode());
        assertNotEquals(device.hashCode(), differentDevice.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        assertNotEquals(device, null);
    }

    @Test
    void testEqualsWithDifferentType() {
        assertNotEquals(device, "not a device");
    }

    @Test
    void testToString() {
        String toString = device.toString();
        assertTrue(toString.contains("device-123"));
        assertTrue(toString.contains("iPhone 14"));
        assertTrue(toString.contains("iOS 17.0"));
    }

    @Test
    void testConnectionStates() {
        // 测试USB连接
        device.setUsbConnected(true);
        device.setWifiConnected(false);
        assertTrue(device.isUsbConnected());
        assertFalse(device.isWifiConnected());

        // 测试WiFi连接
        device.setUsbConnected(false);
        device.setWifiConnected(true);
        assertFalse(device.isUsbConnected());
        assertTrue(device.isWifiConnected());

        // 测试断开连接
        device.setConnected(false);
        assertFalse(device.isConnected());
    }

    @Test
    void testUdidValidation() {
        // 测试有效的UDID
        IOSDevice validDevice = new IOSDevice("device-valid", "0123456789ABCDEF0123456789ABCDEF01234567");
        assertEquals("0123456789ABCDEF0123456789ABCDEF01234567", validDevice.getUdid());
    }
}