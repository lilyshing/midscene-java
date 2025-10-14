package com.midscene.shared.platform;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Point类的单元测试
 */
class PointTest {

    @Test
    void testConstructorAndGetters() {
        // 测试构造函数和getter方法
        Point point = new Point(10, 20);
        
        assertEquals(10, point.getX(), "X coordinate should be 10");
        assertEquals(20, point.getY(), "Y coordinate should be 20");
        
        // 测试负数坐标
        Point negativePoint = new Point(-5, -15);
        assertEquals(-5, negativePoint.getX());
        assertEquals(-15, negativePoint.getY());
        
        // 测试零坐标
        Point zeroPoint = new Point(0, 0);
        assertEquals(0, zeroPoint.getX());
        assertEquals(0, zeroPoint.getY());
    }

    @Test
    void testToString() {
        // 测试toString方法
        Point point = new Point(10, 20);
        String expected = "Point{x=10, y=20}";
        assertEquals(expected, point.toString(), "toString should format the point correctly");
        
        Point negativePoint = new Point(-5, -15);
        expected = "Point{x=-5, y=-15}";
        assertEquals(expected, negativePoint.toString());
    }

    @Test
    void testEquals() {
        // 测试相等性比较
        Point point1 = new Point(10, 20);
        Point point2 = new Point(10, 20);
        Point point3 = new Point(15, 20);
        Point point4 = new Point(10, 25);
        
        // 相同坐标的点应该相等
        assertEquals(point1, point2);
        assertTrue(point1.equals(point2));
        assertTrue(point2.equals(point1));
        
        // 不同坐标的点不应该相等
        assertNotEquals(point1, point3);
        assertNotEquals(point1, point4);
        assertFalse(point1.equals(point3));
        
        // 自反性测试
        assertEquals(point1, point1);
        assertTrue(point1.equals(point1));
        
        // null测试
        assertFalse(point1.equals(null));
        
        // 不同类型测试
        assertFalse(point1.equals("Point(10,20)"));
    }

    @Test
    void testHashCode() {
        // 测试哈希码计算
        Point point1 = new Point(10, 20);
        Point point2 = new Point(10, 20);
        
        // 相等的对象应该有相同的哈希码
        assertEquals(point1.hashCode(), point2.hashCode());
        
        // 验证哈希码的计算符合预期算法 (31 * x + y)
        int expectedHashCode = 31 * 10 + 20;
        assertEquals(expectedHashCode, point1.hashCode());
    }

    @Test
    void testInHashMap() {
        // 测试在HashMap中的行为
        Map<Point, String> pointMap = new HashMap<>();
        Point point1 = new Point(10, 20);
        Point point2 = new Point(10, 20);
        
        // 添加第一个点
        pointMap.put(point1, "Value 1");
        
        // 验证可以通过相等的点获取值
        assertEquals("Value 1", pointMap.get(point2), 
                "Should be able to retrieve value using an equal Point object");
        
        // 验证Map只包含一个条目
        assertEquals(1, pointMap.size());
    }

    @Test
    void testImmutability() {
        // Point类是不可变的，因为x和y是final的
        // 这个测试主要是确认类的设计意图
        Point point = new Point(10, 20);
        // 由于属性是私有的且没有setter，无法直接验证不可变性
        // 但我们可以确认没有修改方法
        assertEquals(10, point.getX());
        assertEquals(20, point.getY());
        // 重新获取值应该保持不变
        assertEquals(10, point.getX());
        assertEquals(20, point.getY());
    }
}