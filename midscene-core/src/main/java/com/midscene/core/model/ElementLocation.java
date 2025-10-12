package com.midscene.core.model;

/**
 * 元素定位结果类，表示元素定位的结果信息
 */
public class ElementLocation {
    private String elementId;
    private String description;
    private Point coordinates;
    private Rect boundingBox;
    private double confidence;
    private String locatorType;
    
    public ElementLocation() {
    }
    
    public ElementLocation(String elementId, Point coordinates) {
        this.elementId = elementId;
        this.coordinates = coordinates;
        this.confidence = 1.0;
    }
    
    public ElementLocation(String elementId, Point coordinates, double confidence) {
        this.elementId = elementId;
        this.coordinates = coordinates;
        this.confidence = confidence;
    }
    
    public String getElementId() {
        return elementId;
    }
    
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Point getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }
    
    public Rect getBoundingBox() {
        return boundingBox;
    }
    
    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public String getLocatorType() {
        return locatorType;
    }
    
    public void setLocatorType(String locatorType) {
        this.locatorType = locatorType;
    }
    
    /**
     * 获取矩形的右边界坐标
     */
    public double getRight() {
        return boundingBox != null ? boundingBox.getRight() : 0;
    }
    
    /**
     * 获取矩形的下边界坐标
     */
    public double getBottom() {
        return boundingBox != null ? boundingBox.getBottom() : 0;
    }
    
    /**
     * 检查定位是否成功
     * @return 如果坐标不为空且置信度大于0.5，则认为定位成功
     */
    public boolean isSuccessful() {
        return coordinates != null && confidence > 0.5;
    }
    
    @Override
    public String toString() {
        return "ElementLocation{" +
               "elementId='" + elementId + '\'' +
               ", description='" + description + '\'' +
               ", coordinates=" + coordinates +
               ", confidence=" + confidence +
               ", locatorType='" + locatorType + '\'' +
               '}';
    }
}