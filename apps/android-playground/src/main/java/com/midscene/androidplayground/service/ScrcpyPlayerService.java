package com.midscene.androidplayground.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class ScrcpyPlayerService {

    private static final Logger logger = Logger.getLogger(ScrcpyPlayerService.class.getName());

    @Value("${android.playground.scrcpy.path}")
    private String scrcpyPath;

    @Value("${android.playground.max.frame.size}")
    private int maxFrameSize;

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Process> activeSessions = new HashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ScrcpyPlayerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Connect to device for screen mirroring
     * @param deviceId ID of the device to connect to
     */
    public void connectDevice(String deviceId) {
        String sessionId = deviceId; // Use deviceId as sessionId
        
        if (activeSessions.containsKey(sessionId)) {
            logger.warning("Session already active: " + sessionId);
            return;
        }

        try {
            // Start scrcpy process for the device
            ProcessBuilder pb = new ProcessBuilder(
                    scrcpyPath,
                    "--serial", deviceId,
                    "--no-control",  // Only mirror, no control
                    "--max-fps", "15",  // Limit FPS for better performance
                    "--bit-rate", "2M", // Limit bitrate
                    "--crop", "-1:-1:-1:-1" // Default crop
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            activeSessions.put(sessionId, process);

            // Notify client that connection is established
            sendVideoMetadata(sessionId, 1080, 1920, "H264");

            // Process video stream in a separate thread
            executorService.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Here we would process the video stream data
                        // In a real implementation, we would extract video frames and send them
                        logger.fine("SCRCPY output: " + line);
                    }
                } catch (Exception e) {
                    logger.severe("Error processing video stream: " + e.getMessage());
                    disconnectDevice(sessionId);
                }
            });

            logger.info("Started scrcpy session for device: " + deviceId + " (session: " + sessionId + ")");
        } catch (Exception e) {
            logger.severe("Failed to start scrcpy: " + e.getMessage());
            // Send error message to client
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error", "Failed to connect to device: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/error", errorMessage);
        }
    }

    /**
     * Disconnect from device and clean up resources
     * @param sessionId Session ID to disconnect
     */
    public void disconnectDevice(String sessionId) {
        Process process = activeSessions.remove(sessionId);
        if (process != null) {
            try {
                process.destroy();
                process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                logger.info("Disconnected scrcpy session: " + sessionId);
            } catch (Exception e) {
                logger.warning("Error disconnecting scrcpy session: " + e.getMessage());
            }
        }
    }

    /**
     * Send video metadata to client
     * @param sessionId Session ID
     * @param width Video width
     * @param height Video height
     * @param codec Video codec
     */
    private void sendVideoMetadata(String sessionId, int width, int height, String codec) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("width", width);
        metadata.put("height", height);
        metadata.put("codec", codec);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/video-metadata", metadata);
    }

    /**
     * Send video frame to client
     * @param sessionId Session ID
     * @param frameData Frame data
     */
    private void sendVideoFrame(String sessionId, byte[] frameData) {
        // Check if frame size exceeds maximum allowed
        if (frameData.length > maxFrameSize * 1024) {
            logger.warning("Frame size exceeds maximum allowed: " + frameData.length + " bytes");
            return;
        }

        // Encode frame data to Base64 for transmission
        String encodedData = Base64.getEncoder().encodeToString(frameData);
        Map<String, Object> frameMessage = new HashMap<>();
        frameMessage.put("data", encodedData);
        frameMessage.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/video-frame", frameMessage);
    }

    /**
     * Take screenshot of the connected device
     * @param sessionId Session ID
     */
    public void takeScreenshot(String sessionId) {
        // In a real implementation, this would capture the current frame
        // and send it to the client
        logger.info("Screenshot requested for session: " + sessionId);
        
        // For demo purposes, send a placeholder message
        Map<String, String> screenshotMessage = new HashMap<>();
        screenshotMessage.put("status", "success");
        screenshotMessage.put("message", "Screenshot functionality would be implemented here");
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/screenshot", screenshotMessage);
    }

    /**
     * Clean up resources when shutting down
     */
    public void shutdown() {
        // Disconnect all active sessions
        for (String sessionId : new ArrayList<>(activeSessions.keySet())) {
            disconnectDevice(sessionId);
        }
        executorService.shutdown();
    }
}