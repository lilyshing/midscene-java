# 平台集成

Midscene Java 通过平台集成模块支持多种自动化平台，提供统一的 API 接口，实现跨平台的自动化操作。

## 🏗️ 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
├─────────────────────────────────────────────────────────────┤
│                      Agent 核心控制器                        │
├─────────────────────────────────────────────────────────────┤
│                PlatformInterface 平台抽象层                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Web 平台    │  │ Android 平台 │  │  桌面平台    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                    平台适配器 (Adapters)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Selenium    │  │  Playwright  │  │  ADB/ADBKit  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Chrome Dev  │  │  Appium      │  │  WinAppDriver│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                    基础设施层 (Infrastructure)               │
└─────────────────────────────────────────────────────────────┘
```

## 🌐 支持的平台

### 1. Web 平台
支持主流浏览器和 Web 应用的自动化操作。

#### 支持的浏览器
- Chrome/Chromium
- Firefox
- Safari
- Edge
- WebKit (Headless)

#### 支持的引擎
- Selenium WebDriver
- Playwright
- Chrome DevTools Protocol

#### 特性
- 元素智能定位
- 页面交互操作
- JavaScript 执行
- 网络请求拦截
- 截图和录制

### 2. Android 平台
支持 Android 应用和系统的自动化操作。

#### 支持的版本
- Android 5.0+ (API Level 21+)
- 支持模拟器和真机

#### 支持的引擎
- ADB (Android Debug Bridge)
- UIAutomator
- Appium
- Espresso (计划支持)

#### 特性
- 应用安装和启动
- UI 元素交互
- 系统操作
- 设备管理
- 性能监控

### 3. 桌面平台 (计划中)
支持桌面应用程序的自动化操作。

#### 支持的系统
- Windows
- macOS
- Linux

#### 支持的引擎
- WinAppDriver (Windows)
- Appium (跨平台)
- Sikuli (图像识别)

#### 特性
- 原生应用操作
- 窗口管理
- 系统交互
- 图像识别

## 🔄 快速开始

### Web 平台集成

#### 使用 Selenium
```java
import com.midscene.web.selenium.SeleniumPage;
import com.midscene.web.selenium.SeleniumUIContextProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

// 创建 WebDriver
WebDriverManager.chromedriver().setup();
ChromeOptions options = new ChromeOptions();
WebDriver driver = new ChromeDriver(options);

// 创建页面实例
SeleniumPage page = new SeleniumPage(driver);

// 创建上下文提供者
SeleniumUIContextProvider contextProvider = new SeleniumUIContextProvider(page);

// 创建 Agent
Agent agent = new Agent(contextProvider);

// 执行操作
agent.ai_action("导航到 https://www.example.com").get();
agent.ai_action("点击登录按钮").get();
```

#### 使用 Playwright
```java
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;

// 创建 Playwright 实例
try (Playwright playwright = Playwright.create()) {
    Browser browser = playwright.chromium().launch();
    
    // 创建页面实例
    PlaywrightPage page = new PlaywrightPage(browser.newPage());
    
    // 创建上下文提供者
    PlaywrightUIContextProvider contextProvider = new PlaywrightUIContextProvider(page);
    
    // 创建 Agent
    Agent agent = new Agent(contextProvider);
    
    // 执行操作
    agent.ai_action("导航到 https://www.example.com").get();
    agent.ai_action("点击登录按钮").get();
}
```

### Android 平台集成

#### 使用 ADB
```java
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;

// 创建设备实例
AndroidDevice device = new AndroidDevice();

// 连接设备
device.connect().join();

// 创建上下文提供者
AndroidUIContextProvider contextProvider = new AndroidUIContextProvider(device);

// 创建 Agent
Agent agent = new Agent(contextProvider);

// 执行操作
agent.ai_action("启动设置应用").get();
agent.ai_action("点击 WLAN 设置").get();
agent.ai_action("打开 Wi-Fi 开关").get();

// 断开连接
device.disconnect();
```

#### 使用 Appium
```java
import com.midscene.android.appium.AppiumDevice;
import com.midscene.android.appium.AppiumUIContextProvider;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;

// 创建 Appium 配置
DesiredCapabilities caps = new DesiredCapabilities();
caps.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
caps.setCapability(MobileCapabilityType.APP, "/path/to/app.apk");
caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");

// 创建驱动
AndroidDriver driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), caps);

// 创建设备实例
AppiumDevice device = new AppiumDevice(driver);

// 创建上下文提供者
AppiumUIContextProvider contextProvider = new AppiumUIContextProvider(device);

// 创建 Agent
Agent agent = new Agent(contextProvider);

// 执行操作
agent.ai_action("点击登录按钮").get();
agent.ai_action("输入用户名").get();
```

## 🔧 统一操作接口

### PlatformInterface 抽象类
```java
public abstract class PlatformInterface {
    // 获取当前上下文
    public abstract UIContext getCurrentContext();
    
