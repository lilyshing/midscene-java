package com.midscene.shared.extractor.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 树结构处理工具类
 */
public class TreeUtils {
    private static final int NODE_SIZE_THRESHOLD = 4;
    
    /**
     * 截断文本
     */
    public static String truncateText(Object text, int maxLength) {
        if (text == null) {
            return "";
        }
        
        String textStr;
        if (text instanceof String) {
            textStr = (String) text;
        } else if (text instanceof Number) {
            return text.toString();
        } else {
            textStr = text.toString();
        }
        
        if (textStr.length() > maxLength) {
            return textStr.substring(0, maxLength) + "...";
        }
        
        return textStr.trim();
    }
    
    public static String truncateText(Object text) {
        return truncateText(text, 150);
    }
    
    /**
     * 裁剪属性，移除不需要的属性并截断文本值
     */
    public static Map<String, String> trimAttributes(Map<String, Object> attributes, Integer truncateTextLength) {
        Map<String, String> tailorAttributes = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            // 跳过不需要的属性
            if ("style".equals(key) || "htmlTagName".equals(key) || "nodeType".equals(key)) {
                continue;
            }
            
            Object value = entry.getValue();
            tailorAttributes.put(key, truncateText(value, truncateTextLength != null ? truncateTextLength : 150));
        }
        
