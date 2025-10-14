package com.midscene.playground;

import com.midscene.playground.code.ExecutionResult;
import com.midscene.playground.code.Executor;
import com.midscene.playground.code.JavaExecutor;
import com.midscene.playground.config.PlaygroundConfig;
import com.midscene.playground.config.PlaygroundConfigLoader;
import com.midscene.playground.PlaygroundException;
import com.midscene.playground.security.SandboxManager;
import com.midscene.playground.security.SecureEnvironment;
import com.midscene.mcp.McpService;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Playground主入口类
 * 提供代码执行环境，支持安全执行用户代码，集成MCP服务
 */
public class Playground {
    private static final Logger logger = Logger.getLogger(Playground.class.getName());
    
    private PlaygroundConfig config;
    private Executor defaultExecutor;
    private SandboxManager sandboxManager;
    private Map<String, PlaygroundSession> sessions;
    private McpService mcpService;
    private boolean initialized;
    
    /**
     * 私有构造函数，通过Builder创建实例
     */
    private Playground() {
        this.sessions = new ConcurrentHashMap<>();
        this.sandboxManager = new SandboxManager();
        this.initialized = false;
    }
    
    /**
     * 获取新的Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder模式实现
     */
    public static class Builder {
        private String configPath;
        private AIModelService aiModelService;
        private InsightEngine insightEngine;
        private TaskExecutor taskExecutor;
        private boolean enableMcp;
        
        public Builder() {
            this.enableMcp = false;
        }
        
        /**
         * 设置配置文件路径
         */
        public Builder withConfigPath(String configPath) {
            this.configPath = configPath;
            return this;
        }
        
        /**
         * 设置AI模型服务
         */
        public Builder withAIModelService(AIModelService aiModelService) {
            this.aiModelService = aiModelService;
            return this;
        }
        
        /**
         * 设置洞察引擎
         */
        public Builder withInsightEngine(InsightEngine insightEngine) {
            this.insightEngine = insightEngine;
            return this;
        }
        
        /**
         * 设置任务执行器
         */
        public Builder withTaskExecutor(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }
        
        /**
         * 启用MCP服务
         */
        public Builder enableMcp() {
            this.enableMcp = true;
            return this;
        }
        
        /**
         * 构建Playground实例
         */
        public Playground build() {
            Playground playground = new Playground();
            
            // 初始化配置
            if (configPath != null) {
                playground.config = PlaygroundConfigLoader.loadFromFile(configPath);
            } else {
                playground.config = PlaygroundConfigLoader.loadDefault();
            }
            
            // 初始化沙箱环境
            playground.sandboxManager.initialize(playground.config);
            
            // 创建默认执行器
            SecureEnvironment secureEnv = playground.sandboxManager.createSecureEnvironment();
            playground.defaultExecutor = new JavaExecutor(playground.config, secureEnv);
            
            // 初始化MCP服务
            if (enableMcp && aiModelService != null && insightEngine != null && taskExecutor != null) {
                playground.initializeMcpService(aiModelService, insightEngine, taskExecutor);
            }
            
            playground.initialized = true;
            logger.info("Playground initialized successfully with" + 
                        (enableMcp ? " MCP support enabled" : "out MCP support"));
            
            return playground;
        }
    }
    
    /**
     * 初始化MCP服务
     */
    private void initializeMcpService(AIModelService aiModelService, InsightEngine insightEngine, TaskExecutor taskExecutor) {
        try {
            this.mcpService = McpService.initialize(aiModelService, insightEngine, taskExecutor);
            // 可以根据需要配置MCP服务
            logger.info("MCP service initialized in Playground");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize MCP service", e);
            throw new PlaygroundException("Failed to initialize MCP service", e);
        }
    }
    
    /**
     * 创建新的会话
     */
    public PlaygroundSession createSession() {
        checkInitialized();
        
        PlaygroundSession session = new PlaygroundSession(config, sandboxManager);
        sessions.put(session.getId(), session);
        return session;
    }
    
