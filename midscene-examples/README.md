# Midscene Java 示例代码

本目录包含了 Midscene Java 框架的各种使用示例，展示了 AI 驱动的 UI 自动化测试的强大功能。

## 示例结构

示例代码按照功能模块组织，每个模块演示了 Midscene 的不同特性：

- **基础示例**：展示 Midscene 的核心功能和基本使用方法
- **Web自动化示例**：演示浏览器自动化操作
- **数据提取示例**：展示智能数据提取功能
- **断言验证示例**：演示 AI 驱动的断言验证
- **复杂Web交互示例**：展示如何处理复杂的 Web 交互场景

## 快速开始

### 前提条件

- JDK 11 或更高版本
- Maven 3.6 或更高版本
- 有效的 AI 模型 API 密钥（如 OpenAI GPT-4 API Key）

### 运行示例

1. 确保已安装所有必要的依赖

```bash
mvn clean install
```

2. 运行特定示例

```bash
# 运行基础示例
mvn exec:java -Dexec.mainClass="com.midscene.examples.basic.BasicExample"

# 运行Web自动化示例
mvn exec:java -Dexec.mainClass="com.midscene.examples.web.WebAutomationExample"

# 运行数据提取示例
mvn exec:java -Dexec.mainClass="com.midscene.examples.data.DataExtractionExample"

# 运行断言验证示例
mvn exec:java -Dexec.mainClass="com.midscene.examples.assertion.AssertionExample"

# 运行复杂Web交互示例
mvn exec:java -Dexec.mainClass="com.midscene.examples.complex.ComplexWebInteractionExample"
```

## 配置说明

示例代码中包含了配置示例，实际使用时需要替换为您自己的配置：

1. **AI模型配置**：
   - 在实际使用前，需要配置有效的 AI 模型 API 密钥
   - 支持多种 AI 模型，如 OpenAI GPT-4、开源模型等

2. **浏览器配置**：
   - 可以配置不同的浏览器（Chrome、Firefox、Edge等）
   - 支持配置浏览器参数、超时设置等

## 示例说明

### 1. 基础示例 (BasicExample)

演示 Midscene 的三个核心功能：
- 自然语言交互：通过自然语言指令控制界面
- 数据提取：从页面中智能提取结构化数据
- 断言验证：验证页面状态是否符合预期

### 2. Web自动化示例 (WebAutomationExample)

演示浏览器自动化的常见操作：
- 页面导航
- 表单交互
- 复杂交互场景
- 多页面操作

### 3. 数据提取示例 (DataExtractionExample)

展示 Midscene 强大的数据提取能力：
- 基本数据提取（标题、文本内容等）
- 结构化数据提取（表格、列表等）
- 复杂数据结构提取
- 实时数据监控

### 4. 断言验证示例 (AssertionExample)

演示 AI 驱动的断言验证功能：
- 基本断言（元素存在、文本内容等）
- 复杂断言（业务规则验证）
- 条件断言
- 视觉断言（UI布局、样式验证）

### 5. 复杂Web交互示例 (ComplexWebInteractionExample)

展示如何处理复杂的 Web 交互场景：
- 多步骤表单处理
- 动态内容交互
- 模态框和弹窗处理
- 拖拽和拖放操作
- 文件上传和下载
- 完整业务流程自动化

## 注意事项

1. **API 密钥安全**：请确保不要将您的 API 密钥提交到版本控制系统中
2. **网络连接**：示例运行需要稳定的网络连接以访问 AI 模型服务
3. **浏览器驱动**：确保已安装正确版本的浏览器驱动
4. **性能考虑**：AI 模型推理可能需要一定时间，请耐心等待

## 更多资源

- [官方文档](https://midscenejs.com/)
- [API 参考](https://midscenejs.com/api.html)
- [Wiki 文档](../wiki/README.md)

## 贡献指南

欢迎提交改进建议或新的示例代码。请遵循项目的贡献指南。