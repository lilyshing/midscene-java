# Midscene Java 代码规范与最佳实践指南

## 概述

本文档定义了Midscene Java项目的代码规范和最佳实践，旨在确保代码质量、可读性和可维护性。所有贡献者都应遵循这些规范。

## 代码风格

### 命名规范

#### 类名

- 使用PascalCase（大驼峰命名法）
- 名词或名词短语
- 避免缩写，除非是广泛接受的缩写

```java
// 好的例子
public class AIModelService {
}

public class TaskExecutor {
}

// 不好的例子
public class aiModelService {
}

public class TaskExec {
}
```

#### 方法名

- 使用camelCase（小驼峰命名法）
- 动词或动词短语
- 返回boolean类型的方法应以is、has、can、should等开头

```java
// 好的例子
public CompletableFuture<TaskResult> executeTask(TaskRequest request) {
}

public boolean isAvailable() {
}

// 不好的例子
public CompletableFuture<TaskResult> ExecuteTask(TaskRequest request) {
}

public boolean available() {
}
```

#### 变量名

- 使用camelCase（小驼峰命名法）
- 有意义的名称，避免单字母变量（除了循环计数器）
- 常量使用全大写，下划线分隔

```java
// 好的例子
private int maxRetryCount;
private static final String DEFAULT_CONFIG_PATH = "/config/default.json";

// 不好的例子
private int m;
private static final String defaultConfigPath = "/config/default.json";
```

#### 包名

- 全小写
- 使用点分隔符
- 遵循反向域名约定

```java
// 好的例子
package com.midscene.core.agent;
package com.midscene.web.playwright;

// 不好的例子
package com.Midscene.Core.Agent;
package com.midscene.web.Playwright;
```

### 代码格式

#### 缩进

- 使用4个空格缩进，不使用Tab
- IDE配置应确保保存时自动格式化

#### 行长度

- 每行不超过120个字符
- 超长行应在适当位置换行，通常在操作符后

```java
// 好的例子
public CompletableFuture<TaskResult> executeTask(
    TaskRequest request, 
    ExecutionContext context
) {
    // 实现
}

// 不好的例子
public CompletableFuture<TaskResult> executeTask(TaskRequest request, ExecutionContext context) {
    // 实现
}
```

#### 大括号

- 使用K&R风格
- 左大括号不换行
- 即使是单行语句也要使用大括号

```java
// 好的例子
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// 不好的例子
if (condition)
{
    doSomething();
}
else
    doSomethingElse();

if (condition) doSomething();
```

#### 空格

- 操作符前后使用空格
- 逗号后使用空格
- 左括号后和右括号前不使用空格

```java
// 好的例子
int result = a + b * c;
method(param1, param2, param3);

// 不好的例子
int result=a+b*c;
method ( param1 , param2 , param3 );
```

## 注释规范

### 类注释

每个公共类都应包含Javadoc注释，描述类的用途和示例用法：

```java
/**
 * AI模型服务，负责处理与AI模型的交互。
 * 
 * <p>该服务提供了与多种AI模型交互的统一接口，支持模型注册、
 * 请求处理和响应解析等功能。</p>
 * 
 * <p>示例用法：</p>
 * <pre>{@code
 * AIModelService service = new DefaultAIModelService();
 * service.registerModel("gpt-4", new GPT4Model());
 * AIResponse response = service.process(new AIRequest("分析这个页面"));
 * }</pre>
 * 
 * @author Midscene Team
 * @version 1.0
 * @since 1.0
 */
public interface AIModelService {
    // 方法定义
}
```

### 方法注释

每个公共方法都应包含Javadoc注释，描述方法的功能、参数、返回值和异常：

```java
/**
 * 执行指定的任务。
 * 
 * @param request 要执行的任务请求，不能为null
 * @return 包含执行结果的CompletableFuture
 * @throws IllegalArgumentException 如果request为null
 * @throws TaskExecutionException 如果任务执行失败
 */
public CompletableFuture<TaskResult> executeTask(TaskRequest request) {
    // 实现
}
```

