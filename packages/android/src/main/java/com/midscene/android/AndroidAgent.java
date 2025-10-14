package com.midscene.android;

import com.midscene.core.agent.impl.BaseAgent;
import com.midscene.core.exception.PlatformException;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;
import com.midscene.shared.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Android专用Agent实现
 * 用于Android平台的自动化测试和交互任务
 */
public class AndroidAgent extends BaseAgent {
    private static final Logger logger = Logger.getLogger(AndroidAgent.class.getName());
    private AndroidPlatform androidPlatform;
    private AndroidElementLocator elementLocator;
    
    /**
     * 构造函数
     * @param aiModelService AI模型服务
     * @param insightEngine 洞察引擎
     * @param taskExecutor 任务执行器
     */
    public AndroidAgent(
            AIModelService aiModelService,
            InsightEngine insightEngine,
            TaskExecutor taskExecutor) {
        // 必须先调用super构造函数，然后才能初始化其他字段
        super(new AndroidPlatform(), aiModelService, insightEngine, taskExecutor);
        
        // 保存平台引用，以便后续使用
        this.androidPlatform = (AndroidPlatform) super.getPlatform();
        this.elementLocator = new AndroidElementLocator();
    }
    
    /**
     * 获取UI上下文，带错误处理
     */
    private UiContext getUiContext() {
        try {
            return androidPlatform.getCurrentUiContext().join();
        } catch (Exception e) {
            logger.severe("Failed to get UI context: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 初始化AndroidAgent
     * @param options 初始化选项
     * @return 初始化是否成功
     */
    @Override
    public CompletableFuture<Boolean> initialize(Map<String, Object> options) {
        logger.info("Initializing AndroidAgent");
        
        // 初始化平台
        return androidPlatform.initialize(options)
                .thenCompose(success -> {
                    if (success) {
                        logger.info("AndroidPlatform initialized successfully for device: " + androidPlatform.getDeviceId());
                        
                        // 如果提供了初始应用包名，则启动该应用
                        if (options != null && options.containsKey("initialPackage")) {
                            String initialPackage = (String) options.get("initialPackage");
                            launchApp(initialPackage);
                            logger.info("Launched initial app: " + initialPackage);
                        }
                        
                        return CompletableFuture.completedFuture(true);
                    } else {
                        logger.severe("Failed to initialize AndroidPlatform");
                        return CompletableFuture.completedFuture(false);
                    }
                });
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return androidPlatform.getDeviceId();
    }
    public void launchApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        
        try {
            androidPlatform.launchApp(packageName);
            logger.info("Successfully launched app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to launch app: " + packageName);
            throw new PlatformException("Failed to launch app: " + packageName, e);
        }
    }
    
    /**
     * 关闭Android应用
     * @param packageName 应用包名
     */
    public void closeApp(String packageName) {
        try {
            androidPlatform.closeApp(packageName);
            logger.info("Successfully closed app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to close app: " + packageName);
            throw new PlatformException("Failed to close app: " + packageName, e);
        }
    }
    
    /**
     * 获取当前运行的应用
     * @return 当前应用包名
     */
    public String getCurrentApp() {
        try {
            String packageName = androidPlatform.getCurrentApp();
            logger.info("Current focused app: " + packageName);
            return packageName;
        } catch (Exception e) {
            logger.severe("Failed to get current app");
            throw new PlatformException("Failed to get current app", e);
        }
    }
    
    /**
     * 发送按键事件
     * @param keyCode 按键代码
     */
    public void pressKey(int keyCode) {
        try {
            androidPlatform.pressKey(keyCode);
            logger.info("Pressed key: " + keyCode);
        } catch (Exception e) {
            logger.severe("Failed to press key: " + keyCode);
            throw new PlatformException("Failed to press key: " + keyCode, e);
        }
    }
    
    /**
     * 返回主屏幕
     */
    public void goToHomeScreen() {
        try {
            androidPlatform.pressKey(3); // KEYCODE_HOME
            logger.info("Navigated to home screen");
        } catch (Exception e) {
            logger.severe("Failed to navigate to home screen");
            throw new PlatformException("Failed to navigate to home screen", e);
        }
    }
    
    /**
     * 返回上一步
     */
    public void goBack() {
        try {
            androidPlatform.pressKey(4); // KEYCODE_BACK
            logger.info("Pressed back button");
        } catch (Exception e) {
            logger.severe("Failed to press back button");
            throw new PlatformException("Failed to press back button", e);
        }
    }
    
    /**
     * 打开最近应用
     */
    public void openRecentApps() {
        try {
            androidPlatform.pressKey(187); // KEYCODE_APP_SWITCH
            logger.info("Opened recent apps");
        } catch (Exception e) {
            logger.severe("Failed to open recent apps");
            throw new PlatformException("Failed to open recent apps", e);
        }
    }
    
    /**
     * 等待元素出现
     * @param elementId 元素ID
     * @param timeoutMs 超时时间（毫秒）
     * @return 如果元素在超时时间内出现返回true，否则返回false
     */
    public boolean waitForElement(String elementId, long timeoutMs) {
        logger.info("Waiting for element: " + elementId + " with timeout: " + timeoutMs + "ms");
        boolean found = androidPlatform.waitForElement(elementId, timeoutMs);
        if (found) {
            logger.info("Element found: " + elementId);
        } else {
            logger.warning("Element not found within timeout: " + elementId);
        }
        return found;
    }
    
    /**
     * 清除应用数据
     * @param packageName 应用包名
     */
    public void clearAppData(String packageName) {
        try {
            androidPlatform.clearAppData(packageName);
            logger.info("Cleared app data for: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to clear app data: " + packageName);
            throw new PlatformException("Failed to clear app data: " + packageName, e);
        }
    }
    
    /**
     * 安装应用
     * @param apkPath APK文件路径
     */
    public void installApp(String apkPath) {
        if (apkPath == null || !apkPath.endsWith(".apk")) {
            throw new IllegalArgumentException("Invalid APK path: " + apkPath);
        }
        
        try {
            androidPlatform.installApp(apkPath);
            logger.info("Successfully installed app from: " + apkPath);
        } catch (Exception e) {
            logger.severe("Failed to install app: " + apkPath);
            throw new PlatformException("Failed to install app: " + apkPath, e);
        }
    }
    
    /**
     * 卸载应用
     * @param packageName 应用包名
     */
    public void uninstallApp(String packageName) {
        try {
            androidPlatform.uninstallApp(packageName);
            logger.info("Successfully uninstalled app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to uninstall app: " + packageName);
            throw new PlatformException("Failed to uninstall app: " + packageName, e);
        }
    }
    
    /**
     * 通过资源ID查找元素
     * @param resourceId 资源ID
     * @return 找到的UiElement
     */
    public CompletableFuture<UiElement> findElementByResourceId(String resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UiContext context = getUiContext();
                List<UiElement> elements = context.getElements();
                for (UiElement element : elements) {
                    Map<String, Object> attributes = element.getAttributes();
                    if (attributes != null && attributes.containsKey("resourceId")) {
                        String id = (String) attributes.get("resourceId");
                        if (id != null && (id.equals(resourceId) || id.endsWith(":id/" + resourceId))) {
                            logger.info("Found element by resourceId: " + resourceId);
                            return element;
                        }
                    }
                }
                logger.warning("Element not found by resourceId: " + resourceId);
                return null;
            } catch (Exception e) {
                logger.severe("Error finding element by resourceId: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 通过文本内容查找元素
     * @param text 文本内容
     * @return 找到的UiElement
     */
    public CompletableFuture<UiElement> findElementByText(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UiContext context = getUiContext();
                List<UiElement> elements = context.getElements();
                for (UiElement element : elements) {
                    if (text.equals(element.getText())) {
                        logger.info("Found element by text: " + text);
                        return element;
                    }
                }
                logger.warning("Element not found by text: " + text);
                return null;
            } catch (Exception e) {
                logger.severe("Error finding element by text: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 通过描述查找元素
     * @param contentDesc 内容描述
     * @return 找到的UiElement
     */
    public CompletableFuture<UiElement> findElementByContentDescription(String contentDesc) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UiContext context = getUiContext();
                List<UiElement> elements = context.getElements();
                for (UiElement element : elements) {
                    Map<String, Object> attributes = element.getAttributes();
                    if (attributes != null && attributes.containsKey("contentDescription")) {
                        String desc = (String) attributes.get("contentDescription");
                        if (contentDesc.equals(desc)) {
                            logger.info("Found element by content description: " + contentDesc);
                            return element;
                        }
                    }
                }
                logger.warning("Element not found by content description: " + contentDesc);
                return null;
            } catch (Exception e) {
                logger.severe("Error finding element by content description: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 执行自定义的Android操作
     * @param action 操作名称
     * @param params 操作参数
     * @return 操作结果
     */
    public CompletableFuture<Object> executeCustomAction(String action, Map<String, Object> params) {
        logger.info("Executing custom Android action: " + action);
        // 可以在这里扩展更多自定义操作
        return CompletableFuture.supplyAsync(() -> {
            throw new UnsupportedOperationException("Custom action not implemented: " + action);
        });
    }
    
    /**
     * 关闭Agent
     */
    @Override
    public CompletableFuture<Void> close() {
        logger.info("Closing AndroidAgent");
        return androidPlatform.close()
                .thenRun(() -> logger.info("AndroidAgent closed successfully"));
    }
    
    /**
     * 获取AndroidPlatform实例
     * @return AndroidPlatform实例
     */
    public AndroidPlatform getAndroidPlatform() {
        return androidPlatform;
    }
    
    /**
     * 创建AndroidAgent实例的工厂方法
     * @param aiModelService AI模型服务
     * @param insightEngine 洞察引擎
     * @param taskExecutor 任务执行器
     * @return AndroidAgent实例
     */
    public static AndroidAgent create(
            AIModelService aiModelService,
            InsightEngine insightEngine,
            TaskExecutor taskExecutor) {
        return new AndroidAgent(aiModelService, insightEngine, taskExecutor);
    }
}