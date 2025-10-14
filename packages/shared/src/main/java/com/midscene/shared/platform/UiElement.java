package com.midscene.shared.platform;

import java.util.Map;

/**
 * UI元素类，表示界面上的一个可交互或可识别的元素
 */
public class UiElement {
    private final String id;
    private final String type;
    private final String text;
    private final String accessibilityId;
    private final Rectangle bounds;
    private final Map<String, Object> attributes;
    private final boolean clickable;
    private final boolean editable;
    private final boolean visible;
    
    public UiElement(String id, String type, String text, String accessibilityId, 
                    Rectangle bounds, Map<String, Object> attributes, 
                    boolean clickable, boolean editable, boolean visible) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.accessibilityId = accessibilityId;
        this.bounds = bounds;
        this.attributes = attributes;
        this.clickable = clickable;
        this.editable = editable;
        this.visible = visible;
    }
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public String getText() {
        return text;
    }
    
    public String getAccessibilityId() {
        return accessibilityId;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public boolean isClickable() {
        return clickable;
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public boolean isVisible() {
        return visible;
    }
}