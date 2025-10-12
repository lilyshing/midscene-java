# Midscene Java API 对齐方案

## 1. 原项目API分析

### 1.1 核心API概述

原项目 `midscene.js` 提供了三大核心API，用于UI自动化、数据提取和断言验证：<mcreference link="https://blog.51cto.com/imaegoo/13858221" index="4">4</mcreference>

1. **Action API (交互)**: 描述步骤并执行交互
2. **Query API (提取)**: 从UI中提取数据，返回JSON格式
3. **Assert API (断言)**: 判断页面状态是否符合指定条件

### 1.2 原项目API使用示例

```javascript
// 导入模块
const { agent } = require('@midscene/web/puppeteer');

// 初始化
await agent.launchBrowser();

// Action API - 交互
await agent.aiAction('点击登录按钮');
await agent.aiAction('在搜索框中输入"Java"并点击搜索');

// Query API - 提取
const productNames = await agent.aiQuery('string(), 所有商品名称');
const productData = await agent.aiQuery('object(), 商品名称和价格');

// Assert API - 断言
await agent.aiAssert('页面显示"登录成功"');
await agent.aiAssert('总价为¥299');
```

## 2. Java API设计原则

### 2.1 设计原则

1. **语义一致性**: Java API在语义上与原项目保持一致
2. **类型安全**: 利用Java的类型系统提供更好的安全性
3. **流畅性**: 提供流畅的API设计，提高可读性
4. **扩展性**: 设计可扩展的API，支持未来功能扩展
5. **异步支持**: 支持异步操作，适应UI自动化场景

### 2.2 命名约定

1. **方法名**: 使用与原项目一致的方法名，如`aiAction`, `aiQuery`, `aiAssert`
2. **参数名**: 使用有意义的参数名，遵循Java命名约定
3. **返回类型**: 使用强类型返回值，提供类型安全

## 3. 核心API设计

### 3.1 Agent类设计

```java
package com.midscene.core;

import com.midscene.core.ai.AIModelService;
import com.midscene.core.model.PlatformInterface;
import com.midscene.core.model.TaskResult;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Midscene Java核心代理类，提供与原项目一致的API
 */
public class Agent {
    private final AIModelService aiModelService;
    private final PlatformInterface platform;
    
    // 构造函数
    public Agent(AIModelService aiModelService, PlatformInterface platform);
    
    // 核心API方法
    public void aiAction(String instruction) throws MidsceneException;
    public <T> T aiQuery(String instruction, Class<T> resultType) throws MidsceneException;
    public void aiAssert(String assertion) throws MidsceneException;
    
    // 异步版本
    public CompletableFuture<Void> aiActionAsync(String instruction);
    public <T> CompletableFuture<T> aiQueryAsync(String instruction, Class<T> resultType);
    public CompletableFuture<Void> aiAssertAsync(String assertion);
    
    // 平台操作方法
    public void navigate(String url) throws MidsceneException;
    public void launchApp(String appId) throws MidsceneException;
    public void close() throws MidsceneException;
    
    // 上下文管理
    public void freezeContext();
    public void unfreezeContext();
    public void clearContext();
}
```

### 3.2 Action API实现

```java
package com.midscene.core.api;

import com.midscene.core.Agent;
import com.midscene.core.model.ActionRequest;
import com.midscene.core.model.ActionResult;

/**
 * Action API实现，用于执行UI交互操作
 */
public class ActionApi {
    private final Agent agent;
    
    public ActionApi(Agent agent) {
        this.agent = agent;
    }
    
    /**
     * 执行单个动作
     * @param instruction 自然语言指令，如"点击登录按钮"
     * @throws MidsceneException 执行失败时抛出异常
     */
    public void execute(String instruction) throws MidsceneException {
        ActionRequest request = new ActionRequest.Builder()
            .instruction(instruction)
            .build();
            
        ActionResult result = agent.executeAction(request);
        
        if (!result.isSuccess()) {
            throw new MidsceneException("Action failed: " + result.getErrorMessage());
        }
    }
    
    /**
     * 执行多个动作
     * @param instructions 指令列表
     * @throws MidsceneException 任何动作失败时抛出异常
     */
    public void executeBatch(List<String> instructions) throws MidsceneException {
        for (String instruction : instructions) {
            execute(instruction);
        }
    }
    
    /**
     * 执行动作并返回结果
     * @param instruction 自然语言指令
     * @return 动作执行结果
     */
    public ActionResult executeWithResult(String instruction) {
        ActionRequest request = new ActionRequest.Builder()
            .instruction(instruction)
            .returnResult(true)
            .build();
            
        return agent.executeAction(request);
    }
}
```

