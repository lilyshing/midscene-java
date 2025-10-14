package com.midscene.webdriver;

import com.midscene.shared.platform.UiElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Web元素定位器类
 * 提供多种策略来定位Web页面中的元素
 */
public class WebLocator {
    private final WebDriver driver;
    
    /**
     * 构造函数
     * @param driver WebDriver实例
     */
    public WebLocator(WebDriver driver) {
        this.driver = driver;
    }
    
    /**
     * 通过ID定位元素
     * @param id 元素ID
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findById(String id) {
        return driver.findElement(By.id(id));
    }
    
    /**
     * 通过CSS选择器定位元素
     * @param selector CSS选择器
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByCssSelector(String selector) {
        return driver.findElement(By.cssSelector(selector));
    }
    
    /**
     * 通过XPath定位元素
     * @param xpath XPath表达式
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByXPath(String xpath) {
        return driver.findElement(By.xpath(xpath));
    }
    
    /**
     * 通过链接文本定位元素
     * @param text 链接文本
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByLinkText(String text) {
        return driver.findElement(By.linkText(text));
    }
    
    /**
     * 通过部分链接文本定位元素
     * @param partialText 部分链接文本
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByPartialLinkText(String partialText) {
        return driver.findElement(By.partialLinkText(partialText));
    }
    
    /**
     * 通过标签名定位元素
     * @param tagName HTML标签名
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByTagName(String tagName) {
        return driver.findElement(By.tagName(tagName));
    }
    
    /**
     * 通过类名定位元素
     * @param className CSS类名
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByClassName(String className) {
        return driver.findElement(By.className(className));
    }
    
    /**
     * 通过名称属性定位元素
     * @param name 元素的name属性值
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByName(String name) {
        return driver.findElement(By.name(name));
    }
    
    /**
     * 查找所有匹配CSS选择器的元素
     * @param selector CSS选择器
     * @return 元素列表
     */
    public List<WebElement> findAllByCssSelector(String selector) {
        return driver.findElements(By.cssSelector(selector));
    }
    
    /**
     * 查找所有匹配XPath的元素
     * @param xpath XPath表达式
     * @return 元素列表
     */
    public List<WebElement> findAllByXPath(String xpath) {
        return driver.findElements(By.xpath(xpath));
    }
    
    /**
     * 查找所有匹配标签名的元素
     * @param tagName HTML标签名
     * @return 元素列表
     */
    public List<WebElement> findAllByTagName(String tagName) {
        return driver.findElements(By.tagName(tagName));
    }
    
    /**
     * 查找所有匹配类名的元素
     * @param className CSS类名
     * @return 元素列表
     */
    public List<WebElement> findAllByClassName(String className) {
        return driver.findElements(By.className(className));
    }
    
    /**
     * 通过文本内容定位元素
     * @param text 元素的文本内容
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByText(String text) {
        String xpath = String.format(".//*[normalize-space(text())='%s']", escapeXpathString(text));
        return findByXPath(xpath);
    }
    
    /**
     * 通过部分文本内容定位元素
     * @param partialText 部分文本内容
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByPartialText(String partialText) {
        String xpath = String.format(".//*[contains(normalize-space(text()), '%s')]", escapeXpathString(partialText));
        return findByXPath(xpath);
    }
    
    /**
     * 查找所有包含指定文本的元素
     * @param text 要查找的文本
     * @return 元素列表
     */
    public List<WebElement> findAllByText(String text) {
        String xpath = String.format(".//*[normalize-space(text())='%s']", escapeXpathString(text));
        return findAllByXPath(xpath);
    }
    
    /**
     * 查找所有包含部分指定文本的元素
     * @param partialText 要查找的部分文本
     * @return 元素列表
     */
    public List<WebElement> findAllByPartialText(String partialText) {
        String xpath = String.format(".//*[contains(normalize-space(text()), '%s')]", escapeXpathString(partialText));
        return findAllByXPath(xpath);
    }
    
    /**
     * 通过属性值定位元素
     * @param attribute 属性名
     * @param value 属性值
     * @return 找到的元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByAttribute(String attribute, String value) {
        String xpath = String.format(".//*[@%s='%s']", attribute, escapeXpathString(value));
        return findByXPath(xpath);
    }
    
    /**
     * 查找所有具有指定属性值的元素
     * @param attribute 属性名
     * @param value 属性值
     * @return 元素列表
     */
    public List<WebElement> findAllByAttribute(String attribute, String value) {
        String xpath = String.format(".//*[@%s='%s']", attribute, escapeXpathString(value));
        return findAllByXPath(xpath);
    }
    
