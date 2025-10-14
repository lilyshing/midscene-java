package com.midscene.shared.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Utils类的单元测试
 */
class UtilsTest {

    @Test
    void testUuid() {
        // 测试UUID生成功能
        String uuid = Utils.uuid();
        assertNotNull(uuid);
        assertTrue(uuid.length() > 0);
        
        // 验证生成的是有效的UUID格式
        assertDoesNotThrow(() -> UUID.fromString(uuid));
        
        // 验证两次生成的UUID不同
        String uuid2 = Utils.uuid();
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testGenerateHashId() {
        // 测试哈希ID生成
        String rect = "{\"x\":10,\"y\":20,\"width\":100,\"height\":50}";
        String content = "test content";
        
        String hashId = Utils.generateHashId(rect, content);
        assertNotNull(hashId);
        assertTrue(hashId.length() > 0);
        
        // 验证相同输入产生相同输出
        String hashId2 = Utils.generateHashId(rect, content);
        assertEquals(hashId, hashId2);
        
        // 验证不同输入产生不同输出
        String differentHashId = Utils.generateHashId(rect, "different content");
        assertNotEquals(hashId, differentHashId);
    }

    @Test
    void testAssertCondition() {
        // 测试断言成功的情况
        assertDoesNotThrow(() -> Utils.assertCondition(true, "This should not throw"));
        
        // 测试断言失败的情况
        AssertionError exception = assertThrows(AssertionError.class, 
                () -> Utils.assertCondition(false, "Assertion failed test"));
        assertEquals("Assertion failed test", exception.getMessage());
        
        // 测试无消息的断言失败
        exception = assertThrows(AssertionError.class, 
                () -> Utils.assertCondition(false, null));
        assertEquals("Assertion failed", exception.getMessage());
    }

    @Test
    void testMcpModeAndLogMsg() {
        // 默认情况下不是MCP环境
        // 由于logMsg方法在非MCP模式下会输出到System.out，我们无法直接测试输出，但可以确保它不会抛出异常
        assertDoesNotThrow(() -> Utils.logMsg("Test log message"));
        
        // 设置为MCP环境
        Utils.setIsMcp(true);
        // 在MCP环境下，logMsg不应该输出任何内容，同样确保不抛出异常
        assertDoesNotThrow(() -> Utils.logMsg("This should not be logged in MCP mode"));
        
        // 测试多参数日志
        assertDoesNotThrow(() -> Utils.logMsg("Multiple", "arguments", 123, true));
        
        // 重置回默认状态
        Utils.setIsMcp(false);
    }

    @Test
    void testEscapeScriptTag() {
        String html = "<script>alert('test');</script>";
        String escaped = Utils.escapeScriptTag(html);
        assertEquals("__midscene_lt__script__midscene_gt__alert('test');__midscene_lt__/script__midscene_gt__", escaped);
        
        // 测试空字符串
        assertEquals("", Utils.escapeScriptTag(""));
        
        // 测试不包含标签的字符串
        assertEquals("plain text", Utils.escapeScriptTag("plain text"));
        
        // 测试混合内容
        String mixed = "Text <with> multiple > symbols";
        String mixedEscaped = Utils.escapeScriptTag(mixed);
        assertEquals("Text __midscene_lt__with__midscene_gt__ multiple __midscene_gt__ symbols", mixedEscaped);
    }

    @Test
    void testAntiEscapeScriptTag() {
        String escaped = "__midscene_lt__script__midscene_gt__alert('test');__midscene_lt__/script__midscene_gt__";
        String unescaped = Utils.antiEscapeScriptTag(escaped);
        assertEquals("<script>alert('test');</script>", unescaped);
        
        // 测试空字符串
        assertEquals("", Utils.antiEscapeScriptTag(""));
        
        // 测试不包含转义序列的字符串
        assertEquals("plain text", Utils.antiEscapeScriptTag("plain text"));
        
        // 测试混合内容
        String mixedEscaped = "Text __midscene_lt__with__midscene_gt__ multiple __midscene_gt__ symbols";
        String mixedUnescaped = Utils.antiEscapeScriptTag(mixedEscaped);
        assertEquals("Text <with> multiple > symbols", mixedUnescaped);
    }

    @Test
    void testReplaceIllegalPathCharsAndSpace() {
        // 测试包含非法字符的字符串
        String illegalPath = "file:name*with?illegal\"chars<>and|spaces";
        String safePath = Utils.replaceIllegalPathCharsAndSpace(illegalPath);
        assertEquals("file-name-with-illegal-chars-and-spaces", safePath);
        
        // 测试不包含非法字符的字符串
        String legalPath = "normal_filename";
        assertEquals(legalPath, Utils.replaceIllegalPathCharsAndSpace(legalPath));
        
        // 测试空字符串
        assertEquals("", Utils.replaceIllegalPathCharsAndSpace(""));
        
        // 测试只有空格的字符串
        assertEquals("---", Utils.replaceIllegalPathCharsAndSpace("   "));
    }

    @Test
    void testEscapeAndAntiEscapeConsistency() {
        // 测试转义后再反转义应该得到原始字符串
        String original = "<div class=\"test\">Content > with < multiple tags</div>";
        String escaped = Utils.escapeScriptTag(original);
        String unescaped = Utils.antiEscapeScriptTag(escaped);
        assertEquals(original, unescaped);
    }
}