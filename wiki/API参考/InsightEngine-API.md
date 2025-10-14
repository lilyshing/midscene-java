# InsightEngine API 文档

## 接口概述

`InsightEngine`是Midscene Java中的洞察引擎接口，负责分析UI上下文、识别元素、理解用户意图并生成操作计划，是AI驱动自动化操作的核心组件。

**全限定类名**：`com.midscene.core.service.InsightEngine`

## 核心方法

### 1. UI上下文分析

```java
CompletableFuture<Map<String, Object>> analyzeUiContext(UiContext uiContext)
```

**功能**：分析UI上下文，识别元素和理解页面结构

**参数**：
- `uiContext`：UI上下文对象，包含页面截图、DOM结构等信息

**返回值**：`CompletableFuture<Map<String, Object>>` - 包含分析结果的Map

**使用示例**：
```java
UiContext uiContext = platform.getUiContext();
Map<String, Object> analysis = insightEngine.analyzeUiContext(uiContext).join();

// 获取页面结构信息
List<UiElement> elements = (List<UiElement>) analysis.get("elements");
String pageType = (String) analysis.get("pageType");

System.out.println("页面类型: " + pageType);
System.out.println("识别到的元素数量: " + elements.size());
```

### 2. 根据描述查找元素

```java
CompletableFuture<List<UiElement>> findElementsByDescription(UiContext uiContext, String description)
```

**功能**：根据自然语言描述在UI上下文中查找匹配的元素

**参数**：
- `uiContext`：UI上下文对象
- `description`：元素的自然语言描述

**返回值**：`CompletableFuture<List<UiElement>>` - 匹配的UI元素列表

**使用示例**：
```java
UiContext uiContext = platform.getUiContext();
List<UiElement> elements = insightEngine
    .findElementsByDescription(uiContext, "登录按钮")
    .join();

if (!elements.isEmpty()) {
    UiElement loginButton = elements.get(0);
    System.out.println("找到登录按钮: " + loginButton.getText());
    // 可以使用找到的元素执行操作
}
```

### 3. 理解用户意图

```java
CompletableFuture<Map<String, Object>> understandIntent(String instruction, Map<String, Object> context)
```

**功能**：分析用户指令，理解用户意图

**参数**：
- `instruction`：用户的自然语言指令
- `context`：额外的上下文信息

**返回值**：`CompletableFuture<Map<String, Object>>` - 包含理解结果的Map

**使用示例**：
```java
String instruction = "点击右上角的设置按钮并修改个人信息";
Map<String, Object> context = new HashMap<>();
context.put("currentPage", "profile");
context.put("userRole", "admin");

Map<String, Object> intent = insightEngine.understandIntent(instruction, context).join();
String actionType = (String) intent.get("actionType");
List<String> targets = (List<String>) intent.get("targets");

System.out.println("动作类型: " + actionType);
System.out.println("目标元素: " + targets);
```

### 4. 生成操作计划

```java
CompletableFuture<List<Map<String, Object>>> generateActionPlan(Map<String, Object> intent, UiContext uiContext)
```

**功能**：基于用户意图和UI上下文生成详细的操作计划

**参数**：
- `intent`：用户意图信息
- `uiContext`：当前UI上下文

**返回值**：`CompletableFuture<List<Map<String, Object>>>` - 操作步骤列表

**使用示例**：
```java
// 先理解意图
Map<String, Object> intent = insightEngine.understandIntent(instruction, context).join();

// 然后生成操作计划
List<Map<String, Object>> actionPlan = insightEngine
    .generateActionPlan(intent, platform.getUiContext())
    .join();

// 执行操作计划
for (Map<String, Object> step : actionPlan) {
    String action = (String) step.get("action");
    UiElement target = (UiElement) step.get("target");
    
    System.out.println("执行步骤: " + action + " - " + target.getText());
    // 执行具体操作
}
```

### 5. 初始化引擎

```java
void initialize(Map<String, Object> config)
```

**功能**：初始化洞察引擎

**参数**：
- `config`：配置信息

**使用示例**：
```java
Map<String, Object> config = new HashMap<>();
config.put("modelName", "gpt-4-vision");
config.put("confidenceThreshold", 0.8);
config.put("maxElements", 500);

insightEngine.initialize(config);
```

### 6. 关闭引擎

```java
void shutdown()
```

**功能**：关闭洞察引擎并释放资源

**参数**：无

**使用示例**：
```java
insightEngine.shutdown();
```

## 关键数据结构

### UiContext

UI上下文对象，包含页面的完整信息：

- `screenshotBase64`：页面截图的Base64编码
- `domStructure`：DOM或视图层次结构
- `currentUrl`：当前URL（Web平台）
- `packageName`：应用包名（移动平台）
- `timestamp`：捕获时间戳

### UiElement

UI元素对象，代表页面中的一个可交互或显示元素：

- `id`：元素唯一标识符
- `text`：元素文本内容
- `type`：元素类型（button、input、div等）
- `bounds`：元素边界坐标
- `attributes`：元素属性映射
- `enabled`：元素是否可用
- `visible`：元素是否可见

## 分析结果结构

`analyzeUiContext`方法返回的Map包含以下主要字段：

| 字段名 | 类型 | 描述 |
|-------|------|------|
| `elements` | List\<UiElement\> | 识别到的UI元素列表 |
| `pageType` | String | 页面类型识别结果 |
| `pageStructure` | Map\<String, Object\> | 页面结构分析 |
| `semanticGroups` | List\<Map\<String, Object\>\> | 语义相关的元素组 |
| `confidence` | Double | 分析置信度 |
| `version` | String | 分析器版本 |

