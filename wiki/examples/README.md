# 示例代码

本章节提供了 Midscene Java 的各种使用示例，帮助您快速上手并掌握框架的使用方法。

## 📋 目录

- [基础示例](#基础示例)
  - [Web 自动化基础示例](#web-自动化基础示例)
  - [Android 自动化基础示例](#android-自动化基础示例)
- [进阶示例](#进阶示例)
  - [数据提取示例](#数据提取示例)
  - [表单填写示例](#表单填写示例)
  - [多步骤流程示例](#多步骤流程示例)
- [高级示例](#高级示例)
  - [跨平台操作示例](#跨平台操作示例)
  - [并发操作示例](#并发操作示例)
  - [自定义策略示例](#自定义策略示例)
- [实际应用场景](#实际应用场景)
  - [电商自动化测试](#电商自动化测试)
  - [社交媒体管理](#社交媒体管理)
  - [移动应用测试](#移动应用测试)

## 🌟 基础示例

### Web 自动化基础示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;

public class BasicWebAutomation {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 导航到网站
            agent.ai_action("导航到 https://www.example.com").get();
            
            // 点击链接
            agent.ai_action("点击'更多'链接").get();
            
            // 验证页面标题
            boolean titleCorrect = agent.ai_assert("页面标题包含'Example Domain'").get();
            System.out.println("标题验证结果: " + titleCorrect);
            
            // 提取页面信息
            Map<String, String> pageInfo = agent.ai_extract("提取页面标题和主要内容").get();
            System.out.println("页面标题: " + pageInfo.get("title"));
            System.out.println("主要内容: " + pageInfo.get("content"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Android 自动化基础示例

```java
import com.midscene.core.Agent;
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;

public class BasicAndroidAutomation {
    public static void main(String[] args) {
        try {
            // 创建设备实例
            AndroidDevice device = new AndroidDevice();
            device.connect().join();
            
            // 创建 Agent
            AndroidUIContextProvider provider = new AndroidUIContextProvider(device);
            Agent agent = new Agent(provider);
            
            // 启动设置应用
            agent.ai_action("启动设置应用").get();
            
            // 点击 WLAN 设置
            agent.ai_action("点击 WLAN 设置").get();
            
            // 打开 Wi-Fi
            agent.ai_action("打开 Wi-Fi 开关").get();
            
            // 验证 Wi-Fi 状态
            boolean wifiEnabled = agent.ai_assert("Wi-Fi 已启用").get();
            System.out.println("Wi-Fi 状态: " + wifiEnabled);
            
            // 返回主屏幕
            agent.ai_action("返回主屏幕").get();
            
            // 断开设备连接
            device.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 🚀 进阶示例

### 数据提取示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;

public class DataExtractionExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 导航到新闻网站
            agent.ai_action("导航到 https://news.example.com").get();
            
            // 提取头条新闻
            Map<String, String> headlineNews = agent.ai_extract(
                "提取头条新闻的标题、摘要和发布时间"
            ).get();
            
            System.out.println("头条新闻:");
            System.out.println("标题: " + headlineNews.get("title"));
            System.out.println("摘要: " + headlineNews.get("summary"));
            System.out.println("发布时间: " + headlineNews.get("publishTime"));
            
            // 提取所有新闻列表
            Map<String, String> newsList = agent.ai_extract(
                "提取新闻列表中所有新闻的标题和链接，以JSON格式返回"
            ).get();
            
            System.out.println("新闻列表:");
            System.out.println(newsList.get("json"));
            
            // 提取天气信息
            Map<String, String> weatherInfo = agent.ai_extract(
                "提取当前天气信息，包括温度、天气状况和湿度"
            ).get();
            
            System.out.println("天气信息:");
            System.out.println("温度: " + weatherInfo.get("temperature"));
            System.out.println("天气状况: " + weatherInfo.get("condition"));
            System.out.println("湿度: " + weatherInfo.get("humidity"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 表单填写示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.HashMap;
import java.util.Map;

public class FormFillingExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 导航到注册页面
            agent.ai_action("导航到 https://register.example.com").get();
            
            // 填写用户信息
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", "testuser123");
            userInfo.put("email", "test@example.com");
            userInfo.put("password", "SecurePassword123!");
            userInfo.put("confirmPassword", "SecurePassword123!");
            userInfo.put("firstName", "Test");
            userInfo.put("lastName", "User");
            
            // 使用循环填写表单
            for (Map.Entry<String, String> entry : userInfo.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                
                // 定位输入框并填写
                String inputId = agent.ai_locate("输入框，标签为'" + fieldName + "'").get();
                agent.inputText(inputId, fieldValue).get();
            }
            
            // 选择国家
            agent.ai_action("选择国家为'中国'").get();
            
            // 同意条款
            agent.ai_action("勾选'我同意服务条款'复选框").get();
            
            // 提交表单
            agent.ai_action("点击注册按钮").get();
            
            // 验证注册成功
            boolean registrationSuccess = agent.ai_assert("显示注册成功消息").get();
            System.out.println("注册结果: " + registrationSuccess);
            
            if (registrationSuccess) {
                // 提取用户ID
                Map<String, String> userId = agent.ai_extract("提取新注册用户的ID").get();
                System.out.println("用户ID: " + userId.get("userId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 多步骤流程示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;

public class MultiStepProcessExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 步骤1: 登录
            System.out.println("步骤1: 登录");
            agent.ai_action("导航到 https://login.example.com").get();
            agent.ai_action("输入用户名: demouser").get();
            agent.ai_action("输入密码: demopass").get();
            agent.ai_action("点击登录按钮").get();
            
            // 验证登录成功
            boolean loginSuccess = agent.ai_assert("登录成功，显示用户仪表盘").get();
            if (!loginSuccess) {
                System.out.println("登录失败，终止流程");
                return;
            }
            System.out.println("登录成功");
            
            // 步骤2: 导航到产品页面
            System.out.println("步骤2: 导航到产品页面");
            agent.ai_action("点击产品菜单").get();
            agent.ai_action("点击电子产品分类").get();
            
            // 步骤3: 搜索产品
            System.out.println("步骤3: 搜索产品");
            agent.ai_action("在搜索框中输入'智能手机'").get();
            agent.ai_action("点击搜索按钮").get();
            
            // 步骤4: 选择产品
            System.out.println("步骤4: 选择产品");
            agent.ai_action("点击第一个搜索结果").get();
            
            // 步骤5: 添加到购物车
            System.out.println("步骤5: 添加到购物车");
            agent.ai_action("选择颜色为'黑色'").get();
            agent.ai_action("选择容量为'128GB'").get();
            agent.ai_action("点击添加到购物车按钮").get();
            
            // 验证添加成功
            boolean addToCartSuccess = agent.ai_assert("显示'已添加到购物车'消息").get();
            if (!addToCartSuccess) {
                System.out.println("添加到购物车失败");
                return;
            }
            System.out.println("已添加到购物车");
            
            // 步骤6: 查看购物车
            System.out.println("步骤6: 查看购物车");
            agent.ai_action("点击购物车图标").get();
            
            // 提取购物车信息
            Map<String, String> cartInfo = agent.ai_extract(
                "提取购物车中的商品名称、价格和数量"
            ).get();
            
            System.out.println("购物车信息:");
            System.out.println("商品名称: " + cartInfo.get("productName"));
            System.out.println("价格: " + cartInfo.get("price"));
            System.out.println("数量: " + cartInfo.get("quantity"));
            
            // 步骤7: 结账
            System.out.println("步骤7: 结账");
            agent.ai_action("点击结账按钮").get();
            
            // 填写收货信息
            agent.ai_action("输入收货人姓名: 张三").get();
            agent.ai_action("输入收货地址: 北京市朝阳区某某街道123号").get();
            agent.ai_action("输入联系电话: 13800138000").get();
            
            // 选择支付方式
            agent.ai_action("选择支付方式为'支付宝'").get();
            
            // 提交订单
            agent.ai_action("点击提交订单按钮").get();
            
            // 验证订单提交成功
            boolean orderSuccess = agent.ai_assert("显示订单提交成功页面").get();
            if (orderSuccess) {
                // 提取订单号
                Map<String, String> orderInfo = agent.ai_extract("提取订单号").get();
                System.out.println("订单提交成功，订单号: " + orderInfo.get("orderNumber"));
            } else {
                System.out.println("订单提交失败");
            }
            
            // 步骤8: 登出
            System.out.println("步骤8: 登出");
            agent.ai_action("点击用户头像").get();
            agent.ai_action("点击登出链接").get();
            
            // 验证登出成功
            boolean logoutSuccess = agent.ai_assert("返回登录页面").get();
            System.out.println("登出结果: " + logoutSuccess);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 🔧 高级示例

### 跨平台操作示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CrossPlatformExample {
    public static void main(String[] args) {
        try {
            // 初始化 Web 平台
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider webProvider = new PlaywrightUIContextProvider(page);
            Agent webAgent = new Agent(webProvider);
            
            // 初始化 Android 平台
            AndroidDevice device = new AndroidDevice();
            device.connect().join();
            AndroidUIContextProvider androidProvider = new AndroidUIContextProvider(device);
            Agent androidAgent = new Agent(androidProvider);
            
            // 场景: 在 Web 上注册账户，然后在 Android 应用上登录
            
            // 步骤1: 在 Web 上注册
            System.out.println("在 Web 上注册账户");
            webAgent.ai_action("导航到 https://register.example.com").get();
            webAgent.ai_action("输入用户名: crossplatform_user").get();
            webAgent.ai_action("输入邮箱: cp@example.com").get();
            webAgent.ai_action("输入密码: CrossPlatform123!").get();
            webAgent.ai_action("确认密码: CrossPlatform123!").get();
            webAgent.ai_action("点击注册按钮").get();
            
            // 验证注册成功
            boolean webRegistrationSuccess = webAgent.ai_assert("显示注册成功消息").get();
            if (!webRegistrationSuccess) {
                System.out.println("Web 注册失败，终止流程");
                return;
            }
            System.out.println("Web 注册成功");
            
            // 步骤2: 在 Android 应用上登录
            System.out.println("在 Android 应用上登录");
            androidAgent.ai_action("启动示例应用").get();
            androidAgent.ai_action("点击登录按钮").get();
            androidAgent.ai_action("输入用户名: crossplatform_user").get();
            androidAgent.ai_action("输入密码: CrossPlatform123!").get();
            androidAgent.ai_action("点击登录按钮").get();
            
            // 验证登录成功
            boolean androidLoginSuccess = androidAgent.ai_assert("显示用户仪表盘").get();
            if (!androidLoginSuccess) {
                System.out.println("Android 登录失败");
                return;
            }
            System.out.println("Android 登录成功");
            
            // 步骤3: 在 Web 上添加数据，然后在 Android 上验证
            System.out.println("在 Web 上添加数据");
            webAgent.ai_action("导航到 https://data.example.com").get();
            webAgent.ai_action("点击添加数据按钮").get();
            webAgent.ai_action("输入数据标题: 跨平台测试数据").get();
            webAgent.ai_action("输入数据内容: 这是通过 Web 添加的测试数据").get();
            webAgent.ai_action("点击保存按钮").get();
            
            // 验证数据添加成功
            boolean dataAddSuccess = webAgent.ai_assert("显示数据保存成功消息").get();
            if (!dataAddSuccess) {
                System.out.println("数据添加失败");
                return;
            }
            System.out.println("数据添加成功");
            
            // 步骤4: 在 Android 上验证数据
            System.out.println("在 Android 上验证数据");
            androidAgent.ai_action("点击数据菜单").get();
            androidAgent.ai_action("点击刷新按钮").get();
            
            // 验证数据存在
            boolean dataExists = androidAgent.ai_assert("显示标题为'跨平台测试数据'的数据项").get();
            if (dataExists) {
                System.out.println("跨平台数据同步验证成功");
                
                // 提取数据详情
                Map<String, String> dataDetails = androidAgent.ai_extract(
                    "提取'跨平台测试数据'的详细内容"
                ).get();
                
                System.out.println("数据详情: " + dataDetails.get("content"));
            } else {
                System.out.println("跨平台数据同步验证失败");
            }
            
            // 清理资源
            device.disconnect();
            playwright.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 并发操作示例

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentOperationsExample {
    public static void main(String[] args) {
        try {
            // 创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            
            // 创建多个浏览器实例
            List<Browser> browsers = new ArrayList<>();
            List<Agent> agents = new ArrayList<>();
            
            Playwright playwright = Playwright.create();
            
            // 初始化 3 个浏览器实例和 Agent
            for (int i = 0; i < 3; i++) {
                Browser browser = playwright.chromium().launch();
                browsers.add(browser);
                
                PlaywrightPage page = new PlaywrightPage(browser.newPage());
                PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
                Agent agent = new Agent(provider);
                agents.add(agent);
            }
            
            // 并发执行任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            // 任务1: 搜索产品
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                try {
                    Agent agent = agents.get(0);
                    agent.ai_action("导航到 https://search.example.com").get();
                    agent.ai_action("在搜索框中输入'笔记本电脑'").get();
                    agent.ai_action("点击搜索按钮").get();
                    
                    // 提取搜索结果
                    Map<String, String> searchResults = agent.ai_extract(
                        "提取前5个搜索结果的标题和价格"
                    ).get();
                    
                    System.out.println("任务1完成 - 搜索结果: " + searchResults.get("json"));
                } catch (Exception e) {
                    System.err.println("任务1执行失败: " + e.getMessage());
                }
            }, executorService);
            
            // 任务2: 获取新闻
            CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                try {
                    Agent agent = agents.get(1);
                    agent.ai_action("导航到 https://news.example.com").get();
                    
                    // 提取头条新闻
                    Map<String, String> headlineNews = agent.ai_extract(
                        "提取头条新闻的标题和摘要"
                    ).get();
                    
                    System.out.println("任务2完成 - 头条新闻: " + headlineNews.get("title"));
                } catch (Exception e) {
                    System.err.println("任务2执行失败: " + e.getMessage());
                }
            }, executorService);
            
            // 任务3: 检查天气
            CompletableFuture<Void> task3 = CompletableFuture.runAsync(() -> {
                try {
                    Agent agent = agents.get(2);
                    agent.ai_action("导航到 https://weather.example.com").get();
                    
                    // 提取天气信息
                    Map<String, String> weatherInfo = agent.ai_extract(
                        "提取当前天气信息，包括温度和天气状况"
                    ).get();
                    
                    System.out.println("任务3完成 - 天气信息: " + weatherInfo.get("temperature") + ", " + weatherInfo.get("condition"));
                } catch (Exception e) {
                    System.err.println("任务3执行失败: " + e.getMessage());
                }
            }, executorService);
            
            // 添加到列表
            futures.add(task1);
            futures.add(task2);
            futures.add(task3);
            
            // 等待所有任务完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            try {
                allTasks.get(60, TimeUnit.SECONDS); // 最多等待60秒
                System.out.println("所有并发任务完成");
            } catch (Exception e) {
                System.err.println("等待任务完成时出错: " + e.getMessage());
            }
            
            // 清理资源
            for (Browser browser : browsers) {
                browser.close();
            }
            playwright.close();
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 自定义策略示例

```java
import com.midscene.core.Agent;
import com.midscene.core.ActionContext;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;
import java.util.HashMap;

public class CustomStrategyExample {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 注册自定义登录策略
            agent.registerActionStrategy("smartLogin", context -> {
                String username = context.getParam("username");
                String password = context.getParam("password");
                
                try {
                    // 导航到登录页面
                    agent.ai_action("导航到 https://login.example.com").get();
                    
                    // 检查是否已经登录
                    boolean alreadyLoggedIn = agent.ai_assert("显示用户仪表盘").get();
                    if (alreadyLoggedIn) {
                        System.out.println("已经登录，跳过登录步骤");
                        return true;
                    }
                    
                    // 尝试多种登录方式
                    // 方式1: 标准登录表单
                    boolean standardFormExists = agent.ai_assert("存在用户名和密码输入框").get();
                    if (standardFormExists) {
                        agent.ai_action("输入用户名: " + username).get();
                        agent.ai_action("输入密码: " + password).get();
                        agent.ai_action("点击登录按钮").get();
                        
                        boolean loginSuccess = agent.ai_assert("登录成功").get();
                        if (loginSuccess) {
                            System.out.println("使用标准登录表单成功");
                            return true;
                        }
                    }
                    
                    // 方式2: 社交登录
                    boolean socialLoginExists = agent.ai_assert("存在社交媒体登录选项").get();
                    if (socialLoginExists) {
                        agent.ai_action("点击Google登录按钮").get();
                        
                        // 这里可能需要处理Google OAuth流程
                        // 简化示例，假设直接成功
                        boolean socialLoginSuccess = agent.ai_assert("登录成功").get();
                        if (socialLoginSuccess) {
                            System.out.println("使用社交媒体登录成功");
                            return true;
                        }
                    }
                    
                    // 方式3: 弹窗登录
                    boolean popupExists = agent.ai_assert("存在登录弹窗").get();
                    if (popupExists) {
                        agent.ai_action("在弹窗中输入用户名: " + username).get();
                        agent.ai_action("在弹窗中输入密码: " + password).get();
                        agent.ai_action("点击弹窗中的登录按钮").get();
                        
                        boolean popupLoginSuccess = agent.ai_assert("登录成功").get();
                        if (popupLoginSuccess) {
                            System.out.println("使用弹窗登录成功");
                            return true;
                        }
                    }
                    
                    // 所有方式都失败
                    System.out.println("所有登录方式都失败");
                    return false;
                } catch (Exception e) {
                    System.err.println("登录过程中出错: " + e.getMessage());
                    return false;
                }
            });
            
            // 注册自定义数据提取策略
            agent.registerExtractionStrategy("productTable", context -> {
                String tableName = context.getParam("tableName");
                
                try {
                    // 定位表格
                    String tableId = agent.ai_locate("表格，标题为'" + tableName + "'").get();
                    
                    // 提取表头
                    Map<String, String> headers = agent.ai_extract(
                        "提取表格的表头信息"
                    ).get();
                    
                    // 提取数据行
                    Map<String, String> rows = agent.ai_extract(
                        "提取表格的所有数据行，以JSON格式返回"
                    ).get();
                    
                    // 组合结果
                    Map<String, String> result = new HashMap<>();
                    result.put("headers", headers.get("json"));
                    result.put("rows", rows.get("json"));
                    
                    return result;
                } catch (Exception e) {
                    System.err.println("提取表格数据时出错: " + e.getMessage());
                    return new HashMap<>();
                }
            });
            
            // 使用自定义登录策略
            Map<String, String> loginParams = new HashMap<>();
            loginParams.put("username", "testuser");
            loginParams.put("password", "testpass");
            
            boolean loginResult = agent.executeStrategy("smartLogin", loginParams);
            System.out.println("自定义登录策略结果: " + loginResult);
            
            if (loginResult) {
                // 使用自定义数据提取策略
                Map<String, String> extractParams = new HashMap<>();
                extractParams.put("tableName", "产品列表");
                
                Map<String, String> productData = agent.executeExtractionStrategy("productTable", extractParams);
                System.out.println("产品数据: " + productData.get("rows"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 🎯 实际应用场景

### 电商自动化测试

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class EcommerceAutomationTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 测试数据
            List<Map<String, String>> testProducts = new ArrayList<>();
            
            Map<String, String> product1 = new HashMap<>();
            product1.put("name", "智能手机");
            product1.put("category", "电子产品");
            product1.put("minPrice", "1000");
            product1.put("maxPrice", "3000");
            testProducts.add(product1);
            
            Map<String, String> product2 = new HashMap<>();
            product2.put("name", "笔记本电脑");
            product2.put("category", "电子产品");
            product2.put("minPrice", "4000");
            product2.put("maxPrice", "8000");
            testProducts.add(product2);
            
            // 测试流程
            for (Map<String, String> product : testProducts) {
                System.out.println("测试产品: " + product.get("name"));
                
                // 步骤1: 搜索产品
                agent.ai_action("导航到 https://shop.example.com").get();
                agent.ai_action("在搜索框中输入'" + product.get("name") + "'").get();
                agent.ai_action("点击搜索按钮").get();
                
                // 步骤2: 验证搜索结果
                boolean searchResultsExist = agent.ai_assert("显示搜索结果").get();
                if (!searchResultsExist) {
                    System.out.println("搜索结果为空，跳过此产品");
                    continue;
                }
                
                // 步骤3: 筛选价格范围
                agent.ai_action("设置最低价格为" + product.get("minPrice")).get();
                agent.ai_action("设置最高价格为" + product.get("maxPrice")).get();
                agent.ai_action("点击应用筛选按钮").get();
                
                // 步骤4: 验证筛选结果
                boolean filterResultsExist = agent.ai_assert("显示筛选后的结果").get();
                if (!filterResultsExist) {
                    System.out.println("筛选后无结果，跳过此产品");
                    continue;
                }
                
                // 步骤5: 提取产品信息
                Map<String, String> productInfo = agent.ai_extract(
                    "提取前3个搜索结果的产品名称、价格和评分"
                ).get();
                
                System.out.println("产品信息: " + productInfo.get("json"));
                
                // 步骤6: 选择第一个产品
                agent.ai_action("点击第一个搜索结果").get();
                
                // 步骤7: 验证产品详情页
                boolean productPageValid = agent.ai_assert("显示产品详情页").get();
                if (!productPageValid) {
                    System.out.println("产品详情页加载失败，跳过此产品");
                    continue;
                }
                
                // 步骤8: 提取详细产品信息
                Map<String, String> detailedInfo = agent.ai_extract(
                    "提取产品的详细描述、规格、价格和库存状态"
                ).get();
                
                System.out.println("详细产品信息:");
                System.out.println("描述: " + detailedInfo.get("description"));
                System.out.println("规格: " + detailedInfo.get("specifications"));
                System.out.println("价格: " + detailedInfo.get("price"));
                System.out.println("库存状态: " + detailedInfo.get("stockStatus"));
                
                // 步骤9: 检查用户评价
                agent.ai_action("滚动到用户评价部分").get();
                
                boolean reviewsExist = agent.ai_assert("存在用户评价").get();
                if (reviewsExist) {
                    Map<String, String> reviewInfo = agent.ai_extract(
                        "提取前3条用户评价的评分和内容"
                    ).get();
                    
                    System.out.println("用户评价: " + reviewInfo.get("json"));
                } else {
                    System.out.println("暂无用户评价");
                }
                
                // 步骤10: 添加到购物车（如果有库存）
                boolean inStock = agent.ai_assert("产品有库存").get();
                if (inStock) {
                    agent.ai_action("点击添加到购物车按钮").get();
                    
                    boolean addToCartSuccess = agent.ai_assert("显示'已添加到购物车'消息").get();
                    if (addToCartSuccess) {
                        System.out.println("成功添加到购物车");
                    } else {
                        System.out.println("添加到购物车失败");
                    }
                } else {
                    System.out.println("产品无库存，无法添加到购物车");
                }
                
                System.out.println("产品测试完成: " + product.get("name"));
                System.out.println("-----------------------------------");
            }
            
            // 最终步骤: 查看购物车
            agent.ai_action("导航到 https://shop.example.com/cart").get();
            
            boolean cartNotEmpty = agent.ai_assert("购物车不为空").get();
            if (cartNotEmpty) {
                Map<String, String> cartInfo = agent.ai_extract(
                    "提取购物车中所有商品的名称、价格和数量"
                ).get();
                
                System.out.println("购物车内容: " + cartInfo.get("json"));
                
                // 计算总价
                Map<String, String> totalPrice = agent.ai_extract(
                    "提取购物车总价"
                ).get();
                
                System.out.println("购物车总价: " + totalPrice.get("total"));
            } else {
                System.out.println("购物车为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 社交媒体管理

```java
import com.midscene.core.Agent;
import com.midscene.web.playwright.PlaywrightPage;
import com.midscene.web.playwright.PlaywrightUIContextProvider;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SocialMediaManagement {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            PlaywrightPage page = new PlaywrightPage(browser.newPage());
            PlaywrightUIContextProvider provider = new PlaywrightUIContextProvider(page);
            Agent agent = new Agent(provider);
            
            // 社交媒体平台列表
            List<String> platforms = List.of("Twitter", "Facebook", "LinkedIn");
            
            // 发布内容
            String postContent = "今天学习了 Midscene Java，一个强大的 AI 驱动自动化框架！#AI #自动化 #Java";
            
            for (String platform : platforms) {
                System.out.println("在 " + platform + " 发布内容");
                
                // 步骤1: 登录平台
                agent.ai_action("导航到 https://" + platform.toLowerCase() + ".example.com").get();
                
                // 检查是否已登录
                boolean isLoggedIn = agent.ai_assert("用户已登录").get();
                if (!isLoggedIn) {
                    // 执行登录流程
                    agent.ai_action("点击登录按钮").get();
                    agent.ai_action("输入用户名: social_user").get();
                    agent.ai_action("输入密码: social_pass").get();
                    agent.ai_action("点击登录提交按钮").get();
                    
                    // 验证登录成功
                    boolean loginSuccess = agent.ai_assert("登录成功").get();
                    if (!loginSuccess) {
                        System.out.println(platform + " 登录失败，跳过此平台");
                        continue;
                    }
                }
                
                // 步骤2: 导航到发布页面
                agent.ai_action("点击发布新内容按钮").get();
                
                // 步骤3: 输入内容
                agent.ai_action("在内容输入框中输入: " + postContent).get();
                
                // 步骤4: 添加标签（根据平台不同）
                switch (platform) {
                    case "Twitter":
                        agent.ai_action("添加标签: #AI #自动化 #Java").get();
                        break;
                    case "Facebook":
                        agent.ai_action("添加标签: AI, 自动化, Java").get();
                        break;
                    case "LinkedIn":
                        agent.ai_action("添加标签: AI, 自动化, Java").get();
                        break;
                }
                
                // 步骤5: 发布内容
                agent.ai_action("点击发布按钮").get();
                
                // 步骤6: 验证发布成功
                boolean publishSuccess = agent.ai_assert("内容发布成功").get();
                if (publishSuccess) {
                    System.out.println(platform + " 内容发布成功");
                    
                    // 提取发布后的内容信息
                    Map<String, String> postInfo = agent.ai_extract(
                        "提取刚发布内容的URL和发布时间"
                    ).get();
                    
                    System.out.println("内容URL: " + postInfo.get("url"));
                    System.out.println("发布时间: " + postInfo.get("publishTime"));
                } else {
                    System.out.println(platform + " 内容发布失败");
                }
                
                System.out.println("-----------------------------------");
            }
            
            // 监控互动
            System.out.println("监控社交媒体互动");
            
            for (String platform : platforms) {
                System.out.println("检查 " + platform + " 上的互动");
                
                // 导航到平台
                agent.ai_action("导航到 https://" + platform.toLowerCase() + ".example.com").get();
                
                // 检查是否已登录
                boolean isLoggedIn = agent.ai_assert("用户已登录").get();
                if (!isLoggedIn) {
                    System.out.println(platform + " 未登录，跳过互动检查");
                    continue;
                }
                
                // 导航到个人主页
                agent.ai_action("点击个人资料").get();
                
                // 查找最近发布的内容
                agent.ai_action("点击最近发布的内容").get();
                
                // 提取互动数据
                Map<String, String> interactionData = agent.ai_extract(
                    "提取内容的点赞数、评论数和分享数"
                ).get();
                
                System.out.println(platform + " 互动数据:");
                System.out.println("点赞数: " + interactionData.get("likes"));
                System.out.println("评论数: " + interactionData.get("comments"));
                System.out.println("分享数: " + interactionData.get("shares"));
                
                // 检查评论
                boolean hasComments = agent.ai_assert("存在评论").get();
                if (hasComments) {
                    // 提取前3条评论
                    Map<String, String> comments = agent.ai_extract(
                        "提取前3条评论的内容和作者"
                    ).get();
                    
                    System.out.println("评论内容: " + comments.get("json"));
                    
                    // 回复评论（如果有正面评论）
                    boolean hasPositiveComments = agent.ai_assert("存在正面评论").get();
                    if (hasPositiveComments) {
                        agent.ai_action("点击第一条正面评论的回复按钮").get();
                        agent.ai_action("输入回复: 感谢您的支持！").get();
                        agent.ai_action("点击提交回复按钮").get();
                        
                        boolean replySuccess = agent.ai_assert("回复成功").get();
                        if (replySuccess) {
                            System.out.println("成功回复评论");
                        }
                    }
                }
                
                System.out.println("-----------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 移动应用测试

```java
import com.midscene.core.Agent;
import com.midscene.android.AndroidDevice;
import com.midscene.android.AndroidUIContextProvider;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MobileAppTesting {
    public static void main(String[] args) {
        try {
            // 连接设备
            AndroidDevice device = new AndroidDevice();
            device.connect().join();
            
            // 创建 Agent
            AndroidUIContextProvider provider = new AndroidUIContextProvider(device);
            Agent agent = new Agent(provider);
            
            // 测试场景列表
            List<Map<String, String>> testScenarios = new ArrayList<>();
            
            // 场景1: 用户注册
            Map<String, String> registrationScenario = new HashMap<>();
            registrationScenario.put("name", "用户注册");
            registrationScenario.put("description", "测试新用户注册流程");
            testScenarios.add(registrationScenario);
            
            // 场景2: 用户登录
            Map<String, String> loginScenario = new HashMap<>();
            loginScenario.put("name", "用户登录");
            loginScenario.put("description", "测试用户登录流程");
            testScenarios.add(loginScenario);
            
            // 场景3: 浏览产品
            Map<String, String> browseProductsScenario = new HashMap<>();
            browseProductsScenario.put("name", "浏览产品");
            browseProductsScenario.put("description", "测试产品浏览功能");
            testScenarios.add(browseProductsScenario);
            
            // 场景4: 搜索产品
            Map<String, String> searchScenario = new HashMap<>();
            searchScenario.put("name", "搜索产品");
            searchScenario.put("description", "测试产品搜索功能");
            testScenarios.add(searchScenario);
            
            // 场景5: 添加到购物车
            Map<String, String> addToCartScenario = new HashMap<>();
            addToCartScenario.put("name", "添加到购物车");
            addToCartScenario.put("description", "测试添加产品到购物车功能");
            testScenarios.add(addToCartScenario);
            
            // 执行测试场景
            for (Map<String, String> scenario : testScenarios) {
                System.out.println("执行测试场景: " + scenario.get("name"));
                System.out.println("描述: " + scenario.get("description"));
                
                try {
                    switch (scenario.get("name")) {
                        case "用户注册":
                            testUserRegistration(agent);
                            break;
                        case "用户登录":
                            testUserLogin(agent);
                            break;
                        case "浏览产品":
                            testBrowseProducts(agent);
                            break;
                        case "搜索产品":
                            testSearchProducts(agent);
                            break;
                        case "添加到购物车":
                            testAddToCart(agent);
                            break;
                    }
                    
                    System.out.println("测试场景完成: " + scenario.get("name"));
                } catch (Exception e) {
                    System.err.println("测试场景失败: " + scenario.get("name") + ", 错误: " + e.getMessage());
                }
                
                // 返回主屏幕
                agent.ai_action("返回主屏幕").get();
                
                // 清除应用数据（如果需要）
                agent.ai_action("清除应用数据").get();
                
                System.out.println("-----------------------------------");
            }
            
            // 断开设备连接
            device.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void testUserRegistration(Agent agent) throws Exception {
        // 启动应用
        agent.ai_action("启动测试应用").get();
        
        // 点击注册按钮
        agent.ai_action("点击注册按钮").get();
        
        // 填写注册表单
        agent.ai_action("输入用户名: testuser123").get();
        agent.ai_action("输入邮箱: test@example.com").get();
        agent.ai_action("输入密码: TestPass123!").get();
        agent.ai_action("确认密码: TestPass123!").get();
        agent.ai_action("输入手机号: 13800138000").get();
        
        // 同意条款
        agent.ai_action("勾选同意服务条款复选框").get();
        
        // 提交注册
        agent.ai_action("点击注册按钮").get();
        
        // 验证注册成功
        boolean registrationSuccess = agent.ai_assert("显示注册成功消息").get();
        if (registrationSuccess) {
            System.out.println("用户注册测试通过");
            
            // 提取用户ID
            Map<String, String> userInfo = agent.ai_extract("提取新注册用户的ID").get();
            System.out.println("用户ID: " + userInfo.get("userId"));
        } else {
            System.out.println("用户注册测试失败");
        }
    }
    
    private static void testUserLogin(Agent agent) throws Exception {
        // 启动应用
        agent.ai_action("启动测试应用").get();
        
        // 点击登录按钮
        agent.ai_action("点击登录按钮").get();
        
        // 填写登录表单
        agent.ai_action("输入用户名: testuser").get();
        agent.ai_action("输入密码: testpass").get();
        
        // 提交登录
        agent.ai_action("点击登录按钮").get();
        
        // 验证登录成功
        boolean loginSuccess = agent.ai_assert("显示用户仪表盘").get();
        if (loginSuccess) {
            System.out.println("用户登录测试通过");
            
            // 提取用户信息
            Map<String, String> userInfo = agent.ai_extract("提取用户名和邮箱").get();
            System.out.println("用户名: " + userInfo.get("username"));
            System.out.println("邮箱: " + userInfo.get("email"));
        } else {
            System.out.println("用户登录测试失败");
        }
    }
    
    private static void testBrowseProducts(Agent agent) throws Exception {
        // 启动应用并登录
        agent.ai_action("启动测试应用").get();
        agent.ai_action("点击登录按钮").get();
        agent.ai_action("输入用户名: testuser").get();
        agent.ai_action("输入密码: testpass").get();
        agent.ai_action("点击登录按钮").get();
        
        // 验证登录成功
        boolean loginSuccess = agent.ai_assert("显示用户仪表盘").get();
        if (!loginSuccess) {
            throw new Exception("登录失败，无法进行产品浏览测试");
        }
        
        // 点击产品菜单
        agent.ai_action("点击产品菜单").get();
        
        // 验证产品列表显示
        boolean productsDisplayed = agent.ai_assert("显示产品列表").get();
        if (productsDisplayed) {
            System.out.println("产品浏览测试通过");
            
            // 提取产品信息
            Map<String, String> productInfo = agent.ai_extract(
                "提取前5个产品的名称和价格"
            ).get();
            
            System.out.println("产品信息: " + productInfo.get("json"));
            
            // 滚动查看更多产品
            agent.ai_action("向下滚动查看更多产品").get();
            
            // 点击第一个产品
            agent.ai_action("点击第一个产品").get();
            
            // 验证产品详情页
            boolean productDetailsDisplayed = agent.ai_assert("显示产品详情页").get();
            if (productDetailsDisplayed) {
                System.out.println("产品详情页显示正常");
                
                // 提取产品详细信息
                Map<String, String> detailedInfo = agent.ai_extract(
                    "提取产品的详细描述、价格和库存状态"
                ).get();
                
                System.out.println("产品详细信息:");
                System.out.println("描述: " + detailedInfo.get("description"));
                System.out.println("价格: " + detailedInfo.get("price"));
                System.out.println("库存状态: " + detailedInfo.get("stockStatus"));
            } else {
                System.out.println("产品详情页显示异常");
            }
        } else {
            System.out.println("产品浏览测试失败");
        }
    }
    
    private static void testSearchProducts(Agent agent) throws Exception {
        // 启动应用并登录
        agent.ai_action("启动测试应用").get();
        agent.ai_action("点击登录按钮").get();
        agent.ai_action("输入用户名: testuser").get();
        agent.ai_action("输入密码: testpass").get();
        agent.ai_action("点击登录按钮").get();
        
        // 验证登录成功
        boolean loginSuccess = agent.ai_assert("显示用户仪表盘").get();
        if (!loginSuccess) {
            throw new Exception("登录失败，无法进行产品搜索测试");
        }
        
        // 点击搜索按钮
        agent.ai_action("点击搜索按钮").get();
        
        // 输入搜索关键词
        agent.ai_action("输入搜索关键词: 手机").get();
        
        // 提交搜索
        agent.ai_action("点击搜索提交按钮").get();
        
        // 验证搜索结果
        boolean searchResultsDisplayed = agent.ai_assert("显示搜索结果").get();
        if (searchResultsDisplayed) {
            System.out.println("产品搜索测试通过");
            
            // 提取搜索结果
            Map<String, String> searchResults = agent.ai_extract(
                "提取前3个搜索结果的名称、价格和评分"
            ).get();
            
            System.out.println("搜索结果: " + searchResults.get("json"));
            
            // 应用筛选条件
            agent.ai_action("点击筛选按钮").get();
            agent.ai_action("选择品牌为'华为'").get();
            agent.ai_action("设置价格范围为2000-5000").get();
            agent.ai_action("点击应用筛选按钮").get();
            
            // 验证筛选结果
            boolean filterResultsDisplayed = agent.ai_assert("显示筛选后的结果").get();
            if (filterResultsDisplayed) {
                System.out.println("搜索筛选功能正常");
                
                // 提取筛选后的结果
                Map<String, String> filterResults = agent.ai_extract(
                    "提取筛选后的产品名称和价格"
                ).get();
                
                System.out.println("筛选结果: " + filterResults.get("json"));
            } else {
                System.out.println("搜索筛选功能异常");
            }
        } else {
            System.out.println("产品搜索测试失败");
        }
    }
    
    private static void testAddToCart(Agent agent) throws Exception {
        // 启动应用并登录
        agent.ai_action("启动测试应用").get();
        agent.ai_action("点击登录按钮").get();
        agent.ai_action("输入用户名: testuser").get();
        agent.ai_action("输入密码: testpass").get();
        agent.ai_action("点击登录按钮").get();
        
        // 验证登录成功
        boolean loginSuccess = agent.ai_assert("显示用户仪表盘").get();
        if (!loginSuccess) {
            throw new Exception("登录失败，无法进行添加到购物车测试");
        }
        
        // 搜索产品
        agent.ai_action("点击搜索按钮").get();
        agent.ai_action("输入搜索关键词: 耳机").get();
        agent.ai_action("点击搜索提交按钮").get();
        
        // 验证搜索结果
        boolean searchResultsDisplayed = agent.ai_assert("显示搜索结果").get();
        if (!searchResultsDisplayed) {
            throw new Exception("搜索结果为空，无法进行添加到购物车测试");
        }
        
        // 点击第一个产品
        agent.ai_action("点击第一个搜索结果").get();
        
        // 验证产品详情页
        boolean productDetailsDisplayed = agent.ai_assert("显示产品详情页").get();
        if (!productDetailsDisplayed) {
            throw new Exception("产品详情页加载失败，无法进行添加到购物车测试");
        }
        
        // 选择产品规格
        agent.ai_action("选择颜色为'黑色'").get();
        agent.ai_action("选择版本为'标准版'").get();
        
        // 设置数量
        agent.ai_action("设置数量为2").get();
        
        // 添加到购物车
        agent.ai_action("点击添加到购物车按钮").get();
        
        // 验证添加成功
        boolean addToCartSuccess = agent.ai_assert("显示'已添加到购物车'消息").get();
        if (addToCartSuccess) {
            System.out.println("添加到购物车测试通过");
            
            // 查看购物车
            agent.ai_action("点击购物车图标").get();
            
            // 验证购物车内容
            boolean cartNotEmpty = agent.ai_assert("购物车不为空").get();
            if (cartNotEmpty) {
                System.out.println("购物车内容正常");
                
                // 提取购物车信息
                Map<String, String> cartInfo = agent.ai_extract(
                    "提取购物车中商品的名称、价格、数量和总价"
                ).get();
                
                System.out.println("购物车信息:");
                System.out.println("商品名称: " + cartInfo.get("productName"));
                System.out.println("价格: " + cartInfo.get("price"));
                System.out.println("数量: " + cartInfo.get("quantity"));
                System.out.println("总价: " + cartInfo.get("totalPrice"));
            } else {
                System.out.println("购物车为空，添加到购物车可能失败");
            }
        } else {
            System.out.println("添加到购物车测试失败");
        }
    }
}
```

---

这些示例涵盖了 Midscene Java 的各种使用场景，从基础的 Web 和 Android 自动化到复杂的多平台协作和自定义策略。您可以根据这些示例快速上手并开发自己的自动化解决方案。