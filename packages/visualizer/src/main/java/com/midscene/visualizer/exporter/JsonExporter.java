package com.midscene.visualizer.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.midscene.visualizer.model.DebugSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JSON导出器
 * 将调试会话导出为JSON文件
 */
public class JsonExporter implements Exporter {
    private ObjectMapper objectMapper;
    
    /**
     * 构造函数
     */
    public JsonExporter() {
        objectMapper = new ObjectMapper();
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 启用格式化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 允许序列化空值
        objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    }
    
    @Override
    public void export(DebugSession session, String outputPath) throws IOException {
        // 确保输出目录存在
        ensureDirectoryExists(outputPath);
        
        // 导出会话数据
        objectMapper.writeValue(new File(outputPath), session);
    }
    
    /**
     * 确保输出目录存在
     */
    private void ensureDirectoryExists(String outputPath) throws IOException {
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null && !Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
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