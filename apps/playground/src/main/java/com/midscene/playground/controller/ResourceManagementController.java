package com.midscene.playground.controller;

import com.midscene.playground.resource.HttpConnectionPoolManager;
import com.midscene.playground.resource.MemoryMonitorService;
import com.midscene.playground.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源管理控制器 - 提供资源使用监控和管理的REST API接口
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagementController.class);

    private final HttpConnectionPoolManager httpConnectionPoolManager;
    private final MemoryMonitorService memoryMonitorService;
    private final ResourceManager resourceManager;

    @Autowired
    public ResourceManagementController(
            HttpConnectionPoolManager httpConnectionPoolManager,
            MemoryMonitorService memoryMonitorService,
            ResourceManager resourceManager) {
        this.httpConnectionPoolManager = httpConnectionPoolManager;
        this.memoryMonitorService = memoryMonitorService;
        this.resourceManager = resourceManager;
    }

    /**
     * 获取所有资源使用统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllResourceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取HTTP连接池统计
        stats.put("httpPool", httpConnectionPoolManager.getPoolStats());
        
        // 获取内存统计
        stats.put("memory", memoryMonitorService.getMemoryStats());
        
        // 获取资源池统计
        stats.put("resourcePools", resourceManager.getPoolStats());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取HTTP连接池统计信息
     */
    @GetMapping("/http-pool")
    public ResponseEntity<Map<String, Object>> getHttpPoolStats() {
        return ResponseEntity.ok(httpConnectionPoolManager.getPoolStats());
    }

    /**
     * 获取内存使用统计信息
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryStats() {
        return ResponseEntity.ok(memoryMonitorService.getMemoryStats());
    }

    /**
     * 获取所有资源池统计信息
     */
    @GetMapping("/resource-pools")
    public ResponseEntity<Map<String, Object>> getResourcePoolStats() {
        return ResponseEntity.ok(resourceManager.getPoolStats());
    }

    /**
     * 获取指定资源池统计信息
     */
    @GetMapping("/pool-stats/{poolName}")
    public ResponseEntity<Map<String, Object>> getResourcePoolStats(@PathVariable String poolName) {
        try {
            Map<String, Object> stats = resourceManager.getPoolStats(poolName);
            if (stats == null || stats.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting resource pool stats for {}", poolName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 触发内存清理
     */
    @PostMapping("/memory/cleanup")
    public ResponseEntity<Map<String, String>> triggerMemoryCleanup() {
        memoryMonitorService.triggerMemoryCleanup();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Memory cleanup triggered");
        return ResponseEntity.ok(response);
    }

    /**
     * 清理空闲资源
     */
    @PostMapping("/cleanup-idle")
    public ResponseEntity<Map<String, String>> cleanupIdleResources() {
        resourceManager.cleanupIdleResources();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Idle resources cleaned up");
        return ResponseEntity.ok(response);
    }

    /**
     * 清理指定资源池
     */
    @PostMapping("/pool/cleanup")
    public ResponseEntity<Map<String, String>> cleanupPool(String poolName) {
        boolean success = resourceManager.cleanupPool(poolName);
        Map<String, String> response = new HashMap<>();
        
        if (success) {
            response.put("status", "success");
            response.put("message", "Pool cleaned up successfully: " + poolName);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Pool not found: " + poolName);
            return ResponseEntity.badRequest().body(response);
        }
    }
}