### 字段注释

重要的字段应添加注释，说明其用途：

```java
/** 最大重试次数 */
private int maxRetryCount = 3;

/** AI模型映射表，键为模型名称，值为模型实例 */
private final Map<String, AIModel> models;
```

### 行内注释

行内注释用于解释复杂的代码逻辑或临时的解决方案：

```java
// 使用缓存减少AI模型调用次数
if (cache.containsKey(request)) {
    return cache.get(request);
}

// TODO: 优化这个算法，当前时间复杂度为O(n²)
for (int i = 0; i < list.size(); i++) {
    for (int j = 0; j < list.size(); j++) {
        // 复杂逻辑
    }
}
```

## 最佳实践

### 异常处理

#### 异常层次结构

定义清晰的异常层次结构：

```java
// 基础异常
public class MidsceneException extends Exception {
    private final ErrorCode errorCode;
    
    public MidsceneException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

// 具体异常
public class AIModelException extends MidsceneException {
    public AIModelException(String message, Throwable cause) {
        super(ErrorCode.AI_MODEL_ERROR, message, cause);
    }
}

public class PlatformException extends MidsceneException {
    public PlatformException(String message, Throwable cause) {
        super(ErrorCode.PLATFORM_ERROR, message, cause);
    }
}
```

#### 异常处理原则

1. **具体异常**：捕获具体的异常，而不是Exception
2. **早期检查**：在方法入口检查参数有效性
3. **资源清理**：使用try-with-resources确保资源释放
4. **异常转换**：将低级异常转换为高级异常

```java
// 好的例子
public void processFile(String filePath) throws FileProcessingException {
    if (filePath == null || filePath.isEmpty()) {
        throw new IllegalArgumentException("File path cannot be null or empty");
    }
    
    try (InputStream is = new FileInputStream(filePath)) {
        // 处理文件
    } catch (IOException e) {
        throw new FileProcessingException("Failed to process file: " + filePath, e);
    }
}

// 不好的例子
public void processFile(String filePath) {
    try {
        InputStream is = new FileInputStream(filePath);
        // 处理文件
        is.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 并发编程

#### CompletableFuture使用

1. **异步操作**：使用supplyAsync和runAsync执行异步操作
2. **组合操作**：使用thenCompose、thenCombine等方法组合异步操作
3. **异常处理**：使用exceptionally、handle处理异常
4. **超时控制**：使用orTimeout设置超时

```java
// 好的例子
public CompletableFuture<TaskResult> executeTask(TaskRequest request) {
    return CompletableFuture.supplyAsync(() -> validateRequest(request))
        .thenCompose(validRequest -> processRequest(validRequest))
        .thenApply(this::formatResult)
        .orTimeout(30, TimeUnit.SECONDS)
        .exceptionally(throwable -> {
            log.error("Task execution failed", throwable);
            return TaskResult.failure(throwable.getMessage());
        });
}

// 不好的例子
public TaskResult executeTask(TaskRequest request) {
    try {
        validateRequest(request);
        ProcessedRequest processed = processRequest(request);
        return formatResult(processed);
    } catch (Exception e) {
        log.error("Task execution failed", e);
        return TaskResult.failure(e.getMessage());
    }
}
```

#### 线程安全

1. **不可变对象**：尽可能使用不可变对象
2. **线程安全集合**：使用ConcurrentHashMap、CopyOnWriteArrayList等
3. **同步机制**：使用synchronized、ReentrantLock等同步机制
4. **原子操作**：使用AtomicInteger、AtomicReference等原子类

```java
// 好的例子
public class TaskCounter {
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final ConcurrentHashMap<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    
    public void incrementCompleted() {
        completedTasks.incrementAndGet();
    }
    
    public void updateTaskStatus(String taskId, TaskStatus status) {
        taskStatuses.put(taskId, status);
    }
}

