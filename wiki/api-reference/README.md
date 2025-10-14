# API 参考

Midscene Java 提供了丰富的 API 接口，支持各种自动化操作场景。本章节详细介绍各个模块的 API 使用方法。

## 📋 目录

- [Agent API](agent-api.md)
- [Web 平台 API](web平台-api.md)
- [Android 平台 API](android平台-api.md)
- [AI 模型 API](ai模型-api.md)
- [配置 API](配置-api.md)
- [Playground API](playground-api.md)

## 🏗️ 核心模块

### 1. Agent 模块
提供 AI 驱动的自动化操作接口，是 Midscene Java 的核心 API。

```java
import com.midscene.core.Agent;
import com.midscene.core.AgentConfig;

// 创建 Agent 实例
Agent agent = new Agent(platformInterface);

// 带配置创建
AgentConfig config = AgentConfig.builder()
    .defaultTimeout(Duration.ofSeconds(30))
    .maxRetryCount(3)
    .build();
Agent agent = new Agent(platformInterface, config);
```

### 2. PlatformInterface 模块
提供跨平台的统一操作接口，是平台抽象层的核心。

```java
import com.midscene.core.PlatformInterface;

// 获取当前上下文
UIContext context = platformInterface.getCurrentContext();

// 执行操作
platformInterface.clickElement("elementId").get();
platformInterface.inputText("elementId", "text").get();
platformInterface.navigateTo("https://example.com").get();
```

### 3. AIModelService 模块
提供 AI 模型服务接口，支持多种 AI 提供商。

```java
import com.midscene.core.ai.AIModelService;
import com.midscene.core.ai.AIModelConfig;

// 创建 AI 服务
AIModelConfig config = AIModelConfig.builder()
    .provider("openai")
    .model("gpt-4-vision-preview")
    .apiKey("your-api-key")
    .build();
AIModelService aiService = new AIModelService(config);
```

## 🌐 平台特定 API

### Web 平台 API
提供 Web 自动化的专用接口，支持 Selenium 和 Playwright。

```java
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;

// 创建 Web 页面
PlaywrightPage page = new PlaywrightPage(browser.newPage());

// 创建上下文提供者
PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);

// 创建 Agent
Agent agent = new Agent(provider);
```

### Android 平台 API
提供 Android 自动化的专用接口，支持 ADB 和 Appium。

```java
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;

// 创建 Android 设备
AndroidDevice device = new AndroidDevice();
device.connect().join();

// 创建上下文提供者
AndroidUIContextProvider provider = new AndroidUIContextProvider(device);

// 创建 Agent
Agent agent = new Agent(provider);
```

## 🔧 配置 API

### AgentConfig
配置 Agent 的行为和性能参数。

```java
AgentConfig config = AgentConfig.builder()
    .defaultTimeout(Duration.ofSeconds(30))
    .actionTimeout(Duration.ofSeconds(10))
    .maxRetryCount(3)
    .enableCache(true)
    .enableDebugLog(true)
    .saveScreenshotsOnError(true)
    .build();
```

### AIModelConfig
配置 AI 模型的参数和连接信息。

```java
AIModelConfig config = AIModelConfig.builder()
    .provider("openai")
    .model("gpt-4-vision-preview")
    .apiKey("your-api-key")
    .baseUrl("https://api.openai.com/v1")
    .temperature(0.1)
    .maxTokens(1000)
    .build();
```

## 📚 使用示例

### 基础操作示例
```java
// 创建 Agent
Agent agent = new Agent(platformInterface);

// 执行操作
agent.ai_action("点击登录按钮").get();
agent.ai_action("输入用户名: testuser").get();
agent.ai_action("输入密码: testpass").get();
agent.ai_action("点击提交按钮").get();

// 验证结果
boolean success = agent.ai_assert("登录成功").get();
System.out.println("登录结果: " + success);
```

### 数据提取示例
```java
// 提取信息
Map<String, String> userInfo = agent.ai_extract(
    "提取用户名、邮箱和注册日期"
).get();

System.out.println("用户名: " + userInfo.get("username"));
System.out.println("邮箱: " + userInfo.get("email"));
System.out.println("注册日期: " + userInfo.get("registrationDate"));
```

### 元素定位示例
```java
// 定位元素
String loginButtonId = agent.ai_locate("登录按钮").get();
String usernameInputId = agent.ai_locate("用户名输入框").get();

// 使用定位到的元素
agent.clickElement(loginButtonId).get();
agent.inputText(usernameInputId, "testuser").get();
```

