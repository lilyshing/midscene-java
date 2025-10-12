# Midscene Java 项目结构对齐方案

## 1. 原项目结构分析

### 1.1 原项目目录结构

根据最新研究，原项目 `https://github.com/web-infra-dev/midscene` 的目录结构如下：<mcreference link="https://blog.csdn.net/gitblog_00539/article/details/147058904" index="1">1</mcreference>

```
midscene/
├── .github/                    # GitHub 相关配置文件
├── .vscode/                    # VS Code 编辑器配置
├── apps/                       # 应用程序代码
├── packages/                   # 核心包和模块
│   ├── midscene/              # 核心包
│   ├── midscene-web/          # Web 平台支持
│   ├── midscene-android/      # Android 平台支持
│   └── midscene-chrome-extension/ # Chrome 扩展
├── scripts/                    # 项目相关脚本
├── .gitignore
├── .npmrc
├── .prettierignore
├── .prettierrc
├── CONTRIBUTING.md
├── LICENSE
├── README.md
├── README.zh.md
├── biome.json
├── cspell.config.cjs
├── nx.json
├── package.json
├── pnpm-lock.yaml
└── pnpm-workspace.yaml
```

### 1.2 原项目核心功能

原项目是一个基于多模态大语言模型（MLLM）的AI驱动UI自动化测试框架，主要功能包括：<mcreference link="https://blog.csdn.net/angellee1988/article/details/151010929" index="2">2</mcreference>

1. **三大核心API**<mcreference link="https://blog.51cto.com/imaegoo/13858221" index="4">4</mcreference>
   - **Action (交互)**: 描述步骤并执行交互，如"点击登录按钮"
   - **Query (提取)**: 从UI中提取数据，返回JSON格式
   - **Assert (断言)**: 判断页面状态是否符合指定条件

2. **多模态模型支持**
   - GPT-4o
   - UI-TARS (字节跳动开源)
   - Qwen2.5-VL

3. **平台支持**
   - Web (通过Puppeteer/Playwright)
   - Android (通过ADB)
   - Chrome扩展

4. **集成方式**
   - Chrome扩展
   - YAML脚本
   - Puppeteer/Playwright集成
   - MCP协议

## 2. 当前Java项目结构分析

### 2.1 当前结构

```
midscene-java/
├── midscene-core/              # 核心模块
├── midscene-web/               # Web平台支持
├── midscene-android/           # Android平台支持
├── docs/                       # 文档
├── pom.xml                     # Maven配置
└── README.md
```

### 2.2 差异分析

1. **缺少应用层结构**: 当前项目缺少类似原项目的`apps/`目录，用于存放示例应用程序
2. **缺少工具脚本**: 没有`scripts/`目录用于存放构建和部署脚本
3. **缺少Chrome扩展支持**: 当前没有实现Chrome扩展的Java版本
4. **配置管理不完善**: 缺少类似原项目的配置文件结构
5. **文档结构不完整**: 缺少多语言支持和贡献指南

## 3. 项目结构对齐方案

### 3.1 目录结构调整

```
midscene-java/
├── .github/                    # GitHub 相关配置文件
│   ├── workflows/              # CI/CD 工作流
│   └── ISSUE_TEMPLATE/         # Issue 模板
├── .vscode/                    # VS Code 编辑器配置
├── apps/                       # 应用程序代码
│   ├── midscene-demo/          # 示例应用程序
│   ├── midscene-chrome-extension/ # Chrome扩展(Java后端)
│   └── midscene-mcp-server/    # MCP协议服务器
├── packages/                   # 核心包和模块
│   ├── midscene-core/          # 核心包
│   ├── midscene-web/           # Web 平台支持
│   ├── midscene-android/       # Android 平台支持
│   └── midscene-integrations/  # 第三方集成
├── scripts/                    # 项目相关脚本
│   ├── build/                  # 构建脚本
│   ├── deploy/                 # 部署脚本
│   └── test/                   # 测试脚本
├── docs/                       # 文档
│   ├── api/                    # API 文档
│   ├── guides/                 # 使用指南
│   └── examples/               # 示例代码
├── .gitignore
├── .editorconfig
├── CONTRIBUTING.md
├── LICENSE
├── README.md
├── README.zh.md
├── pom.xml                     # Maven 父项目配置
└── maven-settings.xml          # Maven 设置
```

### 3.2 模块职责重新定义

