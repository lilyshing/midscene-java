package com.midscene.shared.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 通用工具类，提供项目中常用的工具方法
 */
public class Utils {
    private static final Map<String, String> hashMap = new HashMap<>();
    private static boolean isMcp = false;
    
    /**
     * 生成UUID字符串
     * @return UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成哈希ID
     * @param rect 矩形边界信息
     * @param content 内容字符串
     * @return 生成的哈希ID
     */
    public static String generateHashId(Object rect, String content) {
        String combined = "{\"content\":\"" + content + ",\"rect\":" + rect + "}";
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String hashHex = hexString.toString();
            String hashLetters = hexToLetters(hashHex);
            
            int sliceLength = 5;
            String slicedHash = "";
            
            while (sliceLength < hashLetters.length() - 1) {
                slicedHash = hashLetters.substring(0, sliceLength);
                if (hashMap.containsKey(slicedHash) && !Objects.equals(hashMap.get(slicedHash), combined)) {
                    sliceLength++;
                    continue;
                }
                hashMap.put(slicedHash, combined);
                break;
            }
            
            return slicedHash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
    
    /**
     * 将十六进制字符串转换为字母字符串
     */
    private static String hexToLetters(String hex) {
        StringBuilder result = new StringBuilder();
        for (char c : hex.toCharArray()) {
            int code = Integer.parseInt(String.valueOf(c), 16);
            result.append((char) (97 + (code % 26))); // 97是ASCII中'a'的码值
        }
        return result.toString();
    }
    
    /**
     * 断言函数，如果条件为false则抛出异常
     */
    public static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message != null ? message : "Assertion failed");
        }
    }
    
    /**
     * 设置是否为MCP环境
     */
    public static void setIsMcp(boolean value) {
        isMcp = value;
    }
    
    /**
     * 日志消息输出
     */
    public static void logMsg(Object... messages) {
        if (!isMcp) {
            StringBuilder sb = new StringBuilder();
            for (Object message : messages) {
                sb.append(message).append(" ");
            }
            System.out.println(sb.toString());
        }
    }
    
    /**
     * 转义HTML脚本标签
     */
    public static String escapeScriptTag(String html) {
        return html
            .replace("<", "__midscene_lt__")
            .replace(">", "__midscene_gt__");
    }
    
    /**
     * 反转义HTML脚本标签
     */
    public static String antiEscapeScriptTag(String html) {
        return html
            .replace("__midscene_lt__", "<")
            .replace("__midscene_gt__", ">");
    }
    
    /**
     * 替换非法的路径字符和空格
     */
    public static String replaceIllegalPathCharsAndSpace(String str) {
        // 首先将空格替换为单个连字符（每个空格对应一个连字符）
        String result = str.replace(' ', '-');
        // 然后将其他非法字符替换为连字符，多个连续的非法字符替换为一个连字符
        return result.replaceAll("[:*?\"<>|]+", "-");
    }
    
    private Utils() {
        // 防止实例化
    }
}