# Agent 核心控制器

Agent 是 Midscene Java 的核心控制器，负责协调各个组件，提供统一的 API 接口，实现 AI 驱动的自动化操作。

## 🎯 设计理念

Agent 的设计理念是简化自动化操作，将复杂的底层实现细节封装起来，提供直观、易用的自然语言 API。开发者只需描述想要执行的操作，Agent 会自动处理元素定位、操作执行和结果验证。

## 🏗️ 架构设计

### 核心组件
```
Agent
├── Insight (智能分析引擎)
├── AIModelService (AI 模型服务)
├── PlatformInterface (平台抽象层)
├── ContextManager (上下文管理)
├── ActionExecutor (动作执行器)
└── ResponseHandler (响应处理器)
```

### 类结构
```java
public class Agent {
    private final Insight insight;
    private final AIModelService aiModelService;
    private final PlatformInterface platformInterface;
    private final ContextManager contextManager;
    private final AgentConfig config;
    
    // 核心方法
    public CompletableFuture<Void> ai_action(String instruction);
    public CompletableFuture<String> ai_locate(String description);
    public CompletableFuture<Map<String, String>> ai_extract(String instruction);
    public CompletableFuture<Boolean> ai_assert(String assertion);
    // ... 其他方法
}
```

## 🔧 主要功能

### 1. ai_action - 执行操作
使用自然语言描述执行操作，如点击、输入、滚动等。

```java
// 基础用法
agent.ai_action("点击登录按钮");

// 复杂操作
agent.ai_action("在搜索框中输入'Java自动化'并按回车键");

// 条件操作
agent.ai_action("如果页面显示登录错误，则点击忘记密码链接");
```

**实现流程：**
1. 接收自然语言指令
2. 通过 Insight 分析指令意图
3. 调用 AI 模型生成操作策略
4. 通过 PlatformInterface 执行操作
5. 处理执行结果和异常

### 2. ai_locate - 定位元素
根据描述定位页面元素，返回元素标识符。

```java
// 简单定位
String elementId = agent.ai_locate("登录按钮").get();

// 复杂定位
String elementId = agent.ai_locate("用户名输入框，位于页面顶部").get();

// 多条件定位
String elementId = agent.ai_locate("蓝色的提交按钮，带有'确认'文字").get();
```

**实现流程：**
1. 解析元素描述
2. 结合当前页面上下文
3. 调用 AI 模型生成定位策略
4. 通过 PlatformInterface 执行定位
5. 返回元素标识符

### 3. ai_extract - 提取信息
从页面或应用中提取结构化信息。

```java
// 简单提取
Map<String, String> data = agent.ai_extract("提取用户名和邮箱地址").get();

// 复杂提取
Map<String, String> productInfo = agent.ai_extract(
    "提取商品列表中所有商品的名称、价格和评分，以JSON格式返回"
).get();

// 条件提取
Map<String, String> errorInfo = agent.ai_extract(
    "如果页面显示错误信息，提取错误代码和描述"
).get();
```

**实现流程：**
1. 解析提取需求
2. 分析页面结构
3. 调用 AI 模型识别目标信息
4. 通过 PlatformInterface 获取元素内容
5. 结构化返回提取结果

### 4. ai_assert - 验证断言
验证页面或应用状态是否符合预期。

```java
// 简单断言
boolean result = agent.ai_assert("页面显示'登录成功'消息").get();

// 复杂断言
boolean result = agent.ai_assert(
    "用户头像显示在右上角，且用户名与登录时输入的一致"
).get();

// 否定断言
boolean result = agent.ai_assert("页面不显示任何错误信息").get();
```

**实现流程：**
1. 解析断言条件
2. 获取当前页面状态
3. 调用 AI 模型进行状态比较
4. 返回验证结果

### 5. ai_query - 查询信息
查询页面或应用的特定信息，但不提取结构化数据。

```java
// 简单查询
String info = agent.ai_query("当前页面标题是什么？").get();

// 复杂查询
String info = agent.ai_query("描述当前页面的主要内容和布局").get();

// 状态查询
String info = agent.ai_query("登录按钮是否可用？").get();
```

### 6. ai_waitFor - 等待条件
等待特定条件满足后再继续执行。

```java
// 等待元素出现
agent.ai_waitFor("加载完成提示出现", Duration.ofSeconds(10));

// 等待条件满足
agent.ai_waitFor("页面标题包含'仪表盘'", Duration.ofSeconds(15));

// 等待元素消失
agent.ai_waitFor("加载动画消失", Duration.ofSeconds(5));
```

