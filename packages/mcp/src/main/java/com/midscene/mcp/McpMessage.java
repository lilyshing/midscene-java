package com.midscene.mcp;

import com.midscene.core.exception.PlatformException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * MCP消息类
 * 定义MCP协议的消息结构，支持序列化和反序列化
 */
public class McpMessage {
    private static final AtomicLong MESSAGE_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    // 消息头部字段
    private String messageId;
    private String sessionId;
    private String timestamp;
    private McpProtocol.CommandType commandType;
    private String protocolVersion;
    private boolean isRequest;
    
    // 请求相关字段
    private Map<String, Object> parameters;
    private byte[] binaryData;
    
    // 响应相关字段
    private McpProtocol.ResponseStatus status;
    private String errorCode;
    private String errorMessage;
    private Object result;
    
    // 性能指标
    private Map<String, Object> metrics;
    
    /**
     * 创建新的请求消息
     * @param commandType 命令类型
     * @return MCP消息对象
     */
    public static McpMessage createRequest(McpProtocol.CommandType commandType) {
        McpMessage message = new McpMessage();
        message.messageId = generateMessageId();
        message.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        message.commandType = commandType;
        message.protocolVersion = McpProtocol.PROTOCOL_VERSION;
        message.isRequest = true;
        message.parameters = new HashMap<>();
        message.metrics = new HashMap<>();
        return message;
    }
    
    /**
     * 创建响应消息
     * @param request 请求消息
     * @param status 响应状态
     * @return MCP消息对象
     */
    public static McpMessage createResponse(McpMessage request, McpProtocol.ResponseStatus status) {
        McpMessage response = new McpMessage();
        response.messageId = generateMessageId();
        response.sessionId = request.getSessionId();
        response.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        response.commandType = request.getCommandType();
        response.protocolVersion = request.getProtocolVersion();
        response.isRequest = false;
        response.status = status;
        response.parameters = new HashMap<>();
        response.metrics = new HashMap<>();
        return response;
    }
    
    /**
     * 创建错误响应
     * @param request 请求消息
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @return MCP消息对象
     */
    public static McpMessage createErrorResponse(McpMessage request, McpProtocol.ErrorCode errorCode, String errorMessage) {
        McpMessage response = createResponse(request, McpProtocol.ResponseStatus.INTERNAL_ERROR);
        response.errorCode = errorCode.getCode();
        response.errorMessage = errorMessage != null ? errorMessage : errorCode.getMessage();
        return response;
    }
    
