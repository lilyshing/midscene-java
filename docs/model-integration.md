# Midscene Java 模型集成方案

## 1. 原项目模型支持分析

### 1.1 支持的模型

原项目 `midscene.js` 支持多种多模态大语言模型，主要包括：<mcreference link="https://juejin.cn/post/7462264897654898715" index="1">1</mcreference>

1. **GPT-4o**: OpenAI的多模态模型，支持图像和文本输入
2. **UI-TARS**: 字节跳动开源的UI自动化专用模型
3. **Qwen2.5-VL**: 阿里云通义千问的多模态版本

### 1.2 模型使用方式

原项目通过统一的接口调用不同模型，用户可以配置使用哪种模型：

```javascript
// 配置GPT-4o
const modelConfig = {
  model: "gpt-4o",
  apiKey: "your-openai-api-key",
  baseUrl: "https://api.openai.com/v1"
};

// 配置UI-TARS
const modelConfig = {
  model: "ui-tars",
  apiKey: "your-ui-tars-api-key",
  baseUrl: "https://your-ui-tars-endpoint"
};

// 配置Qwen2.5-VL
const modelConfig = {
  model: "qwen2.5-vl",
  apiKey: "your-dashscope-api-key",
  baseUrl: "https://dashscope.aliyuncs.com/compatible-mode/v1"
};
```

## 2. Java模型集成设计

### 2.1 模型抽象层设计

```java
package com.midscene.core.ai;

import com.midscene.core.model.AIRequest;
import com.midscene.core.model.AIResponse;
import com.midscene.core.model.AIModelConfig;

/**
 * AI模型服务抽象接口
 */
public interface AIModelService {
    /**
     * 执行AI推理
     * @param request AI请求
     * @return AI响应
     * @throws AIModelException 推理失败时抛出异常
     */
    AIResponse infer(AIRequest request) throws AIModelException;
    
    /**
     * 异步执行AI推理
     * @param request AI请求
     * @return 异步AI响应
     */
    CompletableFuture<AIResponse> inferAsync(AIRequest request);
    
    /**
     * 获取模型配置
     * @return 模型配置
     */
    AIModelConfig getConfig();
    
    /**
     * 检查模型是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 关闭模型服务，释放资源
     */
    void close();
}
```

### 2.2 模型配置设计

```java
package com.midscene.core.model;

/**
 * AI模型配置基类
 */
public abstract class AIModelConfig {
    protected String model;
    protected String apiKey;
    protected String baseUrl;
    protected int timeout = 30000; // 默认30秒超时
    protected int maxRetries = 3; // 默认重试3次
    
    // getter和setter方法
    
    /**
     * 构建器基类
     */
    public abstract static class Builder<T extends AIModelConfig, B extends Builder<T, B>> {
        protected T config;
        
        protected abstract T getConfig();
        protected abstract B self();
        
        public B model(String model) {
            config.model = model;
            return self();
        }
        
        public B apiKey(String apiKey) {
            config.apiKey = apiKey;
            return self();
        }
        
        public B baseUrl(String baseUrl) {
            config.baseUrl = baseUrl;
            return self();
        }
        
        public B timeout(int timeout) {
            config.timeout = timeout;
            return self();
        }
        
        public B maxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return self();
        }
        
        public abstract T build();
    }
}
```

### 2.3 AI请求和响应模型

