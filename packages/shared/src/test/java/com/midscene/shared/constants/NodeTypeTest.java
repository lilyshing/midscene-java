package com.midscene.shared.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * NodeType枚举的单元测试
 */
class NodeTypeTest {

    @Test
    void testEnumValues() {
        // 验证所有枚举值都存在
        NodeType[] values = NodeType.values();
        assertNotNull(values);
        assertEquals(7, values.length, "There should be 7 NodeType values");
        
        // 验证所有预期的枚举值都存在
        List<NodeType> expectedValues = Arrays.asList(
                NodeType.CONTAINER,
                NodeType.FORM_ITEM,
                NodeType.BUTTON,
                NodeType.A,
                NodeType.IMG,
                NodeType.TEXT,
                NodeType.POSITION
        );
        
        for (NodeType expected : expectedValues) {
            assertTrue(Arrays.asList(values).contains(expected), 
                    "Expected NodeType value " + expected + " not found");
        }
    }

    @Test
    void testGetDescription() {
        // 验证每个枚举值的描述方法返回正确的描述
        assertEquals("CONTAINER Node", NodeType.CONTAINER.getDescription());
        assertEquals("FORM_ITEM Node", NodeType.FORM_ITEM.getDescription());
        assertEquals("BUTTON Node", NodeType.BUTTON.getDescription());
        assertEquals("Anchor Node", NodeType.A.getDescription());
        assertEquals("IMG Node", NodeType.IMG.getDescription());
        assertEquals("TEXT Node", NodeType.TEXT.getDescription());
        assertEquals("POSITION Node", NodeType.POSITION.getDescription());
    }

    @Test
    void testToString() {
        // 验证toString方法返回与getDescription相同的值
        assertEquals(NodeType.CONTAINER.getDescription(), NodeType.CONTAINER.toString());
        assertEquals(NodeType.FORM_ITEM.getDescription(), NodeType.FORM_ITEM.toString());
        assertEquals(NodeType.BUTTON.getDescription(), NodeType.BUTTON.toString());
        assertEquals(NodeType.A.getDescription(), NodeType.A.toString());
        assertEquals(NodeType.IMG.getDescription(), NodeType.IMG.toString());
        assertEquals(NodeType.TEXT.getDescription(), NodeType.TEXT.toString());
        assertEquals(NodeType.POSITION.getDescription(), NodeType.POSITION.toString());
    }

    @Test
    void testEnumEquality() {
        // 验证枚举值的相等性比较
        NodeType container1 = NodeType.CONTAINER;
        NodeType container2 = NodeType.valueOf("CONTAINER");
        assertSame(container1, container2, "Same enum values should be identical objects");
        
        // 验证不同枚举值不相等
        assertNotSame(NodeType.CONTAINER, NodeType.BUTTON, "Different enum values should not be the same object");
    }
}