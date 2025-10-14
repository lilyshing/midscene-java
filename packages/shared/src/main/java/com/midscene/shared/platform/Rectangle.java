package com.midscene.shared.platform;

/**
 * 矩形类，用于表示元素的边界框
 */
public class Rectangle {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * 获取矩形中心点坐标
     */
    public Point getCenter() {
        return new Point(x + width / 2, y + height / 2);
    }
    
    @Override
    public String toString() {
        return "Rectangle{x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "}";
    }
}