```java
package com.midscene.core.model;

import java.util.List;
import java.util.Map;

/**
 * AI请求模型
 */
public class AIRequest {
    private String instruction; // 自然语言指令
    private List<Content> contents; // 多模态内容（文本、图像等）
    private Map<String, Object> context; // 上下文信息
    private Map<String, Object> parameters; // 模型特定参数
    
    // 构造函数、getter和setter方法
    
    /**
     * 内容模型
     */
    public static class Content {
        private String type; // "text" 或 "image"
        private String text; // 文本内容
        private String image; // 图像URL或Base64编码
        private Map<String, Object> metadata; // 元数据
        
        // 构造函数、getter和setter方法
    }
    
    /**
     * 构建器
     */
    public static class Builder {
        private AIRequest request = new AIRequest();
        
        public Builder instruction(String instruction) {
            request.instruction = instruction;
            return this;
        }
        
        public Builder addTextContent(String text) {
            if (request.contents == null) {
                request.contents = new ArrayList<>();
            }
            Content content = new Content();
            content.setType("text");
            content.setText(text);
            request.contents.add(content);
            return this;
        }
        
        public Builder addImageContent(String image) {
            if (request.contents == null) {
                request.contents = new ArrayList<>();
            }
            Content content = new Content();
            content.setType("image");
            content.setImage(image);
            request.contents.add(content);
            return this;
        }
        
        public Builder context(Map<String, Object> context) {
            request.context = context;
            return this;
        }
        
        public Builder parameter(String key, Object value) {
            if (request.parameters == null) {
                request.parameters = new HashMap<>();
            }
            request.parameters.put(key, value);
            return this;
        }
        
        public AIRequest build() {
            return request;
        }
    }
}

/**
 * AI响应模型
 */
public class AIResponse {
    private String content; // 响应内容
    private Map<String, Object> data; // 结构化数据
    private boolean success; // 是否成功
    private String errorMessage; // 错误信息
    private Map<String, Object> metadata; // 元数据
    private int tokensUsed; // 使用的token数量
    
    // 构造函数、getter和setter方法
    
    /**
     * 构建器
     */
    public static class Builder {
        private AIResponse response = new AIResponse();
        
        public Builder content(String content) {
            response.content = content;
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            response.data = data;
            return this;
        }
        
        public Builder success(boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            response.errorMessage = errorMessage;
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            if (response.metadata == null) {
                response.metadata = new HashMap<>();
            }
            response.metadata.put(key, value);
            return this;
        }
        
        public Builder tokensUsed(int tokensUsed) {
            response.tokensUsed = tokensUsed;
            return this;
        }
        
        public AIResponse build() {
            return response;
        }
    }
}
```

## 3. GPT-4o模型集成

### 3.1 GPT-4o配置

```java
package com.midscene.core.ai.gpt4o;

import com.midscene.core.model.AIModelConfig;

/**
 * GPT-4o模型配置
 */
public class GPT4oConfig extends AIModelConfig {
    private static final String DEFAULT_MODEL = "gpt-4o";
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    
    private double temperature = 0.7; // 默认温度
    private int maxTokens = 4096; // 默认最大token数
    private double topP = 1.0; // 默认top_p
    
    // 构造函数
    public GPT4oConfig() {
        super.model = DEFAULT_MODEL;
        super.baseUrl = DEFAULT_BASE_URL;
    }
    
    // getter和setter方法
    
    /**
     * GPT-4o配置构建器
     */
    public static class Builder extends AIModelConfig.Builder<GPT4oConfig, Builder> {
        private GPT4oConfig config = new GPT4oConfig();
        
        @Override
        protected GPT4oConfig getConfig() {
            return config;
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public Builder temperature(double temperature) {
            config.temperature = temperature;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            config.maxTokens = maxTokens;
            return this;
        }
        
        public Builder topP(double topP) {
            config.topP = topP;
            return this;
        }
        
        @Override
        public GPT4oConfig build() {
            return config;
        }
    }
}
```

### 3.2 GPT-4o服务实现

