package com.midscene.compatibility;

import com.midscene.core.agent.Agent;
import com.midscene.core.agent.PlatformInterface;
import com.midscene.core.model.UiContext;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.Point;
import com.midscene.shared.platform.UiContext as NewUiContext;
import com.midscene.shared.platform.PlatformInterface as NewPlatformInterface;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 兼容性适配器基类
 * 提供原项目与新模块之间的接口转换
 */
public abstract class CompatibilityAdapter {
    
    /**
     * 将原项目的PlatformInterface适配为新的PlatformInterface
     */
    public static NewPlatformInterface adaptOldToNewPlatform(PlatformInterface oldPlatform) {
        return new OldToNewPlatformAdapter(oldPlatform);
    }
    
    /**
     * 将新项目的PlatformInterface适配为原项目的PlatformInterface
     */
    public static PlatformInterface adaptNewToOldPlatform(NewPlatformInterface newPlatform) {
        return new NewToOldPlatformAdapter(newPlatform);
    }
    
    /**
     * 原平台到新平台的适配器实现
     */
    private static class OldToNewPlatformAdapter implements NewPlatformInterface {
        private final PlatformInterface oldPlatform;
        
        public OldToNewPlatformAdapter(PlatformInterface oldPlatform) {
            this.oldPlatform = oldPlatform;
        }
        
        @Override
        public CompletableFuture<Boolean> initialize(Map<String, Object> options) {
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public CompletableFuture<Void> close() {
            oldPlatform.close();
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public CompletableFuture<NewUiContext> getCurrentUiContext() {
            return oldPlatform.getUiContext()
                .thenApply(oldContext -> new NewUiContext(
                    "", // 需要从oldContext中提取截图
                    null, // 需要转换元素列表
                    null  // 平台特定数据
                ));
        }
        
        @Override
        public CompletableFuture<Boolean> tap(ElementLocator elementLocator) {
            // 简化实现，需要根据定位器类型转换
            return oldPlatform.tap(0, 0);
        }
        
        @Override
        public CompletableFuture<Boolean> input(ElementLocator elementLocator, String text) {
            // 简化实现
            return oldPlatform.inputText(text, 0, 0);
        }
        
        @Override
        public CompletableFuture<Boolean> swipe(Point start, Point end, int duration) {
            // 简化实现，需要映射到滚动操作
            return oldPlatform.scroll("down", end.getY() - start.getY());
        }
        
        @Override
        public CompletableFuture<String> screenshot() {
            // 需要实现截图功能
            return CompletableFuture.completedFuture("");
        }
        
        @Override
        public CompletableFuture<String> getElementProperty(ElementLocator elementLocator, String propertyName) {
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public CompletableFuture<Boolean> elementExists(ElementLocator elementLocator) {
            return CompletableFuture.completedFuture(false);
        }
        
        @Override
        public PlatformInfo getPlatformInfo() {
            String platformType = oldPlatform.getInterfaceType();
            PlatformInfo.PlatformType type;
            if (platformType.equals("ANDROID")) {
                type = PlatformInfo.PlatformType.ANDROID;
            } else if (platformType.equals("PLAYWRIGHT") || platformType.equals("WEB")) {
                type = PlatformInfo.PlatformType.WEB;
            } else {
                type = PlatformInfo.PlatformType.IOS;
            }
            return new PlatformInfo(type, "1.0", "Unknown Device", "Unknown OS");
        }
    }
    
    /**
     * 新平台到原平台的适配器实现
     */
    private static class NewToOldPlatformAdapter implements PlatformInterface {
        private final NewPlatformInterface newPlatform;
        
        public NewToOldPlatformAdapter(NewPlatformInterface newPlatform) {
            this.newPlatform = newPlatform;
        }
        
        @Override
        public String getInterfaceType() {
            PlatformInfo info = newPlatform.getPlatformInfo();
            switch (info.getType()) {
                case ANDROID:
                    return "ANDROID";
                case IOS:
                    return "IOS";
                case WEB:
                    return "WEB";
                default:
                    return "UNKNOWN";
            }
        }
        
        @Override
        public CompletableFuture<UiContext> getUiContext() {
            return newPlatform.getCurrentUiContext()
                .thenApply(newContext -> {
                    UiContext oldContext = new UiContext();
                    // 需要转换UI上下文数据
                    return oldContext;
                });
        }
        
        @Override
        public CompletableFuture<Boolean> tap(int x, int y) {
            ElementLocator locator = new ElementLocator(
                ElementLocator.LocatorType.COORDINATES, 
                x + "," + y
            );
            return newPlatform.tap(locator);
        }
        
        @Override
        public CompletableFuture<Boolean> inputText(String text, int x, int y) {
            ElementLocator locator = new ElementLocator(
                ElementLocator.LocatorType.COORDINATES, 
                x + "," + y
            );
            return newPlatform.input(locator, text);
        }
        
        @Override
        public CompletableFuture<Boolean> scroll(String direction, int distance) {
            Point start = new Point(500, 500);
            Point end;
            switch (direction) {
                case "up":
                    end = new Point(500, 500 - distance);
                    break;
                case "down":
                    end = new Point(500, 500 + distance);
                    break;
                case "left":
                    end = new Point(500 - distance, 500);
                    break;
                case "right":
                    end = new Point(500 + distance, 500);
                    break;
                default:
                    end = start;
            }
            return newPlatform.swipe(start, end, 500);
        }
        
        @Override
        public CompletableFuture<Boolean> navigate(String url) {
            // Web平台特定实现
            return CompletableFuture.completedFuture(false);
        }
        
        @Override
        public void navigateTo(String urlOrPage) {
            // 简化实现
        }
        
        @Override
        public CompletableFuture<Boolean> waitForPageLoad(long timeout) {
            return CompletableFuture.completedFuture(true);
        }
        
        @Override
        public void takeScreenshot(String fileName) {
            newPlatform.screenshot().thenAccept(base64 -> {
                // 保存截图实现
            });
        }
        
        @Override
        public void exitApplication() {
            newPlatform.close();
        }
        
        @Override
        public void close() {
            newPlatform.close();
        }
        
        @Override
        public boolean isConnected() {
            return true;
        }
    }
}