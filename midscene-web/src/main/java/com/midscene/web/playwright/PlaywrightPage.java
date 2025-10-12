package com.midscene.web.playwright;

import com.midscene.core.model.*;
import com.midscene.core.exception.ElementNotFoundException;
import com.microsoft.playwright.*;
import com.microsoft.playwright.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Playwright页面操作类，负责与Web页面交互
 */
public class PlaywrightPage implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightPage.class);
    
    private final Page page;
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private boolean isHeadless = true; // 默认为headless模式
    
    public PlaywrightPage() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(isHeadless));
        this.context = browser.newContext();
        this.page = context.newPage();
        
        logger.info("Playwright page initialized successfully (headless: {})", isHeadless);
    }
    
    /**
     * 设置是否使用headless模式
     * 注意：此方法必须在创建PlaywrightPage实例之前调用
     */
    public static void setHeadless(boolean headless) {
        // 这个方法是静态的，用于设置后续创建的PlaywrightPage实例的headless模式
        // 由于构造函数中已经创建了browser实例，我们需要重新设计这个方法
        // 这里仅作为示例，实际使用时可能需要使用工厂模式
        logger.warn("setHeadless方法应在创建PlaywrightPage实例之前调用");
    }
    
    /**
     * 创建非headless模式的PlaywrightPage实例
     */
    public static PlaywrightPage createWithHeadlessMode(boolean headless) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        
        logger.info("Playwright page initialized successfully (headless: {})", headless);
        
        return new PlaywrightPage(page, playwright, browser, context);
    }
    
    /**
     * 使用现有页面创建PlaywrightPage实例
     */
    private PlaywrightPage(Page page, Playwright playwright, Browser browser, BrowserContext context) {
        this.page = page;
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
    }
    
    public PlaywrightPage(Page page) {
        this.page = page;
        this.playwright = null;
        this.browser = null;
        this.context = null;
        
        logger.info("Playwright page wrapper initialized with existing page");
    }
    
    /**
     * 导航到指定URL
     */
    public CompletableFuture<Void> navigate(String url) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Navigating to URL: {}", url);
                page.navigate(url);
                logger.info("Navigation completed successfully");
            } catch (Exception e) {
                logger.error("Navigation failed: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to navigate to " + url, e);
            }
        });
    }
    
    /**
     * 获取当前页面的所有UI元素
     */
    public CompletableFuture<List<UiElement>> getElements() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<UiElement> elements = new ArrayList<>();
                
                // 获取所有可交互元素
                List<ElementHandle> handles = page.querySelectorAll("a, button, input, select, textarea, [role='button'], [role='link']");
                
                for (ElementHandle handle : handles) {
                    try {
                        PlaywrightUiElement element = createUiElement(handle);
                        if (element != null && element.isVisible()) {
                            elements.add(element);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process element: {}", e.getMessage());
                    }
                    // 避免内存泄漏
                    handle.dispose();
                }
                
                logger.info("Retrieved {} visible elements from page", elements.size());
                return elements;
            } catch (Exception e) {
                logger.error("Failed to get elements: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to retrieve UI elements", e);
            }
        });
    }
    
    /**
     * 获取当前页面的文本描述
     */
    public CompletableFuture<String> getPageDescription() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String title = page.title();
                String url = page.url();
                String jsCode = "() => {" +
                                "const headings = Array.from(document.querySelectorAll('h1, h2, h3')).map(h => h.textContent.trim());" +
                                "const buttons = Array.from(document.querySelectorAll('button, [role=\\\"button\\\"]')).map(b => b.textContent.trim()).filter(t => t.length > 0);" +
                                "const links = Array.from(document.querySelectorAll('a')).map(a => a.textContent.trim()).filter(t => t.length > 0);" +
                                "return { headings, buttons, links };" +
                                "}";
                String contentSummary = page.evaluate(jsCode).toString();
                
                return String.format("Page Title: %s\nURL: %s\nContent Summary: %s", title, url, contentSummary);
            } catch (Exception e) {
                logger.error("Failed to get page description: {}", e.getMessage(), e);
                return "Failed to generate page description";
            }
        });
    }
    
    /**
     * 捕获页面截图
     * @param screenshotName 截图名称（用于生成文件名）
     * @return 截图文件的绝对路径
     */
    public String captureScreenshot(String screenshotName) {
        try {
            // 创建截图目录
            String screenshotDir = "test-screenshots";
            File dir = new File(screenshotDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成带时间戳的文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.png", screenshotName, timestamp);
            String filePath = Paths.get(screenshotDir, fileName).toString();
            
            // 捕获截图
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filePath)));
            
            // 获取绝对路径并替换反斜杠为正斜杠，以便在HTML中正确显示
            String absolutePath = new File(filePath).getAbsolutePath();
            absolutePath = absolutePath.replace("\\", "/");
            
            logger.info("Screenshot saved to: {}", absolutePath);
            return absolutePath;
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }
    
    /**
     * 关闭Playwright资源
     */
    @Override
    public void close() {
        try {
            if (page != null) {
                page.close();
            }
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
            logger.info("Playwright resources closed successfully");
        } catch (Exception e) {
            logger.error("Failed to close playwright resources: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建UI元素对象（使用Locator）
     */
    public PlaywrightUIElement createUIElement(Locator locator) {
        try {
            // 检查元素是否可见
            boolean visible = locator.isVisible();
            if (!visible) {
                return null;
            }
            
            // 获取元素属性
            String id = locator.getAttribute("id");
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
            }
            
            String text = locator.textContent() != null ? locator.textContent().trim() : "";
            
            // 获取元素位置和大小
            Rect rect;
            try {
                // 使用默认位置信息
                rect = new Rect(0, 0, 100, 50);
            } catch (Exception e) {
                return null; // 获取位置信息失败
            }
            
            // 创建元素对象
            PlaywrightUIElement element = new PlaywrightUIElement(id, text, rect, locator, page);
            
            // 设置元素属性
            Map<String, Object> attributes = new HashMap<>();
            try {
                // 获取常见属性
                String[] commonAttrs = {"id", "class", "name", "type", "value", "href", "title", "alt"};
                for (String attr : commonAttrs) {
                    String value = locator.getAttribute(attr);
                    if (value != null) {
                        attributes.put(attr, value);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get attributes for element", e);
            }
            element.setAttributes(attributes);
            
            // 设置XPath
            List<String> xpaths = element.getElementXpaths();
            if (!xpaths.isEmpty()) {
                element.setXpaths(xpaths);
            }
            
            return element;
        } catch (Exception e) {
            logger.warn("Error creating PlaywrightUIElement: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建UI元素对象（使用ElementHandle）
     */
    private PlaywrightUiElement createUiElement(ElementHandle handle) {
        try {
            // 检查元素是否可见
            String visibleJs = "el => {" +
                              "const style = window.getComputedStyle(el);" +
                              "return el.offsetParent !== null && " +
                              "       style.display !== 'none' && " +
                              "       style.visibility !== 'hidden' &&" +
                              "       el.clientWidth > 0 && " +
                              "       el.clientHeight > 0;" +
                              "}";
            boolean visible = (boolean) handle.evaluate(visibleJs);
            
            if (!visible) {
                return null;
            }
            
            // 获取元素属性
            String id = handle.evaluate("el => el.id").toString();
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
            }
            
            String text = handle.evaluate("el => el.textContent || ''").toString();
            String tagName = handle.evaluate("el => el.tagName.toLowerCase()").toString();
            
            // 获取元素位置和大小
            String boundingBoxJs = "el => {" +
                                  "const rect = el.getBoundingClientRect();" +
                                  "return { x: rect.left, y: rect.top, width: rect.width, height: rect.height };" +
                                  "}";
            Map<String, Double> boundingBox = (Map<String, Double>) handle.evaluate(boundingBoxJs);
            
            Rect rect = new Rect(
                boundingBox.getOrDefault("x", 0.0),
                boundingBox.getOrDefault("y", 0.0),
                boundingBox.getOrDefault("width", 0.0),
                boundingBox.getOrDefault("height", 0.0)
            );
            
            // 确定节点类型
            NodeType nodeType = determineNodeType(tagName, handle);
            
            // 创建元素对象
            PlaywrightUiElement element = new PlaywrightUiElement(id, text, rect, handle);
            element.setNodeType(nodeType);
            
            // 获取其他属性
            String attributesJs = "el => {" +
                                  "const attrs = {};" +
                                  "for (let attr of el.attributes) {" +
                                  "attrs[attr.name] = attr.value;" +
                                  "}" +
                                  "return attrs;" +
                                  "}";
            Map<String, Object> attributes = (Map<String, Object>) handle.evaluate(attributesJs);
            element.setAttributes(attributes);
            
            // 获取XPath（简化版本）
            String xpath = generateXPath(handle);
            if (xpath != null) {
                element.setXpaths(List.of(xpath));
            }
            
            return element;
        } catch (Exception e) {
            logger.warn("Error creating UI element: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 确定节点类型
     */
    private NodeType determineNodeType(String tagName, ElementHandle handle) {
        switch (tagName) {
            case "button":
            case "a":
                return NodeType.BUTTON;
            case "input":
                String type = handle.evaluate("el => el.type").toString();
                if ("checkbox".equals(type) || "radio".equals(type)) {
                    return NodeType.INPUT; // 使用INPUT替代CHECKBOX
                } else {
                    return NodeType.INPUT;
                }
            case "select":
                return NodeType.INPUT; // 使用INPUT替代SELECT
            case "textarea":
                return NodeType.INPUT; // 使用INPUT替代TEXTAREA
            case "div":
            case "span":
            case "p":
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
                return NodeType.TEXT;
            case "ul":
            case "ol":
            case "li":
                return NodeType.OTHER; // 使用OTHER替代LIST
            default:
                return NodeType.OTHER;
        }
    }
    
    /**
     * 生成元素的XPath（简化版）
     */
    private String generateXPath(ElementHandle handle) {
        try {
            String jsCode = "el => {" +
                            "let path = '';" +
                            "while (el && el.nodeType === Node.ELEMENT_NODE) {" +
                            "let index = 0;" +
                            "let sibling = el.previousSibling;" +
                            "while (sibling) {" +
                            "if (sibling.nodeType === Node.ELEMENT_NODE && sibling.tagName === el.tagName) {" +
                            "index++;" +
                            "}" +
                            "sibling = sibling.previousSibling;" +
                            "}" +
                            "const tagName = el.tagName.toLowerCase();" +
                            "const indexStr = index > 0 ? '[' + (index + 1) + ']' : '';" +
                            "path = '/' + tagName + indexStr + path;" +
                            "el = el.parentNode;" +
                            "}" +
                            "return path;" +
                            "}";
            return handle.evaluate(jsCode).toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Playwright实现的UI元素类
     */
    public class PlaywrightUiElement extends UiElement {
        private final ElementHandle elementHandle;
        
        public PlaywrightUiElement(String id, String content, Rect rect, ElementHandle elementHandle) {
            super(id, content, rect);
            this.elementHandle = elementHandle;
        }
        
        @Override
        public CompletableFuture<Void> tap() {
            return CompletableFuture.runAsync(() -> {
                try {
                    logger.info("Clicking element: {}", getContent());
                    elementHandle.click();
                    logger.info("Element clicked successfully");
                } catch (Exception e) {
                    logger.error("Failed to click element: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to click element", e);
                }
            });
        }
        
        @Override
        public CompletableFuture<Void> inputText(String text) {
            return CompletableFuture.runAsync(() -> {
                try {
                    logger.info("Inputting text '{}' to element: {}", text, getContent());
                    String tagName = elementHandle.evaluate("el => el.tagName.toLowerCase()").toString();
                    
                    if ("input".equals(tagName) || "textarea".equals(tagName)) {
                        // 先清空现有内容
                        elementHandle.fill("");
                        // 输入新内容
                        elementHandle.fill(text);
                    } else {
                        // 对于其他元素，尝试使用type方法
                        elementHandle.type(text);
                    }
                    
                    logger.info("Text input completed successfully");
                } catch (Exception e) {
                    logger.error("Failed to input text: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to input text", e);
                }
            });
        }
    }
}