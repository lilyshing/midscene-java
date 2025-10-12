package com.midscene.core.model;

/**
 * 操作类，表示Agent执行的具体操作
 */
public class Action {
    private ActionType type;
    private String description;
    private String targetDescription;
    private Point coordinates;
    private String text;
    private ScrollDirection scrollDirection;
    private Integer scrollDistance;
    private String verificationCondition;
    private int priority;
    private long timeoutMs; // 操作超时时间（毫秒）
    private boolean retryOnFailure; // 失败时是否重试
    private int maxRetries; // 最大重试次数
    
    public Action(ActionType type, String description) {
        this.type = type;
        this.description = description;
        this.priority = 0;
        this.timeoutMs = 30000; // 默认30秒超时
        this.retryOnFailure = true; // 默认启用重试
        this.maxRetries = 3; // 默认最多重试3次
    }
    
    public ActionType getType() {
        return type;
    }
    
    public void setType(ActionType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTargetDescription() {
        return targetDescription;
    }
    
    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }
    
    public Point getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public ScrollDirection getScrollDirection() {
        return scrollDirection;
    }
    
    public void setScrollDirection(ScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
    }
    
    public Integer getScrollDistance() {
        return scrollDistance;
    }
    
    public void setScrollDistance(Integer scrollDistance) {
        this.scrollDistance = scrollDistance;
    }
    
    public String getVerificationCondition() {
        return verificationCondition;
    }
    
    public void setVerificationCondition(String verificationCondition) {
        this.verificationCondition = verificationCondition;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public long getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public boolean isRetryOnFailure() {
        return retryOnFailure;
    }
    
    public void setRetryOnFailure(boolean retryOnFailure) {
        this.retryOnFailure = retryOnFailure;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    @Override
    public String toString() {
        return "Action{" +
               "type=" + type +
               ", description='" + description + '\'' +
               ", targetDescription='" + targetDescription + '\'' +
               ", text='" + text + '\'' +
               ", verificationCondition='" + verificationCondition + '\'' +
               ", priority=" + priority +
               ", timeoutMs=" + timeoutMs +
               ", retryOnFailure=" + retryOnFailure +
               ", maxRetries=" + maxRetries +
               '}';
    }
}