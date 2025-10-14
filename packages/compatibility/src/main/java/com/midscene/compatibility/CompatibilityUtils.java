package com.midscene.compatibility;

import com.midscene.core.model.UiContext;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.Point;
import com.midscene.shared.platform.UiContext as NewUiContext;
import com.midscene.shared.platform.UiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容性工具类
 * 提供各种数据类型的转换和辅助功能
 */
public class CompatibilityUtils {
    
    /**
     * 将旧的UiContext转换为新的UiContext
     */
    public static NewUiContext convertToNewUiContext(UiContext oldContext) {
        if (oldContext == null) {
            return null;
        }
        
        // 提取截图数据（如果有）
        String screenshotBase64 = "";
        if (oldContext.getMetadata() != null && oldContext.getMetadata().containsKey("screenshot")) {
            screenshotBase64 = (String) oldContext.getMetadata().get("screenshot");
        }
        
        // 转换元素列表（需要根据实际情况实现）
        List<UiElement> elements = new ArrayList<>();
        
        // 提取平台特定数据
        String platformSpecificData = null;
        if (oldContext.getMetadata() != null && oldContext.getMetadata().containsKey("platformData")) {
            platformSpecificData = (String) oldContext.getMetadata().get("platformData");
        }
        
        return new NewUiContext(screenshotBase64, elements, platformSpecificData);
    }
    
    /**
     * 将新的UiContext转换为旧的UiContext
     */
    public static UiContext convertToOldUiContext(NewUiContext newContext) {
        if (newContext == null) {
            return null;
        }
        
        UiContext oldContext = new UiContext();
        
        // 设置截图数据
        if (newContext.getScreenshotBase64() != null && !newContext.getScreenshotBase64().isEmpty()) {
            oldContext.addMetadata("screenshot", newContext.getScreenshotBase64());
        }
        
        // 设置平台信息
        oldContext.addMetadata("platform", "unknown"); // 需要根据实际情况设置
        
        // 转换元素信息（需要根据实际情况实现）
        if (newContext.getElements() != null && !newContext.getElements().isEmpty()) {
            List<Map<String, Object>> oldElements = new ArrayList<>();
            for (UiElement element : newContext.getElements()) {
                Map<String, Object> oldElement = new HashMap<>();
                oldElement.put("id", element.getId());
                oldElement.put("text", element.getText());
                oldElement.put("className", element.getClassName());
                oldElement.put("bounds", element.getBounds());
                oldElements.add(oldElement);
            }
            oldContext.addMetadata("elements", oldElements);
        }
        
        // 设置平台特定数据
        if (newContext.getPlatformSpecificData() != null) {
            oldContext.addMetadata("platformData", newContext.getPlatformSpecificData());
        }
        
        return oldContext;
    }
    
    /**
     * 将坐标字符串解析为Point对象
     */
    public static Point parseCoordinates(String coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return new Point(0, 0);
        }
        
        try {
            String[] parts = coordinates.split(",");
            if (parts.length >= 2) {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                return new Point(x, y);
            }
        } catch (NumberFormatException e) {
            // 忽略异常，返回默认值
        }
        
        return new Point(0, 0);
    }
    
    /**
     * 创建坐标定位器
     */
    public static ElementLocator createCoordinateLocator(int x, int y) {
        return new ElementLocator(ElementLocator.LocatorType.COORDINATES, x + "," + y);
    }
    
    /**
     * 将旧平台的定位器字符串转换为新的ElementLocator
     */
    public static ElementLocator convertToElementLocator(String oldLocator) {
        if (oldLocator == null || oldLocator.isEmpty()) {
            return null;
        }
        
        // 根据旧定位器格式判断类型并转换
        if (oldLocator.startsWith("id=")) {
            String id = oldLocator.substring(3);
            return new ElementLocator(ElementLocator.LocatorType.ID, id);
        } else if (oldLocator.startsWith("xpath=")) {
            String xpath = oldLocator.substring(6);
            return new ElementLocator(ElementLocator.LocatorType.XPATH, xpath);
        } else if (oldLocator.startsWith("css=")) {
            String css = oldLocator.substring(4);
            return new ElementLocator(ElementLocator.LocatorType.CSS_SELECTOR, css);
        } else if (oldLocator.startsWith("accessibility_id=")) {
            String id = oldLocator.substring(15);
            return new ElementLocator(ElementLocator.LocatorType.ACCESSIBILITY_ID, id);
        } else if (oldLocator.contains(",")) {
            // 尝试作为坐标处理
            Point point = parseCoordinates(oldLocator);
            return createCoordinateLocator(point.getX(), point.getY());
        }
        
        // 默认作为ID处理
        return new ElementLocator(ElementLocator.LocatorType.ID, oldLocator);
    }
    
    /**
     * 将新的ElementLocator转换为旧平台的定位器字符串
     */
    public static String convertToOldLocator(ElementLocator elementLocator) {
        if (elementLocator == null) {
            return "";
        }
        
        switch (elementLocator.getType()) {
            case ID:
                return "id=" + elementLocator.getValue();
            case XPATH:
                return "xpath=" + elementLocator.getValue();
            case CSS_SELECTOR:
                return "css=" + elementLocator.getValue();
            case ACCESSIBILITY_ID:
                return "accessibility_id=" + elementLocator.getValue();
            case CONTENT_DESC:
                return "content_desc=" + elementLocator.getValue();
            case COORDINATES:
                return elementLocator.getValue();
            default:
                return elementLocator.getType().name().toLowerCase() + "=" + elementLocator.getValue();
        }
    }
    
    /**
     * 检查两个平台是否兼容
     */
    public static boolean isPlatformCompatible(String oldPlatformType, String newPlatformType) {
        // 实现平台兼容性检查逻辑
        return oldPlatformType.equalsIgnoreCase(newPlatformType);
    }
    
    /**
     * 获取兼容的平台名称
     */
    public static String getCompatiblePlatformName(String platformType) {
        if (platformType == null) {
            return "UNKNOWN";
        }
        
        String lowerCase = platformType.toLowerCase();
        if (lowerCase.contains("web") || lowerCase.contains("browser")) {
            return "WEB";
        } else if (lowerCase.contains("android")) {
            return "ANDROID";
        } else if (lowerCase.contains("ios") || lowerCase.contains("iphone") || lowerCase.contains("ipad")) {
            return "IOS";
        }
        
        return "UNKNOWN";
    }
}