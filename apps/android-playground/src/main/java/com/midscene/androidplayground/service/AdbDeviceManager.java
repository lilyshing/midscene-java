package com.midscene.androidplayground.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AdbDeviceManager {

    private static final Logger logger = Logger.getLogger(AdbDeviceManager.class.getName());

    @Value("${android.playground.adb.path}")
    private String adbPath;

    /**
     * Get list of connected ADB devices
     * @return List of Device objects
     */
    public List<Device> getConnectedDevices() {
        List<Device> devices = new ArrayList<>();
        
        // Check if ADB executable exists
        if (!isAdbAvailable()) {
            return devices; // Return empty list instead of failing
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "devices", "-l");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        String deviceId = parts[0];
                        String status = parts[1];
                        String name = "Unknown Device";
                        
                        // Extract device name if available
                        for (int i = 2; i < parts.length; i++) {
                            if (parts[i].startsWith("model:")) {
                                name = parts[i].substring(6);
                                break;
                            }
                        }
                        
                        devices.add(new Device(deviceId, name, status));
                        logger.info("Found device: " + deviceId + " (" + name + ") - Status: " + status);
                    }
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.warning("ADB command exited with code: " + exitCode);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting connected devices: " + e.getMessage(), e);
        }
        
        return devices;
    }

    /**
     * Connect to a specific device via TCP/IP
     * @param ipAddress IP address of the device
     * @return true if connection successful
     */
    public boolean connectToDevice(String ipAddress) {
        // Check if ADB executable exists
        if (!isAdbAvailable()) {
            return false; // Return false instead of failing
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "connect", ipAddress);
            Process process = pb.start();
            int exitCode = process.waitFor();
            logger.info("Connecting to device at " + ipAddress + ", exit code: " + exitCode);
            return exitCode == 0;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error connecting to device: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Disconnect from a specific device
     * @param deviceId ID of the device to disconnect
     * @return true if disconnection successful
     */
    public boolean disconnectFromDevice(String deviceId) {
        // Check if ADB executable exists
        if (!isAdbAvailable()) {
            return false; // Return false instead of failing
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "disconnect", deviceId);
            Process process = pb.start();
            int exitCode = process.waitFor();
            logger.info("Disconnecting from device " + deviceId + ", exit code: " + exitCode);
            return exitCode == 0;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error disconnecting from device: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if ADB executable is available
     * @return true if ADB executable exists and is accessible
     */
    private boolean isAdbAvailable() {
        File adbFile = new File(adbPath);
        boolean exists = adbFile.exists();
        
        if (!exists) {
            logger.warning("ADB executable not found at: " + adbPath);
        }
        
        return exists;
    }

    /**
     * Device class to represent connected Android devices
     */
    public static class Device {
        private String id;
        private String name;
        private String status;

        public Device(String id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }

        public boolean isOnline() {
            return "device".equals(status);
        }
    }
}