    // 元素操作
    public abstract CompletableFuture<Void> clickElement(String elementId);
    public abstract CompletableFuture<Void> inputText(String elementId, String text);
    public abstract CompletableFuture<Void> scrollToElement(String elementId);
    
    // 页面操作
    public abstract CompletableFuture<Void> navigate(String url);
    public abstract CompletableFuture<String> getCurrentUrl();
    public abstract CompletableFuture<String> getPageTitle();
    
    // 元素查找
    public abstract CompletableFuture<String> findElement(String description);
    public abstract CompletableFuture<List<String>> findElements(String description);
    
    // 信息提取
    public abstract CompletableFuture<String> getElementText(String elementId);
    public abstract CompletableFuture<Map<String, String>> getElementAttributes(String elementId);
    
    // 状态验证
    public abstract CompletableFuture<Boolean> isElementVisible(String elementId);
    public abstract CompletableFuture<Boolean> isElementEnabled(String elementId);
    
    // 截图操作
    public abstract CompletableFuture<byte[]> takeScreenshot();
    
    // 资源管理
    public abstract void close();
}
```

### UIContext 接口
```java
public interface UIContext {
    // 上下文信息
    String getContextType();
    Map<String, Object> getContextData();
    
    // 元素操作
    void click(String selector);
    void input(String selector, String text);
    void select(String selector, String value);
    
    // 页面操作
    void navigate(String url);
    void back();
    void refresh();
    
    // 状态查询
    boolean isVisible(String selector);
    boolean isEnabled(String selector);
    String getText(String selector);
    
    // 等待操作
    void waitFor(String condition);
    void waitForElement(String selector);
    
    // 脚本执行
    Object executeScript(String script);
    Object executeAsyncScript(String script);
}
```

## 🔧 平台适配机制

### 适配器模式
Midscene Java 使用适配器模式将不同平台的底层实现统一为相同的接口。

```java
// Web 平台适配器
public class WebPlatformAdapter extends PlatformInterface {
    private final WebDriver driver;
    
    @Override
    public CompletableFuture<Void> clickElement(String elementId) {
        return CompletableFuture.runAsync(() -> {
            WebElement element = driver.findElement(By.id(elementId));
            element.click();
        });
    }
    
    // 其他方法实现...
}

// Android 平台适配器
public class AndroidPlatformAdapter extends PlatformInterface {
    private final AndroidDevice device;
    
    @Override
    public CompletableFuture<Void> clickElement(String elementId) {
        return CompletableFuture.runAsync(() -> {
            device.click(elementId);
        });
    }
    
    // 其他方法实现...
}
```

### 工厂模式
使用工厂模式创建不同平台的适配器实例。

```java
public class PlatformAdapterFactory {
    public static PlatformInterface createWebAdapter(WebPlatformConfig config) {
        switch (config.getEngine()) {
            case SELENIUM:
                return new SeleniumPlatformAdapter(config);
            case PLAYWRIGHT:
                return new PlaywrightPlatformAdapter(config);
            default:
                throw new IllegalArgumentException("不支持的 Web 引擎: " + config.getEngine());
        }
    }
    
    public static PlatformInterface createAndroidAdapter(AndroidPlatformConfig config) {
        switch (config.getEngine()) {
            case ADB:
                return new AdbPlatformAdapter(config);
            case APPIUM:
                return new AppiumPlatformAdapter(config);
            default:
                throw new IllegalArgumentException("不支持的 Android 引擎: " + config.getEngine());
        }
    }
}
```

### 配置管理
不同平台使用不同的配置类，但都继承自基础配置类。

```java
// 基础配置
public abstract class PlatformConfig {
    private Duration defaultTimeout = Duration.ofSeconds(30);
    private int maxRetryCount = 3;
    private boolean enableScreenshot = true;
    
    // getter/setter...
}

// Web 平台配置
public class WebPlatformConfig extends PlatformConfig {
    private WebEngine engine = WebEngine.PLAYWRIGHT;
    private BrowserType browserType = BrowserType.CHROMIUM;
    private boolean headless = false;
    private List<String> browserArgs = new ArrayList<>();
    
    // getter/setter...
}

// Android 平台配置
public class AndroidPlatformConfig extends PlatformConfig {
    private AndroidEngine engine = AndroidEngine.ADB;
    private String deviceId;
    private String appPackage;
    private String appActivity;
    
