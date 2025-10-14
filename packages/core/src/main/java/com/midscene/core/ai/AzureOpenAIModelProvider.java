package com.midscene.core.ai;

import com.midscene.core.exception.MidsceneException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Azure OpenAI模型提供商实现
 * 负责与Azure OpenAI API进行交互
 */
public class AzureOpenAIModelProvider implements ModelProvider {
    
    private static final String DEFAULT_API_VERSION = "2024-02-01";
    private static final String DEFAULT_API_KEY_HEADER = "api-key";
    
    private String endpoint;
    private String apiKey;
    private String deploymentName;
    private String apiVersion;
    private String apiKeyHeader;
    private HttpClient httpClient;
    private Gson gson;
    private int timeout = 60; // 默认超时时间（秒）
    
    public AzureOpenAIModelProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
        this.apiVersion = DEFAULT_API_VERSION;
        this.apiKeyHeader = DEFAULT_API_KEY_HEADER;
    }
    
    @Override
    public boolean initialize(Map<String, Object> config) {
        if (config == null) {
            return false;
        }
        
        // 从配置中获取Azure OpenAI API端点
        this.endpoint = (String) config.getOrDefault("endpoint", 
                config.getOrDefault("AZURE_OPENAI_ENDPOINT", ""));
        
        // 从配置中获取API密钥
        this.apiKey = (String) config.getOrDefault("apiKey", 
                config.getOrDefault("AZURE_OPENAI_KEY", ""));
        
        // 从配置中获取部署名称
        this.deploymentName = (String) config.getOrDefault("deploymentName", 
                config.getOrDefault("AZURE_OPENAI_DEPLOYMENT", ""));
        
        // 从配置中获取API版本
        this.apiVersion = (String) config.getOrDefault("apiVersion", 
                config.getOrDefault("AZURE_OPENAI_API_VERSION", DEFAULT_API_VERSION));
        
        // 从配置中获取超时时间
        Object timeoutObj = config.getOrDefault("timeout", 
                config.getOrDefault("MIDSCENE_TIMEOUT", timeout));
        if (timeoutObj instanceof Number) {
            this.timeout = ((Number) timeoutObj).intValue();
        }
        
        // 验证必要的配置
        return !endpoint.isEmpty() && !apiKey.isEmpty() && !deploymentName.isEmpty();
    }
    
    @Override
    public CompletableFuture<String> generateText(String prompt, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = new java.util.HashMap<>();
                requestBody.put("prompt", prompt);
                requestBody.put("max_tokens", options.getOrDefault("maxTokens", 2048));
                requestBody.put("temperature", options.getOrDefault("temperature", 0.7));
                
                // 添加额外的选项
                if (options.containsKey("stop")) {
                    requestBody.put("stop", options.get("stop"));
                }
                if (options.containsKey("top_p")) {
                    requestBody.put("top_p", options.get("top_p"));
                }
                
                String response = sendRequest("completions", requestBody);
                return parseResponse(response, "text");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate text with Azure OpenAI: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> chat(List<Map<String, String>> messages, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = new java.util.HashMap<>();
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", options.getOrDefault("maxTokens", 2048));
                requestBody.put("temperature", options.getOrDefault("temperature", 0.7));
                
                String response = sendRequest("chat/completions", requestBody);
                return parseChatResponse(response);
            } catch (Exception e) {
                throw new RuntimeException("Failed to chat with Azure OpenAI: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> analyzeImage(String imageBase64, String prompt, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Map<String, Object>> messages = new java.util.ArrayList<>();
                
                // 创建消息
                Map<String, Object> message = new java.util.HashMap<>();
                message.put("role", "user");
                
                List<Map<String, Object>> content = new java.util.ArrayList<>();
                
                // 添加文本部分
                Map<String, Object> textPart = new java.util.HashMap<>();
                textPart.put("type", "text");
                textPart.put("text", prompt);
                content.add(textPart);
                
                // 添加图像部分
                Map<String, Object> imagePart = new java.util.HashMap<>();
                imagePart.put("type", "image_url");
                
                Map<String, Object> imageUrl = new java.util.HashMap<>();
                imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
                imagePart.put("image_url", imageUrl);
                content.add(imagePart);
                
                message.put("content", content);
                messages.add(message);
                
                Map<String, Object> requestBody = new java.util.HashMap<>();
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", options.getOrDefault("maxTokens", 2048));
                
                String response = sendRequest("chat/completions", requestBody);
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("description", parseChatResponse(response));
                result.put("model", deploymentName);
                result.put("prompt", prompt);
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to analyze image with Azure OpenAI: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<String> extractInformation(String context, String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 使用chat API提取信息
                String prompt = String.format("Context: %s\n\nQuery: %s\n\nPlease extract the most relevant information from the context that answers the query.", context, query);
                
                List<Map<String, String>> messages = new java.util.ArrayList<>();
                Map<String, String> message = new java.util.HashMap<>();
                message.put("role", "user");
                message.put("content", prompt);
                messages.add(message);
                
                Map<String, Object> options = new java.util.HashMap<>();
                return chat(messages, options).join();
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract information with Azure OpenAI: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public void shutdown() {
        // 清理资源
        this.httpClient = null;
    }
    
    @Override
    public String getProviderName() {
        return "azure-openai";
    }
    
    /**
     * 发送HTTP请求到Azure OpenAI API
     */
    private String sendRequest(String path, Map<String, Object> requestBody) throws IOException, InterruptedException {
        String jsonBody = gson.toJson(requestBody);
        
        // 构建完整的URL
        String url = String.format("%s/openai/deployments/%s/%s?api-version=%s", 
                endpoint, deploymentName, path, apiVersion);
        
        // 创建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header(apiKeyHeader, apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(timeout))
                .build();
        
        // 发送请求并获取响应
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 检查响应状态
        if (response.statusCode() != 200) {
            throw new MidsceneException("Azure OpenAI API error: " + response.body());
        }
        
        return response.body();
    }
    
    /**
     * 解析文本生成响应
     */
    private String parseResponse(String responseJson, String type) {
        Map<String, Object> responseMap = gson.fromJson(responseJson, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            throw new MidsceneException("No choices in Azure OpenAI response");
        }
        
        Map<String, Object> firstChoice = choices.get(0);
        return (String) firstChoice.get(type);
    }
    
    /**
     * 解析聊天响应
     */
    private String parseChatResponse(String responseJson) {
        Map<String, Object> responseMap = gson.fromJson(responseJson, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            throw new MidsceneException("No choices in Azure OpenAI response");
        }
        
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        
        if (message == null || !message.containsKey("content")) {
            throw new MidsceneException("No message content in Azure OpenAI response");
        }
        
        return (String) message.get("content");
    }
}