### 3.3 Query API实现

```java
package com.midscene.core.api;

import com.midscene.core.Agent;
import com.midscene.core.model.QueryRequest;
import com.midscene.core.model.QueryResult;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;

/**
 * Query API实现，用于从UI中提取数据
 */
public class QueryApi {
    private final Agent agent;
    
    public QueryApi(Agent agent) {
        this.agent = agent;
    }
    
    /**
     * 查询并返回字符串结果
     * @param instruction 查询指令，如"页面标题"
     * @return 查询结果字符串
     * @throws MidsceneException 查询失败时抛出异常
     */
    public String queryString(String instruction) throws MidsceneException {
        return query(instruction, String.class);
    }
    
    /**
     * 查询并返回字符串列表
     * @param instruction 查询指令，如"所有商品名称"
     * @return 字符串列表
     * @throws MidsceneException 查询失败时抛出异常
     */
    public List<String> queryStringList(String instruction) throws MidsceneException {
        return query(instruction, new TypeReference<List<String>>() {});
    }
    
    /**
     * 查询并返回对象
     * @param instruction 查询指令，如"商品名称和价格"
     * @return 查询结果对象
     * @throws MidsceneException 查询失败时抛出异常
     */
    public Map<String, Object> queryObject(String instruction) throws MidsceneException {
        return query(instruction, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * 查询并返回指定类型的对象
     * @param instruction 查询指令
     * @param resultType 结果类型
     * @return 查询结果
     * @throws MidsceneException 查询失败时抛出异常
     */
    public <T> T query(String instruction, Class<T> resultType) throws MidsceneException {
        QueryRequest request = new QueryRequest.Builder()
            .instruction(instruction)
            .resultType(resultType)
            .build();
            
        QueryResult<T> result = agent.executeQuery(request);
        
        if (!result.isSuccess()) {
            throw new MidsceneException("Query failed: " + result.getErrorMessage());
        }
        
        return result.getData();
    }
    
    /**
     * 查询并返回指定类型的对象（用于复杂类型）
     * @param instruction 查询指令
     * @param typeReference 类型引用
     * @return 查询结果
     * @throws MidsceneException 查询失败时抛出异常
     */
    public <T> T query(String instruction, TypeReference<T> typeReference) throws MidsceneException {
        QueryRequest request = new QueryRequest.Builder()
            .instruction(instruction)
            .typeReference(typeReference)
            .build();
            
        QueryResult<T> result = agent.executeQuery(request);
        
        if (!result.isSuccess()) {
            throw new MidsceneException("Query failed: " + result.getErrorMessage());
        }
        
        return result.getData();
    }
}
```

### 3.4 Assert API实现

```java
package com.midscene.core.api;

import com.midscene.core.Agent;
import com.midscene.core.model.AssertRequest;
import com.midscene.core.model.AssertResult;

/**
 * Assert API实现，用于断言验证
 */
public class AssertApi {
    private final Agent agent;
    
    public AssertApi(Agent agent) {
        this.agent = agent;
    }
    
    /**
     * 执行断言验证
     * @param assertion 断言指令，如"页面显示'登录成功'"
     * @throws MidsceneAssertionError 断言失败时抛出异常
     */
    public void assertCondition(String assertion) throws MidsceneAssertionError {
        AssertRequest request = new AssertRequest.Builder()
            .assertion(assertion)
            .build();
            
        AssertResult result = agent.executeAssert(request);
        
        if (!result.isSuccess()) {
            throw new MidsceneAssertionError("Assertion failed: " + result.getErrorMessage());
        }
    }
    
    /**
     * 执行断言验证并返回结果
     * @param assertion 断言指令
     * @return 断言结果
     */
    public AssertResult assertWithResult(String assertion) {
        AssertRequest request = new AssertRequest.Builder()
            .assertion(assertion)
            .build();
            
        return agent.executeAssert(request);
    }
    
    /**
     * 执行断言验证，失败时不抛出异常
     * @param assertion 断言指令
     * @return 断言是否成功
     */
    public boolean tryAssert(String assertion) {
        try {
            assertCondition(assertion);
            return true;
        } catch (MidsceneAssertionError e) {
            return false;
        }
    }
}
```

## 4. 平台特定API设计

### 4.1 Web平台API