```java
package com.midscene.core.ai.gpt4o;

import com.midscene.core.ai.AIModelService;
import com.midscene.core.model.AIRequest;
import com.midscene.core.model.AIResponse;
import com.midscene.core.model.AIModelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * GPT-4o模型服务实现
 */
public class GPT4oModelService implements AIModelService {
    private final GPT4oConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    
    public GPT4oModelService(GPT4oConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
        this.executor = CompletableFuture::new;
    }
    
    @Override
    public AIResponse infer(AIRequest request) throws AIModelException {
        try {
            // 构建OpenAI API请求
            Map<String, Object> requestBody = buildOpenAIRequest(request);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.parse("application/json")
            );
            
            Request httpRequest = new Request.Builder()
                .url(config.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new AIModelException("GPT-4o API request failed: " + response.code());
                }
                
                // 解析响应
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                return parseOpenAIResponse(responseMap);
            }
        } catch (IOException e) {
            throw new AIModelException("Failed to call GPT-4o API", e);
        }
    }
    
    @Override
    public CompletableFuture<AIResponse> inferAsync(AIRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return infer(request);
            } catch (AIModelException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    @Override
    public GPT4oConfig getConfig() {
        return config;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 简单的健康检查
            Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/models")
                .header("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void close() {
        // 清理资源
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
    
    /**
     * 构建OpenAI API请求
     */
    private Map<String, Object> buildOpenAIRequest(AIRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        requestBody.put("top_p", config.getTopP());
        
        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful AI assistant for UI automation.");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 构建多模态内容
        List<Map<String, Object>> content = new ArrayList<>();
        if (request.getContents() != null) {
            for (AIRequest.Content c : request.getContents()) {
                Map<String, Object> contentItem = new HashMap<>();
                if ("text".equals(c.getType())) {
                    contentItem.put("type", "text");
                    contentItem.put("text", c.getText());
                } else if ("image".equals(c.getType())) {
                    contentItem.put("type", "image_url");
                    Map<String, String> imageUrl = new HashMap<>();
                    imageUrl.put("url", c.getImage());
                    contentItem.put("image_url", imageUrl);
                }
                content.add(contentItem);
            }
        } else {
            // 如果没有指定内容，使用指令作为文本内容
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", request.getInstruction());
            content.add(textContent);
        }
        
        userMessage.put("content", content);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        // 添加其他参数
        if (request.getParameters() != null) {
            requestBody.putAll(request.getParameters());
        }
        
        return requestBody;
    }
    
    /**
     * 解析OpenAI API响应
     */
    private AIResponse parseOpenAIResponse(Map<String, Object> responseMap) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new AIResponse.Builder()
                .success(false)
                .errorMessage("No choices in response")
                .build();
        }
        
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        String content = (String) message.get("content");
        
        Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
        int tokensUsed = 0;
        if (usage != null) {
            tokensUsed = (Integer) usage.get("total_tokens");
        }
        
        return new AIResponse.Builder()
            .content(content)
            .success(true)
            .tokensUsed(tokensUsed)
            .metadata("model", responseMap.get("model"))
            .build();
    }
}
```

## 4. UI-TARS模型集成

### 4.1 UI-TARS配置

```java
package com.midscene.core.ai.uitars;

import com.midscene.core.model.AIModelConfig;

/**
 * UI-TARS模型配置
 */
public class UITarsConfig extends AIModelConfig {
    private static final String DEFAULT_MODEL = "ui-tars";
    
    private String version = "latest"; // 模型版本
    private boolean enableVision = true; // 是否启用视觉能力
    
    // 构造函数
    public UITarsConfig() {
        super.model = DEFAULT_MODEL;
    }
    
    // getter和setter方法
    
    /**
     * UI-TARS配置构建器
     */
    public static class Builder extends AIModelConfig.Builder<UITarsConfig, Builder> {
        private UITarsConfig config = new UITarsConfig();
        
        @Override
        protected UITarsConfig getConfig() {
            return config;
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public Builder version(String version) {
            config.version = version;
            return this;
        }
        
        public Builder enableVision(boolean enableVision) {
            config.enableVision = enableVision;
            return this;
        }
        
        @Override
        public UITarsConfig build() {
            return config;
        }
    }
}
```

### 4.2 UI-TARS服务实现

```java
package com.midscene.core.ai.uitars;

import com.midscene.core.ai.AIModelService;
import com.midscene.core.model.AIRequest;
import com.midscene.core.model.AIResponse;
import com.midscene.core.model.AIModelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * UI-TARS模型服务实现
 */
public class UITarsModelService implements AIModelService {
    private final UITarsConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    
    public UITarsModelService(UITarsConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
        this.executor = CompletableFuture::new;
    }
    
    @Override
    public AIResponse infer(AIRequest request) throws AIModelException {
        try {
            // 构建UI-TARS API请求
            Map<String, Object> requestBody = buildUITarsRequest(request);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.parse("application/json")
            );
            
            Request httpRequest = new Request.Builder()
                .url(config.getBaseUrl() + "/api/v1/infer")
                .header("Authorization", "Bearer " + config.getApiKey())
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new AIModelException("UI-TARS API request failed: " + response.code());
                }
                
                // 解析响应
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                return parseUITarsResponse(responseMap);
            }
        } catch (IOException e) {
            throw new AIModelException("Failed to call UI-TARS API", e);
        }
    }
    
    @Override
    public CompletableFuture<AIResponse> inferAsync(AIRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return infer(request);
            } catch (AIModelException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    @Override
    public UITarsConfig getConfig() {
        return config;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 简单的健康检查
            Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/api/v1/health")
                .header("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void close() {
        // 清理资源
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
    
    /**
     * 构建UI-TARS API请求
     */
    private Map<String, Object> buildUITarsRequest(AIRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("version", config.getVersion());
        requestBody.put("enable_vision", config.isEnableVision());
        
        // 指令
        requestBody.put("instruction", request.getInstruction());
        
        // 多模态内容
        if (request.getContents() != null) {
            List<Map<String, Object>> contents = new ArrayList<>();
            for (AIRequest.Content c : request.getContents()) {
                Map<String, Object> content = new HashMap<>();
                content.put("type", c.getType());
                if ("text".equals(c.getType())) {
                    content.put("text", c.getText());
                } else if ("image".equals(c.getType())) {
                    content.put("image", c.getImage());
                }
                contents.add(content);
            }
            requestBody.put("contents", contents);
        }
        
        // 上下文
        if (request.getContext() != null) {
            requestBody.put("context", request.getContext());
        }
        
        // 其他参数
        if (request.getParameters() != null) {
            requestBody.putAll(request.getParameters());
        }
        
        return requestBody;
    }
    
    /**
     * 解析UI-TARS API响应
     */
    private AIResponse parseUITarsResponse(Map<String, Object> responseMap) {
        Boolean success = (Boolean) responseMap.get("success");
        if (success == null || !success) {
            return new AIResponse.Builder()
                .success(false)
                .errorMessage((String) responseMap.get("error"))
                .build();
        }
        
        String content = (String) responseMap.get("response");
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        
        return new AIResponse.Builder()
            .content(content)
            .data(data)
            .success(true)
            .metadata("model", config.getModel())
            .metadata("version", config.getVersion())
            .build();
    }
}
```

