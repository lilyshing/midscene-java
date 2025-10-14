package com.midscene.ios;

import com.midscene.core.exception.PlatformException;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;

/**
 * WebDriverAgent客户端类
 * 负责与iOS WebDriverAgent服务器通信
 */
public class WdaClient {
    private static final Logger logger = Logger.getLogger(WdaClient.class.getName());
    
    private String wdaUrl;
    private int timeoutMs = 30000;
    private int retryCount = 3;
    private String sessionId;
    
    /**
     * 构造函数
     * @param wdaUrl WebDriverAgent服务器URL
     */
    public WdaClient(String wdaUrl) {
        this.wdaUrl = ensureTrailingSlash(wdaUrl);
        logger.info("WDA client initialized with URL: " + wdaUrl);
    }
    
    /**
     * 设置超时时间
     * @param timeoutMs 超时时间（毫秒）
     */
    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        logger.info("WDA timeout set to: " + timeoutMs + "ms");
    }
    
    /**
     * 设置重试次数
     * @param retryCount 重试次数
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        logger.info("WDA retry count set to: " + retryCount);
    }
    
    /**
     * 启动会话
     * @param capabilities 会话能力参数
     * @return 会话ID
     * @throws PlatformException 如果启动失败
     */
    public String startSession(Map<String, Object> capabilities) throws PlatformException {
        logger.info("Starting WDA session with capabilities: " + capabilities);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("capabilities", capabilities);
        
        try {
            String response = executePostRequest("session", requestBody);
            sessionId = parseSessionId(response);
            logger.info("WDA session started successfully: " + sessionId);
            return sessionId;
        } catch (Exception e) {
            throw new PlatformException("Failed to start WDA session", e);
        }
    }
    
    /**
     * 关闭会话
     * @throws PlatformException 如果关闭失败
     */
    public void closeSession() throws PlatformException {
        if (sessionId == null) {
            logger.warning("No active WDA session to close");
            return;
        }
        
        logger.info("Closing WDA session: " + sessionId);
        try {
            executeDeleteRequest("session/" + sessionId);
            logger.info("WDA session closed successfully");
            sessionId = null;
        } catch (Exception e) {
            throw new PlatformException("Failed to close WDA session", e);
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (sessionId != null) {
                closeSession();
            }
            logger.info("WDA client disconnected");
        } catch (Exception e) {
            logger.warning("Error during disconnect: " + e.getMessage());
        }
    }
    
    /**
     * 检查会话是否活动
     * @return true如果会话活动
     */
    public boolean isSessionActive() {
        if (sessionId == null) {
            return false;
        }
        
        try {
            String response = executeGetRequest("session/" + sessionId);
            return response.contains("sessionId") && response.contains(sessionId);
        } catch (Exception e) {
            logger.warning("Error checking session status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取当前UI上下文
     * @return UI上下文
     * @throws PlatformException 如果获取失败
     */
    public UiContext getUiContext() throws PlatformException {
        ensureSessionActive();
        logger.info("Getting UI context");
        
        try {
            String response = executeGetRequest("session/" + sessionId + "/source");
            return parseUiContext(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to get UI context", e);
        }
    }
    
    /**
     * 查找元素
     * @param locator 定位器
     * @return 元素信息
     * @throws PlatformException 如果查找失败
     */
    public Map<String, Object> findElement(Map<String, Object> locator) throws PlatformException {
        ensureSessionActive();
        logger.info("Finding element with locator: " + locator);
        
        try {
            String response = executePostRequest("session/" + sessionId + "/element", locator);
            return parseElementResponse(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to find element", e);
        }
    }
    
    /**
     * 点击元素
     * @param elementId 元素ID
     * @throws PlatformException 如果点击失败
     */
    public void clickElement(String elementId) throws PlatformException {
        ensureSessionActive();
        logger.info("Clicking element: " + elementId);
        
        try {
            executePostRequest("session/" + sessionId + "/element/" + elementId + "/click", null);
            logger.info("Element clicked successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to click element", e);
        }
    }
    
    /**
     * 在元素中输入文本
     * @param elementId 元素ID
     * @param text 要输入的文本
     * @throws PlatformException 如果输入失败
     */
    public void sendKeys(String elementId, String text) throws PlatformException {
        ensureSessionActive();
        logger.info("Sending keys to element: " + elementId + ", text: " + text);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("value", text.split(""));
            executePostRequest("session/" + sessionId + "/element/" + elementId + "/value", params);
            logger.info("Keys sent successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to send keys", e);
        }
    }
    
    /**
     * 清除元素文本
     * @param elementId 元素ID
     * @throws PlatformException 如果清除失败
     */
    public void clearElement(String elementId) throws PlatformException {
        ensureSessionActive();
        logger.info("Clearing element: " + elementId);
        
        try {
            executePostRequest("session/" + sessionId + "/element/" + elementId + "/clear", null);
            logger.info("Element cleared successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to clear element", e);
        }
    }
    
    /**
     * 获取元素属性
     * @param elementId 元素ID
     * @param attribute 属性名
     * @return 属性值
     * @throws PlatformException 如果获取失败
     */
    public String getElementAttribute(String elementId, String attribute) throws PlatformException {
        ensureSessionActive();
        logger.info("Getting attribute '" + attribute + "' for element: " + elementId);
        
        try {
            String response = executeGetRequest("session/" + sessionId + "/element/" + elementId + "/attribute/" + attribute);
            return parseAttributeResponse(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to get element attribute", e);
        }
    }
    
    /**
     * 点击坐标
     * @param x X坐标
     * @param y Y坐标
     * @throws PlatformException 如果点击失败
     */
    public void tap(int x, int y) throws PlatformException {
        ensureSessionActive();
        logger.info("Tapping at coordinates: (" + x + ", " + y + ")");
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("x", x);
            params.put("y", y);
            executePostRequest("session/" + sessionId + "/tap", params);
            logger.info("Tap successful");
        } catch (Exception e) {
            throw new PlatformException("Failed to tap at coordinates", e);
        }
    }
    
    /**
     * 滑动操作
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 持续时间（毫秒）
     * @throws PlatformException 如果滑动失败
     */
    public void swipe(int startX, int startY, int endX, int endY, int duration) throws PlatformException {
        ensureSessionActive();
        logger.info("Swiping from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ") with duration: " + duration + "ms");
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startX", startX);
            params.put("startY", startY);
            params.put("endX", endX);
            params.put("endY", endY);
            params.put("duration", duration);
            executePostRequest("session/" + sessionId + "/swipe", params);
            logger.info("Swipe successful");
        } catch (Exception e) {
            throw new PlatformException("Failed to swipe", e);
        }
    }
    
    /**
     * 获取设备截图
     * @param outputPath 输出路径
     * @return 截图文件
     * @throws PlatformException 如果截图失败
     */
    public File takeScreenshot(String outputPath) throws PlatformException {
        ensureSessionActive();
        logger.info("Taking screenshot");
        
        try {
            String response = executeGetRequest("session/" + sessionId + "/screenshot");
            String base64Image = parseScreenshotResponse(response);
            
            byte[] imageData = Base64.getDecoder().decode(base64Image);
            Path path = Paths.get(outputPath);
            Files.createDirectories(path.getParent());
            Files.write(path, imageData);
            
            logger.info("Screenshot saved to: " + outputPath);
            return path.toFile();
        } catch (Exception e) {
            throw new PlatformException("Failed to take screenshot", e);
        }
    }
    
    /**
     * 按Home键
     * @throws PlatformException 如果操作失败
     */
    public void pressHomeButton() throws PlatformException {
        ensureSessionActive();
        logger.info("Pressing home button");
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "home");
            executePostRequest("session/" + sessionId + "/press", params);
            logger.info("Home button pressed");
        } catch (Exception e) {
            throw new PlatformException("Failed to press home button", e);
        }
    }
    
    /**
     * 按返回键
     * @throws PlatformException 如果操作失败
     */
    public void pressBackButton() throws PlatformException {
        ensureSessionActive();
        logger.info("Pressing back button");
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "back");
            executePostRequest("session/" + sessionId + "/press", params);
            logger.info("Back button pressed");
        } catch (Exception e) {
            throw new PlatformException("Failed to press back button", e);
        }
    }
    
    /**
     * 锁定设备
     * @throws PlatformException 如果操作失败
     */
    public void lockDevice() throws PlatformException {
        ensureSessionActive();
        logger.info("Locking device");
        
        try {
            executePostRequest("session/" + sessionId + "/lock", null);
            logger.info("Device locked");
        } catch (Exception e) {
            throw new PlatformException("Failed to lock device", e);
        }
    }
    
    /**
     * 解锁设备
     * @throws PlatformException 如果操作失败
     */
    public void unlockDevice() throws PlatformException {
        ensureSessionActive();
        logger.info("Unlocking device");
        
        try {
            executePostRequest("session/" + sessionId + "/unlock", null);
            logger.info("Device unlocked");
        } catch (Exception e) {
            throw new PlatformException("Failed to unlock device", e);
        }
    }
    
    /**
     * 执行JavaScript
     * @param script JavaScript代码
     * @return 执行结果
     * @throws PlatformException 如果执行失败
     */
    public Object executeJavaScript(String script) throws PlatformException {
        ensureSessionActive();
        logger.info("Executing JavaScript: " + script);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("script", script);
            params.put("args", new ArrayList<>());
            String response = executePostRequest("session/" + sessionId + "/execute/sync", params);
            return parseJavaScriptResponse(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to execute JavaScript", e);
        }
    }
    
    /**
     * 获取设备信息
     * @return 设备信息
     * @throws PlatformException 如果获取失败
     */
    public Map<String, String> getDeviceInfo() throws PlatformException {
        ensureSessionActive();
        logger.info("Getting device info");
        
        try {
            String response = executeGetRequest("session/" + sessionId + "/capabilities");
            return parseDeviceInfo(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to get device info", e);
        }
    }
    
    /**
     * 安装应用
     * @param appPath 应用路径
     * @throws PlatformException 如果安装失败
     */
    public void installApp(String appPath) throws PlatformException {
        logger.info("Installing app: " + appPath);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("app", appPath);
            executePostRequest("session/" + sessionId + "/appium/device/install_app", params);
            logger.info("App installed successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to install app", e);
        }
    }
    
    /**
     * 启动应用
     * @param bundleId 应用Bundle ID
     * @throws PlatformException 如果启动失败
     */
    public void launchApp(String bundleId) throws PlatformException {
        ensureSessionActive();
        logger.info("Launching app: " + bundleId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("bundleId", bundleId);
            executePostRequest("session/" + sessionId + "/appium/app/launch", params);
            logger.info("App launched successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to launch app", e);
        }
    }
    
    /**
     * 关闭应用
     * @param bundleId 应用Bundle ID
     * @throws PlatformException 如果关闭失败
     */
    public void closeApp(String bundleId) throws PlatformException {
        ensureSessionActive();
        logger.info("Closing app: " + bundleId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("bundleId", bundleId);
            executePostRequest("session/" + sessionId + "/appium/app/close", params);
            logger.info("App closed successfully");
        } catch (Exception e) {
            throw new PlatformException("Failed to close app", e);
        }
    }
    
    /**
     * 获取当前应用
     * @return 当前应用信息
     * @throws PlatformException 如果获取失败
     */
    public Map<String, String> getCurrentApp() throws PlatformException {
        ensureSessionActive();
        logger.info("Getting current app");
        
        try {
            String response = executeGetRequest("session/" + sessionId + "/appium/app/current");
            return parseCurrentAppResponse(response);
        } catch (Exception e) {
            throw new PlatformException("Failed to get current app", e);
        }
    }
    
    // 私有辅助方法
    private String ensureTrailingSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }
    
    private void ensureSessionActive() throws PlatformException {
        if (sessionId == null) {
            throw new PlatformException("No active session. Call startSession() first.");
        }
    }
    
    private String parseSessionId(String response) throws PlatformException {
        // 简单的JSON解析，实际项目中应使用JSON库
        Pattern pattern = Pattern.compile("\"sessionId\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new PlatformException("Failed to parse session ID from response: " + response);
    }
    
    private UiContext parseUiContext(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库和XML解析
        List<UiElement> elements = new ArrayList<>();
        // 这里应该根据实际的WDA响应格式解析UI元素
        // 为了演示，返回一个空的UI上下文，提供必要的参数
        return new UiContext("", elements, "");
    }
    
    private Map<String, Object> parseElementResponse(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        Map<String, Object> result = new HashMap<>();
        // 这里应该根据实际的WDA响应格式解析元素信息
        return result;
    }
    
    private String parseAttributeResponse(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        Pattern pattern = Pattern.compile("\"value\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String parseScreenshotResponse(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        Pattern pattern = Pattern.compile("\"value\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new PlatformException("Failed to parse screenshot from response");
    }
    
    private Object parseJavaScriptResponse(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        return response;
    }
    
    private Map<String, String> parseDeviceInfo(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        Map<String, String> deviceInfo = new HashMap<>();
        // 这里应该根据实际的WDA响应格式解析设备信息
        return deviceInfo;
    }
    
    private Map<String, String> parseCurrentAppResponse(String response) throws PlatformException {
        // 简化实现，实际项目中应使用JSON库
        Map<String, String> appInfo = new HashMap<>();
        // 这里应该根据实际的WDA响应格式解析应用信息
        return appInfo;
    }
    
    // HTTP请求方法
    private String executeGetRequest(String endpoint) throws IOException, InterruptedException {
        URL url = new URL(wdaUrl + endpoint);
        return executeHttpRequest(url, "GET", null);
    }
    
    private String executePostRequest(String endpoint, Map<String, Object> body) throws IOException, InterruptedException {
        URL url = new URL(wdaUrl + endpoint);
        String bodyJson = (body != null) ? convertMapToJson(body) : "{}";
        return executeHttpRequest(url, "POST", bodyJson);
    }
    
    private String executeDeleteRequest(String endpoint) throws IOException, InterruptedException {
        URL url = new URL(wdaUrl + endpoint);
        return executeHttpRequest(url, "DELETE", null);
    }
    
    private String executeHttpRequest(URL url, String method, String body) throws IOException, InterruptedException {
        int attempts = 0;
        while (attempts <= retryCount) {
            attempts++;
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeoutMs);
                conn.setReadTimeout(timeoutMs);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                
                if (body != null && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                }
                
                int responseCode = conn.getResponseCode();
                String responseBody = readResponseBody(conn);
                
                if (responseCode >= 200 && responseCode < 300) {
                    return responseBody;
                } else {
                    throw new IOException("HTTP error " + responseCode + ": " + responseBody);
                }
            } catch (IOException e) {
                if (attempts >= retryCount) {
                    throw e;
                }
                logger.warning("Request failed, attempt " + attempts + "/" + retryCount + ": " + e.getMessage());
                Thread.sleep(1000 * attempts); // 指数退避
            }
        }
        throw new IOException("Max retries exceeded");
    }
    
    private String readResponseBody(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            // 尝试从错误流读取
            try (InputStream err = conn.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                return errorResponse.toString();
            } catch (IOException ignored) {
                throw e;
            }
        }
    }
    
    private String convertMapToJson(Map<String, Object> map) {
        // 简化的JSON转换，实际项目中应使用JSON库
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append('"').append(entry.getKey()).append('"').append(":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append('"').append(escapeJsonString((String) value)).append('"');
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
            } else if (value instanceof Map) {
                json.append(convertMapToJson((Map<String, Object>) value));
            } else {
                json.append('"').append(value.toString()).append('"');
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            if (item == null) {
                json.append("null");
            } else if (item instanceof String) {
                json.append('"').append(escapeJsonString((String) item)).append('"');
            } else if (item instanceof Number || item instanceof Boolean) {
                json.append(item);
            } else if (item instanceof List) {
                json.append(convertListToJson((List<?>) item));
            } else if (item instanceof Map) {
                json.append(convertMapToJson((Map<String, Object>) item));
            } else {
                json.append('"').append(item.toString()).append('"');
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String escapeJsonString(String input) {
        return input.replace("\"", "\\\"")
                    .replace("\\", "\\\\")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    /**
     * 清除应用数据
     * @param bundleId 应用Bundle ID
     * @throws Exception 如果清除失败
     */
    public void clearAppData(String bundleId) throws Exception {
        logger.info("WDA: Clearing app data for bundle ID - " + bundleId);
        // 简化实现，发送POST请求到WDA清除应用数据接口
        Map<String, Object> payload = new HashMap<>();
        payload.put("bundleId", bundleId);
        // 使用现有的HTTP请求方法发送请求
        executePostRequest("wda/app/clear", payload);
    }
    public void pressKey(String keyName) throws Exception {
        logger.info("WDA: Pressing key - " + keyName);
        // 简化实现，发送POST请求到WDA按键接口
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", keyName);
        executePostRequest("wda/keyboard/press", payload);
    }
    
    /**
     * 截图
     * @return 截图的Base64编码
     * @throws Exception 如果截图失败
     */
    public String takeScreenshot() throws Exception {
        logger.info("WDA: Taking screenshot");
        // 简化实现，发送GET请求到WDA截图接口
       String response = executeGetRequest("wda/screenshot");
        return response; // 假设返回的就是Base64编码的截图
    }
}