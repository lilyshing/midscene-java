package com.midscene.playground.code;

import com.midscene.playground.config.PlaygroundConfig;
import com.midscene.playground.security.SecureEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Java执行器
 * 负责安全执行Java代码
 */
public class JavaExecutor implements Executor {
    private final PlaygroundConfig config;
    private final SecureEnvironment secureEnvironment;
    private final ExecutorService threadPool;
    
    /**
     * 构造函数
     */
    public JavaExecutor(PlaygroundConfig config, SecureEnvironment secureEnvironment) {
        this.config = config;
        this.secureEnvironment = secureEnvironment;
        this.threadPool = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "JavaExecutor-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    @Override
    public ExecutionResult execute(String code) {
        return execute(code, null);
    }
    
    @Override
    public ExecutionResult execute(String code, Map<String, Object> context) {
        long timeoutMs = config.getExecutionTimeoutMs();
        return executeWithTimeout(code, context, timeoutMs);
    }
    
    @Override
    public ExecutionResult executeWithTimeout(String code, long timeoutMs) {
        return executeWithTimeout(code, null, timeoutMs);
    }
    
    @Override
    public ExecutionResult executeWithTimeout(String code, Map<String, Object> context, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        // 创建安全的执行环境
        SecureCodeRunner runner = new SecureCodeRunner(code, context);
        
        try {
            // 在独立线程中执行代码，支持超时控制
            Future<ExecutionResult.Builder> future = threadPool.submit(runner);
            ExecutionResult.Builder resultBuilder;
            
            if (timeoutMs > 0) {
                resultBuilder = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } else {
                resultBuilder = future.get();
            }
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            return resultBuilder.setExecutionTimeMs(executionTime).build();
        } catch (TimeoutException e) {
            future.cancel(true);
            return ExecutionResult.failure()
                    .setError("Execution timed out after " + timeoutMs + "ms")
                    .setExecutionTimeMs(timeoutMs)
                    .build();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return ExecutionResult.failure()
                    .setError(cause.getMessage())
                    .setException(cause instanceof Exception ? (Exception) cause : new Exception(cause))
                    .setExecutionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        } catch (Exception e) {
            return ExecutionResult.failure()
                    .setError("Failed to execute code: " + e.getMessage())
                    .setException(e)
                    .setExecutionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    @Override
    public String getSupportedLanguage() {
        return "java";
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return "java".equalsIgnoreCase(language);
    }
    
    @Override
    public void reset() {
        // 重置执行器状态
        // 在这个简单实现中，我们只需要确保没有资源泄漏
    }
    
    @Override
    public void close() {
        // 关闭线程池
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 关闭安全环境
        secureEnvironment.close();
    }
    
    /**
     * 安全代码运行器
     * 在安全环境中执行代码
     */
    private class SecureCodeRunner implements Callable<ExecutionResult.Builder> {
        private final String code;
        private final Map<String, Object> context;
        
        public SecureCodeRunner(String code, Map<String, Object> context) {
            this.code = code;
            this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        }
        
        @Override
        public ExecutionResult.Builder call() throws Exception {
            // 创建输出流捕获控制台输出
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            
            try {
                // 重定向标准输出和错误
                System.setOut(new PrintStream(outStream));
                System.setErr(new PrintStream(errStream));
                
                // 在安全环境中执行代码
                Object result = executeInSecureContext();
                
                // 构建成功结果
                ExecutionResult.Builder builder = ExecutionResult.success()
                        .setReturnValue(result);
                
                // 添加捕获的输出
                String output = outStream.toString("UTF-8");
                if (!output.isEmpty()) {
                    builder.setOutput(output);
                }
                
                String errorOutput = errStream.toString("UTF-8");
                if (!errorOutput.isEmpty()) {
                    builder.setError(errorOutput);
                }
                
                return builder;
            } catch (Exception e) {
                // 构建失败结果
                return ExecutionResult.failure()
                        .setError(e.getMessage())
                        .setException(e)
                        .setOutput(outStream.toString("UTF-8"))
                        .setError(errStream.toString("UTF-8"));
            } finally {
                // 恢复原始输出流
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }
        
        private Object executeInSecureContext() throws Exception {
            // 这里是简化版的实现
            // 实际项目中应该使用更安全的方式，如自定义类加载器、安全管理器等
            
            // 创建临时类代码
            String className = "PlaygroundCode";
            String fullClassName = "com.midscene.playground.code." + className;
            String classCode = generateClassCode(className, code);
            
            try {
                // 在实际实现中，这里应该使用自定义类加载器加载代码
                // 这里为了简化，直接返回null
                // 后续应该集成一个安全的代码编译器和执行器
                return null;
            } catch (Exception e) {
                throw new Exception("Failed to compile or execute code: " + e.getMessage(), e);
            }
        }
        
        private String generateClassCode(String className, String code) {
            StringBuilder classCode = new StringBuilder();
            classCode.append("package com.midscene.playground.code;\n\n");
            classCode.append("public class ").append(className).append(" {\n");
            classCode.append("    public static Object execute() {");
            
            // 如果代码不是以分号结尾，添加分号
            String trimmedCode = code.trim();
            if (!trimmedCode.endsWith(".") && !trimmedCode.endsWith(";")) {
                classCode.append("\n        return ").append(code).append(";\n");
            } else {
                classCode.append("\n        ").append(code).append("\n");
                classCode.append("        return null;\n");
            }
            
            classCode.append("    }\n");
            classCode.append("}");
            
            return classCode.toString();
        }
    }
}