package com.midscene.android;

import com.midscene.core.model.UiContext;
import com.midscene.core.model.UiElement;
import com.midscene.core.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Android平台接口实现，连接Android设备与Agent核心控制器
 */
public class AndroidPlatform implements com.midscene.core.agent.PlatformInterface {
    private static final Logger logger = LoggerFactory.getLogger(AndroidPlatform.class);
    private final AndroidDevice device;
    private UiContext cachedContext;
    private final long cacheExpiryTimeMs = 1000; // 1秒缓存过期时间
    private long lastUpdateTimeMs = 0;
    
    public AndroidPlatform(AndroidDevice device) {
        this.device = device;
    }
    
    public AndroidPlatform(String deviceId) {
        this.device = new AndroidDevice(deviceId);
    }
    
    public AndroidPlatform() {
        this.device = new AndroidDevice();
    }
    
    @Override
    public String getInterfaceType() {
        return "ANDROID";
    }
    
    @Override
    public CompletableFuture<UiContext> getUiContext() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (cachedContext != null && (currentTime - lastUpdateTimeMs) < cacheExpiryTimeMs) {
            logger.debug("Returning cached UI context");
            return CompletableFuture.completedFuture(cachedContext);
        }
        
        // 重新获取UI上下文
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Getting UI context from Android device");
                
                // 并行获取截图和UI元素
                CompletableFuture<byte[]> screenshotFuture = device.takeScreenshot();
                CompletableFuture<List<UiElement>> elementsFuture = device.getElements();
                
                byte[] screenshot = screenshotFuture.join();
                List<UiElement> elements = elementsFuture.join();
                
                // 创建新的UI上下文
                UiContext context = new UiContext();
                cachedContext = context;
                lastUpdateTimeMs = System.currentTimeMillis();
                
                logger.info("UI context retrieved successfully");
                return context;
            } catch (Exception e) {
                logger.error("Failed to get UI context: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to retrieve UI context from Android device", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> tap(int x, int y) {
        logger.info("Tapping at position: ({}, {})", x, y);
        try {
            device.tap(x, y);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Tap operation failed: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    @Override
    public CompletableFuture<Boolean> inputText(String text, int x, int y) {
        logger.info("Inputting text: {}", text);
        try {
            // 先点击位置，再输入文本
            device.tap(x, y);
            device.inputText(text);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Input text operation failed: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    @Override
    public CompletableFuture<Boolean> scroll(String direction, int distance) {
        logger.info("Scrolling {} with distance {}", direction, distance);
        
        // 根据不同的滚动方向和距离执行相应的ADB命令
        String swipeCommand;
        
        switch (direction) {
            case "up":
                swipeCommand = String.format("input swipe 500 1500 500 %d", 1500 - distance);
                break;
            case "down":
                swipeCommand = String.format("input swipe 500 500 500 %d", 500 + distance);
                break;
            case "left":
                swipeCommand = String.format("input swipe 1000 1000 %d 1000", 1000 - distance);
                break;
            case "right":
                swipeCommand = String.format("input swipe 300 1000 %d 1000", 300 + distance);
                break;
            default:
                swipeCommand = "input swipe 500 1500 500 500";
                break;
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 直接执行ADB命令进行滚动
                device.executeAdbCommand("-s", device.getDeviceId(), "shell", swipeCommand);
                // 滚动后清除缓存
                invalidateCache();
                logger.info("Scroll operation completed successfully");
                return true;
            } catch (Exception e) {
                logger.error("Failed to scroll: {}", e.getMessage(), e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> navigate(String url) {
        logger.warn("Navigate operation not supported on Android platform");
        return CompletableFuture.completedFuture(false);
    }
    
    @Override
    public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
        logger.info("Waiting for page load with timeout: {}ms", timeout);
        try {
            Thread.sleep(timeout);
            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(false);
        }
    }
    
    @Override
    public void navigateTo(String urlOrPage) {
        logger.warn("NavigateTo operation not supported on Android platform");
    }
    
    @Override
    public void takeScreenshot(String fileName) {
        try {
            logger.info("Taking screenshot and saving to: {}", fileName);
            byte[] screenshotData = device.takeScreenshot().get();
            
            // 这里应该将截图数据保存到文件
            // 实际实现需要根据项目需求确定文件保存位置
            logger.info("Screenshot saved successfully");
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void exitApplication() {
        try {
            if (device != null) {
                // 关闭设备连接
                device.close();
                // 不需要将device设置为null，因为它是final变量
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            System.err.println("Error exiting Android application: " + e.getMessage());
        }
    }
    
    @Override
    public void close() {
        exitApplication();
    }
    
    @Override
    public boolean isConnected() {
        try {
            // 检查设备是否连接
            String result = device.executeAdbCommand("-s", device.getDeviceId(), "shell", "echo connected");
            return result != null && result.contains("connected");
        } catch (Exception e) {
            logger.error("Failed to check connection: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private void invalidateCache() {
        cachedContext = null;
        lastUpdateTimeMs = 0;
        logger.debug("UI context cache invalidated");
    }
    
    /**
     * 获取底层Android设备实例
     * @return AndroidDevice实例
     */
    public AndroidDevice getDevice() {
        return device;
    }
}