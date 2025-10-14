package com.midscene.android;

import com.midscene.core.exception.PlatformException;
import com.midscene.shared.platform.PlatformInfo;
import com.midscene.shared.platform.PlatformInfo.PlatformType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Android设备管理类
 * 提供设备发现、连接、信息查询等功能
 */
public class AndroidDevice {
    private static final Logger logger = Logger.getLogger(AndroidDevice.class.getName());
    private AdbClient adbClient;
    private String deviceId;
    private String deviceModel;
    private String androidVersion;
    private String manufacturer;
    private String serialNumber;
    private boolean isConnected;
    private PlatformInfo platformInfo;

    /**
     * 构造函数
     * @param deviceId 设备ID
     */
    public AndroidDevice(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        this.deviceId = deviceId;
        this.adbClient = new AdbClient();
        this.platformInfo = new PlatformInfo(PlatformType.ANDROID, "unknown", "unknown", "Android");
    }

    /**
     * 获取设备ID
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 连接设备
     * @return 是否连接成功
     */
    public boolean connect() {
        try {
            logger.info("Connecting to Android device: " + deviceId);
            
            // 检查设备是否已连接
            List<String> connectedDevices = adbClient.getConnectedDevices();
            if (!connectedDevices.contains(deviceId)) {
                logger.warning("Device not found in connected devices list");
                return false;
            }

            // 获取设备信息
            updateDeviceInfo();
            
            isConnected = true;
            logger.info("Successfully connected to device: " + deviceId + " (" + deviceModel + ")");
            return true;
        } catch (Exception e) {
            logger.severe("Failed to connect to device: " + deviceId + ", " + e.getMessage());
            throw new PlatformException("Failed to connect to device: " + deviceId, e);
        }
    }

    /**
     * 断开设备连接
     */
    public void disconnect() {
        try {
            logger.info("Disconnecting from device: " + deviceId);
            // 这里可以添加清理资源的逻辑
            isConnected = false;
            logger.info("Successfully disconnected from device: " + deviceId);
        } catch (Exception e) {
            logger.warning("Error disconnecting from device: " + deviceId + ", " + e.getMessage());
        }
    }

    /**
     * 检查设备是否已连接
     * @return 是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 获取设备模型
     * @return 设备模型
     */
    public String getDeviceModel() {
        return deviceModel;
    }

    /**
     * 获取Android版本
     * @return Android版本
     */
    public String getAndroidVersion() {
        return androidVersion;
    }

    /**
     * 获取制造商
     * @return 制造商
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * 获取序列号
     * @return 序列号
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * 获取平台信息
     * @return 平台信息
     */
    public PlatformInfo getPlatformInfo() {
        return platformInfo;
    }

    /**
     * 更新设备信息
     */
    private void updateDeviceInfo() {
        try {
            this.deviceModel = adbClient.getDeviceModel(deviceId);
            this.androidVersion = adbClient.getAndroidVersion(deviceId);
            
            // 使用shell命令获取制造商信息
            try {
                this.manufacturer = adbClient.shellCommand(deviceId, "getprop ro.product.manufacturer");
            } catch (Exception e) {
                this.manufacturer = "unknown";
                logger.warning("Failed to get manufacturer: " + e.getMessage());
            }
            
            // 使用shell命令获取序列号
            try {
                this.serialNumber = adbClient.shellCommand(deviceId, "getprop ro.serialno");
            } catch (Exception e) {
                this.serialNumber = "unknown";
                logger.warning("Failed to get serial number: " + e.getMessage());
            }
            
            // 更新平台信息
            this.platformInfo = new PlatformInfo(
                PlatformType.ANDROID,
                androidVersion,
                deviceModel,
                "Android"
            );
        } catch (Exception e) {
            logger.warning("Failed to update device info: " + e.getMessage());
        }
    }

    /**
     * 重启设备
     */
    public void reboot() {
        try {
            logger.info("Rebooting device: " + deviceId);
            adbClient.rebootDevice(deviceId);
            logger.info("Device reboot initiated: " + deviceId);
        } catch (Exception e) {
            logger.severe("Failed to reboot device: " + deviceId + ", " + e.getMessage());
            throw new PlatformException("Failed to reboot device: " + deviceId, e);
        }
    }

    /**
     * 安装应用
     * @param apkPath APK文件路径
     * @return 安装是否成功
     */
    public boolean installApp(String apkPath) {
        try {
            logger.info("Installing app to device " + deviceId + ": " + apkPath);
            
            File apkFile = new File(apkPath);
            if (!apkFile.exists() || !apkFile.isFile() || !apkPath.endsWith(".apk")) {
                throw new IllegalArgumentException("Invalid APK file: " + apkPath);
            }
            
            adbClient.installApp(deviceId, apkPath);
            logger.info("Successfully installed app: " + apkPath);
            return true;
        } catch (Exception e) {
            logger.severe("Error installing app: " + apkPath + ", " + e.getMessage());
            throw new PlatformException("Failed to install app: " + apkPath, e);
        }
    }

