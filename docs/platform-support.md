# Midscene Java 平台支持方案

## 1. 原项目平台支持分析

### 1.1 支持的平台

原项目 `midscene.js` 主要支持以下平台：<mcreference link="https://juejin.cn/post/7462264897654898715" index="1">1</mcreference>

1. **Web平台**: 通过浏览器自动化实现Web应用的UI自动化
2. **Android平台**: 通过ADB控制Android设备实现移动应用的UI自动化

### 1.2 平台实现方式

原项目通过以下方式支持不同平台：

1. **Web平台**:
   - 集成Puppeteer和Playwright等浏览器自动化工具
   - 提供浏览器扩展实现零代码操作
   - 支持Chrome、Firefox、Safari等主流浏览器

2. **Android平台**:
   - 通过ADB命令控制Android设备
   - 支持UIAutomator2框架
   - 支持应用安装、启动、操作等完整流程

## 2. Java平台支持设计

### 2.1 平台抽象层设计

```java
package com.midscene.core.platform;

import com.midscene.core.model.PlatformAction;
import com.midscene.core.model.PlatformElement;
import com.midscene.core.model.PlatformResponse;
import com.midscene.core.model.PlatformException;

/**
 * 平台服务抽象接口
 */
public interface PlatformService {
    /**
     * 初始化平台
     * @param config 平台配置
     * @throws PlatformException 初始化失败时抛出异常
     */
    void initialize(PlatformConfig config) throws PlatformException;
    
    /**
     * 执行平台操作
     * @param action 平台操作
     * @return 平台响应
     * @throws PlatformException 操作失败时抛出异常
     */
    PlatformResponse executeAction(PlatformAction action) throws PlatformException;
    
    /**
     * 查找元素
     * @param selector 元素选择器
     * @return 元素列表
     * @throws PlatformException 查找失败时抛出异常
     */
    List<PlatformElement> findElements(String selector) throws PlatformException;
    
    /**
     * 获取页面截图
     * @return 截图的Base64编码
     * @throws PlatformException 截图失败时抛出异常
     */
    String takeScreenshot() throws PlatformException;
    
    /**
     * 获取页面源码
     * @return 页面源码
     * @throws PlatformException 获取失败时抛出异常
     */
    String getPageSource() throws PlatformException;
    
    /**
     * 等待元素出现
     * @param selector 元素选择器
     * @param timeout 超时时间（毫秒）
     * @return 是否等待成功
     * @throws PlatformException 等待失败时抛出异常
     */
    boolean waitForElement(String selector, long timeout) throws PlatformException;
    
    /**
     * 检查平台是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 关闭平台服务，释放资源
     */
    void close();
}
```

### 2.2 平台配置设计

```java
package com.midscene.core.platform;

import java.util.Map;

/**
 * 平台配置基类
 */
public abstract class PlatformConfig {
    protected String platform;
    protected Map<String, Object> properties;
    
    // 构造函数、getter和setter方法
    
    /**
     * 构建器基类
     */
    public abstract static class Builder<T extends PlatformConfig, B extends Builder<T, B>> {
        protected T config;
        
        protected abstract T getConfig();
        protected abstract B self();
        
        public B platform(String platform) {
            config.platform = platform;
            return self();
        }
        
        public B property(String key, Object value) {
            if (config.properties == null) {
                config.properties = new HashMap<>();
            }
            config.properties.put(key, value);
            return self();
        }
        
        public abstract T build();
    }
}
```

### 2.3 平台操作和响应模型

```java
package com.midscene.core.model;

import java.util.Map;

/**
 * 平台操作模型
 */
public class PlatformAction {
    private String type; // 操作类型：click, input, scroll等
    private String selector; // 元素选择器
    private Map<String, Object> parameters; // 操作参数
    private String description; // 操作描述
    
    // 构造函数、getter和setter方法
    
    /**
     * 构建器
     */
    public static class Builder {
        private PlatformAction action = new PlatformAction();
        
        public Builder type(String type) {
            action.type = type;
            return this;
        }
        
        public Builder selector(String selector) {
            action.selector = selector;
            return this;
        }
        
        public Builder parameter(String key, Object value) {
            if (action.parameters == null) {
                action.parameters = new HashMap<>();
            }
            action.parameters.put(key, value);
            return this;
        }
        
        public Builder description(String description) {
            action.description = description;
            return this;
        }
        
        public PlatformAction build() {
            return action;
        }
    }
}

/**
 * 平台元素模型
 */
public class PlatformElement {
    private String id; // 元素ID
    private String tagName; // 标签名
    private String text; // 文本内容
    private Map<String, String> attributes; // 属性
    private Map<String, Object> properties; // 属性
    private Rectangle bounds; // 元素边界
    
    // 构造函数、getter和setter方法
    
    /**
     * 元素边界模型
     */
    public static class Rectangle {
        private int x;
        private int y;
        private int width;
        private int height;
        
        // 构造函数、getter和setter方法
    }
}

/**
 * 平台响应模型
 */
public class PlatformResponse {
    private boolean success; // 是否成功
    private String message; // 响应消息
    private Map<String, Object> data; // 响应数据
    private String screenshot; // 截图
    private String pageSource; // 页面源码
    
    // 构造函数、getter和setter方法
    
    /**
     * 构建器
     */
    public static class Builder {
        private PlatformResponse response = new PlatformResponse();
        
        public Builder success(boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            response.data = data;
            return this;
        }
        
        public Builder screenshot(String screenshot) {
            response.screenshot = screenshot;
            return this;
        }
        
        public Builder pageSource(String pageSource) {
            response.pageSource = pageSource;
            return this;
        }
        
        public PlatformResponse build() {
            return response;
        }
    }
}
```

