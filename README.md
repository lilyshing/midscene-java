# Midscene Java [![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/Master-Frank/midscene-java)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17+-green.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.1-blue.svg)](https://search.maven.org/artifact/com.midscene/midscene-java-parent)

AI-powered automation framework for Web and Android with natural language-driven UI operations - Java version

## 🌐 Language Version
- [中文版本 (Chinese Version)](README-zh.md)

## 🌟 Project Overview

Midscene Java is a revolutionary AI-powered automation framework designed for UI automation operations on Web and Android platforms. It is the Java implementation of Midscene Python, inheriting its core philosophy: **making automation as simple as speaking**. 

### 🎯 Core Features

- **Natural Language Operations** - Describe operation intentions in everyday language, and AI will automatically understand and execute them
- **Intelligent Element Locating** - Multi-strategy fusion, automatically selects the optimal positioning method, adapts to page changes
- **Structured Data Extraction** - Use natural language to extract complex structured data
- **Intelligent Assertion Verification** - Describe verification conditions in natural language, AI automatically judges
- **Multi-Platform Support** - Unified interface supports Web and Android platforms
- **Visual Debugging** - Detailed execution screenshots and decision process recording
- **Code Optimization and Refactoring** - Systematically refactored for more modular and maintainable code

## 🏗️ Project Structure

```
midscene-java/
├── packages/
│   ├── core/               # Core module, providing Agent and AI engine
│   ├── web/                # Web automation module
│   │   ├── playwright/     # Playwright implementation
│   │   └── selenium/       # Selenium implementation
│   ├── android/            # Android automation module
│   ├── cli/                # Command line tool
│   ├── examples/           # Example code
│   ├── playground/         # Development testing environment
│   └── tests/              # Test cases
├── apps/                   # Application examples
├── docs/                   # Project documentation and optimization plans
└── wiki/                   # Project wiki documentation
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+ or Gradle 7.0+
- Browser (Chrome/Firefox/Edge, for Web automation)
- AI model API Key (Choose one from OpenAI, Claude, Qwen, or Gemini)

### Installation

Add Midscene Java dependencies to your `pom.xml` file:

```xml
<dependencies>
    <!-- Core module -->
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-core</artifactId>
        <version>0.1.1</version>
    </dependency>
    
    <!-- Web automation modules (choose as needed) -->
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
    
    <!-- Android automation module (choose as needed) -->
    <dependency>
        <groupId>com.midscene</groupId>
        <artifactId>midscene-android</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

### Configure AI Model

Create an `application.properties` or `application.yml` file to configure the AI model:

```properties
# application.properties
midscene.ai.provider=openai
midscene.ai.model=gpt-4-vision-preview
midscene.ai.api-key=your_openai_api_key_here
```

### Example Code

#### Web Automation Example

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
            // Create browser instance
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            
            // Create PlaywrightPage wrapper
            PlaywrightPage playwrightPage = new PlaywrightPage(page);
            
            // Create Agent
            Agent agent = new Agent(new PlaywrightUIContextProvider(playwrightPage));
            
            // Navigate to website
            page.navigate("https://www.baidu.com");
            
            // Use natural language for search
            agent.aiAction("Type 'Java tutorial' in the search box");
            agent.aiAction("Click the search button");
            
            // Verify search results
            agent.aiAssert("The page displays search results for Java tutorials");
            
            System.out.println("✅ Search operation completed!");
            
            // Close browser
            browser.close();
        }
    }
}
```

#### Data Extraction Example

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
            
            // Visit news website
            page.navigate("https://news.example.com");
            
            // Extract structured data
            Map<String, Object> schema = new HashMap<>();
            schema.put("articles", List.of(
                Map.of(
                    "title", "News title",
                    "time", "Publish time",
                    "summary", "News summary"
                )
            ));
            
            Map<String, Object> newsData = agent.aiExtract(schema);
            
            // Output results
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

#### Android Automation Example

```java
package com.example;

import com.midscene.core.Agent;
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;

import java.util.concurrent.CompletableFuture;

public class AndroidExample {
    public static void main(String[] args) {
        // Connect to Android device
        AndroidDevice device = new AndroidDevice();
        CompletableFuture<Void> connectFuture = device.connect();
        connectFuture.join(); // Wait for connection to complete
        
        try {
            // Create Agent
            Agent agent = new Agent(new AndroidUIContextProvider(device));
            
            // Launch application
            agent.aiAction("Launch the settings app");
            
            // Perform operations
            agent.aiAction("Tap on the Wi-Fi option");
            agent.aiAssert("The Wi-Fi settings page is open");
            
            System.out.println("✅ Android automation operation completed!");
        } finally {
            device.disconnect();
        }
    }
}
```

## 📖 Documentation

- [Project Overview](wiki/项目概述.md) - *Chinese only*
- [Installation and Configuration](wiki/安装配置.md) - *Chinese only*
- [Quick Start](wiki/快速开始.md) - *Chinese only*
- [API Reference](wiki/api-reference/README.md)
- [Example Code](wiki/examples/README.md)
- [Frequently Asked Questions](wiki/常见问题.md) - *Chinese only*
- [Core Concepts](wiki/核心概念/README.md) - *Chinese only*
- [Platform Integration](wiki/平台集成/README.md)

## 🆚 Comparison with Traditional Tools

| Feature | Traditional Automation Tools | Midscene Java |
|---------|------------------------------|---------------|
| **Learning Curve** | Steep, requires learning complex APIs | Gentle, natural language driven |
| **Code Readability** | Obscure and hard to understand | Intuitive and easy to understand |
| **Maintenance Cost** | High, requires extensive modifications for page changes | Low, AI automatically adapts to changes |
| **Element Locating** | Manual selector writing | AI intelligent locating |
| **Error Handling** | Manual handling of various exceptions | AI automatic retry and recovery |
| **Cross-Platform** | Requires learning different tools | Unified interface |
| **Code Quality** | Varies by project | Systematically refactored, modular design |

## 🤝 Contribution Guidelines

We welcome all forms of contributions! Whether it's submitting bug reports, feature requests, documentation improvements, or code contributions.

### How to Contribute

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

### Development Environment Setup

```bash
# Clone the repository
git clone https://github.com/Master-Frank/midscene-java.git
cd midscene-java

# Build the project
mvn clean install

# Run tests
mvn test
```

### Code Standards

- Follow commit message conventions from [Conventional Commits](https://www.conventionalcommits.org/)
- Add corresponding test cases for new features
- Add JavaDoc documentation for public APIs
- Keep code modular, avoid overly long methods

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📝 Credits

Thanks to Midscene Project: https://github.com/web-infra-dev/midscene for inspiration and technical references

## 📞 Contact Us

- **GitHub**: [Master-Frank/midscene-java](https://github.com/Master-Frank/midscene-java)
- **Issue Reporting**: [GitHub Issues](https://github.com/Master-Frank/midscene-java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Master-Frank/midscene-java/discussions)

---

⭐ If this project helps you, please give us a star!