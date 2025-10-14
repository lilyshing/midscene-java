package com.midscene.visualizer.renderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.midscene.visualizer.model.DebugSession;
import java.io.IOException;

/**
 * JSON渲染器
 * 将调试会话渲染为JSON格式
 */
public class JsonRenderer implements Renderer {
    private ObjectMapper objectMapper;
    
    /**
     * 构造函数
     */
    public JsonRenderer() {
        objectMapper = new ObjectMapper();
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 启用格式化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 允许序列化空值
        objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    }
    
    @Override
    public String render(DebugSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (IOException e) {
            throw new RuntimeException("Failed to render session to JSON", e);
        }
    }
    
    @Override
    public String getFormat() {
        return "json";
    }
    
    @Override
    public boolean supports(String format) {
        return "json".equalsIgnoreCase(format);
    }
}