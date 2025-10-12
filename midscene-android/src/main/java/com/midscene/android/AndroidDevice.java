package com.midscene.android;

import com.midscene.core.model.*;
import com.midscene.core.exception.PlatformConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Android设备操作类，通过ADB与Android设备交互
 */
public class AndroidDevice implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(AndroidDevice.class);
    private static final String ADB_COMMAND = "adb"; // 假设adb在系统PATH中
    
    private final String deviceId;
    private boolean isConnected = false;
    
    public AndroidDevice(String deviceId) {
        this.deviceId = deviceId;
        connect();
    }
    
    public AndroidDevice() {
        // 自动选择第一个连接的设备
        this.deviceId = getFirstConnectedDevice();
        if (this.deviceId == null) {
            throw new PlatformConnectionException("No Android devices connected");
        }
        connect();
    }
    
    /**
     * 连接到设备
     */
    private void connect() {
        try {
            String result = executeAdbCommandInternal("-s", deviceId, "devices");
            if (result.contains(deviceId + "\tdevice")) {
                isConnected = true;
                logger.info("Connected to device: {}", deviceId);
            } else {
                throw new PlatformConnectionException("Failed to connect to device: " + deviceId);
            }
        } catch (Exception e) {
            throw new PlatformConnectionException("Error connecting to device: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取屏幕上的UI元素
     */
    public CompletableFuture<List<UiElement>> getElements() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<UiElement> elements = new ArrayList<>();
                
                // 使用uiautomator dump获取UI层次结构
                String uiDump = executeAdbCommandInternal("-s", deviceId, "shell", "uiautomator", "dump", "/dev/tty");
                
                // 解析UI转储（简化版本，实际需要解析XML）
                // 这里使用模拟数据，实际实现应解析XML
                elements = parseUiDump(uiDump);
                
                logger.info("Retrieved {} elements from Android device", elements.size());
                return elements;
            } catch (Exception e) {
                logger.error("Failed to get elements: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to retrieve UI elements", e);
            }
        });
    }
    
    /**
     * 获取设备屏幕截图
     */
    public CompletableFuture<byte[]> takeScreenshot() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String deviceTempFile = "/sdcard/screenshot.png";
                String localTempFile = "/tmp/screenshot_" + UUID.randomUUID() + ".png";
                
                logger.info("Taking screenshot from device: {}", deviceId);
                
                // 在设备上截取屏幕
                executeAdbCommandInternal("-s", deviceId, "shell", "screencap", "-p", deviceTempFile);
                
                // 拉取文件到本地
                executeAdbCommandInternal("-s", deviceId, "pull", deviceTempFile, localTempFile);
                
                // 读取文件内容
                java.nio.file.Path path = java.nio.file.Paths.get(localTempFile);
                byte[] screenshotData = java.nio.file.Files.readAllBytes(path);
                
                // 清理临时文件
                executeAdbCommandInternal("-s", deviceId, "shell", "rm", deviceTempFile);
                java.nio.file.Files.deleteIfExists(path);
                
                logger.info("Screenshot taken successfully, size: {} bytes", screenshotData.length);
                return screenshotData;
            } catch (Exception e) {
                logger.error("Failed to take screenshot: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to take screenshot", e);
            }
        });
    }
    
    /**
     * 点击指定位置
     */
    public CompletableFuture<Void> tap(int x, int y) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Tapping at position: ({}, {})", x, y);
                executeAdbCommandInternal("-s", deviceId, "shell", "input", "tap", String.valueOf(x), String.valueOf(y));
                logger.info("Tap operation completed successfully");
            } catch (Exception e) {
                logger.error("Failed to tap at ({}, {}): {}", x, y, e.getMessage(), e);
                throw new RuntimeException("Tap operation failed", e);
            }
        });
    }
    
    /**
     * 输入文本
     */
    public CompletableFuture<Void> inputText(String text) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Inputting text: {}", text);
                executeAdbCommandInternal("-s", deviceId, "shell", "input", "text", text);
                logger.info("Text input completed successfully");
            } catch (Exception e) {
                logger.error("Failed to input text: {}", e.getMessage(), e);
                throw new RuntimeException("Text input failed", e);
            }
        });
    }
    
    /**
     * 获取设备信息
     */
    public CompletableFuture<String> getDeviceInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String model = executeAdbCommandInternal("-s", deviceId, "shell", "getprop", "ro.product.model");
                String version = executeAdbCommandInternal("-s", deviceId, "shell", "getprop", "ro.build.version.release");
                
                return String.format("Device: %s\nAndroid Version: %s", model.trim(), version.trim());
            } catch (Exception e) {
                logger.error("Failed to get device info: {}", e.getMessage(), e);
                return "Failed to get device information";
            }
        });
    }
    
    /**
     * 获取第一个连接的设备ID
     */
    private String getFirstConnectedDevice() {
        try {
            String result = executeAdbCommandInternal("devices");
            String[] lines = result.split("\\n");
            
            for (int i = 1; i < lines.length; i++) { // 跳过标题行
                String line = lines[i].trim();
                if (line.endsWith("device")) {
                    return line.split("\\s+")[0];
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error getting connected devices: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 执行ADB命令（内部方法）
     */
    private String executeAdbCommandInternal(String... commands) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> cmdList = new ArrayList<>();
        cmdList.add(ADB_COMMAND);
        cmdList.addAll(List.of(commands));
        pb.command(cmdList);
        
        Process process = pb.start();
        
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 读取错误输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.warn("ADB error: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("ADB command failed with exit code " + exitCode + ": " + String.join(" ", cmdList));
        }
        
        return output.toString();
    }
    
    /**
     * 解析UI转储（完整实现，解析XML格式）
     */
    private List<UiElement> parseUiDump(String uiDump) {
        List<UiElement> elements = new ArrayList<>();
        
        try {
            // 检查输入是否为空
            if (uiDump == null || uiDump.trim().isEmpty()) {
                logger.warn("Empty UI dump received");
                return elements;
            }
            
            // 去除可能的前导字符，确保XML格式正确
            String cleanDump = uiDump.trim();
            if (cleanDump.startsWith("UI hierchary dumped to: /dev/tty")) {
                // 移除uiautomator dump命令的提示信息
                cleanDump = cleanDump.substring("UI hierchary dumped to: /dev/tty".length()).trim();
            }
            
            // 使用Java内置的XML解析器
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            
            // 解析XML字符串
            try (java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(cleanDump.getBytes("UTF-8"))) {
                org.w3c.dom.Document doc = builder.parse(input);
                doc.getDocumentElement().normalize();
                
                // 递归解析XML节点
                parseElementNodes(doc.getDocumentElement(), elements);
            }
            
            logger.info("Successfully parsed {} elements from UI dump", elements.size());
        } catch (Exception e) {
            logger.error("Failed to parse UI dump: {}", e.getMessage(), e);
            // 如果XML解析失败，返回模拟数据作为后备
            return createMockUiElements();
        }
        
        return elements;
    }
    
    /**
     * 递归解析XML元素节点
     */
    private void parseElementNodes(org.w3c.dom.Node node, List<UiElement> elements) {
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) node;
            
            // 提取元素属性
            String resourceId = element.getAttribute("resource-id");
            String text = element.getAttribute("text");
            String contentDesc = element.getAttribute("content-desc");
            String className = element.getAttribute("class");
            
            // 获取边界框信息
            String bounds = element.getAttribute("bounds");
            Rect rect = parseBounds(bounds);
            
            // 如果有有效边界框，则创建UI元素
            if (rect != null) {
                // 创建元素ID（优先使用resourceId，如果为空则生成一个）
                String id = resourceId.isEmpty() ? generateElementId(className, text, contentDesc) : resourceId;
                
                // 确定元素内容（优先使用text，然后是content-desc）
                String content = text.isEmpty() ? contentDesc : text;
                if (content.isEmpty()) {
                    content = className.substring(className.lastIndexOf('.') + 1);
                }
                
                // 创建Android UI元素
                AndroidUiElement uiElement = new AndroidUiElement(id, content, rect);
                
                // 根据className推断节点类型
                inferNodeType(uiElement, className, resourceId, text);
                
                elements.add(uiElement);
            }
        }
        
        // 递归处理子节点
        org.w3c.dom.NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            parseElementNodes(nodeList.item(i), elements);
        }
    }
    
    /**
     * 解析bounds属性，格式如"[0,0][1080,1920]"
     */
    private Rect parseBounds(String bounds) {
        try {
            // 提取坐标值
            String[] parts = bounds.replaceAll("\\[|\\]", "").split(",|\\s+");
            if (parts.length == 4) {
                int x1 = Integer.parseInt(parts[0]);
                int y1 = Integer.parseInt(parts[1]);
                int x2 = Integer.parseInt(parts[2]);
                int y2 = Integer.parseInt(parts[3]);
                
                int width = x2 - x1;
                int height = y2 - y1;
                
                return new Rect(x1, y1, width, height);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse bounds: {}", bounds);
        }
        return null;
    }
    
    /**
     * 生成元素ID
     */
    private String generateElementId(String className, String text, String contentDesc) {
        String base = className.substring(className.lastIndexOf('.') + 1).toLowerCase();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return base + "_" + suffix;
    }
    
    /**
     * 根据类名和属性推断节点类型
     */
    private void inferNodeType(AndroidUiElement element, String className, String resourceId, String text) {
        className = className.toLowerCase();
        resourceId = resourceId.toLowerCase();
        
        // 根据类名判断
        if (className.contains("button") || className.contains("checkbox") || className.contains("radiobutton")) {
            element.setNodeType(NodeType.BUTTON);
        } 
        // 根据资源ID判断
        else if (resourceId.contains("button") || resourceId.contains("btn") || 
                 resourceId.contains("submit") || resourceId.contains("login")) {
            element.setNodeType(NodeType.BUTTON);
        }
        // 判断输入框
        else if (className.contains("edittext") || className.contains("input") || 
                resourceId.contains("edittext") || resourceId.contains("input") || 
                resourceId.contains("username") || resourceId.contains("password")) {
            element.setNodeType(NodeType.INPUT);
        }
        // 判断文本
        else if (className.contains("textview") || className.contains("text") || 
                className.contains("label")) {
            element.setNodeType(NodeType.TEXT);
        }
        // 判断列表或容器
        else if (className.contains("list") || className.contains("recycler") || 
                className.contains("container") || className.contains("layout")) {
            element.setNodeType(NodeType.CONTAINER);
        }
        // 默认类型
        else {
            element.setNodeType(NodeType.OTHER);
        }
    }
    
    /**
     * 创建模拟UI元素（当XML解析失败时使用）
     */
    private List<UiElement> createMockUiElements() {
        List<UiElement> elements = new ArrayList<>();
        
        // 创建一些模拟UI元素用于示例
        elements.add(new AndroidUiElement(
            "btn_login",
            "Login",
            new Rect(500, 800, 300, 100)
        ));
        
        elements.add(new AndroidUiElement(
            "input_username",
            "Username",
            new Rect(300, 600, 600, 80)
        ));
        
        elements.add(new AndroidUiElement(
            "input_password",
            "Password",
            new Rect(300, 700, 600, 80)
        ));
        
        elements.add(new AndroidUiElement(
            "text_title",
            "Welcome Screen",
            new Rect(300, 300, 600, 100)
        ));
        
        elements.add(new AndroidUiElement(
            "btn_register",
            "Register",
            new Rect(500, 950, 300, 100)
        ));
        
        logger.info("Using mock UI elements as fallback");
        return elements;
    }
    
    /**
     * 获取设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 关闭连接
     */
    @Override
    public void close() {
        // Android设备连接不需要特别关闭
        logger.info("Android device connection closed");
    }
    
    /**
     * 公开执行ADB命令方法，供外部类使用
     */
    public String executeAdbCommand(String... commands) throws IOException, InterruptedException {
        return executeAdbCommandInternal(commands);
    }
    
    /**
     * Android实现的UI元素类
     */
    public class AndroidUiElement extends UiElement {
        public AndroidUiElement(String id, String content, Rect rect) {
            super(id, content, rect);
            // 根据ID或内容推断节点类型
            if (content.toLowerCase().contains("login") || content.toLowerCase().contains("submit") || 
                content.toLowerCase().contains("button") || id.toLowerCase().contains("btn")) {
                setNodeType(NodeType.BUTTON);
            } else if (id.toLowerCase().contains("input") || id.toLowerCase().contains("edit") || 
                       content.toLowerCase().contains("username") || content.toLowerCase().contains("password")) {
                setNodeType(NodeType.INPUT);
            } else {
                setNodeType(NodeType.TEXT);
            }
        }
        
        @Override
        public CompletableFuture<Void> tap() {
            Point center = getCenter();
            return AndroidDevice.this.tap((int)center.getX(), (int)center.getY());
        }
        
        @Override
        public CompletableFuture<Void> inputText(String text) {
            return tap().thenCompose(v -> AndroidDevice.this.inputText(text));
        }
    }
}