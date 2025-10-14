# Midscene Java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17+-green.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.1-blue.svg)](https://search.maven.org/artifact/com.midscene/midscene-java-parent)

基于AI的Web和Android自动化框架，支持自然语言驱动的UI操作 - Java版本

## 🌟 项目概述

Midscene Java 是一个革命性的基于 AI 的自动化框架，专为 Web 和 Android 平台的 UI 自动化操作而设计。它是 Midscene Python 的 Java 版本实现，继承了其核心理念：**让自动化变得像说话一样简单**。

### 🎯 核心特性

- **自然语言操作** - 用日常语言描述操作意图，AI自动理解并执行
- **智能元素定位** - 多策略融合，自动选择最优的定位方法，适应页面变化
- **结构化数据提取** - 使用自然语言描述提取复杂结构化数据
- **智能断言验证** - 用自然语言描述验证条件，AI自动判断
- **多平台支持** - 统一接口支持Web和Android平台
- **可视化调试** - 详细的执行截图和决策过程记录
- **代码优化与重构** - 经过系统性重构，代码更加模块化和可维护

## 🏗️ 项目结构

```
midscene-java/
├── packages/
│   ├── core/               # 核心模块，提供Agent和AI引擎
│   ├── web/                # Web自动化模块
│   │   ├── playwright/     # Playwright实现
│   │   └── selenium/       # Selenium实现
│   ├── android/            # Android自动化模块
│   ├── cli/                # 命令行工具
│   ├── examples/           # 示例代码
│   ├── playground/         # 开发测试环境
│   └── tests/              # 测试用例
├── apps/                   # 应用示例
├── docs/                   # 项目文档和优化计划
└── wiki/                   # 项目wiki文档
```

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.6+ 或 Gradle 7.0+
- 浏览器 (Chrome/Firefox/Edge，用于 Web 自动化)
- AI 模型 API Key (OpenAI、Claude、Qwen 或 Gemini 任选其一)

### 安装

在你的 `pom.xml` 文件中添加 Midscene Java 依赖：

```xml
<dependencies>
    <!-- 核心模块 -->
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-core</artifactId>
        <version>0.1.1</version>
    </dependency>
    
    <!-- Web 自动化模块 (根据需要选择) -->
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-web-playwright</artifactId>
        <version>0.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-web-selenium</artifactId>
        <version>0.1.1</version>
    </dependency>
    
    <!-- Android 自动化模块 (根据需要选择) -->
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-android</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

### 配置 AI 模型

创建 `application.properties` 或 `application.yml` 文件配置 AI 模型：

```properties
# application.properties
midscene.ai.provider=openai
midscene.ai.model=gpt-4-vision-preview
midscene.ai.api-key=your_openai_api_key_here
```

### 示例代码

#### Web 自动化示例

```java
package com.example;

import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;

public class SearchExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            // 创建浏览器实例
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            
            // 创建 PlaywrightPage 包装器
            PlaywrightPage playwrightPage = new PlaywrightPage(page);
            
            // 创建 Agent
            Agent agent = new Agent(new PlaywrightUIContextProvider(playwrightPage));
            
            // 导航到网站
            page.navigate("https://www.baidu.com");
            
            // 使用自然语言进行搜索
            agent.aiAction("在搜索框输入'Java 教程'");
            agent.aiAction("点击搜索按钮");
            
            // 验证搜索结果
            agent.aiAssert("页面显示了 Java 教程的搜索结果");
            
            System.out.println("✅ 搜索操作完成！");
            
            // 关闭浏览器
            browser.close();
        }
    }
}
```

#### 数据提取示例

```java
package com.example;

import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            
            PlaywrightPage playwrightPage = new PlaywrightPage(page);
            Agent agent = new Agent(new PlaywrightUIContextProvider(playwrightPage));
            
            // 访问新闻网站
            page.navigate("https://news.example.com");
            
            // 提取结构化数据
            Map<String, Object> schema = new HashMap<>();
            schema.put("articles", List.of(
                Map.of(
                    "title", "新闻标题",
                    "time", "发布时间",
                    "summary", "新闻摘要"
                )
            ));
            
            Map<String, Object> newsData = agent.aiExtract(schema);
            
            // 输出结果
            List<Map<String, String>> articles = (List<Map<String, String>>) newsData.get("articles");
            for (Map<String, String> article : articles) {
                System.out.println("📰 " + article.get("title"));
                System.out.println("⏰ " + article.get("time"));
                System.out.println("📄 " + article.get("summary") + "\n");
            }
            
            browser.close();
        }
    }
}
```

#### Android 自动化示例

```java
package com.example;

import com.midscene.core.Agent;
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;

import java.util.concurrent.CompletableFuture;

public class AndroidExample {
    public static void main(String[] args) {
        // 连接 Android 设备
        AndroidDevice device = new AndroidDevice();
        CompletableFuture<Void> connectFuture = device.connect();
        connectFuture.join(); // 等待连接完成
        
        try {
            // 创建 Agent
            Agent agent = new Agent(new AndroidUIContextProvider(device));
            
            // 启动应用
            agent.aiAction("启动设置应用");
            
            // 执行操作
            agent.aiAction("点击Wi-Fi选项");
            agent.aiAssert("Wi-Fi设置页面已打开");
            
            System.out.println("✅ Android自动化操作完成！");
        } finally {
            device.disconnect();
        }
    }
}
```

## 📖 文档

- [项目概述](wiki/项目概述.md)
- [安装配置](wiki/安装配置.md)
- [快速开始](wiki/快速开始.md)
- [API参考](wiki/api-reference/README.md)
- [示例代码](wiki/examples/README.md)
- [常见问题](wiki/常见问题.md)
- [核心概念](wiki/核心概念/README.md)
- [平台集成](wiki/平台集成/README.md)

## 🆚 与传统工具的对比

| 特性 | 传统自动化工具 | Midscene Java |
|------|---------------|---------------|
| **学习曲线** | 陡峭，需要学习复杂 API | 平缓，自然语言驱动 |
| **代码可读性** | 晦涩难懂 | 直观易懂 |
| **维护成本** | 高，页面变化需要大量修改 | 低，AI 自动适应变化 |
| **元素定位** | 手动编写选择器 | AI 智能定位 |
| **错误处理** | 需要手动处理各种异常 | AI 自动重试和恢复 |
| **跨平台** | 需要学习不同工具 | 统一接口 |
| **代码质量** | 因项目而异 | 经过系统性重构，模块化设计 |

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是提交bug报告、功能请求、文档改进还是代码贡献。

### 如何贡献

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建一个 Pull Request

### 开发环境设置

```bash
# 克隆仓库
git clone https://github.com/Master-Frank/midscene-java.git
cd midscene-java

# 构建项目
mvn clean install

# 运行测试
mvn test
```

### 代码规范

- 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范
- 为新功能添加相应的测试用例
- 为公共API添加JavaDoc文档
- 保持代码模块化，避免过长的方法

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- 感谢 [Midscene](https://github.com/web-infra-dev/midscene) 项目提供的灵感和基础设计
- 感谢所有贡献者和社区成员的支持
- 感谢项目团队完成的代码规范与重构工作

## 📞 联系我们

- **GitHub**: [Master-Frank/midscene-java](https://github.com/Master-Frank/midscene-java)
- **问题反馈**: [GitHub Issues](https://github.com/Master-Frank/midscene-java/issues)
- **讨论**: [GitHub Discussions](https://github.com/Master-Frank/midscene-java/discussions)

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！