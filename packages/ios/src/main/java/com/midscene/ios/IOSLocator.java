package com.midscene.ios;

import com.midscene.core.exception.PlatformException;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * iOS元素定位器类
 * 提供多种iOS元素定位策略
 */
public class IOSLocator {
    private static final Logger logger = Logger.getLogger(IOSLocator.class.getName());
    private iOSPlatform platform;
    
    /**
     * 构造函数
     * @param platform iOS平台实例
     */
    public IOSLocator(iOSPlatform platform) {
        this.platform = platform;
    }
    
    /**
     * 通过元素ID查找元素
     * @param elementId 元素ID
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementById(String elementId) throws PlatformException {
        logger.info("Finding element by ID: " + elementId);
        Map<String, Object> locator = new HashMap<>();
        locator.put("elementId", elementId);
        
        UiContext context = platform.getUiContext();
        return platform.findElement(context, locator);
    }
    
    /**
     * 通过标签查找元素
     * @param label 元素标签
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByLabel(String label) throws PlatformException {
        logger.info("Finding element by label: " + label);
        Map<String, Object> locator = new HashMap<>();
        locator.put("label", label);
        
        UiContext context = platform.getUiContext();
        return platform.findElement(context, locator);
    }
    
    /**
     * 通过标签包含文本查找元素
     * @param partialLabel 部分标签文本
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByPartialLabel(String partialLabel) throws PlatformException {
        logger.info("Finding element by partial label: " + partialLabel);
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.getLabel() != null && iosElement.getLabel().contains(partialLabel)) {
                    return iosElement;
                }
            }
        }
        return null;
    }
    
    /**
     * 通过名称查找元素
     * @param name 元素名称
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByName(String name) throws PlatformException {
        logger.info("Finding element by name: " + name);
        Map<String, Object> locator = new HashMap<>();
        locator.put("name", name);
        
        UiContext context = platform.getUiContext();
        return platform.findElement(context, locator);
    }
    
    /**
     * 通过类型查找元素
     * @param type 元素类型
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByType(String type) throws PlatformException {
        logger.info("Finding element by type: " + type);
        Map<String, Object> locator = new HashMap<>();
        locator.put("type", type);
        
        UiContext context = platform.getUiContext();
        return platform.findElement(context, locator);
    }
    
    /**
     * 通过文本查找元素
     * @param text 元素文本
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByText(String text) throws PlatformException {
        logger.info("Finding element by text: " + text);
        Map<String, Object> locator = new HashMap<>();
        locator.put("value", text);
        
        UiContext context = platform.getUiContext();
        return platform.findElement(context, locator);
    }
    
    /**
     * 通过部分文本查找元素
     * @param partialText 部分文本
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByPartialText(String partialText) throws PlatformException {
        logger.info("Finding element by partial text: " + partialText);
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element.getText() != null && element.getText().contains(partialText)) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * 通过占位符查找元素
     * @param placeholder 占位符文本
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByPlaceholder(String placeholder) throws PlatformException {
        logger.info("Finding element by placeholder: " + placeholder);
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (placeholder.equals(iosElement.getPlaceholderValue())) {
                    return iosElement;
                }
            }
        }
        return null;
    }
    
    /**
     * 通过正则表达式查找元素
     * @param pattern 正则表达式
     * @param attribute 属性名（label、name、value、type等）
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByRegex(String pattern, String attribute) throws PlatformException {
        logger.info("Finding element by regex pattern: " + pattern + " for attribute: " + attribute);
        Pattern regex = Pattern.compile(pattern);
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                String value = getAttributeValue(iosElement, attribute);
                
                if (value != null && regex.matcher(value).matches()) {
                    return iosElement;
                }
            }
        }
        return null;
    }
    
    /**
     * 通过坐标区域查找元素
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param width 宽度
     * @param height 高度
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByCoordinateRegion(int x, int y, int width, int height) throws PlatformException {
        logger.info("Finding element by coordinate region: (" + x + ", " + y + ") - (" + (x + width) + ", " + (y + height) + ")");
        
        UiContext context = platform.getUiContext();
        int endX = x + width;
        int endY = y + height;
        
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.getX() >= x && iosElement.getX() + iosElement.getWidth() <= endX &&
                    iosElement.getY() >= y && iosElement.getY() + iosElement.getHeight() <= endY) {
                    return element;
                }
            }
        }
        return null;
    }
    
    /**
     * 通过多个属性组合查找元素
     * @param attributes 属性键值对
     * @return 找到的元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findElementByMultipleAttributes(Map<String, String> attributes) throws PlatformException {
        logger.info("Finding element by multiple attributes: " + attributes);
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                boolean match = true;
                
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    String key = entry.getKey();
                    String expectedValue = entry.getValue();
                    String actualValue = getAttributeValue(iosElement, key);
                    
                    if (!expectedValue.equals(actualValue)) {
                        match = false;
                        break;
                    }
                }
                
                if (match) {
                    return iosElement;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找可点击的元素
     * @return 找到的第一个可点击元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findClickableElement() throws PlatformException {
        logger.info("Finding clickable element");
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element.isClickable() && element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.isVisible()) {
                    return element;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找可编辑的元素（输入框）
     * @return 找到的第一个可编辑元素
     * @throws PlatformException 如果查找失败
     */
    public UiElement findEditableElement() throws PlatformException {
        logger.info("Finding editable element");
        
        UiContext context = platform.getUiContext();
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.isInputField() && iosElement.isEnabled() && iosElement.isVisible()) {
                    return iosElement;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找可见元素
     * @return 所有可见元素列表
     * @throws PlatformException 如果查找失败
     */
    public List<UiElement> findVisibleElements() throws PlatformException {
        logger.info("Finding all visible elements");
        
        List<UiElement> visibleElements = new ArrayList<>();
        UiContext context = platform.getUiContext();
        
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.isVisible()) {
                    visibleElements.add(element);
                }
            }
        }
        
        return visibleElements;
    }
    
    /**
     * 通过类型查找所有元素
     * @param type 元素类型
     * @return 元素列表
     * @throws PlatformException 如果查找失败
     */
    public List<UiElement> findElementsByType(String type) throws PlatformException {
        logger.info("Finding all elements by type: " + type);
        
        List<UiElement> elements = new ArrayList<>();
        UiContext context = platform.getUiContext();
        
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (type.equals(iosElement.getType())) {
                    elements.add(iosElement);
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 查找所有按钮元素
     * @return 按钮元素列表
     * @throws PlatformException 如果查找失败
     */
    public List<UiElement> findAllButtons() throws PlatformException {
        return findElementsByType("XCUIElementTypeButton");
    }
    
    /**
     * 查找所有输入框元素
     * @return 输入框元素列表
     * @throws PlatformException 如果查找失败
     */
    public List<UiElement> findAllInputFields() throws PlatformException {
        logger.info("Finding all input fields");
        
        List<UiElement> inputFields = new ArrayList<>();
        UiContext context = platform.getUiContext();
        
        for (UiElement element : context.getElements()) {
            if (element instanceof IOSUiElement) {
                IOSUiElement iosElement = (IOSUiElement) element;
                if (iosElement.isInputField()) {
                    inputFields.add(iosElement);
                }
            }
        }
        
        return inputFields;
    }
    
    /**
     * 检查元素是否存在
     * @param locator 元素定位器
     * @return true如果元素存在
     * @throws PlatformException 如果检查失败
     */
    public boolean elementExists(Map<String, Object> locator) throws PlatformException {
        try {
            UiContext context = platform.getUiContext();
            UiElement element = platform.findElement(context, locator);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 等待元素出现
     * @param locator 元素定位器
     * @param timeout 超时时间（毫秒）
     * @return 找到的元素
     * @throws PlatformException 如果超时或查找失败
     */
    public UiElement waitForElement(Map<String, Object> locator, long timeout) throws PlatformException {
        logger.info("Waiting for element: " + locator + " with timeout: " + timeout + "ms");
        
        long startTime = System.currentTimeMillis();
        UiElement element = null;
        
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                UiContext context = platform.getUiContext();
                element = platform.findElement(context, locator);
                if (element != null && element instanceof IOSUiElement) {
                    IOSUiElement iosElement = (IOSUiElement) element;
                    if (iosElement.isVisible()) {
                        logger.info("Element found after " + (System.currentTimeMillis() - startTime) + "ms");
                        return element;
                    }
                }
            } catch (Exception e) {
                // 忽略临时错误，继续等待
                logger.fine("Temporary error while waiting for element: " + e.getMessage());
            }
            
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PlatformException("Element wait interrupted", e);
            }
        }
        
        throw new PlatformException("Timeout waiting for element: " + locator + " after " + timeout + "ms");
    }
    
    /**
     * 获取元素属性值
     * @param element iOS元素
     * @param attribute 属性名
     * @return 属性值
     */
    private String getAttributeValue(IOSUiElement element, String attribute) {
        switch (attribute.toLowerCase()) {
            case "label":
                return element.getLabel();
            case "name":
                return element.getName();
            case "value":
                return element.getValue();
            case "type":
                return element.getType();
            case "placeholder":
            case "placeholdervalue":
                return element.getPlaceholderValue();
            case "elementid":
                return element.getElementId();
            case "bundleid":
                return element.getBundleId();
            default:
                // 尝试从自定义属性中获取
                String customValue = element.getAttribute(attribute);
                return customValue != null ? customValue : null;
        }
    }
    
    /**
     * 生成唯一的元素ID
     * @param element iOS元素
     * @return 唯一ID
     */
    public String generateUniqueId(IOSUiElement element) {
        StringBuilder idBuilder = new StringBuilder("ios_");
        
        if (element.getElementId() != null) {
            return idBuilder.append(element.getElementId().hashCode()).toString();
        }
        
        if (element.getLabel() != null) {
            idBuilder.append("label_");
            idBuilder.append(element.getLabel().replaceAll("\\s+", "_"));
        }
        
        if (element.getType() != null) {
            if (idBuilder.length() > 4) { // 如果已经有内容，添加分隔符
                idBuilder.append("_");
            }
            idBuilder.append("type_");
            idBuilder.append(element.getType());
        }
        
        if (element.getX() >= 0 && element.getY() >= 0) {
            idBuilder.append("_");
            idBuilder.append(element.getX());
            idBuilder.append("_");
            idBuilder.append(element.getY());
        }
        
        return idBuilder.toString().replaceAll("[^a-zA-Z0-9_]", "_");
    }
}