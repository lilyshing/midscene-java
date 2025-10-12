package com.midscene.core.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.StepResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.model.StepStatus;
import com.midscene.core.model.UiContext;
import com.midscene.core.util.LoggerUtil;
import com.midscene.core.exception.ExceptionUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Agent类 - Midscene框架的核心控制器
 * 负责协调AI模型和平台交互，提供统一的自动化操作接口
 */
public class Agent {
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);
    
    private final PlatformInterface platform;
    private final AgentOptions options;
    private UiContext frozenContext;
    private boolean destroyed = false;
    private final AIModelService aiModelService;
    private final InsightEngine insightEngine;
    private final TaskExecutor taskExecutor;
    
    /**
     * 构造函数
     * @param platform 平台接口实现
     * @param options 配置选项
     */
    public Agent(PlatformInterface platform, AgentOptions options) {
        if (platform == null) {
            throw new IllegalArgumentException("Platform cannot be null");
        }
        this.platform = platform;
        this.options = options != null ? options : new AgentOptions();
        
        // 初始化AI服务
        this.aiModelService = new AIModelService(options);
        
        // 初始化洞察引擎
        this.insightEngine = new InsightEngine(
            this::getUiContext,
            aiModelService
        );
        
        // 初始化任务执行器
        this.taskExecutor = new TaskExecutor(
            platform,
            insightEngine,
            aiModelService,
            options
        );
        
        logger.info("Agent initialized for {}", platform.getInterfaceType());
    }

    /**
     * 构造函数 - 使用默认选项
     * @param platform 平台对象
     */
    public Agent(PlatformInterface platform) {
        this(platform, null);
    }
    
    /**
     * 执行AI操作
     * @param prompt 提示词
     * @return 执行结果
     */
    public CompletableFuture<TaskResult> aiAction(String prompt) {
        _ensureNotDestroyed();
        
        logger.info("Executing AI action: {}", prompt);
        
        // 创建任务结果对象
        TaskResult result = new TaskResult();
        result.setStatus(TaskStatus.RUNNING);
        
        try {
            // 委托给任务执行器
            return taskExecutor.executeAiAction(prompt)
                .thenApply(executeResult -> {
                    result.setStatus(TaskStatus.COMPLETED);
                    result.complete();
                    return result;
                })
                .exceptionally(ex -> {
                    String errorMsg = ExceptionUtil.getRootCause(ex).getMessage();
                    result.fail(errorMsg);
                    return result;
                });
        } catch (Exception e) {
            result.fail(e.getMessage());
            return CompletableFuture.completedFuture(result);
        }
    }
    
    /**
     * 智能元素定位
     * @param elementDescription 元素描述
     * @return 定位结果
     */
    public CompletableFuture<LocateResult> aiLocate(String elementDescription) {
        _ensureNotDestroyed();
        
        logger.info("Executing AI locate: {}", elementDescription);
        
        return insightEngine.locate(elementDescription);
    }
    
    /**
     * 冻结当前页面状态
     * @return 操作是否成功
     */
    public CompletableFuture<Boolean> freeze() {
        _ensureNotDestroyed();
        
        if (frozenContext != null) {
            logger.warn("Context already frozen");
            return CompletableFuture.completedFuture(true);
        }
        
        return getUiContext()
            .thenApply(context -> {
                this.frozenContext = context;
                logger.info("Context frozen successfully");
                return true;
            });
    }
    
    /**
     * 解除页面状态冻结
     */
    public void unfreeze() {
        _ensureNotDestroyed();
        
        this.frozenContext = null;
        logger.info("Context unfrozen");
    }
    
    /**
     * 获取UI上下文
     * @return UI上下文对象
     */
    private CompletableFuture<UiContext> getUiContext() {
        if (frozenContext != null) {
            // 如果上下文已冻结，返回冻结的上下文
            return CompletableFuture.completedFuture(frozenContext);
        }
        
        // 从平台获取最新上下文
        return platform.getUiContext();
    }
    
    /**
     * 关闭Agent，释放资源
     */
    public void close() {
        if (destroyed) {
            return;
        }
        
        destroyed = true;
        frozenContext = null;
        
        // 关闭平台连接
        if (platform != null) {
            platform.close();
        }
        
        // 关闭AI服务
        if (aiModelService != null) {
            aiModelService.close();
        }
        
        logger.info("Agent closed and resources released");
    }
    
    /**
     * 确保Agent未被销毁
     */
    private void _ensureNotDestroyed() {
        if (destroyed) {
            throw new IllegalStateException("Agent has been destroyed");
        }
    }
    
    /**
     * 获取平台类型
     * @return 平台类型字符串
     */
    public String getInterfaceType() {
        return platform.getInterfaceType();
    }
    
    /**
     * 检查Agent是否已销毁
     * @return 是否已销毁
     */
    public boolean isDestroyed() {
        return destroyed;
    }
    
    /**
     * 检查上下文是否已冻结
     * @return 是否已冻结
     */
    public boolean isContextFrozen() {
        return frozenContext != null;
    }
}