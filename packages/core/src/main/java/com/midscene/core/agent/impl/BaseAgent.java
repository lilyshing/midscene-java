package com.midscene.core.agent.impl;

import com.midscene.core.agent.Agent;
import com.midscene.core.cache.AIModelCache;
import com.midscene.core.cache.UiContextCache;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.service.AIModelService;
import com.midscene.core.service.InsightEngine;
import com.midscene.core.service.TaskExecutor;
import com.midscene.shared.model.ai.AIActionRequest;
import com.midscene.shared.model.ai.AIActionResult;
import com.midscene.shared.model.ai.AIInputRequest;
import com.midscene.shared.model.ai.AIInputResult;
import com.midscene.shared.model.ai.AITapRequest;
import com.midscene.shared.model.ai.AITapResult;
import com.midscene.shared.model.ai.ExtractDataRequest;
import com.midscene.shared.model.ai.ExtractDataResult;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 基础Agent实现类
 * 提供AI驱动的自动化操作核心功能，支持异步处理、超时控制和任务取消
 */
public class BaseAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(BaseAgent.class);
    private static final long DEFAULT_TIMEOUT_MS = 60000; // 默认超时时间：60秒
    private final Map<String, CompletableFuture<?>> activeTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
        1,
        r -> {
            Thread thread = new Thread(r, "midscene-agent-timeout-scheduler");
            thread.setDaemon(true);
            return thread;
        }
    );
    
    // UI上下文多级缓存
    private final UiContextCache uiContextCache;
    
    // AI模型结果缓存
    private final AIModelCache aiModelCache;

    public BaseAgent(PlatformInterface platform, AIModelService aiModelService, InsightEngine insightEngine, TaskExecutor taskExecutor) {
        super(platform, aiModelService, insightEngine, taskExecutor);
        // 初始化UI上下文缓存：一级缓存300ms（快速访问），二级缓存3s（较持久）
        this.uiContextCache = new UiContextCache(300, 3000);
        // 初始化AI模型结果缓存：缓存30秒，最多1000条记录
        this.aiModelCache = new AIModelCache(30000, 1000);
    }

    /**
     * 获取UI上下文，带错误处理、超时支持和多级缓存
     */
    private CompletableFuture<UiContext> getUiContextAsync(long timeoutMs) {
        try {
            // 尝试从缓存获取
            UiContext cachedContext = uiContextCache.get();
            if (cachedContext != null) {
                logger.debug("Using cached UI context");
                return CompletableFuture.completedFuture(cachedContext);
            }
            
            // 缓存未命中，从平台获取新的UI上下文
            return applyTimeout(platform.getCurrentUiContext(), timeoutMs)
                .thenApply(uiContext -> {
                    // 获取成功后放入缓存
                    if (uiContext != null) {
                        uiContextCache.put(uiContext);
                    }
                    return uiContext;
                })
                .exceptionally(ex -> {
                    logger.error("Failed to get UI context", ex);
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error in getUiContextAsync", e);
            return CompletableFuture.completedFuture(null);
        }
    }
    
    /**
     * 手动使UI上下文缓存失效
     * 应在页面状态可能发生变化后调用
     */
    protected void invalidateUiContextCache() {
        logger.debug("Invalidating UI context cache");
        uiContextCache.invalidate();
    }
    
    /**
     * 基于操作类型使UI上下文缓存智能失效
     * @param operationType 操作类型
     */
    protected void invalidateUiContextCacheBasedOnOperation(UiContextCache.OperationType operationType) {
        uiContextCache.invalidateBasedOnOperation(operationType, null);
    }
    
    /**
     * 基于操作类型使UI上下文缓存智能失效
     * @param operationType 操作类型
     * @param affectedElements 受影响的元素ID列表
     */
    protected void invalidateUiContextCacheBasedOnOperation(UiContextCache.OperationType operationType, List<String> affectedElements) {
        uiContextCache.invalidateBasedOnOperation(operationType, affectedElements);
    }
    
    /**
     * 根据任务描述确定操作类型
     * @param task 任务描述
     * @return 操作类型
     */
    private UiContextCache.OperationType determineOperationType(String task) {
        if (task == null) {
            return UiContextCache.OperationType.UNKNOWN;
        }
        
        String lowerTask = task.toLowerCase();
        
        // 导航操作关键词
        if (containsAny(lowerTask, "navigate", "go to", "open", "close", "back", "forward", "refresh", "reload", "switch", "change tab")) {
            return UiContextCache.OperationType.NAVIGATE;
        }
        
        // 修改操作关键词
        if (containsAny(lowerTask, "click", "tap", "press", "type", "input", "fill", "select", "drag", "drop", "submit", "clear", "delete", "remove", "add", "toggle", "switch")) {
            return UiContextCache.OperationType.MODIFY;
        }
        
        // 只读操作关键词
        if (containsAny(lowerTask, "find", "locate", "identify", "check", "verify", "read", "view", "get", "retrieve", "list", "show", "display", "scan", "search")) {
            return UiContextCache.OperationType.READ;
        }
        
        return UiContextCache.OperationType.UNKNOWN;
    }
    
    /**
     * 手动使AI模型缓存失效
     * @param type 缓存类型
     */
    protected void invalidateAIModelCache(AIModelCache.CacheType type) {
        logger.debug("Invalidating AI model cache: {}", type);
        aiModelCache.invalidateByType(type);
    }
    
    /**
     * 清理所有AI模型缓存
     */
    protected void clearAIModelCache() {
        logger.debug("Clearing all AI model caches");
        aiModelCache.clear();
    }
    
    /**
     * 检查字符串是否包含任一关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取AI模型缓存统计信息
     */
    public String getAIModelCacheStatistics() {
        return aiModelCache.getStatistics().toString();
    }
    
    /**
     * 获取Agent类型（平台类型）
     */
    private String getAgentType() {
        try {
            if (platform != null && platform.getPlatformInfo() != null) {
                return platform.getPlatformInfo().getType().name();
            }
        } catch (Exception e) {
            logger.warn("Failed to get agent type: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
    
    @Override
    public CompletableFuture<TaskResult> aiAction(String instruction) {
        // 为操作生成唯一ID
        String actionId = generateTaskId("aiAction");
        
        CompletableFuture<TaskResult> actionFuture = CompletableFuture.supplyAsync(() -> {
            logger.info("Starting AI action: {}", actionId);
            
            try {
                return executeAIActionInternal(instruction);
            } catch (Exception ex) {
                logger.error("AI action failed", ex);
                return createFailedTaskResult(instruction, "AI action failed: " + ex.getMessage());
            } finally {
                activeTasks.remove(actionId);
            }
        });
        
        // 记录活跃任务
        activeTasks.put(actionId, actionFuture);
        return actionFuture;
    }
    
    /**
     * 执行AI动作的内部方法
     * @param instruction 自然语言指令
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeAIActionInternal(String instruction) throws Exception {
        // 获取UI上下文
        UiContext uiContext = getUiContextAsync(DEFAULT_TIMEOUT_MS / 3).join();
        if (uiContext == null) {
            return createFailedTaskResult(instruction, "Failed to get UI context");
        }
        
        // 尝试从缓存获取结果
        TaskResult cachedResult = tryGetCachedAIActionResult(instruction, uiContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 缓存未命中，执行新的AI动作
        return executeNewAIAction(instruction, uiContext);
    }
    
    /**
     * 尝试从缓存获取AI动作结果
     * @param instruction 自然语言指令
     * @param uiContext UI上下文
     * @return 缓存的任务结果，如果缓存未命中则返回null
     */
    private TaskResult tryGetCachedAIActionResult(String instruction, UiContext uiContext) {
        // 创建AI动作请求对象用于缓存键
        AIActionRequest request = AIActionRequest.builder()
            .task(instruction)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        // 尝试从缓存获取结果
        AIActionResult cachedResult = aiModelService.getCachedResult(request);
        if (cachedResult != null && cachedResult.getActionPlan() != null) {
            logger.info("Cache hit for AI action: {}", instruction);
            
            try {
                // 执行缓存的操作计划
                Map<String, Object> result = executeActionPlan(cachedResult.getActionPlan(), true).join();
                
                // 根据操作类型使缓存失效
                UiContextCache.OperationType opType = determineOperationType(instruction);
                invalidateUiContextCacheBasedOnOperation(opType);
                
                return TaskResult.builder()
                    .status(TaskStatus.COMPLETED)
                    .message("AI action completed successfully (from cache)")
                    .data(Map.of(
                        "instruction", instruction,
                        "intent", cachedResult.getIntent(),
                        "result", result,
                        "fromCache", true
                    ))
                    .build();
            } catch (Exception e) {
                logger.warn("Failed to execute cached action plan", e);
                // 缓存执行失败，继续执行新动作
            }
        }
        
        logger.debug("Cache miss for AI action: {}", instruction);
        return null;
    }
    
    /**
     * 执行新的AI动作（缓存未命中时）
     * @param instruction 自然语言指令
     * @param uiContext UI上下文
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeNewAIAction(String instruction, UiContext uiContext) throws Exception {
        // 理解用户意图
        Map<String, Object> context = Map.of("uiContext", uiContext);
        Map<String, Object> intent = insightEngine.understandIntent(instruction, context)
            .orTimeout(DEFAULT_TIMEOUT_MS / 3, TimeUnit.MILLISECONDS).join();
        
        // 生成操作计划
        List<Map<String, Object>> actionPlan = generateActionPlanWithTimeout(intent);
        
        // 缓存结果
        cacheActionResult(instruction, uiContext, intent, actionPlan);
        
        // 执行操作计划
        Map<String, Object> result = executeActionPlan(actionPlan, true).join();
        
        // 根据操作类型使缓存失效
        UiContextCache.OperationType opType = determineOperationType(instruction);
        invalidateUiContextCacheBasedOnOperation(opType);
        
        return TaskResult.builder()
            .status(TaskStatus.COMPLETED)
            .message("AI action completed successfully")
            .data(Map.of(
                "instruction", instruction,
                "intent", intent,
                "result", result,
                "fromCache", false
            ))
            .build();
    }
    
    /**
     * 生成操作计划并设置超时
     * @param intent 用户意图
     * @return 操作计划列表
     * @throws Exception 生成过程中的异常
     */
    private List<Map<String, Object>> generateActionPlanWithTimeout(Map<String, Object> intent) throws Exception {
        return insightEngine.generateActionPlan(intent, null)
            .orTimeout(DEFAULT_TIMEOUT_MS / 3, TimeUnit.MILLISECONDS).join();
    }
    
    /**
     * 缓存AI动作结果
     * @param instruction 指令
     * @param uiContext UI上下文
     * @param intent 意图
     * @param actionPlan 操作计划
     */
    private void cacheActionResult(String instruction, UiContext uiContext, 
                                 Map<String, Object> intent, List<Map<String, Object>> actionPlan) {
        AIActionRequest request = AIActionRequest.builder()
            .task(instruction)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
            
        AIActionResult resultToCache = AIActionResult.builder()
            .intent(intent.toString()) // 转换Map为String表示
            .actionPlan(actionPlan)
            .build();
            
        aiModelService.cacheResult(request, resultToCache);
    }
    
    /**
     * 创建失败的任务结果
     * @param instruction 指令
     * @param message 错误消息
     * @return 失败的任务结果
     */
    private TaskResult createFailedTaskResult(String instruction, String message) {
        return TaskResult.builder()
            .status(TaskStatus.FAILED)
            .message(message)
            .data(Map.of("instruction", instruction))
            .build();
    }

    @Override
    public CompletableFuture<TaskResult> aiTap(String targetDescription) {
        String actionId = generateTaskId("aiTap");
        long timeoutMs = 30000; // 30秒超时
        
        CompletableFuture<TaskResult> tapFuture = CompletableFuture.supplyAsync(() -> {
            logger.info("Starting AI tap: {}", actionId);
            
            try {
                return executeAITapInternal(targetDescription, timeoutMs);
            } catch (Exception ex) {
                logger.error("AI tap failed", ex);
                return createFailedTapResult(targetDescription, "AI tap failed: " + ex.getMessage());
            } finally {
                activeTasks.remove(actionId);
            }
        });
        
        activeTasks.put(actionId, tapFuture);
        return tapFuture;
    }
    
    /**
     * 执行AI点击操作的内部方法
     * @param targetDescription 目标元素描述
     * @param timeoutMs 超时时间（毫秒）
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeAITapInternal(String targetDescription, long timeoutMs) throws Exception {
        // 获取UI上下文
        UiContext uiContext = getUiContextAsync(timeoutMs / 3).join();
        if (uiContext == null) {
            return createFailedTapResult(targetDescription, "Failed to get UI context");
        }
        
        // 尝试从缓存获取结果
        TaskResult cachedResult = tryGetCachedAITapResult(targetDescription, uiContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 缓存未命中，执行新的点击操作
        return executeNewAITap(targetDescription, uiContext, timeoutMs);
    }
    
    /**
     * 尝试从缓存获取AI点击结果
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @return 缓存的任务结果，如果缓存未命中则返回null
     */
    private TaskResult tryGetCachedAITapResult(String targetDescription, UiContext uiContext) {
        // 创建AITapRequest对象用于缓存键
        AITapRequest request = AITapRequest.builder()
            .task(targetDescription)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        // 尝试从缓存获取结果
        AITapResult cachedResult = aiModelService.getCachedResult(request);
        if (cachedResult != null && cachedResult.getTapTarget() != null) {
            logger.info("Cache hit for AI tap: {}", targetDescription);
            
            try {
                String elementId = cachedResult.getTapTarget().getValue();
                TaskResult tapResult = executeTapOperation(elementId);
                
                // 点击操作后使缓存失效，因为页面状态可能已改变
                invalidateUiContextCacheAfterElementInteraction(elementId);
                
                return TaskResult.builder()
                    .status(tapResult.getStatus())
                    .message("AI tap completed successfully (from cache)")
                    .data(Map.of(
                        "targetDescription", targetDescription,
                        "elementId", elementId,
                        "fromCache", true
                    ))
                    .build();
            } catch (Exception e) {
                logger.warn("Failed to execute cached tap operation", e);
                // 缓存执行失败，继续执行新动作
            }
        }
        
        logger.debug("Cache miss for AI tap: {}", targetDescription);
        return null;
    }
    
    /**
     * 执行新的AI点击操作（缓存未命中时）
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @param timeoutMs 超时时间
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeNewAITap(String targetDescription, UiContext uiContext, long timeoutMs) throws Exception {
        // 根据描述查找元素列表
        List<UiElement> elements = insightEngine.findElementsByDescription(uiContext, targetDescription)
            .orTimeout(timeoutMs / 3, TimeUnit.MILLISECONDS).join();
        
        if (elements == null || elements.isEmpty()) {
            return createFailedTapResult(targetDescription, "No elements found for description: " + targetDescription);
        }
        
        String elementId = elements.get(0).getId();
        
        // 创建并缓存结果
        cacheAITapResult(targetDescription, uiContext, elementId);
        
        // 执行点击操作
        TaskResult tapResult = executeTapOperation(elementId);
        
        // 点击操作后使缓存失效
        invalidateUiContextCacheAfterElementInteraction(elementId);
        
        return TaskResult.builder()
            .status(tapResult.getStatus())
            .message(tapResult.getMessage())
            .data(Map.of(
                "targetDescription", targetDescription,
                "elementId", elementId,
                "fromCache", false
            ))
            .build();
    }
    
    /**
     * 缓存AI点击结果
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @param elementId 元素ID
     */
    private void cacheAITapResult(String targetDescription, UiContext uiContext, String elementId) {
        AITapRequest request = AITapRequest.builder()
            .task(targetDescription)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
            
        AITapResult resultToCache = AITapResult.builder()
            .tapTarget(new ElementLocator(ElementLocator.LocatorType.ID, elementId))
            .build();
            
        aiModelService.cacheResult(request, resultToCache);
    }
    
    /**
     * 执行点击操作
     * @param elementId 元素ID
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeTapOperation(String elementId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("elementId", elementId);
        params.put("timeout", DEFAULT_TIMEOUT_MS / 3);
        
        return taskExecutor.executeTask("tap", params).join();
    }
    
    /**
     * 元素交互后使UI上下文缓存失效
     * @param elementId 交互的元素ID
     */
    private void invalidateUiContextCacheAfterElementInteraction(String elementId) {
        List<String> affectedElements = Collections.singletonList(elementId);
        invalidateUiContextCacheBasedOnOperation(UiContextCache.OperationType.MODIFY, affectedElements);
    }
    
    /**
     * 创建失败的点击任务结果
     * @param targetDescription 目标元素描述
     * @param message 错误消息
     * @return 失败的任务结果
     */
    private TaskResult createFailedTapResult(String targetDescription, String message) {
        return TaskResult.builder()
            .status(TaskStatus.FAILED)
            .message(message)
            .data(Map.of("targetDescription", targetDescription))
            .build();
    }

    @Override
    public CompletableFuture<TaskResult> aiInput(String targetDescription, String text) {
        String actionId = generateTaskId("aiInput");
        long timeoutMs = 40000; // 40秒超时
        
        CompletableFuture<TaskResult> inputFuture = CompletableFuture.supplyAsync(() -> {
            logger.info("Starting AI input: {}", actionId);
            
            try {
                return executeAIInputInternal(targetDescription, text, timeoutMs);
            } catch (Exception ex) {
                logger.error("AI input failed", ex);
                return createFailedInputResult(targetDescription, text, "AI input failed: " + ex.getMessage());
            } finally {
                activeTasks.remove(actionId);
            }
        });
        
        activeTasks.put(actionId, inputFuture);
        return inputFuture;
    }
    
    /**
     * 执行AI输入操作的内部方法
     * @param targetDescription 目标元素描述
     * @param text 要输入的文本
     * @param timeoutMs 超时时间（毫秒）
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeAIInputInternal(String targetDescription, String text, long timeoutMs) throws Exception {
        // 获取UI上下文
        UiContext uiContext = getUiContextAsync(timeoutMs / 3).join();
        if (uiContext == null) {
            return createFailedInputResult(targetDescription, text, "Failed to get UI context");
        }
        
        // 尝试从缓存获取结果
        TaskResult cachedResult = tryGetCachedAIInputResult(targetDescription, text, uiContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 缓存未命中，执行新的输入操作
        return executeNewAIInput(targetDescription, text, uiContext, timeoutMs);
    }
    
    /**
     * 尝试从缓存获取AI输入结果
     * @param targetDescription 目标元素描述
     * @param text 要输入的文本
     * @param uiContext UI上下文
     * @return 缓存的任务结果，如果缓存未命中则返回null
     */
    private TaskResult tryGetCachedAIInputResult(String targetDescription, String text, UiContext uiContext) {
        // 创建AIInputRequest对象用于缓存键
        AIInputRequest request = AIInputRequest.builder()
            .task(targetDescription)
            .inputText(text)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        // 尝试从缓存获取结果
        AIInputResult cachedResult = aiModelService.getCachedResult(request);
        if (cachedResult != null && cachedResult.getInputTarget() != null) {
            logger.info("Cache hit for AI input: {}", targetDescription);
            
            try {
                String elementId = cachedResult.getInputTarget().getId();
                TaskResult inputResult = executeInputOperation(elementId, text);
                
                // 输入操作后使缓存失效，因为页面状态可能已改变
                invalidateUiContextCacheAfterElementInteraction(elementId);
                
                return TaskResult.builder()
                    .status(inputResult.getStatus())
                    .message("AI input completed successfully (from cache)")
                    .data(Map.of(
                        "targetDescription", targetDescription,
                        "text", text,
                        "elementId", elementId,
                        "fromCache", true
                    ))
                    .build();
            } catch (Exception e) {
                logger.warn("Failed to execute cached input operation", e);
                // 缓存执行失败，继续执行新动作
            }
        }
        
        logger.debug("Cache miss for AI input: {}", targetDescription);
        return null;
    }
    
    /**
     * 执行新的AI输入操作（缓存未命中时）
     * @param targetDescription 目标元素描述
     * @param text 要输入的文本
     * @param uiContext UI上下文
     * @param timeoutMs 超时时间
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeNewAIInput(String targetDescription, String text, UiContext uiContext, long timeoutMs) throws Exception {
        // 根据描述查找元素列表
        List<UiElement> elements = insightEngine.findElementsByDescription(uiContext, targetDescription)
            .orTimeout(timeoutMs / 3, TimeUnit.MILLISECONDS).join();
        
        if (elements == null || elements.isEmpty()) {
            return createFailedInputResult(targetDescription, text, "No elements found for description: " + targetDescription);
        }
        
        UiElement targetElement = elements.get(0);
        String elementId = targetElement.getId();
        
        // 创建并缓存结果
        cacheAIInputResult(targetDescription, text, uiContext, targetElement);
        
        // 执行输入操作
        TaskResult inputResult = executeInputOperation(elementId, text);
        
        // 输入操作后使缓存失效
        invalidateUiContextCacheAfterElementInteraction(elementId);
        
        return TaskResult.builder()
            .status(inputResult.getStatus())
            .message(inputResult.getMessage())
            .data(Map.of(
                "targetDescription", targetDescription,
                "text", text,
                "elementId", elementId,
                "fromCache", false
            ))
            .build();
    }
    
    /**
     * 缓存AI输入结果
     * @param targetDescription 目标元素描述
     * @param text 要输入的文本
     * @param uiContext UI上下文
     * @param targetElement 目标元素
     */
    private void cacheAIInputResult(String targetDescription, String text, UiContext uiContext, UiElement targetElement) {
        AIInputRequest request = AIInputRequest.builder()
            .task(targetDescription)
            .inputText(text)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
            
        AIInputResult resultToCache = AIInputResult.builder()
            .inputTarget(targetElement)
            .build();
            
        aiModelService.cacheResult(request, resultToCache);
    }
    
    /**
     * 执行输入操作
     * @param elementId 元素ID
     * @param text 要输入的文本
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeInputOperation(String elementId, String text) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("elementId", elementId);
        params.put("text", text);
        params.put("timeout", DEFAULT_TIMEOUT_MS / 3);
        
        return taskExecutor.executeTask("input", params).join();
    }
    
    /**
     * 创建失败的输入任务结果
     * @param targetDescription 目标元素描述
     * @param text 要输入的文本
     * @param message 错误消息
     * @return 失败的任务结果
     */
    private TaskResult createFailedInputResult(String targetDescription, String text, String message) {
        return TaskResult.builder()
            .status(TaskStatus.FAILED)
            .message(message)
            .data(Map.of(
                "targetDescription", targetDescription,
                "text", text
            ))
            .build();
    }

    @Override
    public CompletableFuture<String> extractData(String targetDescription) {
        String actionId = generateTaskId("extractData");
        long timeoutMs = 30000; // 30秒超时
        
        CompletableFuture<String> extractFuture = CompletableFuture.supplyAsync(() -> {
            logger.info("Starting data extraction: {}", actionId);
            
            try {
                return executeExtractDataInternal(targetDescription, timeoutMs);
            } catch (Exception ex) {
                logger.error("Data extraction failed", ex);
                return "Error extracting data: " + ex.getMessage();
            } finally {
                activeTasks.remove(actionId);
                logger.info("Data extraction completed: {}", actionId);
            }
        });
        
        activeTasks.put(actionId, extractFuture);
        return extractFuture;
    }
    
    /**
     * 执行数据提取的内部方法
     * @param targetDescription 目标元素描述
     * @param timeoutMs 超时时间（毫秒）
     * @return 提取的数据
     * @throws Exception 执行过程中的异常
     */
    private String executeExtractDataInternal(String targetDescription, long timeoutMs) throws Exception {
        // 获取UI上下文
        UiContext uiContext = getUiContextAsync(timeoutMs / 2).join();
        if (uiContext == null) {
            throw new RuntimeException("Failed to get UI context");
        }
        
        // 尝试从缓存获取结果
        String cachedResult = tryGetCachedExtractDataResult(targetDescription, uiContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 缓存未命中，执行新的数据提取
        return executeNewExtractData(targetDescription, uiContext, timeoutMs);
    }
    
    /**
     * 尝试从缓存获取数据提取结果
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @return 缓存的数据，如果缓存未命中则返回null
     */
    private String tryGetCachedExtractDataResult(String targetDescription, UiContext uiContext) {
        // 构建ExtractDataRequest对象用于缓存键
        ExtractDataRequest request = ExtractDataRequest.builder()
            .task(targetDescription)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        // 尝试从缓存获取结果
        ExtractDataResult cachedResult = aiModelService.getCachedResult(request);
        if (cachedResult != null && cachedResult.getData() != null) {
            logger.info("Cache hit for data extraction: {}", targetDescription);
            return cachedResult.getData();
        }
        
        logger.debug("Cache miss for data extraction: {}", targetDescription);
        return null;
    }
    
    /**
     * 执行新的数据提取（缓存未命中时）
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @param timeoutMs 超时时间
     * @return 提取的数据
     * @throws Exception 执行过程中的异常
     */
    private String executeNewExtractData(String targetDescription, UiContext uiContext, long timeoutMs) throws Exception {
        // 根据描述查找元素列表
        List<UiElement> elements = insightEngine.findElementsByDescription(uiContext, targetDescription)
            .orTimeout(timeoutMs / 2, TimeUnit.MILLISECONDS).join();
        
        if (elements.isEmpty()) {
            throw new RuntimeException("No elements found for description: " + targetDescription);
        }
        
        // 提取元素数据
        String extractedData = extractElementData(elements.get(0));
        
        // 缓存结果
        cacheExtractDataResult(targetDescription, uiContext, extractedData);
        
        return extractedData;
    }
    
    /**
     * 缓存数据提取结果
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @param extractedData 提取的数据
     */
    private void cacheExtractDataResult(String targetDescription, UiContext uiContext, String extractedData) {
        ExtractDataRequest request = ExtractDataRequest.builder()
            .task(targetDescription)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        ExtractDataResult resultToCache = ExtractDataResult.builder()
            .data(extractedData)
            .build();
        
        aiModelService.cacheResult(request, resultToCache);
    }
    
    public TaskResult extractInformation(String targetDescription, int timeoutMs) {
        try {
            return executeExtractInformationInternal(targetDescription, timeoutMs);
        } catch (Exception ex) {
            logger.error("数据提取失败", ex);
            return TaskResult.fail("数据提取失败: " + ex.getMessage());
        }
    }
    
    /**
     * 执行信息提取的内部方法
     * @param targetDescription 目标元素描述
     * @param timeoutMs 超时时间
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeExtractInformationInternal(String targetDescription, int timeoutMs) throws Exception {
        // 获取当前UI上下文
        UiContext uiContext = getCurrentUiContext();
        if (uiContext == null) {
            return TaskResult.fail("无法获取当前UI上下文");
        }
        
        // 尝试从缓存获取结果
        TaskResult cachedResult = tryGetCachedExtractInformationResult(targetDescription, uiContext);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 缓存未命中，执行新的信息提取
        return executeNewExtractInformation(targetDescription, uiContext, timeoutMs);
    }
    
    /**
     * 尝试从缓存获取信息提取结果
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @return 缓存的任务结果，如果缓存未命中则返回null
     */
    private TaskResult tryGetCachedExtractInformationResult(String targetDescription, UiContext uiContext) {
        // 构建ExtractDataRequest对象用于缓存键
        ExtractDataRequest request = ExtractDataRequest.builder()
            .task(targetDescription)
            .uiContext(uiContext)
            .agentType(getAgentType())
            .build();
        
        // 尝试从缓存获取结果
        ExtractDataResult cachedResult = aiModelService.getCachedResult(request);
        if (cachedResult != null && cachedResult.getData() != null) {
            logger.info("Cache hit for data extraction: {}", targetDescription);
            return TaskResult.builder()
                .status(TaskStatus.SUCCESS)
                .message("数据提取成功 (来自缓存)")
                .data(Map.of(
                    "targetDescription", targetDescription,
                    "extractedData", cachedResult.getData(),
                    "fromCache", true
                ))
                .build();
        }
        
        logger.debug("Cache miss for data extraction: {}", targetDescription);
        return null;
    }
    
    /**
     * 执行新的信息提取（缓存未命中时）
     * @param targetDescription 目标元素描述
     * @param uiContext UI上下文
     * @param timeoutMs 超时时间
     * @return 任务执行结果
     * @throws Exception 执行过程中的异常
     */
    private TaskResult executeNewExtractInformation(String targetDescription, UiContext uiContext, int timeoutMs) throws Exception {
        // 根据描述查找元素列表
        List<UiElement> elements = insightEngine.findElementsByDescription(uiContext, targetDescription)
            .orTimeout(timeoutMs / 2, TimeUnit.MILLISECONDS).join();
        
        if (elements.isEmpty()) {
            throw new RuntimeException("未找到匹配描述的元素: " + targetDescription);
        }
        
        // 提取元素数据
        String extractedData = extractElementData(elements.get(0));
        
        // 缓存结果
        cacheExtractDataResult(targetDescription, uiContext, extractedData);
        
        return TaskResult.builder()
            .status(TaskStatus.SUCCESS)
            .message("数据提取成功")
            .data(Map.of(
                "targetDescription", targetDescription,
                "extractedData", extractedData,
                "fromCache", false
            ))
            .build();
    }
    
    // 获取当前UI上下文的辅助方法
    private UiContext getCurrentUiContext() {
        try {
            return getUiContextAsync(DEFAULT_TIMEOUT_MS / 3).join();
        } catch (Exception e) {
            logger.error("获取UI上下文失败", e);
            return null;
        }
    }
    
    /**
     * 执行操作计划，支持顺序或并行执行
     */
    private CompletableFuture<Map<String, Object>> executeActionPlan(List<Map<String, Object>> actionPlan, boolean allowParallel) {
        if (actionPlan == null || actionPlan.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        // 检查是否有可以并行执行的操作
        if (allowParallel) {
            Map<String, List<Map<String, Object>>> parallelGroups = groupParallelizableActions(actionPlan);
            
            // 如果只有一个组，或者操作不适合并行执行，使用顺序执行
            if (parallelGroups.size() == 1) {
                return executeActionsSequentially(actionPlan);
            }
            
            // 并行执行不同组的操作
            return executeActionsInGroups(parallelGroups);
        }
        
        // 默认顺序执行
        return executeActionsSequentially(actionPlan);
    }
    
    /**
     * 将可并行执行的操作分组
     */
    private Map<String, List<Map<String, Object>>> groupParallelizableActions(List<Map<String, Object>> actionPlan) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        String currentGroup = "group-0";
        groups.put(currentGroup, new ArrayList<>());
        
        for (Map<String, Object> action : actionPlan) {
            String actionType = (String) action.get("action");
            
            // 简单的分组策略：tap和input操作通常可以并行，swipe和wait操作应该顺序执行
            if ("swipe".equals(actionType) || "wait".equals(actionType)) {
                // 开始新组
                currentGroup = "group-" + groups.size();
                groups.put(currentGroup, new ArrayList<>());
            }
            
            groups.get(currentGroup).add(action);
        }
        
        return groups;
    }
    
    /**
     * 顺序执行操作列表
     */
    private CompletableFuture<Map<String, Object>> executeActionsSequentially(List<Map<String, Object>> actions) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            for (int i = 0; i < actions.size(); i++) {
                Map<String, Object> action = actions.get(i);
                String actionType = (String) action.get("action");
                Map<String, Object> params = (Map<String, Object>) action.getOrDefault("params", new HashMap<>());
                
                try {
                    TaskResult taskResult = taskExecutor.executeTask(actionType, params).join();
                    String key = actionType + "-" + i;
                    result.put(key, taskResult);
                } catch (Exception e) {
                    logger.error("Error executing task: {}", actionType, e);
                    result.put(actionType + "-error-" + i, e.getMessage());
                }
            }
            
            return result;
        });
    }
    
    /**
     * 并行执行不同组的操作
     */
    private CompletableFuture<Map<String, Object>> executeActionsInGroups(Map<String, List<Map<String, Object>>> parallelGroups) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> overallResult = new HashMap<>();
            
            // 按顺序处理每个组，但组内操作可以并行执行
            for (Map.Entry<String, List<Map<String, Object>>> group : parallelGroups.entrySet()) {
                String groupId = group.getKey();
                List<Map<String, Object>> actions = group.getValue();
                
                // 对于只有一个操作的组，直接顺序执行
                if (actions.size() == 1) {
                    try {
                        Map<String, Object> action = actions.get(0);
                        String actionType = (String) action.get("action");
                        Map<String, Object> params = (Map<String, Object>) action.getOrDefault("params", new HashMap<>());
                        
                        TaskResult taskResult = taskExecutor.executeTask(actionType, params).join();
                        overallResult.put(groupId + ":" + actionType, taskResult);
                    } catch (Exception e) {
                        logger.error("Error executing task in group: {}", groupId, e);
                        overallResult.put(groupId + ":error", e.getMessage());
                    }
                } else {
                    // 对于多个操作的组，并行执行
                    executeGroupInParallelSync(groupId, actions, overallResult);
                }
            }
            
            return overallResult;
        });
    }
    
    /**
     * 同步执行并行组操作（在当前线程中等待所有并行任务完成）
     */
    private void executeGroupInParallelSync(String groupId, List<Map<String, Object>> actions, Map<String, Object> resultMap) {
        try {
            // 为每个操作创建任务
            List<CompletableFuture<Map.Entry<String, TaskResult>>> parallelTasks = new ArrayList<>();
            
            for (int i = 0; i < actions.size(); i++) {
                final int index = i;
                Map<String, Object> action = actions.get(i);
                String actionType = (String) action.get("action");
                Map<String, Object> params = (Map<String, Object>) action.getOrDefault("params", new HashMap<>());
                String taskId = groupId + ":" + actionType + ":" + i;
                
                // 创建并行任务
                CompletableFuture<Map.Entry<String, TaskResult>> taskFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        TaskResult taskResult = taskExecutor.executeTask(actionType, params).join();
                        return new AbstractMap.SimpleEntry<>(taskId, taskResult);
                    } catch (Exception e) {
                        logger.error("Error executing parallel task: {}", taskId, e);
                        return new AbstractMap.SimpleEntry<>(
                            taskId,
                            TaskResult.builder()
                                .status(TaskStatus.FAILED)
                                .message("Error: " + e.getMessage())
                                .build()
                        );
                    }
                });
                parallelTasks.add(taskFuture);
            }
            
            // 等待所有任务完成并收集结果
            for (CompletableFuture<Map.Entry<String, TaskResult>> taskFuture : parallelTasks) {
                try {
                    Map.Entry<String, TaskResult> entry = taskFuture.join();
                    resultMap.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    logger.error("Error joining task result in group: {}", groupId, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in parallel execution group: {}", groupId, e);
            resultMap.put(groupId + ":execution-error", e.getMessage());
        }
    }
    
    /**
     * 提取元素数据
     */
    private String extractElementData(com.midscene.shared.platform.UiElement element) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("Element ID: ").append(element.getId()).append("\n");
        dataBuilder.append("Type: ").append(element.getType()).append("\n");
        dataBuilder.append("Text: ").append(element.getText()).append("\n");
        
        if (element.getBounds() != null) {
            dataBuilder.append("Bounds: ")
                      .append(element.getBounds().getX()).append(",")
                      .append(element.getBounds().getY()).append(",")
                      .append(element.getBounds().getWidth()).append(",")
                      .append(element.getBounds().getHeight()).append("\n");
        }
        
        return dataBuilder.toString();
    }
    
    /**
     * 生成唯一的任务ID
     */
    private String generateTaskId(String taskType) {
        return taskType + "-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(10000);
    }
    
    /**
     * 为CompletableFuture添加超时支持
     */
    private <T> CompletableFuture<T> applyTimeout(CompletableFuture<T> future, long timeoutMs) {
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        
        // 创建超时任务
        scheduler.schedule(() -> {
            timeoutFuture.completeExceptionally(new TimeoutException("Operation timed out after " + timeoutMs + "ms"));
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        // 当原始future完成时取消超时任务
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                timeoutFuture.completeExceptionally(ex);
            } else {
                timeoutFuture.complete(result);
            }
        });
        
        return timeoutFuture;
    }
    
    /**
     * 取消所有活跃任务
     */
    public void cancelAllTasks() {
        logger.info("Cancelling all active tasks ({} tasks)", activeTasks.size());
        activeTasks.forEach((taskId, future) -> {
            if (!future.isDone()) {
                boolean cancelled = future.cancel(true);
                logger.info("Task {} cancelled: {}", taskId, cancelled);
            }
        });
        activeTasks.clear();
    }
    
    /**
     * 获取活跃任务数量
     */
    public int getActiveTaskCount() {
        return activeTasks.size();
    }
}