## 意图理解结果结构

`understandIntent`方法返回的Map包含以下主要字段：

| 字段名 | 类型 | 描述 |
|-------|------|------|
| `actionType` | String | 动作类型（click、input、scroll等） |
| `targets` | List\<String\> | 目标元素描述列表 |
| `parameters` | Map\<String, Object\> | 动作参数 |
| `confidence` | Double | 理解置信度 |
| `contextRelevance` | Double | 上下文相关性 |

## 操作计划结构

`generateActionPlan`方法返回的每个步骤Map包含以下字段：

| 字段名 | 类型 | 描述 |
|-------|------|------|
| `stepId` | String | 步骤唯一标识 |
| `action` | String | 动作名称 |
| `target` | UiElement | 目标元素 |
| `parameters` | Map\<String, Object\> | 动作参数 |
| `priority` | Integer | 执行优先级 |
| `description` | String | 步骤描述 |
| `prerequisites` | List\<String\> | 前置步骤ID列表 |

## 配置选项

初始化时可以提供的配置选项：

| 配置项 | 类型 | 描述 | 默认值 |
|-------|------|------|-------|
| `modelName` | String | 使用的AI模型名称 | "gpt-4-vision" |
| `confidenceThreshold` | Double | 元素匹配的置信度阈值 | 0.7 |
| `maxElements` | Integer | 最大处理元素数量 | 300 |
| `cacheEnabled` | Boolean | 是否启用分析结果缓存 | true |
| `timeout` | Integer | 操作超时时间(ms) | 10000 |
| `debugMode` | Boolean | 是否启用调试模式 | false |
| `customRules` | List\<Map\> | 自定义识别规则 | 空列表 |

## 异步操作模式

大部分方法都返回`CompletableFuture`，支持灵活的异步操作：

```java
// 异步链式操作示例
insightEngine.analyzeUiContext(uiContext)
    .thenCompose(analysis -> {
        String pageType = (String) analysis.get("pageType");
        System.out.println("页面类型: " + pageType);
        return insightEngine.findElementsByDescription(uiContext, "登录按钮");
    })
    .thenAccept(elements -> {
        if (!elements.isEmpty()) {
            System.out.println("找到登录按钮: " + elements.get(0).getText());
        } else {
            System.out.println("未找到登录按钮");
        }
    })
    .exceptionally(ex -> {
        System.err.println("UI分析失败: " + ex.getMessage());
        return null;
    });
```

## 实现类

主要的实现类包括：

- `DefaultInsightEngine` - 默认实现
- `MockInsightEngine` - 用于测试的模拟实现
- `EnhancedInsightEngine` - 增强版实现，支持更复杂的UI分析

## 错误处理

常见的异常类型：

- `InsightException` - 洞察引擎异常
- `ElementNotFoundException` - 元素未找到异常
- `TimeoutException` - 操作超时异常
- `InvalidUiContextException` - 无效的UI上下文异常

**异常处理示例**：
```java
try {
    List<UiElement> elements = insightEngine
        .findElementsByDescription(uiContext, "提交按钮")
        .orTimeout(5, TimeUnit.SECONDS)
        .join();
    
    // 处理结果
} catch (CompletionException e) {
    Throwable cause = e.getCause();
    if (cause instanceof TimeoutException) {
        System.err.println("元素查找超时");
    } else if (cause instanceof InvalidUiContextException) {
        System.err.println("无效的UI上下文");
    } else {
        System.err.println("查找失败: " + cause.getMessage());
    }
}
```

## 最佳实践

1. **重用UI上下文**：避免频繁获取UI上下文，减少性能开销
2. **缓存分析结果**：对静态或变化缓慢的UI，缓存分析结果
3. **优化元素描述**：提供精确、简洁的元素描述，提高匹配准确性
4. **批处理操作**：尽量批量执行元素查找，减少API调用
5. **资源管理**：在应用关闭时调用shutdown()释放资源
6. **渐进式增强**：先尝试精确匹配，失败时再进行模糊匹配
7. **监控与日志**：记录关键分析结果和性能指标

```java
// 优化的元素查找示例
public UiElement findElementWithFallback(UiContext uiContext, String exactDescription, String fallbackDescription) {
    List<UiElement> elements = insightEngine
        .findElementsByDescription(uiContext, exactDescription)
        .join();
    
    if (elements.isEmpty()) {
        // 尝试模糊匹配
        elements = insightEngine
            .findElementsByDescription(uiContext, fallbackDescription)
            .join();
    }
    
    return elements.isEmpty() ? null : elements.get(0);
}
```

## 与其他组件的集成

InsightEngine与其他核心组件密切配合：

1. **与Agent集成**：为Agent提供UI理解能力
2. **与AIModelService集成**：使用AI模型进行深度语义理解
3. **与TaskExecutor集成**：生成的操作计划由TaskExecutor执行

```java
// 完整流程示例
public CompletableFuture<TaskResult> executeWithInsight(String instruction) {
    // 1. 获取UI上下文
    return platform.getUiContextAsync()
        .thenCompose(uiContext -> {
            // 2. 理解用户意图
            return insightEngine.understandIntent(instruction, Collections.emptyMap())
                .thenCompose(intent -> {
                    // 3. 生成操作计划
                    return insightEngine.generateActionPlan(intent, uiContext)
                        .thenCompose(actionPlan -> {
                            // 4. 执行操作计划
                            return taskExecutor.executePlan(actionPlan, uiContext);
                        });
                });
        });
}