## 5. Qwen2.5-VL模型集成

### 5.1 Qwen2.5-VL配置

```java
package com.midscene.core.ai.qwen;

import com.midscene.core.model.AIModelConfig;

/**
 * Qwen2.5-VL模型配置
 */
public class QwenConfig extends AIModelConfig {
    private static final String DEFAULT_MODEL = "qwen2.5-vl";
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    
    private double temperature = 0.7; // 默认温度
    private int maxTokens = 4096; // 默认最大token数
    private double topP = 1.0; // 默认top_p
    
    // 构造函数
    public QwenConfig() {
        super.model = DEFAULT_MODEL;
        super.baseUrl = DEFAULT_BASE_URL;
    }
    
    // getter和setter方法
    
    /**
     * Qwen配置构建器
     */
    public static class Builder extends AIModelConfig.Builder<QwenConfig, Builder> {
        private QwenConfig config = new QwenConfig();
        
        @Override
        protected QwenConfig getConfig() {
            return config;
        }
        
        @Override
        protected Builder self() {
            return this;
        }
        
        public Builder temperature(double temperature) {
            config.temperature = temperature;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            config.maxTokens = maxTokens;
            return this;
        }
        
        public Builder topP(double topP) {
            config.topP = topP;
            return this;
        }
        
        @Override
        public QwenConfig build() {
            return config;
        }
    }
}
```

### 5.2 Qwen2.5-VL服务实现