```java
package com.midscene.web;

import com.midscene.core.Agent;
import com.midscene.core.PlatformInterface;
import com.midscene.web.model.BrowserConfig;
import com.midscene.web.model.Page;
import java.util.List;

/**
 * Web平台特定API
 */
public class WebAgent extends Agent {
    private final WebPlatform webPlatform;
    
    public WebAgent(AIModelService aiModelService, WebPlatform webPlatform) {
        super(aiModelService, webPlatform);
        this.webPlatform = webPlatform;
    }
    
    /**
     * 启动浏览器
     * @param config 浏览器配置
     */
    public void launchBrowser(BrowserConfig config) {
        webPlatform.launchBrowser(config);
    }
    
    /**
     * 导航到URL
     * @param url 目标URL
     */
    public void navigate(String url) {
        webPlatform.navigate(url);
    }
    
    /**
     * 获取当前页面
     * @return 当前页面对象
     */
    public Page getCurrentPage() {
        return webPlatform.getCurrentPage();
    }
    
    /**
     * 获取所有打开的页面
     * @return 页面列表
     */
    public List<Page> getAllPages() {
        return webPlatform.getAllPages();
    }
    
    /**
     * 切换到指定页面
     * @param page 目标页面
     */
    public void switchToPage(Page page) {
        webPlatform.switchToPage(page);
    }
    
    /**
     * 等待页面加载完成
     * @param timeout 超时时间（毫秒）
     */
    public void waitForPageLoad(long timeout) {
        webPlatform.waitForPageLoad(timeout);
    }
    
    /**
     * 截图
     * @return 截图字节数组
     */
    public byte[] takeScreenshot() {
        return webPlatform.takeScreenshot();
    }
    
    /**
     * 执行JavaScript
     * @param script JavaScript代码
     * @return 执行结果
     */
    public Object executeScript(String script) {
        return webPlatform.executeScript(script);
    }
}
```

### 4.2 Android平台API

```java
package com.midscene.android;

import com.midscene.core.Agent;
import com.midscene.core.PlatformInterface;
import com.midscene.android.model.DeviceConfig;
import com.midscene.android.model.AppInfo;
import java.util.List;

/**
 * Android平台特定API
 */
public class AndroidAgent extends Agent {
    private final AndroidPlatform androidPlatform;
    
    public AndroidAgent(AIModelService aiModelService, AndroidPlatform androidPlatform) {
        super(aiModelService, androidPlatform);
        this.androidPlatform = androidPlatform;
    }
    
    /**
     * 连接设备
     * @param config 设备配置
     */
    public void connectDevice(DeviceConfig config) {
        androidPlatform.connectDevice(config);
    }
    
    /**
     * 启动应用
     * @param appId 应用包名
     */
    public void launchApp(String appId) {
        androidPlatform.launchApp(appId);
    }
    
    /**
     * 获取已安装的应用列表
     * @return 应用列表
     */
    public List<AppInfo> getInstalledApps() {
        return androidPlatform.getInstalledApps();
    }
    
    /**
     * 获取当前应用信息
     * @return 当前应用信息
     */
    public AppInfo getCurrentApp() {
        return androidPlatform.getCurrentApp();
    }
    
    /**
     * 安装应用
     * @param apkPath APK文件路径
     */
    public void installApp(String apkPath) {
        androidPlatform.installApp(apkPath);
    }
    
    /**
     * 卸载应用
     * @param appId 应用包名
     */
    public void uninstallApp(String appId) {
        androidPlatform.uninstallApp(appId);
    }
    
    /**
     * 截图
     * @return 截图字节数组
     */
    public byte[] takeScreenshot() {
        return androidPlatform.takeScreenshot();
    }
    
    /**
     * 获取设备属性
     * @return 设备属性
     */
    public Map<String, String> getDeviceProperties() {
        return androidPlatform.getDeviceProperties();
    }
}
```

## 5. 流畅API设计

### 5.1 流畅API基础

