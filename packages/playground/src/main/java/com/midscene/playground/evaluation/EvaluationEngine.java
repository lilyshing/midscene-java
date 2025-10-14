package com.midscene.playground.evaluation;

import com.midscene.playground.code.Executor;
import com.midscene.playground.code.ExecutionResult;
import com.midscene.playground.code.JavaExecutor;
import com.midscene.playground.config.PlaygroundConfig;
import com.midscene.playground.context.ContextManager;
import com.midscene.playground.security.SandboxManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 评估引擎
 * 负责管理代码执行和评估
 */
public class EvaluationEngine {
    private static final Logger logger = Logger.getLogger(EvaluationEngine.class.getName());
    private final Map<String, Executor> executors = new ConcurrentHashMap<>();
    private final ExecutorService threadPool;
    private final PlaygroundConfig config;
    private final SandboxManager sandboxManager;
    
    /**
     * 构造函数
     */
    public EvaluationEngine(PlaygroundConfig config, SandboxManager sandboxManager) {
        this.config = config;
        this.sandboxManager = sandboxManager;
        this.threadPool = Executors.newFixedThreadPool(
            Math.max(4, config.getMaxThreads()),
            r -> {
                Thread thread = new Thread(r, "playground-eval-");
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    logger.log(Level.SEVERE, "Uncaught exception in evaluation thread: " + t.getName(), e);
                });
                return thread;
            }
        );
        
        // 初始化默认执行器
        initializeDefaultExecutors();
        
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    /**
     * 初始化默认执行器
     */
    private void initializeDefaultExecutors() {
        // 注册Java执行器
        registerExecutor("java", new JavaExecutor(config, sandboxManager));
        logger.info("Initialized default executors");
    }
    
    /**
     * 注册执行器
     */
    public void registerExecutor(String language, Executor executor) {
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        
        if (executor == null) {
            throw new IllegalArgumentException("Executor cannot be null");
        }
        
        executors.put(language.toLowerCase(), executor);
        logger.info("Registered executor for language: " + language);
    }
    
    /**
     * 获取执行器
     */
    public Executor getExecutor(String language) {
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        
        Executor executor = executors.get(language.toLowerCase());
        if (executor == null) {
            throw new UnsupportedOperationException("No executor found for language: " + language);
        }
        
        return executor;
    }
    
    /**
     * 评估代码
     */
    public ExecutionResult evaluate(String language, String code, ContextManager context) {
        return evaluate(language, code, context, config.getExecutionTimeoutMs());
    }
    
    /**
     * 评估代码（指定超时时间）
     */
    public ExecutionResult evaluate(String language, String code, ContextManager context, long timeoutMs) {
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.failure("Code cannot be null or empty");
        }
        
        logger.info("Evaluating code in language: " + language + ", session: " + context.getSessionId());
        
        try {
            // 获取执行器
            Executor executor = getExecutor(language);
            
            // 验证代码安全性
            if (!isCodeSafe(code, context)) {
                return ExecutionResult.failure("Code contains potentially unsafe operations");
            }
            
            // 创建评估任务
            EvaluationTask task = new EvaluationTask(executor, code, context);
            
            // 提交任务到线程池
            Future<ExecutionResult> future = threadPool.submit(task);
            
            // 等待结果，带超时
            ExecutionResult result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            // 记录执行结果
            if (result.isSuccess()) {
                logger.info("Code evaluation successful, session: " + context.getSessionId());
            } else {
                logger.warning("Code evaluation failed, session: " + context.getSessionId() + ", error: " + result.getErrorMessage());
            }
            
            return result;
        } catch (TimeoutException e) {
            logger.warning("Code execution timed out, session: " + context.getSessionId());
            return ExecutionResult.failure("Execution timed out after " + timeoutMs + "ms");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error evaluating code, session: " + context.getSessionId(), e);
            return ExecutionResult.failure("Error during evaluation: " + e.getMessage());
        }
    }
    
    /**
     * 检查代码安全性
     */
    private boolean isCodeSafe(String code, ContextManager context) {
        // 这里可以实现更复杂的代码安全检查
        // 目前简单检查是否包含危险关键词
        String[] dangerousPatterns = {
            "System.exit",
            "Runtime.getRuntime()",
            "ProcessBuilder",
            "Thread.stop",
            "setSecurityManager",
            "ClassLoader.defineClass",
            "sun.misc.Unsafe"
        };
        
        for (String pattern : dangerousPatterns) {
            if (code.contains(pattern)) {
                logger.warning("Detected dangerous code pattern: " + pattern + " in session: " + context.getSessionId());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 批量评估代码
     */
    public Map<String, ExecutionResult> batchEvaluate(String language, Map<String, String> codeMap, ContextManager context) {
        Map<String, ExecutionResult> results = new ConcurrentHashMap<>();
        
        if (codeMap == null || codeMap.isEmpty()) {
            return results;
        }
        
        logger.info("Starting batch evaluation for " + codeMap.size() + " code snippets in session: " + context.getSessionId());
        
        // 并行评估所有代码片段
        codeMap.forEach((key, code) -> {
            try {
                ExecutionResult result = evaluate(language, code, context);
                results.put(key, result);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error evaluating code snippet: " + key, e);
                results.put(key, ExecutionResult.failure("Error: " + e.getMessage()));
            }
        });
        
        logger.info("Batch evaluation completed, session: " + context.getSessionId());
        return results;
    }
    
    /**
     * 预热执行器
     */
    public void warmupExecutors() {
        logger.info("Warming up executors");
        
        executors.forEach((language, executor) -> {
            try {
                logger.fine("Warming up executor for language: " + language);
                // 执行简单的代码片段进行预热
                String warmupCode = getWarmupCode(language);
                if (warmupCode != null) {
                    ContextManager dummyContext = new ContextManager("warmup");
                    executor.execute(warmupCode, dummyContext);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to warmup executor for language: " + language, e);
            }
        });
    }
    
    /**
     * 获取预热代码
     */
    private String getWarmupCode(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "System.out.println(\"Warming up Java executor\");";
            default:
                return null;
        }
    }
    
    /**
     * 关闭引擎
     */
    public void shutdown() {
        logger.info("Shutting down evaluation engine");
        
        // 关闭所有执行器
        executors.values().forEach(Executor::close);
        executors.clear();
        
        // 关闭线程池
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Evaluation engine shutdown completed");
    }
    
    /**
     * 获取支持的语言列表
     */
    public String[] getSupportedLanguages() {
        return executors.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查是否支持指定语言
     */
    public boolean isLanguageSupported(String language) {
        if (language == null || language.trim().isEmpty()) {
            return false;
        }
        return executors.containsKey(language.toLowerCase());
    }
    
    /**
     * 评估任务内部类
     */
    private static class EvaluationTask implements java.util.concurrent.Callable<ExecutionResult> {
        private final Executor executor;
        private final String code;
        private final ContextManager context;
        
        public EvaluationTask(Executor executor, String code, ContextManager context) {
            this.executor = executor;
            this.code = code;
            this.context = context;
        }
        
        @Override
        public ExecutionResult call() throws Exception {
            long startTime = System.currentTimeMillis();
            
            try {
                // 执行代码
                ExecutionResult result = executor.execute(code, context);
                
                // 设置执行时间
                long executionTime = System.currentTimeMillis() - startTime;
                result.setExecutionTimeMs(executionTime);
                
                return result;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error executing code in task", e);
                return ExecutionResult.failure("Execution error: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取线程池状态信息
     */
    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        
        // 注意：由于Java的ExecutorService不直接提供活跃线程数等信息
        // 这里使用反射获取ThreadPoolExecutor的状态
        try {
            java.lang.reflect.Field poolField = threadPool.getClass().getDeclaredField("pool");
            poolField.setAccessible(true);
            java.util.concurrent.ThreadPoolExecutor pool = (java.util.concurrent.ThreadPoolExecutor) threadPool;
            
            status.put("activeThreads", pool.getActiveCount());
            status.put("poolSize", pool.getPoolSize());
            status.put("corePoolSize", pool.getCorePoolSize());
            status.put("maxPoolSize", pool.getMaximumPoolSize());
            status.put("queueSize", pool.getQueue().size());
            status.put("completedTasks", pool.getCompletedTaskCount());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get thread pool status", e);
            status.put("error", e.getMessage());
        }
        
        return status;
    }
}