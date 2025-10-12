package com.midscene.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UI树节点表示
 */
public class UINode {
    private String id;
    private String content;
    private Rect rect;
    private Point center;
    private NodeType nodeType;
    private Map<String, Object> attributes = new HashMap<>();
    private boolean visible = true;
    private List<UINode> children = new ArrayList<>();
    private UINode parent;
    private String accessibilityId;
    private String className;
    private String tagName;

    public UINode(String id, String content, Rect rect, NodeType nodeType) {
        this.id = id;
        this.content = content;
        this.rect = rect;
        this.center = rect.getCenter();
        this.nodeType = nodeType;
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

    public List<UINode> getChildren() {
        return children;
    }

    public void setChildren(List<UINode> children) {
        this.children.clear();
        if (children != null) {
            children.forEach(this::addChild);
        }
    }

    public void addChild(UINode child) {
        if (child != null) {
            this.children.add(child);
            child.parent = this;
        }
    }

    public UINode getParent() {
        return parent;
    }

    public void removeChild(UINode child) {
        if (child != null && this.children.remove(child)) {
            child.parent = null;
        }
    }

    public void clearChildren() {
        for (UINode child : children) {
            child.parent = null;
        }
        children.clear();
    }

    public String getAccessibilityId() {
        return accessibilityId;
    }

    public void setAccessibilityId(String accessibilityId) {
        this.accessibilityId = accessibilityId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * 根据ID查找节点（包括当前节点和所有子节点）
     * @param nodeId 节点ID
     * @return 找到的节点，未找到则返回null
     */
    public UINode findById(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        
        if (nodeId.equals(this.id)) {
            return this;
        }
        
        for (UINode child : children) {
            UINode found = child.findById(nodeId);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }

    /**
     * 根据内容查找节点（包括当前节点和所有子节点）
     * @param content 节点内容
     * @return 符合条件的节点列表
     */
    public List<UINode> findByContent(String content) {
        List<UINode> result = new ArrayList<>();
        findByContentInternal(content, result);
        return result;
    }

    private void findByContentInternal(String content, List<UINode> result) {
        if (content == null) {
            return;
        }
        
        if (content.equals(this.content)) {
            result.add(this);
        }
        
        for (UINode child : children) {
            child.findByContentInternal(content, result);
        }
    }

    /**
     * 根据节点类型查找节点（包括当前节点和所有子节点）
     * @param nodeType 节点类型
     * @return 符合条件的节点列表
     */
    public List<UINode> findByType(NodeType nodeType) {
        List<UINode> result = new ArrayList<>();
        findByTypeInternal(nodeType, result);
        return result;
    }

    private void findByTypeInternal(NodeType nodeType, List<UINode> result) {
        if (nodeType == null) {
            return;
        }
        
        if (nodeType.equals(this.nodeType)) {
            result.add(this);
        }
        
        for (UINode child : children) {
            child.findByTypeInternal(nodeType, result);
        }
    }

    /**
     * 查找所有可见节点
     * @return 可见节点列表
     */
    public List<UINode> findVisibleNodes() {
        List<UINode> result = new ArrayList<>();
        findVisibleNodesInternal(result);
        return result;
    }

    private void findVisibleNodesInternal(List<UINode> result) {
        if (this.visible) {
            result.add(this);
        }
        
        for (UINode child : children) {
            child.findVisibleNodesInternal(result);
        }
    }

    /**
     * 获取节点的深度（根节点深度为0）
     * @return 节点深度
     */
    public int getDepth() {
        int depth = 0;
        UINode current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * 检查节点是否包含某个属性
     * @param key 属性名
     * @return 是否包含该属性
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * 获取属性值
     * @param key 属性名
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 设置属性值
     * @param key 属性名
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
        }
    }

    /**
     * 获取节点的完整路径
     * @return 节点路径，格式如：root/container/child
     */
    public String getPath() {
        List<String> pathParts = new ArrayList<>();
        UINode current = this;
        while (current != null) {
            pathParts.add(0, current.id);
            current = current.parent;
        }
        return String.join("/", pathParts);
    }

    /**
     * 检查节点是否在某个区域内
     * @param rect 检查区域
     * @return 是否在区域内
     */
    public boolean isInRect(Rect rect) {
        if (rect == null || this.rect == null) {
            return false;
        }
        return this.rect.intersects(rect);
    }

    /**
     * 查找距离指定点最近的节点
     * @param point 指定点
     * @return 最近的节点（可能是自身或子节点）
     */
    public Optional<UINode> findNearestNode(Point point) {
        if (point == null) {
            return Optional.empty();
        }
        
        double minDistance = Double.MAX_VALUE;
        UINode nearestNode = null;
        
        // 计算当前节点到指定点的距离
        double currentDistance = this.center.distanceTo(point);
        if (currentDistance < minDistance && this.visible) {
            minDistance = currentDistance;
            nearestNode = this;
        }
        
        // 递归查找子节点
        for (UINode child : children) {
            Optional<UINode> childNearest = child.findNearestNode(point);
            if (childNearest.isPresent()) {
                double distance = childNearest.get().getCenter().distanceTo(point);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestNode = childNearest.get();
                }
            }
        }
        
        return Optional.ofNullable(nearestNode);
    }

    /**
     * 获取节点的文本内容（包括自身和所有子节点）
     * @return 合并后的文本内容
     */
    public String getFullText() {
        StringBuilder sb = new StringBuilder();
        if (this.content != null) {
            sb.append(this.content);
        }
        
        for (UINode child : children) {
            if (sb.length() > 0 && child.content != null) {
                sb.append(" ");
            }
            sb.append(child.getFullText());
        }
        
        return sb.toString();
    }

    /**
     * 深度复制节点（包括所有子节点）
     * @return 复制后的节点
     */
    public UINode deepCopy() {
        UINode copy = new UINode(this.id, this.content, this.rect.deepCopy(), this.nodeType);
        copy.visible = this.visible;
        copy.attributes.putAll(this.attributes);
        copy.accessibilityId = this.accessibilityId;
        copy.className = this.className;
        copy.tagName = this.tagName;
        
        for (UINode child : children) {
            UINode childCopy = child.deepCopy();
            copy.addChild(childCopy);
        }
        
        return copy;
    }

    @Override
    public String toString() {
        return "UINode{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", nodeType=" + nodeType +
                ", visible=" + visible +
                ", childCount=" + children.size() +
                ", depth=" + getDepth() +
                '}';
    }
}