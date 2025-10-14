package com.midscene.mcp;

import com.midscene.core.exception.PlatformException;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MCP服务器类
 * 实现MCP协议服务器，处理客户端连接和消息
 */
public class McpServer {
    private static final Logger logger = Logger.getLogger(McpServer.class.getName());
    
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning;
    
    // 会话管理
    private final Map<String, McpSession> sessions = new ConcurrentHashMap<>();
    
    // 依赖服务
    private AIModelService aiModelService;
    private InsightEngine insightEngine;
    private TaskExecutor taskExecutor;
    
    // 消息处理器
    private final Map<McpProtocol.CommandType, MessageHandler> commandHandlers = new HashMap<>();
    
    /**
     * 构造函数
     * @param port 监听端口
     */
    public McpServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
        initializeCommandHandlers();
    }
    
    /**
     * 构造函数（使用默认端口）
     */
    public McpServer() {
        this(McpProtocol.DEFAULT_PORT);
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
     * 启动服务器
     * @throws PlatformException 如果启动失败
     */
    public void start() throws PlatformException {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            logger.info("MCP Server started on port " + port);
            
            // 启动主线程接受连接
            threadPool.submit(this::acceptConnections);
        } catch (IOException e) {
            throw new PlatformException("Failed to start MCP server", e);
        }
    }
    
    /**
     * 停止服务器
     * @throws PlatformException 如果停止失败
     */
    public void stop() throws PlatformException {
        try {
            isRunning = false;
            
            // 关闭所有会话
            for (McpSession session : sessions.values()) {
                session.close();
            }
            sessions.clear();
            
            // 关闭服务器socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // 关闭线程池
            threadPool.shutdown();
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            
            logger.info("MCP Server stopped");
        } catch (Exception e) {
            throw new PlatformException("Failed to stop MCP server", e);
        }
    }
    
    /**
     * 接受客户端连接
     */
    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected from " + clientSocket.getInetAddress().getHostAddress());
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (isRunning) { // 只在服务器运行时记录错误
                    logger.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        }
    }
    
    /**
     * 处理客户端连接
     * @param clientSocket 客户端Socket
     */
    private void handleClient(Socket clientSocket) {
        String clientId = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        logger.info("Handling client: " + clientId);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)) {
            
            clientSocket.setSoTimeout(30000); // 设置超时时间
            
            while (isRunning && !clientSocket.isClosed()) {
                // 读取客户端消息
                String messageStr = readMessage(reader);
                if (messageStr == null) {
                    break; // 连接关闭
                }
                
                // 处理消息
                processClientMessage(messageStr, writer);
            }
        } catch (SocketTimeoutException e) {
            logger.warning("Client timeout: " + clientId);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error handling client " + clientId, e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
            logger.info("Client disconnected: " + clientId);
        }
    }
    
    /**
     * 读取客户端消息
     * @param reader BufferedReader
     * @return 消息字符串
     * @throws IOException 如果读取失败
     */
    private String readMessage(BufferedReader reader) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        String line;
        
        // 读取直到消息结束标记或连接关闭
        while ((line = reader.readLine()) != null) {
            messageBuilder.append(line).append(McpProtocol.MESSAGE_SEPARATOR);
            // 简单的消息边界检测，实际项目中应该使用更复杂的协议
            if (line.isEmpty()) {
                break;
            }
        }
        
        return messageBuilder.length() > 0 ? messageBuilder.toString() : null;
    }
    
    /**
     * 处理客户端消息
     * @param messageStr 消息字符串
     * @param writer PrintWriter
     */
    private void processClientMessage(String messageStr, PrintWriter writer) {
        try {
            // 解析消息
            McpMessage message = McpMessage.fromString(messageStr);
            
            if (!message.isValid()) {
                McpMessage errorResponse = McpMessage.createErrorResponse(
                    message, 
                    McpProtocol.ErrorCode.INVALID_PARAMETER, 
                    "Invalid message format"
                );
                writer.println(errorResponse.toString());
                writer.flush();
                return;
            }
            
            // 验证协议版本
            if (!McpProtocol.isCompatibleVersion(message.getProtocolVersion())) {
                McpMessage errorResponse = McpMessage.createErrorResponse(
                    message, 
                    McpProtocol.ErrorCode.INVALID_PARAMETER, 
                    "Incompatible protocol version"
                );
                writer.println(errorResponse.toString());
                writer.flush();
                return;
            }
            
            // 处理会话
            handleSession(message);
            
            // 执行命令处理
            McpMessage response = executeCommand(message);
            
            // 发送响应
            writer.println(response.toString());
            writer.flush();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing client message", e);
            // 发送错误响应
            McpMessage errorResponse = McpMessage.createRequest(McpProtocol.CommandType.SEND_PROMPT);
            errorResponse = McpMessage.createErrorResponse(
                errorResponse, 
                McpProtocol.ErrorCode.PROCESSING_ERROR, 
                "Internal server error: " + e.getMessage()
            );
            writer.println(errorResponse.toString());
            writer.flush();
        }
    }
    
    /**
     * 处理会话
     * @param message 消息对象
     */
    private void handleSession(McpMessage message) {
        if (message.getCommandType() == McpProtocol.CommandType.START_SESSION) {
            // 创建新会话
            String sessionId = generateSessionId();
            McpSession session = new McpSession(sessionId);
            sessions.put(sessionId, session);
            message.setSessionId(sessionId);
            logger.info("Created new session: " + sessionId);
        } else if (message.getCommandType() == McpProtocol.CommandType.END_SESSION) {
            // 结束会话
            if (message.getSessionId() != null) {
                McpSession session = sessions.remove(message.getSessionId());
                if (session != null) {
                    session.close();
                    logger.info("Closed session: " + message.getSessionId());
                }
            }
        } else if (message.getSessionId() != null) {
            // 验证会话存在
            McpSession session = sessions.get(message.getSessionId());
            if (session == null) {
                throw new IllegalStateException("Session not found: " + message.getSessionId());
            }
            // 更新会话最后活动时间
            session.updateLastActivity();
        }
    }
    
    /**
     * 执行命令
     * @param request 请求消息
     * @return 响应消息
     */
    private McpMessage executeCommand(McpMessage request) {
        MessageHandler handler = commandHandlers.get(request.getCommandType());
        
        if (handler == null) {
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.NOT_FOUND, 
                "Unsupported command: " + request.getCommandType()
            );
        }
        
        try {
            long startTime = System.currentTimeMillis();
            McpMessage response = handler.handle(request);
            
            // 添加性能指标
            long processingTime = System.currentTimeMillis() - startTime;
            response.addMetric(McpProtocol.PerformanceMetrics.PROCESSING_TIME, processingTime);
            
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing command: " + request.getCommandType(), e);
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.PROCESSING_ERROR, 
                e.getMessage()
            );
        }
    }
    
    /**
     * 初始化命令处理器
     */
    private void initializeCommandHandlers() {
        commandHandlers.put(McpProtocol.CommandType.START_SESSION, this::handleStartSession);
        commandHandlers.put(McpProtocol.CommandType.END_SESSION, this::handleEndSession);
        commandHandlers.put(McpProtocol.CommandType.SEND_PROMPT, this::handleSendPrompt);
        commandHandlers.put(McpProtocol.CommandType.PROCESS_IMAGE, this::handleProcessImage);
        commandHandlers.put(McpProtocol.CommandType.EXTRACT_ELEMENTS, this::handleExtractElements);
        commandHandlers.put(McpProtocol.CommandType.ANALYZE_SCREEN, this::handleAnalyzeScreen);
        commandHandlers.put(McpProtocol.CommandType.MAKE_DECISION, this::handleMakeDecision);
        commandHandlers.put(McpProtocol.CommandType.GET_STATUS, this::handleGetStatus);
        commandHandlers.put(McpProtocol.CommandType.SET_CONFIGURATION, this::handleSetConfiguration);
        commandHandlers.put(McpProtocol.CommandType.PING, this::handlePing);
    }
    
    // 命令处理器实现
    private McpMessage handleStartSession(McpMessage request) {
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("sessionId", request.getSessionId()));
        return response;
    }
    
    private McpMessage handleEndSession(McpMessage request) {
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Session closed successfully"));
        return response;
    }
    
    private McpMessage handleSendPrompt(McpMessage request) {
        if (aiModelService == null) {
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.SERVICE_UNAVAILABLE, 
                "AI Model Service not available"
            );
        }
        
        String prompt = request.getParameter("prompt", String.class);
        if (prompt == null) {
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.INVALID_PARAMETER, 
                "Prompt parameter is required"
            );
        }
        
        try {
            // 使用AI模型服务处理提示词
            String result = aiModelService.processPrompt(prompt);
            McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
            response.setResult(Collections.singletonMap("result", result));
            return response;
        } catch (Exception e) {
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.PROCESSING_ERROR, 
                "Failed to process prompt: " + e.getMessage()
            );
        }
    }
    
    private McpMessage handleProcessImage(McpMessage request) {
        // 实现图像处理逻辑
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Image processing not implemented yet"));
        return response;
    }
    
    private McpMessage handleExtractElements(McpMessage request) {
        // 实现元素提取逻辑
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Element extraction not implemented yet"));
        return response;
    }
    
    private McpMessage handleAnalyzeScreen(McpMessage request) {
        if (insightEngine == null) {
            return McpMessage.createErrorResponse(
                request, 
                McpProtocol.ErrorCode.SERVICE_UNAVAILABLE, 
                "Insight Engine not available"
            );
        }
        
        // 实现屏幕分析逻辑
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Screen analysis not implemented yet"));
        return response;
    }
    
    private McpMessage handleMakeDecision(McpMessage request) {
        // 实现决策逻辑
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Decision making not implemented yet"));
        return response;
    }
    
    private McpMessage handleGetStatus(McpMessage request) {
        Map<String, Object> status = new HashMap<>();
        status.put("running", isRunning);
        status.put("sessions", sessions.size());
        status.put("version", McpProtocol.PROTOCOL_VERSION);
        
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(status);
        return response;
    }
    
    private McpMessage handleSetConfiguration(McpMessage request) {
        // 实现配置设置逻辑
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("message", "Configuration updated successfully"));
        return response;
    }
    
    private McpMessage handlePing(McpMessage request) {
        McpMessage response = McpMessage.createResponse(request, McpProtocol.ResponseStatus.SUCCESS);
        response.setResult(Collections.singletonMap("pong", System.currentTimeMillis()));
        return response;
    }
    
    /**
     * 生成会话ID
     * @return 会话ID
     */
    private String generateSessionId() {
        return "MCP-SESSION-" + UUID.randomUUID().toString().substring(0, 13);
    }
    
    /**
     * 获取当前活动会话数
     * @return 会话数
     */
    public int getActiveSessionsCount() {
        return sessions.size();
    }
    
    /**
     * 检查服务器是否正在运行
     * @return 是否运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 会话内部类
     */
    private static class McpSession {
        private final String sessionId;
        private long lastActivityTime;
        private final Map<String, Object> sessionData = new HashMap<>();
        
        public McpSession(String sessionId) {
            this.sessionId = sessionId;
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public long getLastActivityTime() {
            return lastActivityTime;
        }
        
        public void updateLastActivity() {
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        public void setData(String key, Object value) {
            sessionData.put(key, value);
        }
        
        public Object getData(String key) {
            return sessionData.get(key);
        }
        
        public void close() {
            sessionData.clear();
        }
        
        public boolean isExpired(long timeoutMs) {
            return System.currentTimeMillis() - lastActivityTime > timeoutMs;
        }
    }
    
    /**
     * 消息处理器接口
     */
    private interface MessageHandler {
        McpMessage handle(McpMessage request);
    }
}