#### 3.2.1 midscene-core (核心包)

**职责**: 提供核心AI模型抽象、基础API和通用功能

**关键类**:
- `Agent`: 核心代理类，协调AI模型和平台交互
- `AIModelService`: AI模型服务抽象
- `InsightEngine`: UI理解引擎
- `TaskExecutor`: 任务执行器
- `PlatformInterface`: 平台接口抽象

**新增功能**:
- 多模态模型支持 (GPT-4o, UI-TARS, Qwen2.5-VL)
- 自然语言解析器
- UI元素语义理解
- 动作规划器

#### 3.2.2 midscene-web (Web平台支持)

**职责**: 提供Web平台特定的实现

**关键类**:
- `WebPlatform`: Web平台实现
- `PlaywrightPage`: Playwright页面封装
- `WebUIContextProvider`: Web UI上下文提供者
- `WebUIElement`: Web UI元素抽象

**新增功能**:
- Puppeteer集成
- Chrome DevTools协议支持
- 浏览器扩展通信接口

#### 3.2.3 midscene-android (Android平台支持)

**职责**: 提供Android平台特定的实现

**关键类**:
- `AndroidPlatform`: Android平台实现
- `AndroidDevice`: Android设备抽象
- `AndroidUIContextProvider`: Android UI上下文提供者
- `AndroidUIElement`: Android UI元素抽象

**新增功能**:
- ADB命令封装
- UIAutomator集成
- 设备管理器

#### 3.2.4 midscene-integrations (第三方集成)

**职责**: 提供与第三方工具的集成

**关键模块**:
- `YamlScriptIntegration`: YAML脚本支持
- `TestFrameworkIntegration`: 测试框架集成
- `CIIntegration`: CI/CD工具集成

### 3.3 apps目录新增应用

#### 3.3.1 midscene-demo (示例应用程序)

**功能**: 提供完整的使用示例，展示Midscene Java的各种功能

**包含示例**:
- Web自动化测试示例
- Android自动化测试示例
- 数据抓取示例
- 端到端测试示例

#### 3.3.2 midscene-chrome-extension (Chrome扩展)

**功能**: 提供Chrome扩展的Java后端，支持通过REST API与扩展通信

**架构**:
- Spring Boot REST API服务器
- WebSocket支持实时通信
- 与核心模块集成

#### 3.3.3 midscene-mcp-server (MCP协议服务器)

**功能**: 实现MCP协议服务器，使Midscene能力可以被任何兼容MCP的客户端调用

**特性**:
- MCP协议实现
- 工具注册和调用
- 资源管理

## 4. 功能对齐方案

### 4.1 核心API对齐

原项目提供三大核心API，Java版本需要实现相同的API：

#### 4.1.1 Action API (交互)

```java
// 原项目: agent.aiAction('点击登录按钮')
// Java版本:
agent.aiAction("点击登录按钮");
agent.aiAction("在搜索框中输入'Java'并点击搜索");
```

#### 4.1.2 Query API (提取)

```java
// 原项目: agent.aiQuery('string(), 所有商品名称')
// Java版本:
List<String> productNames = agent.aiQuery("string(), 所有商品名称");
Map<String, Object> productData = agent.aiQuery("object(), 商品名称和价格");
```

#### 4.1.3 Assert API (断言)

```java
// 原项目: agent.aiAssert('页面显示"登录成功"')
// Java版本:
agent.aiAssert("页面显示'登录成功'");
agent.aiAssert("总价为¥299");
```

### 4.2 模型支持对齐

原项目支持多种模型，Java版本需要提供相同的模型支持：

#### 4.2.1 GPT-4o集成

```java
AIModelConfig config = new GPT4oConfig.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.openai.com/v1")
    .build();

AIModelService modelService = new GPT4oModelService(config);
Agent agent = new Agent(modelService, platform);
```

#### 4.2.2 UI-TARS集成

```java
AIModelConfig config = new UITarsConfig.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://your-ui-tars-endpoint")
    .build();

AIModelService modelService = new UITarsModelService(config);
Agent agent = new Agent(modelService, platform);
```

#### 4.2.3 Qwen2.5-VL集成

```java
AIModelConfig config = new QwenConfig.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
    .model("qwen2.5-vl")
    .build();

AIModelService modelService = new QwenModelService(config);
Agent agent = new Agent(modelService, platform);
```

### 4.3 平台支持对齐

#### 4.3.1 Web平台

