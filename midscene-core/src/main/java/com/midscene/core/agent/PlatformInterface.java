package com.midscene.core.agent;

import com.midscene.core.model.UiContext;
import java.util.concurrent.CompletableFuture;

/**
 * 平台接口，定义Agent与不同平台交互的标准方法
 * 支持Web、Android等不同平台的统一操作接口
 */
public interface PlatformInterface {
    
    /**
     * 获取当前平台类型
     * @return 平台类型字符串
     */
    String getInterfaceType();
    
    /**
     * 获取当前UI上下文（包含截图、节点信息等）
     * @return UI上下文对象
     */
    CompletableFuture<UiContext> getUiContext();
    
    /**
     * 点击指定坐标位置
     * @param x X坐标
     * @param y Y坐标
     * @return 是否成功
     */
    CompletableFuture<Boolean> tap(int x, int y);
    
    /**
     * 在指定坐标位置输入文本
     * @param text 要输入的文本
     * @param x 目标区域X坐标
     * @param y 目标区域Y坐标
     * @return 是否成功
     */
    CompletableFuture<Boolean> inputText(String text, int x, int y);
    
    /**
     * 滚动页面
     * @param direction 滚动方向（up/down/left/right）
     * @param distance 滚动距离
     * @return 是否成功
     */
    CompletableFuture<Boolean> scroll(String direction, int distance);
    
    /**
     * 导航到指定URL（Web平台专用）
     * @param url 目标URL
     * @return 是否成功
     */
    CompletableFuture<Boolean> navigate(String url);
    
    /**
     * 导航到指定URL或页面（通用方法）
     * @param urlOrPage 目标URL或页面标识符
     */
    void navigateTo(String urlOrPage);
    
    /**
     * 等待页面加载
     * @param timeout 超时时间（毫秒）
     * @return 是否成功
     */
    CompletableFuture<Boolean> waitForPageLoad(long timeout);
    
    /**
     * 截取当前页面/屏幕的截图
     * @param fileName 截图文件名
     */
    void takeScreenshot(String fileName);
    
    /**
     * 退出应用程序或关闭浏览器
     */
    void exitApplication();
    
    /**
     * 关闭平台连接，释放资源
     */
    void close();
    
    /**
     * 检查平台连接是否有效
     * @return 连接状态
     */
    boolean isConnected();
}