```java
package com.midscene.core.ai.qwen;

import com.midscene.core.ai.AIModelService;
import com.midscene.core.model.AIRequest;
import com.midscene.core.model.AIResponse;
import com.midscene.core.model.AIModelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Qwen2.5-VL模型服务实现
 */
public class QwenModelService implements AIModelService {
    private final QwenConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    
    public QwenModelService(QwenConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .build();
        this.objectMapper = new ObjectMapper();
        this.executor = CompletableFuture::new;
    }
    
    @Override
    public AIResponse infer(AIRequest request) throws AIModelException {
        try {
            // 构建Qwen API请求
            Map<String, Object> requestBody = buildQwenRequest(request);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.parse("application/json")
            );
            
            Request httpRequest = new Request.Builder()
                .url(config.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new AIModelException("Qwen API request failed: " + response.code());
                }
                
                // 解析响应
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                return parseQwenResponse(responseMap);
            }
        } catch (IOException e) {
            throw new AIModelException("Failed to call Qwen API", e);
        }
    }
    
    @Override
    public CompletableFuture<AIResponse> inferAsync(AIRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return infer(request);
            } catch (AIModelException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    @Override
    public QwenConfig getConfig() {
        return config;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 简单的健康检查
            Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/models")
                .header("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void close() {
        // 清理资源
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
    
    /**
     * 构建Qwen API请求
     */
    private Map<String, Object> buildQwenRequest(AIRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        requestBody.put("top_p", config.getTopP());
        
        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful AI assistant for UI automation.");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 构建多模态内容
        List<Map<String, Object>> content = new ArrayList<>();
        if (request.getContents() != null) {
            for (AIRequest.Content c : request.getContents()) {
                Map<String, Object> contentItem = new HashMap<>();
                if ("text".equals(c.getType())) {
                    contentItem.put("type", "text");
                    contentItem.put("text", c.getText());
                } else if ("image".equals(c.getType())) {
                    contentItem.put("type", "image");
                    Map<String, String> image = new HashMap<>();
                    image.put("image", c.getImage());
                    contentItem.put("image", image);
                }
                content.add(contentItem);
            }
        } else {
            // 如果没有指定内容，使用指令作为文本内容
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", request.getInstruction());
            content.add(textContent);
        }
        
        userMessage.put("content", content);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        // 添加其他参数
        if (request.getParameters() != null) {
            requestBody.putAll(request.getParameters());
        }
        
        return requestBody;
    }
    
    /**
     * 解析Qwen API响应
     */
    private AIResponse parseQwenResponse(Map<String, Object> responseMap) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new AIResponse.Builder()
                .success(false)
                .errorMessage("No choices in response")
                .build();
        }
        
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        String content = (String) message.get("content");
        
        Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
        int tokensUsed = 0;
        if (usage != null) {
            tokensUsed = (Integer) usage.get("total_tokens");
        }
        
        return new AIResponse.Builder()
            .content(content)
            .success(true)
            .tokensUsed(tokensUsed)
            .metadata("model", responseMap.get("model"))
            .build();
    }
}
```

## 6. 模型工厂和自动选择

### 6.1 模型工厂

```java
package com.midscene.core.ai;

import com.midscene.core.ai.gpt4o.GPT4oConfig;
import com.midscene.core.ai.gpt4o.GPT4oModelService;
import com.midscene.core.ai.uitars.UITarsConfig;
import com.midscene.core.ai.uitars.UITarsModelService;
import com.midscene.core.ai.qwen.QwenConfig;
import com.midscene.core.ai.qwen.QwenModelService;
import com.midscene.core.model.AIModelConfig;

/**
 * AI模型服务工厂
 */
public class AIModelServiceFactory {
    
    /**
     * 根据配置创建AI模型服务
     * @param config 模型配置
     * @return AI模型服务
     */
    public static AIModelService create(AIModelConfig config) {
        if (config instanceof GPT4oConfig) {
            return new GPT4oModelService((GPT4oConfig) config);
        } else if (config instanceof UITarsConfig) {
            return new UITarsModelService((UITarsConfig) config);
        } else if (config instanceof QwenConfig) {
            return new QwenModelService((QwenConfig) config);
        } else {
            throw new IllegalArgumentException("Unsupported model config: " + config.getClass());
        }
    }
    
    /**
     * 根据模型名称创建默认配置的AI模型服务
     * @param modelName 模型名称
     * @param apiKey API密钥
     * @return AI模型服务
     */
    public static AIModelService create(String modelName, String apiKey) {
        switch (modelName.toLowerCase()) {
            case "gpt-4o":
                GPT4oConfig gpt4oConfig = new GPT4oConfig.Builder()
                    .apiKey(apiKey)
                    .build();
                return new GPT4oModelService(gpt4oConfig);
                
            case "ui-tars":
                UITarsConfig uiTarsConfig = new UITarsConfig.Builder()
                    .apiKey(apiKey)
                    .build();
                return new UITarsModelService(uiTarsConfig);
                
            case "qwen2.5-vl":
                QwenConfig qwenConfig = new QwenConfig.Builder()
                    .apiKey(apiKey)
                    .build();
                return new QwenModelService(qwenConfig);
                
            default:
                throw new IllegalArgumentException("Unsupported model name: " + modelName);
        }
    }
}
```

### 6.2 模型自动选择