    /**
     * 卸载应用
     * @param packageName 应用包名
     * @return 卸载是否成功
     */
    public boolean uninstallApp(String packageName) {
        try {
            logger.info("Uninstalling app from device " + deviceId + ": " + packageName);
            
            adbClient.uninstallApp(deviceId, packageName);
            logger.info("Successfully uninstalled app: " + packageName);
            return true;
        } catch (Exception e) {
            logger.severe("Error uninstalling app: " + packageName + ", " + e.getMessage());
            throw new PlatformException("Failed to uninstall app: " + packageName, e);
        }
    }

    /**
     * 获取已安装的应用列表
     * @return 应用列表
     */
    public List<Map<String, String>> getInstalledApps() {
        try {
            logger.info("Getting installed apps for device: " + deviceId);
            // 使用shell命令获取已安装应用
            String output = adbClient.shellCommand(deviceId, "pm list packages -f");
            List<Map<String, String>> apps = new ArrayList<>();
            
            for (String line : output.split("\\n")) {
                if (line.startsWith("package:")) {
                    Map<String, String> appInfo = new HashMap<>();
                    // 提取路径和包名
                    int eqIndex = line.indexOf('=');
                    if (eqIndex > 0) {
                        String packageName = line.substring(eqIndex + 1);
                        appInfo.put("package", packageName);
                        apps.add(appInfo);
                    }
                }
            }
            
            return apps;
        } catch (Exception e) {
            logger.severe("Failed to get installed apps: " + e.getMessage());
            throw new PlatformException("Failed to get installed apps", e);
        }
    }

    /**
     * 获取设备屏幕分辨率
     * @return 屏幕分辨率信息
     */
    public Map<String, Integer> getScreenResolution() {
        try {
            logger.info("Getting screen resolution for device: " + deviceId);
            String resolutionStr = adbClient.getDeviceResolution(deviceId);
            Map<String, Integer> resolution = new HashMap<>();
            
            // 解析分辨率字符串，格式为"width x height"
            String[] parts = resolutionStr.split("x");
            if (parts.length == 2) {
                resolution.put("width", Integer.parseInt(parts[0].trim()));
                resolution.put("height", Integer.parseInt(parts[1].trim()));
            }
            
            return resolution;
        } catch (Exception e) {
            logger.severe("Failed to get screen resolution: " + e.getMessage());
            throw new PlatformException("Failed to get screen resolution", e);
        }
    }

