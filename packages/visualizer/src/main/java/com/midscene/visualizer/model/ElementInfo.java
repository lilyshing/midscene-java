package com.midscene.visualizer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素信息类
 * 存储UI元素的所有相关属性和状态
 */
public class ElementInfo {
    private String id;
    private String tagName;
    private String text;
    private String contentDescription;
    private String resourceId;
    private String className;
    private String accessibilityId;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean clickable;
    private boolean enabled;
    private boolean selected;
    private boolean visible;
    private Map<String, String> attributes;
    private Map<String, Object> properties;
    private long timestamp;
    
    /**
     * 构造函数
     */
    public ElementInfo() {
        this.attributes = new HashMap<>();
        this.properties = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取元素ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 设置元素ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 获取标签名
     */
    public String getTagName() {
        return tagName;
    }
    
    /**
     * 设置标签名
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    /**
     * 获取文本内容
     */
    public String getText() {
        return text;
    }
    
    /**
     * 设置文本内容
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * 获取内容描述
     */
    public String getContentDescription() {
        return contentDescription;
    }
    
    /**
     * 设置内容描述
     */
    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }
    
    /**
     * 获取资源ID
     */
    public String getResourceId() {
        return resourceId;
    }
    
    /**
     * 设置资源ID
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    /**
     * 获取类名
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * 设置类名
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * 获取可访问性ID
     */
    public String getAccessibilityId() {
        return accessibilityId;
    }
    
    /**
     * 设置可访问性ID
     */
    public void setAccessibilityId(String accessibilityId) {
        this.accessibilityId = accessibilityId;
    }
    
    /**
     * 获取X坐标
     */
    public int getX() {
        return x;
    }
    
    /**
     * 设置X坐标
     */
    public void setX(int x) {
        this.x = x;
    }
    
    /**
     * 获取Y坐标
     */
    public int getY() {
        return y;
    }
    
    /**
     * 设置Y坐标
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * 获取宽度
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * 设置宽度
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * 获取高度
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * 设置高度
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    /**
     * 检查是否可点击
     */
    public boolean isClickable() {
        return clickable;
    }
    
    /**
     * 设置是否可点击
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    
    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 检查是否选中
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * 设置是否选中
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * 检查是否可见
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * 设置是否可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * 获取所有属性
     */
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    /**
     * 设置属性
     */
    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }
    
    /**
     * 获取属性值
     */
    public String getAttribute(String key) {
        return this.attributes.get(key);
    }
    
    /**
     * 获取所有属性
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * 设置属性
     */
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }
    
    /**
     * 获取属性值
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }
    
    /**
     * 获取时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取元素的中心点X坐标
     */
    public int getCenterX() {
        return x + (width / 2);
    }
    
    /**
     * 获取元素的中心点Y坐标
     */
    public int getCenterY() {
        return y + (height / 2);
    }
    
    /**
     * 获取元素的边界信息
     */
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    /**
     * 检查点是否在元素内
     */
    public boolean contains(int pointX, int pointY) {
        return pointX >= x && pointX <= x + width && 
               pointY >= y && pointY <= y + height;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ElementInfo{\n");
        sb.append("  id='").append(id).append("'\n");
        sb.append("  tagName='").append(tagName).append("'\n");
        if (text != null && !text.isEmpty()) {
            sb.append("  text='").append(text.length() > 50 ? text.substring(0, 50) + "..." : text).append("'\n");
        }
        sb.append("  resourceId='").append(resourceId).append("'\n");
        sb.append("  bounds=(")
          .append(x).append(", ")
          .append(y).append(", ")
          .append(width).append(", ")
          .append(height).append(")\n");
        sb.append("  clickable=").append(clickable).append("\n");
        sb.append("  enabled=").append(enabled).append("\n");
        sb.append("  visible=").append(visible).append("\n");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 边界信息类
     */
    public static class Bounds {
        private int x;
        private int y;
        private int width;
        private int height;
        
        public Bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getRight() { return x + width; }
        public int getBottom() { return y + height; }
        public int getCenterX() { return x + width / 2; }
        public int getCenterY() { return y + height / 2; }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ", " + width + ", " + height + ")";
        }
    }
}