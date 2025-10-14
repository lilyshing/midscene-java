package com.midscene.ios;

import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * iOS UI元素类
 * 扩展基础的UiElement类，提供iOS平台特有的元素属性和行为
 */
public class IOSUiElement extends UiElement {
    private String elementId; // WDA元素ID
    private String label;
    private String value;
    private String name;
    private String placeholderValue;
    private boolean accessible;
    private boolean selected;
    private boolean focused;
    private boolean secureTextEntry;
    private boolean keyboardFocused;
    private boolean hittable;
    private Map<String, String> attributes;
    private String bundleId;
    private String webView;
    private int elementIndex;
    
    /**
     * 构造函数
     */
    public IOSUiElement(String id, String type, String text, String accessibilityId, 
                      Rectangle bounds, Map<String, Object> attributes, 
                      boolean clickable, boolean editable) {
        super(id, type, text, accessibilityId, bounds, attributes, clickable, editable, true);
        this.attributes = new HashMap<>();
        // 初始化其他iOS特有的属性
        this.elementId = null;
        this.label = text;
        this.value = null;
        this.name = null;
        this.placeholderValue = null;
        this.accessible = true;
        this.selected = false;
        this.focused = false;
        this.secureTextEntry = false;
        this.keyboardFocused = false;
        this.hittable = clickable;
        this.bundleId = null;
        this.webView = null;
        this.elementIndex = 0;
    }
    
    /**
     * 无参构造函数，用于创建空的元素实例
     */
    public IOSUiElement() {
        super(null, null, null, null, null, new HashMap<>(), false, false, false);
        this.attributes = new HashMap<>();
    }
    
    /**
     * 获取WDA元素ID
     * @return WDA元素ID
     */
    public String getElementId() {
        return elementId;
    }
    
    /**
     * 设置WDA元素ID
     * @param elementId WDA元素ID
     */
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    /**
     * 获取元素标签（相当于Android的contentDescription）
     * @return 元素标签
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * 设置元素标签
     * @param label 元素标签
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * 获取元素值
     * @return 元素值
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 设置元素值
     * @param value 元素值
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * 获取元素名称
     * @return 元素名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置元素名称
     * @param name 元素名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取占位符值
     * @return 占位符值
     */
    public String getPlaceholderValue() {
        return placeholderValue;
    }
    
    /**
     * 设置占位符值
     * @param placeholderValue 占位符值
     */
    public void setPlaceholderValue(String placeholderValue) {
        this.placeholderValue = placeholderValue;
    }
    
    /**
     * 是否可访问
     * @return true如果元素可访问
     */
    public boolean isAccessible() {
        return accessible;
    }
    