## ⚙️ 配置选项

### AgentConfig 类
```java
public class AgentConfig {
    // AI 模型配置
    private AIModelConfig aiModelConfig;
    
    // 超时设置
    private Duration defaultTimeout = Duration.ofSeconds(30);
    private Duration actionTimeout = Duration.ofSeconds(10);
    private Duration locateTimeout = Duration.ofSeconds(5);
    
    // 重试设置
    private int maxRetryCount = 3;
    private Duration retryInterval = Duration.ofSeconds(1);
    
    // 缓存设置
    private boolean enableCache = true;
    private Duration cacheExpiry = Duration.ofMinutes(5);
    
    // 调试设置
    private boolean enableDebugLog = false;
    private boolean saveScreenshotsOnError = true;
    private String screenshotPath = "./screenshots";
    
    // 性能设置
    private int maxConcurrentActions = 5;
    private boolean enablePerformanceMetrics = false;
}
```

### 配置示例
```java
// 基础配置
AgentConfig config = AgentConfig.builder()
    .defaultTimeout(Duration.ofSeconds(20))
    .maxRetryCount(2)
    .enableCache(true)
    .build();

// 高级配置
AIModelConfig aiConfig = AIModelConfig.builder()
    .provider("openai")
    .model("gpt-4-vision-preview")
    .temperature(0.1)
    .build();

AgentConfig config = AgentConfig.builder()
    .aiModelConfig(aiConfig)
    .defaultTimeout(Duration.ofSeconds(30))
    .actionTimeout(Duration.ofSeconds(15))
    .maxRetryCount(3)
    .enableCache(true)
    .cacheExpiry(Duration.ofMinutes(10))
    .enableDebugLog(true)
    .saveScreenshotsOnError(true)
    .maxConcurrentActions(3)
    .build();

Agent agent = new Agent(platformInterface, config);
```

## 🔄 生命周期管理

### 初始化
```java
// 创建 Agent 实例
Agent agent = new Agent(platformInterface);

// 带配置创建
Agent agent = new Agent(platformInterface, config);

// 异步初始化
CompletableFuture<Agent> futureAgent = Agent.createAsync(platformInterface, config);
```

### 执行操作
```java
// 同步执行
agent.ai_action("点击登录按钮").get();

// 异步执行
CompletableFuture<Void> future = agent.ai_action("点击登录按钮");
future.thenRun(() -> System.out.println("操作完成"))
      .exceptionally(e -> {
          System.err.println("操作失败: " + e.getMessage());
          return null;
      });

// 批量执行
List<CompletableFuture<Void>> futures = Arrays.asList(
    agent.ai_action("输入用户名"),
    agent.ai_action("输入密码"),
    agent.ai_action("点击登录按钮")
);

CompletableFuture<Void> allActions = CompletableFuture.allOf(
    futures.toArray(new CompletableFuture[0])
);
allActions.get();
```

### 资源清理
```java
// 使用 try-with-resources 自动清理
try (Agent agent = new Agent(platformInterface)) {
    // 执行操作
    agent.ai_action("执行一些操作").get();
} // 自动调用 close() 方法

// 手动清理
Agent agent = new Agent(platformInterface);
try {
    // 执行操作
} finally {
    agent.close();
}
```

## 🚀 高级特性

### 1. 上下文感知
Agent 能够维护操作上下文，使后续操作基于前序操作的结果。

```java
// 上下文传递示例
agent.ai_action("打开用户设置页面");
agent.ai_action("修改通知设置为关闭");  // 基于前一步的上下文
agent.ai_assert("通知开关处于关闭状态");
```

### 2. 自适应重试
当操作失败时，Agent 会自动分析失败原因并调整策略进行重试。

```java
// 配置自适应重试
AgentConfig config = AgentConfig.builder()
    .maxRetryCount(3)
    .enableAdaptiveRetry(true)
    .retryStrategy(RetryStrategy.INTELLIGENT)
    .build();
```

### 3. 并发操作
Agent 支持并发执行多个操作，提高执行效率。

```java
// 并发执行示例
CompletableFuture<Void> action1 = agent.ai_action("加载用户数据");
CompletableFuture<Void> action2 = agent.ai_action("加载系统配置");
CompletableFuture<Void> action3 = agent.ai_action("加载权限信息");

CompletableFuture<Void> allActions = CompletableFuture.allOf(action1, action2, action3);
allActions.get();
```