```java
package com.midscene.core.ai;

import com.midscene.core.ai.gpt4o.GPT4oModelService;
import com.midscene.core.ai.uitars.UITarsModelService;
import com.midscene.core.ai.qwen.QwenModelService;
import java.util.List;

/**
 * AI模型自动选择器
 */
public class AIModelSelector {
    
    /**
     * 根据任务类型自动选择最适合的模型
     * @param taskType 任务类型
     * @param availableModels 可用模型列表
     * @return 最适合的模型
     */
    public static AIModelService selectModel(String taskType, List<AIModelService> availableModels) {
        if (availableModels == null || availableModels.isEmpty()) {
            throw new IllegalArgumentException("No available models");
        }
        
        // 优先选择UI-TARS用于UI自动化任务
        if ("ui-automation".equals(taskType)) {
            for (AIModelService model : availableModels) {
                if (model instanceof UITarsModelService && model.isAvailable()) {
                    return model;
                }
            }
        }
        
        // 优先选择GPT-4o用于复杂推理任务
        if ("complex-reasoning".equals(taskType)) {
            for (AIModelService model : availableModels) {
                if (model instanceof GPT4oModelService && model.isAvailable()) {
                    return model;
                }
            }
        }
        
        // 优先选择Qwen2.5-VL用于多模态任务
        if ("multimodal".equals(taskType)) {
            for (AIModelService model : availableModels) {
                if (model instanceof QwenModelService && model.isAvailable()) {
                    return model;
                }
            }
        }
        
        // 如果没有特定偏好，返回第一个可用的模型
        for (AIModelService model : availableModels) {
            if (model.isAvailable()) {
                return model;
            }
        }
        
        // 如果所有模型都不可用，返回第一个模型
        return availableModels.get(0);
    }
    
    /**
     * 根据成本效益自动选择模型
     * @param availableModels 可用模型列表
     * @return 最具成本效益的模型
     */
    public static AIModelService selectCostEffectiveModel(List<AIModelService> availableModels) {
        if (availableModels == null || availableModels.isEmpty()) {
            throw new IllegalArgumentException("No available models");
        }
        
        // 模型成本排序（从低到高）
        // 1. Qwen2.5-VL
        // 2. UI-TARS
        // 3. GPT-4o
        
        for (AIModelService model : availableModels) {
            if (model instanceof QwenModelService && model.isAvailable()) {
                return model;
            }
        }
        
        for (AIModelService model : availableModels) {
            if (model instanceof UITarsModelService && model.isAvailable()) {
                return model;
            }
        }
        
        for (AIModelService model : availableModels) {
            if (model instanceof GPT4oModelService && model.isAvailable()) {
                return model;
            }
        }
        
        // 如果所有模型都不可用，返回第一个模型
        return availableModels.get(0);
    }
}
```

## 7. 模型管理和监控

### 7.1 模型管理器

```java
package com.midscene.core.ai;

import com.midscene.core.model.AIModelException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AI模型管理器
 */
public class AIModelManager {
    private static AIModelManager instance;
    private final List<AIModelService> models;
    private final Map<String, AIModelService> modelMap;
    private AIModelService defaultModel;
    
    private AIModelManager() {
        this.models = new CopyOnWriteArrayList<>();
        this.modelMap = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     * @return 模型管理器实例
     */
    public static synchronized AIModelManager getInstance() {
        if (instance == null) {
            instance = new AIModelManager();
        }
        return instance;
    }
    
    /**
     * 注册模型
     * @param name 模型名称
     * @param model 模型服务
     */
    public void registerModel(String name, AIModelService model) {
        models.add(model);
        modelMap.put(name, model);
        
        // 如果是第一个模型，设为默认模型
        if (defaultModel == null) {
            defaultModel = model;
        }
    }
    
    /**
     * 获取模型
     * @param name 模型名称
     * @return 模型服务
     */
    public AIModelService getModel(String name) {
        return modelMap.get(name);
    }
    
    /**
     * 获取所有模型
     * @return 模型列表
     */
    public List<AIModelService> getAllModels() {
        return new CopyOnWriteArrayList<>(models);
    }
    
    /**
     * 获取可用模型
     * @return 可用模型列表
     */
    public List<AIModelService> getAvailableModels() {
        List<AIModelService> availableModels = new CopyOnWriteArrayList<>();
        for (AIModelService model : models) {
            if (model.isAvailable()) {
                availableModels.add(model);
            }
        }
        return availableModels;
    }
    
    /**
     * 设置默认模型
     * @param name 模型名称
     */
    public void setDefaultModel(String name) {
        AIModelService model = modelMap.get(name);
        if (model != null) {
            defaultModel = model;
        } else {
            throw new IllegalArgumentException("Model not found: " + name);
        }
    }
    
    /**
     * 获取默认模型
     * @return 默认模型
     */
    public AIModelService getDefaultModel() {
        return defaultModel;
    }
    
    /**
     * 注销模型
     * @param name 模型名称
     */
    public void unregisterModel(String name) {
        AIModelService model = modelMap.remove(name);
        if (model != null) {
            models.remove(model);
            model.close();
            
            // 如果注销的是默认模型，重新选择默认模型
            if (model == defaultModel && !models.isEmpty()) {
                defaultModel = models.get(0);
            }
        }
    }
    
    /**
     * 关闭所有模型
     */
    public void closeAllModels() {
        for (AIModelService model : models) {
            model.close();
        }
        models.clear();
        modelMap.clear();
        defaultModel = null;
    }
}
```