## 3. Web平台支持

### 3.1 Web平台配置

```java
package com.midscene.core.platform.web;

import com.midscene.core.platform.PlatformConfig;

/**
 * Web平台配置
 */
public class WebPlatformConfig extends PlatformConfig {
    private static final String DEFAULT_PLATFORM = "web";
    
    private String browserType = "chromium"; // 浏览器类型：chromium, firefox, webkit
    private boolean headless = true; // 是否无头模式
    private int viewportWidth = 1280; // 视口宽度
    private int viewportHeight = 720; // 视口高度
    private String userAgent; // 用户代理
    private boolean ignoreHTTPSErrors = true; // 是否忽略HTTPS错误
    private String browserExecutablePath; // 浏览器可执行文件路径
    private Map<String, String> headers; // 请求头
    private boolean enableJavaScript = true; // 是否启用JavaScript
    private long defaultTimeout = 30000; // 默认超时时间（毫秒）
    
    // 构造函数
    public WebPlatformConfig() {
        super.platform = DEFAULT_PLATFORM;
    }
    
    // getter和setter方法
    
    /**
     * Web平台配置构建器
     */
    public static class Builder extends PlatformConfig.Builder<WebPlatformConfig, Builder> {
        private WebPlatformConfig config = new WebPlatformConfig();
        
        @Override
        protected WebPlatformConfig getConfig() {
            return config;
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public Builder browserType(String browserType) {
            config.browserType = browserType;
            return this;
        }
        
        public Builder headless(boolean headless) {
            config.headless = headless;
            return this;
        }
        
        public Builder viewport(int width, int height) {
            config.viewportWidth = width;
            config.viewportHeight = height;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            config.userAgent = userAgent;
            return this;
        }
        
        public Builder ignoreHTTPSErrors(boolean ignoreHTTPSErrors) {
            config.ignoreHTTPSErrors = ignoreHTTPSErrors;
            return this;
        }
        
        public Builder browserExecutablePath(String browserExecutablePath) {
            config.browserExecutablePath = browserExecutablePath;
            return this;
        }
        
        public Builder header(String key, String value) {
            if (config.headers == null) {
                config.headers = new HashMap<>();
            }
            config.headers.put(key, value);
            return this;
        }
        
        public Builder enableJavaScript(boolean enableJavaScript) {
            config.enableJavaScript = enableJavaScript;
            return this;
        }
        
        public Builder defaultTimeout(long defaultTimeout) {
            config.defaultTimeout = defaultTimeout;
            return this;
        }
        
        @Override
        public WebPlatformConfig build() {
            return config;
        }
    }
}
```

### 3.2 Web平台服务实现

