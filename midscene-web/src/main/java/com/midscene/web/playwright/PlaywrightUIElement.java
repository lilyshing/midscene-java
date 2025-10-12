package com.midscene.web.playwright;

import com.midscene.core.model.UiElement;
import com.midscene.core.model.Rect;
import com.midscene.core.model.Point;
import com.midscene.core.model.NodeType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;

/**
 * Playwright实现的UI元素类
 */
public class PlaywrightUIElement extends UiElement {
    private final Locator locator;
    private final Page page;
    
    public PlaywrightUIElement(String id, String content, Rect rect, Locator locator, Page page) {
        super(id, content, rect);
        this.locator = locator;
        this.page = page;
        // 根据标签名确定节点类型
        determineNodeType();
    }
    
    private void determineNodeType() {
        try {
            String tagName = locator.evaluate("el => el.tagName.toLowerCase()").toString();
            String type = locator.evaluate("el => el.type || ''").toString();
            
            switch (tagName) {
                case "button":
                case "input":
                    if ("button".equals(type) || "submit".equals(type) || "reset".equals(type)) {
                        setNodeType(NodeType.BUTTON);
                    } else {
                        // 其他输入类型都视为INPUT
                        setNodeType(NodeType.INPUT);
                    }
                    break;
                case "a":
                    setNodeType(NodeType.LINK);
                    break;
                case "img":
                    setNodeType(NodeType.IMAGE);
                    break;
                case "div":
                case "span":
                case "section":
                case "main":
                case "article":
                    setNodeType(NodeType.CONTAINER);
                    break;
                case "p":
                case "h1":
                case "h2":
                case "h3":
                case "h4":
                case "h5":
                case "h6":
                case "label":
                    setNodeType(NodeType.TEXT);
                    break;
                default:
                    setNodeType(NodeType.OTHER);
            }
        } catch (Exception e) {
            // 如果无法确定类型，默认为OTHER
            setNodeType(NodeType.OTHER);
        }
    }
    
    @Override
    public CompletableFuture<Void> tap() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 等待元素可见并可交互
                locator.waitFor();
                // 点击元素
                locator.click(new Locator.ClickOptions().setTimeout(5000));
            } catch (Exception e) {
                throw new RuntimeException("Failed to tap element: " + getContent(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> inputText(String text) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 等待元素可见并可交互
                locator.waitFor();
                
                // 根据元素类型执行不同的输入操作
                String tagName = locator.evaluate("el => el.tagName.toLowerCase()").toString();
                
                if ("input".equals(tagName)) {
                    // 对于输入框，先清空再输入
                    locator.fill("");
                    locator.type(text, new Locator.TypeOptions().setDelay(50));
                } else if ("textarea".equals(tagName)) {
                    // 对于文本域，也先清空再输入
                    locator.fill("");
                    locator.type(text, new Locator.TypeOptions().setDelay(50));
                } else if ("select".equals(tagName)) {
                    // 对于下拉选择框，使用selectOption
                    locator.selectOption(text);
                } else {
                    // 对于其他元素，尝试输入
                    locator.type(text, new Locator.TypeOptions().setDelay(50));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to input text to element: " + getContent(), e);
            }
        });
    }
    
    /**
     * 获取元素的属性值
     */
    public String getAttribute(String attributeName) {
        try {
            return locator.getAttribute(attributeName);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查元素是否可见
     */
    public boolean isElementVisible() {
        try {
            return locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取元素的文本内容
     */
    public String getElementText() {
        try {
            return locator.textContent().trim();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 获取元素的内部HTML
     */
    public String getInnerHTML() {
        try {
            return locator.innerHTML();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 获取元素的所有属性
     */
    public Map<String, String> getAllAttributes() {
        Map<String, String> attributes = new HashMap<>();
        try {
            // 获取元素的所有属性
            List<String> attributeNames = (List<String>) locator.evaluate("el => Array.from(el.attributes).map(attr => attr.name)");
            
            for (String attrName : attributeNames) {
                String attrValue = getAttribute(attrName);
                if (attrValue != null) {
                    attributes.put(attrName, attrValue);
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回已收集的属性
        }
        return attributes;
    }
    
    /**
     * 获取元素的CSS样式
     */
    public String getCssProperty(String propertyName) {
        try {
            return (String) locator.evaluate(
                "(el, prop) => window.getComputedStyle(el).getPropertyValue(prop)", 
                propertyName
            );
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取元素的XPath
     */
    public List<String> getElementXpaths() {
        List<String> xpaths = new ArrayList<>();
        try {
            // 使用Playwright的evaluate获取XPath
            String xpath = (String) locator.evaluate(
                "el => {" +
                "    if (!(el instanceof Element)) return '';" +
                "    let path = [];" +
                "    while (el && el.nodeType === Node.ELEMENT_NODE) {" +
                "        let selector = el.nodeName.toLowerCase();" +
                "        if (el.id) {" +
                "            selector += '[id=\"' + el.id + '\"]';" +
                "            path.unshift(selector);" +
                "            break;" +
                "        }" +
                "        let siblings = Array.from(el.parentNode.children).filter(child => child.nodeName === el.nodeName);" +
                "        if (siblings.length > 1) {" +
                "            let index = siblings.indexOf(el) + 1;" +
                "            selector += '[' + index + ']';" +
                "        }" +
                "        path.unshift(selector);" +
                "        el = el.parentNode;" +
                "    }" +
                "    return path.length ? '/' + path.join('/') : '';" +
                "}"
            );
            
            if (xpath != null && !xpath.isEmpty()) {
                xpaths.add(xpath);
            }
        } catch (Exception e) {
            // 忽略异常，返回空列表
        }
        return xpaths;
    }
    
    /**
     * 滚动到元素可见
     */
    public void scrollIntoView() {
        try {
            locator.scrollIntoViewIfNeeded();
        } catch (Exception e) {
            // 忽略异常
        }
    }
}