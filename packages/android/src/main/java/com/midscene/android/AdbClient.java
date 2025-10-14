package com.midscene.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * ADB客户端类
 * 提供与Android设备通信的ADB命令执行功能
 */
public class AdbClient {
    private static final Logger logger = Logger.getLogger(AdbClient.class.getName());
    private static final String ADB_EXECUTABLE = "adb";
    private static final int DEFAULT_TIMEOUT = 30000; // 默认超时时间30秒
    
    /**
     * 执行ADB命令
     * @param command 命令参数列表
     * @return 命令输出
     * @throws IOException 如果执行命令失败
     */
    private String executeAdbCommand(String... command) throws IOException {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(ADB_EXECUTABLE);
        for (String arg : command) {
            fullCommand.add(arg);
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.redirectErrorStream(true);
        
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IOException("Failed to execute ADB command: " + String.join(" ", fullCommand), e);
        }
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        try {
            boolean finished = process.waitFor(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("ADB command timed out: " + String.join(" ", fullCommand));
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IOException("ADB command failed with exit code " + exitCode + ": " + output.toString().trim());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ADB command interrupted", e);
        }
        
        return output.toString().trim();
    }
    
    /**
     * 获取已连接的设备列表
     * @return 设备ID列表
     * @throws IOException 如果无法获取设备列表
     */
    public List<String> getConnectedDevices() throws IOException {
        String output = executeAdbCommand("devices");
        List<String> devices = new ArrayList<>();
        
        for (String line : output.split("\n")) {
            if (line.endsWith("device")) {
                String deviceId = line.split("\\s+")[0];
                if (!deviceId.equals("List")) { // 跳过标题行
                    devices.add(deviceId);
                }
            }
        }
        
        return devices;
    }
    
    /**
     * 获取Android版本
     * @param deviceId 设备ID
     * @return Android版本号
     * @throws IOException 如果无法获取版本信息
     */
    public String getAndroidVersion(String deviceId) throws IOException {
        return executeAdbCommand("-s", deviceId, "shell", "getprop", "ro.build.version.release");
    }
    
    /**
     * 获取设备型号
     * @param deviceId 设备ID
     * @return 设备型号
     * @throws IOException 如果无法获取设备型号
     */
    public String getDeviceModel(String deviceId) throws IOException {
        return executeAdbCommand("-s", deviceId, "shell", "getprop", "ro.product.model");
    }
    
    /**
     * 截取屏幕截图
     * @param deviceId 设备ID
     * @return 截图字节数据
     * @throws IOException 如果无法截取屏幕
     */
    public byte[] takeScreenshot(String deviceId) throws IOException {
        // 创建临时文件
        Path tempFile = Files.createTempFile("screenshot", ".png");
        String tempFilePath = tempFile.toString();
        
        try {
            // 使用screencap命令截取屏幕
            executeAdbCommand("-s", deviceId, "shell", "screencap", "/sdcard/screenshot.png");
            // 拉取截图文件
            executeAdbCommand("-s", deviceId, "pull", "/sdcard/screenshot.png", tempFilePath);
            // 读取截图数据
            return Files.readAllBytes(tempFile);
        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            // 清理设备上的临时文件
            try {
                executeAdbCommand("-s", deviceId, "shell", "rm", "/sdcard/screenshot.png");
            } catch (IOException e) {
                // 忽略清理错误
                logger.warning("Failed to clean up screenshot on device: " + e.getMessage());
            }
        }
    }
    
    /**
     * 点击屏幕
     * @param deviceId 设备ID
     * @param x x坐标
     * @param y y坐标
     * @throws IOException 如果点击失败
     */
    public void tap(String deviceId, int x, int y) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "input", "tap", String.valueOf(x), String.valueOf(y));
    }
    
    /**
     * 输入文本
     * @param deviceId 设备ID
     * @param text 要输入的文本
     * @throws IOException 如果输入失败
     */
    public void inputText(String deviceId, String text) throws IOException {
        // 处理特殊字符，使用转义
        String escapedText = text.replace(" ", "\\ ")
                                .replace("'", "\\'")
                                .replace("\"", "\\\"")
                                .replace("\\", "\\\\");
        
        executeAdbCommand("-s", deviceId, "shell", "input", "text", escapedText);
    }
    
    /**
     * 滑动屏幕
     * @param deviceId 设备ID
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 滑动持续时间（毫秒）
     * @throws IOException 如果滑动失败
     */
    public void swipe(String deviceId, int startX, int startY, int endX, int endY, int duration) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "input", "swipe", 
                          String.valueOf(startX), String.valueOf(startY), 
                          String.valueOf(endX), String.valueOf(endY), 
                          String.valueOf(duration));
    }
    
    /**
     * 发送按键事件
     * @param deviceId 设备ID
     * @param keyCode 按键代码
     * @throws IOException 如果按键失败
     */
    public void pressKey(String deviceId, int keyCode) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "input", "keyevent", String.valueOf(keyCode));
    }
    
    /**
     * 启动应用
     * @param deviceId 设备ID
     * @param packageName 应用包名
     * @throws IOException 如果启动失败
     */
    public void launchApp(String deviceId, String packageName) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "monkey", "-p", packageName, "-c", 
                          "android.intent.category.LAUNCHER", "1");
    }
    
    /**
     * 关闭应用
     * @param deviceId 设备ID
     * @param packageName 应用包名
     * @throws IOException 如果关闭失败
     */
    public void closeApp(String deviceId, String packageName) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "am", "force-stop", packageName);
    }
    
    /**
     * 获取当前聚焦的应用包名
     * @param deviceId 设备ID
     * @return 当前应用包名
     * @throws IOException 如果获取失败
     */
    public String getCurrentFocusedPackage(String deviceId) throws IOException {
        String output = executeAdbCommand("-s", deviceId, "shell", "dumpsys", "window", "displays");
        
        // 查找mCurrentFocus行
        for (String line : output.split("\n")) {
            if (line.contains("mCurrentFocus")) {
                // 提取包名，格式通常是: mCurrentFocus=Window{... u0 com.package.name/com.package.name.Activity}
                int startIndex = line.indexOf('{');
                if (startIndex > 0) {
                    int packageStart = line.indexOf("u0") + 2;
                    if (packageStart > 0) {
                        int slashIndex = line.indexOf('/', packageStart);
                        if (slashIndex > 0) {
                            return line.substring(packageStart, slashIndex).trim();
                        }
                    }
                }
            }
        }
        
        throw new IOException("Could not determine current focused package");
    }
    
    /**
     * 清除应用数据
     * @param deviceId 设备ID
     * @param packageName 应用包名
     * @throws IOException 如果清除失败
     */
    public void clearAppData(String deviceId, String packageName) throws IOException {
        executeAdbCommand("-s", deviceId, "shell", "pm", "clear", packageName);
    }
    
    /**
     * 安装应用
     * @param deviceId 设备ID
     * @param apkPath APK文件路径
     * @throws IOException 如果安装失败
     */
    public void installApp(String deviceId, String apkPath) throws IOException {
        // 检查APK文件是否存在
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || !apkFile.isFile()) {
            throw new IOException("APK file does not exist: " + apkPath);
        }
        
        executeAdbCommand("-s", deviceId, "install", "-r", apkPath);
    }
    
    /**
     * 卸载应用
     * @param deviceId 设备ID
     * @param packageName 应用包名
     * @throws IOException 如果卸载失败
     */
    public void uninstallApp(String deviceId, String packageName) throws IOException {
        executeAdbCommand("-s", deviceId, "uninstall", packageName);
    }
    
    /**
     * 将文件推送到设备
     * @param deviceId 设备ID
     * @param localPath 本地文件路径
     * @param remotePath 设备文件路径
     * @throws IOException 如果推送失败
     */
    public void pushFile(String deviceId, String localPath, String remotePath) throws IOException {
        executeAdbCommand("-s", deviceId, "push", localPath, remotePath);
    }
    
    /**
     * 从设备拉取文件
     * @param deviceId 设备ID
     * @param remotePath 设备文件路径
     * @param localPath 本地文件路径
     * @throws IOException 如果拉取失败
     */
    public void pullFile(String deviceId, String remotePath, String localPath) throws IOException {
        executeAdbCommand("-s", deviceId, "pull", remotePath, localPath);
    }
    
    /**
     * 运行设备上的命令
     * @param deviceId 设备ID
     * @param command 要执行的命令
     * @return 命令输出
     * @throws IOException 如果执行失败
     */
    public String shellCommand(String deviceId, String command) throws IOException {
        return executeAdbCommand("-s", deviceId, "shell", command);
    }
    
    /**
     * 重启设备
     * @param deviceId 设备ID
     * @throws IOException 如果重启失败
     */
    public void rebootDevice(String deviceId) throws IOException {
        executeAdbCommand("-s", deviceId, "reboot");
    }
    
    /**
     * 获取设备分辨率
     * @param deviceId 设备ID
     * @return 分辨率字符串，格式为"width x height"
     * @throws IOException 如果获取失败
     */
    public String getDeviceResolution(String deviceId) throws IOException {
        String output = executeAdbCommand("-s", deviceId, "shell", "wm", "size");
        
        // 格式通常是: Physical size: 1080x1920
        if (output.contains("Physical size: ")) {
            return output.substring("Physical size: ".length()).trim();
        }
        
        throw new IOException("Could not determine device resolution");
    }
    
    /**
     * 断开ADB连接
     * 这个方法实际上不需要做太多事情，因为ADB连接是由系统管理的
     */
    public void disconnect() {
        logger.info("ADB connection disconnected");
    }
}