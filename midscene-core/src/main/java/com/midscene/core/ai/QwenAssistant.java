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
 * Qwen模型实现的AI助手
 */
public class QwenAssistant extends AIAssistant {
    private static final Logger logger = LoggerFactory.getLogger(QwenAssistant.class);
    private final OkHttpClient httpClient;
    private final AIModelConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QwenAssistant(AIModelConfig config) {
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
        
        return sendChatCompletionRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<String> planSteps(String request, String uiContext) {
        String fullPrompt = String.format(
                "Based on the following user request and UI context, create a detailed step-by-step plan to accomplish the task:\n\nUser Request: %s\n\nUI Context: %s\n\nPlease provide the plan in a clear, structured format.",
                request, uiContext);
        
        return sendChatCompletionRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<Integer> locateElement(String condition, List<UiElement> uiElements) {
        String uiDescription = buildUIDescription(uiElements);
        String fullPrompt = String.format(
                "Based on the following condition, locate the index of the UI element that best matches:\n\nCondition: %s\n\nUI Elements:\n%s\n\nReturn only the zero-based index of the element as a number, or -1 if no element matches.",
                condition, uiDescription);
        
        return sendChatCompletionRequest(fullPrompt)
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
        
        return sendChatCompletionRequest(fullPrompt);
    }

    @Override
    public CompletableFuture<Boolean> verifyCondition(String condition, List<UiElement> uiElements) {
        String uiDescription = buildUIDescription(uiElements);
        String fullPrompt = String.format(
                "Verify if the following condition is true based on the UI elements:\n\nCondition: %s\n\nUI Elements:\n%s\n\nReturn only 'true' or 'false'.",
                condition, uiDescription);
        
        return sendChatCompletionRequest(fullPrompt)
                .thenApply(response -> Boolean.parseBoolean(response.trim().toLowerCase()));
    }

    @Override
    public CompletableFuture<String> handleException(String error, String context) {
        String fullPrompt = String.format(
                "An error occurred during task execution. Please provide guidance on how to handle it:\n\nError: %s\n\nContext: %s\n\nSuggest a solution or recovery strategy.",
                error, context);
        
        return sendChatCompletionRequest(fullPrompt);
    }

    /**
     * 发送对话完成请求到Qwen API
     */
    private CompletableFuture<String> sendChatCompletionRequest(String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            // 构建请求体
            String requestBody = buildRequestBody(prompt);
            
            // 创建请求
            String endpoint = config.getBaseUrl() != null ? config.getBaseUrl() : 
                "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
            
            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
            
            // 异步发送请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("Qwen API call failed", e);
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (response.isSuccessful() && responseBody != null) {
                            String responseString = responseBody.string();
                            if (config.isDebugMode()) {
                                logger.debug("Qwen API response: {}", responseString);
                            }
                            
                            JsonNode rootNode = objectMapper.readTree(responseString);
                            // 解析Qwen API响应格式
                            String assistantReply = rootNode.path("output").path("text").asText();
                            
                            future.complete(assistantReply);
                        } else {
                            String errorMessage = responseBody != null ? responseBody.string() : "No response body";
                            logger.error("Qwen API returned error: {}, Status: {}", errorMessage, response.code());
                            future.completeExceptionally(new IOException("API call failed: " + errorMessage));
                        }
                    } catch (Exception e) {
                        logger.error("Failed to process Qwen API response", e);
                        future.completeExceptionally(e);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Failed to prepare Qwen API request", e);
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
        
        // 设置模型名称
        String modelName = config.getModelName() != null ? config.getModelName() : "qwen-plus";
        requestBody.put("model", modelName);
        
        // 构建messages数组
        ArrayNode messagesArray = mapper.createArrayNode();
        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant specialized in UI interaction and analysis.");
        messagesArray.add(systemMessage);
        
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messagesArray.add(userMessage);
        
        requestBody.set("messages", messagesArray);
        
        // 添加参数
        ObjectNode parameters = mapper.createObjectNode();
        if (config.getTemperature() != null) {
            parameters.put("temperature", config.getTemperature());
        } else {
            parameters.put("temperature", 0.7);
        }
        
        if (config.getMaxTokens() != null) {
            parameters.put("max_tokens", config.getMaxTokens());
        }
        
        requestBody.set("parameters", parameters);
        
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
                
                // 检查是否已有parameters节点
                if (!mutableNode.has("parameters")) {
                    mutableNode.set("parameters", tempMapper.createObjectNode());
                }
                
                ObjectNode paramsNode = (ObjectNode) mutableNode.get("parameters");
                
                for (java.util.Map.Entry<String, Object> entry : config.getAdditionalParams().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Number) {
                        paramsNode.put(entry.getKey(), ((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        paramsNode.put(entry.getKey(), (Boolean) value);
                    } else {
                        paramsNode.put(entry.getKey(), value.toString());
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