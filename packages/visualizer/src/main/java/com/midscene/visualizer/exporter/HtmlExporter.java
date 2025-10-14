package com.midscene.visualizer.exporter;

import com.midscene.visualizer.model.DebugSession;
import com.midscene.visualizer.renderer.HtmlRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HTML导出器
 * 将调试会话导出为HTML文件
 */
public class HtmlExporter implements Exporter {
    private HtmlRenderer renderer;
    
    /**
     * 构造函数
     */
    public HtmlExporter() {
        this.renderer = new HtmlRenderer();
    }
    
    @Override
    public void export(DebugSession session, String outputPath) throws IOException {
        // 确保输出目录存在
        ensureDirectoryExists(outputPath);
        
        // 使用HTML渲染器生成HTML内容
        String htmlContent = renderer.render(session);
        
        // 写入文件
        Path outputFilePath = Paths.get(outputPath);
        Files.write(outputFilePath, htmlContent.getBytes("UTF-8"));
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
        return "html";
    }
    
    @Override
    public boolean supports(String format) {
        return "html".equalsIgnoreCase(format);
    }
}