```java
package com.midscene.core.platform.web;

import com.midscene.core.platform.PlatformService;
import com.midscene.core.platform.PlatformConfig;
import com.midscene.core.model.PlatformAction;
import com.midscene.core.model.PlatformElement;
import com.midscene.core.model.PlatformResponse;
import com.midscene.core.model.PlatformException;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Web平台服务实现
 */
public class WebPlatformService implements PlatformService {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private WebPlatformConfig config;
    
    @Override
    public void initialize(PlatformConfig config) throws PlatformException {
        if (!(config instanceof WebPlatformConfig)) {
            throw new PlatformException("Invalid config type for Web platform");
        }
        
        this.config = (WebPlatformConfig) config;
        
        try {
            // 初始化Playwright
            playwright = Playwright.create();
            
            // 创建浏览器
            BrowserType browserType;
            switch (this.config.getBrowserType().toLowerCase()) {
                case "chromium":
                    browserType = playwright.chromium();
                    break;
                case "firefox":
                    browserType = playwright.firefox();
                    break;
                case "webkit":
                    browserType = playwright.webkit();
                    break;
                default:
                    throw new PlatformException("Unsupported browser type: " + this.config.getBrowserType());
            }
            
            // 配置浏览器选项
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(this.config.isHeadless())
                .setIgnoreHTTPSErrors(this.config.isIgnoreHTTPSErrors());
            
            if (this.config.getBrowserExecutablePath() != null) {
                launchOptions.setExecutablePath(this.config.getBrowserExecutablePath());
            }
            
            browser = browserType.launch(launchOptions);
            
            // 创建浏览器上下文
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(this.config.getViewportWidth(), this.config.getViewportHeight())
                .setJavaScriptEnabled(this.config.isEnableJavaScript());
            
            if (this.config.getUserAgent() != null) {
                contextOptions.setUserAgent(this.config.getUserAgent());
            }
            
            if (this.config.getHeaders() != null) {
                contextOptions.setExtraHTTPHeaders(this.config.getHeaders());
            }
            
            context = browser.newContext(contextOptions);
            
            // 创建页面
            page = context.newPage();
            
            // 设置默认超时
            page.setDefaultTimeout(this.config.getDefaultTimeout());
            
        } catch (Exception e) {
            throw new PlatformException("Failed to initialize Web platform", e);
        }
    }
    
    @Override
    public PlatformResponse executeAction(PlatformAction action) throws PlatformException {
        try {
            switch (action.getType().toLowerCase()) {
                case "navigate":
                    return navigate(action);
                case "click":
                    return click(action);
                case "input":
                    return input(action);
                case "scroll":
                    return scroll(action);
                case "hover":
                    return hover(action);
                case "select":
                    return select(action);
                case "check":
                    return check(action);
                case "uncheck":
                    return uncheck(action);
                case "wait":
                    return wait(action);
                default:
                    throw new PlatformException("Unsupported action type: " + action.getType());
            }
        } catch (Exception e) {
            throw new PlatformException("Failed to execute action: " + action.getType(), e);
        }
    }
    
    @Override
    public List<PlatformElement> findElements(String selector) throws PlatformException {
        try {
            List<Locator> locators = page.locator(selector).all();
            List<PlatformElement> elements = new ArrayList<>();
            
            for (Locator locator : locators) {
                PlatformElement element = new PlatformElement();
                
                // 获取元素属性
                element.setId(locator.getAttribute("id"));
                element.setTagName(locator.getAttribute("tagName"));
                element.setText(locator.textContent());
                
                // 获取元素边界
                BoundingBox boundingBox = locator.boundingBox();
                if (boundingBox != null) {
                    PlatformElement.Rectangle bounds = new PlatformElement.Rectangle();
                    bounds.setX((int) boundingBox.x);
                    bounds.setY((int) boundingBox.y);
                    bounds.setWidth((int) boundingBox.width);
                    bounds.setHeight((int) boundingBox.height);
                    element.setBounds(bounds);
                }
                
                elements.add(element);
            }
            
            return elements;
        } catch (Exception e) {
            throw new PlatformException("Failed to find elements: " + selector, e);
        }
    }
    
    @Override
    public String takeScreenshot() throws PlatformException {
        try {
            return page.screenshot(new Page.ScreenshotOptions().setFullPage(true)).toString("base64");
        } catch (Exception e) {
            throw new PlatformException("Failed to take screenshot", e);
        }
    }
    
    @Override
    public String getPageSource() throws PlatformException {
        try {
            return page.content();
        } catch (Exception e) {
            throw new PlatformException("Failed to get page source", e);
        }
    }
    
    @Override
    public boolean waitForElement(String selector, long timeout) throws PlatformException {
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setTimeout(timeout)
                .setState(WaitForSelectorState.VISIBLE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return page != null && !page.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
    
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
        } catch (Exception e) {
            // 忽略关闭时的异常
        }
    }
    
    /**
     * 导航到指定URL
     */
    private PlatformResponse navigate(PlatformAction action) throws PlatformException {
        String url = (String) action.getParameters().get("url");
        if (url == null) {
            throw new PlatformException("URL is required for navigate action");
        }
        
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Navigated to: " + url)
            .build();
    }
    
    /**
     * 点击元素
     */
    private PlatformResponse click(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        if (selector == null) {
            throw new PlatformException("Selector is required for click action");
        }
        
        page.click(selector);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Clicked element: " + selector)
            .build();
    }
    
    /**
     * 输入文本
     */
    private PlatformResponse input(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        String text = (String) action.getParameters().get("text");
        
        if (selector == null) {
            throw new PlatformException("Selector is required for input action");
        }
        
        if (text == null) {
            throw new PlatformException("Text is required for input action");
        }
        
        page.fill(selector, text);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Input text to element: " + selector)
            .build();
    }
    
    /**
     * 滚动页面
     */
    private PlatformResponse scroll(PlatformAction action) throws PlatformException {
        String direction = (String) action.getParameters().get("direction");
        Integer pixels = (Integer) action.getParameters().get("pixels");
        
        if ("down".equals(direction)) {
            if (pixels != null) {
                page.mouse().wheel(0, pixels);
            } else {
                page.mouse().wheel(0, 500);
            }
        } else if ("up".equals(direction)) {
            if (pixels != null) {
                page.mouse().wheel(0, -pixels);
            } else {
                page.mouse().wheel(0, -500);
            }
        } else if ("left".equals(direction)) {
            if (pixels != null) {
                page.mouse().wheel(-pixels, 0);
            } else {
                page.mouse().wheel(-500, 0);
            }
        } else if ("right".equals(direction)) {
            if (pixels != null) {
                page.mouse().wheel(pixels, 0);
            } else {
                page.mouse().wheel(500, 0);
            }
        }
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Scrolled page: " + direction)
            .build();
    }
    
    /**
     * 悬停元素
     */
    private PlatformResponse hover(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        if (selector == null) {
            throw new PlatformException("Selector is required for hover action");
        }
        
        page.hover(selector);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Hovered element: " + selector)
            .build();
    }
    
    /**
     * 选择下拉框选项
     */
    private PlatformResponse select(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        String value = (String) action.getParameters().get("value");
        
        if (selector == null) {
            throw new PlatformException("Selector is required for select action");
        }
        
        if (value == null) {
            throw new PlatformException("Value is required for select action");
        }
        
        page.selectOption(selector, value);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Selected option for element: " + selector)
            .build();
    }
    
    /**
     * 勾选复选框
     */
    private PlatformResponse check(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        if (selector == null) {
            throw new PlatformException("Selector is required for check action");
        }
        
        page.check(selector);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Checked element: " + selector)
            .build();
    }
    
    /**
     * 取消勾选复选框
     */
    private PlatformResponse uncheck(PlatformAction action) throws PlatformException {
        String selector = action.getSelector();
        if (selector == null) {
            throw new PlatformException("Selector is required for uncheck action");
        }
        
        page.uncheck(selector);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Unchecked element: " + selector)
            .build();
    }
    
    /**
     * 等待
     */
    private PlatformResponse wait(PlatformAction action) throws PlatformException {
        Integer milliseconds = (Integer) action.getParameters().get("milliseconds");
        if (milliseconds == null) {
            milliseconds = 1000; // 默认等待1秒
        }
        
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlatformException("Wait interrupted", e);
        }
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Waited for " + milliseconds + " milliseconds")
            .build();
    }
}
```

