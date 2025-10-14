package com.midscene.visualizer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 调试会话类
 * 存储调试过程中的所有相关数据
 */
public class DebugSession {
    private String id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<byte[]> screenshots;
    private List<ElementInfo> elements;
    private List<TestExecution> testExecutions;
    private List<DebugData> debugData;
    private Map<String, Object> pageInfo;
    private String platform;
    private String deviceName;
    private String appPackage;
    private String appActivity;
    private Map<String, String> metadata;
    
    /**
     * 构造函数
     */
    public DebugSession(String id, String title) {
        this.id = id != null ? id : generateUniqueId();
        this.title = title;
        this.startTime = LocalDateTime.now();
        this.screenshots = new ArrayList<>();
        this.elements = new ArrayList<>();
        this.testExecutions = new ArrayList<>();
        this.debugData = new ArrayList<>();
        this.pageInfo = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * 生成唯一ID
     */
    private String generateUniqueId() {
        return "session-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 获取会话ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取会话标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置会话标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取开始时间
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * 获取结束时间
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * 设置结束时间
     */
    public void end() {
        this.endTime = LocalDateTime.now();
    }
    
    /**
     * 获取所有截图
     */
    public List<byte[]> getScreenshots() {
        return new ArrayList<>(screenshots);
    }
    
    /**
     * 添加截图
     */
    public void addScreenshot(byte[] screenshot) {
        this.screenshots.add(screenshot);
    }
    
    /**
     * 获取最后一张截图
     */
    public byte[] getLastScreenshot() {
        if (screenshots.isEmpty()) {
            return null;
        }
        return screenshots.get(screenshots.size() - 1);
    }
    
    /**
     * 获取元素列表
     */
    public List<ElementInfo> getElements() {
        return new ArrayList<>(elements);
    }
    
    /**
     * 添加元素信息
     */
    public void addElements(List<ElementInfo> elements) {
        this.elements.addAll(elements);
    }
    
    /**
     * 获取测试执行列表
     */
    public List<TestExecution> getTestExecutions() {
        return new ArrayList<>(testExecutions);
    }
    
    /**
     * 添加测试执行
     */
    public void addTestExecution(TestExecution execution) {
        this.testExecutions.add(execution);
    }
    
    /**
     * 获取调试数据列表
     */
    public List<DebugData> getDebugData() {
        return new ArrayList<>(debugData);
    }
    
    /**
     * 添加调试数据
     */
    public void addDebugData(DebugData data) {
        this.debugData.add(data);
    }
    
    /**
     * 获取页面信息
     */
    public Map<String, Object> getPageInfo() {
        return new HashMap<>(pageInfo);
    }
    
    /**
     * 设置页面信息
     */
    public void setPageInfo(Map<String, Object> pageInfo) {
        this.pageInfo = new HashMap<>(pageInfo);
    }
    
    /**
     * 获取平台信息
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * 设置平台信息
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /**
     * 获取设备名称
     */
    public String getDeviceName() {
        return deviceName;
    }
    
    /**
     * 设置设备名称
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    /**
     * 获取应用包名
     */
    public String getAppPackage() {
        return appPackage;
    }
    
    /**
     * 设置应用包名
     */
    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }
    
    /**
     * 获取应用活动
     */
    public String getAppActivity() {
        return appActivity;
    }
    
    /**
     * 设置应用活动
     */
    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }
    
    /**
     * 获取元数据
     */
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    /**
     * 设置元数据
     */
    public void setMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    /**
     * 获取会话持续时间（秒）
     */
    public long getDurationSeconds() {
        if (endTime == null) {
            return startTime.until(LocalDateTime.now(), java.time.temporal.ChronoUnit.SECONDS);
        }
        return startTime.until(endTime, java.time.temporal.ChronoUnit.SECONDS);
    }
    
    /**
     * 获取成功的测试执行数量
     */
    public long getSuccessfulExecutionsCount() {
        return testExecutions.stream()
                .filter(TestExecution::isSuccess)
                .count();
    }
    
    /**
     * 获取失败的测试执行数量
     */
    public long getFailedExecutionsCount() {
        return testExecutions.stream()
                .filter(execution -> !execution.isSuccess())
                .count();
    }
    
    /**
     * 检查会话是否已结束
     */
    public boolean isEnded() {
        return endTime != null;
    }
    
    @Override
    public String toString() {
        return "DebugSession{" +
                "id='