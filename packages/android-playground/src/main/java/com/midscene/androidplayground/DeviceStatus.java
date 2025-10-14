package com.midscene.androidplayground;

/**
 * 设备状态枚举
 */
public enum DeviceStatus {
    /** 在线，可以正常使用 */
    ONLINE,
    /** 离线，无法连接 */
    OFFLINE,
    /** 正在连接 */
    CONNECTING,
    /** 连接失败 */
    CONNECTION_FAILED,
    /** 正在准备 */
    PREPARING
}