package com.midscene.shared.platform;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 平台接口，定义所有支持平台（Web、Android、iOS）需要实现的核心方法
 * 确保跨平台操作的一致性
 */
public interface PlatformInterface {
    
    /**
     * 初始化平台连接
     * @param options 平台初始化选项
     * @return 初始化是否成功
     */
    CompletableFuture<Boolean> initialize(Map<String, Object> options);
    
    /**
     * 关闭平台连接
     */
    CompletableFuture<Void> close();
    
    /**
     * 获取当前UI上下文
     * @return UI上下文对象
     */
    CompletableFuture<UiContext> getCurrentUiContext();
    
    /**
     * 执行点击操作
     * @param elementLocator 元素定位信息
     * @return 操作结果
     */
    CompletableFuture<Boolean> tap(ElementLocator elementLocator);
    
    /**
     * 执行输入操作
     * @param elementLocator 元素定位信息
     * @param text 要输入的文本
     * @return 操作结果
     */
    CompletableFuture<Boolean> input(ElementLocator elementLocator, String text);
    
    /**
     * 执行滑动操作
     * @param start 起始坐标
     * @param end 结束坐标
     * @param duration 滑动持续时间（毫秒）
     * @return 操作结果
     */
    CompletableFuture<Boolean> swipe(Point start, Point end, int duration);
    
    /**
     * 截图操作
     * @return 截图数据（Base64编码）
     */
    CompletableFuture<String> screenshot();
    
    /**
     * 获取元素属性
     * @param elementLocator 元素定位信息
     * @param propertyName 属性名称
     * @return 属性值
     */
    CompletableFuture<String> getElementProperty(ElementLocator elementLocator, String propertyName);
    
    /**
     * 检查元素是否存在
     * @param elementLocator 元素定位信息
     * @return 是否存在
     */
    CompletableFuture<Boolean> elementExists(ElementLocator elementLocator);
    
    /**
     * 获取平台信息
     * @return 平台信息对象
     */
    PlatformInfo getPlatformInfo();
}