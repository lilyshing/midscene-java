# AIModelService API 文档

## 接口概述

`AIModelService`是Midscene Java中负责AI模型交互的核心接口，提供了文本生成、图像分析、对话交互等AI能力，是整个系统AI功能的基础。

**全限定类名**：`com.midscene.core.service.AIModelService`

## 核心方法

### 1. 文本生成

```java
CompletableFuture<String> generate(String prompt, Map<String, Object> options)
```

**功能**：根据给定的提示文本生成响应内容

**参数**：
- `prompt`：提示文本，指导AI生成内容
- `options`：生成选项，包含温度、最大长度等参数

**返回值**：`CompletableFuture<String>` - 生成的文本内容

**使用示例**：
```java
Map<String, Object> options = new HashMap<>();
options.put("temperature", 0.7);
options.put("maxLength", 500);

String result = aiModelService.generate("解释量子计算的基本原理", options).join();
System.out.println("生成结果: " + result);
```

### 2. 多轮对话

```java
CompletableFuture<String> chat(List<Map<String, String>> messages, Map<String, Object> options)
```

**功能**：进行多轮对话交互

**参数**：
- `messages`：对话消息列表，每个消息包含角色和内容
- `options`：生成选项

**返回值**：`CompletableFuture<String>` - 模型的回复内容

**使用示例**：
```java
List<Map<String, String>> messages = new ArrayList<>();

// 添加系统消息
Map<String, String> systemMsg = new HashMap<>();
systemMsg.put("role", "system");
systemMsg.put("content", "你是一个帮助用户的助手。");
messages.add(systemMsg);

// 添加用户消息
Map<String, String> userMsg = new HashMap<>();
userMsg.put("role", "user");
userMsg.put("content", "什么是AI？");
messages.add(userMsg);

String reply = aiModelService.chat(messages, Collections.emptyMap()).join();
System.out.println("AI回复: " + reply);
```

### 3. 图像分析

```java
CompletableFuture<Map<String, Object>> analyzeImage(String imageBase64, String prompt, Map<String, Object> options)
```

**功能**：分析给定的图像并返回分析结果

**参数**：
- `imageBase64`：Base64编码的图像数据
- `prompt`：分析提示，指定要提取的信息
- `options`：分析选项

**返回值**：`CompletableFuture<Map<String, Object>>` - 包含分析结果的Map

**使用示例**：
```java
String imageBase64 = "...";
String prompt = "描述这张图片中有什么物体";

Map<String, Object> result = aiModelService.analyzeImage(imageBase64, prompt, Collections.emptyMap()).join();
System.out.println("图像分析结果: " + result.get("description"));
```

### 4. 信息提取

```java
CompletableFuture<String> extractInformation(String context, String query)
```

**功能**：从给定的上下文中提取指定信息

**参数**：
- `context`：上下文文本
- `query`：查询内容，指定要提取的信息

**返回值**：`CompletableFuture<String>` - 提取的信息

**使用示例**：
```java
String context = "张三，男，35岁，高级工程师，联系方式：13800138000，邮箱：zhangsan@example.com";
String phoneNumber = aiModelService.extractInformation(context, "提取联系电话").join();
System.out.println("电话号码: " + phoneNumber);
```

### 5. 初始化服务

```java
void initialize(Map<String, Object> config)
```

**功能**：初始化AI模型服务

**参数**：
- `config`：配置信息，包含API密钥、模型名称等

**使用示例**：
```java
Map<String, Object> config = new HashMap<>();
config.put("apiKey", "your_api_key");
config.put("modelName", "gpt-4-turbo");
config.put("timeout", 30000);

aiModelService.initialize(config);
```

### 6. 关闭服务

```java
void shutdown()
```

**功能**：关闭AI模型服务并释放资源

**参数**：无

**使用示例**：
```java
aiModelService.shutdown();
```

## 缓存管理方法

AIModelService提供了一套完整的缓存管理机制，可以缓存各种AI操作的结果以提高性能：

### 1. 获取缓存的AI动作结果

```java
AIActionResult getCachedResult(AIActionRequest request)
```

**功能**：获取指定AI动作请求的缓存结果

**参数**：
- `request`：AI动作请求

**返回值**：缓存的结果，如果没有则返回null

### 2. 缓存AI动作结果

```java
void cacheResult(AIActionRequest request, AIActionResult result)
```

**功能**：缓存AI动作请求的结果

**参数**：
- `request`：AI动作请求
- `result`：AI动作结果

### 3. 获取缓存的AI点击结果

```java
AITapResult getCachedResult(AITapRequest request)
```

**功能**：获取指定AI点击请求的缓存结果

**参数**：
- `request`：AI点击请求

**返回值**：缓存的结果，如果没有则返回null

### 4. 缓存AI点击结果

