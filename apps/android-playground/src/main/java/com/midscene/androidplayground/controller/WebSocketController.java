package com.midscene.androidplayground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import com.midscene.androidplayground.service.ScrcpyPlayerService;
import com.midscene.androidplayground.service.AdbDeviceManager;

@Controller
@CrossOrigin(origins = "*")
public class WebSocketController {

    private static final Logger logger = Logger.getLogger(WebSocketController.class.getName());

    @Autowired
    private ScrcpyPlayerService scrcpyPlayerService;

    @Autowired
    private AdbDeviceManager adbDeviceManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle device connection request
     */
    @MessageMapping("/connect-device")
    @SendTo("/topic/device-connection-status")
    public Map<String, Object> connectDevice(@Payload Map<String, Object> payload) {
        logger.info("Connect device request: " + payload);
        
        String deviceId = (String) payload.get("deviceId");
        // If no deviceId provided, generate a session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Connect to the device using scrcpy
        scrcpyPlayerService.connectDevice(deviceId);
        
        // Return connection status
        return Map.of(
            "status", "connected",
            "deviceId", deviceId,
            "sessionId", sessionId
        );
    }

    /**
     * Handle device disconnection request
     */
    @MessageMapping("/disconnect-device")
    @SendTo("/topic/device-connection-status")
    public Map<String, Object> disconnectDevice(@Payload Map<String, Object> payload) {
        logger.info("Disconnect device request: " + payload);
        
        String sessionId = (String) payload.get("sessionId");
        scrcpyPlayerService.disconnectDevice(sessionId);
        
        return Map.of(
            "status", "disconnected",
            "sessionId", sessionId
        );
    }

    /**
     * Handle screenshot request
     */
    @MessageMapping("/take-screenshot")
    public void takeScreenshot(@Payload Map<String, Object> payload) {
        logger.info("Screenshot request: " + payload);
        
        String sessionId = (String) payload.get("sessionId");
        scrcpyPlayerService.takeScreenshot(sessionId);
    }

    /**
     * Get list of connected devices
     */
    @MessageMapping("/get-devices")
    @SendTo("/topic/devices-list")
    public Map<String, Object> getDevices() {
        logger.info("Get devices request");
        
        var devices = adbDeviceManager.getConnectedDevices();
        return Map.of(
            "devices", devices,
            "total", devices.size()
        );
    }

    /**
     * Connect to device via TCP/IP
     */
    @MessageMapping("/connect-tcpip")
    @SendTo("/topic/tcpip-connection-status")
    public Map<String, Object> connectTcpIp(@Payload Map<String, Object> payload) {
        logger.info("TCP/IP connection request: " + payload);
        
        String ipAddress = (String) payload.get("ipAddress");
        boolean success = adbDeviceManager.connectToDevice(ipAddress);
        
        return Map.of(
            "success", success,
            "ipAddress", ipAddress,
            "message", success ? "Connected successfully" : "Failed to connect"
        );
    }

    /**
     * Send heartbeat response
     */
    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public Map<String, Object> heartbeat() {
        return Map.of(
            "timestamp", System.currentTimeMillis(),
            "status", "alive"
        );
    }
}