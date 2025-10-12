# Midscene Java 测试策略

## 概述

本文档定义了Midscene Java项目的测试策略，包括测试类型、测试框架、测试覆盖率要求和测试流程。该策略旨在确保项目质量、可靠性和可维护性。

## 测试金字塔

```
        /\
       /  \
      / E2E \  <- 端到端测试 (少量，高价值)
     /______\
    /        \
   /Integration\ <- 集成测试 (适量，验证组件交互)
  /____________\
 /              \
/   Unit Tests   \ <- 单元测试 (大量，快速反馈)
/________________\
```

### 测试类型比例

- **单元测试**：70% - 快速、独立、验证单个组件
- **集成测试**：20% - 验证组件间交互
- **端到端测试**：10% - 验证完整用户场景

## 单元测试

### 测试框架

- **JUnit 5**：主要的单元测试框架
- **Mockito**：用于模拟依赖
- **AssertJ**：提供流畅的断言API
- **TestContainers**：用于需要外部依赖的测试

### 测试结构

使用Given-When-Then结构组织测试：

```java
class TaskExecutorTest {
    @Mock
    private AIModelService aiModelService;
    
    @Mock
    private InsightEngine insightEngine;
    
    @InjectMocks
    private TaskExecutor taskExecutor;
    
    @Test
    @DisplayName("应该成功执行简单任务")
    void execute_simpleTask_success() {
        // Given
        TaskRequest request = new TaskRequest("test-task", "click button");
        AIResponse aiResponse = new AIResponse("success", "Button clicked");
        when(aiModelService.process(any(AIRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(aiResponse));
        
        // When
        CompletableFuture<TaskResult> future = taskExecutor.execute(request);
        TaskResult result = future.join();
        
        // Then
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getMessage()).isEqualTo("Button clicked");
        verify(aiModelService).process(any(AIRequest.class));
    }
}
```

### 测试命名规范

使用`methodName_condition_expectedResult`格式：

```java
// 好的例子
void execute_validRequest_success() {}
void execute_nullRequest_throwsIllegalArgumentException() {}
void execute_aiModelFailure_returnsFailureResult() {}

// 不好的例子
void test1() {}
void executeTest() {}
void testExecute() {}
```

### 测试覆盖要求

- **代码覆盖率**：最低80%，目标90%
- **分支覆盖率**：最低75%，目标85%
- **关键路径**：100%覆盖

### 测试数据管理

使用测试数据构建器模式创建测试数据：

```java
public class TaskRequestBuilder {
    private String id = "default-task";
    private String description = "default description";
    private Map<String, Object> parameters = new HashMap<>();
    
    public static TaskRequestBuilder aTaskRequest() {
        return new TaskRequestBuilder();
    }
    
    public TaskRequestBuilder withId(String id) {
        this.id = id;
        return this;
    }
    
    public TaskRequestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public TaskRequestBuilder withParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
    
    public TaskRequest build() {
        return new TaskRequest(id, description, parameters);
    }
}

// 使用示例
TaskRequest request = TaskRequestBuilder.aTaskRequest()
    .withId("test-task")
    .withDescription("click button")
    .withParameter("timeout", 5000)
    .build();
```

## 集成测试

### 测试框架

- **JUnit 5**：测试执行框架
- **Spring Boot Test**：用于Spring集成测试
- **TestContainers**：用于容器化测试环境
- **WireMock**：用于模拟外部服务

### 测试范围

1. **组件集成**：测试多个组件的交互
2. **数据库集成**：测试数据访问层
3. **外部服务集成**：测试与外部服务的交互
4. **平台集成**：测试与不同平台的集成

### 示例：AI服务集成测试

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
@Testcontainers
class AIModelServiceIntegrationTest {
    
    @Container
    static GenericContainer<?> aiServiceContainer = new GenericContainer<>("openai-gpt:latest")
        .withExposedPorts(8080)
        .withEnv("OPENAI_API_KEY", "test-key");
    
    @Autowired
    private AIModelService aiModelService;
    
