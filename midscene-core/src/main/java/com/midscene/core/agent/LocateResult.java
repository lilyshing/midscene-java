package com.midscene.core.agent;

import com.midscene.core.model.Point;
import com.midscene.core.model.Rect;
import java.util.Optional;

/**
 * 元素定位结果
 * 包含定位到的元素信息、位置、置信度等
 */
public class LocateResult {
    private boolean success;
    private Object element;
    private Rect boundingBox;
    private Point centerPoint;
    private String elementInfo;
    private double confidence;
    private String errorMessage;
    private String strategy;
    
    public LocateResult() {
        this.success = false;
        this.confidence = 0.0;
    }
    
    public LocateResult(boolean success) {
        this.success = success;
        this.confidence = 0.0;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Optional<Object> getElement() {
        return Optional.ofNullable(element);
    }
    
    public void setElement(Object element) {
        this.element = element;
    }
    
    public Optional<Rect> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }
    
    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public Optional<Point> getCenterPoint() {
        return Optional.ofNullable(centerPoint);
    }
    
    public void setCenterPoint(Point centerPoint) {
        this.centerPoint = centerPoint;
    }
    
    public Optional<String> getElementInfo() {
        return Optional.ofNullable(elementInfo);
    }
    
    public void setElementInfo(String elementInfo) {
        this.elementInfo = elementInfo;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Optional<String> getStrategy() {
        return Optional.ofNullable(strategy);
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    /**
     * 创建成功的定位结果
     * @param element 定位到的元素
     * @param boundingBox 元素边界框
     * @param confidence 置信度
     * @return 定位结果对象
     */
    public static LocateResult createSuccess(Object element, Rect boundingBox, double confidence) {
        LocateResult result = new LocateResult(true);
        result.setElement(element);
        result.setBoundingBox(boundingBox);
        
        // 计算中心点
        if (boundingBox != null) {
            result.setCenterPoint(boundingBox.getCenter());
        }
        
        result.setConfidence(confidence);
        return result;
    }
    
    /**
     * 创建失败的定位结果
     * @param errorMessage 错误信息
     * @return 定位结果对象
     */
    public static LocateResult createFailure(String errorMessage) {
        LocateResult result = new LocateResult(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    @Override
    public String toString() {
        if (!success) {
            return "LocateResult{success=false, errorMessage='" + errorMessage + "'}";
        }
        
        return "LocateResult{" +
                "success=" + success +
                ", confidence=" + confidence +
                ", elementInfo='" + elementInfo + "'" +
                ", boundingBox=" + boundingBox +
                "}";
    }
}