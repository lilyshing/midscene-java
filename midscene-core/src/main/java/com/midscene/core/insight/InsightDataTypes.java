package com.midscene.core.insight;

import java.util.Map;
import java.util.List;

/**
 * AI请求类，封装AI模型调用所需的参数
 */
class AIRequest {
    private String prompt; // 用户提示
    private Map<String, String> schema; // 数据提取模式
    private Object context; // UI上下文
    private TaskType taskType; // 任务类型
    private Map<String, Object> additionalParams; // 额外参数
    
    public AIRequest() {
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public Map<String, String> getSchema() {
        return schema;
    }
    
    public void setSchema(Map<String, String> schema) {
        this.schema = schema;
    }
    
    public Object getContext() {
        return context;
    }
    
    public void setContext(Object context) {
        this.context = context;
    }
    
    public TaskType getTaskType() {
        return taskType;
    }
    
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
    
    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }
    
    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }
}

/**
 * 操作计划类，包含AI生成的操作步骤
 */
class OperationPlan {
    private List<OperationStep> steps; // 操作步骤列表
    private String planExplanation; // 计划解释
    private Map<String, Object> metadata; // 元数据
    
    public OperationPlan() {
    }
    
    public List<OperationStep> getSteps() {
        return steps;
    }
    
    public void setSteps(List<OperationStep> steps) {
        this.steps = steps;
    }
    
    public String getPlanExplanation() {
        return planExplanation;
    }
    
    public void setPlanExplanation(String planExplanation) {
        this.planExplanation = planExplanation;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}

/**
 * 操作步骤类
 */
class OperationStep {
    private String action; // 操作类型（tap, input, scroll等）
    private Map<String, Object> params; // 操作参数
    private String description; // 步骤描述
    private int priority; // 优先级
    
    public OperationStep() {
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
}

/**
 * 元素定位结果类
 */
class LocateResult {
    private String elementId; // 元素ID
    private double x; // X坐标
    private double y; // Y坐标
    private double confidence; // 置信度
    private String description; // 元素描述
    private Map<String, Object> properties; // 元素属性
    
    public LocateResult() {
    }
    
    public String getElementId() {
        return elementId;
    }
    
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}

/**
 * 断言结果类
 */
class AssertResult {
    private boolean passed; // 是否通过
    private String message; // 结果消息
    private double confidence; // 置信度
    private Map<String, Object> details; // 详细信息
    
    public AssertResult() {
    }
    
    public boolean isPassed() {
        return passed;
    }
    
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}

/**
 * 任务类型枚举
 */
enum TaskType {
    INTENT_ANALYSIS, // 意图分析
    ELEMENT_LOCATION, // 元素定位
    DATA_EXTRACTION, // 数据提取
    ASSERTION, // 断言验证
    EXECUTION_PLANNING // 执行规划
}