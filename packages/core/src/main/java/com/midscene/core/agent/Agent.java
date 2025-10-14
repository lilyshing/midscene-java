package com.midscene.core.agent;

import com.midscene.shared.platform.PlatformInterface;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent抽象类，为各平台的Agent实现提供基础框架
 * 定义了AI驱动的自动化操作核心方法
 */
public abstract class Agent {
    protected final PlatformInterface platform;
    protected final AIModelService aiModelService;
    protected final InsightEngine insightEngine;
    protected final TaskExecutor taskExecutor;
    
    public Agent(PlatformInterface platform, AIModelService aiModelService,
                InsightEngine insightEngine, TaskExecutor taskExecutor) {
        this.platform = platform;
        this.aiModelService = aiModelService;
        this.insightEngine = insightEngine;
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * 初始化Agent
     * @param options 初始化选项
     * @return 初始化是否成功
     */
    public CompletableFuture<Boolean> initialize(Map<String, Object> options) {
        return platform.initialize(options);
    }
    
    /**
     * 执行基于AI的通用操作
     * @param instruction 自然语言指令
     * @return 任务执行结果
     */
    public abstract CompletableFuture<TaskResult> aiAction(String instruction);
    
    /**
     * 执行基于AI的点击操作
     * @param targetDescription 目标描述
     * @return 任务执行结果
     */
    public abstract CompletableFuture<TaskResult> aiTap(String targetDescription);
    
    /**
     * 执行基于AI的输入操作
     * @param targetDescription 目标描述
     * @param text 要输入的文本
     * @return 任务执行结果
     */
    public abstract CompletableFuture<TaskResult> aiInput(String targetDescription, String text);
    
    /**
     * 执行基于AI的数据提取操作
     * @param targetDescription 目标描述
     * @return 提取的结果
     */
    public abstract CompletableFuture<String> extractData(String targetDescription);
    
    /**
     * 执行截图操作
     * @return 截图数据（Base64编码）
     */
    public CompletableFuture<String> takeScreenshot() {
        return platform.screenshot();
    }
    
    /**
     * 获取当前平台信息
     * @return 平台信息
     */
    public PlatformInterface getPlatform() {
        return platform;
    }
    
    /**
     * 关闭Agent
     * @return 关闭操作的CompletableFuture
     */
    public CompletableFuture<Void> close() {
        return platform.close();
    }
}