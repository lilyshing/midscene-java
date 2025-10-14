package com.midscene.core.ai;

import com.google.gson.Gson;
import java.util.concurrent.CompletableFuture;
import com.google.gson.GsonBuilder;
import com.midscene.core.exception.MidsceneException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.GsonBuilder;

/**
 * OpenAI模型提供商实现
 */
public class OpenAIModelProvider implements ModelProvider {
    private static final String API_URL = "https://api.openai.com/v1";
    private String apiKey;
    private String model = "gpt-3.5-turbo";
    private int timeout = 30; // 默认超时时间为30秒
    private HttpClient httpClient;
    private Gson gson;
    
    public OpenAIModelProvider() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().create();
    }
    
    @Override
    public boolean initialize(Map<String, Object> config) {
        try {
            if (config == null) {
                return false;
            }
            
            // 初始化API密钥
            if (config.containsKey("apiKey")) {
                this.apiKey = (String) config.get("apiKey");
            }
            
            // 初始化模型名称
            if (config.containsKey("model")) {
                this.model = (String) config.get("model");
            }
            
            // 初始化超时时间
            if (config.containsKey("timeout")) {
                this.timeout = (Integer) config.get("timeout");
            }
            
            // 初始化HttpClient
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout))
                    .build();
                    
            return true;
        } catch (Exception e) {
            throw new MidsceneException("Failed to initialize OpenAI model provider: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getProviderName() {
        return "openai";    
    }
    
    @Override
    public CompletableFuture<String> generateText(String prompt, Map<String, Object> options) {
        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", options.getOrDefault("model", model));
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", options.getOrDefault("maxTokens", 1000));
        requestBody.put("temperature", options.getOrDefault("temperature", 0.7));
        
        // 发送请求
        return sendRequestAsync("/completions", requestBody)
                .thenApply(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
                        if (responseMap.containsKey("choices") && !responseMap.get("choices").equals(null)) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                            if (!choices.isEmpty() && choices.get(0).containsKey("text")) {
                                return (String) choices.get(0).get("text");
                            }
                        }
                        throw new MidsceneException("Invalid response from OpenAI API");
                    } catch (Exception e) {
                        throw new MidsceneException("Failed to parse OpenAI response: " + e.getMessage(), e);
                    }
                });
    }
    
    @Override
    public CompletableFuture<String> chat(List<Map<String, String>> messages, Map<String, Object> options) {
        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", options.getOrDefault("model", model));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", options.getOrDefault("maxTokens", 1000));
        requestBody.put("temperature", options.getOrDefault("temperature", 0.7));
        
        // 发送请求
        return sendRequestAsync("/chat/completions", requestBody)
                .thenApply(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
                        if (responseMap.containsKey("choices") && !responseMap.get("choices").equals(null)) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                            if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                                if (message.containsKey("content")) {
                                    return message.get("content");
                                }
                            }
                        }
                        throw new MidsceneException("Invalid response from OpenAI API");
                    } catch (Exception e) {
                        throw new MidsceneException("Failed to parse OpenAI response: " + e.getMessage(), e);
                    }
                });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> analyzeImage(String imageBase64, String prompt, Map<String, Object> options) {
        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", options.getOrDefault("model", "gpt-4-vision-preview"));
        
        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        List<Map<String, String>> content = new java.util.ArrayList<>();
        Map<String, String> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);
        
        Map<String, String> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", "data:image/jpeg;base64," + imageBase64);
        content.add(imageContent);
        
        message.put("content", content);
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", options.getOrDefault("maxTokens", 1000));
        
        // 发送请求
        return sendRequestAsync("/chat/completions", requestBody)
                .thenApply(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
                        Map<String, Object> result = new HashMap<>();
                        
                        if (responseMap.containsKey("choices") && !responseMap.get("choices").equals(null)) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                            if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> messageResponse = (Map<String, String>) choices.get(0).get("message");
                                if (messageResponse.containsKey("content")) {
                                    result.put("description", messageResponse.get("content"));
                                }
                            }
                        }
                        
                        return result;
                    } catch (Exception e) {
                        throw new MidsceneException("Failed to parse OpenAI response: " + e.getMessage(), e);
                    }
                });
    }
    
    @Override
    public CompletableFuture<String> extractInformation(String context, String query) {
        String prompt = String.format("Context:\n%s\n\nQuery: %s\n\nPlease extract the information from the context that answers the query.", context, query);
        
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        return chat(messages, new HashMap<>());
    }
    
    @Override
    public void shutdown() {
        // 清理资源
        this.httpClient = null;
        this.gson = null;
    }
    
    /**
     * 异步发送HTTP请求到OpenAI API
     */
    private CompletableFuture<String> sendRequestAsync(String endpoint, Map<String, Object> requestBody) {
        try {
            String jsonBody = gson.toJson(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(timeout))
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new MidsceneException("OpenAI API request failed with status code: " + 
                                    response.statusCode() + ", response: " + response.body());
                        }
                        return response.body();
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new MidsceneException("Failed to send request to OpenAI API: " + 
                    e.getMessage(), e));
        }
    }
}