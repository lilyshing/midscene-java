package com.midscene.mcp;

import com.midscene.core.exception.PlatformException;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MCP客户端类
 * 实现与MCP协议服务器的通信功能
 */
public class McpClient implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(McpClient.class.getName());
    
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String sessionId;
    private boolean isConnected;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    // 超时设置
    private int connectionTimeout = 10000;
    private int readTimeout = 30000;
    private int writeTimeout = 10000;
    
    /**
     * 构造函数
     * @param host 服务器主机名
     * @param port 服务器端口
     */
    public McpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * 构造函数（使用默认端口）
     * @param host 服务器主机名
     */
    public McpClient(String host) {
        this(host, McpProtocol.DEFAULT_PORT);
    }
    
    /**
     * 设置连接超时时间
     * @param timeout 超时时间（毫秒）
     */
    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
    }
    
    /**
     * 设置读取超时时间
     * @param timeout 超时时间（毫秒）
     */
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }
    
    /**
     * 设置写入超时时间
     * @param timeout 超时时间（毫秒）
     */
    public void setWriteTimeout(int timeout) {
        this.writeTimeout = timeout;
    }
    
    /**
     * 连接到服务器
     * @throws PlatformException 如果连接失败
     */
    public void connect() throws PlatformException {
        try {
            if (isConnected) {
                logger.warning("Already connected to MCP server");
                return;
            }
            
            logger.info("Connecting to MCP server at " + host + ":" + port);
            
            // 创建socket连接
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, connectionTimeout);
            socket.setSoTimeout(readTimeout);
            
            // 初始化IO流
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            
            isConnected = true;
            logger.info("Successfully connected to MCP server");
            
            // 启动会话
            startSession();
            
        } catch (IOException e) {
            close(); // 确保资源被释放
            throw new PlatformException("Failed to connect to MCP server", e);
        }
    }
    
    /**
     * 断开连接
     */
    @Override
    public void close() {
        try {
            if (isConnected) {
                // 结束会话
                try {
                    endSession();
                } catch (Exception ignored) {
                }
                
                // 关闭IO流
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                
                isConnected = false;
                sessionId = null;
                logger.info("Disconnected from MCP server");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing connection to MCP server", e);
        }
        
        // 关闭线程池
        executorService.shutdownNow();
    }
    
    /**
     * 启动会话
     * @throws PlatformException 如果启动会话失败
     */
    public void startSession() throws PlatformException {
        checkConnection();
        
        try {
            // 创建启动会话请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.START_SESSION);
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to start session: " + response.getErrorMessage());
            }
            
            // 获取会话ID
            Map<String, Object> result = response.getResult();
            if (result != null && result.containsKey("sessionId")) {
                this.sessionId = (String) result.get("sessionId");
                logger.info("Session started with ID: " + sessionId);
            } else {
                throw new PlatformException("Invalid session response: sessionId not found");
            }
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to start session", e);
        }
    }
    
    /**
     * 结束会话
     * @throws PlatformException 如果结束会话失败
     */
    public void endSession() throws PlatformException {
        if (!isConnected || sessionId == null) {
            return;
        }
        
        try {
            // 创建结束会话请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.END_SESSION);
            request.setSessionId(sessionId);
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                logger.warning("Failed to end session: " + response.getErrorMessage());
            }
            
            logger.info("Session ended: " + sessionId);
            sessionId = null;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error ending session: " + sessionId, e);
            // 不抛出异常，因为会话结束是关闭过程的一部分
        }
    }
    
    /**
     * 发送提示词到服务器
     * @param prompt 提示词内容
     * @return 服务器响应结果
     * @throws PlatformException 如果请求失败
     */
    public String sendPrompt(String prompt) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.SEND_PROMPT);
            request.setSessionId(sessionId);
            request.addParameter("prompt", prompt);
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to send prompt: " + response.getErrorMessage());
            }
            
            // 提取结果
            Map<String, Object> result = response.getResult();
            if (result != null && result.containsKey("result")) {
                return (String) result.get("result");
            } else {
                throw new PlatformException("Invalid response: result not found");
            }
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to send prompt", e);
        }
    }
    
    /**
     * 获取服务器状态
     * @return 服务器状态信息
     * @throws PlatformException 如果请求失败
     */
    public Map<String, Object> getStatus() throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.GET_STATUS);
            if (sessionId != null) {
                request.setSessionId(sessionId);
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to get status: " + response.getErrorMessage());
            }
            
            return response.getResult();
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to get status", e);
        }
    }
    
    /**
     * 发送ping请求到服务器
     * @return 服务器响应时间
     * @throws PlatformException 如果请求失败
     */
    public long ping() throws PlatformException {
        checkConnection();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 创建ping请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.PING);
            if (sessionId != null) {
                request.setSessionId(sessionId);
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Ping failed: " + response.getErrorMessage());
            }
            
            long endTime = System.currentTimeMillis();
            return endTime - startTime;
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Ping failed", e);
        }
    }
    
    /**
     * 设置服务器配置
     * @param configuration 配置参数
     * @return 是否成功
     * @throws PlatformException 如果请求失败
     */
    public boolean setConfiguration(Map<String, Object> configuration) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.SET_CONFIGURATION);
            request.setSessionId(sessionId);
            request.setParameters(configuration);
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 返回响应状态
            return response.isSuccess();
            
        } catch (Exception e) {
            throw new PlatformException("Failed to set configuration", e);
        }
    }
    
    /**
     * 处理图像
     * @param imageData 图像数据（Base64编码）
     * @param parameters 处理参数
     * @return 处理结果
     * @throws PlatformException 如果请求失败
     */
    public Map<String, Object> processImage(String imageData, Map<String, Object> parameters) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.PROCESS_IMAGE);
            request.setSessionId(sessionId);
            request.addParameter("imageData", imageData);
            
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    request.addParameter(entry.getKey(), entry.getValue());
                }
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to process image: " + response.getErrorMessage());
            }
            
            return response.getResult();
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to process image", e);
        }
    }
    
    /**
     * 提取UI元素
     * @param parameters 提取参数
     * @return 提取的元素列表
     * @throws PlatformException 如果请求失败
     */
    public Map<String, Object> extractElements(Map<String, Object> parameters) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.EXTRACT_ELEMENTS);
            request.setSessionId(sessionId);
            
            if (parameters != null) {
                request.setParameters(parameters);
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to extract elements: " + response.getErrorMessage());
            }
            
            return response.getResult();
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to extract elements", e);
        }
    }
    
    /**
     * 分析屏幕
     * @param parameters 分析参数
     * @return 分析结果
     * @throws PlatformException 如果请求失败
     */
    public Map<String, Object> analyzeScreen(Map<String, Object> parameters) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.ANALYZE_SCREEN);
            request.setSessionId(sessionId);
            
            if (parameters != null) {
                request.setParameters(parameters);
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to analyze screen: " + response.getErrorMessage());
            }
            
            return response.getResult();
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to analyze screen", e);
        }
    }
    
    /**
     * 做出决策
     * @param parameters 决策参数
     * @return 决策结果
     * @throws PlatformException 如果请求失败
     */
    public Map<String, Object> makeDecision(Map<String, Object> parameters) throws PlatformException {
        checkConnection();
        
        try {
            // 创建请求
            McpMessage request = McpMessage.createRequest(McpProtocol.CommandType.MAKE_DECISION);
            request.setSessionId(sessionId);
            
            if (parameters != null) {
                request.setParameters(parameters);
            }
            
            // 发送请求并获取响应
            McpMessage response = sendRequest(request);
            
            // 验证响应状态
            if (!response.isSuccess()) {
                throw new PlatformException("Failed to make decision: " + response.getErrorMessage());
            }
            
            return response.getResult();
            
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Failed to make decision", e);
        }
    }
    
    /**
     * 发送请求并获取响应
     * @param request 请求消息
     * @return 响应消息
     * @throws Exception 如果通信失败
     */
    private McpMessage sendRequest(McpMessage request) throws Exception {
        // 发送请求
        String requestStr = request.toString();
        logger.fine("Sending request: " + requestStr);
        
        // 创建一个Callable任务来执行发送和接收操作
        Callable<McpMessage> task = () -> {
            synchronized (this) {
                // 发送请求
                writer.println(requestStr);
                writer.flush();
                
                // 接收响应
                String responseStr = readResponse();
                logger.fine("Received response: " + responseStr);
                
                // 解析响应
                return McpMessage.fromString(responseStr);
            }
        };
        
        // 使用超时执行任务
        Future<McpMessage> future = executorService.submit(task);
        try {
            return future.get(readTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new PlatformException("Request timed out after " + readTimeout + "ms");
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
    
    /**
     * 读取服务器响应
     * @return 响应字符串
     * @throws IOException 如果读取失败
     */
    private String readResponse() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        
        // 读取直到消息结束标记或连接关闭
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append(McpProtocol.MESSAGE_SEPARATOR);
            // 简单的消息边界检测，实际项目中应该使用更复杂的协议
            if (line.isEmpty()) {
                break;
            }
        }
        
        if (responseBuilder.length() == 0) {
            throw new IOException("Empty response from server");
        }
        
        return responseBuilder.toString();
    }
    
    /**
     * 检查连接状态
     * @throws PlatformException 如果未连接
     */
    private void checkConnection() throws PlatformException {
        if (!isConnected) {
            throw new PlatformException("Not connected to MCP server");
        }
    }
    
    /**
     * 获取连接状态
     * @return 是否连接
     */
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed() && socket.isConnected();
    }
    
    /**
     * 获取会话ID
     * @return 会话ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 获取服务器主机名
     * @return 主机名
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取服务器端口
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 测试连接是否可用
     * @return 是否可用
     */
    public boolean testConnection() {
        try {
            if (!isConnected()) {
                return false;
            }
            
            ping();
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Connection test failed", e);
            return false;
        }
    }
    
    /**
     * 重新连接
     * @throws PlatformException 如果重连失败
     */
    public void reconnect() throws PlatformException {
        try {
            close();
            connect();
        } catch (Exception e) {
            throw new PlatformException("Failed to reconnect", e);
        }
    }
    
    /**
     * 创建一个简单的客户端实例（使用默认参数）
     * @param host 服务器主机名
     * @return 客户端实例
     */
    public static McpClient createDefault(String host) {
        McpClient client = new McpClient(host);
        // 设置默认超时值
        client.setConnectionTimeout(5000);
        client.setReadTimeout(15000);
        client.setWriteTimeout(5000);
        return client;
    }
}