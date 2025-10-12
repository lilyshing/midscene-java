package com.midscene.examples;

import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.model.UiContext;
import com.midscene.android.AndroidDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Android平台接口实现
 * 连接Midscene框架与Android设备
 */
public class AndroidPlatformInterface implements PlatformInterface {
    private static final Logger logger = LoggerFactory.getLogger(AndroidPlatformInterface.class);
    
    private final AndroidDevice androidDevice;
    
    public AndroidPlatformInterface(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }
    
    @Override
    public String getInterfaceType() {
        return "ANDROID";
    }
    
    @Override
    public CompletableFuture<UiContext> getUiContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建基本的UI上下文
                UiContext context = new UiContext();
                
                // 获取设备信息
                String deviceId = androidDevice.getDeviceId();
                
                // 设置上下文属性
                context.addMetadata("deviceId", deviceId);
                
                logger.debug("Android UI上下文获取成功: {}", deviceId);
                return context;
            } catch (Exception e) {
                logger.error("获取Android UI上下文失败", e);
                throw new RuntimeException("获取Android UI上下文失败", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> tap(int x, int y) {
        return androidDevice.tap(x, y)
            .thenApply(v -> true)
            .exceptionally(e -> {
                logger.error("点击操作失败", e);
                return false;
            });
    }
    
    @Override
    public CompletableFuture<Boolean> inputText(String text, int x, int y) {
        return androidDevice.inputText(text)
            .thenApply(v -> true)
            .exceptionally(e -> {
                logger.error("输入操作失败", e);
                return false;
            });
    }
    
    @Override
    public CompletableFuture<Boolean> scroll(String direction, int distance) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("滚动方向: {}, 距离: {}", direction, distance);
                
                // AndroidDevice类没有scroll方法，我们使用adb命令模拟滚动
                // 根据方向转换为坐标
                int startX = 0, startY = 0, endX = 0, endY = 0;
                
                if ("up".equals(direction)) {
                    startX = 500; // 屏幕中间
                    startY = 1000; // 从下往上
                    endX = 500;
                    endY = 200;
                } else if ("down".equals(direction)) {
                    startX = 500; // 屏幕中间
                    startY = 200; // 从上往下
                    endX = 500;
                    endY = 1000;
                } else if ("left".equals(direction)) {
                    startX = 800; // 从右往左
                    startY = 500; // 屏幕中间
                    endX = 200;
                    endY = 500;
                } else if ("right".equals(direction)) {
                    startX = 200; // 从左往右
                    startY = 500; // 屏幕中间
                    endX = 800;
                    endY = 500;
                }
                
                // 使用adb命令执行滑动
                String result = androidDevice.executeAdbCommand("shell", "input", "swipe", 
                    String.valueOf(startX), String.valueOf(startY), 
                    String.valueOf(endX), String.valueOf(endY));
                
                logger.info("滑动操作完成");
                return true;
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
                
                // 在Android平台上，导航操作可能需要启动特定应用或Activity
                // 这里使用Intent打开URL
                androidDevice.executeAdbCommand("shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", url);
                
                return true;
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
                
                // 在Android平台上，等待页面加载可能需要等待特定Activity启动
                // 这里简单等待一段时间
                Thread.sleep(timeout);
                
                return true;
            } catch (Exception e) {
                logger.error("等待页面加载失败", e);
                return false;
            }
        });
    }
    
    public void navigateTo(String urlOrPage) {
        logger.warn("NavigateTo操作在Android平台上不支持");
    }
    
    public void takeScreenshot(String fileName) {
        try {
            logger.info("截图保存到: {}", fileName);
            // 使用AndroidDevice的takeScreenshot方法获取字节数组
            androidDevice.takeScreenshot().thenAccept(bytes -> {
                try {
                    // 将字节数组写入文件
                    java.nio.file.Files.write(java.nio.file.Paths.get(fileName), bytes);
                    logger.info("截图保存成功: {}", fileName);
                } catch (Exception e) {
                    logger.error("保存截图失败", e);
                }
            }).join(); // 等待异步操作完成
        } catch (Exception e) {
            logger.error("截图失败", e);
        }
    }
    
    public void exitApplication() {
        try {
            logger.info("退出Android应用程序");
            // AndroidDevice类没有exitApplication方法，这里我们只关闭连接
            androidDevice.close();
        } catch (Exception e) {
            logger.error("退出Android应用程序失败", e);
        }
    }
    
    @Override
    public void close() {
        exitApplication();
    }
    
    @Override
    public boolean isConnected() {
        return androidDevice != null;
    }
}