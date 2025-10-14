package com.midscene.playground.service;

import com.midscene.playground.model.PlaygroundSession;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform Manager
 * Responsible for managing platform instances and executing commands
 */
@Component
public class PlatformManager {
    private static final Logger logger = LoggerFactory.getLogger(PlatformManager.class);
    private final ConcurrentHashMap<String, Map<String, Object>> sessionStates = new ConcurrentHashMap<>();

    /**
     * Initialize platform connection
     */
    public void initializePlatform(PlaygroundSession session) throws Exception {
        logger.info("Initializing platform for session: {}", session.getSessionId());
        String platformType = session.getPlatformType();
        String serverUrl = session.getServerUrl();
        
        Map<String, Object> state = new HashMap<>();
        state.put("platformType", platformType);
        state.put("serverUrl", serverUrl);
        state.put("initializedAt", System.currentTimeMillis());
        
        sessionStates.put(session.getSessionId(), state);
    }

    /**
     * Execute command - returns Map response that can be converted to JsonNode
     */
    public Map<String, Object> executeCommand(PlaygroundSession session, String commandType, JsonNode parameters) throws Exception {
        logger.info("Executing command: {} for session: {}", commandType, session.getSessionId());
        
        // Convert JsonNode to Map for processing
        Map<String, Object> paramsMap = new HashMap<>();
        if (parameters != null && parameters.isObject()) {
            parameters.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                // Simple conversion logic - can be enhanced for complex types
                if (value.isTextual()) {
                    paramsMap.put(key, value.asText());
                } else if (value.isInt()) {
                    paramsMap.put(key, value.asInt());
                } else if (value.isLong()) {
                    paramsMap.put(key, value.asLong());
                } else if (value.isDouble()) {
                    paramsMap.put(key, value.asDouble());
                } else if (value.isBoolean()) {
                    paramsMap.put(key, value.asBoolean());
                } else if (value.isNull()) {
                    paramsMap.put(key, null);
                } else {
                    // For complex objects and arrays, store as string representation
                    paramsMap.put(key, value.toString());
                }
            });
        }
        
        // Create response map with command execution details
        Map<String, Object> response = new HashMap<>();
        response.put("command", commandType);
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        
        // Add parameters to response for debugging
        response.put("parameters", paramsMap);
        
        return response;
    }

    /**
     * Close platform connection
     */
    public void closePlatform(PlaygroundSession session) throws Exception {
        logger.info("Closing platform for session: {}", session.getSessionId());
        sessionStates.remove(session.getSessionId());
    }
}