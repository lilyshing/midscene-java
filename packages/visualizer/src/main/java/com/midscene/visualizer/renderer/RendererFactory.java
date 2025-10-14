package com.midscene.visualizer.renderer;

import java.util.HashMap;
import java.util.Map;

/**
 * 渲染器工厂类
 * 用于创建和管理不同类型的渲染器
 */
public class RendererFactory {
    private Map<String, Renderer> renderers;
    
    /**
     * 构造函数
     */
    public RendererFactory() {
        renderers = new HashMap<>();
        initializeRenderers();
    }
    
    /**
     * 初始化渲染器
     */
    private void initializeRenderers() {
        registerRenderer(new JsonRenderer());
        registerRenderer(new HtmlRenderer());
        // 可以在这里注册更多的渲染器
    }
    
    /**
     * 注册渲染器
     */
    public void registerRenderer(Renderer renderer) {
        if (renderer != null && renderer.getFormat() != null) {
            renderers.put(renderer.getFormat().toLowerCase(), renderer);
        }
    }
    
    /**
     * 获取渲染器
     */
    public Renderer getRenderer(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        
        String formatLower = format.toLowerCase();
        Renderer renderer = renderers.get(formatLower);
        
        if (renderer == null) {
            // 如果找不到指定格式的渲染器，尝试找到支持该格式的渲染器
            for (Renderer r : renderers.values()) {
                if (r.supports(formatLower)) {
                    renderer = r;
                    break;
                }
            }
        }
        
        if (renderer == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        
        return renderer;
    }
    
    /**
     * 检查是否支持指定格式
     */
    public boolean supportsFormat(String format) {
        if (format == null) {
            return false;
        }
        
        String formatLower = format.toLowerCase();
        if (renderers.containsKey(formatLower)) {
            return true;
        }
        
        // 检查是否有渲染器支持该格式
        for (Renderer renderer : renderers.values()) {
            if (renderer.supports(formatLower)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取支持的所有格式
     */
    public String[] getSupportedFormats() {
        return renderers.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取渲染器数量
     */
    public int getRendererCount() {
        return renderers.size();
    }
    
    /**
     * 移除渲染器
     */
    public void removeRenderer(String format) {
        if (format != null) {
            renderers.remove(format.toLowerCase());
        }
    }
    
    /**
     * 清空所有渲染器
     */
    public void clearRenderers() {
        renderers.clear();
    }
}