    // getter/setter...
}
```

## 📊 性能对比

### Web 平台引擎对比

| 特性 | Selenium | Playwright | Chrome DevTools |
|------|----------|------------|-----------------|
| 浏览器支持 | 广泛 | 现代 | 仅 Chrome/Edge |
| 执行速度 | 中等 | 快 | 最快 |
| 稳定性 | 高 | 高 | 中等 |
| 功能丰富度 | 高 | 高 | 中等 |
| 社区支持 | 强 | 强 | 中等 |
| 学习曲线 | 中等 | 简单 | 简单 |

### Android 平台引擎对比

| 特性 | ADB | Appium | UIAutomator |
|------|-----|--------|-------------|
| 执行速度 | 快 | 中等 | 快 |
| 稳定性 | 高 | 中等 | 高 |
| 功能丰富度 | 中等 | 高 | 中等 |
| 设备支持 | 广泛 | 广泛 | Android 原生 |
| 学习曲线 | 简单 | 中等 | 中等 |

## 🌍 跨平台最佳实践

### 1. 统一测试脚本
编写可以在多个平台运行的测试脚本，提高代码复用率。

```java
// 平台无关的测试逻辑
public void performLogin(String username, String password) {
    agent.ai_action("点击登录按钮").get();
    agent.ai_action("输入用户名: " + username).get();
    agent.ai_action("输入密码: " + password).get();
    agent.ai_action("点击提交按钮").get();
    agent.ai_assert("登录成功").get();
}

// 在不同平台执行
@Test
public void testWebLogin() {
    PlatformInterface webInterface = createWebInterface();
    Agent agent = new Agent(webInterface);
    
    performLogin("testuser", "testpass");
}

@Test
public void testAndroidLogin() {
    PlatformInterface androidInterface = createAndroidInterface();
    Agent agent = new Agent(androidInterface);
    
    performLogin("testuser", "testpass");
}
```

### 2. 平台特定优化
针对不同平台的特点进行优化，提高执行效率。

```java
// Web 平台优化
public void optimizeWebExecution() {
    // 使用 Playwright 的自动等待
    PlaywrightPage page = new PlaywrightPage(browser.newPage());
    page.setDefaultTimeout(10000); // 设置默认等待时间
    
    // 使用 CSS 选择器提高定位速度
    agent.ai_action("使用 CSS 选择器 '.login-button' 点击登录按钮").get();
}

// Android 平台优化
public void optimizeAndroidExecution() {
    // 使用设备特定功能
    AndroidDevice device = new AndroidDevice();
    device.setWaitForIdleTimeout(5000); // 设置等待空闲时间
    
    // 使用资源 ID 提高定位速度
    agent.ai_action("使用资源 ID 'login_button' 点击登录按钮").get();
}
```

### 3. 错误处理策略
针对不同平台的常见错误实现特定的处理策略。

```java
// Web 平台错误处理
public void handleWebErrors(Exception e) {
    if (e instanceof StaleElementReferenceException) {
        // 元素过期，重新查找
        agent.ai_action("重新查找并点击登录按钮").get();
    } else if (e instanceof TimeoutException) {
        // 超时，可能需要等待页面加载
        agent.ai_waitFor("页面加载完成", Duration.ofSeconds(10)).get();
    }
}

// Android 平台错误处理
public void handleAndroidErrors(Exception e) {
    if (e instanceof UiObjectNotFoundException) {
        // 元素未找到，可能需要滚动
        agent.ai_action("向下滚动查找登录按钮").get();
    } else if (e instanceof ActivityNotFoundException) {
        // 活动未找到，可能需要启动应用
        agent.ai_action("启动应用").get();
    }
}
```

## 🔮 扩展新平台

### 1. 实现 PlatformInterface
创建新平台的适配器类，实现 PlatformInterface 接口。

```java
public class NewPlatformAdapter extends PlatformInterface {
    private final NewPlatformDriver driver;
    
    public NewPlatformAdapter(NewPlatformConfig config) {
        this.driver = new NewPlatformDriver(config);
    }
    
    @Override
    public CompletableFuture<Void> clickElement(String elementId) {
        return CompletableFuture.runAsync(() -> {
            driver.click(elementId);
        });
    }
    
    // 实现其他方法...
}
```

### 2. 创建配置类
创建新平台的配置类，继承 PlatformInterface。

```java
public class NewPlatformConfig extends PlatformConfig {
    private String connectionUrl;
    private boolean enableSpecialFeature;
    
    // getter/setter...
}
```

### 3. 注册平台
在工厂类中注册新平台的创建方法。

```java
public class PlatformAdapterFactory {
    // 现有方法...
    
    public static PlatformInterface createNewPlatformAdapter(NewPlatformConfig config) {
        return new NewPlatformAdapter(config);
    }
}
```

### 4. 添加文档和示例
为新平台编写文档和示例代码，帮助用户快速上手。

```java
// 新平台示例
public class NewPlatformExample {
    public static void main(String[] args) {
        NewPlatformConfig config = NewPlatformConfig.builder()
            .connectionUrl("http://localhost:8080")
            .enableSpecialFeature(true)
            .build();
            
        PlatformInterface platform = PlatformAdapterFactory.createNewPlatformAdapter(config);
        Agent agent = new Agent(platform);
        
        agent.ai_action("执行新平台操作").get();
    }
}
```

---

通过平台集成模块，Midscene Java 提供了统一的跨平台自动化解决方案，使开发者能够用相同的 API 操作不同的平台，大大提高了代码复用率和开发效率。