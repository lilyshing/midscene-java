package com.midscene.shared.platform;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Rectangle类的单元测试
 */
class RectangleTest {

    @Test
    void testConstructorAndGetters() {
        // 测试构造函数和getter方法
        Rectangle rect = new Rectangle(10, 20, 100, 50);
        
        assertEquals(10, rect.getX(), "X coordinate should be 10");
        assertEquals(20, rect.getY(), "Y coordinate should be 20");
        assertEquals(100, rect.getWidth(), "Width should be 100");
        assertEquals(50, rect.getHeight(), "Height should be 50");
        
        // 测试负数坐标但正尺寸
        Rectangle negativePositionRect = new Rectangle(-10, -20, 100, 50);
        assertEquals(-10, negativePositionRect.getX());
        assertEquals(-20, negativePositionRect.getY());
        assertEquals(100, negativePositionRect.getWidth());
        assertEquals(50, negativePositionRect.getHeight());
    }

    @Test
    void testGetCenter() {
        // 测试获取中心点方法
        // 偶数尺寸的中心点应该是精确的
        Rectangle rect1 = new Rectangle(10, 20, 100, 50);
        Point center1 = rect1.getCenter();
        assertEquals(60, center1.getX(), "Center X should be 10 + 100/2 = 60");
        assertEquals(45, center1.getY(), "Center Y should be 20 + 50/2 = 45");
        
        // 奇数尺寸的中心点应该向下取整（整数除法）
        Rectangle rect2 = new Rectangle(10, 20, 99, 49);
        Point center2 = rect2.getCenter();
        assertEquals(59, center2.getX(), "Center X should be 10 + 99/2 = 59 (integer division)");
        assertEquals(44, center2.getY(), "Center Y should be 20 + 49/2 = 44 (integer division)");
        
        // 验证返回的是新的Point对象
        assertNotNull(center1);
        assertNotSame(center1, center2);
    }

    @Test
    void testToString() {
        // 测试toString方法
        Rectangle rect = new Rectangle(10, 20, 100, 50);
        String expected = "Rectangle{x=10, y=20, width=100, height=50}";
        assertEquals(expected, rect.toString(), "toString should format the rectangle correctly");
        
        Rectangle negativeRect = new Rectangle(-5, -15, 200, 150);
        expected = "Rectangle{x=-5, y=-15, width=200, height=150}";
        assertEquals(expected, negativeRect.toString());
    }

    @Test
    void testZeroDimensions() {
        // 测试零尺寸的矩形
        Rectangle zeroRect = new Rectangle(0, 0, 0, 0);
        assertEquals(0, zeroRect.getX());
        assertEquals(0, zeroRect.getY());
        assertEquals(0, zeroRect.getWidth());
        assertEquals(0, zeroRect.getHeight());
        
        // 零尺寸矩形的中心点应该是(0, 0)
        Point center = zeroRect.getCenter();
        assertEquals(0, center.getX());
        assertEquals(0, center.getY());
    }

    @Test
    void testImmutability() {
        // Rectangle类是不可变的，因为所有属性都是final的
        // 这个测试主要是确认类的设计意图
        Rectangle rect = new Rectangle(10, 20, 100, 50);
        // 由于属性是私有的且没有setter，无法直接验证不可变性
        // 但我们可以确认没有修改方法
        assertEquals(10, rect.getX());
        assertEquals(20, rect.getY());
        assertEquals(100, rect.getWidth());
        assertEquals(50, rect.getHeight());
        
        // 重新获取值应该保持不变
        assertEquals(10, rect.getX());
        assertEquals(20, rect.getY());
        assertEquals(100, rect.getWidth());
        assertEquals(50, rect.getHeight());
    }
}