    /**
     * 通过正则表达式匹配元素的文本内容
     * @param regex 正则表达式
     * @return 找到的第一个匹配元素
     * @throws NoSuchElementException 如果元素未找到
     */
    public WebElement findByTextRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        List<WebElement> elements = driver.findElements(By.xpath(".//*[text()]")).stream()
            .filter(element -> {
                String text = element.getText();
                Matcher matcher = pattern.matcher(text);
                return matcher.find();
            })
            .collect(Collectors.toList());
        
        if (elements.isEmpty()) {
            throw new NoSuchElementException("No element found matching regex: " + regex);
        }
        
        return elements.get(0);
    }
    
    /**
     * 检查元素是否存在
     * @param by 定位策略
     * @return 如果元素存在返回true，否则返回false
     */
    public boolean elementExists(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * 检查ID元素是否存在
     * @param id 元素ID
     * @return 如果元素存在返回true，否则返回false
     */
    public boolean idExists(String id) {
        return elementExists(By.id(id));
    }
    
    /**
     * 检查CSS选择器元素是否存在
     * @param selector CSS选择器
     * @return 如果元素存在返回true，否则返回false
     */
    public boolean cssSelectorExists(String selector) {
        return elementExists(By.cssSelector(selector));
    }
    
    /**
     * 检查XPath元素是否存在
     * @param xpath XPath表达式
     * @return 如果元素存在返回true，否则返回false
     */
    public boolean xpathExists(String xpath) {
        return elementExists(By.xpath(xpath));
    }
    
    /**
     * 转义XPath字符串中的特殊字符
     * @param str 要转义的字符串
     * @return 转义后的字符串
     */
    private String escapeXpathString(String str) {
        // 处理单引号和双引号
        if (!str.contains("'")) {
            return str;
        } else if (!str.contains("\"")) {
            return "\"" + str + "\"";
        } else {
            // 如果字符串同时包含单引号和双引号，使用concat函数
            StringBuilder sb = new StringBuilder();
            sb.append("concat(");
            String[] parts = str.split("'");
            for (int i = 0; i < parts.length; i++) {
                sb.append("'").append(parts[i]).append("'");
                if (i < parts.length - 1) {
                    sb.append(", \"'\", ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }
    
    /**
     * 转换WebElement为UiElement
     * @param webElement WebElement实例
     * @return UiElement实例
     */
    public UiElement toUiElement(WebElement webElement) {
        String id = webElement.getAttribute("id");
        if (id == null || id.isEmpty()) {
            id = generateElementId(webElement);
        }
        
        String tagName = webElement.getTagName();
        String text = webElement.getText();
        
        // 获取元素位置和大小
        org.openqa.selenium.Point location = webElement.getLocation();
        org.openqa.selenium.Dimension size = webElement.getSize();
        com.midscene.shared.platform.Rectangle bounds = new com.midscene.shared.platform.Rectangle(
            location.getX(),
            location.getY(),
            size.getWidth(),
            size.getHeight()
        );
        
        // 获取元素属性
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        attributes.put("tagName", tagName);
        attributes.put("className", webElement.getAttribute("class"));
        attributes.put("id", id);
        attributes.put("type", webElement.getAttribute("type"));
        attributes.put("value", webElement.getAttribute("value"));
        attributes.put("placeholder", webElement.getAttribute("placeholder"));
        attributes.put("aria-label", webElement.getAttribute("aria-label"));
        attributes.put("title", webElement.getAttribute("title"));
        attributes.put("href", webElement.getAttribute("href"));
        
        return new UiElement(
            id,
            tagName,
            text,
            null,  // description
            bounds,
            attributes,
            text,  // content
            webElement.isDisplayed()
        );
    }
    
    /**
     * 为没有ID的元素生成唯一ID
     * @param element WebElement实例
     * @return 生成的唯一ID
     */
    private String generateElementId(WebElement element) {
        String tagName = element.getTagName();
        org.openqa.selenium.Point location = element.getLocation();
        return String.format("%s_%d_%d", tagName, location.getX(), location.getY());
    }
}