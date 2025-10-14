package com.midscene.mcp;

import com.midscene.core.exception.PlatformException;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MCP服务类
 * 作为MCP协议模块的服务入口，负责协调服务器和客户端的工作
 */
public class McpService {
    private static final Logger logger = Logger.getLogger(McpService.class.getName());
    
    // 服务实例
    private static volatile McpService instance;
    
    // 服务器实例
    private McpServer server;
    
    // 客户端实例映射（host -> client）
    private final Map<String, McpClient> clients = new ConcurrentHashMap<>();
    
    // 配置信息
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    
    // 依赖服务
    private AIModelService aiModelService;
    private InsightEngine insightEngine;
    private TaskExecutor taskExecutor;
    
    /**
     * 私有构造函数（单例模式）
     */
    private McpService() {
        // 设置默认配置
        setDefaultConfiguration();
    }
    
    /**
     * 获取单例实例
     * @return McpService实例
     */
    public static McpService getInstance() {
        if (instance == null) {
            synchronized (McpService.class) {
                if (instance == null) {
                    instance = new McpService();
                }
            }
        }
        return instance;
    }
    
    /**
     * 设置依赖服务
     * @param aiModelService AI模型服务
     * @param insightEngine 洞察引擎
     * @param taskExecutor 任务执行器
     */
    public void setServices(AIModelService aiModelService, InsightEngine insightEngine, TaskExecutor taskExecutor) {
        this.aiModelService = aiModelService;
        this.insightEngine = insightEngine;
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * 设置默认配置
     */
    private void setDefaultConfiguration() {
        configuration.put(McpProtocol.ConfigKeys.SERVER_PORT, McpProtocol.DEFAULT_PORT);
        configuration.put(McpProtocol.ConfigKeys.CONNECTION_TIMEOUT, 10000);
        configuration.put(McpProtocol.ConfigKeys.READ_TIMEOUT, 30000);
        configuration.put(McpProtocol.ConfigKeys.WRITE_TIMEOUT, 10000);
        configuration.put(McpProtocol.ConfigKeys.MAX_SESSIONS, 100);
        configuration.put(McpProtocol.ConfigKeys.SESSION_TIMEOUT, 3600000); // 1小时
        configuration.put(McpProtocol.ConfigKeys.ENABLE_COMPRESSION, false);
    }
    
    /**
     * 设置配置
     * @param key 配置键
     * @param value 配置值
     */
    public void setConfiguration(String key, Object value) {
        configuration.put(key, value);
        logger.info("MCP configuration updated: " + key + " = " + value);
    }
    
    /**
     * 获取配置
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfiguration(String key, T defaultValue) {
        Object value = configuration.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 启动MCP服务器
     * @throws PlatformException 如果启动失败
     */
    public void startServer() throws PlatformException {
        synchronized (this) {
            if (server != null && server.isRunning()) {
                logger.warning("MCP server is already running");
                return;
            }
            
            try {
                int port = getConfiguration(McpProtocol.ConfigKeys.SERVER_PORT, McpProtocol.DEFAULT_PORT);
                server = new McpServer(port);
                
                // 设置依赖服务
                server.setServices(aiModelService, insightEngine, taskExecutor);
                
                // 启动服务器
                server.start();
                logger.info("MCP server started successfully on port " + port);
                
            } catch (Exception e) {
                server = null;
                throw new PlatformException("Failed to start MCP server", e);
            }
        }
    }
    
    /**
     * 停止MCP服务器
     * @throws PlatformException 如果停止失败
     */
    public void stopServer() throws PlatformException {
        synchronized (this) {
            if (server == null || !server.isRunning()) {
                logger.warning("MCP server is not running");
                return;
            }
            
            try {
                server.stop();
                logger.info("MCP server stopped successfully");
                server = null;
            } catch (Exception e) {
                throw new PlatformException("Failed to stop MCP server", e);
            }
        }
    }
    
    /**
     * 检查服务器是否正在运行
     * @return 是否运行
     */
    public boolean isServerRunning() {
        return server != null && server.isRunning();
    }
    
    /**
     * 获取服务器活动会话数
     * @return 会话数
     */
    public int getActiveSessionsCount() {
        if (server == null) {
            return 0;
        }
        return server.getActiveSessionsCount();
    }
    
    /**
     * 创建并连接到MCP服务器
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 客户端实例
     * @throws PlatformException 如果连接失败
     */
    public McpClient connectClient(String host, int port) throws PlatformException {
        String clientKey = host + ":" + port;
        
        // 检查是否已经存在连接
        McpClient existingClient = clients.get(clientKey);
        if (existingClient != null && existingClient.isConnected()) {
            logger.info("Reusing existing MCP client connection to " + clientKey);
            return existingClient;
        }
        
        try {
            // 创建新客户端
            McpClient client = new McpClient(host, port);
            
            // 设置超时配置
            int connectionTimeout = getConfiguration(McpProtocol.ConfigKeys.CONNECTION_TIMEOUT, 10000);
            int readTimeout = getConfiguration(McpProtocol.ConfigKeys.READ_TIMEOUT, 30000);
            int writeTimeout = getConfiguration(McpProtocol.ConfigKeys.WRITE_TIMEOUT, 10000);
            
            client.setConnectionTimeout(connectionTimeout);
            client.setReadTimeout(readTimeout);
            client.setWriteTimeout(writeTimeout);
            
            // 连接到服务器
            client.connect();
            
            // 存储客户端实例
            clients.put(clientKey, client);
            logger.info("Connected to MCP server at " + clientKey);
            
            return client;
            
        } catch (Exception e) {
            throw new PlatformException("Failed to connect to MCP server at " + clientKey, e);
        }
    }
    
    /**
     * 创建并连接到MCP服务器（使用默认端口）
     * @param host 服务器主机名
     * @return 客户端实例
     * @throws PlatformException 如果连接失败
     */
    public McpClient connectClient(String host) throws PlatformException {
        return connectClient(host, McpProtocol.DEFAULT_PORT);
    }
    
    /**
     * 获取已有客户端连接
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 客户端实例，如果不存在则返回null
     */
    public McpClient getClient(String host, int port) {
        String clientKey = host + ":" + port;
        return clients.get(clientKey);
    }
    
    /**
     * 断开客户端连接
     * @param host 服务器主机名
     * @param port 服务器端口
     */
    public void disconnectClient(String host, int port) {
        String clientKey = host + ":" + port;
        McpClient client = clients.remove(clientKey);
        
        if (client != null) {
            try {
                client.close();
                logger.info("Disconnected from MCP server at " + clientKey);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error disconnecting client", e);
            }
        }
    }
    
    /**
     * 断开所有客户端连接
     */
    public void disconnectAllClients() {
        for (Map.Entry<String, McpClient> entry : clients.entrySet()) {
            try {
                entry.getValue().close();
                logger.info("Disconnected from MCP server at " + entry.getKey());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error disconnecting client " + entry.getKey(), e);
            }
        }
        clients.clear();
    }
    
    /**
     * 获取客户端连接数
     * @return 客户端数
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * 测试服务器连接
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 是否连接成功
     */
    public boolean testServerConnection(String host, int port) {
        McpClient client = null;
        try {
            // 创建临时客户端进行测试
            client = new McpClient(host, port);
            client.setConnectionTimeout(3000); // 较短的超时时间用于测试
            client.connect();
            boolean result = client.testConnection();
            logger.info("Server connection test to " + host + ":" + port + " - " + (result ? "SUCCESS" : "FAILED"));
            return result;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Server connection test failed for " + host + ":" + port, e);
            return false;
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
    
    /**
     * 发送全局配置更新到所有连接的客户端
     */
    public void broadcastConfiguration() {
        for (McpClient client : clients.values()) {
            try {
                if (client.isConnected()) {
                    client.setConfiguration(configuration);
                    logger.fine("Broadcasted configuration to client: " + client.getHost() + ":" + client.getPort());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to broadcast configuration to client", e);
            }
        }
    }
    
    /**
     * 发送提示词到指定服务器
     * @param host 服务器主机名
     * @param port 服务器端口
     * @param prompt 提示词内容
     * @return 服务器响应
     * @throws PlatformException 如果操作失败
     */
    public String sendPromptToServer(String host, int port, String prompt) throws PlatformException {
        McpClient client = connectClient(host, port);
        try {
            return client.sendPrompt(prompt);
        } catch (PlatformException e) {
            // 如果发送失败，尝试重连
            try {
                client.reconnect();
                return client.sendPrompt(prompt);
            } catch (Exception re) {
                // 重连也失败，重新抛出原始异常
                throw e;
            }
        }
    }
    
    /**
     * 发送提示词到指定服务器（使用默认端口）
     * @param host 服务器主机名
     * @param prompt 提示词内容
     * @return 服务器响应
     * @throws PlatformException 如果操作失败
     */
    public String sendPromptToServer(String host, String prompt) throws PlatformException {
        return sendPromptToServer(host, McpProtocol.DEFAULT_PORT, prompt);
    }
    
    /**
     * 获取服务状态
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 服务器状态信息
     * @throws PlatformException 如果操作失败
     */
    public Map<String, Object> getServerStatus(String host, int port) throws PlatformException {
        McpClient client = connectClient(host, port);
        return client.getStatus();
    }
    
    /**
     * 关闭服务（停止服务器并断开所有客户端连接）
     * @throws PlatformException 如果关闭失败
     */
    public void shutdown() throws PlatformException {
        logger.info("Shutting down MCP service...");
        
        // 断开所有客户端连接
        disconnectAllClients();
        
        // 停止服务器
        if (isServerRunning()) {
            stopServer();
        }
        
        logger.info("MCP service shutdown complete");
    }
    
    /**
     * 初始化MCP服务
     * @param aiModelService AI模型服务
     * @param insightEngine 洞察引擎
     * @param taskExecutor 任务执行器
     * @return 初始化后的服务实例
     */
    public static McpService initialize(AIModelService aiModelService, InsightEngine insightEngine, TaskExecutor taskExecutor) {
        McpService service = getInstance();
        service.setServices(aiModelService, insightEngine, taskExecutor);
        logger.info("MCP service initialized");
        return service;
    }
    
    /**
     * 重置服务（仅用于测试）
     */
    public static void reset() {
        if (instance != null) {
            try {
                instance.shutdown();
            } catch (Exception ignored) {
            }
            instance = null;
        }
    }
    
    /**
     * 获取服务器版本信息
     * @return 版本信息
     */
    public Map<String, String> getVersionInfo() {
        Map<String, String> versionInfo = new ConcurrentHashMap<>();
        versionInfo.put("protocolVersion", McpProtocol.PROTOCOL_VERSION);
        versionInfo.put("protocolId", McpProtocol.PROTOCOL_ID);
        versionInfo.put("serviceName", "MCP Service");
        return versionInfo;
    }
    
    /**
     * 获取性能统计信息
     * @return 性能统计
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // 添加服务级统计
        stats.put("clientCount", getClientCount());
        stats.put("activeSessions", getActiveSessionsCount());
        stats.put("serverRunning", isServerRunning());
        
        // 添加配置统计
        stats.put("configuration", configuration);
        
        return stats;
    }
}