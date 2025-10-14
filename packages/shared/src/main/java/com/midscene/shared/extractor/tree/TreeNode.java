package com.midscene.shared.extractor.tree;

import java.util.List;

/**
 * 树节点接口，定义通用的树结构操作
 * @param <T> 节点值的类型
 */
public interface TreeNode<T> {
    /**
     * 获取节点值
     */
    T getNode();
    
    /**
     * 设置节点值
     */
    void setNode(T node);
    
    /**
     * 获取子节点列表
     */
    List<? extends TreeNode<T>> getChildren();
    
    /**
     * 添加子节点
     */
    void addChild(TreeNode<T> child);
}