package com.midscene.visualizer.exporter;

import com.midscene.visualizer.model.DebugSession;
import java.io.IOException;

/**
 * 导出器接口
 * 定义调试会话数据的导出功能
 */
public interface Exporter {
    
    /**
     * 导出调试会话
     * @param session 调试会话对象
     * @param outputPath 输出路径
     * @throws IOException 导出失败时抛出
     */
    void export(DebugSession session, String outputPath) throws IOException;
    
    /**
     * 获取导出器支持的格式
     * @return 格式名称
     */
    String getFormat();
    
    /**
     * 检查是否支持指定的格式
     * @param format 格式名称
     * @return 是否支持
     */
    boolean supports(String format);
}