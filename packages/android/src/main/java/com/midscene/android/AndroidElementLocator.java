package com.midscene.android;

import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.Rectangle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Android元素定位器类
 * 提供多种策略来定位Android界面中的元素，采用缓存机制和并行处理优化性能
 */
public class AndroidElementLocator {
    
    // 定位结果缓存，使用ConcurrentHashMap确保线程安全
    private final Map<CacheKey, CacheValue> elementCache = new ConcurrentHashMap<>();
    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRY_MS = 2000; // 2秒缓存
    
    // 用于缓存查找结果的键类
    private static class CacheKey {
        private final String locatorType;
        private final String locatorValue;
        private final List<String> elementIds;
        
        public CacheKey(String locatorType, String locatorValue, List<UiElement> elements) {
            this.locatorType = locatorType;
            this.locatorValue = locatorValue;
            // 使用元素ID的列表作为缓存键的一部分，确保在界面变化时缓存失效
            this.elementIds = elements != null ? elements.stream()
                    .map(e -> String.valueOf(e.getAttributes().getOrDefault("resourceId", "")))
                    .collect(Collectors.toList()) : Collections.emptyList();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(locatorType, cacheKey.locatorType) &&
                   Objects.equals(locatorValue, cacheKey.locatorValue) &&
                   Objects.equals(elementIds, cacheKey.elementIds);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(locatorType, locatorValue, elementIds);
        }
    }
    
    // 缓存值类，包含结果和时间戳
    private static class CacheValue {
        private final UiElement element;
        private final List<UiElement> elementList;
        private final long timestamp;
        private final boolean isSingleElement;
        
        public CacheValue(UiElement element) {
            this.element = element;
            this.elementList = null;
            this.timestamp = System.currentTimeMillis();
            this.isSingleElement = true;
        }
        
        public CacheValue(List<UiElement> elementList) {
            this.element = null;
            this.elementList = elementList;
            this.timestamp = System.currentTimeMillis();
            this.isSingleElement = false;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }
    