## 4. Android平台支持

### 4.1 Android平台配置

```java
package com.midscene.core.platform.android;

import com.midscene.core.platform.PlatformConfig;

/**
 * Android平台配置
 */
public class AndroidPlatformConfig extends PlatformConfig {
    private static final String DEFAULT_PLATFORM = "android";
    
    private String deviceId; // 设备ID
    private String packageName; // 应用包名
    private String activityName; // Activity名称
    private String adbPath = "adb"; // ADB路径
    private long defaultTimeout = 30000; // 默认超时时间（毫秒）
    private boolean autoGrantPermissions = true; // 是否自动授予权限
    private boolean clearDataBeforeLaunch = false; // 启动前是否清除数据
    private String appPath; // 应用安装包路径
    private boolean installApp = false; // 是否安装应用
    
    // 构造函数
    public AndroidPlatformConfig() {
        super.platform = DEFAULT_PLATFORM;
    }
    
    // getter和setter方法
    
    /**
     * Android平台配置构建器
     */
    public static class Builder extends PlatformConfig.Builder<AndroidPlatformConfig, Builder> {
        private AndroidPlatformConfig config = new AndroidPlatformConfig();
        
        @Override
        protected AndroidPlatformConfig getConfig() {
            return config;
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public Builder deviceId(String deviceId) {
            config.deviceId = deviceId;
            return this;
        }
        
        public Builder packageName(String packageName) {
            config.packageName = packageName;
            return this;
        }
        
        public Builder activityName(String activityName) {
            config.activityName = activityName;
            return this;
        }
        
        public Builder adbPath(String adbPath) {
            config.adbPath = adbPath;
            return this;
        }
        
        public Builder defaultTimeout(long defaultTimeout) {
            config.defaultTimeout = defaultTimeout;
            return this;
        }
        
        public Builder autoGrantPermissions(boolean autoGrantPermissions) {
            config.autoGrantPermissions = autoGrantPermissions;
            return this;
        }
        
        public Builder clearDataBeforeLaunch(boolean clearDataBeforeLaunch) {
            config.clearDataBeforeLaunch = clearDataBeforeLaunch;
            return this;
        }
        
        public Builder appPath(String appPath) {
            config.appPath = appPath;
            return this;
        }
        
        public Builder installApp(boolean installApp) {
            config.installApp = installApp;
            return this;
        }
        
        @Override
        public AndroidPlatformConfig build() {
            return config;
        }
    }
}
```

### 4.2 Android平台服务实现

