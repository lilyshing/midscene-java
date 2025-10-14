package com.midscene.iosplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaygroundSessionTest {
    private PlaygroundSession session;
    private IOSDevice mockDevice;

    @BeforeEach
    void setUp() {
        mockDevice = mock(IOSDevice.class);
        when(mockDevice.getDeviceId()).thenReturn("test-device-id");
        when(mockDevice.isConnected()).thenReturn(true);
        
        session = new PlaygroundSession(mockDevice);
    }

    @Test
    void testConstructorAndGetters() {
        assertNotNull(session.getSessionId());
        assertNotNull(session.getDevice());
        assertTrue(session.isActive());
        // getDuration方法返回毫秒数，只要大于等于0即可
        assertTrue(session.getDuration() >= 0);
    }

    @Test
    void testSessionIdGeneration() {
        // 验证sessionId是有效的UUID格式
        try {
            UUID.fromString(session.getSessionId());
            // 如果没有抛出异常，则sessionId是有效的UUID
            assertTrue(true);
        } catch (IllegalArgumentException e) {
            fail("Session ID should be a valid UUID");
        }
    }

    @Test
    void testExecuteCommand() {
        // 测试执行命令的基本功能
        String command = "ideviceinfo";
        PlaygroundSession.CommandResult result = session.executeCommand(command);
        
        assertNotNull(result);
        // 由于是模拟实现，这里只是检查结果对象不为空
    }

    @Test
    void testInstallApp() {
        // 测试安装应用
        String appPath = "/path/to/app.ipa";
        PlaygroundSession.CommandResult result = session.installApp(appPath);
        assertNotNull(result);
    }

    @Test
    void testLaunchApp() {
        // 测试启动应用
        String bundleId = "com.example.app";
        PlaygroundSession.CommandResult result = session.launchApp(bundleId);
        assertNotNull(result);
    }

    @Test
    void testStopApp() {
        // 测试停止应用
        String bundleId = "com.example.app";
        PlaygroundSession.CommandResult result = session.stopApp(bundleId);
        assertNotNull(result);
    }

    @Test
    void testTakeScreenshot() {
        // 测试截图功能
        String filePath = "/path/to/screenshot.png";
        PlaygroundSession.CommandResult result = session.takeScreenshot(filePath);
        assertNotNull(result);
    }

    @Test
    void testCommandResultClass() {
        // 测试CommandResult内部类
        PlaygroundSession.CommandResult result = new PlaygroundSession.CommandResult(true, "output");
        assertEquals("output", result.getOutput());
        assertTrue(result.isSuccess());
        
        PlaygroundSession.CommandResult errorResult = new PlaygroundSession.CommandResult(false, "error");
        assertEquals("error", errorResult.getOutput());
        assertFalse(errorResult.isSuccess());
    }
}