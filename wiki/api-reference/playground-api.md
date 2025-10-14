# Playground 模块 API 文档

## 模块概述

Playground 模块是 Midscene Java 的交互式执行环境模块，提供了代码执行、会话管理和 MCP 服务集成等功能。该模块允许用户在安全的沙箱环境中执行 Java 代码，并与 MCP（模型控制协议）服务进行交互。

## 核心类

### 1. Playground 类

**全限定类名**：`com.midscene.playground.Playground`

Playground 是模块的主要入口类，使用 Builder 模式提供灵活的配置选项，管理代码执行会话，并处理与 MCP 服务的交互。

#### 创建实例（Builder 模式）

```java
import com.midscene.playground.Playground;

// 使用 Builder 模式创建 Playground 实例
Playground playground = Playground.builder()
    .withMaxSessions(10)
    .withSessionTimeout(3600) // 秒
    .withMcpServiceUrl("http://localhost:8080/mcp")
    .withSecureMode(true)
    .build();
```

#### 主要方法

```java
// 创建新的代码执行会话
public String createSession()

// 关闭指定会话
public void closeSession(String sessionId)

// 获取会话
public PlaygroundSession getSession(String sessionId)

// 使用 MCP 服务发送提示
public CompletableFuture<String> sendPromptViaMcp(String prompt, Map<String, Object> parameters)

// 关闭 Playground 并释放所有资源
public void shutdown()
```

#### Builder 类方法

```java
// 设置最大会话数
public Builder withMaxSessions(int maxSessions)

// 设置会话超时时间（秒）
public Builder withSessionTimeout(int sessionTimeout)

// 设置 MCP 服务 URL
public Builder withMcpServiceUrl(String url)

// 设置安全模式
public Builder withSecureMode(boolean secureMode)

// 设置自定义配置
public Builder withConfig(Map<String, Object> config)

// 构建 Playground 实例
public Playground build()
```

### 2. PlaygroundSession 类

**全限定类名**：`com.midscene.playground.PlaygroundSession`

PlaygroundSession 管理单个代码执行会话，提供代码执行环境和资源管理功能。

#### 主要方法

```java
// 执行 Java 代码
public CompletableFuture<ExecutionResult> executeCode(String code)

// 执行带参数的 Java 代码
public CompletableFuture<ExecutionResult> executeCode(String code, Map<String, Object> parameters)

// 关闭会话并释放资源
public void close()

// 获取会话 ID
public String getSessionId()

// 获取会话创建时间
public long getCreationTime()

// 获取最后活动时间
public long getLastActivityTime()
```

### 3. ExecutionResult 类

**全限定类名**：`com.midscene.playground.ExecutionResult`

ExecutionResult 封装代码执行的结果信息，包括输出、错误和执行状态。

#### 主要方法和属性

```java
// 是否执行成功
public boolean isSuccess()

// 获取执行输出
public String getOutput()

// 获取执行错误
public String getError()

// 获取执行异常
public Exception getException()

// 获取执行时间（毫秒）
public long getExecutionTime()
```

### 4. PlaygroundException 类

**全限定类名**：`com.midscene.playground.PlaygroundException`

PlaygroundException 表示 Playground 模块中发生的错误，继承自 RuntimeException。

#### 构造方法

```java
// 带错误消息的构造方法
public PlaygroundException(String message)

// 带错误消息和原因的构造方法
public PlaygroundException(String message, Throwable cause)
```

## MCP 服务集成

Playground 模块通过以下方法与 MCP（模型控制协议）服务进行交互：

```java
// 使用 MCP 服务发送提示
public CompletableFuture<String> sendPromptViaMcp(String prompt, Map<String, Object> parameters)

// 获取 MCP 服务实例
public McpService getMcpService()
```

## 使用示例

### 基本使用

```java
// 创建 Playground 实例
Playground playground = Playground.builder()
    .withMaxSessions(5)
    .withSessionTimeout(1800)
    .build();

// 创建会话
String sessionId = playground.createSession();

// 获取会话并执行代码
PlaygroundSession session = playground.getSession(sessionId);
CompletableFuture<ExecutionResult> resultFuture = session.executeCode("System.out.println(\"Hello, Playground!\");");

// 处理执行结果
ExecutionResult result = resultFuture.join();
if (result.isSuccess()) {
    System.out.println("执行结果: " + result.getOutput());
} else {
    System.err.println("执行错误: " + result.getError());
}

// 使用完毕后关闭资源
session.close();
playground.shutdown();
```

### 与 MCP 服务交互

```java
// 创建带有 MCP 服务配置的 Playground 实例
Playground playground = Playground.builder()
    .withMcpServiceUrl("http://localhost:8080/mcp")
    .build();

// 准备提示和参数
String prompt = "分析这段代码并提供优化建议";
Map<String, Object> parameters = new HashMap<>();
parameters.put("code", "public void inefficientMethod() { for (int i = 0; i < 1000; i++) { /* 操作 */ } }" );
parameters.put("language", "java");

// 发送到 MCP 服务
CompletableFuture<String> mcpResultFuture = playground.sendPromptViaMcp(prompt, parameters);

// 处理 MCP 服务返回结果
String mcpResult = mcpResultFuture.join();
System.out.println("MCP 服务响应: " + mcpResult);
```

## 安全注意事项

- 在生产环境中，建议启用安全模式（`withSecureMode(true)`）
- 限制最大会话数和会话超时时间，防止资源泄漏
- 避免在 Playground 中执行未经验证的用户代码
- 监控并记录所有代码执行活动，便于安全审计

## 常见问题

### 1. 会话创建失败

**问题**：调用 `createSession()` 方法返回 null 或抛出异常

**可能原因**：
- 已达到最大会话数限制
- 系统资源不足
- 安全检查失败

**解决方案**：
- 增加最大会话数限制
- 关闭不需要的会话
- 检查系统资源状况

### 2. 代码执行超时

**问题**：代码执行时间过长或超时

**解决方案**：
- 优化执行的代码，减少计算复杂度
- 实现超时机制
- 增加系统资源分配

### 3. MCP 服务连接失败

**问题**：与 MCP 服务通信失败

**可能原因**：
- MCP 服务未启动
- 网络连接问题
- URL 配置错误

**解决方案**：
- 检查 MCP 服务状态
- 验证网络连接
- 确认 MCP 服务 URL 配置正确