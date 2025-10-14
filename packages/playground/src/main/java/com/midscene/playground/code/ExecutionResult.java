package com.midscene.playground.code;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 执行结果类
 * 存储代码执行的结果信息
 */
public class ExecutionResult {
    private final boolean success;
    private final Object returnValue;
    private final String output;
    private final String error;
    private final Map<String, Object> context;
    private final long executionTimeMs;
    private final Exception exception;
    
    /**
     * 私有构造函数，使用Builder模式
     */
    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.returnValue = builder.returnValue;
        this.output = builder.output;
        this.error = builder.error;
        this.context = builder.context != null ? Collections.unmodifiableMap(builder.context) : Collections.emptyMap();
        this.executionTimeMs = builder.executionTimeMs;
        this.exception = builder.exception;
    }
    
    /**
     * 创建成功结果的构建器
     */
    public static Builder success() {
        return new Builder().setSuccess(true);
    }
    
    /**
     * 创建失败结果的构建器
     */
    public static Builder failure() {
        return new Builder().setSuccess(false);
    }
    
    /**
     * 获取是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 获取返回值
     */
    public Object getReturnValue() {
        return returnValue;
    }
    
    /**
     * 获取输出信息
     */
    public String getOutput() {
        return output;
    }
    
    /**
     * 获取错误信息
     */
    public String getError() {
        return error;
    }
    
    /**
     * 获取上下文
     */
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * 获取执行时间（毫秒）
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * 获取异常对象
     */
    public Exception getException() {
        return exception;
    }
    
    /**
     * 获取错误堆栈跟踪
     */
    public String getStackTrace() {
        if (exception == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExecutionResult{").append("success=").append(success);
        
        if (returnValue != null) {
            sb.append(", returnValue=").append(returnValue);
        }
        
        if (output != null) {
            sb.append(", output=\"").append(truncate(output, 100)).append(\"\");
        }
        
        if (error != null) {
            sb.append(", error=\"").append(truncate(error, 100)).append(\"\");
        }
        
        sb.append(", executionTimeMs=").append(executionTimeMs);
        sb.append(", contextSize=").append(context.size());
        
        if (exception != null) {
            sb.append(", exception=").append(exception.getClass().getSimpleName());
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private boolean success;
        private Object returnValue;
        private String output;
        private String error;
        private Map<String, Object> context;
        private long executionTimeMs;
        private Exception exception;
        
        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
            return this;
        }
        
        public Builder setOutput(String output) {
            this.output = output;
            return this;
        }
        
        public Builder setError(String error) {
            this.error = error;
            return this;
        }
        
        public Builder setContext(Map<String, Object> context) {
            this.context = context;
            return this;
        }
        
        public Builder setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public Builder setException(Exception exception) {
            this.exception = exception;
            if (exception != null) {
                this.success = false;
                if (this.error == null) {
                    this.error = exception.getMessage();
                }
            }
            return this;
        }
        
        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}