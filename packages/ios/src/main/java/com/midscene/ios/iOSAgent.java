package com.midscene.ios;

import com.midscene.core.agent.impl.BaseAgent;
import com.midscene.shared.platform.AgentException;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * iOS平台的Agent实现类
 * 继承BaseAgent，提供iOS平台特有的自动化功能
 */
public class iOSAgent extends BaseAgent {
    private static final Logger logger = Logger.getLogger(iOSAgent.class.getName());
    
    private iOSPlatform platform;
    private String deviceId;
    private String bundleId;
    
    /**
     * 构造函数
     * @param aiModelService AI模型服务
     * @param insightEngine 洞察引擎
     * @param taskExecutor 任务执行器
     */
    public iOSAgent(AIModelService aiModelService, InsightEngine insightEngine, TaskExecutor taskExecutor) {
        super(new iOSPlatform(), aiModelService, insightEngine, taskExecutor);
        this.platform = (iOSPlatform) super.platform;
    }
    
    /**
     * 初始化Agent
     * @param options 初始化选项
     * @return 初始化结果的CompletableFuture
     * @throws AgentException 如果初始化失败
     */
    @Override
    public CompletableFuture<Boolean> initialize(Map<String, Object> options) throws AgentException {
        try {
            logger.info("Initializing iOS Agent...");
            
            // 保存设备ID
            if (options != null && options.containsKey("deviceId")) {
                this.deviceId = (String) options.get("deviceId");
            }
            
            // 保存Bundle ID
            if (options != null && options.containsKey("bundleId")) {
                this.bundleId = (String) options.get("bundleId");
            }
            
            // 初始化平台
            platform.initialize(options).join(); // 使用join等待异步初始化完成
            
            // 如果提供了Bundle ID且需要自动启动应用
            if (bundleId != null && options != null && Boolean.TRUE.equals(options.get("autoLaunchApp"))) {
                launchApp(bundleId);
            }
            
            CompletableFuture<Boolean> result = super.initialize(options);
            logger.info("iOS Agent initialized successfully");
            return result;
        } catch (Exception e) {
            logger.severe("Failed to initialize iOS Agent: " + e.getMessage());
            throw new AgentException("Failed to initialize iOS Agent", e);
        }
    }
    
    /**
     * 启动应用
     * @param bundleId 应用Bundle ID
     * @throws AgentException 如果启动失败
     */
    public void launchApp(String bundleId) throws AgentException {
        try {
            logger.info("Launching app: " + bundleId);
            platform.launchApp(bundleId);
            this.bundleId = bundleId;
            
            // 等待应用启动
            waitForAppToLaunch(bundleId, 5000);
        } catch (Exception e) {
            throw new AgentException("Failed to launch app: " + bundleId, e);
        }
    }
    
    /**
     * 关闭应用
     * @param bundleId 应用Bundle ID
     * @throws AgentException 如果关闭失败
     */
    public void closeApp(String bundleId) throws AgentException {
        try {
            logger.info("Closing app: " + bundleId);
            platform.closeApp(bundleId);
        } catch (Exception e) {
            throw new AgentException("Failed to close app: " + bundleId, e);
        }
    }
    
    /**
     * 获取当前运行的应用Bundle ID
     * @return 当前应用的Bundle ID
     * @throws AgentException 如果获取失败
     */
    public String getCurrentApp() throws AgentException {
        try {
            return platform.getCurrentApp();
        } catch (Exception e) {
            throw new AgentException("Failed to get current app", e);
        }
    }
    
    /**
     * 清除应用数据
     * @param bundleId 应用Bundle ID
     * @throws AgentException 如果清除失败
     */
    public void clearAppData(String bundleId) throws AgentException {
        try {
            logger.info("Clearing data for app: " + bundleId);
            platform.clearAppData(bundleId);
        } catch (Exception e) {
            throw new AgentException("Failed to clear app data: " + bundleId, e);
        }
    }
    
    /**
     * 安装应用
     * @param appPath IPA文件路径
     * @throws AgentException 如果安装失败
     */
    public void installApp(String appPath) throws AgentException {
        try {
            logger.info("Installing app: " + appPath);
            platform.installApp(appPath);
        } catch (Exception e) {
            throw new AgentException("Failed to install app: " + appPath, e);
        }
    }
    
    /**
     * 发送按键事件
     * @param keyName 按键名称
     * @throws AgentException 如果发送失败
     */
    public void pressKey(String keyName) throws AgentException {
        try {
            logger.info("Pressing key: " + keyName);
            platform.pressKey(keyName);
        } catch (Exception e) {
            throw new AgentException("Failed to press key: " + keyName, e);
        }
    }
    
    /**
     * 回到主屏幕
     * @throws AgentException 如果操作失败
     */
    public void goToHomeScreen() throws AgentException {
        try {
            logger.info("Going to home screen");
            platform.pressKey("HOME");
        } catch (Exception e) {
            throw new AgentException("Failed to go to home screen", e);
        }
    }
    
    /**
     * 返回上一页
     * @throws AgentException 如果操作失败
     */
    public void goBack() throws AgentException {
        try {
            logger.info("Going back");
            platform.pressKey("BACK");
        } catch (Exception e) {
            throw new AgentException("Failed to go back", e);
        }
    }
    
    /**
     * 打开最近应用
     * @throws AgentException 如果操作失败
     */
    public void openRecentApps() throws AgentException {
        try {
            logger.info("Opening recent apps");
            platform.pressKey("RECENTS");
        } catch (Exception e) {
            throw new AgentException("Failed to open recent apps", e);
        }
    }
    