```java
package com.midscene.core.fluent;

import com.midscene.core.Agent;
import com.midscene.core.api.ActionApi;
import com.midscene.core.api.QueryApi;
import com.midscene.core.api.AssertApi;

/**
 * 流畅API基础类
 */
public class FluentAgent {
    private final Agent agent;
    private final ActionApi actionApi;
    private final QueryApi queryApi;
    private final AssertApi assertApi;
    
    public FluentAgent(Agent agent) {
        this.agent = agent;
        this.actionApi = new ActionApi(agent);
        this.queryApi = new QueryApi(agent);
        this.assertApi = new AssertApi(agent);
    }
    
    /**
     * 创建流畅API实例
     * @param agent 代理实例
     * @return 流畅API实例
     */
    public static FluentAgent create(Agent agent) {
        return new FluentAgent(agent);
    }
    
    /**
     * 执行动作
     * @param instruction 动作指令
     * @return 当前实例，支持链式调用
     */
    public FluentAgent act(String instruction) {
        try {
            actionApi.execute(instruction);
        } catch (MidsceneException e) {
            throw new FluentActionException("Action failed: " + instruction, e);
        }
        return this;
    }
    
    /**
     * 查询数据
     * @param instruction 查询指令
     * @param resultType 结果类型
     * @param <T> 结果类型
     * @return 查询结果
     */
    public <T> T query(String instruction, Class<T> resultType) {
        try {
            return queryApi.query(instruction, resultType);
        } catch (MidsceneException e) {
            throw new FluentQueryException("Query failed: " + instruction, e);
        }
    }
    
    /**
     * 查询字符串数据
     * @param instruction 查询指令
     * @return 查询结果
     */
    public String query(String instruction) {
        return query(instruction, String.class);
    }
    
    /**
     * 断言验证
     * @param assertion 断言指令
     * @return 当前实例，支持链式调用
     */
    public FluentAgent assert(String assertion) {
        try {
            assertApi.assertCondition(assertion);
        } catch (MidsceneAssertionError e) {
            throw new FluentAssertException("Assertion failed: " + assertion, e);
        }
        return this;
    }
    
    /**
     * 等待
     * @param milliseconds 等待时间（毫秒）
     * @return 当前实例，支持链式调用
     */
    public FluentAgent wait(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FluentWaitException("Wait interrupted", e);
        }
        return this;
    }
    
    /**
     * 导航到URL
     * @param url 目标URL
     * @return 当前实例，支持链式调用
     */
    public FluentAgent navigate(String url) {
        try {
            agent.navigate(url);
        } catch (MidsceneException e) {
            throw new FluentNavigationException("Navigation failed: " + url, e);
        }
        return this;
    }
}
```

### 5.2 流畅API使用示例

```java
// 创建流畅API实例
FluentAgent fluent = FluentAgent.create(agent);

// 链式调用示例
fluent.navigate("https://example.com/login")
      .wait(1000)
      .act("点击登录按钮")
      .wait(500)
      .act("输入用户名'admin'")
      .act("输入密码'password'")
      .act("点击提交按钮")
      .wait(1000)
      .assert("显示'登录成功'");

// 查询示例
String welcomeMessage = fluent.query("欢迎消息");
List<String> productNames = fluent.query("所有商品名称", List.class);
Map<String, Object> productData = fluent.query("商品名称和价格", Map.class);
```

## 6. 异步API设计

### 6.1 异步API基础

```java
package com.midscene.core.async;

import com.midscene.core.Agent;
import com.midscene.core.api.ActionApi;
import com.midscene.core.api.QueryApi;
import com.midscene.core.api.AssertApi;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 异步API基础类
 */
public class AsyncAgent {
    private final Agent agent;
    private final ActionApi actionApi;
    private final QueryApi queryApi;
    private final AssertApi assertApi;
    private final Executor executor;
    
    public AsyncAgent(Agent agent, Executor executor) {
        this.agent = agent;
        this.actionApi = new ActionApi(agent);
        this.queryApi = new QueryApi(agent);
        this.assertApi = new AssertApi(agent);
        this.executor = executor;
    }
    
    /**
     * 创建异步API实例
     * @param agent 代理实例
     * @return 异步API实例
     */
    public static AsyncAgent create(Agent agent) {
        return new AsyncAgent(agent, CompletableFuture::new);
    }
    
    /**
     * 异步执行动作
     * @param instruction 动作指令
     * @return 异步结果
     */
    public CompletableFuture<Void> actAsync(String instruction) {
        return CompletableFuture.runAsync(() -> {
            try {
                actionApi.execute(instruction);
            } catch (MidsceneException e) {
                throw new AsyncActionException("Async action failed: " + instruction, e);
            }
        }, executor);
    }
    
    /**
     * 异步查询数据
     * @param instruction 查询指令
     * @param resultType 结果类型
     * @param <T> 结果类型
     * @return 异步查询结果
     */
    public <T> CompletableFuture<T> queryAsync(String instruction, Class<T> resultType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return queryApi.query(instruction, resultType);
            } catch (MidsceneException e) {
                throw new AsyncQueryException("Async query failed: " + instruction, e);
            }
        }, executor);
    }
    
    /**
     * 异步断言验证
     * @param assertion 断言指令
     * @return 异步结果
     */
    public CompletableFuture<Void> assertAsync(String assertion) {
        return CompletableFuture.runAsync(() -> {
            try {
                assertApi.assertCondition(assertion);
            } catch (MidsceneAssertionError e) {
                throw new AsyncAssertException("Async assertion failed: " + assertion, e);
            }
        }, executor);
    }
}
```

