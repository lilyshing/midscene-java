package com.midscene.core.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * UI上下文类 - 表示UI界面的完整上下文信息
 */
public class UiContext {
    private String screenshotBase64;
    private Size screenSize;
    private List<UINode> nodes;
    private Map<String, Object> metadata;
    private String platform;
    private String pageUrl;
    private String appPackage;
    private String appActivity;
    
    /**
     * 构造函数
     */
    public UiContext() {
        this.nodes = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 获取截图的Base64编码
     * @return 截图的Base64字符串
     */
    public String getScreenshotBase64() {
        return screenshotBase64;
    }
    
    /**
     * 设置截图的Base64编码
     * @param screenshotBase64 截图的Base64字符串
     */
    public void setScreenshotBase64(String screenshotBase64) {
        this.screenshotBase64 = screenshotBase64;
    }
    
    /**
     * 获取屏幕尺寸
     * @return 屏幕尺寸
     */
    public Size getScreenSize() {
        return screenSize;
    }
    
    /**
     * 设置屏幕尺寸
     * @param screenSize 屏幕尺寸
     */
    public void setScreenSize(Size screenSize) {
        this.screenSize = screenSize;
    }
    
    /**
     * 获取UI节点列表
     * @return UI节点列表
     */
    public List<UINode> getNodes() {
        return nodes;
    }
    
    /**
     * 设置UI节点列表
     * @param nodes UI节点列表
     */
    public void setNodes(List<UINode> nodes) {
        this.nodes = nodes;
    }
    
    /**
     * 添加UI节点
     * @param node UI节点
     */
    public void addNode(UINode node) {
        this.nodes.add(node);
    }
    
    /**
     * 获取元数据
     * @return 元数据映射
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * 设置元数据
     * @param metadata 元数据映射
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * 添加元数据项
     * @param key 键
     * @param value 值
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * 获取平台信息
     * @return 平台名称
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * 设置平台信息
     * @param platform 平台名称
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /**
     * 获取页面URL (Web平台)
     * @return 页面URL
     */
    public String getPageUrl() {
        return pageUrl;
    }
    
    /**
     * 设置页面URL (Web平台)
     * @param pageUrl 页面URL
     */
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    /**
     * 获取应用包名 (Android平台)
     * @return 应用包名
     */
    public String getAppPackage() {
        return appPackage;
    }
    
    /**
     * 设置应用包名 (Android平台)
     * @param appPackage 应用包名
     */
    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }
    
    /**
     * 获取应用活动 (Android平台)
     * @return 应用活动
     */
    public String getAppActivity() {
        return appActivity;
    }
    
    /**
     * 设置应用活动 (Android平台)
     * @param appActivity 应用活动
     */
    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }
    
    @Override
    public String toString() {
        return "UiContext{" +
               "screenshotBase64=" + (screenshotBase64 != null ? "<present>" : "null") +
               ", screenSize=" + screenSize +
               ", nodesCount=" + nodes.size() +
               ", platform=" + platform +
               ", metadataSize=" + metadata.size() +
               "}";
    }
}