// 不好的例子
public class TaskCounter {
    private int completedTasks = 0;
    private Map<String, TaskStatus> taskStatuses = new HashMap<>();
    
    public void incrementCompleted() {
        completedTasks++;  // 非线程安全
    }
    
    public void updateTaskStatus(String taskId, TaskStatus status) {
        taskStatuses.put(taskId, status);  // 非线程安全
    }
}
```

### 资源管理

#### 资源释放

使用try-with-resources确保资源正确释放：

```java
// 好的例子
public void processScreenshot(String filePath) throws ImageProcessingException {
    try (InputStream is = new FileInputStream(filePath);
         BufferedImage image = ImageIO.read(is)) {
        // 处理图像
    } catch (IOException e) {
        throw new ImageProcessingException("Failed to process image", e);
    }
}

// 不好的例子
public void processScreenshot(String filePath) throws ImageProcessingException {
    try {
        InputStream is = new FileInputStream(filePath);
        BufferedImage image = ImageIO.read(is);
        // 处理图像
        is.close();
    } catch (IOException e) {
        throw new ImageProcessingException("Failed to process image", e);
    }
}
```

#### 资源池

使用对象池管理昂贵资源：

```java
// 好的例子
public class BrowserContextPool {
    private final GenericObjectPool<BrowserContext> pool;
    
    public BrowserContextPool() {
        this.pool = new GenericObjectPool<>(new BrowserContextFactory());
        this.pool.setMaxTotal(10);
        this.pool.setMaxIdle(5);
    }
    
    public <T> T execute(BrowserOperation<T> operation) throws Exception {
        BrowserContext context = pool.borrowObject();
        try {
            return operation.apply(context);
        } finally {
            pool.returnObject(context);
        }
    }
}

// 不好的例子
public class BrowserContextManager {
    public <T> T execute(BrowserOperation<T> operation) throws Exception {
        BrowserContext context = createBrowserContext();  // 每次都创建新实例
        try {
            return operation.apply(context);
        } finally {
            context.close();
        }
    }
}
```

### 日志记录

#### 日志级别

- **ERROR**：系统错误，需要立即关注
- **WARN**：潜在问题，可能需要关注
- **INFO**：重要的业务流程信息
- **DEBUG**：详细的调试信息
- **TRACE**：更详细的跟踪信息

#### 日志格式

使用结构化日志，包含上下文信息：

```java
// 好的例子
private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

public CompletableFuture<TaskResult> execute(TaskRequest request) {
    String taskId = request.getId();
    logger.info("Starting task execution: taskId={}", taskId);
    
    try {
        TaskResult result = doExecute(request);
        logger.info("Task execution completed: taskId={}, status={}", taskId, result.getStatus());
        return CompletableFuture.completedFuture(result);
    } catch (Exception e) {
        logger.error("Task execution failed: taskId={}, error={}", taskId, e.getMessage(), e);
        throw new TaskExecutionException("Failed to execute task: " + taskId, e);
    }
}

// 不好的例子
public CompletableFuture<TaskResult> execute(TaskRequest request) {
    System.out.println("Starting task execution: " + request.getId());
    
    try {
        TaskResult result = doExecute(request);
        System.out.println("Task execution completed: " + request.getId() + ", " + result.getStatus());
        return CompletableFuture.completedFuture(result);
    } catch (Exception e) {
        System.out.println("Task execution failed: " + request.getId() + ", " + e.getMessage());
        e.printStackTrace();
        throw new TaskExecutionException("Failed to execute task: " + request.getId(), e);
    }
}
```

### 测试

#### 单元测试

1. **命名规范**：测试方法名应为methodName_condition_expectedResult
2. **测试结构**：使用Given-When-Then结构
3. **断言**：使用有意义的断言消息
4. **模拟**：使用Mockito模拟依赖

```java
// 好的例子
public class TaskExecutorTest {
    @Mock
    private AIModelService aiModelService;
    
    @InjectMocks
    private TaskExecutor taskExecutor;
    