### 4. 操作冻结与解冻
Agent 支持冻结当前状态，执行特定操作后再解冻恢复。

```java
// 冻结状态
agent.freeze();

// 执行临时操作
agent.ai_action("打开弹窗查看详情").get();

// 解冻恢复
agent.unfreeze();
```

### 5. 自定义操作策略
开发者可以注册自定义操作策略，扩展 Agent 的功能。

```java
// 注册自定义策略
agent.registerActionStrategy("customLogin", context -> {
    // 自定义登录逻辑
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

## 📊 性能优化

### 1. 缓存机制
Agent 实现了多层缓存机制，减少重复计算和 AI 调用。

```java
// 配置缓存
AgentConfig config = AgentConfig.builder()
    .enableCache(true)
    .cacheExpiry(Duration.ofMinutes(10))
    .maxCacheSize(1000)
    .build();
```

### 2. 批量操作
将多个相关操作合并为批量请求，减少网络开销。

```java
// 批量操作示例
BatchRequest batch = agent.createBatch();
batch.addAction("输入用户名");
batch.addAction("输入密码");
batch.addAction("点击登录按钮");

CompletableFuture<BatchResult> result = batch.execute();
```

### 3. 预加载
预加载可能需要的资源，提高响应速度。

```java
// 预加载配置
AgentConfig config = AgentConfig.builder()
    .enablePreloading(true)
    .preloadResources(Arrays.asList("common-elements", "frequent-actions"))
    .build();
```

## 🐛 错误处理

### 1. 异常类型
```java
// 基础异常
public class MidsceneException extends Exception {
    private final ErrorCode errorCode;
    private final Map<String, Object> context;
}

// 特定异常
public class ActionExecutionException extends MidsceneException { }
public class ElementNotFoundException extends MidsceneException { }
public class AIModelException extends MidsceneException { }
public class PlatformException extends MidsceneException { }
```

### 2. 错误处理策略
```java
try {
    agent.ai_action("执行操作").get();
} catch (ElementNotFoundException e) {
    // 元素未找到处理
    System.err.println("元素未找到: " + e.getMessage());
} catch (ActionExecutionException e) {
    // 操作执行失败处理
    System.err.println("操作执行失败: " + e.getMessage());
} catch (AIModelException e) {
    // AI 模型错误处理
    System.err.println("AI 模型错误: " + e.getMessage());
} catch (MidsceneException e) {
    // 通用错误处理
    System.err.println("通用错误: " + e.getMessage());
}
```

### 3. 错误恢复
Agent 提供自动错误恢复机制，能够从常见错误中自动恢复。

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
```

## 📈 监控与日志

### 1. 性能监控
```java
// 启用性能监控
AgentConfig config = AgentConfig.builder()
    .enablePerformanceMetrics(true)
    .metricsCollector(new CustomMetricsCollector())
    .build();

// 获取性能指标
PerformanceMetrics metrics = agent.getPerformanceMetrics();
System.out.println("平均响应时间: " + metrics.getAverageResponseTime());
System.out.println("成功率: " + metrics.getSuccessRate());
```

### 2. 操作日志
```java
// 配置日志
AgentConfig config = AgentConfig.builder()
    .enableOperationLog(true)
    .logLevel(LogLevel.DEBUG)
    .logFormatter(new CustomLogFormatter())
    .build();
```

### 3. 调试信息
```java
// 启用调试模式
AgentConfig config = AgentConfig.builder()
    .enableDebugLog(true)
    .saveScreenshotsOnError(true)
    .debugScreenshotPath("./debug-screenshots")
    .build();
```

## 🎯 最佳实践

### 1. 指令设计
- 使用清晰、具体的自然语言描述
- 避免歧义和模糊的表达
- 将复杂操作分解为多个简单步骤

### 2. 错误处理
- 始终处理可能的异常情况
- 使用适当的超时设置
- 实现合理的重试机制

### 3. 性能优化
- 启用缓存减少重复计算
- 使用批量操作提高效率
- 合理设置并发级别

### 4. 资源管理
- 使用 try-with-resources 自动清理资源
- 及时释放不需要的资源
- 避免长时间持有 Agent 实例

---

Agent 是 Midscene Java 的核心组件，掌握其使用方法对于构建高效的自动化解决方案至关重要。通过合理配置和使用 Agent，您可以充分发挥 AI 驱动自动化的强大能力。