### 7.2 模型监控

```java
package com.midscene.core.ai;

import com.midscene.core.model.AIRequest;
import com.midscene.core.model.AIResponse;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI模型监控器
 */
public class AIModelMonitor {
    private final AIModelService model;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong totalTokensUsed = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final ConcurrentHashMap<String, LocalDateTime> lastRequestTime = new ConcurrentHashMap<>();
    
    public AIModelMonitor(AIModelService model) {
        this.model = model;
    }
    
    /**
     * 监控请求
     * @param request 请求
     * @param response 响应
     * @param responseTime 响应时间（毫秒）
     */
    public void recordRequest(AIRequest request, AIResponse response, long responseTime) {
        requestCount.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        
        if (response.isSuccess()) {
            successCount.incrementAndGet();
            totalTokensUsed.addAndGet(response.getTokensUsed());
        } else {
            failureCount.incrementAndGet();
        }
        
        lastRequestTime.put("last", LocalDateTime.now());
    }
    
    /**
     * 获取请求总数
     * @return 请求总数
     */
    public int getRequestCount() {
        return requestCount.get();
    }
    
    /**
     * 获取成功请求数
     * @return 成功请求数
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * 获取失败请求数
     * @return 失败请求数
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * 获取成功率
     * @return 成功率（0-1之间）
     */
    public double getSuccessRate() {
        int total = requestCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successCount.get() / total;
    }
    
    /**
     * 获取总token使用量
     * @return 总token使用量
     */
    public long getTotalTokensUsed() {
        return totalTokensUsed.get();
    }
    
    /**
     * 获取平均响应时间
     * @return 平均响应时间（毫秒）
     */
    public double getAverageResponseTime() {
        int total = requestCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalResponseTime.get() / total;
    }
    
    /**
     * 获取最后请求时间
     * @return 最后请求时间
     */
    public LocalDateTime getLastRequestTime() {
        return lastRequestTime.get("last");
    }
    
    /**
     * 获取模型状态
     * @return 模型状态
     */
    public ModelStatus getStatus() {
        if (model.isAvailable()) {
            return ModelStatus.AVAILABLE;
        } else {
            return ModelStatus.UNAVAILABLE;
        }
    }
    
    /**
     * 模型状态枚举
     */
    public enum ModelStatus {
        AVAILABLE,    // 可用
        UNAVAILABLE   // 不可用
    }
}
```

## 8. 实施计划

### 8.1 第一阶段：模型抽象层实现 (1周)

1. 实现AIModelService接口
2. 实现AIRequest和AIResponse模型
3. 实现AIModelConfig基类
4. 编写单元测试

### 8.2 第二阶段：GPT-4o模型集成 (1周)

1. 实现GPT4oConfig配置类
2. 实现GPT4oModelService服务类
3. 实现OpenAI API调用
4. 编写集成测试

### 8.3 第三阶段：UI-TARS模型集成 (1周)

1. 实现UITarsConfig配置类
2. 实现UITarsModelService服务类
3. 实现UI-TARS API调用
4. 编写集成测试

### 8.4 第四阶段：Qwen2.5-VL模型集成 (1周)

1. 实现QwenConfig配置类
2. 实现QwenModelService服务类
3. 实现Qwen API调用
4. 编写集成测试

### 8.5 第五阶段：模型管理和监控 (1周)

1. 实现AIModelServiceFactory工厂类
2. 实现AIModelSelector选择器
3. 实现AIModelManager管理器
4. 实现AIModelMonitor监控器
5. 编写集成测试

## 9. 总结

通过本方案，我们将确保Midscene Java版本能够支持与原项目相同的多模态模型，包括GPT-4o、UI-TARS和Qwen2.5-VL。这将使Java版本能够充分利用这些强大的AI模型，提供与原项目相同的UI自动化能力。同时，通过模型管理和监控功能，我们可以确保模型的稳定运行和高效使用。