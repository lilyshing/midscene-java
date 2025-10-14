package com.midscene.shared.platform;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素定位信息类，包含定位元素所需的各种属性
 */
public class ElementLocator {
    private final LocatorType type;
    private final String value;
    private final Map<String, Object> attributes;
    
    public ElementLocator(LocatorType type, String value) {
        this.type = type;
        this.value = value;
        this.attributes = new HashMap<>();
    }
    
    public ElementLocator withAttribute(String name, Object value) {
        this.attributes.put(name, value);
        return this;
    }
    
    public LocatorType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    /**
     * 定位器类型枚举
     */
    public enum LocatorType {
        ID,
        XPATH,
        CSS_SELECTOR,
        CLASS_NAME,
        TAG_NAME,
        LINK_TEXT,
        PARTIAL_LINK_TEXT,
        ACCESSIBILITY_ID,
        CONTENT_DESC,
        COORDINATES
    }
    
    @Override
    public String toString() {
        return "ElementLocator{type=" + type + ", value='" + value + "', attributes=" + attributes + "}";
    }
}