### 异步操作示例
```java
// 异步执行操作
CompletableFuture<Void> action1 = agent.ai_action("加载用户数据");
CompletableFuture<Void> action2 = agent.ai_action("加载系统配置");

// 等待所有操作完成
CompletableFuture<Void> allActions = CompletableFuture.allOf(action1, action2);
allActions.thenRun(() -> System.out.println("所有操作完成"))
        .exceptionally(e -> {
            System.err.println("操作失败: " + e.getMessage());
            return null;
        });
```

## 🔍 错误处理

### 异常类型
```java
try {
    agent.ai_action("执行操作").get();
} catch (ElementNotFoundException e) {
    // 元素未找到
    System.err.println("元素未找到: " + e.getMessage());
} catch (ActionExecutionException e) {
    // 操作执行失败
    System.err.println("操作失败: " + e.getMessage());
} catch (AIModelException e) {
    // AI 模型错误
    System.err.println("AI 模型错误: " + e.getMessage());
} catch (MidsceneException e) {
    // 通用错误
    System.err.println("通用错误: " + e.getMessage());
}
```

### 错误恢复
```java
// 配置错误恢复
AgentConfig config = AgentConfig.builder()
    .enableErrorRecovery(true)
    .recoveryStrategies(Arrays.asList(
        RecoveryStrategy.REFRESH_PAGE,
        RecoveryStrategy.WAIT_AND_RETRY,
        RecoveryStrategy.ALTERNATIVE_ACTION
    ))
    .build();

Agent agent = new Agent(platformInterface, config);
```

## 📊 性能监控

### 获取性能指标
```java
// 启用性能监控
AgentConfig config = AgentConfig.builder()
    .enablePerformanceMetrics(true)
    .build();

Agent agent = new Agent(platformInterface, config);

// 获取性能指标
PerformanceMetrics metrics = agent.getPerformanceMetrics();
System.out.println("平均响应时间: " + metrics.getAverageResponseTime());
System.out.println("成功率: " + metrics.getSuccessRate());
System.out.println("总操作数: " + metrics.getTotalOperations());
```

### 自定义指标收集器
```java
// 实现自定义指标收集器
public class CustomMetricsCollector implements MetricsCollector {
    @Override
    public void recordAction(String actionName, Duration duration, boolean success) {
        // 记录操作指标
        System.out.println("操作: " + actionName + ", 耗时: " + duration + ", 成功: " + success);
    }
    
    // 其他方法实现...
}

// 使用自定义收集器
AgentConfig config = AgentConfig.builder()
    .enablePerformanceMetrics(true)
    .metricsCollector(new CustomMetricsCollector())
    .build();
```

## 🔧 高级功能

### 批量操作
```java
// 创建批量请求
BatchRequest batch = agent.createBatch();
batch.addAction("输入用户名");
batch.addAction("输入密码");
batch.addAction("点击登录按钮");

// 执行批量请求
CompletableFuture<BatchResult> result = batch.execute();
result.thenAccept(batchResult -> {
    System.out.println("批量操作完成，成功: " + batchResult.getSuccessCount());
});
```

### 条件操作
```java
// 条件执行
agent.ai_action("如果页面显示登录错误，则点击忘记密码链接").get();

// 循环执行
agent.ai_action("重复点击加载更多按钮，直到没有更多内容").get();
```

### 自定义操作策略
```java
// 注册自定义策略
agent.registerActionStrategy("customLogin", context -> {
    String username = context.getParam("username");
    String password = context.getParam("password");
    
    agent.ai_action("输入用户名: " + username).get();
    agent.ai_action("输入密码: " + password).get();
    agent.ai_action("点击登录按钮").get();
    
    return agent.ai_assert("登录成功").get();
});

// 使用自定义策略
agent.executeStrategy("customLogin", Map.of(
    "username", "testuser",
    "password", "testpass"
));
```

## 📖 更多资源

- [Agent API 详细文档](agent-api.md)
- [Web 平台 API 详细文档](web平台-api.md)
- [Android 平台 API 详细文档](android平台-api.md)
- [AI 模型 API 详细文档](ai模型-api.md)
- [配置 API 详细文档](配置-api.md)
- [示例代码](../examples/README.md)

---

通过这些 API，您可以构建强大、灵活的自动化解决方案，满足各种复杂的业务需求。建议根据具体场景选择合适的 API，并参考详细文档了解使用方法和最佳实践。