    @Test
    public void executeTask_validRequest_success() {
        // Given
        TaskRequest request = new TaskRequest("test-task", "click button");
        AIResponse aiResponse = new AIResponse("success", "Button clicked");
        when(aiModelService.process(any(AIRequest.class))).thenReturn(CompletableFuture.completedFuture(aiResponse));
        
        // When
        CompletableFuture<TaskResult> future = taskExecutor.execute(request);
        TaskResult result = future.join();
        
        // Then
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getMessage()).isEqualTo("Button clicked");
        verify(aiModelService).process(any(AIRequest.class));
    }
}

// 不好的例子
public class TaskExecutorTest {
    @Test
    public void test1() {
        TaskExecutor taskExecutor = new TaskExecutor();
        TaskRequest request = new TaskRequest("test-task", "click button");
        CompletableFuture<TaskResult> future = taskExecutor.execute(request);
        TaskResult result = future.join();
        assertTrue(result.isSuccess());
    }
}
```

#### 集成测试

1. **测试环境**：使用测试专用的配置
2. **测试数据**：使用可预测的测试数据
3. **清理**：测试后清理资源

```java
// 好的例子
@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
public class TaskExecutorIntegrationTest {
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Test
    public void executeTask_realAIModel_success() {
        // Given
        TaskRequest request = new TaskRequest("integration-test", "find title");
        
        // When
        CompletableFuture<TaskResult> future = taskExecutor.execute(request);
        TaskResult result = future.join();
        
        // Then
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getMessage()).isNotEmpty();
    }
    
    @AfterEach
    public void cleanup() {
        // 清理测试资源
    }
}
```

## 性能优化

### 内存管理

#### 对象创建

1. **避免不必要的对象创建**：重用对象，使用对象池
2. **基本类型**：优先使用基本类型而非包装类
3. **字符串**：使用StringBuilder进行字符串拼接

```java
// 好的例子
public String generateReport(List<TaskResult> results) {
    StringBuilder sb = new StringBuilder();
    sb.append("Task Report\n");
    sb.append("-----------\n");
    
    for (TaskResult result : results) {
        sb.append("Task: ").append(result.getId())
          .append(", Status: ").append(result.getStatus())
          .append("\n");
    }
    
    return sb.toString();
}

// 不好的例子
public String generateReport(List<TaskResult> results) {
    String report = "Task Report\n-----------\n";
    
    for (TaskResult result : results) {
        report += "Task: " + result.getId() + ", Status: " + result.getStatus() + "\n";
    }
    
    return report;
}
```

#### 集合使用

1. **初始容量**：为集合设置合适的初始容量
2. **集合选择**：根据使用场景选择合适的集合类型

```java
// 好的例子
public class TaskManager {
    private final Map<String, Task> tasks = new HashMap<>(100);
    private final List<TaskResult> recentResults = new ArrayList<>(50);
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }
}

// 不好的例子
public class TaskManager {
    private final Map<String, Task> tasks = new HashMap<>();  // 默认容量16，可能导致扩容
    private final List<TaskResult> recentResults = new LinkedList<>();  // 不适合随机访问
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }
}
```

### 并发优化

#### 线程池配置

根据任务类型配置合适的线程池：

```java
// 好的例子
public class ExecutorServiceManager {
    // CPU密集型任务
    private final ExecutorService cpuIntensiveExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        new ThreadFactoryBuilder().setNameFormat("cpu-intensive-%d").build()
    );
    
    // IO密集型任务
    private final ExecutorService ioIntensiveExecutor = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setNameFormat("io-intensive-%d").build()
    );
    
    // 定时任务
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(
        2,
        new ThreadFactoryBuilder().setNameFormat("scheduled-%d").build()
    );
}

