package com.midscene.examples;

import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.model.UiContext;
import com.midscene.web.playwright.PlaywrightPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Playwright平台接口实现
 * 连接Midscene框架与Playwright自动化工具
 */
public class PlaywrightPlatformInterface implements PlatformInterface {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightPlatformInterface.class);
    
    private final PlaywrightPage playwrightPage;
    
    public PlaywrightPlatformInterface(PlaywrightPage playwrightPage) {
        this.playwrightPage = playwrightPage;
    }
    
    @Override
    public String getInterfaceType() {
        return "PLAYWRIGHT";
    }
    
    @Override
    public CompletableFuture<UiContext> getUiContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建基本的UI上下文
                UiContext context = new UiContext();
                
                // 获取页面描述
                return playwrightPage.getPageDescription()
                    .thenApply(description -> {
                        // 设置上下文属性
                        context.addMetadata("description", description);
                        logger.debug("Playwright UI上下文获取成功");
                        return context;
                    })
                    .get(); // 等待异步操作完成
            } catch (Exception e) {
                logger.error("获取Playwright UI上下文失败", e);
                throw new RuntimeException("获取Playwright UI上下文失败", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> tap(int x, int y) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("点击坐标: ({}, {})", x, y);
                // 由于PlaywrightPage没有提供直接访问Page的方法，
                // 我们需要通过反射获取内部的Page对象
                try {
                    java.lang.reflect.Field pageField = PlaywrightPage.class.getDeclaredField("page");
                    pageField.setAccessible(true);
                    com.microsoft.playwright.Page page = (com.microsoft.playwright.Page) pageField.get(playwrightPage);
                    page.mouse().click(x, y);
                    return true;
                } catch (Exception e) {
                    logger.error("通过反射访问Page对象失败", e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("点击操作失败", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> inputText(String text, int x, int y) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("在坐标({}, {})输入文本: {}", x, y, text);
                
                // 通过反射获取内部的Page对象
                try {
                    java.lang.reflect.Field pageField = PlaywrightPage.class.getDeclaredField("page");
                    pageField.setAccessible(true);
                    com.microsoft.playwright.Page page = (com.microsoft.playwright.Page) pageField.get(playwrightPage);
                    
                    // 先点击位置
                    page.mouse().click(x, y);
                    
                    // 然后输入文本
                    page.keyboard().type(text);
                    
                    return true;
                } catch (Exception e) {
                    logger.error("通过反射访问Page对象失败", e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("输入操作失败", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> scroll(String direction, int distance) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("滚动方向: {}, 距离: {}", direction, distance);
                
                // 通过反射获取内部的Page对象
                try {
                    java.lang.reflect.Field pageField = PlaywrightPage.class.getDeclaredField("page");
                    pageField.setAccessible(true);
                    com.microsoft.playwright.Page page = (com.microsoft.playwright.Page) pageField.get(playwrightPage);
                    
                    // 根据方向计算滚动距离
                    int deltaX = 0;
                    int deltaY = 0;
                    
                    switch (direction.toLowerCase()) {
                        case "up":
                            deltaY = -distance;
                            break;
                        case "down":
                            deltaY = distance;
                            break;
                        case "left":
                            deltaX = -distance;
                            break;
                        case "right":
                            deltaX = distance;
                            break;
                        default:
                            logger.warn("未知的滚动方向: {}", direction);
                            return false;
                    }
                    
                    // 使用Playwright的wheel方法进行滚动
                    page.mouse().wheel(deltaX, deltaY);
                    
                    return true;
                } catch (Exception e) {
                    logger.error("通过反射访问Page对象失败", e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("滚动操作失败", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> navigate(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("导航到URL: {}", url);
                // 使用PlaywrightPage的navigate方法
                try {
                    playwrightPage.navigate(url).get(); // 等待导航完成
                    return true;
                } catch (Exception e) {
                    logger.error("导航失败", e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("导航失败", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("等待页面加载，超时时间: {}ms", timeout);
                
                // 通过反射获取内部的Page对象
                try {
                    java.lang.reflect.Field pageField = PlaywrightPage.class.getDeclaredField("page");
                    pageField.setAccessible(true);
                    com.microsoft.playwright.Page page = (com.microsoft.playwright.Page) pageField.get(playwrightPage);
                    
                    // 使用Playwright的waitForLoadState方法等待页面加载完成
                    page.waitForLoadState();
                    
                    return true;
                } catch (Exception e) {
                    logger.error("通过反射访问Page对象失败", e);
                    return false;
                }
            } catch (Exception e) {
                logger.error("等待页面加载失败", e);
                return false;
            }
        });
    }
    
    public void navigateTo(String urlOrPage) {
        logger.warn("NavigateTo操作在Playwright平台上不支持");
    }
    
    public void takeScreenshot(String fileName) {
        try {
            logger.info("截图保存到: {}", fileName);
            // 使用PlaywrightPage的captureScreenshot方法
            String screenshotPath = playwrightPage.captureScreenshot(fileName);
            logger.info("截图已保存: {}", screenshotPath);
        } catch (Exception e) {
            logger.error("截图失败", e);
        }
    }
    
    public void exitApplication() {
        try {
            logger.info("退出Playwright应用程序");
            // 使用PlaywrightPage的close方法
            playwrightPage.close();
        } catch (Exception e) {
            logger.error("退出Playwright应用程序失败", e);
        }
    }
    
    @Override
    public void close() {
        exitApplication();
    }
    
    @Override
    public boolean isConnected() {
        try {
            // 通过反射获取内部的Page对象
            java.lang.reflect.Field pageField = PlaywrightPage.class.getDeclaredField("page");
            pageField.setAccessible(true);
            com.microsoft.playwright.Page page = (com.microsoft.playwright.Page) pageField.get(playwrightPage);
            return page != null && !page.isClosed();
        } catch (Exception e) {
            logger.error("检查连接状态失败", e);
            return false;
        }
    }
}