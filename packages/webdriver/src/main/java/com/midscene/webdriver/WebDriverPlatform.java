package com.midscene.webdriver;

import com.midscene.core.exception.PlatformException;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.shared.platform.PlatformInfo;
import com.midscene.shared.platform.PlatformType;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;
import com.midscene.shared.platform.Rectangle;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * WebDriver平台实现类
 * 提供基于Selenium WebDriver的Web平台自动化操作
 */
public class WebDriverPlatform implements PlatformInterface {
    private WebDriver driver;
    private PlatformInfo platformInfo;
    private boolean initialized = false;
    
    public WebDriverPlatform() {
        // 初始化平台信息
        this.platformInfo = new PlatformInfo(
            PlatformType.WEB,
            "1.0.0",
            "Web Browser",
            System.getProperty("os.name", "Unknown OS")
        );
    }
    
    @Override
    public CompletableFuture<Boolean> initialize(Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 配置ChromeDriver
                ChromeOptions chromeOptions = new ChromeOptions();
                
                // 应用用户配置的选项
                if (options != null) {
                    if (options.containsKey("headless")) {
                        chromeOptions.setHeadless((Boolean) options.get("headless"));
                    }
                    if (options.containsKey("args")) {
                        List<String> args = (List<String>) options.get("args");
                        chromeOptions.addArguments(args);
                    }
                }
                
                // 创建WebDriver实例
                this.driver = new ChromeDriver(chromeOptions);
                
                // 配置超时等参数
                if (options != null && options.containsKey("implicitWaitTimeout")) {
                    long timeout = (Long) options.get("implicitWaitTimeout");
                    this.driver.manage().timeouts().implicitlyWait(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
                
                initialized = true;
                return true;
            } catch (Exception e) {
                throw new PlatformException("Failed to initialize WebDriver", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> screenshot() {
        validateDriver();
        return CompletableFuture.supplyAsync(() -> {
            try {
                TakesScreenshot screenshot = (TakesScreenshot) driver;
                byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
                return Base64.getEncoder().encodeToString(screenshotBytes);
            } catch (Exception e) {
                throw new PlatformException("Failed to take screenshot", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<UiContext> getUiContext() {
        validateDriver();
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取截图
                String screenshotBase64 = screenshot().join();
                
                // 提取UI元素
                List<UiElement> elements = extractUiElements();
                
                // 创建UI上下文
                return new UiContext(screenshotBase64, elements, new HashMap<>(), System.currentTimeMillis());
            } catch (Exception e) {
                throw new PlatformException("Failed to get UI context", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> tap(String elementId) {
        validateDriver();
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebElement element = driver.findElement(By.id(elementId));
                element.click();
                return true;
            } catch (Exception e) {
                throw new PlatformException("Failed to tap element: " + elementId, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> input(String elementId, String text) {
        validateDriver();
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebElement element = driver.findElement(By.id(elementId));
                element.clear();
                element.sendKeys(text);
                return true;
            } catch (Exception e) {
                throw new PlatformException("Failed to input text to element: " + elementId, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> swipe(int startX, int startY, int endX, int endY) {
        validateDriver();
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 在Web中模拟滑动操作
                // 这里使用JavaScript执行滑动操作
                String script = String.format(
                    "const startPoint = {x: %d, y: %d};
                     const endPoint = {x: %d, y: %d};
                     
                     // 创建鼠标事件
                     function createMouseEvent(type, point) {
                         const event = new MouseEvent(type, {
                             clientX: point.x,
                             clientY: point.y,
                             bubbles: true,
                             cancelable: true,
                             view: window
                         });
                         return event;
                     }
                     
                     // 模拟鼠标按下
                     const downEvent = createMouseEvent('mousedown', startPoint);
                     document.elementFromPoint(startPoint.x, startPoint.y).dispatchEvent(downEvent);
                     
                     // 模拟鼠标移动
                     const moveEvent = createMouseEvent('mousemove', endPoint);
                     document.elementFromPoint(endPoint.x, endPoint.y).dispatchEvent(moveEvent);
                     
                     // 模拟鼠标释放
                     const upEvent = createMouseEvent('mouseup', endPoint);
                     document.elementFromPoint(endPoint.x, endPoint.y).dispatchEvent(upEvent);
                     
                     return true;",
                    startX, startY, endX, endY
                );
                
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script);
                return true;
            } catch (Exception e) {
                throw new PlatformException("Failed to swipe", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            if (driver != null) {
                driver.quit();
                driver = null;
                initialized = false;
            }
        });
    }
    
    @Override
    public PlatformInfo getPlatformInfo() {
        return platformInfo;
    }
    
    /**
     * 验证WebDriver是否已初始化
     */
    private void validateDriver() {
        if (!initialized || driver == null) {
            throw new PlatformException("WebDriver not initialized");
        }
    }
    
    /**
     * 提取页面中的UI元素
     */
    private List<UiElement> extractUiElements() {
        try {
            // 获取所有可交互的元素
            List<WebElement> webElements = driver.findElements(By.xpath(
                "//a | //button | //input | //textarea | //select | //div[@role='button'] | //*[not(self::script) and not(self::style) and text()]")
            );
            
            // 转换为UiElement列表
            return webElements.stream()
                .map(this::convertToUiElement)
                .filter(element -> element != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new PlatformException("Failed to extract UI elements", e);
        }
    }
    
    /**
     * 将WebElement转换为UiElement
     */
    private UiElement convertToUiElement(WebElement webElement) {
        try {
            String id = webElement.getAttribute("id");
            if (id == null || id.isEmpty()) {
                id = generateElementId(webElement);
            }
            
            String tagName = webElement.getTagName();
            String type = tagName;
            String text = webElement.getText();
            String content = text;
            
            // 获取元素位置和大小
            org.openqa.selenium.Dimension size = webElement.getSize();
            org.openqa.selenium.Point location = webElement.getLocation();
            Rectangle bounds = new Rectangle(
                location.getX(),
                location.getY(),
                size.getWidth(),
                size.getHeight()
            );
            
            // 获取元素属性
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("tagName", tagName);
            attributes.put("className", webElement.getAttribute("class"));
            attributes.put("id", id);
            attributes.put("type", webElement.getAttribute("type"));
            attributes.put("value", webElement.getAttribute("value"));
            attributes.put("placeholder", webElement.getAttribute("placeholder"));
            attributes.put("aria-label", webElement.getAttribute("aria-label"));
            attributes.put("title", webElement.getAttribute("title"));
            attributes.put("href", webElement.getAttribute("href"));
            
            // 检查元素是否可见
            boolean isVisible = webElement.isDisplayed();
            
            return new UiElement(id, type, text, null, bounds, attributes, content, isVisible);
        } catch (Exception e) {
            // 如果转换失败，返回null
            return null;
        }
    }
    
    /**
     * 为没有ID的元素生成唯一ID
     */
    private String generateElementId(WebElement element) {
        String tagName = element.getTagName();
        org.openqa.selenium.Point location = element.getLocation();
        return String.format("%s_%d_%d", tagName, location.getX(), location.getY());
    }
    
    /**
     * 导航到指定URL
     */
    public void navigateTo(String url) {
        validateDriver();
        driver.get(url);
    }
    
    /**
     * 获取当前页面URL
     */
    public String getCurrentUrl() {
        validateDriver();
        return driver.getCurrentUrl();
    }
    
    /**
     * 获取当前页面标题
     */
    public String getTitle() {
        validateDriver();
        return driver.getTitle();
    }
}