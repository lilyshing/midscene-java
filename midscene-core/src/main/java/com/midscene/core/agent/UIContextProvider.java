package com.midscene.core.agent;

import com.midscene.core.model.UiElement;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * UI上下文提供者接口，负责获取当前UI状态和元素
 */
public interface UIContextProvider {
    /**
     * 获取当前UI上下文的文本描述
     * @return UI上下文描述
     */
    CompletableFuture<String> getCurrentUIContext();
    
    /**
     * 获取当前屏幕上的所有UI元素
     * @return UI元素列表
     */
    CompletableFuture<List<UiElement>> getCurrentUIElements();
    
    /**
     * 刷新UI上下文（重新获取最新的UI状态）
     * @return 刷新是否成功
     */
    CompletableFuture<Boolean> refresh();
}