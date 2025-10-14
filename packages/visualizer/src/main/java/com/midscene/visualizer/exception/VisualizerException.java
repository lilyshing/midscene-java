package com.midscene.visualizer.exception;

/**
 * 可视化调试异常类
 * 用于处理可视化调试过程中的特定异常情况
 */
public class VisualizerException extends RuntimeException {
    
    /**
     * 异常类型枚举
     */
    public enum ExceptionType {
        DEVICE_CONNECTION,
        SCREENSHOT_CAPTURE,
        ELEMENT_NOT_FOUND,
        EXPORT_FAILED,
        RENDER_FAILED,
        INVALID_FORMAT,
        SESSION_ERROR,
        UNSUPPORTED_OPERATION,
        PERMISSION_DENIED,
        TIMEOUT,
        INTERNAL_ERROR
    }
    
    private final ExceptionType type;
    private final String details;
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param type 异常类型
     */
    public VisualizerException(String message, ExceptionType type) {
        super(message);
        this.type = type;
        this.details = null;
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param type 异常类型
     * @param cause 异常原因
     */
    public VisualizerException(String message, ExceptionType type, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.details = null;
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param type 异常类型
     * @param details 详细信息
     */
    public VisualizerException(String message, ExceptionType type, String details) {
        super(message);
        this.type = type;
        this.details = details;
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param type 异常类型
     * @param details 详细信息
     * @param cause 异常原因
     */
    public VisualizerException(String message, ExceptionType type, String details, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.details = details;
    }
    
    /**
     * 获取异常类型
     * @return 异常类型
     */
    public ExceptionType getType() {
        return type;
    }
    
    /**
     * 获取详细信息
     * @return 详细信息
     */
    public String getDetails() {
        return details;
    }
    
    /**
     * 创建设备连接异常
     */
    public static VisualizerException deviceConnection(String message) {
        return new VisualizerException(message, ExceptionType.DEVICE_CONNECTION);
    }
    
    /**
     * 创建截图捕获异常
     */
    public static VisualizerException screenshotCapture(String message, Throwable cause) {
        return new VisualizerException(message, ExceptionType.SCREENSHOT_CAPTURE, cause);
    }
    
    /**
     * 创建元素未找到异常
     */
    public static VisualizerException elementNotFound(String elementId) {
        return new VisualizerException("Element not found: " + elementId, ExceptionType.ELEMENT_NOT_FOUND);
    }
    
    /**
     * 创建导出失败异常
     */
    public static VisualizerException exportFailed(String format, Throwable cause) {
        return new VisualizerException("Failed to export to " + format, ExceptionType.EXPORT_FAILED, cause);
    }
    
    /**
     * 创建渲染失败异常
     */
    public static VisualizerException renderFailed(String format, Throwable cause) {
        return new VisualizerException("Failed to render in " + format, ExceptionType.RENDER_FAILED, cause);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VisualizerException[type=")
          .append(type)
          .append(", message=")
          .append(getMessage());
        
        if (details != null) {
            sb.append(", details=")
              .append(details);
        }
        
        if (getCause() != null) {
            sb.append(", cause=")
              .append(getCause().getMessage());
        }
        
        sb.append("]");
        return sb.toString();
    }
}