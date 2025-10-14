package com.midscene.playground.controller;

import com.midscene.playground.model.CommandRequest;
import com.midscene.playground.model.ExecutionResult;
import com.midscene.playground.model.PlaygroundSession;
import com.midscene.playground.service.PlaygroundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Playground REST控制器
 * 提供会话管理和命令执行的HTTP API
 */
@RestController
@RequestMapping("/playground/api")
public class PlaygroundController {
    private static final Logger logger = LoggerFactory.getLogger(PlaygroundController.class);
    private final PlaygroundService playgroundService;

    @Autowired
    public PlaygroundController(PlaygroundService playgroundService) {
        this.playgroundService = playgroundService;
    }

    /**
     * 创建新的Playground会话
     */
    @PostMapping("/sessions")
    public ResponseEntity<PlaygroundSession> createSession(@RequestBody Map<String, String> request) {
        try {
            String platformType = request.get("platformType");
            String serverUrl = request.get("serverUrl");
            
            if (platformType == null || platformType.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            PlaygroundSession session = playgroundService.createSession(platformType, serverUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (Exception e) {
            logger.error("Failed to create session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取会话信息
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<PlaygroundSession> getSession(@PathVariable String sessionId) {
        PlaygroundSession session = playgroundService.getSession(sessionId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 执行命令
     */
    @PostMapping("/commands")
    public ResponseEntity<ExecutionResult> executeCommand(@RequestBody CommandRequest request) {
        try {
            logger.info("Received command request: {}", request.getCommandType());
            ExecutionResult result = playgroundService.executeCommand(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error executing command: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ExecutionResult.failure(request.getCommandType(), "Internal server error", e.getMessage())
            );
        }
    }

    /**
     * 关闭会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        boolean closed = playgroundService.closeSession(sessionId);
        if (closed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = Map.of(
            "status", "running",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(status);
    }
}