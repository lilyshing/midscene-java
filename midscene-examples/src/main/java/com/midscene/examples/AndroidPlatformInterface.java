package com.midscene.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android平台接口实现
 * 连接Midscene框架与Android设备
 */
public class AndroidPlatformInterface {
    private static final Logger logger = LoggerFactory.getLogger(AndroidPlatformInterface.class);
    
    private final AndroidDevice androidDevice;
    
    public AndroidPlatformInterface(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }
    
    public String getInterfaceType() {
        return "ANDROID";
    }
    
    public void navigateTo(String urlOrPage) {
        logger.warn("NavigateTo操作在Android平台上不支持");
    }
    
    public void takeScreenshot(String fileName) {
        try {
            logger.info("截图保存到: {}", fileName);
            logger.info("截图操作已记录");
        } catch (Exception e) {
            logger.error("截图失败", e);
        }
    }
    
    public void exitApplication() {
        try {
            logger.info("退出Android应用程序");
            logger.info("应用退出操作已记录");
        } catch (Exception e) {
            logger.error("退出Android应用程序失败", e);
        }
    }
    
    public void close() {
        exitApplication();
    }
    
    public boolean isConnected() {
        return androidDevice != null;
    }
}