package com.midscene.visualizer.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 调试数据类
 * 用于存储测试过程中的自定义调试信息
 */
public class DebugData {
    private String key;
    private Object value;
    private LocalDateTime timestamp;
    private String dataType;
    private String description;
    private String source;
    
    /**
     * 构造函数
     */
    public DebugData(String key, Object value) {
        this.key = key;
        this.value = value;
        this.timestamp = LocalDateTime.now();
        this.dataType = value != null ? value.getClass().getName() : "null";
    }
    
    /**
     * 获取键名
     */
    public String getKey() {
        return key;
    }
    
    /**
     * 设置键名
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * 获取值
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * 设置值
     */
    public void setValue(Object value) {
        this.value = value;
        this.dataType = value != null ? value.getClass().getName() : "null";
    }
    
    /**
     * 获取时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置时间戳
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取数据类型
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * 设置数据类型
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取数据源
     */
    public String getSource() {
        return source;
    }
    
    /**
     * 设置数据源
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * 获取字符串形式的值
     */
    public String getValueAsString() {
        if (value == null) {
            return "null";
        }
        // 对于数组和集合类型，限制输出长度
        String valueStr = value.toString();
        if (valueStr.length() > 200) {
            return valueStr.substring(0, 200) + "...";
        }
        return valueStr;
    }
    
    /**
     * 检查是否为基本类型
     */
    public boolean isPrimitive() {
        return value instanceof String || 
               value instanceof Number || 
               value instanceof Boolean ||
               value instanceof Character;
    }
    
    /**
     * 检查是否为复杂类型
     */
    public boolean isComplex() {
        return !isPrimitive() && value != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugData debugData = (DebugData) o;
        return Objects.equals(key, debugData.key) && 
               Objects.equals(timestamp, debugData.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(key, timestamp);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DebugData{\n");
        sb.append("  key='").append(key).append("'\n");
        sb.append("  type='").append(dataType).append("'\n");
        sb.append("  value='").append(getValueAsString()).append("'\n");
        sb.append("  timestamp=").append(timestamp).append("\n");
        if (description != null) {
            sb.append("  description='").append(description).append("'\n");
        }
        if (source != null) {
            sb.append("  source='").append(source).append("'\n");
        }
        sb.append("}");
        return sb.toString();
    }
}