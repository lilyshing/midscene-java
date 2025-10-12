package com.midscene.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Android设备类
 * 提供与Android设备交互的基本功能
 */
public class AndroidDevice {
    private static final Logger logger = LoggerFactory.getLogger(AndroidDevice.class);
    
    private boolean connected = false;
    private String deviceId;
    private String model;
    private String osVersion;
    private String currentApp;
    private boolean useRealDevice = false; // 新增字段，标识是否使用真实设备
    
    /**
     * 连接到Android设备
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("正在连接到Android设备...");
                
                if (useRealDevice) {
                    // 连接真实设备
                    connectToRealDevice();
                } else {
                    // 使用模拟数据
                    connectToMockDevice();
                }
                
                logger.info("已成功连接到Android设备: {} ({})", model, deviceId);
            } catch (Exception e) {
                logger.error("连接Android设备失败", e);
                throw new RuntimeException("连接Android设备失败", e);
            }
        });
    }
    
    /**
     * 连接到真实Android设备
     */
    private void connectToRealDevice() {
        try {
            // 执行adb devices命令获取设备列表
            Process process = Runtime.getRuntime().exec("adb devices");
            process.waitFor();
            
            // 读取命令输出
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line;
            boolean foundDevice = false;
            
            while ((line = reader.readLine()) != null) {
                // 跳过第一行（List of devices attached）
                if (line.startsWith("List of devices")) {
                    continue;
                }
                
                // 解析设备行
                String[] parts = line.split("\t");
                if (parts.length >= 2 && "device".equals(parts[1])) {
                    this.deviceId = parts[0];
                    foundDevice = true;
                    break;
                }
            }
            
            if (!foundDevice) {
                throw new RuntimeException("未找到已连接的Android设备，请确保设备已启用USB调试并已连接");
            }
            
            // 获取设备信息
            this.model = getDeviceProperty("ro.product.model");
            this.osVersion = getDeviceProperty("ro.build.version.release");
            this.currentApp = getCurrentAppFromDevice();
            
            this.connected = true;
            
            logger.info("已连接到真实Android设备: {} (型号: {}, 系统: Android {})", 
                deviceId, model, osVersion);
        } catch (Exception e) {
            throw new RuntimeException("连接真实Android设备失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 连接到模拟设备
     */
    private void connectToMockDevice() {
        // 这里应该实现实际的设备连接逻辑
        // 简化实现，仅作为示例
        this.connected = true;
        this.deviceId = "emulator-5554";
        this.model = "Pixel 4 API 30";
        this.osVersion = "11";
        this.currentApp = "com.android.launcher3";
    }
    
    /**
     * 获取设备属性
     */
    private String getDeviceProperty(String property) {
        try {
            Process process = Runtime.getRuntime().exec(
                String.format("adb -s %s shell getprop %s", deviceId, property));
            process.waitFor();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            
            return line != null ? line.trim() : "Unknown";
        } catch (Exception e) {
            logger.error("获取设备属性失败: {}", property, e);
            return "Unknown";
        }
    }
    
    /**
     * 获取当前应用包名
     */
    private String getCurrentAppFromDevice() {
        try {
            Process process = Runtime.getRuntime().exec(
                String.format("adb -s %s shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'", deviceId));
            process.waitFor();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            
            if (line != null && line.contains("=")) {
                String[] parts = line.split("=");
                if (parts.length > 1) {
                    String focusInfo = parts[1].trim();
                    // 提取包名
                    if (focusInfo.contains("/")) {
                        return focusInfo.split("/")[0];
                    }
                }
            }
            
            return "com.android.launcher3"; // 默认返回桌面
        } catch (Exception e) {
            logger.error("获取当前应用失败", e);
            return "com.android.launcher3";
        }
    }
    
    /**
     * 设置是否使用真实设备
     */
    public void setRealDevice(boolean useRealDevice) {
        this.useRealDevice = useRealDevice;
    }
    
    /**
     * 断开与Android设备的连接
     */
    public void disconnect() {
        try {
            if (connected) {
                logger.info("正在断开与Android设备的连接...");
                
                // 这里应该实现实际的设备断开逻辑
                this.connected = false;
                
                logger.info("已成功断开与Android设备的连接");
            }
        } catch (Exception e) {
            logger.error("断开Android设备连接时出错", e);
        }
    }
    
    /**
     * 获取设备型号
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 获取操作系统版本
     */
    public String getOsVersion() {
        return osVersion;
    }
    
    /**
     * 获取当前应用包名
     */
    public String getCurrentApp() {
        if (useRealDevice) {
            return getCurrentAppFromDevice();
        } else {
            return currentApp;
        }
    }
    
    /**
     * 获取屏幕描述
     */
    public String getScreenDescription() {
        // 这里应该实现实际的屏幕描述获取逻辑
        // 简化实现，仅作为示例
        return "主屏幕显示应用列表";
    }
    
    /**
     * 在指定坐标点击
     */
    public boolean tap(int x, int y) {
        try {
            logger.info("在坐标 ({}, {}) 执行点击", x, y);
            
            if (useRealDevice) {
                // 使用真实ADB命令执行点击
                String command = String.format("adb -s %s shell input tap %d %d", deviceId, x, y);
                executeAdbCommand(command);
            } else {
                // 模拟点击
                logger.debug("模拟点击操作");
            }
            
            return true;
        } catch (Exception e) {
            logger.error("点击操作失败", e);
            return false;
        }
    }
    
    /**
     * 输入文本
     */
    public boolean inputText(String text) {
        try {
            logger.info("输入文本: {}", text);
            
            if (useRealDevice) {
                // 使用真实ADB命令输入文本
                String command = String.format("adb -s %s shell input text \"%s\"", deviceId, text.replace(" ", "%s"));
                executeAdbCommand(command);
            } else {
                // 模拟输入
                logger.debug("模拟输入操作");
            }
            
            return true;
        } catch (Exception e) {
            logger.error("文本输入失败", e);
            return false;
        }
    }
    
    /**
     * 滚动屏幕
     */
    public boolean scroll(int x, int y) {
        try {
            logger.info("滚动到坐标 ({}, {})", x, y);
            
            if (useRealDevice) {
                // 使用真实ADB命令执行滚动
                String command = String.format("adb -s %s shell input swipe %d %d %d %d", deviceId, x, y, x, y - 500);
                executeAdbCommand(command);
            } else {
                // 模拟滚动
                logger.debug("模拟滚动操作");
            }
            
            return true;
        } catch (Exception e) {
            logger.error("滚动操作失败", e);
            return false;
        }
    }
    
    /**
     * 查找元素
     */
    public ElementInfo findElement(String selector) {
        try {
            logger.info("查找元素: {}", selector);
            
            // 这里应该实现实际的元素查找逻辑
            // 简化实现，仅作为示例
            if ("text='设置'".equals(selector)) {
                return new ElementInfo("设置", new Rect(100, 200, 200, 100));
            } else if ("text='WLAN'".equals(selector)) {
                return new ElementInfo("WLAN", new Rect(100, 300, 200, 100));
            } else if ("content-desc='返回'".equals(selector)) {
                return new ElementInfo("返回", new Rect(50, 100, 100, 100));
            }
            
            return null;
        } catch (Exception e) {
            logger.error("查找元素失败", e);
            return null;
        }
    }
    
    /**
     * 执行ADB命令
     */
    private String executeAdbCommand(String command) {
        try {
            logger.debug("执行ADB命令: {}", command);
            
            if (useRealDevice) {
                // 执行真实的ADB命令
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                
                // 读取命令输出
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                return output.toString();
            } else {
                // 模拟命令执行
                logger.debug("模拟ADB命令执行: {}", command);
                return "";
            }
        } catch (Exception e) {
            logger.error("执行ADB命令失败: {}", command, e);
            return "";
        }
    }
    
    /**
     * 截取屏幕截图
     */
    public void takeScreenshot(String fileName) {
        try {
            logger.info("截取屏幕截图并保存到: {}", fileName);
            
            if (useRealDevice) {
                // 使用真实ADB命令截图
                String command = String.format("adb -s %s shell screencap -p /sdcard/screenshot.png", deviceId);
                executeAdbCommand(command);
                
                // 将截图从设备拉取到本地
                String pullCommand = String.format("adb -s %s pull /sdcard/screenshot.png %s", deviceId, fileName);
                executeAdbCommand(pullCommand);
            } else {
                // 模拟截图
                logger.debug("模拟截图操作");
            }
        } catch (Exception e) {
            logger.error("截图操作失败", e);
        }
    }
    
    /**
     * 退出应用程序
     */
    public void exitApplication() {
        try {
            logger.info("退出Android应用程序");
            
            if (useRealDevice) {
                // 使用真实ADB命令返回主屏幕
                String command = String.format("adb -s %s shell input keyevent KEYCODE_HOME", deviceId);
                executeAdbCommand(command);
            } else {
                // 模拟退出
                logger.debug("模拟退出应用程序操作");
            }
        } catch (Exception e) {
            logger.error("退出应用程序失败", e);
        }
    }
    
    /**
     * 元素信息类
     */
    public static class ElementInfo {
        private final String text;
        private final Rect bounds;
        
        public ElementInfo(String text, Rect bounds) {
            this.text = text;
            this.bounds = bounds;
        }
        
        public String getText() {
            return text;
        }
        
        public Rect getBounds() {
            return bounds;
        }
    }
    
    /**
     * 矩形区域类
     */
    public static class Rect {
        private final int left;
        private final int top;
        private final int width;
        private final int height;
        
        public Rect(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }
        
        public int left() {
            return left;
        }
        
        public int top() {
            return top;
        }
        
        public int width() {
            return width;
        }
        
        public int height() {
            return height;
        }
        
        public int centerX() {
            return left + width / 2;
        }
        
        public int centerY() {
            return top + height / 2;
        }
    }
}