    // 清除过期缓存
    private void cleanupCache() {
        elementCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 优化的字符串包含检查，考虑空值和性能
     */
    private boolean containsText(String text, String substring) {
        return text != null && substring != null && !substring.isEmpty() && text.contains(substring);
    }
    
    /**
     * 优化的ID匹配检查，使用常量和预计算
     */
    private boolean matchesResourceId(String actualId, String expectedId) {
        return actualId != null && expectedId != null && !expectedId.isEmpty() &&
               (actualId.equals(expectedId) || actualId.endsWith(":id/" + expectedId));
    }
    
    /**
     * 优化的类名匹配检查
     */
    private boolean matchesClassName(String actualClassName, String expectedClassName) {
        return actualClassName != null && expectedClassName != null && !expectedClassName.isEmpty() &&
               (actualClassName.equals(expectedClassName) || actualClassName.endsWith("." + expectedClassName));
    }
    /**
     * 通过资源ID查找元素（优化版）
     * @param elements 元素列表
     * @param resourceId 资源ID
     * @return 找到的元素，如果未找到返回null
     */
    public UiElement findByResourceId(List<UiElement> elements, String resourceId) {
        // 参数验证
        if (elements == null || resourceId == null || resourceId.isEmpty()) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("resourceId", resourceId, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 使用并行流优化查找性能，特别是在元素数量较多时
        UiElement result = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && attributes.containsKey("resourceId") &&
                           matchesResourceId((String) attributes.get("resourceId"), resourceId);
                })
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 通过文本内容查找元素（优化版）
     * @param elements 元素列表
     * @param text 文本内容
     * @return 找到的元素，如果未找到返回null
     */
    public UiElement findByText(List<UiElement> elements, String text) {
        // 参数验证
        if (elements == null || text == null) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("text", text, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 使用并行流优化查找性能
        UiElement result = elements.parallelStream()
                .filter(element -> text.equals(element.getText()))
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 通过部分文本内容查找元素（优化版）
     * @param elements 元素列表
     * @param partialText 部分文本内容
     * @return 找到的第一个匹配元素，如果未找到返回null
     */
    public UiElement findByPartialText(List<UiElement> elements, String partialText) {
        // 参数验证
        if (elements == null || partialText == null || partialText.isEmpty()) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("partialText", partialText, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 使用并行流和优化的字符串包含检查
        UiElement result = elements.parallelStream()
                .filter(element -> containsText(element.getText(), partialText))
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 通过内容描述查找元素（优化版）
     * @param elements 元素列表
     * @param contentDesc 内容描述
     * @return 找到的元素，如果未找到返回null
     */
    public UiElement findByContentDescription(List<UiElement> elements, String contentDesc) {
        // 参数验证
        if (elements == null || contentDesc == null) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("contentDescription", contentDesc, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 使用并行流优化查找性能
        UiElement result = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && attributes.containsKey("contentDescription") &&
                           contentDesc.equals(attributes.get("contentDescription"));
                })
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 通过类名查找元素（优化版）
     * @param elements 元素列表
     * @param className 类名
     * @return 找到的第一个匹配元素，如果未找到返回null
     */
    public UiElement findByClassName(List<UiElement> elements, String className) {
        // 参数验证
        if (elements == null || className == null || className.isEmpty()) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("className", className, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 使用并行流和优化的类名匹配检查
        UiElement result = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && attributes.containsKey("className") &&
                           matchesClassName((String) attributes.get("className"), className);
                })
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 通过文本内容的正则表达式查找元素（优化版）
     * @param elements 元素列表
     * @param regex 正则表达式
     * @return 找到的第一个匹配元素，如果未找到返回null
     */
    public UiElement findByTextRegex(List<UiElement> elements, String regex) {
        // 参数验证
        if (elements == null || regex == null || regex.isEmpty()) {
            return null;
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("textRegex", regex, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && cachedValue.isSingleElement) {
            return cachedValue.element;
        }
        
        // 预编译正则表达式以提高性能
        Pattern pattern = Pattern.compile(regex);
        
        // 使用并行流优化查找性能
        UiElement result = elements.parallelStream()
                .filter(element -> {
                    String text = element.getText();
                    return text != null && pattern.matcher(text).find();
                })
                .findFirst()
                .orElse(null);
        
        // 缓存结果
        if (result != null) {
            elementCache.put(key, new CacheValue(result));
        }
        
        return result;
    }
    
    /**
     * 查找所有匹配文本正则表达式的元素（优化版）
     * @param elements 元素列表
     * @param regex 正则表达式
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByTextRegex(List<UiElement> elements, String regex) {
        // 参数验证
        if (elements == null || regex == null || regex.isEmpty()) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allTextRegex", regex, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 预编译正则表达式以提高性能
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            // 处理无效的正则表达式
            return List.of();
        }
        
        // 使用并行流优化查找性能
        List<UiElement> results = elements.parallelStream()
                .filter(element -> {
                    String text = element.getText();
                    return text != null && pattern.matcher(text).matches();
                })
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 查找所有匹配资源ID的元素（优化版）
     * @param elements 元素列表
     * @param resourceId 资源ID
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByResourceId(List<UiElement> elements, String resourceId) {
        // 参数验证
        if (elements == null || resourceId == null || resourceId.isEmpty()) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allResourceId", resourceId, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 使用并行流优化查找性能，特别是在元素数量较多时
        List<UiElement> results = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && attributes.containsKey("resourceId") &&
                           matchesResourceId((String) attributes.get("resourceId"), resourceId);
                })
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 查找所有匹配文本内容的元素（优化版）
     * @param elements 元素列表
     * @param text 文本内容
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByText(List<UiElement> elements, String text) {
        // 参数验证
        if (elements == null || text == null) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allText", text, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 使用并行流优化查找性能
        List<UiElement> results = elements.parallelStream()
                .filter(element -> text.equals(element.getText()))
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 查找所有匹配部分文本内容的元素（优化版）
     * @param elements 元素列表
     * @param partialText 部分文本内容
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByPartialText(List<UiElement> elements, String partialText) {
        // 参数验证
        if (elements == null || partialText == null || partialText.isEmpty()) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allPartialText", partialText, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 使用并行流和优化的字符串包含检查
        List<UiElement> results = elements.parallelStream()
                .filter(element -> containsText(element.getText(), partialText))
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 查找所有匹配类名的元素（优化版）
     * @param elements 元素列表
     * @param className 类名
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByClassName(List<UiElement> elements, String className) {
        // 参数验证
        if (elements == null || className == null || className.isEmpty()) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allClassName", className, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 使用并行流和优化的类名匹配检查
        List<UiElement> results = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && attributes.containsKey("className") &&
                           matchesClassName((String) attributes.get("className"), className);
                })
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 查找所有匹配内容描述的元素（优化版）
     * @param elements 元素列表
     * @param contentDesc 内容描述
     * @return 匹配的元素列表
     */
    public List<UiElement> findAllByContentDescription(List<UiElement> elements, String contentDesc) {
        // 参数验证
        if (elements == null || contentDesc == null) {
            return List.of();
        }
        
        // 清理过期缓存并检查缓存
        cleanupCache();
        CacheKey key = new CacheKey("allContentDesc", contentDesc, elements);
        CacheValue cachedValue = elementCache.get(key);
        if (cachedValue != null && !cachedValue.isExpired() && !cachedValue.isSingleElement) {
            return cachedValue.elementList;
        }
        
        // 使用并行流优化查找性能
        List<UiElement> results = elements.parallelStream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && contentDesc.equals(attributes.get("contentDescription"));
                })
                .collect(Collectors.toList());
        
        // 缓存结果
        if (!results.isEmpty()) {
            elementCache.put(key, new CacheValue(results));
        }
        
        return results;
    }
    
    /**
     * 通过坐标区域查找元素
     * @param elements 元素列表
     * @param x x坐标
     * @param y y坐标
     * @return 包含该坐标的元素，如果未找到返回null
     */
    public UiElement findElementAtPosition(List<UiElement> elements, int x, int y) {
        if (elements == null) {
            return null;
        }
        
        for (UiElement element : elements) {
            Rectangle bounds = element.getBounds();
            if (bounds != null && x >= bounds.getX() && x <= bounds.getX() + bounds.getWidth() &&
                y >= bounds.getY() && y <= bounds.getY() + bounds.getHeight()) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * 通过多个属性组合查找元素
     * @param elements 元素列表
     * @param criteria 查找条件，键为属性名，值为期望的值
     * @return 找到的第一个匹配元素，如果未找到返回null
     */
    public UiElement findElementByMultipleAttributes(List<UiElement> elements, Map<String, String> criteria) {
        if (elements == null || criteria == null || criteria.isEmpty()) {
            return null;
        }
        
        for (UiElement element : elements) {
            Map<String, Object> attributes = element.getAttributes();
            if (attributes != null) {
                boolean match = true;
                for (Map.Entry<String, String> entry : criteria.entrySet()) {
                    String key = entry.getKey();
                    String expectedValue = entry.getValue();
                    Object actualValue = attributes.get(key);
                    
                    // 特殊处理resourceId属性
                    if (key.equals("resourceId") && actualValue != null) {
                        String actualId = (String) actualValue;
                        if (!actualId.equals(expectedValue) && !actualId.endsWith(":id/" + expectedValue)) {
                            match = false;
                            break;
                        }
                    }
                    // 特殊处理className属性
                    else if (key.equals("className") && actualValue != null) {
                        String actualClassName = (String) actualValue;
                        if (!actualClassName.equals(expectedValue) && !actualClassName.endsWith("." + expectedValue)) {
                            match = false;
                            break;
                        }
                    }
                    // 处理其他属性
                    else if (!expectedValue.equals(actualValue)) {
                        match = false;
                        break;
                    }
                }
                
                if (match) {
                    return element;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找可点击的元素
     * @param elements 元素列表
     * @return 可点击的元素列表
     */
    public List<UiElement> findClickableElements(List<UiElement> elements) {
        if (elements == null) {
            return List.of();
        }
        
        return elements.stream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && Boolean.TRUE.equals(attributes.get("clickable"));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 查找可编辑的元素
     * @param elements 元素列表
     * @return 可编辑的元素列表
     */
    public List<UiElement> findEditableElements(List<UiElement> elements) {
        if (elements == null) {
            return List.of();
        }
        
        return elements.stream()
                .filter(element -> {
                    Map<String, Object> attributes = element.getAttributes();
                    return attributes != null && Boolean.TRUE.equals(attributes.get("editable"));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 查找可见的元素
     * @param elements 元素列表
     * @return 可见的元素列表
     */
    public List<UiElement> findVisibleElements(List<UiElement> elements) {
        if (elements == null) {
            return List.of();
        }
        
        return elements.stream()
                .filter(element -> {
                    // 检查元素是否为AndroidUiElement实例并访问其visible字段
                    if (element instanceof AndroidUiElement) {
                        return ((AndroidUiElement) element).isVisible();
                    }
                    // 或者从元素属性中获取可见性信息
                    Map<String, Object> attributes = element.getAttributes();
                    if (attributes != null && attributes.containsKey("visible")) {
                        Object visibleAttr = attributes.get("visible");
                        return visibleAttr instanceof Boolean && (Boolean) visibleAttr;
                    }
                    // 默认假设元素可见
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 通过元素类型查找元素
     * @param elements 元素列表
     * @param type 元素类型
     * @return 匹配的元素列表
     */
    public List<UiElement> findElementsByType(List<UiElement> elements, String type) {
        if (elements == null || type == null || type.isEmpty()) {
            return List.of();
        }
        
        return elements.stream()
                .filter(element -> type.equals(element.getType()))
                .collect(Collectors.toList());
    }
    
    /**
     * 创建一个查找条件映射
     * @param resourceId 资源ID
     * @param text 文本内容
     * @param className 类名
     * @param contentDesc 内容描述
     * @return 查找条件映射
     */
    public Map<String, String> createFindCriteria(String resourceId, String text, String className, String contentDesc) {
        Map<String, String> criteria = new HashMap<>();
        
        if (resourceId != null) {
            criteria.put("resourceId", resourceId);
        }
        if (text != null) {
            criteria.put("text", text);
        }
        if (className != null) {
            criteria.put("className", className);
        }
        if (contentDesc != null) {
            criteria.put("contentDescription", contentDesc);
        }
        
        return criteria;
    }
}