```java
void cacheResult(AITapRequest request, AITapResult result)
```

**功能**：缓存AI点击请求的结果

**参数**：
- `request`：AI点击请求
- `result`：AI点击结果

### 5. 获取缓存的AI输入结果

```java
AIInputResult getCachedResult(AIInputRequest request)
```

**功能**：获取指定AI输入请求的缓存结果

**参数**：
- `request`：AI输入请求

**返回值**：缓存的结果，如果没有则返回null

### 6. 缓存AI输入结果

```java
void cacheResult(AIInputRequest request, AIInputResult result)
```

**功能**：缓存AI输入请求的结果

**参数**：
- `request`：AI输入请求
- `result`：AI输入结果

### 7. 获取缓存的数据提取结果

```java
ExtractDataResult getCachedResult(ExtractDataRequest request)
```

**功能**：获取指定数据提取请求的缓存结果

**参数**：
- `request`：数据提取请求

**返回值**：缓存的结果，如果没有则返回null

### 8. 缓存数据提取结果

```java
void cacheResult(ExtractDataRequest request, ExtractDataResult result)
```

**功能**：缓存数据提取请求的结果

**参数**：
- `request`：数据提取请求
- `result`：数据提取结果

### 9. 获取缓存统计信息

```java
String getCacheStatistics()
```

**功能**：获取缓存系统的统计信息

**参数**：无

**返回值**：缓存统计信息字符串

**使用示例**：
```java
String stats = aiModelService.getCacheStatistics();
System.out.println("缓存统计: " + stats);
```

## 异步操作模式

大多数核心方法都返回`CompletableFuture`，支持异步操作模式：

1. **直接阻塞等待**：使用`.join()`方法
2. **异步链式处理**：使用`.thenApply()`, `.thenCompose()`等方法
3. **错误处理**：使用`.exceptionally()`方法处理异常

**异步组合示例**：
```java
// 先分析图像，然后基于分析结果生成文本
aiModelService.analyzeImage(imageBase64, "分析这张图片", Collections.emptyMap())
    .thenCompose(analysis -> {
        String description = (String) analysis.get("description");
        return aiModelService.generate(
            "基于图片描述生成一篇简短文章: " + description,
            Collections.emptyMap()
        );
    })
    .thenAccept(article -> {
        System.out.println("生成的文章: " + article);
    })
    .exceptionally(ex -> {
        System.err.println("操作失败: " + ex.getMessage());
        return null;
    });
```

## 实现类

主要的实现类包括：

- `DefaultAIModelService` - 默认实现
- `MockAIModelService` - 用于测试的模拟实现
- `CachedAIModelService` - 带有缓存功能的装饰器实现

## 配置选项

初始化时可以提供的常见配置选项：

| 配置项 | 类型 | 描述 | 默认值 |
|-------|------|------|-------|
| `apiKey` | String | AI服务API密钥 | 必填 |
| `modelName` | String | 使用的模型名称 | "gpt-4-turbo" |
| `temperature` | Double | 生成温度 (0-2) | 0.7 |
| `maxLength` | Integer | 最大输出长度 | 1000 |
| `timeout` | Integer | 请求超时时间(ms) | 30000 |
| `cacheEnabled` | Boolean | 是否启用缓存 | true |
| `cacheSize` | Integer | 缓存大小 | 1000 |
| `cacheTTL` | Integer | 缓存过期时间(ms) | 3600000 |

## 错误处理

异步操作可能抛出的异常：

- `AIModelException` - AI模型服务异常
- `TimeoutException` - 请求超时异常
- `IllegalArgumentException` - 参数非法异常
- `IOException` - IO异常

**异常处理示例**：
```java
try {
    String result = aiModelService.generate("生成内容", Collections.emptyMap())
        .orTimeout(30, TimeUnit.SECONDS)
        .join();
} catch (CompletionException e) {
    Throwable cause = e.getCause();
    if (cause instanceof TimeoutException) {
        System.err.println("请求超时");
    } else if (cause instanceof AIModelException) {
        System.err.println("AI模型错误: " + cause.getMessage());
    } else {
        System.err.println("未知错误: " + cause.getMessage());
    }
}
```

## 最佳实践

1. **重用服务实例**：避免频繁创建和销毁AIModelService实例
2. **合理配置缓存**：根据使用场景配置适当的缓存大小和TTL
3. **实现降级策略**：当AI服务不可用时实现降级方案
4. **添加重试机制**：对重要操作实现指数退避重试
5. **监控与日志**：记录关键操作和性能指标
6. **资源管理**：在应用关闭时调用shutdown()释放资源

```java
// 应用关闭钩子
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    if (aiModelService != null) {
        aiModelService.shutdown();
        System.out.println("AI模型服务已关闭");
    }
}));
```