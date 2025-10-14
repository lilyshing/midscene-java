# Midscene Java 示例

本目录包含了使用 Midscene Java 框架进行自动化操作的示例代码。这些示例展示了如何使用 AI 驱动的方法进行 Web、Android 和 iOS 自动化。

## 示例列表

1. **BasicWebAutomationExample** - 基本的 Web 自动化示例
2. **AdvancedWebAutomationExample** - 高级的 Web 自动化示例
3. **AndroidAutomationExample** - Android 设备自动化示例
4. **IOSAutomationExample** - iOS 设备自动化示例
5. **CombinedAutomationExample** - 综合自动化示例，同时使用 Web、Android 和 iOS

## 前置条件

1. Java 17 或更高版本
2. Maven 3.6 或更高版本
3. 对于 Web 自动化示例，需要安装 Chromium 浏览器
4. 对于 Android 自动化示例，需要：
   - Android SDK
   - 启用了开发者选项和 USB 调试的 Android 设备或模拟器
5. 对于 iOS 自动化示例，需要：
   - Xcode（在 macOS 上）
   - 安装了 Apple Configurator 2
   - 已配置开发者账户的 iOS 设备

## 构建和运行

### 构建项目

```bash
mvn clean compile
```

### 打包项目

```bash
mvn package
```

### 运行示例

#### 基本Web自动化示例

```bash
java -cp target/midscene-examples-0.1.1-jar-with-dependencies.jar com.midscene.examples.BasicWebAutomationExample
```

#### 高级Web自动化示例

```bash
java -cp target/midscene-examples-0.1.1-jar-with-dependencies.jar com.midscene.examples.AdvancedWebAutomationExample
```

#### Android自动化示例

```bash
java -cp target/midscene-examples-0.1.1-jar-with-dependencies.jar com.midscene.examples.AndroidAutomationExample
```

#### iOS自动化示例

```bash
java -cp target/midscene-examples-0.1.1-jar-with-dependencies.jar com.midscene.examples.IOSAutomationExample
```

#### 综合自动化示例

```bash
java -cp target/midscene-examples-0.1.1-jar-with-dependencies.jar com.midscene.examples.CombinedAutomationExample
```

## 示例说明

### BasicWebAutomationExample

这个示例展示了最基本的 Web 自动化流程：
1. 初始化 Playwright 页面
2. 导航到指定网站
3. 创建 Agent 实例
4. 执行简单的 AI 驱动操作（如点击链接）
5. 清理资源

### AdvancedWebAutomationExample

这个示例展示了更高级的 Web 自动化功能：
1. 使用 AgentOptions 配置高级选项（超时、重试、缓存等）
2. 执行复杂的 AI 驱动操作（搜索、提取信息、验证页面状态）
3. 使用页面状态冻结功能进行批量操作

### AndroidAutomationExample

这个示例展示了 Android 设备自动化：
1. 连接到 Android 设备
2. 启动应用程序
3. 执行 AI 驱动的设备操作（点击、滑动、提取信息）
4. 清理资源

### IOSAutomationExample

这个示例展示了 iOS 设备自动化：
1. 连接到 iOS 设备
2. 初始化 iOS 平台接口
3. 执行 AI 驱动的设备操作（点击、滑动、提取信息）
4. 监控设备状态并获取截图
5. 清理资源

### CombinedAutomationExample

这个示例展示了如何在一个应用中同时使用 Web、Android 和 iOS 自动化：
1. 同时初始化 Web、Android 和 iOS 自动化环境
2. 在不同平台上执行操作
3. 实现跨平台协调操作
4. 管理多平台资源和状态

## 注意事项

1. 首次运行时，Playwright 会自动下载必要的浏览器二进制文件
2. Android 自动化需要正确配置 ADB 和设备连接
3. iOS 自动化需要正确配置 Xcode 和设备权限
4. 示例中的 AI 操作可能需要一些时间来完成，具体取决于网络环境和模型响应速度
5. 如果遇到超时问题，可以调整 AgentOptions 中的超时设置

## 故障排除

### Playwright 相关问题

如果遇到 Playwright 相关问题，可以尝试：

```bash
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.Install"
```

### Android 连接问题

如果遇到 Android 连接问题，请检查：

1. 设备是否已启用 USB 调试
2. 是否已通过 `adb devices` 命令看到设备
3. 是否已授予设备调试授权

## 更多信息

更多关于 Midscene Java 框架的信息，请参考项目文档。