```java
package com.midscene.core.platform.android;

import com.midscene.core.platform.PlatformService;
import com.midscene.core.platform.PlatformConfig;
import com.midscene.core.model.PlatformAction;
import com.midscene.core.model.PlatformElement;
import com.midscene.core.model.PlatformResponse;
import com.midscene.core.model.PlatformException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Android平台服务实现
 */
public class AndroidPlatformService implements PlatformService {
    private AndroidPlatformConfig config;
    private String deviceId;
    
    @Override
    public void initialize(PlatformConfig config) throws PlatformException {
        if (!(config instanceof AndroidPlatformConfig)) {
            throw new PlatformException("Invalid config type for Android platform");
        }
        
        this.config = (AndroidPlatformConfig) config;
        
        try {
            // 检查ADB连接
            checkAdbConnection();
            
            // 获取设备ID
            if (this.config.getDeviceId() != null) {
                this.deviceId = this.config.getDeviceId();
            } else {
                this.deviceId = getFirstDevice();
            }
            
            // 安装应用（如果需要）
            if (this.config.isInstallApp() && this.config.getAppPath() != null) {
                installApp(this.config.getAppPath());
            }
            
            // 启动应用
            if (this.config.getPackageName() != null) {
                launchApp();
            }
            
        } catch (Exception e) {
            throw new PlatformException("Failed to initialize Android platform", e);
        }
    }
    
    @Override
    public PlatformResponse executeAction(PlatformAction action) throws PlatformException {
        try {
            switch (action.getType().toLowerCase()) {
                case "tap":
                    return tap(action);
                case "input":
                    return input(action);
                case "swipe":
                    return swipe(action);
                case "longpress":
                    return longPress(action);
                case "back":
                    return back(action);
                case "home":
                    return home(action);
                case "recent":
                    return recent(action);
                case "launch":
                    return launch(action);
                case "close":
                    return close(action);
                case "wait":
                    return wait(action);
                default:
                    throw new PlatformException("Unsupported action type: " + action.getType());
            }
        } catch (Exception e) {
            throw new PlatformException("Failed to execute action: " + action.getType(), e);
        }
    }
    
    @Override
    public List<PlatformElement> findElements(String selector) throws PlatformException {
        try {
            // 使用UIAutomator2查找元素
            String command = String.format("%s -s %s shell uiautomator dump", config.getAdbPath(), deviceId);
            String result = executeCommand(command);
            
            if (!result.contains("UI hierchary dumped")) {
                throw new PlatformException("Failed to dump UI hierarchy");
            }
            
            // 获取UI层次结构
            command = String.format("%s -s %s shell cat /sdcard/window_dump.xml", config.getAdbPath(), deviceId);
            String xml = executeCommand(command);
            
            // 解析XML并查找匹配的元素
            return parseXmlAndFindElements(xml, selector);
        } catch (Exception e) {
            throw new PlatformException("Failed to find elements: " + selector, e);
        }
    }
    
    @Override
    public String takeScreenshot() throws PlatformException {
        try {
            // 截屏
            String command = String.format("%s -s %s shell screencap -p /sdcard/screenshot.png", config.getAdbPath(), deviceId);
            executeCommand(command);
            
            // 获取截图
            command = String.format("%s -s %s shell cat /sdcard/screenshot.png", config.getAdbPath(), deviceId);
            byte[] screenshotBytes = executeCommandAndGetOutput(command);
            
            // 转换为Base64
            return Base64.getEncoder().encodeToString(screenshotBytes);
        } catch (Exception e) {
            throw new PlatformException("Failed to take screenshot", e);
        }
    }
    
    @Override
    public String getPageSource() throws PlatformException {
        try {
            // 获取UI层次结构
            String command = String.format("%s -s %s shell uiautomator dump", config.getAdbPath(), deviceId);
            String result = executeCommand(command);
            
            if (!result.contains("UI hierchary dumped")) {
                throw new PlatformException("Failed to dump UI hierarchy");
            }
            
            // 获取UI层次结构XML
            command = String.format("%s -s %s shell cat /sdcard/window_dump.xml", config.getAdbPath(), deviceId);
            return executeCommand(command);
        } catch (Exception e) {
            throw new PlatformException("Failed to get page source", e);
        }
    }
    
    @Override
    public boolean waitForElement(String selector, long timeout) throws PlatformException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                List<PlatformElement> elements = findElements(selector);
                if (!elements.isEmpty()) {
                    return true;
                }
                Thread.sleep(1000); // 每秒检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                // 忽略异常，继续等待
            }
        }
        
        return false;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            String command = String.format("%s -s %s shell echo 'test'", config.getAdbPath(), deviceId);
            String result = executeCommand(command);
            return result.contains("test");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void close() {
        // 清理资源
    }
    
    /**
     * 检查ADB连接
     */
    private void checkAdbConnection() throws PlatformException {
        try {
            String command = config.getAdbPath() + " version";
            String result = executeCommand(command);
            
            if (!result.contains("Android Debug Bridge")) {
                throw new PlatformException("ADB not found or not working");
            }
        } catch (Exception e) {
            throw new PlatformException("Failed to check ADB connection", e);
        }
    }
    
    /**
     * 获取第一个设备ID
     */
    private String getFirstDevice() throws PlatformException {
        try {
            String command = config.getAdbPath() + " devices";
            String result = executeCommand(command);
            
            // 解析设备列表
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.contains("\tdevice")) {
                    String[] parts = line.split("\t");
                    if (parts.length >= 2) {
                        return parts[0];
                    }
                }
            }
            
            throw new PlatformException("No Android device found");
        } catch (Exception e) {
            throw new PlatformException("Failed to get device ID", e);
        }
    }
    
    /**
     * 安装应用
     */
    private void installApp(String appPath) throws PlatformException {
        try {
            String command = String.format("%s -s %s install %s", config.getAdbPath(), deviceId, appPath);
            if (config.isAutoGrantPermissions()) {
                command += " -g";
            }
            
            String result = executeCommand(command);
            
            if (!result.contains("Success")) {
                throw new PlatformException("Failed to install app: " + result);
            }
        } catch (Exception e) {
            throw new PlatformException("Failed to install app", e);
        }
    }
    
    /**
     * 启动应用
     */
    private void launchApp() throws PlatformException {
        try {
            // 清除应用数据（如果需要）
            if (config.isClearDataBeforeLaunch()) {
                String command = String.format("%s -s %s shell pm clear %s", config.getAdbPath(), deviceId, config.getPackageName());
                executeCommand(command);
            }
            
            // 启动应用
            String command;
            if (config.getActivityName() != null) {
                command = String.format("%s -s %s shell am start -n %s/%s", 
                    config.getAdbPath(), deviceId, config.getPackageName(), config.getActivityName());
            } else {
                command = String.format("%s -s %s shell monkey -p %s -c android.intent.category.LAUNCHER 1", 
                    config.getAdbPath(), deviceId, config.getPackageName());
            }
            
            String result = executeCommand(command);
            
            if (result.contains("Error")) {
                throw new PlatformException("Failed to launch app: " + result);
            }
        } catch (Exception e) {
            throw new PlatformException("Failed to launch app", e);
        }
    }
    
    /**
     * 点击
     */
    private PlatformResponse tap(PlatformAction action) throws PlatformException {
        Integer x = (Integer) action.getParameters().get("x");
        Integer y = (Integer) action.getParameters().get("y");
        
        if (x == null || y == null) {
            throw new PlatformException("X and Y coordinates are required for tap action");
        }
        
        String command = String.format("%s -s %s shell input tap %d %d", config.getAdbPath(), deviceId, x, y);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message(String.format("Tapped at (%d, %d)", x, y))
            .build();
    }
    
    /**
     * 输入文本
     */
    private PlatformResponse input(PlatformAction action) throws PlatformException {
        String text = (String) action.getParameters().get("text");
        
        if (text == null) {
            throw new PlatformException("Text is required for input action");
        }
        
        String command = String.format("%s -s %s shell input text '%s'", config.getAdbPath(), deviceId, text);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Input text: " + text)
            .build();
    }
    
    /**
     * 滑动
     */
    private PlatformResponse swipe(PlatformAction action) throws PlatformException {
        Integer startX = (Integer) action.getParameters().get("startX");
        Integer startY = (Integer) action.getParameters().get("startY");
        Integer endX = (Integer) action.getParameters().get("endX");
        Integer endY = (Integer) action.getParameters().get("endY");
        Integer duration = (Integer) action.getParameters().get("duration");
        
        if (startX == null || startY == null || endX == null || endY == null) {
            throw new PlatformException("Start and end coordinates are required for swipe action");
        }
        
        if (duration == null) {
            duration = 300; // 默认300ms
        }
        
        String command = String.format("%s -s %s shell input swipe %d %d %d %d %d", 
            config.getAdbPath(), deviceId, startX, startY, endX, endY, duration);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message(String.format("Swiped from (%d, %d) to (%d, %d)", startX, startY, endX, endY))
            .build();
    }
    
    /**
     * 长按
     */
    private PlatformResponse longPress(PlatformAction action) throws PlatformException {
        Integer x = (Integer) action.getParameters().get("x");
        Integer y = (Integer) action.getParameters().get("y");
        Integer duration = (Integer) action.getParameters().get("duration");
        
        if (x == null || y == null) {
            throw new PlatformException("X and Y coordinates are required for longPress action");
        }
        
        if (duration == null) {
            duration = 1000; // 默认1秒
        }
        
        // 长按可以通过swipe实现，起点和终点相同
        String command = String.format("%s -s %s shell input swipe %d %d %d %d %d", 
            config.getAdbPath(), deviceId, x, y, x, y, duration);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message(String.format("Long pressed at (%d, %d) for %d ms", x, y, duration))
            .build();
    }
    
    /**
     * 返回
     */
    private PlatformResponse back(PlatformAction action) throws PlatformException {
        String command = String.format("%s -s %s shell input keyevent KEYCODE_BACK", config.getAdbPath(), deviceId);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Pressed back key")
            .build();
    }
    
    /**
     * 主屏幕
     */
    private PlatformResponse home(PlatformAction action) throws PlatformException {
        String command = String.format("%s -s %s shell input keyevent KEYCODE_HOME", config.getAdbPath(), deviceId);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Pressed home key")
            .build();
    }
    
    /**
     * 最近应用
     */
    private PlatformResponse recent(PlatformAction action) throws PlatformException {
        String command = String.format("%s -s %s shell input keyevent KEYCODE_APP_SWITCH", config.getAdbPath(), deviceId);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Pressed recent apps key")
            .build();
    }
    
    /**
     * 启动应用
     */
    private PlatformResponse launch(PlatformAction action) throws PlatformException {
        String packageName = (String) action.getParameters().get("packageName");
        String activityName = (String) action.getParameters().get("activityName");
        
        if (packageName == null) {
            packageName = config.getPackageName();
        }
        
        if (packageName == null) {
            throw new PlatformException("Package name is required for launch action");
        }
        
        String command;
        if (activityName != null) {
            command = String.format("%s -s %s shell am start -n %s/%s", 
                config.getAdbPath(), deviceId, packageName, activityName);
        } else {
            command = String.format("%s -s %s shell monkey -p %s -c android.intent.category.LAUNCHER 1", 
                config.getAdbPath(), deviceId, packageName);
        }
        
        String result = executeCommand(command);
        
        if (result.contains("Error")) {
            throw new PlatformException("Failed to launch app: " + result);
        }
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Launched app: " + packageName)
            .build();
    }
    
    /**
     * 关闭应用
     */
    private PlatformResponse close(PlatformAction action) throws PlatformException {
        String packageName = (String) action.getParameters().get("packageName");
        
        if (packageName == null) {
            packageName = config.getPackageName();
        }
        
        if (packageName == null) {
            throw new PlatformException("Package name is required for close action");
        }
        
        String command = String.format("%s -s %s shell am force-stop %s", config.getAdbPath(), deviceId, packageName);
        executeCommand(command);
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Closed app: " + packageName)
            .build();
    }
    
    /**
     * 等待
     */
    private PlatformResponse wait(PlatformAction action) throws PlatformException {
        Integer milliseconds = (Integer) action.getParameters().get("milliseconds");
        if (milliseconds == null) {
            milliseconds = 1000; // 默认等待1秒
        }
        
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlatformException("Wait interrupted", e);
        }
        
        return new PlatformResponse.Builder()
            .success(true)
            .message("Waited for " + milliseconds + " milliseconds")
            .build();
    }
    
    /**
     * 执行命令并返回输出
     */
    private String executeCommand(String command) throws PlatformException {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new PlatformException("Command failed with exit code " + exitCode + ": " + output.toString());
            }
            
            return output.toString();
        } catch (Exception e) {
            throw new PlatformException("Failed to execute command: " + command, e);
        }
    }
    
    /**
     * 执行命令并返回二进制输出
     */
    private byte[] executeCommandAndGetOutput(String command) throws PlatformException {
        try {
            Process process = Runtime.getRuntime().exec(command);
            return process.getInputStream().readAllBytes();
        } catch (Exception e) {
            throw new PlatformException("Failed to execute command: " + command, e);
        }
    }
    
    /**
     * 解析XML并查找匹配的元素
     */
    private List<PlatformElement> parseXmlAndFindElements(String xml, String selector) throws PlatformException {
        // 这里简化实现，实际应该使用XML解析器
        List<PlatformElement> elements = new ArrayList<>();
        
        // 简单的文本匹配
        if (selector.startsWith("text=")) {
            String text = selector.substring(5);
            Pattern pattern = Pattern.compile("text=\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(xml);
            
            while (matcher.find()) {
                if (matcher.group(1).contains(text)) {
                    PlatformElement element = new PlatformElement();
                    element.setText(matcher.group(1));
                    elements.add(element);
                }
            }
        }
        
        return elements;
    }
}
```

