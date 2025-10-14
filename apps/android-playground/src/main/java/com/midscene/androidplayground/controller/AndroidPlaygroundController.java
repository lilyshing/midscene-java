package com.midscene.androidplayground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.midscene.androidplayground.service.AdbDeviceManager;

@RestController
@RequestMapping("/api/android-playground")
public class AndroidPlaygroundController {

    @Autowired
    private AdbDeviceManager adbDeviceManager;

    /**
     * Get list of connected Android devices
     */
    @GetMapping("/devices")
    public ResponseEntity<?> getDevices() {
        var devices = adbDeviceManager.getConnectedDevices();
        return ResponseEntity.ok(Map.of(
            "devices", devices,
            "total", devices.size()
        ));
    }

    /**
     * Connect to a device via TCP/IP
     */
    @PostMapping("/connect")
    public ResponseEntity<?> connectDevice(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        if (ipAddress == null || ipAddress.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "IP address is required"));
        }
        
        boolean success = adbDeviceManager.connectToDevice(ipAddress);
        return ResponseEntity.ok(Map.of(
            "success", success,
            "ipAddress", ipAddress,
            "message", success ? "Connected successfully" : "Failed to connect"
        ));
    }

    /**
     * Disconnect from a device
     */
    @DeleteMapping("/disconnect/{deviceId}")
    public ResponseEntity<?> disconnectDevice(@PathVariable String deviceId) {
        boolean success = adbDeviceManager.disconnectFromDevice(deviceId);
        return ResponseEntity.ok(Map.of(
            "success", success,
            "deviceId", deviceId,
            "message", success ? "Disconnected successfully" : "Failed to disconnect"
        ));
    }

    /**
     * Get application status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Test ADB connection
     */
    @GetMapping("/test-adb")
    public ResponseEntity<?> testAdbConnection() {
        try {
            var devices = adbDeviceManager.getConnectedDevices();
            return ResponseEntity.ok(Map.of(
                "connected", true,
                "message", "ADB connection successful",
                "deviceCount", devices.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "connected", false,
                "message", "ADB connection failed: " + e.getMessage()
            ));
        }
    }
}