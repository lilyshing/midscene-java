package com.midscene.shared.platform;

/**
 * 平台信息类，包含平台的基本信息
 */
public class PlatformInfo {
    private final PlatformType type;
    private final String version;
    private final String deviceName;
    private final String osVersion;
    
    public PlatformInfo(PlatformType type, String version, String deviceName, String osVersion) {
        this.type = type;
        this.version = version;
        this.deviceName = deviceName;
        this.osVersion = osVersion;
    }
    
    public PlatformType getType() {
        return type;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    /**
     * 平台类型枚举
     */
    public enum PlatformType {
        WEB,
        ANDROID,
        IOS
    }
}