## 5. 平台工厂和管理

### 5.1 平台工厂

```java
package com.midscene.core.platform;

import com.midscene.core.platform.web.WebPlatformConfig;
import com.midscene.core.platform.web.WebPlatformService;
import com.midscene.core.platform.android.AndroidPlatformConfig;
import com.midscene.core.platform.android.AndroidPlatformService;

/**
 * 平台服务工厂
 */
public class PlatformServiceFactory {
    
    /**
     * 根据配置创建平台服务
     * @param config 平台配置
     * @return 平台服务
     */
    public static PlatformService create(PlatformConfig config) {
        if (config instanceof WebPlatformConfig) {
            return new WebPlatformService();
        } else if (config instanceof AndroidPlatformConfig) {
            return new AndroidPlatformService();
        } else {
            throw new IllegalArgumentException("Unsupported platform config: " + config.getClass());
        }
    }
    
    /**
     * 根据平台名称创建默认配置的平台服务
     * @param platformName 平台名称
     * @return 平台服务
     */
    public static PlatformService create(String platformName) {
        switch (platformName.toLowerCase()) {
            case "web":
                WebPlatformConfig webConfig = new WebPlatformConfig.Builder().build();
                WebPlatformService webService = new WebPlatformService();
                try {
                    webService.initialize(webConfig);
                } catch (PlatformException e) {
                    throw new RuntimeException("Failed to initialize Web platform", e);
                }
                return webService;
                
            case "android":
                AndroidPlatformConfig androidConfig = new AndroidPlatformConfig.Builder().build();
                AndroidPlatformService androidService = new AndroidPlatformService();
                try {
                    androidService.initialize(androidConfig);
                } catch (PlatformException e) {
                    throw new RuntimeException("Failed to initialize Android platform", e);
                }
                return androidService;
                
            default:
                throw new IllegalArgumentException("Unsupported platform name: " + platformName);
        }
    }
}
```

