package com.midscene.core.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务执行结果类
 */
public class TaskResult {
    private final TaskStatus status;
    private final String message;
    private final Object data;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;
    
    private TaskResult(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.data = builder.data;
        this.timestamp = LocalDateTime.now();
        this.metadata = builder.metadata;
    }

    public TaskStatus getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public boolean isSuccess() {
        return status == TaskStatus.COMPLETED;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 创建失败的任务结果
     */
    public static TaskResult fail(String message) {
        return builder()
                .status(TaskStatus.FAILED)
                .message(message)
                .build();
    }
    
    public static class Builder {
        private TaskStatus status;
        private String message;
        private Object data;
        private Map<String, Object> metadata;
        
        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder data(Object data) {
            this.data = data;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public TaskResult build() {
            return new TaskResult(this);
        }
    }
}