    /**
     * 检查应用是否已安装
     * @param packageName 应用包名
     * @return 是否已安装
     */
    public boolean isAppInstalled(String packageName) {
        try {
            List<Map<String, String>> installedApps = getInstalledApps();
            for (Map<String, String> app : installedApps) {
                if (packageName.equals(app.get("package"))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warning("Error checking if app is installed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 启动应用
     * @param packageName 应用包名
     */
    public void launchApp(String packageName) {
        try {
            logger.info("Launching app on device " + deviceId + ": " + packageName);
            adbClient.launchApp(deviceId, packageName);
            logger.info("Successfully launched app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to launch app: " + packageName + ", " + e.getMessage());
            throw new PlatformException("Failed to launch app: " + packageName, e);
        }
    }

    /**
     * 停止应用
     * @param packageName 应用包名
     */
    public void stopApp(String packageName) {
        try {
            logger.info("Stopping app on device " + deviceId + ": " + packageName);
            adbClient.closeApp(deviceId, packageName);
            logger.info("Successfully stopped app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to stop app: " + packageName + ", " + e.getMessage());
            throw new PlatformException("Failed to stop app: " + packageName, e);
        }
    }

    /**
     * 获取设备状态信息
     * @return 设备状态信息
     */
    public Map<String, Object> getDeviceStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("deviceId", deviceId);
            status.put("connected", isConnected);
            status.put("model", deviceModel);
            status.put("androidVersion", androidVersion);
            status.put("manufacturer", manufacturer);
            status.put("serialNumber", serialNumber);
            status.put("screenResolution", getScreenResolution());
            return status;
        } catch (Exception e) {
            logger.severe("Failed to get device status: " + e.getMessage());
            throw new PlatformException("Failed to get device status", e);
        }
    }

    /**
     * 工厂方法：发现并返回所有连接的Android设备
     * @return 设备列表
     */
    public static List<AndroidDevice> findDevices() {
        try {
            logger.info("Discovering Android devices");
            AdbClient adbClient = new AdbClient();
            List<String> deviceIds = adbClient.getConnectedDevices();
            List<AndroidDevice> devices = new ArrayList<>();
            
            for (String deviceId : deviceIds) {
                AndroidDevice device = new AndroidDevice(deviceId);
                if (device.connect()) {
                    devices.add(device);
                }
            }
            
            logger.info("Found " + devices.size() + " connected Android devices");
            return devices;
        } catch (Exception e) {
            logger.severe("Failed to discover devices: " + e.getMessage());
            throw new PlatformException("Failed to discover Android devices", e);
        }
    }

    /**
     * 关闭应用
     * @param packageName 应用包名
     */
    public void closeApp(String packageName) {
        try {
            logger.info("Closing app on device " + deviceId + ": " + packageName);
            adbClient.closeApp(deviceId, packageName);
            logger.info("Successfully closed app: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to close app: " + packageName + ", " + e.getMessage());
            throw new PlatformException("Failed to close app: " + packageName, e);
        }
    }
    
    /**
     * 获取当前焦点包名
     * @return 当前焦点包名
     */
    public String getCurrentFocusedPackage() {
        try {
            logger.info("Getting current focused package for device: " + deviceId);
            return adbClient.getCurrentFocusedPackage(deviceId);
        } catch (Exception e) {
            logger.severe("Failed to get current focused package: " + e.getMessage());
            throw new PlatformException("Failed to get current focused package", e);
        }
    }
    
    /**
     * 清除应用数据
     * @param packageName 应用包名
     */
    public void clearAppData(String packageName) {
        try {
            logger.info("Clearing app data for device " + deviceId + ": " + packageName);
            adbClient.clearAppData(deviceId, packageName);
            logger.info("Successfully cleared app data: " + packageName);
        } catch (Exception e) {
            logger.severe("Failed to clear app data: " + packageName + ", " + e.getMessage());
            throw new PlatformException("Failed to clear app data: " + packageName, e);
        }
    }
    
    /**
     * 输入文本
     * @param text 要输入的文本
     */
    public void inputText(String text) {
        try {
            logger.info("Inputting text on device " + deviceId + ": " + text);
            adbClient.inputText(deviceId, text);
            logger.info("Successfully input text");
        } catch (Exception e) {
            logger.severe("Failed to input text: " + e.getMessage());
            throw new PlatformException("Failed to input text", e);
        }
    }
    
    /**
     * 执行滑动操作
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 持续时间（毫秒）
     */
    public void swipe(int startX, int startY, int endX, int endY, int duration) {
        try {
            logger.info("Performing swipe on device " + deviceId + ": (" + startX + "," + startY + ") -> (" + endX + "," + endY + ") duration=" + duration);
            adbClient.swipe(deviceId, startX, startY, endX, endY, duration);
            logger.info("Successfully performed swipe");
        } catch (Exception e) {
            logger.severe("Failed to perform swipe: " + e.getMessage());
            throw new PlatformException("Failed to perform swipe", e);
        }
    }
    
    /**
     * 按键操作
     * @param keyCode 键码
     */
    public void pressKey(int keyCode) {
        try {
            logger.info("Pressing key on device " + deviceId + ": " + keyCode);
            adbClient.pressKey(deviceId, keyCode);
            logger.info("Successfully pressed key");
        } catch (Exception e) {
            logger.severe("Failed to press key: " + e.getMessage());
            throw new PlatformException("Failed to press key", e);
        }
    }
    
    /**
     * 截取屏幕截图
     * @return 截图字节数组
     */
    public byte[] takeScreenshot() {
        try {
            logger.info("Taking screenshot on device: " + deviceId);
            byte[] screenshotBytes = adbClient.takeScreenshot(deviceId);
            logger.info("Successfully took screenshot");
            return screenshotBytes;
        } catch (Exception e) {
            logger.severe("Failed to take screenshot: " + e.getMessage());
            throw new PlatformException("Failed to take screenshot", e);
        }
    }
    
    /**
     * 点击屏幕指定位置
     * @param x X坐标
     * @param y Y坐标
     */
    public void tap(int x, int y) {
        try {
            logger.info("Tapping on device " + deviceId + " at position: (" + x + ", " + y + ")");
            adbClient.tap(deviceId, x, y);
            logger.info("Successfully tapped at position: (" + x + ", " + y + ")");
        } catch (Exception e) {
            logger.severe("Failed to tap at position (" + x + ", " + y + "): " + e.getMessage());
            throw new PlatformException("Failed to tap at position (" + x + ", " + y + ")", e);
        }
    }
    
    @Override
    public String toString() {
        return "AndroidDevice{" +
                "deviceId='" + deviceId + "'" +
                ", deviceModel='" + deviceModel + "'" +
                ", androidVersion='" + androidVersion + "'" +
                ", connected=" + isConnected +
                "}";
    }
}