package com.midscene.playground.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midscene.playground.model.CommandRequest;
import com.midscene.playground.model.ExecutionResult;
import com.midscene.playground.model.PlaygroundSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Playground service class
 * Provides core business logic for session management and command execution
 */
@Service
public class PlaygroundService {
    private static final Logger logger = LoggerFactory.getLogger(PlaygroundService.class);
    private final ConcurrentHashMap<String, PlaygroundSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final PlatformManager platformManager;

    @Autowired
    public PlaygroundService(ObjectMapper objectMapper, PlatformManager platformManager) {
        this.objectMapper = objectMapper;
        this.platformManager = platformManager;
    }

    /**
     * Create new Playground session
     */
    public PlaygroundSession createSession(String platformType, String serverUrl) {
        PlaygroundSession session = new PlaygroundSession();
        session.setPlatformType(platformType);
        session.setServerUrl(serverUrl);
        
        try {
            // Initialize platform connection
            platformManager.initializePlatform(session);
            session.setStatus(PlaygroundSession.SessionStatus.IDLE);
            sessions.put(session.getSessionId(), session);
            logger.info("Created new session: {}", session.getSessionId());
        } catch (Exception e) {
            logger.error("Failed to create session: {}", e.getMessage());
            session.setStatus(PlaygroundSession.SessionStatus.ERROR);
        }
        
        return session;
    }

    /**
     * Get session information
     */
    public PlaygroundSession getSession(String sessionId) {
        PlaygroundSession session = sessions.get(sessionId);
        if (session != null) {
            session.setLastActivityTime(java.time.LocalDateTime.now());
        }
        return session;
    }

    /**
     * Execute command
     */
    public ExecutionResult executeCommand(CommandRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            PlaygroundSession session = getSession(request.getSessionId());
            if (session == null) {
                return ExecutionResult.failure(request.getCommandType(), "Session not found", "Session ID: " + request.getSessionId());
            }

            session.setCurrentCommand(request.getCommandType());
            session.setStatus(PlaygroundSession.SessionStatus.RUNNING);
            
            // Execute command through platform manager
            Map<String, Object> resultMap = platformManager.executeCommand(session, request.getCommandType(), request.getParameters());
            JsonNode resultData = objectMapper.valueToTree(resultMap);
            long executionTime = System.currentTimeMillis() - startTime;
            
            session.setStatus(PlaygroundSession.SessionStatus.IDLE);
            ExecutionResult result = ExecutionResult.success(
                    request.getCommandType(),
                    "Command executed successfully",
                    resultData,
                    executionTime
            );
            session.setLastResult(result);
            
            return result;
        } catch (Exception e) {
            logger.error("Error executing command: {}", e.getMessage(), e);
            PlaygroundSession session = sessions.get(request.getSessionId());
            if (session != null) {
                session.setStatus(PlaygroundSession.SessionStatus.ERROR);
            }
            
            return ExecutionResult.failure(
                    request.getCommandType(),
                    "Command execution failed",
                    e.getMessage()
            );
        }
    }

    /**
     * Close session
     */
    public boolean closeSession(String sessionId) {
        PlaygroundSession session = sessions.remove(sessionId);
        if (session != null) {
            try {
                platformManager.closePlatform(session);
                logger.info("Closed session: {}", sessionId);
                return true;
            } catch (Exception e) {
                logger.error("Error closing session: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Cleanup expired sessions
     */
    public void cleanupExpiredSessions() {
        long expiryDuration = TimeUnit.MINUTES.toMillis(30); // 30 minutes expiry
        long currentTime = System.currentTimeMillis();
        
        sessions.forEach((id, session) -> {
            long lastActivity = session.getLastActivityTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (currentTime - lastActivity > expiryDuration) {
                logger.info("Cleaning up expired session: {}", id);
                closeSession(id);
            }
        });
    }
}