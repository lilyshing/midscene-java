package com.midscene.shared.extractor.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 元素树节点实现类
 * @param <T> 节点值的类型
 */
public class ElementTreeNode<T> implements TreeNode<T> {
    private T node;
    private List<ElementTreeNode<T>> children;
    
    public ElementTreeNode() {
        this.children = new ArrayList<>();
    }
    
    public ElementTreeNode(T node) {
        this.node = node;
        this.children = new ArrayList<>();
    }
    
    @Override
    public T getNode() {
        return node;
    }
    
    @Override
    public void setNode(T node) {
        this.node = node;
    }
    
    @Override
    public List<ElementTreeNode<T>> getChildren() {
        return children;
    }
    
    @Override
    public void addChild(TreeNode<T> child) {
        if (child instanceof ElementTreeNode) {
            this.children.add((ElementTreeNode<T>) child);
        }
    }
    
    /**
     * 设置子节点列表
     */
    public void setChildren(List<ElementTreeNode<T>> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}