    /**
     * 从字符串解析MCP消息
     * @param messageStr 消息字符串
     * @return MCP消息对象
     * @throws PlatformException 如果解析失败
     */
    public static McpMessage fromString(String messageStr) throws PlatformException {
        try {
            McpMessage message = new McpMessage();
            String[] parts = messageStr.split(McpProtocol.MESSAGE_SEPARATOR);
            
            // 解析头部信息
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                
                int separatorIndex = part.indexOf(McpProtocol.FIELD_SEPARATOR);
                if (separatorIndex == -1) {
                    continue;
                }
                
                String key = part.substring(0, separatorIndex).trim();
                String value = part.substring(separatorIndex + 1).trim();
                
                switch (key.toUpperCase()) {
                    case "MID": // Message ID
                        message.messageId = value;
                        break;
                    case "SID": // Session ID
                        message.sessionId = value;
                        break;
                    case "TS": // Timestamp
                        message.timestamp = value;
                        break;
                    case "CMD": // Command Type
                        try {
                            message.commandType = McpProtocol.CommandType.valueOf(value);
                        } catch (IllegalArgumentException e) {
                            throw new PlatformException("Invalid command type: " + value);
                        }
                        break;
                    case "VER": // Protocol Version
                        message.protocolVersion = value;
                        break;
                    case "TYPE": // Message Type (REQ/RES)
                        message.isRequest = "REQ".equals(value);
                        break;
                    case "STAT": // Status (for response)
                        try {
                            int statusCode = Integer.parseInt(value);
                            message.status = McpProtocol.ResponseStatus.fromCode(statusCode);
                        } catch (NumberFormatException e) {
                            throw new PlatformException("Invalid status code: " + value);
                        }
                        break;
                    case "ERR": // Error Code (for error response)
                        message.errorCode = value;
                        break;
                    case "MSG": // Error Message (for error response)
                        message.errorMessage = value;
                        break;
                    case "DATA": // Binary Data (Base64 encoded)
                        message.binaryData = Base64.getDecoder().decode(value);
                        break;
                    // 参数和结果会在JSON解析中处理
                }
            }
            
            // 这里简化了参数和结果的解析逻辑
            // 实际项目中应该使用JSON库解析复杂的参数和结果
            
            return message;
        } catch (Exception e) {
            throw new PlatformException("Failed to parse MCP message: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将消息序列化为字符串
     * @return 序列化后的消息字符串
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        // 添加头部信息
        builder.append("MID:").append(messageId).append(McpProtocol.MESSAGE_SEPARATOR);
        if (sessionId != null) {
            builder.append("SID:").append(sessionId).append(McpProtocol.MESSAGE_SEPARATOR);
        }
        builder.append("TS:").append(timestamp).append(McpProtocol.MESSAGE_SEPARATOR);
        builder.append("CMD:").append(commandType).append(McpProtocol.MESSAGE_SEPARATOR);
        builder.append("VER:").append(protocolVersion).append(McpProtocol.MESSAGE_SEPARATOR);
        builder.append("TYPE:").append(isRequest ? "REQ" : "RES").append(McpProtocol.MESSAGE_SEPARATOR);
        
        // 添加响应信息（如果是响应消息）
        if (!isRequest) {
            builder.append("STAT:").append(status.getCode()).append(McpProtocol.MESSAGE_SEPARATOR);
            if (errorCode != null) {
                builder.append("ERR:").append(errorCode).append(McpProtocol.MESSAGE_SEPARATOR);
            }
            if (errorMessage != null) {
                builder.append("MSG:").append(errorMessage).append(McpProtocol.MESSAGE_SEPARATOR);
            }
        }
        
        // 添加二进制数据（如果有）
        if (binaryData != null && binaryData.length > 0) {
            String base64Data = Base64.getEncoder().encodeToString(binaryData);
            builder.append("DATA:").append(base64Data).append(McpProtocol.MESSAGE_SEPARATOR);
        }
        
        // 添加参数（简化处理，实际应该使用JSON）
        if (parameters != null && !parameters.isEmpty()) {
            // 这里简化了参数的序列化逻辑
            // 实际项目中应该使用JSON库序列化复杂的参数
        }
        
        // 添加性能指标（如果有）
        if (metrics != null && !metrics.isEmpty()) {
            // 这里简化了性能指标的序列化逻辑
            // 实际项目中应该使用JSON库序列化复杂的指标
        }
        
        return builder.toString();
    }
    
    // 私有方法：生成唯一的消息ID
    private static String generateMessageId() {
        return "MCP-" + UUID.randomUUID().toString().substring(0, 8) + "-" + MESSAGE_COUNTER.incrementAndGet();
    }
    
    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public McpProtocol.CommandType getCommandType() {
        return commandType;
    }
    
    public void setCommandType(McpProtocol.CommandType commandType) {
        this.commandType = commandType;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public boolean isRequest() {
        return isRequest;
    }
    
    public void setRequest(boolean request) {
        isRequest = request;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }
    
    public <T> T getParameter(String key, Class<T> type) {
        if (parameters == null) {
            return null;
        }
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    public byte[] getBinaryData() {
        return binaryData;
    }
    
    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }
    
    public void setBinaryDataFromString(String data) {
        this.binaryData = data.getBytes(StandardCharsets.UTF_8);
    }
    
    public String getBinaryDataAsString() {
        if (binaryData == null) {
            return null;
        }
        return new String(binaryData, StandardCharsets.UTF_8);
    }
    
    public McpProtocol.ResponseStatus getStatus() {
        return status;
    }
    
    public void setStatus(McpProtocol.ResponseStatus status) {
        this.status = status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public Map<String, Object> getMetrics() {
        return metrics;
    }
    
    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
    
    public void addMetric(String key, Object value) {
        if (metrics == null) {
            metrics = new HashMap<>();
        }
        metrics.put(key, value);
    }
    
    public <T> T getMetric(String key, Class<T> type) {
        if (metrics == null) {
            return null;
        }
        Object value = metrics.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    /**
     * 检查消息是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return messageId != null && commandType != null && protocolVersion != null;
    }
    
    /**
     * 检查是否为成功响应
     * @return 是否成功
     */
    public boolean isSuccess() {
        return !isRequest && status == McpProtocol.ResponseStatus.SUCCESS && errorCode == null;
    }
    
    /**
     * 检查是否为错误响应
     * @return 是否错误
     */
    public boolean isError() {
        return !isRequest && (status != McpProtocol.ResponseStatus.SUCCESS || errorCode != null);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        McpMessage other = (McpMessage) obj;
        if (messageId == null) {
            return other.messageId == null;
        } else {
            return messageId.equals(other.messageId);
        }
    }
}