    /**
     * 锁定设备
     * @throws AgentException 如果操作失败
     */
    public void lockDevice() throws AgentException {
        try {
            logger.info("Locking device");
            platform.lockDevice();
        } catch (Exception e) {
            throw new AgentException("Failed to lock device", e);
        }
    }
    
    /**
     * 解锁设备
     * @throws AgentException 如果操作失败
     */
    public void unlockDevice() throws AgentException {
        try {
            logger.info("Unlocking device");
            platform.unlockDevice();
        } catch (Exception e) {
            throw new AgentException("Failed to unlock device", e);
        }
    }
    
    /**
     * 通过标签查找元素
     * @param label 元素标签
     * @return 找到的元素
     * @throws AgentException 如果查找失败
     */
    public UiElement findElementByLabel(String label) throws AgentException {
        try {
            logger.info("Finding element by label: " + label);
            Map<String, Object> locator = new HashMap<>();
            locator.put("label", label);
            
            UiContext context = platform.getCurrentUiContext().join(); // 使用join等待异步操作完成
            return platform.findElement(context, locator);
        } catch (Exception e) {
            throw new AgentException("Failed to find element by label: " + label, e);
        }
    }
    
    /**
     * 通过名称查找元素
     * @param name 元素名称
     * @return 找到的元素
     * @throws AgentException 如果查找失败
     */
    public UiElement findElementByName(String name) throws AgentException {
        try {
            logger.info("Finding element by name: " + name);
            Map<String, Object> locator = new HashMap<>();
            locator.put("name", name);
            
            UiContext context = platform.getCurrentUiContext().join(); // 使用join等待异步操作完成
            return platform.findElement(context, locator);
        } catch (Exception e) {
            throw new AgentException("Failed to find element by name: " + name, e);
        }
    }
    
    /**
     * 通过类型查找元素
     * @param type 元素类型
     * @return 找到的元素
     * @throws AgentException 如果查找失败
     */
    public UiElement findElementByType(String type) throws AgentException {
        try {
            logger.info("Finding element by type: " + type);
            Map<String, Object> locator = new HashMap<>();
            locator.put("type", type);
            
            UiContext context = platform.getCurrentUiContext().join(); // 使用join等待异步操作完成
            return platform.findElement(context, locator);
        } catch (Exception e) {
            throw new AgentException("Failed to find element by type: " + type, e);
        }
    }
    
    /**
     * 通过文本查找元素
     * @param text 元素文本
     * @return 找到的元素
     * @throws AgentException 如果查找失败
     */
    public UiElement findElementByText(String text) throws AgentException {
        try {
            logger.info("Finding element by text: " + text);
            Map<String, Object> locator = new HashMap<>();
            locator.put("value", text);
            
            UiContext context = platform.getCurrentUiContext().join(); // 使用join等待异步操作完成
            return platform.findElement(context, locator);
        } catch (Exception e) {
            throw new AgentException("Failed to find element by text: " + text, e);
        }
    }
    
    /**
     * 等待元素出现
     * @param locator 元素定位器
     * @param timeout 超时时间（毫秒）
     * @return 找到的元素
     * @throws AgentException 如果超时或查找失败
     */
    public UiElement waitForElement(Map<String, Object> locator, long timeout) throws AgentException {
        try {
            logger.info("Waiting for element: " + locator);
            long startTime = System.currentTimeMillis();
            UiElement element = null;
            
            while (System.currentTimeMillis() - startTime < timeout) {
                UiContext context = platform.getCurrentUiContext().join(); // 使用join等待异步操作完成
                element = platform.findElement(context, locator);
                if (element != null && element.isVisible()) {
                    return element;
                }
                TimeUnit.MILLISECONDS.sleep(200);
            }
            
            throw new AgentException("Timeout waiting for element: " + locator);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentException("Element wait interrupted", e);
        } catch (AgentException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentException("Failed to wait for element: " + e.getMessage(), e);
        }
    }
    
    /**
     * 等待应用启动
     * @param bundleId 应用Bundle ID
     * @param timeout 超时时间（毫秒）
     * @throws Exception 如果超时或检查失败
     */
    private void waitForAppToLaunch(String bundleId, long timeout) throws Exception {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeout) {
            String currentApp = platform.getCurrentApp();
            if (currentApp != null && currentApp.equals(bundleId)) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(200);
        }
        
        throw new Exception("Timeout waiting for app to launch: " + bundleId);
    }
    
    /**
     * 获取设备信息
     * @return 设备信息Map
     * @throws AgentException 如果获取失败
     */
    public Map<String, String> getDeviceInfo() throws AgentException {
        try {
            Map<String, String> info = new HashMap<>();
            info.put("deviceId", deviceId);
            info.put("iosVersion", platform.getIOSVersion());
            info.put("deviceModel", platform.getDeviceModel());
            info.put("resolution", platform.getDeviceResolution());
            info.put("currentApp", platform.getCurrentApp());
            
            return info;
        } catch (Exception e) {
            throw new AgentException("Failed to get device info", e);
        }
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取平台实例
     * @return iOS平台实例
     */
    @Override
    public PlatformInterface getPlatform() {
        return platform;
    }
    
    /**
     * 关闭Agent
     */
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Closing iOS Agent");
                
                // 关闭平台
                if (platform != null) {
                    platform.shutdown();
                }
                
                super.close();
                logger.info("iOS Agent closed successfully");
            } catch (Exception e) {
                logger.severe("Failed to close iOS Agent: " + e.getMessage());
                throw new CompletionException(new AgentException("Failed to close iOS Agent", e));
            }
        });
    }
}