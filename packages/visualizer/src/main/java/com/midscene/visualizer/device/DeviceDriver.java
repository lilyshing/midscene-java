package com.midscene.visualizer.device;

import com.midscene.visualizer.model.DebugSession;
import com.midscene.visualizer.model.ElementInfo;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * 设备驱动接口
 * 定义与设备交互的基本功能
 */
public interface DeviceDriver {
    
    /**
     * 连接设备
     * @param deviceId 设备ID
     * @return 是否连接成功
     */
    boolean connect(String deviceId);
    
    /**
     * 断开连接
     */
    void disconnect();
    
    /**
     * 检查连接状态
     * @return 是否已连接
     */
    boolean isConnected();
    
    /**
     * 获取设备屏幕截图
     * @return 截图对象
     */
    BufferedImage captureScreenshot();
    
    /**
     * 获取设备信息
     * @return 设备信息映射
     */
    Map<String, Object> getDeviceInfo();
    
    /**
     * 获取平台信息
     * @return 平台信息映射
     */
    Map<String, Object> getPlatformInfo();
    
    /**
     * 获取页面信息
     * @return 页面信息映射
     */
    Map<String, Object> getPageInfo();
    
    /**
     * 获取所有UI元素
     * @return 元素信息列表
     */
    List<ElementInfo> getAllElements();
    
    /**
     * 通过ID查找元素
     * @param elementId 元素ID
     * @return 元素信息
     */
    ElementInfo findElementById(String elementId);
    
    /**
     * 通过坐标查找元素
     * @param x X坐标
     * @param y Y坐标
     * @return 元素信息
     */
    ElementInfo findElementByCoordinates(int x, int y);
    
    /**
     * 执行点击操作
     * @param elementId 元素ID
     * @return 是否执行成功
     */
    boolean performClick(String elementId);
    
    /**
     * 执行滑动操作
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 持续时间（毫秒）
     * @return 是否执行成功
     */
    boolean performSwipe(int startX, int startY, int endX, int endY, int duration);
    
    /**
     * 获取设备日志
     * @param count 日志数量
     * @return 日志列表
     */
    List<String> getDeviceLogs(int count);
    
    /**
     * 刷新调试会话数据
     * @param session 调试会话对象
     * @return 更新后的调试会话
     */
    DebugSession refreshDebugData(DebugSession session);
    
    /**
     * 获取设备状态
     * @return 设备状态映射
     */
    Map<String, Object> getDeviceState();
}