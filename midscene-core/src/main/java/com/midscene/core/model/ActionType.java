package com.midscene.core.model;

/**
 * 操作类型枚举，定义Agent支持的各种操作类型
 */
public enum ActionType {
    /**
     * 定位并交互 - 先定位元素，然后进行交互操作
     */
    LOCATE_AND_INTERACT,
    
    /**
     * 点击操作 - 直接在指定坐标点击
     */
    TAP,
    
    /**
     * 输入操作 - 输入文本内容
     */
    INPUT,
    
    /**
     * 滚动操作 - 滚动页面或视图
     */
    SCROLL,
    
    /**
     * 验证操作 - 验证某个条件是否满足
     */
    VERIFY,
    
    /**
     * 等待操作 - 等待某个条件满足
     */
    WAIT,
    
    /**
     * 导航操作 - 导航到指定URL或页面
     */
    NAVIGATE,
    
    /**
     * 截图操作 - 截取当前屏幕
     */
    SCREENSHOT,
    
    /**
     * 退出操作 - 退出应用或关闭浏览器
     */
    EXIT
}