### 6.2 异步API使用示例

```java
// 创建异步API实例
AsyncAgent async = AsyncAgent.create(agent);

// 异步执行示例
CompletableFuture<Void> future = async.actAsync("点击登录按钮")
    .thenCompose(v -> async.actAsync("输入用户名'admin'"))
    .thenCompose(v -> async.actAsync("输入密码'password'"))
    .thenCompose(v -> async.actAsync("点击提交按钮"))
    .thenCompose(v -> async.assertAsync("显示'登录成功'"))
    .exceptionally(ex -> {
        System.err.println("操作失败: " + ex.getMessage());
        return null;
    });

// 异步查询示例
CompletableFuture<String> welcomeFuture = async.queryAsync("欢迎消息", String.class);
welcomeFuture.thenAccept(message -> System.out.println("欢迎消息: " + message));

// 等待所有异步操作完成
CompletableFuture.allOf(future, welcomeFuture).join();
```

## 7. 配置API设计

### 7.1 配置模型

```java
package com.midscene.core.config;

/**
 * Midscene配置类
 */
public class MidsceneConfig {
    private AIModelConfig aiModelConfig;
    private PlatformConfig platformConfig;
    private ExecutionConfig executionConfig;
    private ReportingConfig reportingConfig;
    
    // 构造函数、getter和setter方法
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private AIModelConfig aiModelConfig;
        private PlatformConfig platformConfig;
        private ExecutionConfig executionConfig = new ExecutionConfig();
        private ReportingConfig reportingConfig = new ReportingConfig();
        
        public Builder aiModelConfig(AIModelConfig aiModelConfig) {
            this.aiModelConfig = aiModelConfig;
            return this;
        }
        
        public Builder platformConfig(PlatformConfig platformConfig) {
            this.platformConfig = platformConfig;
            return this;
        }
        
        public Builder executionConfig(ExecutionConfig executionConfig) {
            this.executionConfig = executionConfig;
            return this;
        }
        
        public Builder reportingConfig(ReportingConfig reportingConfig) {
            this.reportingConfig = reportingConfig;
            return this;
        }
        
        public MidsceneConfig build() {
            MidsceneConfig config = new MidsceneConfig();
            config.aiModelConfig = this.aiModelConfig;
            config.platformConfig = this.platformConfig;
            config.executionConfig = this.executionConfig;
            config.reportingConfig = this.reportingConfig;
            return config;
        }
    }
}
```

### 7.2 配置API使用示例

```java
// 创建配置
MidsceneConfig config = new MidsceneConfig.Builder()
    .aiModelConfig(new GPT4oConfig.Builder()
        .apiKey("your-api-key")
        .baseUrl("https://api.openai.com/v1")
        .model("gpt-4o")
        .build())
    .platformConfig(new WebPlatformConfig.Builder()
        .browserType(BrowserType.CHROMIUM)
        .headless(false)
        .viewport(1280, 720)
        .build())
    .executionConfig(new ExecutionConfig.Builder()
        .timeout(30000)
        .retryCount(3)
        .build())
    .reportingConfig(new ReportingConfig.Builder()
        .enableHtmlReport(true)
        .enableScreenshot(true)
        .reportPath("./reports")
        .build())
    .build();

// 使用配置创建Agent
Agent agent = AgentFactory.create(config);
```

## 8. 实施计划

### 8.1 第一阶段：核心API实现 (2周)

1. 实现Agent类和基础API
2. 实现Action、Query、Assert三大核心API
3. 实现基础异常处理
4. 编写单元测试

### 8.2 第二阶段：平台特定API (2周)

1. 实现Web平台特定API
2. 实现Android平台特定API
3. 实现平台配置模型
4. 编写集成测试

### 8.3 第三阶段：高级API (2周)

1. 实现流畅API
2. 实现异步API
3. 实现配置API
4. 编写性能测试

### 8.4 第四阶段：文档和示例 (1周)

1. 编写API文档
2. 创建使用示例
3. 编写最佳实践指南
4. 完善代码注释

## 9. 总结

通过本方案，我们将确保Midscene Java版本的API与原项目保持一致，同时利用Java的类型安全和面向对象特性提供更好的开发体验。这将使Java开发者能够轻松上手使用Midscene Java，同时保持与原项目相同的语义和功能。