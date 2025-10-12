package com.midscene.web.playwright;

import com.midscene.core.agent.UIContextProvider;
import com.midscene.core.model.UiElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Playwright实现的UI上下文提供者
 */
public class PlaywrightUIContextProvider implements UIContextProvider {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightUIContextProvider.class);
    private final Page page;
    private final PlaywrightPage playwrightPage;
    
    public PlaywrightUIContextProvider(Page page, PlaywrightPage playwrightPage) {
        this.page = page;
        this.playwrightPage = playwrightPage;
    }
    
    @Override
    public CompletableFuture<String> getCurrentUIContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Fetching UI context from Playwright");
                
                // 获取页面标题
                String title = page.title();
                
                // 获取页面URL
                String url = page.url();
                
                // 获取基本的页面结构信息
                int totalElements = page.locator("*").count();
                
                // 获取主要可见元素的文本内容
                Locator visibleElements = page.locator("body *:visible");
                StringBuilder elementsText = new StringBuilder();
                
                for (int i = 0; i < Math.min(visibleElements.count(), 10); i++) {
                    String text = visibleElements.nth(i).textContent();
                    if (text != null && !text.trim().isEmpty()) {
                        elementsText.append("- " + text.trim() + "\n");
                    }
                }
                
                // 构建上下文描述
                String context = "Page Title: " + title + "\n" +
                                "URL: " + url + "\n" +
                                "Total Elements: " + totalElements + "\n" +
                                "Visible Elements:\n" + elementsText.toString();
                
                logger.debug("UI Context fetched successfully");
                return context;
            } catch (Exception e) {
                logger.error("Failed to fetch UI context", e);
                throw new RuntimeException("Failed to fetch UI context", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<UiElement>> getCurrentUIElements() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Fetching UI elements from Playwright");
                List<UiElement> elements = new ArrayList<>();
                
                // 获取页面上的主要交互元素
                List<String> selectors = List.of(
                    "a", "button", "input", "select", "textarea", 
                    "[role='button']", "[role='link']", "[tabindex]:not([tabindex='-1'])",
                    "h1", "h2", "h3", "h4", "h5", "h6", 
                    "p", "div:not(:empty)"
                );
                
                for (String selector : selectors) {
                    Locator elementsLocator = page.locator(selector);
                    int count = elementsLocator.count();
                    
                    for (int i = 0; i < count; i++) {
                        try {
                            Locator element = elementsLocator.nth(i);
                            if (element.isVisible()) {
                                // 创建PlaywrightUIElement实例
                                PlaywrightUIElement uiElement = playwrightPage.createUIElement(element);
                                elements.add(uiElement);
                            }
                        } catch (Exception e) {
                            logger.warn("Error processing element at index " + i + " with selector " + selector, e);
                        }
                    }
                }
                
                logger.info("Retrieved {} UI elements", elements.size());
                return elements;
            } catch (Exception e) {
                logger.error("Failed to fetch UI elements", e);
                throw new RuntimeException("Failed to fetch UI elements", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> refresh() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Refreshing UI context");
                // 这里可以执行一些刷新操作，比如等待页面加载完成
                page.waitForLoadState();
                logger.debug("UI context refreshed successfully");
                return true;
            } catch (Exception e) {
                logger.error("Failed to refresh UI context", e);
                return false;
            }
        });
    }
}