    @Test
    @DisplayName("应该成功处理AI请求")
    void process_validRequest_success() {
        // Given
        String baseUrl = "http://" + aiServiceContainer.getHost() + ":" + aiServiceContainer.getMappedPort(8080);
        AIModelServiceConfig config = new AIModelServiceConfig();
        config.setBaseUrl(baseUrl);
        
        AIRequest request = new AIRequest("分析这个页面", null);
        
        // When
        CompletableFuture<AIResponse> future = aiModelService.process(request);
        AIResponse response = future.join();
        
        // Then
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getContent()).isNotEmpty();
    }
}
```

### 示例：Web平台集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebPlatformIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    @DisplayName("应该成功执行Web操作")
    void executeWebOperation_success() {
        // Given
        String url = "http://localhost:" + port + "/api/web/execute";
        WebOperationRequest request = new WebOperationRequest();
        request.setUrl("https://example.com");
        request.setAction("click");
        request.setSelector("#submit-button");
        
        // When
        ResponseEntity<WebOperationResponse> response = restTemplate.postForEntity(
            url, request, WebOperationResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
}
```

## 端到端测试

### 测试框架

- **Selenium/Playwright**：用于Web自动化测试
- **Appium**：用于移动应用测试
- **TestCafe**：用于现代Web应用测试
- **Cucumber**：用于BDD风格的测试

### 测试场景

1. **完整用户流程**：从开始到结束的完整用户场景
2. **跨平台场景**：验证不同平台的一致性
3. **性能场景**：验证系统性能
4. **错误场景**：验证错误处理

### 示例：Web端到端测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebEndToEndTest {
    
    @Autowired
    private PlaywrightPage playwrightPage;
    
    @Test
    @DisplayName("应该完成完整的UI自动化流程")
    void completeUIAutomationFlow_success() {
        // Given
        playwrightPage.navigate("https://example.com/login");
        
        // When
        playwrightPage.fill("#username", "testuser");
        playwrightPage.fill("#password", "password");
        playwrightPage.click("#login-button");
        
        // Then
        playwrightPage.waitForSelector("#dashboard");
        assertThat(playwrightPage.isVisible("#dashboard")).isTrue();
        
        // When
        playwrightPage.click("#profile-link");
        
        // Then
        playwrightPage.waitForSelector("#profile-page");
        assertThat(playwrightPage.getText("#profile-name")).isEqualTo("Test User");
    }
}
```

### 示例：Android端到端测试

```java
@SpringBootTest
class AndroidEndToEndTest {
    
    @Autowired
    private AndroidPlatform androidPlatform;
    
    @Test
    @DisplayName("应该完成完整的Android应用流程")
    void completeAndroidAppFlow_success() {
        // Given
        androidPlatform.launchApp("com.example.app");
        
        // When
        androidPlatform.tap("Login Button");
        androidPlatform.inputText("Username Field", "testuser");
        androidPlatform.inputText("Password Field", "password");
        androidPlatform.tap("Submit Button");
        
        // Then
        assertThat(androidPlatform.isElementPresent("Dashboard")).isTrue();
        
        // When
        androidPlatform.tap("Menu Button");
        androidPlatform.tap("Profile Item");
        
        // Then
        assertThat(androidPlatform.getText("Profile Name")).isEqualTo("Test User");
    }
}
```

## 性能测试

### 测试工具

- **JMeter**：用于负载和压力测试
- **Gatling**：用于高性能负载测试
- **Microbenchmark**：用于微基准测试
- **YourKit**：用于性能分析

### 测试指标

1. **响应时间**：平均、95百分位、99百分位
2. **吞吐量**：每秒处理的请求数
3. **资源使用**：CPU、内存、IO使用率
4. **并发能力**：最大并发用户数

### 示例：性能测试

```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TaskExecutorBenchmark {
    
    private TaskExecutor taskExecutor;
    private TaskRequest request;
    
    @Setup
    public void setup() {
        taskExecutor = new TaskExecutor();
        request = new TaskRequest("benchmark-task", "click button");
    }
    
    @Benchmark
    public TaskResult executeTask() {
        return taskExecutor.execute(request).join();
    }
}
```

## 测试数据管理

### 测试数据策略

1. **测试数据隔离**：每个测试使用独立的数据
2. **数据清理**：测试后清理数据
3. **数据版本控制**：管理测试数据的版本
4. **敏感数据**：使用脱敏或模拟数据

### 测试数据工具

- **DBUnit**：用于数据库测试数据管理
- **Faker**：用于生成测试数据
- **TestDataBuilder**：自定义测试数据构建器

### 示例：测试数据管理

```java
@TestMethodOrder(OrderAnnotation.class)
class TaskRepositoryIntegrationTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TestDataCleaner testDataCleaner;
    
    @Test
    @Order(1)
    @DisplayName("应该成功创建任务")
    void createTask_validData_success() {
        // Given
        Task task = TaskBuilder.aTask()
            .withId("test-task-1")
            .withDescription("Test task")
            .withStatus(TaskStatus.PENDING)
            .build();
        
        // When
        Task savedTask = taskRepository.save(task);
        
        // Then
        assertThat(savedTask.getId()).isEqualTo("test-task-1");
        assertThat(savedTask.getDescription()).isEqualTo("Test task");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.PENDING);
    }
    
    @Test
    @Order(2)
    @DisplayName("应该成功查找任务")
    void findTask_existingId_success() {
        // When
        Optional<Task> foundTask = taskRepository.findById("test-task-1");
        
        // Then
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getId()).isEqualTo("test-task-1");
    }
    
    @AfterAll
    static void cleanup() {
        testDataCleaner.cleanAll();
    }
}
```

## 测试环境管理

### 环境类型

1. **开发环境**：本地开发测试
2. **集成环境**：持续集成测试
3. **预生产环境**：生产前验证
4. **生产环境**：生产监控测试

### 环境配置

使用Spring Profile管理不同环境的配置：

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
ai:
  model:
    provider: mock
    api-key: test-key
    
# application-integration.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/midscene_test
    driver-class-name: org.postgresql.Driver
    username: test_user
    password: test_password
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    
ai:
  model:
    provider: openai
    api-key: ${OPENAI_API_KEY}
```

