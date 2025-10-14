package com.midscene.android;

import com.midscene.core.exception.PlatformException;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.shared.platform.PlatformInfo;
import com.midscene.shared.platform.PlatformInfo.PlatformType;
import com.midscene.shared.platform.Point;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.Rectangle;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Android平台实现类
 * 提供基于ADB和UIAutomator的Android平台自动化操作
 */
public class AndroidPlatform implements PlatformInterface {
    private static final Logger logger = Logger.getLogger(AndroidPlatform.class.getName());
    private AdbClient adbClient;
    // 暂时移除UIAutomatorClient引用，因为它未在代码库中定义
    private String deviceId;
    private PlatformInfo platformInfo;
    private boolean initialized = false;
    private AndroidDevice device;
    
    public AndroidPlatform() {
        // 初始化平台信息
        this.platformInfo = new PlatformInfo(
            PlatformType.ANDROID,
            "1.0.0",
            "Android Device",
            "Android"
        );
    }
    
    /**
     * 获取Android设备实例
     * @return AndroidDevice实例
     */
    public AndroidDevice getDevice() {
        return device;
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return device != null ? device.getDeviceId() : null;
    }
    
    @Override
    public CompletableFuture<Boolean> initialize(Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 初始化ADB客户端
                this.adbClient = new AdbClient();
                
                // 获取设备ID
                String deviceId = null;
                if (options != null && options.containsKey("deviceId")) {
                    deviceId = (String) options.get("deviceId");
                } else {
                    // 如果没有指定设备ID，使用第一个连接的设备
                    List<String> devices = adbClient.getConnectedDevices();
                    if (devices.isEmpty()) {
                        throw new PlatformException("No Android devices connected");
                    }
                    deviceId = devices.get(0);
                }
                
                this.deviceId = deviceId;
                
                // 创建并连接Android设备
                this.device = new AndroidDevice(deviceId);
                if (!device.connect()) {
                    throw new PlatformException("Failed to connect to device: " + deviceId);
                }
                
                // 获取设备信息并更新平台信息
                this.platformInfo = device.getPlatformInfo();
                
                initialized = true;
                logger.info("AndroidPlatform initialized successfully for device: " + deviceId);
                return true;
            } catch (Exception e) {
                logger.severe("Failed to initialize AndroidPlatform: " + e.getMessage());
                throw new PlatformException("Failed to initialize AndroidPlatform", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> screenshot() {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] screenshotBytes = device.takeScreenshot();
                return Base64.getEncoder().encodeToString(screenshotBytes);
            } catch (Exception e) {
                logger.severe("Failed to take screenshot: " + e.getMessage());
                throw new PlatformException("Failed to take screenshot", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<UiContext> getCurrentUiContext() {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取截图
                String screenshotBase64 = screenshot().join();
                
                // 提取UI元素
                List<UiElement> elements = extractUiElements();
                
                // 创建UI上下文 - 修改构造器参数以匹配接口定义
                return new UiContext(screenshotBase64, elements, "Android UI Context");
            } catch (Exception e) {
                logger.severe("Failed to get UI context: " + e.getMessage());
                throw new PlatformException("Failed to get UI context", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> tap(ElementLocator elementLocator) {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                UiElement element = findElementByLocator(elementLocator);
                if (element != null) {
                    Rectangle bounds = element.getBounds();
                    int x = bounds.getX() + bounds.getWidth() / 2;
                    int y = bounds.getY() + bounds.getHeight() / 2;
                    device.tap(x, y);
                    return true;
                }
                throw new PlatformException("Element not found: " + elementLocator);
            } catch (Exception e) {
                logger.severe("Failed to tap element: " + elementLocator + ", " + e.getMessage());
                throw new PlatformException("Failed to tap element: " + elementLocator, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> input(ElementLocator elementLocator, String text) {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 首先点击元素使其获得焦点
                tap(elementLocator).join();
                
                // 然后输入文本
                device.inputText(text);
                return true;
            } catch (Exception e) {
                logger.severe("Failed to input text to element: " + elementLocator + ", " + e.getMessage());
                throw new PlatformException("Failed to input text to element: " + elementLocator, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> swipe(Point start, Point end, int duration) {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                device.swipe(start.getX(), start.getY(), end.getX(), end.getY(), duration);
                return true;
            } catch (Exception e) {
                logger.severe("Failed to swipe: " + e.getMessage());
                throw new PlatformException("Failed to swipe", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> getElementProperty(ElementLocator elementLocator, String propertyName) {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                UiElement element = findElementByLocator(elementLocator);
                if (element != null) {
                    Map<String, Object> attributes = element.getAttributes();
                    if (attributes != null && attributes.containsKey(propertyName)) {
                        Object value = attributes.get(propertyName);
                        return value != null ? value.toString() : null;
                    }
                }
                throw new PlatformException("Property not found: " + propertyName + " for element: " + elementLocator);
            } catch (Exception e) {
                logger.severe("Failed to get element property: " + propertyName + ", " + e.getMessage());
                throw new PlatformException("Failed to get element property: " + propertyName, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> elementExists(ElementLocator elementLocator) {
        validateInitialized();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findElementByLocator(elementLocator) != null;
            } catch (Exception e) {
                logger.warning("Error checking element existence: " + e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (device != null && device.isConnected()) {
                    device.disconnect();
                }
                if (adbClient != null) {
                    adbClient.disconnect();
                    adbClient = null;
                }
                initialized = false;
                logger.info("AndroidPlatform closed successfully");
            } catch (Exception e) {
                logger.severe("Error closing AndroidPlatform: " + e.getMessage());
            }
        });
    }
    
    @Override
    public PlatformInfo getPlatformInfo() {
        return platformInfo;
    }
    
    /**
     * 验证平台是否已初始化
     */
    private void validateInitialized() {
        if (!initialized || adbClient == null) {
            throw new PlatformException("AndroidPlatform not initialized");
        }
    }
    
    /**
     * 提取UI元素
     */
    private List<UiElement> extractUiElements() {
        try {
            // 临时返回空列表，因为UIAutomatorClient未定义
            return new ArrayList<>();
        } catch (Exception e) {
            throw new PlatformException("Failed to extract UI elements", e);
        }
    }
    
    /**
     * 通过定位器查找元素
     */
    private UiElement findElementByLocator(ElementLocator elementLocator) {
        try {
            List<UiElement> elements = extractUiElements();
            if (elementLocator.getType() == ElementLocator.LocatorType.ID) {
                String elementId = elementLocator.getValue();
                for (UiElement element : elements) {
                    Map<String, Object> attributes = element.getAttributes();
                    if (attributes != null && attributes.containsKey("resourceId")) {
                        String resourceId = (String) attributes.get("resourceId");
                        if (resourceId != null && (resourceId.equals(elementId) || resourceId.endsWith(":id/" + elementId))) {
                            return element;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger.warning("Failed to find element by locator: " + elementLocator + ", " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 发送按键事件
     */
    public void pressKey(int keyCode) {
        validateInitialized();
        try {
            device.pressKey(keyCode);
        } catch (Exception e) {
            throw new PlatformException("Failed to press key: " + keyCode, e);
        }
    }
    
    /**
     * 启动应用
     */
    public void launchApp(String packageName) {
        validateInitialized();
        try {
            device.launchApp(packageName);
        } catch (Exception e) {
            throw new PlatformException("Failed to launch app: " + packageName, e);
        }
    }
    
    /**
     * 关闭应用
     */
    public void closeApp(String packageName) {
        validateInitialized();
        try {
            device.closeApp(packageName);
        } catch (Exception e) {
            throw new PlatformException("Failed to close app: " + packageName, e);
        }
    }
    
    /**
     * 获取当前运行的应用
     */
    public String getCurrentApp() {
        validateInitialized();
        try {
            return device.getCurrentFocusedPackage();
        } catch (Exception e) {
            throw new PlatformException("Failed to get current app", e);
        }
    }
    
    /**
     * 等待元素出现
     */
    public boolean waitForElement(String elementId, long timeoutMs) {
        validateInitialized();
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                ElementLocator locator = new ElementLocator(ElementLocator.LocatorType.ID, elementId);
                if (findElementByLocator(locator) != null) {
                    return true;
                }
                Thread.sleep(100);
            } catch (Exception e) {
                // 忽略异常，继续等待
            }
        }
        return false;
    }
    
    /**
     * 清除应用数据
     */
    public void clearAppData(String packageName) {
        validateInitialized();
        try {
            device.clearAppData(packageName);
        } catch (Exception e) {
            throw new PlatformException("Failed to clear app data: " + packageName, e);
        }
    }
    
    /**
     * 安装应用
     */
    public void installApp(String apkPath) {
        validateInitialized();
        try {
            device.installApp(apkPath);
        } catch (Exception e) {
            throw new PlatformException("Failed to install app: " + apkPath, e);
        }
    }
    
    /**
     * 卸载应用
     */
    public void uninstallApp(String packageName) {
        validateInitialized();
        try {
            device.uninstallApp(packageName);
        } catch (Exception e) {
            throw new PlatformException("Failed to uninstall app: " + packageName, e);
        }
    }
}