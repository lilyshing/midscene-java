package com.midscene.shared.constants;

/**
 * 共享常量定义类
 */
public class Constants {
    // 文本相关常量
    public static final int TEXT_SIZE_THRESHOLD = 9;
    public static final int TEXT_MAX_SIZE = 40;
    
    // 容器相关常量
    public static final int CONTAINER_MINI_HEIGHT = 3;
    public static final int CONTAINER_MINI_WIDTH = 3;
    
    // 服务器端口常量
    public static final int PLAYGROUND_SERVER_PORT = 5800;
    public static final int SCRCPY_SERVER_PORT = 5700;
    
    // WebDriver常量
    public static final String WEBDRIVER_ELEMENT_ID_KEY = "element-6066-11e4-a52e-4f735466cecf";
    public static final int DEFAULT_WDA_PORT = 8100;
    
    // 等待相关常量
    public static final long DEFAULT_WAIT_FOR_NAVIGATION_TIMEOUT = 5000L;
    public static final long DEFAULT_WAIT_FOR_NETWORK_IDLE_TIMEOUT = 2000L;
    public static final long DEFAULT_WAIT_FOR_NETWORK_IDLE_TIME = 300L;
    public static final int DEFAULT_WAIT_FOR_NETWORK_IDLE_CONCURRENCY = 2;
    
    private Constants() {
        // 防止实例化
    }
}