        return tailorAttributes;
    }
    
    /**
     * 生成树的描述文本
     */
    public static <T extends BaseElement> String descriptionOfTree(ElementTreeNode<T> tree, 
                                                                 Integer truncateTextLength, 
                                                                 boolean filterNonTextContent, 
                                                                 boolean visibleOnly) {
        String result = buildContentTree(tree, 0, truncateTextLength, filterNonTextContent, visibleOnly);
        // 移除开头的空行
        return result.replaceAll("^\\s*\\n", "");
    }
    
    public static <T extends BaseElement> String descriptionOfTree(ElementTreeNode<T> tree) {
        return descriptionOfTree(tree, null, false, true);
    }
    
    private static <T extends BaseElement> String buildContentTree(ElementTreeNode<T> node, 
                                                                  int indent, 
                                                                  Integer truncateTextLength, 
                                                                  boolean filterNonTextContent, 
                                                                  boolean visibleOnly) {
        StringBuilder result = new StringBuilder();
        String indentStr = "  ".repeat(indent);
        
        // 处理子节点
        StringBuilder childrenStr = new StringBuilder();
        for (ElementTreeNode<T> child : node.getChildren()) {
            String childContent = buildContentTree(child, indent + 1, 
                                                  truncateTextLength, filterNonTextContent, visibleOnly);
            if (!childContent.isEmpty()) {
                childrenStr.append("\n").append(childContent);
            }
        }
        
        T element = node.getNode();
        if (element != null && 
            element.getRect() != null && 
            element.getRect().getWidth() > NODE_SIZE_THRESHOLD && 
            element.getRect().getHeight() > NODE_SIZE_THRESHOLD &&
            (!filterNonTextContent || (filterNonTextContent && element.getContent() != null && !element.getContent().isEmpty())) &&
            (!visibleOnly || (visibleOnly && element.isVisible()))) {
            
            // 构建节点类型字符串
            String nodeTypeString;
            Map<String, Object> attributes = element.getAttributes();
            if (attributes != null && attributes.containsKey("htmlTagName")) {
                nodeTypeString = attributes.get("htmlTagName").toString().replaceAll("[<>]", "");
            } else if (attributes != null && attributes.containsKey("nodeType")) {
                nodeTypeString = attributes.get("nodeType").toString().replaceAll("\\sNode$", "").toLowerCase();
            } else {
                nodeTypeString = "element";
            }
            
            // 构建属性字符串
            StringBuilder attributesBuilder = new StringBuilder();
            attributesBuilder.append("id=\"").append(element.getId()).append("\"");
            
            // 添加markerId
            if (element.getIndexId() != null) {
                attributesBuilder.append(" markerId=\"").append(element.getIndexId()).append("\"");
            }
            
            // 添加裁剪后的属性
            Map<String, String> trimmedAttrs = trimAttributes(attributes, truncateTextLength);
            for (Map.Entry<String, String> entry : trimmedAttrs.entrySet()) {
                attributesBuilder.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
            }
            
            // 添加矩形属性
            Rectangle rect = element.getRect();
            if (rect != null) {
                attributesBuilder.append(" left=\"").append(rect.getLeft()).append("\"");
                attributesBuilder.append(" top=\"").append(rect.getTop()).append("\"");
                attributesBuilder.append(" width=\"").append(rect.getWidth()).append("\"");
                attributesBuilder.append(" height=\"").append(rect.getHeight()).append("\"");
            }
            
            // 构建节点内容
            result.append(indentStr).append("<").append(nodeTypeString).append(" ").append(attributesBuilder).append(">");
            
            // 添加节点内容文本
            String content = element.getContent();
            if (content != null && !content.isEmpty()) {
                result.append("\n").append(indentStr).append("  ")
                      .append(truncateText(content, truncateTextLength));
            }
            
            // 添加子节点
            if (childrenStr.length() > 0) {
                result.append(childrenStr);
            }
            
            // 闭合标签
            result.append("\n").append(indentStr).append("</").append(nodeTypeString).append(">");
        } else if (!filterNonTextContent && childrenStr.length() > 0) {
            // 处理只有子节点的情况
            if (!childrenStr.toString().trim().startsWith("<>") && !childrenStr.toString().trim().isEmpty()) {
                result.append(indentStr).append("<>");
                result.append(childrenStr);
                result.append("\n").append(indentStr).append("</>");
            }
        }
        
        return result.toString();
    }
    
    /**
     * 将树转换为元素列表
     */
    public static <T extends BaseElement> List<T> treeToList(ElementTreeNode<T> tree) {
        List<T> result = new ArrayList<>();
        dfsToList(tree, result);
        return result;
    }
    
    private static <T extends BaseElement> void dfsToList(ElementTreeNode<T> node, List<T> result) {
        T element = node.getNode();
        if (element != null) {
            result.add(element);
        }
        for (ElementTreeNode<T> child : node.getChildren()) {
            dfsToList(child, result);
        }
    }
    
    /**
     * 遍历树，对每个节点应用转换函数
     */
    public static <T extends BaseElement, R extends BaseElement> ElementTreeNode<R> traverseTree(
            ElementTreeNode<T> tree, Function<T, R> onNode) {
        ElementTreeNode<R> result = new ElementTreeNode<>();
        dfsTraverse(tree, result, onNode);
        return result;
    }
    
    private static <T extends BaseElement, R extends BaseElement> void dfsTraverse(
            ElementTreeNode<T> sourceNode, ElementTreeNode<R> targetNode, Function<T, R> onNode) {
        T sourceElement = sourceNode.getNode();
        if (sourceElement != null) {
            targetNode.setNode(onNode.apply(sourceElement));
        }
        
        for (ElementTreeNode<T> sourceChild : sourceNode.getChildren()) {
            ElementTreeNode<R> targetChild = new ElementTreeNode<>();
            dfsTraverse(sourceChild, targetChild, onNode);
            targetNode.addChild(targetChild);
        }
    }
    
    /**
     * 基础元素接口，定义元素树所需的核心属性
     */
    public interface BaseElement {
        String getId();
        String getContent();
        Rectangle getRect();
        Map<String, Object> getAttributes();
        String getIndexId();
        boolean isVisible();
    }
    
    /**
     * 矩形边界接口
     */
    public interface Rectangle {
        int getLeft();
        int getTop();
        int getWidth();
        int getHeight();
    }
    
    private TreeUtils() {
        // 防止实例化
    }
}