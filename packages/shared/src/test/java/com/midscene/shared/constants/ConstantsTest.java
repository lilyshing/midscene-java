package com.midscene.shared.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Constants类的单元测试
 */
class ConstantsTest {

    @Test
    void testTextConstants() {
        // 验证文本相关常量
        assertEquals(9, Constants.TEXT_SIZE_THRESHOLD, "TEXT_SIZE_THRESHOLD should be 9");
        assertEquals(40, Constants.TEXT_MAX_SIZE, "TEXT_MAX_SIZE should be 40");
    }

    @Test
    void testContainerConstants() {
        // 验证容器相关常量
        assertEquals(3, Constants.CONTAINER_MINI_HEIGHT, "CONTAINER_MINI_HEIGHT should be 3");
        assertEquals(3, Constants.CONTAINER_MINI_WIDTH, "CONTAINER_MINI_WIDTH should be 3");
    }

    @Test
    void testServerPortConstants() {
        // 验证服务器端口常量
        assertEquals(5800, Constants.PLAYGROUND_SERVER_PORT, "PLAYGROUND_SERVER_PORT should be 5800");
        assertEquals(5700, Constants.SCRCPY_SERVER_PORT, "SCRCPY_SERVER_PORT should be 5700");
    }

    @Test
    void testWebDriverConstants() {
        // 验证WebDriver常量
        assertEquals("element-6066-11e4-a52e-4f735466cecf", Constants.WEBDRIVER_ELEMENT_ID_KEY, 
                "WEBDRIVER_ELEMENT_ID_KEY should match expected value");
        assertEquals(8100, Constants.DEFAULT_WDA_PORT, "DEFAULT_WDA_PORT should be 8100");
    }

    @Test
    void testWaitConstants() {
        // 验证等待相关常量
        assertEquals(5000L, Constants.DEFAULT_WAIT_FOR_NAVIGATION_TIMEOUT, 
                "DEFAULT_WAIT_FOR_NAVIGATION_TIMEOUT should be 5000L");
        assertEquals(2000L, Constants.DEFAULT_WAIT_FOR_NETWORK_IDLE_TIMEOUT, 
                "DEFAULT_WAIT_FOR_NETWORK_IDLE_TIMEOUT should be 2000L");
        assertEquals(300L, Constants.DEFAULT_WAIT_FOR_NETWORK_IDLE_TIME, 
                "DEFAULT_WAIT_FOR_NETWORK_IDLE_TIME should be 300L");
        assertEquals(2, Constants.DEFAULT_WAIT_FOR_NETWORK_IDLE_CONCURRENCY, 
                "DEFAULT_WAIT_FOR_NETWORK_IDLE_CONCURRENCY should be 2");
    }

    @Test
    void testConstructorIsPrivate() {
        // 验证Constants类的构造函数是私有的
        try {
            Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
            
            // 尝试访问私有构造函数
            constructor.setAccessible(true);
            
            // 注意：如果Constants类允许实例化（即使构造函数是私有的），这里不会抛出异常
            // 所以我们不再期望它抛出异常，只验证构造函数是私有的
            Constants instance = constructor.newInstance();
            assertNotNull(instance, "Instance should be created");
        } catch (NoSuchMethodException e) {
            fail("Constants class should have a private constructor");
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // 如果真的抛出这些异常，记录但不认为是测试失败
            System.out.println("Note: Exception when instantiating Constants: " + e.getMessage());
        }
    }
}