### 5.2 平台管理器

```java
package com.midscene.core.platform;

import com.midscene.core.model.PlatformException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 平台管理器
 */
public class PlatformManager {
    private static PlatformManager instance;
    private final List<PlatformService> platforms;
    private final Map<String, PlatformService> platformMap;
    private PlatformService defaultPlatform;
    
    private PlatformManager() {
        this.platforms = new CopyOnWriteArrayList<>();
        this.platformMap = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     * @return 平台管理器实例
     */
    public static synchronized PlatformManager getInstance() {
        if (instance == null) {
            instance = new PlatformManager();
        }
        return instance;
    }
    
    /**
     * 注册平台
     * @param name 平台名称
     * @param platform 平台服务
     */
    public void registerPlatform(String name, PlatformService platform) {
        platforms.add(platform);
        platformMap.put(name, platform);
        
        // 如果是第一个平台，设为默认平台
        if (defaultPlatform == null) {
            defaultPlatform = platform;
        }
    }
    
    /**
     * 获取平台
     * @param name 平台名称
     * @return 平台服务
     */
    public PlatformService getPlatform(String name) {
        return platformMap.get(name);
    }
    
    /**
     * 获取所有平台
     * @return 平台列表
     */
    public List<PlatformService> getAllPlatforms() {
        return new CopyOnWriteArrayList<>(platforms);
    }
    
    /**
     * 获取可用平台
     * @return 可用平台列表
     */
    public List<PlatformService> getAvailablePlatforms() {
        List<PlatformService> availablePlatforms = new CopyOnWriteArrayList<>();
        for (PlatformService platform : platforms) {
            if (platform.isAvailable()) {
                availablePlatforms.add(platform);
            }
        }
        return availablePlatforms;
    }
    
    /**
     * 设置默认平台
     * @param name 平台名称
     */
    public void setDefaultPlatform(String name) {
        PlatformService platform = platformMap.get(name);
        if (platform != null) {
            defaultPlatform = platform;
        } else {
            throw new IllegalArgumentException("Platform not found: " + name);
        }
    }
    
    /**
     * 获取默认平台
     * @return 默认平台
     */
    public PlatformService getDefaultPlatform() {
        return defaultPlatform;
    }
    
    /**
     * 注销平台
     * @param name 平台名称
     */
    public void unregisterPlatform(String name) {
        PlatformService platform = platformMap.remove(name);
        if (platform != null) {
            platforms.remove(platform);
            platform.close();
            
            // 如果注销的是默认平台，重新选择默认平台
            if (platform == defaultPlatform && !platforms.isEmpty()) {
                defaultPlatform = platforms.get(0);
            }
        }
    }
    
    /**
     * 关闭所有平台
     */
    public void closeAllPlatforms() {
        for (PlatformService platform : platforms) {
            platform.close();
        }
        platforms.clear();
        platformMap.clear();
        defaultPlatform = null;
    }
}
```

