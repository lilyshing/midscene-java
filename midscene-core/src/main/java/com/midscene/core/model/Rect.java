package com.midscene.core.model;

import java.util.Objects;

/**
 * 矩形表示
 */
public class Rect {
    private double left;
    private double top;
    private double width;
    private double height;

    public Rect(double left, double top, double width, double height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * 获取矩形的右边界坐标
     */
    public double getRight() {
        return left + width;
    }

    /**
     * 获取矩形的下边界坐标
     */
    public double getBottom() {
        return top + height;
    }

    /**
     * 获取矩形的中心点坐标
     */
    public Point getCenter() {
        return new Point(left + width / 2, top + height / 2);
    }
    
    /**
     * 检查当前矩形是否与指定矩形相交
     * @param other 另一个矩形
     * @return 是否相交
     */
    public boolean intersects(Rect other) {
        if (other == null) {
            return false;
        }
        
        // 矩形不相交的情况：一个矩形完全在另一个矩形的左边、右边、上边或下边
        return !(getRight() <= other.getLeft() || 
                getLeft() >= other.getRight() || 
                getBottom() <= other.getTop() || 
                getTop() >= other.getBottom());
    }
    
    /**
     * 创建矩形的深拷贝
     * @return 新的矩形对象
     */
    public Rect deepCopy() {
        return new Rect(this.left, this.top, this.width, this.height);
    }
    
    /**
     * 检查点是否在矩形内
     * @param point 检查点
     * @return 是否在矩形内
     */
    public boolean contains(Point point) {
        if (point == null) {
            return false;
        }
        
        return point.getX() >= left && 
               point.getX() <= getRight() && 
               point.getY() >= top && 
               point.getY() <= getBottom();
    }
    
    /**
     * 计算两个矩形的并集
     * @param other 另一个矩形
     * @return 并集矩形
     */
    public Rect union(Rect other) {
        if (other == null) {
            return deepCopy();
        }
        
        double unionLeft = Math.min(this.left, other.left);
        double unionTop = Math.min(this.top, other.top);
        double unionRight = Math.max(this.getRight(), other.getRight());
        double unionBottom = Math.max(this.getBottom(), other.getBottom());
        
        return new Rect(
            unionLeft, 
            unionTop, 
            unionRight - unionLeft, 
            unionBottom - unionTop
        );
    }
    
    /**
     * 计算两个矩形的交集
     * @param other 另一个矩形
     * @return 交集矩形，如果不相交则返回null
     */
    public Rect intersection(Rect other) {
        if (other == null || !intersects(other)) {
            return null;
        }
        
        double intersectLeft = Math.max(this.left, other.left);
        double intersectTop = Math.max(this.top, other.top);
        double intersectRight = Math.min(this.getRight(), other.getRight());
        double intersectBottom = Math.min(this.getBottom(), other.getBottom());
        
        return new Rect(
            intersectLeft, 
            intersectTop, 
            intersectRight - intersectLeft, 
            intersectBottom - intersectTop
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rect rect = (Rect) o;
        return Double.compare(rect.left, left) == 0 &&
                Double.compare(rect.top, top) == 0 &&
                Double.compare(rect.width, width) == 0 &&
                Double.compare(rect.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, width, height);
    }

    @Override
    public String toString() {
        return "Rect{left=" + left + ", top=" + top + ", width=" + width + ", height=" + height + '}';
    }
}