## 持续集成中的测试

### CI/CD流程

1. **代码提交**：触发构建
2. **静态分析**：运行代码质量检查
3. **单元测试**：运行所有单元测试
4. **构建应用**：构建应用包
5. **集成测试**：运行集成测试
6. **部署测试环境**：部署到测试环境
7. **端到端测试**：运行端到端测试
8. **部署预生产**：部署到预生产环境
9. **验收测试**：运行验收测试
10. **部署生产**：部署到生产环境

### 示例：GitHub Actions配置

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: midscene_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run static analysis
      run: mvn spotbugs:check checkstyle:check
    
    - name: Run unit tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn test -P integration-test
      env:
        SPRING_PROFILES_ACTIVE: integration
        POSTGRES_HOST: localhost
        POSTGRES_PORT: 5432
        POSTGRES_DB: midscene_test
        POSTGRES_USER: postgres
        POSTGRES_PASSWORD: postgres
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v1
      with:
        file: target/site/jacoco/jacoco.xml
```

## 测试报告和监控

### 测试报告

1. **单元测试报告**：JUnit XML格式
2. **覆盖率报告**：JaCoCo HTML报告
3. **性能测试报告**：JMeter/Gatling报告
4. **端到端测试报告**：Allure报告

### 测试监控

1. **测试执行时间**：监控测试执行时间
2. **测试稳定性**：监控测试通过率
3. **测试覆盖率**：监控代码覆盖率趋势
4. **缺陷分析**：分析测试发现的缺陷

### 示例：测试报告配置

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 测试最佳实践

### 测试设计原则

1. **FIRST原则**：
   - **Fast**：测试应该快速运行
   - **Independent**：测试应该独立，不依赖其他测试
   - **Repeatable**：测试应该在任何环境下可重复
   - **Self-Validating**：测试应该有明确的通过/失败结果
   - **Timely**：测试应该及时编写

2. **AAA模式**：
   - **Arrange**：准备测试数据和条件
   - **Act**：执行被测试的操作
   - **Assert**：验证结果

### 测试反模式

1. **测试依赖**：测试之间不应该有依赖关系
2. **测试顺序**：不应该依赖测试执行顺序
3. **测试数据泄露**：测试不应该修改共享数据
4. **测试过度复杂**：测试逻辑应该简单明了
5. **测试不足**：关键路径必须有测试覆盖

### 测试维护

1. **定期重构**：定期重构测试代码
2. **删除冗余**：删除冗余或过时的测试
3. **更新测试**：随着功能变化更新测试
4. **测试文档**：维护测试文档和说明

## 总结

本测试策略为Midscene Java项目提供了全面的测试指导，包括：

1. **测试金字塔**：平衡不同类型的测试
2. **测试框架**：选择合适的测试工具和框架
3. **测试覆盖率**：确保足够的测试覆盖
4. **测试环境**：管理不同测试环境
5. **持续集成**：集成测试到CI/CD流程
6. **测试监控**：监控测试质量和效果

通过遵循这些策略，可以确保Midscene Java项目的质量、可靠性和可维护性。