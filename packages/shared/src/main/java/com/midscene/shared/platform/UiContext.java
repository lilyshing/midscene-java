package com.midscene.shared.platform;

import java.util.List;

/**
 * UI上下文类，包含当前界面的所有信息
 */
public class UiContext {
    private final String screenshotBase64;
    private final List<UiElement> elements;
    private final String platformSpecificData;
    private final long timestamp;
    
    public UiContext(String screenshotBase64, List<UiElement> elements, String platformSpecificData) {
        this.screenshotBase64 = screenshotBase64;
        this.elements = elements;
        this.platformSpecificData = platformSpecificData;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getScreenshotBase64() {
        return screenshotBase64;
    }
    
    public List<UiElement> getElements() {
        return elements;
    }
    
    public String getPlatformSpecificData() {
        return platformSpecificData;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}