package com.midscene.core.model;

import java.util.Objects;

/**
 * 二维点表示
 */
public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{x=" + x + ", y=" + y + '}';
    }

    /**
     * 计算到另一个点的欧几里得距离
     * @param other 另一个点
     * @return 距离值
     */
    public double distanceTo(Point other) {
        if (other == null) {
            throw new IllegalArgumentException("Other point cannot be null");
        }
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 计算到另一个点的曼哈顿距离
     * @param other 另一个点
     * @return 曼哈顿距离值
     */
    public double manhattanDistanceTo(Point other) {
        if (other == null) {
            throw new IllegalArgumentException("Other point cannot be null");
        }
        
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * 创建点的深拷贝
     * @return 新的点对象
     */
    public Point deepCopy() {
        return new Point(this.x, this.y);
    }
    
    /**
     * 移动点到新位置
     * @param dx x方向的偏移量
     * @param dy y方向的偏移量
     * @return 移动后的新点
     */
    public Point translate(double dx, double dy) {
        return new Point(this.x + dx, this.y + dy);
    }
    
    /**
     * 检查点是否在指定的矩形内
     * @param rect 检查矩形
     * @return 是否在矩形内
     */
    public boolean isInRect(Rect rect) {
        if (rect == null) {
            return false;
        }
        
        return this.x >= rect.getLeft() && 
               this.x <= rect.getRight() && 
               this.y >= rect.getTop() && 
               this.y <= rect.getBottom();
    }
    
    /**
     * 计算两点之间的中点
     * @param other 另一个点
     * @return 中点坐标
     */
    public Point midpoint(Point other) {
        if (other == null) {
            throw new IllegalArgumentException("Other point cannot be null");
        }
        
        return new Point(
            (this.x + other.x) / 2, 
            (this.y + other.y) / 2
        );
    }
}