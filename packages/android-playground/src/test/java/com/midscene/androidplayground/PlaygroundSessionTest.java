package com.midscene.androidplayground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaygroundSessionTest {
    private PlaygroundSession session;
    @Mock
    private AndroidDevice mockDevice;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDevice.getDeviceId()).thenReturn("test-device-id");
        session = new PlaygroundSession("test-session-id", mockDevice);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(session);
        assertEquals("test-session-id", session.getSessionId());
        assertEquals(mockDevice, session.getDevice());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastActivityTime());
        assertFalse(session.isClosed());
    }
    
    @Test
    void testGetters() {
        assertEquals("test-session-id", session.getSessionId());
        assertEquals(mockDevice, session.getDevice());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastActivityTime());
    }
    
    @Test
    void testSessionIdGeneration() {
        assertNotNull(session.getSessionId());
        assertFalse(session.getSessionId().isEmpty());
    }
    
    @Test
    void testClose() {
        assertFalse(session.isClosed());
        session.close();
        assertTrue(session.isClosed());
        
        session.close();
        assertTrue(session.isClosed());
    }
    
    @Test
    void testClosePreventsFurtherOperations() {
        session.close();
        assertTrue(session.isClosed());
        
        assertThrows(IllegalStateException.class, () -> {
            session.executeCommand("test-command");
        });
        
        assertThrows(IllegalStateException.class, () -> {
            session.installApp("test.apk");
        });
        
        assertThrows(IllegalStateException.class, () -> {
            session.launchApp("com.test.app");
        });
    }
    
    @Test
    void testExecuteCommand() throws PlaygroundException {
        PlaygroundSession.CommandResult result = session.executeCommand("test-command");
        assertNotNull(result);
        assertEquals("test-command", result.getCommand());
        assertEquals(0, result.getExitCode());
        assertTrue(result.isSuccess());
        assertEquals("Command executed successfully", result.getStdout());
        assertEquals("", result.getStderr());
    }
    
    @Test
    void testInstallApp() throws PlaygroundException {
        String apkPath = "test-app.apk";
        PlaygroundSession.InstallResult result = session.installApp(apkPath);
        assertNotNull(result);
        assertEquals(apkPath, result.getApkPath());
        assertTrue(result.isSuccess());
        assertEquals("Successfully installed", result.getMessage());
    }
    
    @Test
    void testLaunchApp() throws PlaygroundException {
        String packageName = "com.test.app";
        boolean result = session.launchApp(packageName);
        assertTrue(result);
    }
    
    @Test
    void testCommandResultClass() {
        PlaygroundSession.CommandResult result = new PlaygroundSession.CommandResult("command", 0, "stdout", "stderr");
        assertEquals("command", result.getCommand());
        assertEquals(0, result.getExitCode());
        assertEquals("stdout", result.getStdout());
        assertEquals("stderr", result.getStderr());
        assertTrue(result.isSuccess());
        
        PlaygroundSession.CommandResult errorResult = new PlaygroundSession.CommandResult("error-command", 1, "", "error-output");
        assertEquals("error-command", errorResult.getCommand());
        assertEquals(1, errorResult.getExitCode());
        assertEquals("", errorResult.getStdout());
        assertEquals("error-output", errorResult.getStderr());
        assertFalse(errorResult.isSuccess());
    }
    
    @Test
    void testInstallResultClass() {
        PlaygroundSession.InstallResult result = new PlaygroundSession.InstallResult("test.apk", true, "Success");
        assertEquals("test.apk", result.getApkPath());
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getMessage());
        
        PlaygroundSession.InstallResult errorResult = new PlaygroundSession.InstallResult("test.apk", false, "Failed");
        assertEquals("test.apk", errorResult.getApkPath());
        assertFalse(errorResult.isSuccess());
        assertEquals("Failed", errorResult.getMessage());
    }
}