    /**
     * 执行代码
     * @param code 要执行的代码
     */
    public ExecutionResult executeCode(String code) {
        return executeCode(code, null);
    }
    
    /**
     * 执行代码
     * @param code 要执行的代码
     * @param executionContext 执行上下文
     */
    public ExecutionResult executeCode(String code, Map<String, Object> executionContext) {
        checkInitialized();
        
        return defaultExecutor.execute(code, executionContext);
    }
    
    /**
     * 获取会话
     * @param sessionId 会话ID
     */
    public PlaygroundSession getSession(String sessionId) {
        checkInitialized();
        
        if (sessionId == null) {
            return null;
        }
        return sessions.get(sessionId);
    }
    
    /**
     * 关闭会话
     * @param sessionId 会话ID
     */
    public void closeSession(String sessionId) {
        checkInitialized();
        
        if (sessionId != null) {
            PlaygroundSession session = sessions.remove(sessionId);
            if (session != null) {
                session.close();
                logger.fine("Session closed: " + sessionId);
            }
        }
    }
    
    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        checkInitialized();
        
        long currentTime = System.currentTimeMillis();
        sessions.forEach((id, session) -> {
            if (session.isExpired(currentTime)) {
                closeSession(id);
                logger.fine("Expired session cleaned up: " + id);
            }
        });
    }
    
    /**
     * 获取配置
     */
    public PlaygroundConfig getConfig() {
        checkInitialized();
        return config;
    }
    
    /**
     * 设置配置
     * @param config 新的配置
     */
    public void setConfig(PlaygroundConfig config) {
        this.config = config;
        // 重新初始化沙箱和执行器
        sandboxManager.initialize(config);
        SecureEnvironment secureEnv = sandboxManager.createSecureEnvironment();
        this.defaultExecutor = new JavaExecutor(config, secureEnv);
        logger.info("Playground configuration updated");
    }
    
    /**
     * 获取沙箱管理器
     */
    public SandboxManager getSandboxManager() {
        checkInitialized();
        return sandboxManager;
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        checkInitialized();
        return sessions.size();
    }
    
    /**
     * 获取MCP服务实例
     */
    public McpService getMcpService() {
        checkInitialized();
        return mcpService;
    }
    
    /**
     * 检查是否启用了MCP服务
     */
    public boolean isMcpEnabled() {
        return mcpService != null;
    }
    
    /**
     * 通过MCP发送提示词到指定服务器
     * @param host 服务器主机名
     * @param port 服务器端口
     * @param prompt 提示词内容
     * @return 服务器响应
     */
    public String sendPromptViaMcp(String host, int port, String prompt) {
        checkInitialized();
        
        if (!isMcpEnabled()) {
            throw new PlaygroundException("MCP service is not enabled");
        }
        
        return mcpService.sendPromptToServer(host, port, prompt);
    }
    
    /**
     * 通过MCP发送提示词到指定服务器（使用默认端口）
     * @param host 服务器主机名
     * @param prompt 提示词内容
     * @return 服务器响应
     */
    public String sendPromptViaMcp(String host, String prompt) {
        return sendPromptViaMcp(host, 8080, prompt); // 使用默认端口
    }
    
    /**
     * 关闭Playground
     */
    public void shutdown() {
        // 关闭所有会话
        sessions.forEach((id, session) -> {
            try {
                session.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error closing session: " + id, e);
            }
        });
        sessions.clear();
        
        // 关闭MCP服务
        if (mcpService != null) {
            try {
                mcpService.shutdown();
                logger.info("MCP service shutdown completed");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error shutting down MCP service", e);
            }
        }
        
        // 关闭沙箱管理器
        sandboxManager.shutdown();
        
        initialized = false;
        logger.info("Playground shutdown completed");
    }
    
    /**
     * 检查是否已初始化
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new PlaygroundException("Playground not initialized. Use Builder to create and initialize Playground.");
        }
    }
}