    /**
     * 设置是否可访问
     * @param accessible 是否可访问
     */
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }
    
    /**
     * 是否已选中
     * @return true如果元素已选中
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * 设置是否已选中
     * @param selected 是否已选中
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * 是否获得焦点
     * @return true如果元素获得焦点
     */
    public boolean isFocused() {
        return focused || keyboardFocused;
    }
    
    /**
     * 设置是否获得焦点
     * @param focused 是否获得焦点
     */
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
    /**
     * 是否是安全文本输入（密码框）
     * @return true如果是安全文本输入
     */
    public boolean isSecureTextEntry() {
        return secureTextEntry;
    }
    
    /**
     * 设置是否是安全文本输入
     * @param secureTextEntry 是否是安全文本输入
     */
    public void setSecureTextEntry(boolean secureTextEntry) {
        this.secureTextEntry = secureTextEntry;
    }
    
    /**
     * 键盘是否聚焦
     * @return true如果键盘聚焦
     */
    public boolean isKeyboardFocused() {
        return keyboardFocused;
    }
    
    /**
     * 设置键盘是否聚焦
     * @param keyboardFocused 键盘是否聚焦
     */
    public void setKeyboardFocused(boolean keyboardFocused) {
        this.keyboardFocused = keyboardFocused;
    }
    
    /**
     * 是否可点击（hittable）
     * @return true如果元素可点击
     */
    public boolean isHittable() {
        return hittable;
    }
    
    /**
     * 设置是否可点击
     * @param hittable 是否可点击
     */
    public void setHittable(boolean hittable) {
        this.hittable = hittable;
    }
    
    /**
     * 获取应用Bundle ID
     * @return Bundle ID
     */
    public String getBundleId() {
        return bundleId;
    }
    
    /**
     * 设置应用Bundle ID
     * @param bundleId Bundle ID
     */
    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }
    
    /**
     * 获取WebView标识
     * @return WebView标识
     */
    public String getWebView() {
        return webView;
    }
    
    /**
     * 设置WebView标识
     * @param webView WebView标识
     */
    public void setWebView(String webView) {
        this.webView = webView;
    }
    
    /**
     * 获取元素索引
     * @return 元素索引
     */
    public int getElementIndex() {
        return elementIndex;
    }
    
    /**
     * 设置元素索引
     * @param elementIndex 元素索引
     */
    public void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }
    
    /**
     * 获取元素X坐标
     * @return X坐标
     */
    public int getX() {
        Rectangle bounds = super.getBounds();
        return bounds != null ? bounds.getX() : 0;
    }
    
    /**
     * 获取元素Y坐标
     * @return Y坐标
     */
    public int getY() {
        Rectangle bounds = super.getBounds();
        return bounds != null ? bounds.getY() : 0;
    }
    
    /**
     * 获取元素宽度
     * @return 宽度
     */
    public int getWidth() {
        Rectangle bounds = super.getBounds();
        return bounds != null ? bounds.getWidth() : 0;
    }
    
    /**
     * 获取元素高度
     * @return 高度
     */
    public int getHeight() {
        Rectangle bounds = super.getBounds();
        return bounds != null ? bounds.getHeight() : 0;
    }
    
    /**
     * 获取中心点X坐标
     * @return 中心点X坐标
     */
    public int getCenterX() {
        return getX() + getWidth() / 2;
    }
    
    /**
     * 获取中心点Y坐标
     * @return 中心点Y坐标
     */
    public int getCenterY() {
        return getY() + getHeight() / 2;
    }
    
    /**
     * 检查是否包含文本
     * @param text 要检查的文本
     * @return true如果元素包含指定文本
     */
    public boolean containsText(String text) {
        String elementText = super.getText();
        return elementText != null && elementText.contains(text);
    }
    
    /**
     * 生成元素的定位器
     * @return 元素的定位器Map
     */
    public Map<String, Object> generateLocator() {
        Map<String, Object> locator = new HashMap<>();
        
        if (elementId != null && !elementId.isEmpty()) {
            locator.put("elementId", elementId);
        } else if (label != null && !label.isEmpty()) {
            locator.put("label", label);
        } else if (name != null && !name.isEmpty()) {
            locator.put("name", name);
        } else if (super.getType() != null && !super.getType().isEmpty()) {
            locator.put("type", super.getType());
        }
        
        return locator;
    }
    
    /**
     * 检查元素是否为输入框
     * @return true如果元素是输入框
     */
    public boolean isInputField() {
        String type = super.getType();
        return type != null && 
               (type.equals("XCUIElementTypeTextField") || 
                type.equals("XCUIElementTypeTextView") ||
                type.equals("XCUIElementTypeSearchField") ||
                (type.equals("XCUIElementTypeSecureTextField") && isSecureTextEntry()));
    }
    
    /**
     * 检查元素是否为按钮
     * @return true如果元素是按钮
     */
    public boolean isButton() {
        String type = super.getType();
        return type != null && (type.equals("XCUIElementTypeButton") || super.isClickable());
    }
    
    /**
     * 检查元素是否为开关
     * @return true如果元素是开关
     */
    public boolean isSwitch() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeSwitch");
    }
    
    /**
     * 检查元素是否为滑块
     * @return true如果元素是滑块
     */
    public boolean isSlider() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeSlider");
    }
    
    /**
     * 检查元素是否为表视图
     * @return true如果元素是表视图
     */
    public boolean isTable() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeTable");
    }
    
    /**
     * 检查元素是否为集合视图
     * @return true如果元素是集合视图
     */
    public boolean isCollectionView() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeCollectionView");
    }
    
    /**
     * 检查元素是否为单元格
     * @return true如果元素是单元格
     */
    public boolean isCell() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeCell");
    }
    
    /**
     * 检查元素是否为导航栏
     * @return true如果元素是导航栏
     */
    public boolean isNavigationBar() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeNavigationBar");
    }
    
    /**
     * 检查元素是否为标签栏
     * @return true如果元素是标签栏
     */
    public boolean isTabBar() {
        String type = super.getType();
        return type != null && type.equals("XCUIElementTypeTabBar");
    }
    
    /**
     * 检查元素是否可见
     * @return true如果元素可见
     */
    public boolean isVisible() {
        // 简化实现，基于hittable属性判断可见性
        return hittable;
    }
    
    /**
     * 检查元素是否可用
     * @return true如果元素可用
     */
    public boolean isEnabled() {
        // 简化实现，默认返回true
        return true;
    }
    
    /**
     * 获取元素属性
     * @param attributeName 属性名
     * @return 属性值
     */
    public String getAttribute(String attributeName) {
        if (attributes != null) {
            return attributes.get(attributeName);
        }
        // 根据不同的属性名返回相应的值
        switch (attributeName) {
            case "label":
                return getLabel();
            case "value":
                return getValue();
            case "name":
                return getName();
            case "placeholder":
                return getPlaceholderValue();
            case "type":
                return super.getType();
            case "text":
                return super.getText();
            case "elementId":
                return getElementId();
            default:
                return null;
        }
    }
}