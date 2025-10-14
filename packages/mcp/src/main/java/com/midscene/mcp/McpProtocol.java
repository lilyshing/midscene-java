package com.midscene.mcp;

/**
 * MCP（Model Control Protocol）协议定义类
 * 定义模型控制协议的常量、命令类型、响应状态等基础结构
 */
public class McpProtocol {
    
    // 协议版本
    public static final String PROTOCOL_VERSION = "1.0.0";
    
    // 默认端口
    public static final int DEFAULT_PORT = 50051;
    
    // 消息分隔符
    public static final String MESSAGE_SEPARATOR = "\n";
    public static final String FIELD_SEPARATOR = ":";
    
    /**
     * 命令类型枚举
     */
    public enum CommandType {
        // 控制命令
        START_SESSION,         // 开始会话
        END_SESSION,           // 结束会话
        SEND_PROMPT,           // 发送提示词
        CANCEL_PROCESSING,     // 取消处理
        SET_CONFIGURATION,     // 设置配置
        GET_STATUS,            // 获取状态
        
        // 视觉相关命令
        PROCESS_IMAGE,         // 处理图像
        EXTRACT_ELEMENTS,      // 提取元素
        ANALYZE_SCREEN,        // 分析屏幕
        
        // 决策相关命令
        MAKE_DECISION,         // 做出决策
        EXECUTE_ACTION,        // 执行动作
        
        // 调试命令
        LOG_MESSAGE,           // 记录日志
        DEBUG_INFO,            // 调试信息
        PING                   // 心跳检测
    }
    
    /**
     * 响应状态枚举
     */
    public enum ResponseStatus {
        SUCCESS(200, "成功"),
        PROCESSING(202, "处理中"),
        BAD_REQUEST(400, "请求错误"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "未找到"),
        INTERNAL_ERROR(500, "内部错误"),
        SERVICE_UNAVAILABLE(503, "服务不可用");
        
        private final int code;
        private final String message;
        
        ResponseStatus(int code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public static ResponseStatus fromCode(int code) {
            for (ResponseStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return INTERNAL_ERROR;
        }
    }
    
    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        // 通用错误
        GENERIC_ERROR("ERR_GENERIC", "通用错误"),
        INVALID_PARAMETER("ERR_INVALID_PARAM", "无效参数"),
        SESSION_NOT_FOUND("ERR_SESSION_NOT_FOUND", "会话未找到"),
        
        // 处理错误
        PROCESSING_ERROR("ERR_PROCESSING", "处理错误"),
        TIMEOUT_ERROR("ERR_TIMEOUT", "超时错误"),
        
        // 资源错误
        RESOURCE_ERROR("ERR_RESOURCE", "资源错误"),
        MEMORY_LIMIT_EXCEEDED("ERR_MEMORY_LIMIT", "内存限制超出"),
        
        // 模型错误
        MODEL_ERROR("ERR_MODEL", "模型错误"),
        MODEL_NOT_READY("ERR_MODEL_NOT_READY", "模型未准备好"),
        
        // 网络错误
        NETWORK_ERROR("ERR_NETWORK", "网络错误"),
        CONNECTION_REFUSED("ERR_CONNECTION_REFUSED", "连接被拒绝");
        
        private final String code;
        private final String message;
        
        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public static ErrorCode fromCode(String code) {
            for (ErrorCode error : values()) {
                if (error.code.equals(code)) {
                    return error;
                }
            }
            return GENERIC_ERROR;
        }
    }
    
    /**
     * 模型类型枚举
     */
    public enum ModelType {
        TEXT_GENERATION("text_gen"),
        IMAGE_ANALYSIS("image_analysis"),
        MULTIMODAL("multimodal"),
        VISION_LANGUAGE("vision_language"),
        DECISION_MAKING("decision_making"),
        CODE_GENERATION("code_gen");
        
        private final String value;
        
        ModelType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static ModelType fromValue(String value) {
            for (ModelType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return TEXT_GENERATION;
        }
    }
    
    /**
     * 配置键常量
     */
    public static class ConfigKeys {
        public static final String MODEL_NAME = "model_name";
        public static final String API_KEY = "api_key";
        public static final String TEMPERATURE = "temperature";
        public static final String MAX_TOKENS = "max_tokens";
        public static final String TIMEOUT = "timeout";
        public static final String CONTEXT_SIZE = "context_size";
        public static final String ENABLE_STREAMING = "enable_streaming";
        public static final String VERBOSE_LOGGING = "verbose_logging";
        public static final String PROXY_SETTINGS = "proxy_settings";
        public static final String CUSTOM_HEADERS = "custom_headers";
    }
    
    /**
     * 性能指标常量
     */
    public static class PerformanceMetrics {
        public static final String PROCESSING_TIME = "processing_time_ms";
        public static final String TOKEN_COUNT = "token_count";
        public static final String TOKENS_PER_SECOND = "tokens_per_second";
        public static final String MEMORY_USAGE = "memory_usage_mb";
        public static final String REQUEST_QUEUE_SIZE = "request_queue_size";
    }
    
    /**
     * 获取协议标识
     * @return 协议标识字符串
     */
    public static String getProtocolIdentifier() {
        return "MCP/v" + PROTOCOL_VERSION;
    }
    
    /**
     * 验证协议版本兼容性
     * @param version 要验证的版本
     * @return 是否兼容
     */
    public static boolean isCompatibleVersion(String version) {
        // 简单的版本兼容性检查，实际项目中可能需要更复杂的逻辑
        return version != null && version.startsWith("1.");
    }
}