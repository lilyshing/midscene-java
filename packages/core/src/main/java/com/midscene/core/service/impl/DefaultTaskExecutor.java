package com.midscene.core.service.impl;

import com.midscene.core.exception.PlatformException;
import com.midscene.core.model.TaskResult;
import com.midscene.core.model.TaskStatus;
import com.midscene.core.service.TaskExecutor;
import com.midscene.shared.platform.PlatformInterface;
import com.midscene.shared.platform.ElementLocator;
import com.midscene.shared.platform.Point;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务执行器的默认实现类
 * 提供各种自动化操作的执行功能，支持异步处理、超时控制和任务取消
 */
public class DefaultTaskExecutor implements TaskExecutor {
    private PlatformInterface platform;
    private boolean initialized = false;
    private ExecutorService executorService;
    private final Map<String, CompletableFuture<TaskResult>> activeTasks = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor customExecutor;
    
    public DefaultTaskExecutor() {
        // 创建自定义线程池，优化并发性能
        this.customExecutor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "midscene-task-executor-" + counter++);
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        // 允许核心线程超时，避免资源浪费
        this.customExecutor.allowCoreThreadTimeOut(true);
        this.executorService = this.customExecutor;
    }

    @Override
    public void setPlatform(PlatformInterface platform) {
        this.platform = platform;
    }

    @Override
    public CompletableFuture<TaskResult> executeTask(String taskType, Map<String, Object> parameters) {
        // 从参数中提取超时设置，如果没有指定，默认30秒
        long timeout = parameters != null ? 
                ((Number) parameters.getOrDefault("timeout", 30000)).longValue() : 30000;
        
        // 创建任务ID
        String taskId = generateTaskId(taskType);
        
        // 执行任务并应用超时
        CompletableFuture<TaskResult> taskFuture = switch (taskType) {
            case "tap" -> executeTap(parameters);
            case "input" -> executeInput(
                parameters,
                (String) parameters.getOrDefault("text", "")
            );
            case "swipe" -> {
                Map<String, Object> start = new java.util.HashMap<>();
                start.put("x", parameters.getOrDefault("startX", 0));
                start.put("y", parameters.getOrDefault("startY", 0));
                
                Map<String, Object> end = new java.util.HashMap<>();
                end.put("x", parameters.getOrDefault("endX", 0));
                end.put("y", parameters.getOrDefault("endY", 0));
                
                int duration = ((Number) parameters.getOrDefault("duration", 500)).intValue();
                yield executeSwipe(start, end, duration);
            }
            case "wait" -> {
                long waitTimeout = ((Number) parameters.getOrDefault("timeout", 1000)).longValue();
                yield executeWait(parameters, waitTimeout);
            }
            default -> CompletableFuture.completedFuture(
                buildErrorResult("Unknown task type: " + taskType, null, parameters)
            );
        };
        
        // 应用超时并记录活跃任务
        CompletableFuture<TaskResult> resultFuture = applyTimeout(taskFuture, timeout)
            .whenComplete((result, ex) -> activeTasks.remove(taskId));
        
        activeTasks.put(taskId, resultFuture);
        return resultFuture;
    }

    @Override
    public CompletableFuture<TaskResult> executeTap(Map<String, Object> target) {
        return CompletableFuture.supplyAsync(() -> {
            ensurePlatformInitialized();
            
            String elementId = target != null ? (String) target.getOrDefault("elementId", "") : "";
            
            // 尝试从target获取坐标
            int x = 0, y = 0;
            if (target != null) {
                x = target.containsKey("x") ? ((Number) target.get("x")).intValue() : 0;
                y = target.containsKey("y") ? ((Number) target.get("y")).intValue() : 0;
            }
            
            try {
                // 根据参数类型选择合适的platform方法
                if (!elementId.isEmpty()) {
                    ElementLocator locator = new ElementLocator(ElementLocator.LocatorType.ID, elementId);
                    boolean success = platform.tap(locator).get();
                    return buildTapResult(success, elementId, target);
                } else {
                    final int finalX = x;
                    final int finalY = y;
                    ElementLocator locator = new ElementLocator(ElementLocator.LocatorType.COORDINATES, "");
                    locator.withAttribute("x", finalX).withAttribute("y", finalY);
                    boolean success = platform.tap(locator).get();
                    return buildTapResult(success, finalX, finalY, target);
                }
            } catch (Exception e) {
                return buildErrorResult("Tap operation error", e, target);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<TaskResult> executeInput(Map<String, Object> target, String text) {
        return CompletableFuture.supplyAsync(() -> {
            ensurePlatformInitialized();
            
            String elementId = target != null ? (String) target.getOrDefault("elementId", "") : "";
            
            // 尝试从target获取坐标
            int x = 0, y = 0;
            if (target != null) {
                x = target.containsKey("x") ? ((Number) target.get("x")).intValue() : 0;
                y = target.containsKey("y") ? ((Number) target.get("y")).intValue() : 0;
            }
            
            try {
                // 根据参数类型选择合适的platform方法
                if (!elementId.isEmpty()) {
                    ElementLocator locator = new ElementLocator(ElementLocator.LocatorType.ID, elementId);
                    boolean success = platform.input(locator, text).get();
                    return buildInputResult(success, elementId, text, target);
                } else {
                    final int finalX = x;
                    final int finalY = y;
                    final String finalText = text;
                    ElementLocator locator = new ElementLocator(ElementLocator.LocatorType.COORDINATES, "");
                    locator.withAttribute("x", finalX).withAttribute("y", finalY);
                    boolean success = platform.input(locator, finalText).get();
                    return buildInputResult(success, finalX, finalY, finalText, target);
                }
            } catch (Exception e) {
                return buildErrorResult("Input operation error", e, target);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<TaskResult> executeSwipe(Map<String, Object> start, Map<String, Object> end, int duration) {
        return CompletableFuture.supplyAsync(() -> {
            ensurePlatformInitialized();
            
            // 从start和end中获取坐标
            int startX = start != null ? ((Number) start.getOrDefault("x", 0)).intValue() : 0;
            int startY = start != null ? ((Number) start.getOrDefault("y", 0)).intValue() : 0;
            int endX = end != null ? ((Number) end.getOrDefault("x", 0)).intValue() : 0;
            int endY = end != null ? ((Number) end.getOrDefault("y", 0)).intValue() : 0;
            
            try {
                Point startPoint = new Point(startX, startY);
                Point endPoint = new Point(endX, endY);
                boolean success = platform.swipe(startPoint, endPoint, duration).get();
                return buildSwipeResult(success, startX, startY, endX, endY);
            } catch (Exception e) {
                Map<String, Object> data = Map.of(
                    "start", start,
                    "end", end,
                    "duration", duration
                );
                return buildErrorResult("Swipe operation error", e, data);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<TaskResult> executeWait(Map<String, Object> condition, long timeout) {
        // 使用AtomicBoolean支持任务取消
        AtomicBoolean cancelled = new AtomicBoolean(false);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 支持可中断的等待
                long remainingTime = timeout;
                long startTime = System.currentTimeMillis();
                
                while (remainingTime > 0 && !cancelled.get()) {
                    long sleepTime = Math.min(100, remainingTime); // 检查取消状态的间隔
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    remainingTime = timeout - (System.currentTimeMillis() - startTime);
                }
                
                if (cancelled.get()) {
                    return TaskResult.builder()
                        .status(TaskStatus.CANCELLED)
                        .message("Wait operation cancelled")
                        .data(Map.of(
                            "condition", condition,
                            "timeout", timeout
                        ))
                        .build();
                }
                
                return TaskResult.builder()
                    .status(TaskStatus.COMPLETED)
                    .message("Wait operation completed successfully")
                    .data(Map.of(
                        "condition", condition,
                        "timeout", timeout
                    ))
                    .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return buildErrorResult("Wait operation interrupted", e, Map.of(
                    "condition", condition,
                    "timeout", timeout
                ));
            }
        }, executorService);
    }
    
    /**
     * 并行执行多个任务
     * @param tasks 任务列表
     * @return 包含所有任务结果的CompletableFuture
     */
    public CompletableFuture<Map<String, TaskResult>> executeTasksInParallel(Map<String, CompletableFuture<TaskResult>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }
        
        // 创建所有任务的数组
        CompletableFuture<?>[] taskFutures = tasks.values().toArray(new CompletableFuture[0]);
        
        // 使用allOf等待所有任务完成
        return CompletableFuture.allOf(taskFutures)
            .thenApply(v -> {
                Map<String, TaskResult> results = new ConcurrentHashMap<>();
                tasks.forEach((taskId, future) -> {
                    try {
                        results.put(taskId, future.get());
                    } catch (Exception e) {
                        results.put(taskId, buildErrorResult("Failed to get result for task: " + taskId, e, Map.of("taskId", taskId)));
                    }
                });
                return results;
            })
            .exceptionally(ex -> {
                Map<String, TaskResult> errorResults = new ConcurrentHashMap<>();
                tasks.forEach((taskId, future) -> {
                    errorResults.put(taskId, buildErrorResult("Parallel execution failed", ex, Map.of("taskId", taskId)));
                });
                return errorResults;
            });
    }
    
    /**
     * 取消指定任务
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    public boolean cancelTask(String taskId) {
        CompletableFuture<TaskResult> future = activeTasks.get(taskId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if (cancelled) {
                activeTasks.remove(taskId);
            }
            return cancelled;
        }
        return false;
    }
    
    /**
     * 取消所有活跃任务
     */
    public void cancelAllTasks() {
        activeTasks.forEach((taskId, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        activeTasks.clear();
    }

    @Override
    public void initialize(Map<String, Object> config) {
        // 初始化任务执行器
        initialized = true;
    }

    @Override
    public void shutdown() {
        // 关闭任务执行器，清理资源
        cancelAllTasks();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        initialized = false;
    }
    
    /**
     * 确保平台接口已初始化
     */
    private void ensurePlatformInitialized() {
        if (platform == null) {
            throw new PlatformException("Platform not initialized");
        }
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
    
    // 线程调度器，用于超时处理
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
        1,
        r -> {
            Thread thread = new Thread(r, "midscene-timeout-scheduler");
            thread.setDaemon(true);
            return thread;
        }
    );
    
    /**
     * 构建点击操作的结果（使用元素ID）
     */
    private TaskResult buildTapResult(boolean success, String elementId, Map<String, Object> target) {
        if (success) {
            return TaskResult.builder()
                .status(TaskStatus.COMPLETED)
                .message("Tap operation completed successfully")
                .data(Map.of(
                    "target", target,
                    "elementId", elementId
                ))
                .build();
        } else {
            return TaskResult.builder()
                .status(TaskStatus.FAILED)
                .message("Tap operation failed")
                .data(Map.of(
                    "target", target,
                    "elementId", elementId
                ))
                .build();
        }
    }
    
    /**
     * 构建带坐标的点击操作结果
     */
    private TaskResult buildTapResult(boolean success, int x, int y, Map<String, Object> target) {
        if (success) {
            return TaskResult.builder()
                .status(TaskStatus.COMPLETED)
                .message("Tap operation completed successfully")
                .data(Map.of(
                    "target", target,
                    "x", x,
                    "y", y
                ))
                .build();
        } else {
            return TaskResult.builder()
                .status(TaskStatus.FAILED)
                .message("Tap operation failed")
                .data(Map.of(
                    "target", target,
                    "x", x,
                    "y", y
                ))
                .build();
        }
    }
    
    /**
     * 构建输入操作的结果
     */
    private TaskResult buildInputResult(boolean success, String elementId, String text, Map<String, Object> target) {
        if (success) {
            return TaskResult.builder()
                .status(TaskStatus.COMPLETED)
                .message("Input operation completed successfully")
                .data(Map.of(
                    "target", target,
                    "elementId", elementId,
                    "text", text
                ))
                .build();
        } else {
            return TaskResult.builder()
                .status(TaskStatus.FAILED)
                .message("Input operation failed")
                .data(Map.of(
                    "target", target,
                    "elementId", elementId,
                    "text", text
                ))
                .build();
        }
    }
    
    /**
     * 构建带坐标的输入操作结果
     */
    private TaskResult buildInputResult(boolean success, int x, int y, String text, Map<String, Object> target) {
        if (success) {
            return TaskResult.builder()
                .status(TaskStatus.COMPLETED)
                .message("Input operation completed successfully")
                .data(Map.of(
                    "target", target,
                    "x", x,
                    "y", y,
                    "text", text
                ))
                .build();
        } else {
            return TaskResult.builder()
                .status(TaskStatus.FAILED)
                .message("Input operation failed")
                .data(Map.of(
                    "target", target,
                    "x", x,
                    "y", y,
                    "text", text
                ))
                .build();
        }
    }
    
    /**
     * 构建滑动操作的结果
     */
    private TaskResult buildSwipeResult(boolean success, int startX, int startY, int endX, int endY) {
        if (success) {
            return TaskResult.builder()
                .status(TaskStatus.COMPLETED)
                .message("Swipe operation completed successfully")
                .data(Map.of(
                    "startX", startX,
                    "startY", startY,
                    "endX", endX,
                    "endY", endY
                ))
                .build();
        } else {
            return TaskResult.builder()
                .status(TaskStatus.FAILED)
                .message("Swipe operation failed")
                .data(Map.of(
                    "startX", startX,
                    "startY", startY,
                    "endX", endX,
                    "endY", endY
                ))
                .build();
        }
    }
    
    /**
     * 构建错误结果
     */
    private TaskResult buildErrorResult(String message, Throwable ex, Map<String, Object> data) {
        return TaskResult.builder()
            .status(TaskStatus.FAILED)
            .message(message + ": " + (ex != null ? ex.getMessage() : "Unknown error"))
            .data(data)
            .build();
    }
}