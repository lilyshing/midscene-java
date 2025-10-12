package com.midscene.core.model;

/**
 * 滚动方向枚举，定义页面或视图的滚动方向
 */
public enum ScrollDirection {
    /**
     * 向上滚动
     */
    UP,
    
    /**
     * 向下滚动
     */
    DOWN,
    
    /**
     * 向左滚动
     */
    LEFT,
    
    /**
     * 向右滚动
     */
    RIGHT,
    
    /**
     * 滚动到顶部
     */
    TO_TOP,
    
    /**
     * 滚动到底部
     */
    TO_BOTTOM,
    
    /**
     * 滚动到指定元素
     */
    TO_ELEMENT,
    
    /**
     * 滚动到指定坐标
     */
    TO_COORDINATES
}