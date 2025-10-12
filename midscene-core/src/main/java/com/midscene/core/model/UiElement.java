package com.midscene.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * UI元素抽象基类
 */
public abstract class UiElement {
    private String id;
    private String content;
    private Rect rect;
    private Point center;
    private NodeType nodeType = NodeType.OTHER;
    private Map<String, Object> attributes = new HashMap<>();
    private boolean visible = true;
    private List<String> xpaths;

    public UiElement(String id, String content, Rect rect) {
        this.id = id;
        this.content = content;
        this.rect = rect;
        this.center = rect.getCenter();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
        this.center = rect.getCenter();
    }

    public Point getCenter() {
        return center;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getXpaths() {
        return xpaths;
    }

    public void setXpaths(List<String> xpaths) {
        this.xpaths = xpaths;
    }

    /**
     * 点击/点击元素
     */
    public abstract CompletableFuture<Void> tap();

    /**
     * 向元素输入文本
     * @param text 要输入的文本
     */
    public abstract CompletableFuture<Void> inputText(String text);

    @Override
    public String toString() {
        return "UiElement{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", nodeType=" + nodeType +
                ", rect=" + rect +
                '}';
    }
}