package com.midscene.core.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.midscene.core.model.UiElement;
import com.midscene.core.model.UINode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Google Gemini模型实现的AI助手
 */
public class GoogleGeminiAssistant extends AIAssistant {
    private static final Logger logger = LoggerFactory.getLogger(GoogleGeminiAssistant.class);
    private final OkHttpClient httpClient;
    private final AIModelConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleGeminiAssistant(AIModelConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public CompletableFuture<String> analyzeUI(String prompt, List<UiElement> uiElements) {
        // 将UI元素转换为结构化描述
        String uiDescription = buildUIDescription(uiElements);
        String fullPrompt = String.format("%s\n\nUI Context:\n%s", prompt, uiDescription);
        
        return sendGenerateContentRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<String> planSteps(String request, String uiContext) {
        String fullPrompt = String.format(
                "Based on the following user request and UI context, create a detailed step-by-step plan to accomplish the task:\n\nUser Request: %s\n\nUI Context: %s\n\nPlease provide the plan in a clear, structured format.",
                request, uiContext);
        
        return sendGenerateContentRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<Integer> locateElement(String condition, List<UiElement> uiElements) {
        String uiDescription = buildUIDescription(uiElements);
        String fullPrompt = String.format(
                "Based on the following condition, locate the index of the UI element that best matches:\n\nCondition: %s\n\nUI Elements:\n%s\n\nReturn only the zero-based index of the element as a number, or -1 if no element matches.",
                condition, uiDescription);
        
        return sendGenerateContentRequest(fullPrompt)
                .thenApply(response -> {
                    try {
                        return Integer.parseInt(response.trim());
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse element index from response: {}", response);
                        return -1;
                    }
                });
    }

    @Override
    public CompletableFuture<String> extractData(String query, UINode uiNode) {
        String nodeDescription = buildNodeDescription(uiNode);
        String fullPrompt = String.format(
                "Extract the following information from the UI node structure:\n\nQuery: %s\n\nUI Node:\n%s\n\nReturn only the requested data in a structured format.",
                query, nodeDescription);
        
        return sendGenerateContentRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<Boolean> verifyCondition(String condition, List<UiElement> uiElements) {
        String uiDescription = buildUIDescription(uiElements);
        String fullPrompt = String.format(
                "Verify if the following condition is true based on the UI elements:\n\nCondition: %s\n\nUI Elements:\n%s\n\nReturn only 'true' or 'false'.",
                condition, uiDescription);
        
        return sendGenerateContentRequest(fullPrompt)
                .thenApply(response -> Boolean.parseBoolean(response.trim().toLowerCase()));
    }

    @Override
    public CompletableFuture<String> handleException(String error, String context) {
        String fullPrompt = String.format(
                "An error occurred during task execution. Please provide guidance on how to handle it:\n\nError: %s\n\nContext: %s\n\nSuggest a solution or recovery strategy.",
                error, context);
        
        return sendGenerateContentRequest(fullPrompt);
    }

    /**
     * 发送生成内容请求到Google Gemini API
     */
    private CompletableFuture<String> sendGenerateContentRequest(String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            // 构建请求体
            String requestBody = buildRequestBody(prompt);
            
            // 创建请求
            String endpoint = config.getBaseUrl() != null ? config.getBaseUrl() : 
                String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", 
                config.getModelName() != null ? config.getModelName() : "gemini-1.5-flash");
                
            Request request = new Request.Builder()
                    .url(endpoint + "?key=" + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
            
            // 异步发送请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("Google Gemini API call failed", e);
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (response.isSuccessful() && responseBody != null) {
                            String responseString = responseBody.string();
                            if (config.isDebugMode()) {
                                logger.debug("Google Gemini API response: {}", responseString);
                            }
                            
                            JsonNode rootNode = objectMapper.readTree(responseString);
                            // 解析Gemini API响应格式
                            String assistantReply = rootNode.path("candidates").get(0)
                                    .path("content").path("parts").get(0)
                                    .path("text").asText();
                            
                            future.complete(assistantReply);
                        } else {
                            String errorMessage = responseBody != null ? responseBody.string() : "No response body";
                            logger.error("Google Gemini API returned error: {}, Status: {}", errorMessage, response.code());
                            future.completeExceptionally(new IOException("API call failed: " + errorMessage));
                        }
                    } catch (Exception e) {
                        logger.error("Failed to process Google Gemini API response", e);
                        future.completeExceptionally(e);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Failed to prepare Google Gemini API request", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * 构建API请求体
     */
    private String buildRequestBody(String prompt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        
        // 构建内容数组
        ObjectNode contentNode = mapper.createObjectNode();
        ArrayNode partsArray = mapper.createArrayNode();
        
        ObjectNode textPart = mapper.createObjectNode();
        textPart.put("text", prompt);
        partsArray.add(textPart);
        
        contentNode.set("parts", partsArray);
        contentNode.put("role", "user");
        
        ArrayNode contentsArray = mapper.createArrayNode();
        contentsArray.add(contentNode);
        
        requestBody.set("contents", contentsArray);
        
        // 添加生成参数
        ObjectNode generationConfig = mapper.createObjectNode();
        if (config.getTemperature() != null) {
            generationConfig.put("temperature", config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            generationConfig.put("maxOutputTokens", config.getMaxTokens());
        }
        
        requestBody.set("generationConfig", generationConfig);
        
        return toJsonString(requestBody);
    }
    
    /**
     * 将请求体转换为JSON字符串，并添加附加参数
     */
    private String toJsonString(JsonNode requestBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        // 添加附加参数
        if (config.getAdditionalParams() != null && !config.getAdditionalParams().isEmpty()) {
            ObjectMapper tempMapper = new ObjectMapper();
            try {
                String tempBody = tempMapper.writeValueAsString(requestBody);
                com.fasterxml.jackson.databind.node.ObjectNode mutableNode = (com.fasterxml.jackson.databind.node.ObjectNode) tempMapper.readTree(tempBody);
                
                for (java.util.Map.Entry<String, Object> entry : config.getAdditionalParams().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Number) {
                        mutableNode.put(entry.getKey(), ((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        mutableNode.put(entry.getKey(), (Boolean) value);
                    } else {
                        mutableNode.put(entry.getKey(), value.toString());
                    }
                }
                
                return tempMapper.writeValueAsString(mutableNode);
            } catch (Exception e) {
                logger.warn("Failed to add additional parameters: {}", e.getMessage());
                // 如果处理失败，返回原始请求体
                return mapper.writeValueAsString(requestBody);
            }
        }
        
        return mapper.writeValueAsString(requestBody);
    }

    /**
     * 构建UI元素的文本描述
     */
    private String buildUIDescription(List<UiElement> uiElements) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uiElements.size(); i++) {
            UiElement element = uiElements.get(i);
            sb.append(String.format("Element %d:\n", i));
            sb.append(String.format("  Type: %s\n", element.getNodeType()));
            sb.append(String.format("  Content: %s\n", element.getContent() != null ? element.getContent() : "<empty>"));
            sb.append(String.format("  Position: %s\n", element.getRect()));
            sb.append(String.format("  Visible: %s\n\n", element.isVisible()));
        }
        return sb.toString();
    }

    /**
     * 构建UI节点树的文本描述
     */
    private String buildNodeDescription(UINode uiNode) {
        StringBuilder sb = new StringBuilder();
        buildNodeDescriptionRecursive(uiNode, sb, 0);
        return sb.toString();
    }

    private void buildNodeDescriptionRecursive(UINode node, StringBuilder sb, int indentLevel) {
        String indent = String.join("", Collections.nCopies(indentLevel, "  "));
        sb.append(String.format("%sNode (Type: %s, Content: '%s')\n", 
                indent, node.getNodeType(), node.getContent()));
        
        for (UINode child : node.getChildren()) {
            buildNodeDescriptionRecursive(child, sb, indentLevel + 1);
        }
    }
}