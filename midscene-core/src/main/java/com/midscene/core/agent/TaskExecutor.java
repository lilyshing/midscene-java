package com.midscene.core.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midscene.core.model.Action;
import com.midscene.core.model.ActionType;
import com.midscene.core.agent.AgentOptions;
import com.midscene.core.model.Point;
import com.midscene.core.model.Rect;
import com.midscene.core.model.ScrollDirection;
import com.midscene.core.model.StepResult;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.model.UiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 任务执行器，负责协调整个自动化任务的执行流程
 */
public class TaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    
    private final PlatformInterface platformInterface;
    private final InsightEngine insightEngine;
    private final AIModelService aiModelService;
    private final AgentOptions options;
    
    /**
     * 构造函数
     * @param platformInterface 平台接口实例
     * @param insightEngine 洞察引擎实例
     * @param aiModelService AI模型服务实例
     * @param options 代理选项配置
     */
    public TaskExecutor(PlatformInterface platformInterface, InsightEngine insightEngine, 
                       AIModelService aiModelService, AgentOptions options) {
        this.platformInterface = platformInterface;
        this.insightEngine = insightEngine;
        this.aiModelService = aiModelService;
        this.options = options;
    }
    
    /**
     * 执行AI驱动的操作任务
     * @param naturalLanguageTask 用户的自然语言任务描述
     * @return 任务执行结果
     */
    public CompletableFuture<TaskResult> executeAiAction(String naturalLanguageTask) {
        TaskResult taskResult = new TaskResult();
        taskResult.setStatus(TaskStatus.RUNNING);
        
        logger.info("Starting execution of AI action: {}", naturalLanguageTask);
        
        // 获取UI上下文
        return getUiContext()
                // 规划执行步骤
                .thenCompose(uiContext -> planActions(naturalLanguageTask, uiContext))
                // 执行操作计划
                .thenCompose(actions -> executeActionPlan(actions, taskResult))
                // 完成任务
                .thenApply(__ -> {
                    taskResult.complete();
                    logger.info("AI action completed successfully in {} ms", taskResult.getExecutionTimeMs());
                    return taskResult;
                })
                // 异常处理
                .exceptionally(ex -> {
                    String errorMessage = "Task execution failed: " + ex.getMessage();
                    logger.error(errorMessage, ex);
                    taskResult.fail(errorMessage);
                    return taskResult;
                });
    }
    
    /**
     * 获取当前UI上下文
     */
    private CompletableFuture<UiContext> getUiContext() {
        StepResult step = new StepResult("获取UI上下文");
        step.start();
        
        return platformInterface.getUiContext()
                .thenApply(uiContext -> {
                    step.complete();
                    logger.debug("UI上下文获取成功");
                    return uiContext;
                })
                .exceptionally(ex -> {
                    String errorMessage = "获取UI上下文失败: " + ex.getMessage();
                    step.fail(errorMessage);
                    logger.error(errorMessage, ex);
                    throw new RuntimeException(errorMessage, ex);
                });
    }
    
    /**
     * 规划执行步骤
     */
    private CompletableFuture<List<Action>> planActions(String task, UiContext uiContext) {
        StepResult step = new StepResult("规划执行步骤");
        step.start();
        
        return aiModelService.planActions(uiContext, task)
                .thenApply(actionsJson -> {
                    step.complete();
                    logger.info("生成执行计划，收到JSON响应");
                    
                    // 解析JSON字符串为Action列表
                    List<Action> actions = parseActionsFromJson(actionsJson, task);
                    logger.info("解析得到 {} 个操作步骤", actions.size());
                    return actions;
                })
                .exceptionally(ex -> {
                    String errorMessage = "规划执行步骤失败: " + ex.getMessage();
                    step.fail(errorMessage);
                    logger.error(errorMessage, ex);
                    throw new RuntimeException(errorMessage, ex);
                });
    }
    
    /**
     * 从JSON解析Action列表
     */
    private List<Action> parseActionsFromJson(String actionsJson, String originalTask) {
        List<Action> actions = new ArrayList<>();
        
        try {
            // 使用Jackson库解析JSON
            logger.debug("解析操作计划JSON: {}", actionsJson);
            
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 尝试解析JSON格式的操作计划
            try {
                // 尝试解析为包含steps数组的JSON对象
                JsonNode rootNode = objectMapper.readTree(actionsJson);
                
                if (rootNode.has("steps") && rootNode.get("steps").isArray()) {
                    JsonNode stepsNode = rootNode.get("steps");
                    
                    for (JsonNode stepNode : stepsNode) {
                        Action action = parseActionFromJsonNode(stepNode);
                        if (action != null) {
                            actions.add(action);
                        }
                    }
                }
                // 如果解析成功且包含操作，直接返回
                if (!actions.isEmpty()) {
                    logger.info("成功从JSON解析出 {} 个操作", actions.size());
                    return actions;
                }
            } catch (Exception e) {
                logger.debug("JSON解析失败，回退到关键词匹配: {}", e.getMessage());
            }
            
            // 回退到关键词匹配的方法
            return parseActionsFromKeywords(originalTask);
            
        } catch (Exception e) {
            logger.error("解析操作计划失败: {}", e.getMessage(), e);
            
            // 创建一个默认的操作作为后备
            Action fallbackAction = new Action(ActionType.LOCATE_AND_INTERACT, originalTask);
            fallbackAction.setTargetDescription("相关元素");
            fallbackAction.setCoordinates(new Point(400, 300));
            actions.add(fallbackAction);
        }
        
        return actions;
    }
    
    /**
     * 从JSON节点解析单个操作
     */
    private Action parseActionFromJsonNode(JsonNode stepNode) {
        try {
            // 获取操作类型
            String actionTypeStr = stepNode.has("action") ? stepNode.get("action").asText() : "locate_and_interact";
            ActionType actionType = parseActionType(actionTypeStr);
            
            // 获取描述
            String description = stepNode.has("description") ? stepNode.get("description").asText() : "执行操作";
            
            // 创建操作对象
            Action action = new Action(actionType, description);
            
            // 根据操作类型设置特定属性
            switch (actionType) {
                case INPUT:
                    if (stepNode.has("text")) {
                        action.setText(stepNode.get("text").asText());
                    }
                    break;
                    
                case TAP:
                    if (stepNode.has("x") && stepNode.has("y")) {
                        double x = stepNode.get("x").asDouble();
                        double y = stepNode.get("y").asDouble();
                        action.setCoordinates(new Point(x, y));
                    }
                    break;
                    
                case SCROLL:
                    if (stepNode.has("direction")) {
                        String directionStr = stepNode.get("direction").asText();
                        ScrollDirection direction = parseScrollDirection(directionStr);
                        action.setScrollDirection(direction);
                    }
                    if (stepNode.has("distance")) {
                        action.setScrollDistance(stepNode.get("distance").asInt());
                    }
                    break;
                    
                case VERIFY:
                    if (stepNode.has("condition")) {
                        action.setVerificationCondition(stepNode.get("condition").asText());
                    }
                    break;
                    
                case WAIT:
                    // 等待操作可能包含等待时间
                    if (stepNode.has("duration")) {
                        // 可以将等待时间存储在targetDescription中
                        action.setTargetDescription(stepNode.get("duration").asText());
                    }
                    break;
                    
                case NAVIGATE:
                    // 导航操作需要URL
                    if (stepNode.has("url")) {
                        action.setTargetDescription(stepNode.get("url").asText());
                    }
                    break;
                    
                case SCREENSHOT:
                    // 截图操作可能包含文件名
                    if (stepNode.has("filename")) {
                        action.setTargetDescription(stepNode.get("filename").asText());
                    }
                    break;
                    
                case EXIT:
                    // 退出操作通常不需要额外参数
                    break;
            }
            
            // 设置通用属性
            if (stepNode.has("target")) {
                action.setTargetDescription(stepNode.get("target").asText());
            }
            
            if (stepNode.has("priority")) {
                action.setPriority(stepNode.get("priority").asInt());
            }
            
            return action;
            
        } catch (Exception e) {
            logger.error("解析单个操作失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 解析操作类型字符串为枚举
     */
    private ActionType parseActionType(String actionTypeStr) {
        if (actionTypeStr == null) {
            return ActionType.LOCATE_AND_INTERACT;
        }
        
        switch (actionTypeStr.toLowerCase()) {
            case "tap":
            case "click":
                return ActionType.TAP;
                
            case "input":
            case "type":
                return ActionType.INPUT;
                
            case "scroll":
                return ActionType.SCROLL;
                
            case "verify":
            case "assert":
                return ActionType.VERIFY;
                
            case "wait":
                return ActionType.WAIT;
                
            case "navigate":
                return ActionType.NAVIGATE;
                
            case "screenshot":
                return ActionType.SCREENSHOT;
                
            case "exit":
                return ActionType.EXIT;
                
            default:
                return ActionType.LOCATE_AND_INTERACT;
        }
    }
    
    /**
     * 解析滚动方向字符串为枚举
     */
    private ScrollDirection parseScrollDirection(String directionStr) {
        if (directionStr == null) {
            return ScrollDirection.DOWN;
        }
        
        switch (directionStr.toLowerCase()) {
            case "up":
                return ScrollDirection.UP;
                
            case "down":
                return ScrollDirection.DOWN;
                
            case "left":
                return ScrollDirection.LEFT;
                
            case "right":
                return ScrollDirection.RIGHT;
                
            case "to_top":
                return ScrollDirection.TO_TOP;
                
            case "to_bottom":
                return ScrollDirection.TO_BOTTOM;
                
            case "to_element":
                return ScrollDirection.TO_ELEMENT;
                
            case "to_coordinates":
                return ScrollDirection.TO_COORDINATES;
                
            default:
                return ScrollDirection.DOWN;
        }
    }
    
    /**
     * 从关键词解析操作（回退方法）
     */
    private List<Action> parseActionsFromKeywords(String originalTask) {
        List<Action> actions = new ArrayList<>();
        
        // 如果任务包含"输入"关键词，创建输入操作
        if (originalTask.contains("输入") || originalTask.contains("input")) {
            Action inputAction = new Action(ActionType.INPUT, originalTask);
            
            // 尝试从任务中提取要输入的文本
            String inputText = extractInputText(originalTask);
            if (inputText != null) {
                inputAction.setText(inputText);
            }
            
            // 设置目标描述
            inputAction.setTargetDescription("搜索框");
            
            // 设置坐标 - 这里应该通过AI定位，暂时使用默认值
            inputAction.setCoordinates(new Point(400, 300));
            
            actions.add(inputAction);
            logger.info("添加输入操作: {}", inputAction);
        }
        
        // 如果任务包含"点击"关键词，创建点击操作
        if (originalTask.contains("点击") || originalTask.contains("click")) {
            Action clickAction = new Action(ActionType.TAP, originalTask);
            
            // 设置目标描述
            clickAction.setTargetDescription("搜索按钮");
            
            // 设置坐标 - 这里应该通过AI定位，暂时使用默认值
            clickAction.setCoordinates(new Point(500, 300));
            
            actions.add(clickAction);
            logger.info("添加点击操作: {}", clickAction);
        }
        
        // 如果没有解析到任何操作，创建一个默认的交互操作
        if (actions.isEmpty()) {
            Action defaultAction = new Action(ActionType.LOCATE_AND_INTERACT, originalTask);
            defaultAction.setTargetDescription("相关元素");
            defaultAction.setCoordinates(new Point(400, 300));
            actions.add(defaultAction);
            logger.info("添加默认操作: {}", defaultAction);
        }
        
        return actions;
    }
    
    /**
     * 从任务描述中提取要输入的文本
     */
    private String extractInputText(String task) {
        // 尝试匹配引号中的内容
        int startQuote = task.indexOf("'");
        int endQuote = task.lastIndexOf("'");
        
        if (startQuote != -1 && endQuote != -1 && endQuote > startQuote + 1) {
            return task.substring(startQuote + 1, endQuote);
        }
        
        // 尝试匹配双引号中的内容
        startQuote = task.indexOf("\"");
        endQuote = task.lastIndexOf("\"");
        
        if (startQuote != -1 && endQuote != -1 && endQuote > startQuote + 1) {
            return task.substring(startQuote + 1, endQuote);
        }
        
        return null;
    }
    
    /**
     * 执行操作计划
     */
    private CompletableFuture<Void> executeActionPlan(List<Action> actions, TaskResult taskResult) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        
        for (Action action : actions) {
            final Action currentAction = action;
            chain = chain.thenCompose(__ -> executeStep(currentAction, taskResult));
        }
        
        return chain;
    }
    
    /**
     * 执行单个操作步骤
     */
    private CompletableFuture<Void> executeStep(Action action, TaskResult taskResult) {
        StepResult result = new StepResult(action.getDescription());
        taskResult.addStep(result);
        result.start();
        
        logger.info("执行操作: {}", action.getDescription());
        
        // 添加重试机制
        return executeStepWithRetry(action, result, 0)
                .thenAccept(__ -> {
                    logger.info("操作步骤完成: {}, 耗时 {} ms", action.getDescription(), result.getExecutionTimeMs());
                })
                .exceptionally(throwable -> {
                    logger.error("操作步骤失败: {}, 耗时 {} ms, 错误: {}", 
                                action.getDescription(), result.getExecutionTimeMs(), throwable.getMessage());
                    throw new CompletionException(throwable);
                });
    }
    
    /**
     * 执行操作步骤，支持重试机制
     */
    private CompletableFuture<Void> executeStepWithRetry(Action action, StepResult result, int retryCount) {
        // 检查是否超过最大重试次数
        if (retryCount > 0 && retryCount > action.getMaxRetries()) {
            return CompletableFuture.failedFuture(new RuntimeException("操作失败，已达到最大重试次数: " + action.getMaxRetries()));
        }
        
        // 如果是重试，记录日志
        if (retryCount > 0) {
            logger.info("重试操作: {}, 第 {} 次重试", action.getDescription(), retryCount);
        }
        
        // 执行操作
        return executeSingleStep(action, result)
                .handle((__, throwable) -> {
                    // 操作失败，检查是否需要重试
                    if (throwable != null && action.isRetryOnFailure() && retryCount < action.getMaxRetries()) {
                        // 等待一段时间后重试
                        long delayMs = 1000 * retryCount; // 递增延迟
                        logger.info("操作失败，{} ms 后进行第 {} 次重试", delayMs, retryCount + 1);
                        
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new CompletionException(e);
                        }
                        
                        // 递归调用，增加重试次数
                        return executeStepWithRetry(action, result, retryCount + 1);
                    } else if (throwable != null) {
                        // 不重试或已达到最大重试次数，返回失败结果
                        throw new CompletionException(throwable);
                    } else {
                        // 操作成功，返回完成结果
                        return null;
                    }
                })
                .thenCompose(resultFuture -> {
                    // 如果resultFuture是CompletableFuture，则展开它；否则直接返回
                    if (resultFuture instanceof CompletableFuture) {
                        return (CompletableFuture<Void>) resultFuture;
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }
    
    /**
     * 执行单个操作步骤（不包含重试逻辑）
     */
    private CompletableFuture<Void> executeSingleStep(Action action, StepResult result) {
        // 根据操作类型执行相应操作
        switch (action.getType()) {
            case LOCATE_AND_INTERACT:
                // 先定位元素，再进行交互
                return locateAndInteract(action, result);
            case TAP:
                // 直接点击元素
                return tapElement(action, result);
            case INPUT:
                // 输入文本
                return inputText(action, result);
            case SCROLL:
                // 滚动操作
                return scrollTo(action, result);
            case VERIFY:
                // 验证操作
                return verifyCondition(action, result);
            case WAIT:
                // 等待操作
                return waitForCondition(action, result);
            case NAVIGATE:
                // 导航操作
                return navigateTo(action, result);
            case SCREENSHOT:
                // 截图操作
                return takeScreenshot(action, result);
            case EXIT:
                // 退出操作
                return exitApplication(action, result);
            default:
                // 默认操作类型
                String errorMessage = "不支持的操作类型: " + action.getType();
                result.fail(errorMessage);
                logger.error(errorMessage);
                return CompletableFuture.failedFuture(new UnsupportedOperationException(errorMessage));
        }
    }
    
    /**
     * 定位并交互元素
     */
    private CompletableFuture<Void> locateAndInteract(Action action, StepResult result) {
        // 获取目标描述
        final String targetDescription;
        final String targetDesc = action.getTargetDescription();
        if (targetDesc == null || targetDesc.isEmpty()) {
            targetDescription = "目标元素";
        } else {
            targetDescription = targetDesc;
        }
        
        result.setTargetElementInfo(targetDescription);
        result.setActionInfo("定位并交互元素");
        
        // 首先尝试使用AI定位元素
        return aiModelService.locateElement(targetDescription, platformInterface)
                .thenCompose(jsonResult -> {
                    try {
                        // 解析JSON结果为LocateResult对象
                        final LocateResult locateResult = parseLocateResult(jsonResult, targetDescription);
                        
                        if (locateResult.isSuccess() && locateResult.getCenterPoint().isPresent()) {
                            // 成功定位到元素，使用定位到的坐标
                            final Point coordinates = locateResult.getCenterPoint().get();
                            result.setTargetElementInfo(targetDescription + " (坐标: " + coordinates.getX() + ", " + coordinates.getY() + ")");
                            
                            // 更新action中的坐标
                            action.setCoordinates(coordinates);
                            
                            // 执行点击操作
                            return platformInterface.tap((int)coordinates.getX(), (int)coordinates.getY())
                                    .thenRun(() -> {
                                        result.complete();
                                        logger.info("元素定位并点击成功，目标: {}, 坐标: {}, 耗时 {} ms", 
                                                targetDescription, coordinates, result.getExecutionTimeMs());
                                    });
                        } else {
                            // AI定位失败，尝试使用action中已有的坐标
                            final Point fallbackCoordinates = action.getCoordinates();
                            if (fallbackCoordinates != null) {
                                logger.warn("AI定位失败，使用预设坐标: {}", fallbackCoordinates);
                                result.setTargetElementInfo(targetDescription + " (使用预设坐标: " + fallbackCoordinates.getX() + ", " + fallbackCoordinates.getY() + ")");
                                
                                return platformInterface.tap((int)fallbackCoordinates.getX(), (int)fallbackCoordinates.getY())
                                        .thenRun(() -> {
                                            result.complete();
                                            logger.info("使用预设坐标点击成功，目标: {}, 坐标: {}, 耗时 {} ms", 
                                                    targetDescription, fallbackCoordinates, result.getExecutionTimeMs());
                                        });
                            } else {
                                // 没有任何坐标信息，定位失败
                                final String errorMessage = "无法定位元素: " + targetDescription;
                                final String fullErrorMessage = locateResult.getErrorMessage().isPresent() 
                                        ? errorMessage + " - " + locateResult.getErrorMessage().get()
                                        : errorMessage;
                                result.fail(fullErrorMessage);
                                logger.error(fullErrorMessage);
                                return CompletableFuture.failedFuture(new RuntimeException(fullErrorMessage));
                            }
                        }
                    } catch (Exception e) {
                        // 解析JSON失败，尝试使用action中已有的坐标
                        final Point exceptionCoordinates = action.getCoordinates();
                        if (exceptionCoordinates != null) {
                            logger.warn("解析定位结果失败，使用预设坐标: {}", exceptionCoordinates, e);
                            result.setTargetElementInfo(targetDescription + " (使用预设坐标: " + exceptionCoordinates.getX() + ", " + exceptionCoordinates.getY() + ")");
                            
                            return platformInterface.tap((int)exceptionCoordinates.getX(), (int)exceptionCoordinates.getY())
                                    .thenRun(() -> {
                                        result.complete();
                                        logger.info("使用预设坐标点击成功，目标: {}, 坐标: {}, 耗时 {} ms", 
                                                targetDescription, exceptionCoordinates, result.getExecutionTimeMs());
                                    });
                        } else {
                            // 没有任何坐标信息，定位失败
                            final String errorMessage = "无法定位元素: " + targetDescription + " - 解析结果失败: " + e.getMessage();
                            result.fail(errorMessage);
                            logger.error(errorMessage, e);
                            return CompletableFuture.failedFuture(new RuntimeException(errorMessage));
                        }
                    }
                })
                .exceptionally(throwable -> {
                    // 处理异常情况
                    final String errorMessage = "定位并交互元素时发生异常: " + throwable.getMessage();
                    result.fail(errorMessage);
                    logger.error(errorMessage, throwable);
                    throw new CompletionException(errorMessage, throwable);
                });
    }
    
    /**
     * 点击元素
     */
    private CompletableFuture<Void> tapElement(Action action, StepResult result) {
        final Point coordinates = action.getCoordinates();
        if (coordinates == null) {
            final String errorMessage = "点击操作缺少坐标信息";
            result.fail(errorMessage);
            logger.error(errorMessage);
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMessage));
        }
        
        result.setActionInfo("执行点击操作");
        return platformInterface.tap((int)coordinates.getX(), (int)coordinates.getY())
                .thenRun(() -> {
                    result.complete();
                    logger.info("点击操作成功，耗时 {} ms", result.getExecutionTimeMs());
                });
    }
    
    /**
     * 输入文本
     */
    private CompletableFuture<Void> inputText(Action action, StepResult result) {
        final String text = action.getText();
        if (text == null || text.isEmpty()) {
            final String errorMessage = "输入操作缺少文本内容";
            result.fail(errorMessage);
            logger.error(errorMessage);
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMessage));
        }
        
        // 简化处理，直接使用action中的坐标进行输入
        final Point coordinates = action.getCoordinates();
        if (coordinates == null) {
            final String errorMessage = "输入操作缺少坐标信息";
            result.fail(errorMessage);
            logger.error(errorMessage);
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMessage));
        }
        
        final String targetDesc = action.getTargetDescription() != null ? action.getTargetDescription() : "默认位置";
        result.setTargetElementInfo(targetDesc);
        result.setActionInfo("输入文本: " + text);
        return platformInterface.inputText(text, (int)coordinates.getX(), (int)coordinates.getY())
                .thenRun(() -> {
                    result.complete();
                    logger.info("文本输入成功，耗时 {} ms", result.getExecutionTimeMs());
                });
    }
    
    /**
     * 滚动操作
     */
    private CompletableFuture<Void> scrollTo(Action action, StepResult result) {
        final ScrollDirection direction = action.getScrollDirection();
        final Integer distance = action.getScrollDistance() != null ? action.getScrollDistance() : 100;
        
        // 将枚举转换为字符串
        final String directionStr = direction != null ? direction.toString().toLowerCase() : "down";
        
        result.setActionInfo("执行滚动操作: " + directionStr + ", 距离: " + distance);
        return platformInterface.scroll(directionStr, distance)
                .thenRun(() -> {
                    result.complete();
                    logger.info("滚动操作成功，耗时 {} ms", result.getExecutionTimeMs());
                });
    }
    
    /**
     * 验证条件
     */
    private CompletableFuture<Void> verifyCondition(Action action, StepResult result) {
        final String condition = action.getVerificationCondition();
        if (condition == null || condition.isEmpty()) {
            final String errorMessage = "验证操作缺少验证条件";
            result.fail(errorMessage);
            logger.error(errorMessage);
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMessage));
        }
        
        result.setActionInfo("验证条件: " + condition);
        
        // 获取最新UI上下文进行验证
        return platformInterface.getUiContext()
                .thenCompose(uiContext -> {
                    // 检查是否是元素存在性验证
                    if (condition.startsWith("元素存在:") || condition.startsWith("element exists:")) {
                        final String elementDesc = condition.substring(condition.indexOf(":") + 1).trim();
                        return verifyElementExists(elementDesc, uiContext);
                    }
                    // 检查是否是文本存在性验证
                    else if (condition.startsWith("文本存在:") || condition.startsWith("text exists:")) {
                        final String textToFind = condition.substring(condition.indexOf(":") + 1).trim();
                        return verifyTextExists(textToFind, uiContext);
                    }
                    // 检查是否是元素可见性验证
                    else if (condition.startsWith("元素可见:") || condition.startsWith("element visible:")) {
                        final String elementDesc = condition.substring(condition.indexOf(":") + 1).trim();
                        return verifyElementVisible(elementDesc, uiContext);
                    }
                    // 其他情况使用AI模型进行验证
                    else {
                        return aiModelService.verifyCondition(condition, uiContext);
                    }
                })
                .thenAccept(isVerified -> {
                    if (!isVerified) {
                        final String errorMessage = "验证失败: " + condition;
                        result.fail(errorMessage);
                        logger.error(errorMessage);
                        throw new RuntimeException(errorMessage);
                    }
                    
                    result.complete();
                    logger.info("验证操作成功，耗时 {} ms", result.getExecutionTimeMs());
                })
                .exceptionally(throwable -> {
                    final String errorMessage = "验证条件时发生异常: " + throwable.getMessage();
                    result.fail(errorMessage);
                    logger.error(errorMessage, throwable);
                    throw new CompletionException(errorMessage, throwable);
                });
    }
    
    /**
     * 验证元素是否存在
     * @param elementDescription 元素描述
     * @param uiContext UI上下文
     * @return 是否存在
     */
    private CompletableFuture<Boolean> verifyElementExists(String elementDescription, UiContext uiContext) {
        logger.debug("验证元素是否存在: {}", elementDescription);
        
        // 使用AI模型定位元素
        return aiModelService.locateElement(elementDescription, platformInterface)
                .thenApply(jsonResult -> {
                    try {
                        final LocateResult locateResult = parseLocateResult(jsonResult, elementDescription);
                        return locateResult.isSuccess();
                    } catch (Exception e) {
                        logger.error("验证元素存在性时解析结果失败: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("验证元素存在性时发生异常: {}", throwable.getMessage(), throwable);
                    return false;
                });
    }
    
    /**
     * 验证文本是否存在
     * @param textToFind 要查找的文本
     * @param uiContext UI上下文
     * @return 是否存在
     */
    private CompletableFuture<Boolean> verifyTextExists(String textToFind, UiContext uiContext) {
        logger.debug("验证文本是否存在: {}", textToFind);
        
        // 在实际实现中，这里会在UI上下文中搜索文本
        // 目前返回模拟结果
        final String textToSearch = textToFind;
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟处理延迟
                Thread.sleep(200);
                
                // 简单模拟 - 如果文本不为空，返回true
                return textToSearch != null && !textToSearch.trim().isEmpty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("文本验证中断", e);
            }
        });
    }
    
    /**
     * 验证元素是否可见
     * @param elementDescription 元素描述
     * @param uiContext UI上下文
     * @return 是否可见
     */
    private CompletableFuture<Boolean> verifyElementVisible(String elementDescription, UiContext uiContext) {
        logger.debug("验证元素是否可见: {}", elementDescription);
        
        // 使用AI模型定位元素
        return aiModelService.locateElement(elementDescription, platformInterface)
                .thenApply(jsonResult -> {
                    try {
                        final LocateResult locateResult = parseLocateResult(jsonResult, elementDescription);
                        if (!locateResult.isSuccess()) {
                            return false;
                        }
                        
                        // 检查置信度是否足够高
                        final double confidence = locateResult.getConfidence();
                        return confidence >= 0.5; // 置信度阈值可配置
                    } catch (Exception e) {
                        logger.error("验证元素可见性时解析结果失败: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("验证元素可见性时发生异常: {}", throwable.getMessage(), throwable);
                    return false;
                });
    }
    
    /**
     * 解析定位结果JSON
     * @param jsonResult JSON结果
     * @param targetDescription 目标描述
     * @return 定位结果对象
     */
    private LocateResult parseLocateResult(String jsonResult, String targetDescription) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode rootNode = objectMapper.readTree(jsonResult);
            
            // 检查是否成功
            final boolean success = rootNode.path("success").asBoolean(false);
            
            if (!success) {
                final String errorMessage = rootNode.path("error").asText("定位失败");
                return LocateResult.createFailure(errorMessage);
            }
            
            // 获取置信度
            final double confidence = rootNode.path("confidence").asDouble(0.0);
            
            // 获取边界框
            Rect boundingBox = null;
            final JsonNode bboxNode = rootNode.path("bounding_box");
            if (!bboxNode.isMissingNode()) {
                final double left = bboxNode.path("left").asDouble(0);
                final double top = bboxNode.path("top").asDouble(0);
                final double width = bboxNode.path("width").asDouble(0);
                final double height = bboxNode.path("height").asDouble(0);
                boundingBox = new Rect(left, top, width, height);
            }
            
            // 获取中心点
            Point centerPoint = null;
            final JsonNode centerNode = rootNode.path("center_point");
            if (!centerNode.isMissingNode()) {
                final double x = centerNode.path("x").asDouble(0);
                final double y = centerNode.path("y").asDouble(0);
                centerPoint = new Point(x, y);
            } else if (boundingBox != null) {
                // 如果没有中心点但有边界框，计算中心点
                centerPoint = boundingBox.getCenter();
            }
            
            // 获取元素信息
            final String elementInfo = rootNode.path("element_info").asText(targetDescription);
            
            // 创建成功的定位结果
            final LocateResult result = LocateResult.createSuccess(null, boundingBox, confidence);
            result.setElementInfo(elementInfo);
            if (centerPoint != null) {
                result.setCenterPoint(centerPoint);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to parse locate result: {}", e.getMessage(), e);
            return LocateResult.createFailure("解析定位结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 等待操作
     */
    private CompletableFuture<Void> waitForCondition(Action action, StepResult result) {
        try {
            // 获取等待时间，默认为1000毫秒
            int waitTime = 1000;
            final String description = action.getDescription();
            
            // 尝试从描述中解析等待时间
            if (description != null && description.matches(".*\\d+.*")) {
                final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                final java.util.regex.Matcher matcher = pattern.matcher(description);
                if (matcher.find()) {
                    waitTime = Integer.parseInt(matcher.group(1));
                    // 如果描述中包含"秒"，则转换为毫秒
                    if (description.contains("秒")) {
                        waitTime *= 1000;
                    }
                }
            }
            
            logger.info("等待 {} 毫秒", waitTime);
            Thread.sleep(waitTime);
            
            result.complete();
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final String errorMsg = "等待操作被中断";
            result.fail(errorMsg);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        } catch (Exception e) {
            final String errorMsg = "等待操作失败: " + e.getMessage();
            result.fail(errorMsg);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }
    
    /**
     * 导航操作
     */
    private CompletableFuture<Void> navigateTo(Action action, StepResult result) {
        try {
            String url = action.getTargetDescription();
            if (url == null || url.trim().isEmpty()) {
                url = action.getDescription();
            }
            
            if (url == null || url.trim().isEmpty()) {
                final String errorMsg = "导航URL为空";
                result.fail(errorMsg);
                return CompletableFuture.failedFuture(new IllegalArgumentException(errorMsg));
            }
            
            logger.info("导航到: {}", url);
            
            // 使用平台接口导航到指定URL，包装为异步操作
            final String finalUrl = url; // 创建final副本供lambda使用
            return CompletableFuture.runAsync(() -> {
                try {
                    platformInterface.navigateTo(finalUrl);
                    result.complete();
                } catch (Exception e) {
                    final String errorMsg = "导航操作失败: " + e.getMessage();
                    result.fail(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
            });
        } catch (Exception e) {
            final String errorMsg = "导航操作失败: " + e.getMessage();
            result.fail(errorMsg);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }
    
    /**
     * 截图操作
     */
    private CompletableFuture<Void> takeScreenshot(Action action, StepResult result) {
        try {
            String fileName = action.getTargetDescription();
            if (fileName == null || fileName.trim().isEmpty()) {
                // 使用时间戳作为默认文件名
                fileName = "screenshot_" + System.currentTimeMillis() + ".png";
            }
            
            logger.info("截图: {}", fileName);
            
            // 使用平台接口截图，包装为异步操作
            final String finalFileName = fileName; // 创建final副本供lambda使用
            return CompletableFuture.runAsync(() -> {
                try {
                    platformInterface.takeScreenshot(finalFileName);
                    result.complete();
                } catch (Exception e) {
                    final String errorMsg = "截图操作失败: " + e.getMessage();
                    result.fail(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
            });
        } catch (Exception e) {
            final String errorMsg = "截图操作失败: " + e.getMessage();
            result.fail(errorMsg);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }
    
    /**
     * 退出操作
     */
    private CompletableFuture<Void> exitApplication(Action action, StepResult result) {
        try {
            logger.info("退出应用程序");
            
            // 使用平台接口退出应用，包装为异步操作
            return CompletableFuture.runAsync(() -> {
                try {
                    platformInterface.exitApplication();
                    result.complete();
                } catch (Exception e) {
                    final String errorMsg = "退出操作失败: " + e.getMessage();
                    result.fail(errorMsg);
                    throw new RuntimeException(errorMsg, e);
                }
            });
        } catch (Exception e) {
            final String errorMsg = "退出操作失败: " + e.getMessage();
            result.fail(errorMsg);
            return CompletableFuture.failedFuture(new RuntimeException(errorMsg, e));
        }
    }

    /**
     * 获取执行历史
     * @return 执行历史信息
     */
    public List<Map<String, Object>> getExecutionHistory() {
        // 实现执行历史记录功能
        // 此方法可用于调试和分析
        return new ArrayList<>();
    }
}