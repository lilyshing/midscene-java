package com.midscene.chromeextension.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ChromeExtensionService {

    /**
     * 初始化Chrome扩展桥接服务
     */
    void initializeBridgeService();

    /**
     * 处理来自Chrome扩展的消息
     * @param message 消息内容
     * @return 响应结果
     */
    CompletableFuture<Map<String, Object>> processExtensionMessage(Map<String, Object> message);

    /**
     * 启动录制功能
     * @param options 录制选项
     * @return 录制ID
     */
    String startRecording(Map<String, Object> options);

    /**
     * 停止录制功能
     * @param recordingId 录制ID
     * @return 录制结果
     */
    Map<String, Object> stopRecording(String recordingId);

    /**
     * 获取扩展配置信息
     * @return 配置信息
     */
    Map<String, String> getExtensionConfig();

    /**
     * 更新扩展配置
     * @param config 新的配置信息
     * @return 更新是否成功
     */
    boolean updateExtensionConfig(Map<String, String> config);

    /**
     * 发送消息到Chrome扩展
     * @param message 要发送的消息
     * @return 发送是否成功
     */
    CompletableFuture<Boolean> sendMessageToExtension(Map<String, Object> message);

}