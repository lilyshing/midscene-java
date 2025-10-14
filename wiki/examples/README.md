# Midscene Java 使用示例与最佳实践

## 概述

本文档提供了 Midscene Java 的使用示例和最佳实践，帮助开发者快速上手并高效使用框架的各种功能。

## 目录

- [快速入门示例](#快速入门示例)
- [Agent 使用示例](#agent-使用示例)
- [Playground 使用示例](#playground-使用示例)
- [MCP 服务集成示例](#mcp-服务集成示例)
- [跨平台使用示例](#跨平台使用示例)
- [最佳实践](#最佳实践)
- [常见问题解决方案](#常见问题解决方案)

## 快速入门示例

### Web 平台快速入门

```java
import com.midscene.web.driver.WebDriverPlatform;
import com.midscene.core.agent.BaseAgent;
import com.midscene.core.service.DefaultAIModelService;
import com.midscene.core.service.DefaultInsightEngine;
import com.midscene.core.service.DefaultTaskExecutor;
import com.midscene.core.model.TaskResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebQuickStart {
    public static void main(String[] args) {
        try {
            // 初始化 WebDriver 平台
            WebDriverPlatform platform = new WebDriverPlatform();
            Map<String, Object> platformOptions = new HashMap<>();
            platformOptions.put("browser", "chrome");
            platformOptions.put("headless", false);
            platform.initialize(platformOptions).join();
            
            // 初始化 Agent 组件
            DefaultAIModelService aiModelService = new DefaultAIModelService();
            DefaultInsightEngine insightEngine = new DefaultInsightEngine();
            DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();
            
            // 创建 Agent 实例
            BaseAgent agent = new BaseAgent(platform, aiModelService, insightEngine, taskExecutor);
            
            // 初始化 Agent
            agent.initialize(platformOptions).join();
            
            // 导航到目标网站
            platform.navigateTo("https://example.com").join();
            
            // 执行 AI 驱动操作
            CompletableFuture<TaskResult> resultFuture = agent.aiAction("点击页面中的 'More information...' 链接");
            TaskResult result = resultFuture.join();
            
            // 处理结果
            System.out.println("操作状态: " + result.getStatus());
            System.out.println("操作结果: " + result.getResult());
            
            // 清理资源
            agent.close();
            platform.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Android 平台快速入门

```java
import com.midscene.android.AndroidPlatform;
import com.midscene.core.agent.BaseAgent;
import com.midscene.core.service.DefaultAIModelService;
import com.midscene.core.service.DefaultInsightEngine;
import com.midscene.core.service.DefaultTaskExecutor;

import java.util.HashMap;
import java.util.Map;

public class AndroidQuickStart {
    public static void main(String[] args) {
        try {
            // 初始化 Android 平台
            AndroidPlatform platform = new AndroidPlatform();
            Map<String, Object> platformOptions = new HashMap<>();
            platformOptions.put("deviceId", "emulator-5554"); // 设备 ID
            platform.initialize(platformOptions).join();
            
            // 初始化 Agent 组件
            DefaultAIModelService aiModelService = new DefaultAIModelService();
            DefaultInsightEngine insightEngine = new DefaultInsightEngine();
            DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();
            
            // 创建 Agent 实例
            BaseAgent agent = new BaseAgent(platform, aiModelService, insightEngine, taskExecutor);
            
            // 初始化 Agent
            agent.initialize(platformOptions).join();
            
            // 执行 AI 驱动操作
            agent.aiAction("打开设置应用").join();
            
            // 清理资源
            agent.close();
            platform.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Agent 使用示例

### 基本操作示例

```java
import com.midscene.core.agent.BaseAgent;
import com.midscene.core.model.TaskResult;
import com.midscene.web.driver.WebDriverPlatform;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AgentBasicExamples {
    public static void main(String[] args) {
        try {
            // 设置平台
            WebDriverPlatform platform = new WebDriverPlatform();
            Map<String, Object> options = new HashMap<>();
            options.put("browser", "chrome");
            platform.initialize(options).join();
            
            // 创建 Agent
            BaseAgent agent = new BaseAgent(platform);
            agent.initialize(options).join();
            
            // 导航到测试页面
            platform.navigateTo("https://example.com").join();
            
            // 示例 1: 通用 AI 操作
            CompletableFuture<TaskResult> result1 = agent.aiAction("提取页面上的所有链接文本");
            TaskResult taskResult1 = result1.join();
            System.out.println("提取的链接: " + taskResult1.getResult());
            
            // 示例 2: AI 点击操作
            CompletableFuture<TaskResult> result2 = agent.aiTap("更多信息链接");
            TaskResult taskResult2 = result2.join();
            System.out.println("点击操作结果: " + taskResult2.getStatus());
            
            // 示例 3: AI 输入操作
            CompletableFuture<TaskResult> result3 = agent.aiInput("搜索框", "Midscene Java");
            TaskResult taskResult3 = result3.join();
            System.out.println("输入操作结果: " + taskResult3.getStatus());
            
            // 示例 4: 数据提取
            CompletableFuture<TaskResult> result4 = agent.extractData("页面标题", "text");
            TaskResult taskResult4 = result4.join();
            System.out.println("页面标题: " + taskResult4.getResult());
            
            // 清理资源
            agent.close();
            platform.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 复杂任务示例

```java
import com.midscene.core.agent.BaseAgent;
import com.midscene.core.model.TaskResult;
import com.midscene.web.driver.WebDriverPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AgentComplexTaskExample {
    public static void main(String[] args) {
        try {
            // 设置平台和 Agent
            WebDriverPlatform platform = new WebDriverPlatform();
            Map<String, Object> options = new HashMap<>();
            options.put("browser", "chrome");
            platform.initialize(options).join();
            
            BaseAgent agent = new BaseAgent(platform);
            agent.initialize(options).join();
            
            // 执行复杂任务序列
            executeLoginSequence(agent, "https://example-login.com", "username", "password");
            
            // 清理资源
            agent.close();
            platform.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void executeLoginSequence(BaseAgent agent, String url, String username, String password) {
        try {
            // 导航到登录页面
            agent.getPlatform().navigateTo(url).join();
            
            // 登录序列
            System.out.println("开始登录流程...");
            
            // 输入用户名
            TaskResult usernameResult = agent.aiInput("用户名输入框", username).join();
            if (usernameResult.getStatus().isSuccess()) {
                System.out.println("用户名输入成功");
                
                // 输入密码
                TaskResult passwordResult = agent.aiInput("密码输入框", password).join();
                if (passwordResult.getStatus().isSuccess()) {
                    System.out.println("密码输入成功");
                    
                    // 点击登录按钮
                    TaskResult loginResult = agent.aiTap("登录按钮").join();
                    if (loginResult.getStatus().isSuccess()) {
                        System.out.println("登录成功");
                        
                        // 验证登录状态
                        TaskResult verifyResult = agent.aiAction("检查是否成功登录，查找欢迎信息或用户头像").join();
                        System.out.println("登录验证结果: " + verifyResult.getResult());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("登录流程失败: " + e.getMessage());
            throw e;
        }
    }
    
    // 批量数据提取示例
    private static List<String> extractMultipleElements(BaseAgent agent, List<String> elementDescriptions) {
        List<CompletableFuture<TaskResult>> futures = elementDescriptions.stream()
                .map(desc -> agent.aiAction("提取" + desc + "的内容"))
                .collect(Collectors.toList());
        
        // 等待所有提取操作完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        // 收集结果
        return allOf.thenApply(v -> 
                futures.stream()
                        .map(CompletableFuture::join)
                        .map(TaskResult::getResult)
                        .collect(Collectors.toList())
        ).join();
    }
}
```

## Playground 使用示例

### 基本代码执行

```java
import com.midscene.playground.Playground;
import com.midscene.playground.PlaygroundSession;
import com.midscene.playground.ExecutionResult;

import java.util.concurrent.CompletableFuture;

public class PlaygroundBasicExample {
    public static void main(String[] args) {
        // 创建 Playground 实例
        Playground playground = Playground.builder()
            .withMaxSessions(5)
            .withSessionTimeout(3600) // 1小时
            .withSecureMode(true)
            .build();
        
        try {
            // 创建执行会话
            String sessionId = playground.createSession();
            PlaygroundSession session = playground.getSession(sessionId);
            
            // 执行简单的 Java 代码
            String code = "\"Hello, Midscene Playground!\"";
            CompletableFuture<ExecutionResult> resultFuture = session.executeCode(code);
            
            // 获取执行结果
            ExecutionResult result = resultFuture.join();
            
            if (result.isSuccess()) {
                System.out.println("执行成功!");
                System.out.println("输出: " + result.getOutput());
                System.out.println("执行时间: " + result.getExecutionTime() + "ms");
            } else {
                System.err.println("执行失败!");
                System.err.println("错误: " + result.getError());
                if (result.getException() != null) {
                    result.getException().printStackTrace();
                }
            }
            
        } finally {
            // 清理资源
            playground.shutdown();
        }
    }
}
```

### 高级代码执行

```java
import com.midscene.playground.Playground;
import com.midscene.playground.PlaygroundSession;
import com.midscene.playground.ExecutionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlaygroundAdvancedExample {
    public static void main(String[] args) {
        // 创建 Playground 实例
        Playground playground = Playground.builder()
            .withMaxSessions(10)
            .withSessionTimeout(1800) // 30分钟
            .withSecureMode(true)
            .build();
        
        try {
            // 创建执行会话
            String sessionId = playground.createSession();
            PlaygroundSession session = playground.getSession(sessionId);
            
            // 示例 1: 执行带参数的代码
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", "Midscene User");
            parameters.put("version", 1.0);
            
            String codeWithParams = "\"Hello, " + "${name}" + "! You're using version " + "${version}" + ".\"";
            CompletableFuture<ExecutionResult> paramResult = session.executeCode(codeWithParams, parameters);
            
            // 示例 2: 执行复杂计算
            String complexCode = """
            int sum = 0;
            for (int i = 1; i <= 100; i++) {
                sum += i;
            }
            sum;
            """;
            
            CompletableFuture<ExecutionResult> complexResult = session.executeCode(complexCode);
            
            // 并行处理结果
            CompletableFuture.allOf(paramResult, complexResult).join();
            
            // 输出参数化执行结果
            ExecutionResult paramExecutionResult = paramResult.join();
            System.out.println("参数化执行结果: " + paramExecutionResult.getOutput());
            
            // 输出复杂计算结果
            ExecutionResult complexExecutionResult = complexResult.join();
            System.out.println("复杂计算结果: " + complexExecutionResult.getOutput());
            
        } finally {
            // 清理资源
            playground.shutdown();
        }
    }
}
```

## MCP 服务集成示例

### 基本 MCP 交互

```java
import com.midscene.playground.Playground;
import com.midscene.mcp.McpService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class McpIntegrationExample {
    public static void main(String[] args) {
        // 创建配置了 MCP 服务的 Playground 实例
        Playground playground = Playground.builder()
            .withMcpServiceUrl("http://localhost:8080/mcp")
            .build();
        
        try {
            // 准备提示和参数
            String prompt = "分析以下代码并提供改进建议";
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("code", """
            public void inefficientMethod() {
                List<String> results = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    results.add(String.valueOf(i));
                }
                String joined = "";
                for (String s : results) {
                    joined += s + ", ";
                }
                return joined;
            }
            """);
            parameters.put("language", "java");
            parameters.put("analysisType", "performance");
            
            // 发送到 MCP 服务
            CompletableFuture<String> mcpResultFuture = playground.sendPromptViaMcp(prompt, parameters);
            
            // 处理结果
            String mcpResult = mcpResultFuture.join();
            System.out.println("MCP 服务分析结果:");
            System.out.println(mcpResult);
            
            // 直接使用 McpService
            McpService mcpService = playground.getMcpService();
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("prompt", "解释 Java 中的 Stream API");
            requestParams.put("format", "markdown");
            
            CompletableFuture<String> directResult = mcpService.sendRequest("explain", requestParams);
            String directMcpResult = directResult.join();
            System.out.println("\n直接 MCP 请求结果:");
            System.out.println(directMcpResult);
            
        } catch (Exception e) {
            System.err.println("MCP 服务交互失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            playground.shutdown();
        }
    }
}
```

### 高级 MCP 交互模式

```java
import com.midscene.playground.Playground;
import com.midscene.mcp.McpService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AdvancedMcpInteractionExample {
    public static void main(String[] args) {
        // 创建 Playground 实例
        Playground playground = Playground.builder()
            .withMcpServiceUrl("http://localhost:8080/mcp")
            .build();
        
        try {
            // 示例 1: 批量 MCP 请求
            List<String> prompts = new ArrayList<>();
            prompts.add("解释 Java 中的多线程");
            prompts.add("解释 Java 中的集合框架");
            prompts.add("解释 Java 中的 Lambda 表达式");
            
            List<CompletableFuture<String>> futures = prompts.stream()
                .map(prompt -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("prompt", prompt);
                    params.put("max_tokens", 500);
                    return playground.sendPromptViaMcp(prompt, params);
                })
                .collect(Collectors.toList());
            
            // 等待所有请求完成并收集结果
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            List<String> results = allOf.thenApply(v ->
                futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList())
            ).join();
            
            // 输出结果
            for (int i = 0; i < prompts.size(); i++) {
                System.out.println("\n=== " + prompts.get(i) + " ===");
                System.out.println(results.get(i));
            }
            
            // 示例 2: 多轮对话
            executeMultiTurnConversation(playground);
            
        } finally {
            playground.shutdown();
        }
    }
    
    private static void executeMultiTurnConversation(Playground playground) {
        try {
            // 初始化对话历史
            List<Map<String, String>> conversationHistory = new ArrayList<>();
            
            // 第一轮对话
            String initialPrompt = "创建一个简单的 Java 类来表示一个矩形，包含计算面积和周长的方法";
            Map<String, Object> params1 = new HashMap<>();
            params1.put("prompt", initialPrompt);
            params1.put("conversation_history", conversationHistory);
            
            String response1 = playground.sendPromptViaMcp(initialPrompt, params1).join();
            System.out.println("\n=== 第一轮对话 ===");
            System.out.println("用户: " + initialPrompt);
            System.out.println("AI: " + response1);
            
            // 更新对话历史
            Map<String, String> userMessage1 = new HashMap<>();
            userMessage1.put("role", "user");
            userMessage1.put("content", initialPrompt);
            
            Map<String, String> aiMessage1 = new HashMap<>();
            aiMessage1.put("role", "assistant");
            aiMessage1.put("content", response1);
            
            conversationHistory.add(userMessage1);
            conversationHistory.add(aiMessage1);
            
            // 第二轮对话
            String followupPrompt = "现在添加一个方法来检查两个矩形是否重叠";
            Map<String, Object> params2 = new HashMap<>();
            params2.put("prompt", followupPrompt);
            params2.put("conversation_history", conversationHistory);
            
            String response2 = playground.sendPromptViaMcp(followupPrompt, params2).join();
            System.out.println("\n=== 第二轮对话 ===");
            System.out.println("用户: " + followupPrompt);
            System.out.println("AI: " + response2);
            
        } catch (Exception e) {
            System.err.println("多轮对话失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## 跨平台使用示例

### 平台适配器模式

```java
import com.midscene.core.agent.BaseAgent;
import com.midscene.core.platform.PlatformInterface;
import com.midscene.web.driver.WebDriverPlatform;
import com.midscene.android.AndroidPlatform;
import com.midscene.ios.IOSPlatform;
import com.midscene.core.model.TaskResult;

import java.util.HashMap;
import java.util.Map;

public class CrossPlatformExample {
    public static void main(String[] args) {
        try {
            // 示例: 根据目标平台选择合适的实现
            String targetPlatform = "web"; // 可以是 "web", "android", 或 "ios"
            
            // 创建平台实例
            PlatformInterface platform = createPlatformInstance(targetPlatform);
            
            // 初始化平台
            Map<String, Object> platformOptions = getPlatformOptions(targetPlatform);
            platform.initialize(platformOptions).join();
            
            // 创建 Agent
            BaseAgent agent = new BaseAgent(platform);
            agent.initialize(platformOptions).join();
            
            // 执行跨平台操作
            TaskResult result = agent.aiAction("查找并点击主页上的主要按钮").join();
            System.out.println("操作结果: " + result.getStatus());
            
            // 清理资源
            agent.close();
            platform.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static PlatformInterface createPlatformInstance(String platformType) {
        switch (platformType.toLowerCase()) {
            case "web":
                return new WebDriverPlatform();
            case "android":
                return new AndroidPlatform();
            case "ios":
                return new IOSPlatform();
            default:
                throw new IllegalArgumentException("不支持的平台类型: " + platformType);
        }
    }
    
    private static Map<String, Object> getPlatformOptions(String platformType) {
        Map<String, Object> options = new HashMap<>();
        
        switch (platformType.toLowerCase()) {
            case "web":
                options.put("browser", "chrome");
                options.put("headless", false);
                options.put("url", "https://example.com");
                break;
            case "android":
                options.put("deviceId", "emulator-5554");
                options.put("appPackage", "com.example.app");
                break;
            case "ios":
                options.put("deviceId", "device-id");
                options.put("bundleId", "com.example.app");
                break;
        }
        
        return options;
    }
    
    // 通用任务执行器，可以在不同平台上执行相同的任务逻辑
    public static class CrossPlatformTaskExecutor {
        private final BaseAgent agent;
        
        public CrossPlatformTaskExecutor(BaseAgent agent) {
            this.agent = agent;
        }
        
        public void performLogin(String username, String password) {
            // 通用登录逻辑，适用于任何平台
            agent.aiAction("导航到登录页面").join();
            agent.aiInput("用户名输入框", username).join();
            agent.aiInput("密码输入框", password).join();
            agent.aiTap("登录按钮").join();
            
            // 验证登录成功
            TaskResult result = agent.aiAction("检查是否成功登录").join();
            System.out.println("登录验证: " + result.getResult());
        }
        
        public void searchForItem(String searchTerm) {
            // 通用搜索逻辑
            agent.aiTap("搜索图标").join();
            agent.aiInput("搜索输入框", searchTerm).join();
            agent.aiAction("提交搜索").join();
        }
    }
}
```

## 最佳实践

### 1. 资源管理最佳实践

```java
// 错误的资源管理
public void badResourceManagement() {
    Playground playground = Playground.builder().build();
    String sessionId = playground.createSession();
    PlaygroundSession session = playground.getSession(sessionId);
    // 执行操作，但忘记关闭资源
    session.executeCode("System.out.println(\"Hello\");").join();
    // 没有调用 close() 方法
}

// 正确的资源管理
public void goodResourceManagement() {
    Playground playground = null;
    PlaygroundSession session = null;
    
    try {
        playground = Playground.builder().build();
        String sessionId = playground.createSession();
        session = playground.getSession(sessionId);
        
        // 执行操作
        session.executeCode("System.out.println(\"Hello\");").join();
        
    } finally {
        // 确保关闭资源
        if (session != null) {
            session.close();
        }
        if (playground != null) {
            playground.shutdown();
        }
    }
}

// 使用 try-with-resources (如果类实现了 AutoCloseable)
public void tryWithResources() {
    try (Playground playground = Playground.builder().build();
         PlaygroundSession session = playground.getSession(playground.createSession())) {
        
        // 执行操作
        session.executeCode("System.out.println(\"Hello\");").join();
        
    } // 自动调用 close() 方法
}
```

### 2. 异步操作最佳实践

```java
// 错误的异步处理
public void badAsyncHandling() {
    Playground playground = Playground.builder().build();
    String sessionId = playground.createSession();
    PlaygroundSession session = playground.getSession(sessionId);
    
    // 直接使用 join() 阻塞主线程
    ExecutionResult result = session.executeCode("System.out.println(\"Hello\");").join();
    System.out.println(result.getOutput());
    
    playground.shutdown();
}

// 正确的异步处理 - 非阻塞方式
public void goodAsyncHandling() {
    Playground playground = Playground.builder().build();
    String sessionId = playground.createSession();
    PlaygroundSession session = playground.getSession(sessionId);
    
    // 使用回调处理异步结果
    session.executeCode("System.out.println(\"Hello\");")
           .thenAccept(result -> {
               System.out.println(result.getOutput());
               // 关闭资源
               session.close();
               playground.shutdown();
           })
           .exceptionally(ex -> {
               System.err.println("执行失败: " + ex.getMessage());
               ex.printStackTrace();
               // 确保错误情况下也关闭资源
               session.close();
               playground.shutdown();
               return null;
           });
    
    // 主线程可以继续执行其他操作
    System.out.println("操作已提交，主线程继续执行");
}

// 组合多个异步操作
public void composeAsyncOperations() {
    Playground playground = Playground.builder().build();
    String sessionId = playground.createSession();
    PlaygroundSession session = playground.getSession(sessionId);
    
    // 第一个操作
    CompletableFuture<ExecutionResult> firstOp = session.executeCode("1 + 1");
    
    // 组合操作 - 第二个操作依赖第一个操作的结果
    CompletableFuture<ExecutionResult> secondOp = firstOp.thenCompose(result -> {
        String firstResult = result.getOutput();
        return session.executeCode(firstResult + " * 2");
    });
    
    // 处理最终结果
    secondOp.thenAccept(finalResult -> {
        System.out.println("最终结果: " + finalResult.getOutput());
        session.close();
        playground.shutdown();
    }).exceptionally(ex -> {
        System.err.println("组合操作失败: " + ex.getMessage());
        ex.printStackTrace();
        session.close();
        playground.shutdown();
        return null;
    });
}
```

### 3. 异常处理最佳实践

```java
// 错误的异常处理
public void badExceptionHandling() {
    try {
        Playground playground = Playground.builder().build();
        String sessionId = playground.createSession();
        PlaygroundSession session = playground.getSession(sessionId);
        
        // 没有具体的异常类型，捕获所有异常
        ExecutionResult result = session.executeCode("invalid code").join();
        
    } catch (Exception e) {
        // 没有记录具体错误信息
        System.out.println("发生错误");
    }
}

// 正确的异常处理
public void goodExceptionHandling() {
    Playground playground = null;
    PlaygroundSession session = null;
    
    try {
        playground = Playground.builder().build();
        String sessionId = playground.createSession();
        session = playground.getSession(sessionId);
        
        // 捕获特定类型的异常
        ExecutionResult result = session.executeCode("invalid code").join();
        
        // 检查执行结果中的错误
        if (!result.isSuccess()) {
            System.err.println("代码执行错误: " + result.getError());
            if (result.getException() != null) {
                System.err.println("异常类型: " + result.getException().getClass().getName());
            }
        }
        
    } catch (IllegalArgumentException e) {
        System.err.println("参数错误: " + e.getMessage());
        e.printStackTrace();
    } catch (PlaygroundException e) {
        System.err.println("Playground 模块错误: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        System.err.println("未预期的错误: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // 确保资源释放
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("关闭会话时出错: " + e.getMessage());
            }
        }
        if (playground != null) {
            try {
                playground.shutdown();
            } catch (Exception e) {
                System.err.println("关闭 Playground 时出错: " + e.getMessage());
            }
        }
    }
}
```

### 4. 配置管理最佳实践

```java
// 错误的配置管理
public void badConfiguration() {
    // 硬编码配置值
    Playground playground = Playground.builder()
        .withMaxSessions(10)
        .withSessionTimeout(3600)
        .withMcpServiceUrl("http://localhost:8080/mcp")
        .build();
}

// 正确的配置管理
public void goodConfiguration() {
    // 从环境变量或配置文件加载配置
    int maxSessions = Integer.parseInt(System.getenv().getOrDefault("PLAYGROUND_MAX_SESSIONS", "10"));
    int sessionTimeout = Integer.parseInt(System.getenv().getOrDefault("PLAYGROUND_SESSION_TIMEOUT", "3600"));
    String mcpUrl = System.getenv().getOrDefault("PLAYGROUND_MCP_URL", "http://localhost:8080/mcp");
    boolean secureMode = Boolean.parseBoolean(System.getenv().getOrDefault("PLAYGROUND_SECURE_MODE", "true"));
    
    // 使用配置值
    Playground playground = Playground.builder()
        .withMaxSessions(maxSessions)
        .withSessionTimeout(sessionTimeout)
        .withMcpServiceUrl(mcpUrl)
        .withSecureMode(secureMode)
        .build();
}

// 使用配置类管理配置
public class PlaygroundConfig {
    private int maxSessions;
    private int sessionTimeout;
    private String mcpServiceUrl;
    private boolean secureMode;
    
    // 构造函数、getter 和 setter 方法
    // ...
    
    // 从环境变量加载配置
    public static PlaygroundConfig fromEnvironment() {
        PlaygroundConfig config = new PlaygroundConfig();
        config.setMaxSessions(Integer.parseInt(System.getenv().getOrDefault("PLAYGROUND_MAX_SESSIONS", "10")));
        config.setSessionTimeout(Integer.parseInt(System.getenv().getOrDefault("PLAYGROUND_SESSION_TIMEOUT", "3600")));
        config.setMcpServiceUrl(System.getenv().getOrDefault("PLAYGROUND_MCP_URL", "http://localhost:8080/mcp"));
        config.setSecureMode(Boolean.parseBoolean(System.getenv().getOrDefault("PLAYGROUND_SECURE_MODE", "true")));
        return config;
    }
}

// 使用配置类
public void useConfigClass() {
    PlaygroundConfig config = PlaygroundConfig.fromEnvironment();
    
    Playground playground = Playground.builder()
        .withMaxSessions(config.getMaxSessions())
        .withSessionTimeout(config.getSessionTimeout())
        .withMcpServiceUrl(config.getMcpServiceUrl())
        .withSecureMode(config.isSecureMode())
        .build();
}
```

## 常见问题解决方案

### 1. 会话创建失败

**问题**：调用 `createSession()` 返回 null 或抛出异常

**解决方案**：

```java
public String safeCreateSession(Playground playground) {
    try {
        // 检查是否已达到最大会话数
        int activeSessions = playground.getActiveSessionsCount(); // 假设存在此方法
        int maxSessions = playground.getMaxSessions(); // 假设存在此方法
        
        System.out.println("当前活动会话数: " + activeSessions + ", 最大会话数: " + maxSessions);
        
        // 如果接近最大会话数，清理过期会话
        if (activeSessions >= maxSessions * 0.9) { // 当达到 90% 容量时清理
            playground.cleanupExpiredSessions();
            System.out.println("已清理过期会话");
        }
        
        // 创建新会话
        String sessionId = playground.createSession();
        if (sessionId == null) {
            throw new PlaygroundException("创建会话失败：会话 ID 为 null");
        }
        
        System.out.println("成功创建会话: " + sessionId);
        return sessionId;
        
    } catch (Exception e) {
        System.err.println("创建会话时发生错误: " + e.getMessage());
        // 尝试备选方案 - 强制关闭最旧的会话
        try {
            String oldestSessionId = playground.getOldestSessionId(); // 假设存在此方法
            if (oldestSessionId != null) {
                System.out.println("尝试关闭最旧的会话: " + oldestSessionId);
                playground.closeSession(oldestSessionId);
                // 再次尝试创建会话
                return playground.createSession();
            }
        } catch (Exception fallbackEx) {
            System.err.println("备选方案也失败: " + fallbackEx.getMessage());
        }
        throw e;
    }
}
```

### 2. 代码执行超时

**问题**：代码执行时间过长或超时

**解决方案**：

```java
public ExecutionResult executeCodeWithTimeout(PlaygroundSession session, String code, long timeoutMs) {
    CompletableFuture<ExecutionResult> executionFuture = session.executeCode(code);
    
    try {
        // 设置超时
        return executionFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
        
    } catch (TimeoutException e) {
        System.err.println("代码执行超时 (" + timeoutMs + "ms)");
        
        // 取消执行（如果支持）
        executionFuture.cancel(true);
        
        // 创建超时结果对象
        ExecutionResult timeoutResult = new ExecutionResult(); // 假设存在此构造函数
        timeoutResult.setSuccess(false);
        timeoutResult.setError("代码执行超时: " + timeoutMs + "ms 后未完成");
        timeoutResult.setException(e);
        timeoutResult.setExecutionTime(timeoutMs);
        
        return timeoutResult;
        
    } catch (Exception e) {
        System.err.println("执行代码时发生错误: " + e.getMessage());
        throw new RuntimeException("代码执行失败", e);
    }
}

// 使用示例
public void useTimeoutExecution() {
    Playground playground = Playground.builder().build();
    String sessionId = playground.createSession();
    PlaygroundSession session = playground.getSession(sessionId);
    
    // 可能会超时的代码
    String longRunningCode = """
    Thread.sleep(5000); // 睡眠 5 秒
    "Hello after sleep";
    """;
    
    try {
        // 设置 3 秒超时
        ExecutionResult result = executeCodeWithTimeout(session, longRunningCode, 3000);
        
        if (result.isSuccess()) {
            System.out.println("执行成功: " + result.getOutput());
        } else {
            System.err.println("执行失败: " + result.getError());
        }
        
    } finally {
        session.close();
        playground.shutdown();
    }
}
```

### 3. MCP 服务连接问题

**问题**：与 MCP 服务通信失败

**解决方案**：

```java
public class McpServiceConnector {
    private final Playground playground;
    private final int maxRetries;
    private final long retryDelayMs;
    
    public McpServiceConnector(Playground playground, int maxRetries, long retryDelayMs) {
        this.playground = playground;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }
    
    public String sendWithRetry(String prompt, Map<String, Object> parameters) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            attempts++;
            
            try {
                System.out.println("尝试发送 MCP 请求 (尝试 " + attempts + "/" + maxRetries + ")");
                
                // 发送请求
                return playground.sendPromptViaMcp(prompt, parameters).join();
                
            } catch (Exception e) {
                lastException = e;
                System.err.println("MCP 请求失败: " + e.getMessage());
                
                // 如果不是最后一次尝试，则等待并重试
                if (attempts < maxRetries) {
                    try {
                        System.out.println("将在 " + retryDelayMs + "ms 后重试...");
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        // 所有尝试都失败
        throw new RuntimeException("MCP 请求在 " + maxRetries + " 次尝试后失败", lastException);
    }
    
    // 检查 MCP 服务健康状态
    public boolean checkMcpHealth() {
        try {
            Map<String, Object> healthParams = new HashMap<>();
            healthParams.put("action", "health_check");
            
            String result = playground.getMcpService().sendRequest("health", healthParams).join();
            return result.contains("ok") || result.contains("healthy");
            
        } catch (Exception e) {
            System.err.println("MCP 服务健康检查失败: " + e.getMessage());
            return false;
        }
    }
}

// 使用示例
public void useResilientMcpConnector() {
    Playground playground = Playground.builder()
        .withMcpServiceUrl("http://localhost:8080/mcp")
        .build();
    
    try {
        McpServiceConnector connector = new McpServiceConnector(playground, 3, 1000);
        
        // 首先检查 MCP 服务健康状态
        if (connector.checkMcpHealth()) {
            System.out.println("MCP 服务健康检查通过");
            
            // 发送请求
            Map<String, Object> params = new HashMap<>();
            params.put("prompt", "解释 Java 的异常处理机制");
            
            String result = connector.sendWithRetry("请详细解释", params);
            System.out.println("MCP 响应: " + result);
            
        } else {
            System.err.println("MCP 服务不可用");
            // 提供备选方案...
        }
        
    } finally {
        playground.shutdown();
    }
}
```

### 4. 并发控制问题

**问题**：多线程环境下的并发问题

**解决方案**：

```java
public class ThreadSafePlaygroundManager {
    private final Playground playground;
    private final ConcurrentHashMap<String, PlaygroundSession> activeSessions;
    private final Semaphore sessionSemaphore;
    
    public ThreadSafePlaygroundManager(int maxConcurrentSessions) {
        this.playground = Playground.builder()
            .withMaxSessions(maxConcurrentSessions)
            .build();
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionSemaphore = new Semaphore(maxConcurrentSessions);
    }
    
    // 获取会话（线程安全）
    public String getOrCreateSession(String sessionId) {
        try {
            // 尝试获取信号量
            sessionSemaphore.acquire();
            
            // 检查会话是否已存在
            if (activeSessions.containsKey(sessionId)) {
                // 更新最后活动时间
                updateSessionActivity(sessionId);
                return sessionId;
            }
            
            // 创建新会话
            String newSessionId = playground.createSession();
            PlaygroundSession session = playground.getSession(newSessionId);
            activeSessions.put(newSessionId, session);
            
            System.out.println("创建新会话: " + newSessionId);
            return newSessionId;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取会话时被中断", e);
        } catch (Exception e) {
            sessionSemaphore.release(); // 确保释放信号量
            throw e;
        }
    }
    
    // 关闭会话（线程安全）
    public void closeSession(String sessionId) {
        if (sessionId != null && activeSessions.containsKey(sessionId)) {
            PlaygroundSession session = activeSessions.remove(sessionId);
            
            try {
                if (session != null) {
                    session.close();
                }
                playground.closeSession(sessionId);
                System.out.println("关闭会话: " + sessionId);
                
            } finally {
                // 释放信号量
                sessionSemaphore.release();
            }
        }
    }
    
    // 执行代码（线程安全）
    public ExecutionResult executeCode(String sessionId, String code) {
        try {
            // 检查会话是否存在
            PlaygroundSession session = activeSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("会话不存在: " + sessionId);
            }
            
            // 更新活动时间
            updateSessionActivity(sessionId);
            
            // 执行代码
            return session.executeCode(code).join();
            
        } catch (Exception e) {
            throw new RuntimeException("执行代码失败", e);
        }
    }
    
    private void updateSessionActivity(String sessionId) {
        // 实现会话活动更新逻辑
        // 可以记录最后活动时间用于会话超时管理
    }
    
    // 清理过期会话
    public void cleanupExpiredSessions() {
        playground.cleanupExpiredSessions();
        
        // 同步本地活动会话映射
        Set<String> expiredSessions = new HashSet<>();
        for (Map.Entry<String, PlaygroundSession> entry : activeSessions.entrySet()) {
            String sessionId = entry.getKey();
            if (!playground.hasActiveSession(sessionId)) { // 假设存在此方法
                expiredSessions.add(sessionId);
            }
        }
        
        // 移除过期会话
        for (String sessionId : expiredSessions) {
            activeSessions.remove(sessionId);
            sessionSemaphore.release(); // 释放信号量
            System.out.println("清理过期会话: " + sessionId);
        }
    }
    
    // 关闭管理器
    public void shutdown() {
        // 关闭所有活动会话
        for (String sessionId : new ArrayList<>(activeSessions.keySet())) {
            closeSession(sessionId);
        }
        
        // 关闭 Playground
        playground.shutdown();
        System.out.println("Playground 管理器已关闭");
    }
}

// 使用示例
public void useThreadSafeManager() {
    // 创建线程安全的 Playground 管理器
    ThreadSafePlaygroundManager manager = new ThreadSafePlaygroundManager(10);
    
    try {
        // 在多线程环境中使用
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executorService.submit(() -> {
                try {
                    // 获取或创建会话
                    String sessionId = manager.getOrCreateSession("session-" + (taskId % 5));
                    
                    // 执行代码
                    String code = "\"Task " + taskId + " executed in session " + sessionId + "\"";
                    ExecutionResult result = manager.executeCode(sessionId, code);
                    
                    System.out.println("任务 " + taskId + " 结果: " + result.getOutput());
                    
                    // 模拟业务逻辑
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("任务 " + taskId + " 失败: " + e.getMessage());
                }
            });
        }
        
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        
        // 定期清理过期会话
        manager.cleanupExpiredSessions();
        
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        manager.shutdown();
    }
}