```java
// 使用Playwright
WebPlatform webPlatform = new PlaywrightPlatform.Builder()
    .headless(false)
    .browserType(BrowserType.CHROMIUM)
    .build();

Agent agent = new Agent(modelService, webPlatform);

// 导航到页面
agent.navigate("https://example.com");

// 执行操作
agent.aiAction("点击登录按钮");
agent.aiAction("输入用户名'admin'和密码'password'");
agent.aiAction("点击提交按钮");

// 提取数据
String welcomeMessage = agent.aiQuery("string(), 欢迎消息");

// 断言
agent.aiAssert("显示'登录成功'");
```

#### 4.3.2 Android平台

```java
// 使用ADB
AndroidPlatform androidPlatform = new AndroidPlatform.Builder()
    .deviceId("emulator-5554")
    .build();

Agent agent = new Agent(modelService, androidPlatform);

// 启动应用
agent.launchApp("com.example.app");

// 执行操作
agent.aiAction("点击登录按钮");
agent.aiAction("输入用户名'admin'和密码'password'");
agent.aiAction("点击提交按钮");

// 提取数据
String welcomeMessage = agent.aiQuery("string(), 欢迎消息");

// 断言
agent.aiAssert("显示'登录成功'");
```

### 4.4 集成方式对齐

#### 4.4.1 Chrome扩展

```java
// 启动Chrome扩展后端服务器
ChromeExtensionServer server = new ChromeExtensionServer.Builder()
    .port(8080)
    .agent(agent)
    .build();

server.start();
```

#### 4.4.2 YAML脚本

```yaml
# test.yaml
name: "登录测试"
steps:
  - action: "导航到登录页面"
    target: "https://example.com/login"
  - action: "输入用户名"
    target: "#username"
    value: "admin"
  - action: "输入密码"
    target: "#password"
    value: "password"
  - action: "点击登录按钮"
    target: "#login-button"
  - assert: "显示'登录成功'"
```

```java
// 执行YAML脚本
YamlScriptExecutor executor = new YamlScriptExecutor(agent);
TestResult result = executor.execute("test.yaml");
```

#### 4.4.3 MCP协议

```java
// 启动MCP服务器
MCPServer server = new MCPServer.Builder()
    .port(3000)
    .agent(agent)
    .build();

server.start();

// 客户端可以通过MCP协议调用Midscene能力
```

## 5. 实施计划

### 5.1 第一阶段：核心结构对齐 (2周)

1. 调整项目目录结构
2. 创建新的模块和包
3. 迁移现有代码到新结构
4. 更新构建配置

### 5.2 第二阶段：核心API实现 (3周)

1. 实现三大核心API (Action, Query, Assert)
2. 集成多模态模型支持
3. 实现自然语言解析
4. 完善平台抽象

### 5.3 第三阶段：平台支持完善 (3周)

1. 完善Web平台实现
2. 完善Android平台实现
3. 实现Chrome扩展后端
4. 实现MCP协议服务器

### 5.4 第四阶段：集成和示例 (2周)

1. 实现YAML脚本支持
2. 创建示例应用程序
3. 编写文档和教程
4. 性能优化和测试

## 6. 风险评估与缓解

### 6.1 技术风险

1. **模型API兼容性**: 不同模型的API可能存在差异
   - 缓解：设计统一的模型抽象层

2. **平台特定功能**: Web和Android平台可能有特定需求
   - 缓解：设计灵活的平台扩展机制

3. **性能问题**: AI模型调用可能较慢
   - 缓解：实现缓存和异步处理

### 6.2 项目风险

1. **范围蔓延**: 功能需求可能不断增加
   - 缓解：明确MVP范围，分阶段实施

2. **资源不足**: 开发资源可能不足以完成所有功能
   - 缓解：优先实现核心功能，非核心功能后续迭代

## 7. 成功指标

1. **功能对齐度**: Java版本实现原项目90%以上的核心功能
2. **API一致性**: Java API与原项目API在语义上保持一致
3. **性能指标**: 关键操作响应时间与原项目相当
4. **易用性**: 开发者能够快速上手使用Java版本
5. **文档完整性**: 提供与原项目同等质量的文档和示例

## 8. 总结

通过本方案，我们将确保Midscene Java项目与原项目保持一致的项目结构和功能实现。这将使Java版本能够充分利用原项目的生态和社区资源，同时为Java开发者提供与原项目相同的体验和能力。