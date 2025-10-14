package com.midscene.visualizer.renderer;

import com.midscene.visualizer.model.DebugSession;

/**
 * 渲染器接口
 * 定义调试会话数据的渲染功能
 */
public interface Renderer {
    
    /**
     * 渲染调试会话
     * @param session 调试会话对象
     * @return 渲染后的结果字符串
     */
    String render(DebugSession session);
    
    /**
     * 获取渲染器支持的格式
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