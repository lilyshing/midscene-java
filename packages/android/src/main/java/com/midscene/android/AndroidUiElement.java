package com.midscene.android;

import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Android UI元素类
 * 扩展基础的UiElement类，提供Android平台特有的元素属性和行为
 */
public class AndroidUiElement extends UiElement {
    private String id;
    private String resourceId;
    private String className;
    private String text;
    private String contentDescription;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean enabled;
    private boolean clickable;
    private boolean longClickable;
    private boolean checkable;
    private boolean checked;
    private boolean scrollable;
    private boolean password;
    private boolean focused;
    private boolean selected;
    private boolean visible;
    private Map<String, Object> attributes;
    private String packageName;
    private int index;
    private int instance;
    
    /**
     * 构造函数
     */
    public AndroidUiElement(String id, String type, String text, String accessibilityId, 
                          Rectangle bounds, Map<String, Object> attributes, 
                          boolean clickable, boolean editable) {
        super(id, type, text, accessibilityId, bounds, attributes, clickable, editable, true);
        this.attributes = new HashMap<>();
        // 初始化其他Android特有的属性
        this.resourceId = null;
        this.className = type;
        this.contentDescription = accessibilityId;
        this.x = bounds != null ? bounds.getX() : 0;
        this.y = bounds != null ? bounds.getY() : 0;
        this.width = bounds != null ? bounds.getWidth() : 0;
        this.height = bounds != null ? bounds.getHeight() : 0;
        this.enabled = true;
        this.clickable = clickable;
        this.longClickable = false;
        this.checkable = false;
        this.checked = false;
        this.scrollable = false;
        this.password = false;
        this.focused = false;
        this.selected = false;
        this.visible = true;
        this.packageName = null;
        this.index = 0;
        this.instance = 0;
    }
    
    /**
     * 无参构造函数，用于创建空的元素实例
     */
    public AndroidUiElement() {
        super(null, null, null, null, null, new HashMap<>(), false, false, false);
        this.attributes = new HashMap<>();
    }
    
    /**
     * 获取资源ID
     * @return 资源ID
     */
    public String getResourceId() {
        return resourceId;
    }
    
    /**
     * 设置资源ID
     * @param resourceId 资源ID
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    /**
     * 是否可长按
     * @return true如果可长按
     */
    public boolean isLongClickable() {
        return longClickable;
    }
    
    /**
     * 设置是否可长按
     * @param longClickable 是否可长按
     */
    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }
    
    /**
     * 是否可选中
     * @return true如果可选中
     */
    public boolean isCheckable() {
        return checkable;
    }
    
    /**
     * 设置是否可选中
     * @param checkable 是否可选中
     */
    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }
    
    /**
     * 是否已选中
     * @return true如果已选中
     */
    public boolean isChecked() {
        return checked;
    }
    
    /**
     * 设置是否已选中
     * @param checked 是否已选中
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    /**
     * 是否可滚动
     * @return true如果可滚动
     */
    public boolean isScrollable() {
        return scrollable;
    }
    
    /**
     * 设置是否可滚动
     * @param scrollable 是否可滚动
     */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
    
    /**
     * 是否是密码输入框
     * @return true如果是密码输入框
     */
    public boolean isPassword() {
        return password;
    }
    
    /**
     * 设置是否是密码输入框
     * @param password 是否是密码输入框
     */
    public void setPassword(boolean password) {
        this.password = password;
    }
    
    /**
     * 是否获得焦点
     * @return true如果获得焦点
     */
    public boolean isFocused() {
        return focused;
    }
    
    /**
     * 设置是否获得焦点
     * @param focused 是否获得焦点
     */
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
    /**
     * 是否被选中
     * @return true如果被选中
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * 设置是否被选中
     * @param selected 是否被选中
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * 是否可见
     * @return true如果可见
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * 设置是否可见
     * @param visible 是否可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * 获取元素属性
     * @return 元素属性
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    /**
     * 设置元素属性
     * @param attributes 元素属性
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * 添加单个属性
     * @param key 属性键
     * @param value 属性值
     */
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    /**
     * 获取包名
     * @return 包名
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * 设置包名
     * @param packageName 包名
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * 获取索引
     * @return 索引
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * 设置索引
     * @param index 索引
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * 获取实例编号
     * @return 实例编号
     */
    public int getInstance() {
        return instance;
    }
    
    /**
     * 设置实例编号
     * @param instance 实例编号
     */
    public void setInstance(int instance) {
        this.instance = instance;
    }
    
    /**
     * 获取中心点X坐标
     * @return 中心点X坐标
     */
    public int getCenterX() {
        return x + width / 2;
    }
    
    /**
     * 获取中心点Y坐标
     * @return 中心点Y坐标
     */
    public int getCenterY() {
        return y + height / 2;
    }
    
    /**
     * 检查是否包含文本
     * @param text 要检查的文本
     * @return true如果元素包含指定文本
     */
    public boolean containsText(String text) {
        return this.text != null && this.text.contains(text);
    }
    
    /**
     * 生成元素的XPath
     * @return 元素的XPath表达式
     */
    public String generateXPath() {
        StringBuilder xpath = new StringBuilder("//*");
        
        if (resourceId != null && !resourceId.isEmpty()) {
            xpath.append("[@resource-id='").append(resourceId).append("']");
        } else if (text != null && !text.isEmpty()) {
            xpath.append("[@text='").append(escapeForXPath(text)).append("']");
        } else if (contentDescription != null && !contentDescription.isEmpty()) {
            xpath.append("[@content-desc='").append(escapeForXPath(contentDescription)).append("']");
        }
        
        if (className != null && !className.isEmpty()) {
            xpath.insert(2, className.substring(className.lastIndexOf('.') + 1));
        }
        
        return xpath.toString();
    }
    
    /**
     * 转义XPath特殊字符
     * @param text 要转义的文本
     * @return 转义后的文本
     */
    private String escapeForXPath(String text) {
        return text.replace("'", "'\\''");
    }
    
    /**
     * 检查元素是否为输入框
     * @return true如果元素是输入框
     */
    public boolean isInputField() {
        return className != null && 
               (className.endsWith("EditText") || 
                className.endsWith("TextView"));
    }
}