package com.midscene.core.ai;

import com.midscene.core.model.UiElement;
import com.midscene.core.model.UINode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 默认AI助手实现类
 */
public class DefaultAIAssistant extends AIAssistant {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAIAssistant.class);
    
    @Override
    public CompletableFuture<String> analyzeUI(String prompt, List<UiElement> uiElements) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Analyzing UI with prompt: {}", prompt);
            
            // 模拟AI分析，实际项目中应调用真实的AI模型
            String elementsSummary = uiElements.stream()
                .map(e -> String.format("%s (type: %s)", e.getContent(), e.getNodeType()))
                .collect(Collectors.joining(", "));
                
            return String.format("Analyzed UI with %d elements: %s\nAnalysis based on prompt: %s", 
                uiElements.size(), elementsSummary, prompt);
        });
    }

    @Override
    public CompletableFuture<String> planSteps(String request, String uiContext) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Planning steps for request: {}", request);
            
            // 模拟步骤规划，实际应调用AI模型
            // 这里返回一个简单的执行计划示例
            return "1. Locate login button and click it\n" +
                   "2. Find username input field and enter credentials\n" +
                   "3. Find password input field and enter password\n" +
                   "4. Click submit button to login\n" +
                   "5. Verify successful login by checking for dashboard element";
        });
    }

    @Override
    public CompletableFuture<Integer> locateElement(String condition, List<UiElement> uiElements) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Locating element with condition: {}", condition);
            
            // 简单的模拟实现，实际应使用AI模型进行智能定位
            for (int i = 0; i < uiElements.size(); i++) {
                UiElement element = uiElements.get(i);
                // 基本的文本匹配逻辑
                if (element.getContent() != null && 
                    element.getContent().toLowerCase().contains(condition.toLowerCase())) {
                    logger.info("Element found at index: {}", i);
                    return i;
                }
            }
            
            // 默认返回第一个可见元素作为演示
            for (int i = 0; i < uiElements.size(); i++) {
                if (uiElements.get(i).isVisible()) {
                    logger.warn("No exact match found, returning first visible element at index: {}", i);
                    return i;
                }
            }
            
            // 如果没有元素，返回-1
            logger.error("No elements found");
            return -1;
        });
    }

    @Override
    public CompletableFuture<String> extractData(String query, UINode uiNode) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Extracting data with query: {}", query);
            
            // 模拟数据提取，实际应使用AI模型
            return String.format("Extracted data based on query: %s\nFrom UI node: %s", query, uiNode);
        });
    }

    @Override
    public CompletableFuture<Boolean> verifyCondition(String condition, List<UiElement> uiElements) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Verifying condition: {}", condition);
            
            // 模拟条件验证，实际应使用AI模型
            // 简单的逻辑：如果有任何元素包含条件文本，则认为验证通过
            for (UiElement element : uiElements) {
                if (element.getContent() != null && 
                    element.getContent().toLowerCase().contains(condition.toLowerCase())) {
                    logger.info("Condition verified successfully");
                    return true;
                }
            }
            
            // 默认返回true用于演示目的
            logger.warn("Condition not explicitly verified, returning default true for demo");
            return true;
        });
    }

    @Override
    public CompletableFuture<String> handleException(String error, String context) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Handling exception: {}, context: {}", error, context);
            
            // 模拟异常处理，实际应使用AI模型生成智能建议
            return String.format("Error: %s\nContext: %s\nRecommended Action: Check element visibility and retry operation.", 
                error, context);
        });
    }
}