// 不好的例子
public class ExecutorServiceManager {
    // 所有任务使用同一个线程池，可能导致资源竞争
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
}
```

#### 锁优化

1. **锁范围**：尽量缩小锁的范围
2. **锁类型**：根据场景选择合适的锁类型
3. **避免嵌套锁**：防止死锁

```java
// 好的例子
public class TaskQueue {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<Task> queue = new LinkedList<>();
    
    public void addTask(Task task) {
        lock.lock();
        try {
            queue.add(task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public Task takeTask() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }
}

// 不好的例子
public class TaskQueue {
    private final Queue<Task> queue = new LinkedList<>();
    
    public synchronized void addTask(Task task) {
        queue.add(task);
        notifyAll();
    }
    
    public synchronized Task takeTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.remove();
    }
}
```

## 安全实践

### 输入验证

对所有外部输入进行验证：

```java
// 好的例子
public class TaskRequestValidator {
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final Pattern VALID_TASK_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    public void validate(TaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Task request cannot be null");
        }
        
        if (request.getId() == null || !VALID_TASK_ID_PATTERN.matcher(request.getId()).matches()) {
            throw new IllegalArgumentException("Invalid task ID");
        }
        
        if (request.getDescription() == null || 
            request.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Invalid task description");
        }
    }
}

// 不好的例子
public class TaskRequestValidator {
    public void validate(TaskRequest request) {
        // 没有进行任何验证
    }
}
```

### 敏感信息处理

1. **日志脱敏**：不在日志中记录敏感信息
2. **密码存储**：使用安全的密码存储方式
3. **加密传输**：使用HTTPS等加密传输

```java
// 好的例子
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);  // 不记录密码
        
        String hashedPassword = hashPassword(password);
        User user = userRepository.findByUsername(username);
        
        if (user != null && user.getPasswordHash().equals(hashedPassword)) {
            logger.info("User authentication successful: {}", username);
            return true;
        } else {
            logger.warn("User authentication failed: {}", username);
            return false;
        }
    }
    
    private String hashPassword(String password) {
        // 使用安全的哈希算法
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}

// 不好的例子
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating user: {}, password: {}", username, password);  // 记录了密码
        
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password);  // 明文存储和比较
    }
}
```

## 工具和配置

### IDE配置

推荐使用IntelliJ IDEA或Eclipse，并配置以下插件：

1. **代码格式化**：使用项目统一的代码格式化配置
2. **静态分析**：集成SpotBugs、Checkstyle等
3. **代码覆盖率**：集成JaCoCo
4. **代码审查**：集成SonarLint

### 构建工具

使用Maven或Gradle进行构建管理，配置以下内容：

1. **代码质量检查**：集成SpotBugs、Checkstyle、PMD
2. **测试覆盖率**：配置JaCoCo插件
3. **依赖管理**：定期更新依赖，使用依赖检查插件
4. **构建优化**：配置并行构建和增量构建

### CI/CD配置

配置持续集成和持续部署流水线：

1. **自动构建**：每次提交触发构建
2. **自动测试**：运行单元测试和集成测试
3. **代码质量检查**：运行静态分析工具
4. **安全扫描**：运行依赖漏洞扫描

## 代码审查

### 审查清单

1. **代码风格**：是否符合项目代码规范
2. **设计模式**：是否使用了合适的设计模式
3. **异常处理**：是否正确处理异常
4. **性能**：是否存在性能问题
5. **安全性**：是否存在安全漏洞
6. **测试**：是否有足够的测试覆盖

### 审查流程

1. **提交PR**：创建Pull Request
2. **自动检查**：运行自动检查和测试
3. **人工审查**：至少一人进行代码审查
4. **修改**：根据审查意见修改代码
5. **合并**：审查通过后合并代码

## 总结

遵循这些代码规范和最佳实践，将有助于：

1. **提高代码质量**：减少bug，提高可读性
2. **增强可维护性**：便于后续维护和扩展
3. **提升开发效率**：减少不必要的沟通和修改
4. **保证系统稳定性**：减少生产环境问题

所有团队成员都应熟悉并遵循这些规范，共同维护高质量的代码库。