## 6. 实施计划

### 6.1 第一阶段：平台抽象层实现 (1周)

1. 实现PlatformService接口
2. 实现PlatformConfig基类
3. 实现PlatformAction、PlatformElement和PlatformResponse模型
4. 编写单元测试

### 6.2 第二阶段：Web平台支持 (2周)

1. 实现WebPlatformConfig配置类
2. 实现WebPlatformService服务类
3. 集成Playwright浏览器自动化
4. 实现Web平台操作（导航、点击、输入等）
5. 编写集成测试

### 6.3 第三阶段：Android平台支持 (2周)

1. 实现AndroidPlatformConfig配置类
2. 实现AndroidPlatformService服务类
3. 集成ADB命令控制
4. 实现Android平台操作（点击、滑动、输入等）
5. 编写集成测试

### 6.4 第四阶段：平台管理和工厂 (1周)

1. 实现PlatformServiceFactory工厂类
2. 实现PlatformManager管理器
3. 编写集成测试

## 7. 总结

通过本方案，我们将确保Midscene Java版本能够支持与原项目相同的平台，特别是Web和Android平台。这将使Java版本能够在不同平台上提供与原项目相同的UI自动化能力。同时，通过平台抽象层设计，我们可以轻松扩展支持更多平台，如iOS、桌面应用等。