package com.midscene.visualizer.exporter;

import java.util.HashMap;
import java.util.Map;

/**
 * 导出器工厂类
 * 用于创建和管理不同类型的导出器
 */
public class ExporterFactory {
    private Map<String, Exporter> exporters;
    
    /**
     * 构造函数
     */
    public ExporterFactory() {
        exporters = new HashMap<>();
        initializeExporters();
    }
    
    /**
     * 初始化导出器
     */
    private void initializeExporters() {
        registerExporter(new JsonExporter());
        registerExporter(new HtmlExporter());
        // 可以在这里注册更多的导出器
    }
    
    /**
     * 注册导出器
     */
    public void registerExporter(Exporter exporter) {
        if (exporter != null && exporter.getFormat() != null) {
            exporters.put(exporter.getFormat().toLowerCase(), exporter);
        }
    }
    
    /**
     * 获取导出器
     */
    public Exporter getExporter(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        
        String formatLower = format.toLowerCase();
        Exporter exporter = exporters.get(formatLower);
        
        if (exporter == null) {
            // 如果找不到指定格式的导出器，尝试找到支持该格式的导出器
            for (Exporter e : exporters.values()) {
                if (e.supports(formatLower)) {
                    exporter = e;
                    break;
                }
            }
        }
        
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        
        return exporter;
    }
    
    /**
     * 检查是否支持指定格式
     */
    public boolean supportsFormat(String format) {
        if (format == null) {
            return false;
        }
        
        String formatLower = format.toLowerCase();
        if (exporters.containsKey(formatLower)) {
            return true;
        }
        
        // 检查是否有导出器支持该格式
        for (Exporter exporter : exporters.values()) {
            if (exporter.supports(formatLower)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取支持的所有格式
     */
    public String[] getSupportedFormats() {
        return exporters.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取导出器数量
     */
    public int getExporterCount() {
        return exporters.size();
    }
    
    /**
     * 移除导出器
     */
    public void removeExporter(String format) {
        if (format != null) {
            exporters.remove(format.toLowerCase());
        }
    }
    
    /**
     * 清空所有导出器
     */
    public void clearExporters() {
        exporters.clear();
    }
}