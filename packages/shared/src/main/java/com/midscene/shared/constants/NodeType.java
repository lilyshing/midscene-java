package com.midscene.shared.constants;

/**
 * UI节点类型枚举
 */
public enum NodeType {
    CONTAINER("CONTAINER Node"),
    FORM_ITEM("FORM_ITEM Node"),
    BUTTON("BUTTON Node"),
    A("Anchor Node"),
    IMG("IMG Node"),
    TEXT("TEXT Node"),
    POSITION("POSITION Node");
    
    private final String description;
    
    NodeType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}