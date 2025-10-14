# Agent API 文档

## 类概述

`Agent`是Midscene Java中的核心抽象类，为各平台的AI驱动自动化操作提供基础框架。它定义了统一的AI交互接口，通过自然语言指令实现UI自动化操作。

**全限定类名**：`com.midscene.core.agent.Agent`

## 构造方法

```java
public Agent(PlatformInterface platform, AIModelService aiModelService,
             InsightEngine insightEngine, TaskExecutor taskExecutor)
```

**参数说明**：
- `platform`：平台接口，提供底层平台操作能力
- `aiModelService`：AI模型服务，负责AI模型的调用
- `insightEngine`：UI理解引擎，负责分析和理解UI
- `taskExecutor`：任务执行器，负责执行具体操作任务

## 核心方法

### 1. 初始化方法

```java
public CompletableFuture<Boolean> initialize(Map<String, Object> options)
```

**功能**：初始化Agent及其关联组件

**参数**：
- `options`：初始化选项，包含平台特定配置

**返回值**：`CompletableFuture<Boolean>` - 初始化成功返回true，失败返回false

**使用示例**：
```java
Map<String, Object> options = new HashMap<>();
options.put("browser", "chrome");
options.put("headless", false);
boolean initialized = agent.initialize(options).join();
```

### 2. AI通用操作

```java
public abstract CompletableFuture<TaskResult> aiAction(String instruction)
```

**功能**：执行基于自然语言指令的通用AI操作

**参数**：
- `instruction`：自然语言指令，描述要执行的操作

**返回值**：`CompletableFuture<TaskResult>` - 包含操作执行结果和状态信息

**使用示例**：
```java
TaskResult result = agent.aiAction("点击页面右上角的设置按钮并选择账号管理").join();
System.out.println("执行状态: " + result.getStatus());
System.out.println("执行结果: " + result.getResult());
```

### 3. AI点击操作

```java
public abstract CompletableFuture<TaskResult> aiTap(String targetDescription)
```

**功能**：基于目标描述执行AI驱动的点击操作

**参数**：
- `targetDescription`：目标元素的自然语言描述

**返回值**：`CompletableFuture<TaskResult>` - 点击操作的执行结果

**使用示例**：
```java
TaskResult result = agent.aiTap("登录按钮").join();
if (result.getStatus() == TaskStatus.SUCCESS) {
    System.out.println("点击成功");
}
```

### 4. AI输入操作

```java
public abstract CompletableFuture<TaskResult> aiInput(String targetDescription, String text)
```

**功能**：基于目标描述执行AI驱动的文本输入操作

**参数**：
- `targetDescription`：目标输入框的自然语言描述
- `text`：要输入的文本内容

**返回值**：`CompletableFuture<TaskResult>` - 输入操作的执行结果

**使用示例**：
```java
TaskResult result = agent.aiInput("用户名输入框", "testuser@example.com").join();
```

### 5. 数据提取操作

```java
public abstract CompletableFuture<String> extractData(String targetDescription)
```

**功能**：从UI中提取指定的数据

**参数**：
- `targetDescription`：目标数据区域的自然语言描述

**返回值**：`CompletableFuture<String>` - 提取的数据内容

**使用示例**：
```java
String productPrice = agent.extractData("产品价格标签").join();
System.out.println("提取的价格: " + productPrice);
```

### 6. 截图操作

```java
public CompletableFuture<String> takeScreenshot()
```

**功能**：捕获当前屏幕的截图

**参数**：无

**返回值**：`CompletableFuture<String>` - Base64编码的截图数据

**使用示例**：
```java
String screenshotBase64 = agent.takeScreenshot().join();
// 可以将截图保存到文件或用于分析
```

### 7. 获取平台接口

```java
public PlatformInterface getPlatform()
```

**功能**：获取底层平台接口实例

**参数**：无

**返回值**：`PlatformInterface` - 当前Agent使用的平台接口

**使用示例**：
```java
PlatformInterface platform = agent.getPlatform();
// 使用平台接口执行特定操作
```

### 8. 关闭方法

```java
public CompletableFuture<Void> close()
```

**功能**：关闭Agent及其关联资源

**参数**：无

**返回值**：`CompletableFuture<Void>` - 完成关闭操作的Future

**使用示例**：
```java
agent.close().join(); // 等待关闭完成
```

## 异步操作模式

所有方法都返回`CompletableFuture`，支持异步操作模式。您可以：

1. **直接阻塞等待**：使用`.join()`或`.get()`方法
2. **链式异步处理**：使用`.thenApply()`, `.thenAccept()`等方法
3. **组合多个异步操作**：使用`CompletableFuture.allOf()`等组合方法

**异步处理示例**：
```java
agent.aiTap("登录按钮")
     .thenCompose(result -> {
         if (result.getStatus() == TaskStatus.SUCCESS) {
             return agent.aiInput("用户名", "test@example.com");
         }
         return CompletableFuture.completedFuture(result);
     })
     .thenAccept(result -> {
         System.out.println("操作完成，状态: " + result.getStatus());
     })
     .exceptionally(ex -> {
         System.err.println("操作失败: " + ex.getMessage());
         return null;
     });
```

## 实现类

`Agent`是一个抽象类，需要通过具体平台的实现类来使用。主要实现包括：

- `WebAgent` - 用于Web平台的实现
- `AndroidAgent` - 用于Android平台的实现
- `IOSAgent` - 用于iOS平台的实现

## 错误处理

所有异步操作都可能抛出异常，请务必正确处理这些异常：

```java
try {
    TaskResult result = agent.aiAction("执行操作").join();
    // 处理结果
} catch (CompletionException e) {
    Throwable cause = e.getCause();
    System.err.println("操作失败: " + cause.getMessage());
}
```

## 最佳实践

1. **重用Agent实例**：避免频繁创建和销毁Agent实例
2. **合理管理异步操作**：使用异步链式调用而非阻塞等待
3. **提供精确的描述**：为AI操作提供尽可能精确的目标描述
4. **始终关闭资源**：在不需要Agent时调用close()方法释放资源
5. **使用try-with-resources**（如果实现了AutoCloseable）

```java
// 使用try-with-resources模式（如果支持）
try (WebAgent agent = new WebAgent(...)) {
    agent.initialize(options).join();
    agent.aiAction("执行任务").join();
} // 自动调用close()
```