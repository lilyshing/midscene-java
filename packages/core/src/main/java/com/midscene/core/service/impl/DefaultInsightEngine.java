package com.midscene.core.service.impl;

import com.midscene.core.service.AIModelService;
import com.midscene.core.exception.MidsceneException;
import com.midscene.core.service.InsightEngine;
import com.midscene.shared.platform.UiContext;
import com.midscene.shared.platform.UiElement;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 洞察引擎的默认实现类
 * 提供UI分析和元素识别功能
 * 包含优化的UI分析算法，支持分层分析、并行处理和缓存机制
 */
public class DefaultInsightEngine implements InsightEngine {
    private final AIModelService aiModelService;
    private final ConcurrentHashMap<String, CachedAnalysisResult> analysisCache;
    private final ScheduledExecutorService cacheCleanupService;
    private static final long CACHE_EXPIRY_TIME_MS = 30000; // 30秒缓存过期时间
    private static final int MAX_CACHE_SIZE = 100; // 最大缓存条目数
    private boolean isInitialized = false;
    
    public DefaultInsightEngine(AIModelService aiModelService) {
        this.aiModelService = aiModelService;
        this.analysisCache = new ConcurrentHashMap<>();
        this.cacheCleanupService = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * 缓存的分析结果
     */
    private static class CachedAnalysisResult {
        private final Map<String, Object> result;
        private final long timestamp;
        
        public CachedAnalysisResult(Map<String, Object> result) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME_MS;
        }
        
        public Map<String, Object> getResult() {
            return result;
        }
    }
    
    /**
     * 清理过期的缓存
     */
    private void cleanupExpiredCache() {
        analysisCache.forEach((key, value) -> {
            if (value.isExpired()) {
                analysisCache.remove(key);
            }
        });
        
        // 控制缓存大小
        if (analysisCache.size() > MAX_CACHE_SIZE) {
            // 移除最旧的条目
            analysisCache.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> 
                    Long.compare(a.timestamp, b.timestamp)))
                .limit(analysisCache.size() - MAX_CACHE_SIZE)
                .forEach(entry -> analysisCache.remove(entry.getKey()));
        }
    }
    
    /**
     * 生成UI上下文的缓存键
     */
    private String generateCacheKey(UiContext uiContext) {
        return uiContext.getTimestamp() + "_" + 
               (uiContext.getScreenshotBase64() != null ? 
                uiContext.getScreenshotBase64().hashCode() : 0) + "_" + 
               uiContext.getElements().size();
    }
    
    /**
     * 生成查找描述的缓存键
     */
    private String generateFindByDescCacheKey(UiContext uiContext, String description) {
        return "findByDesc_" + description + "_" + generateCacheKey(uiContext);
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> analyzeUiContext(UiContext uiContext) {
        if (uiContext == null) {
            return CompletableFuture.completedFuture(Map.of("error", "UI context is null"));
        }
        
        // 检查缓存
        String cacheKey = generateCacheKey(uiContext);
        CachedAnalysisResult cachedResult = analysisCache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            return CompletableFuture.completedFuture(new HashMap<>(cachedResult.getResult()));
        }
        
        // 开始分析时间
        long startTime = System.currentTimeMillis();
        
        // 创建结果容器
        Map<String, Object> analysisResult = new ConcurrentHashMap<>();
        
        try {
            // 使用并行处理进行分层分析
            CompletableFuture.runAsync(() -> {
                // 第一层：基本统计信息
                basicAnalysis(uiContext, analysisResult);
            });
            
            // 第二层：元素类型分析
            CompletableFuture.runAsync(() -> {
                elementTypeAnalysis(uiContext, analysisResult);
            });
            
            // 第三层：交互元素分析
            CompletableFuture.runAsync(() -> {
                interactiveElementAnalysis(uiContext, analysisResult);
            });
            
            // 第四层：结构分析
            CompletableFuture.runAsync(() -> {
                structuralAnalysis(uiContext, analysisResult);
            }).join(); // 等待最后一个分析完成
            
            // 计算分析时间
            long analysisTime = System.currentTimeMillis() - startTime;
            analysisResult.put("analysisTime", analysisTime);
            
            // 缓存结果
            analysisCache.put(cacheKey, new CachedAnalysisResult(new HashMap<>(analysisResult)));
            
        } catch (Exception e) {
            analysisResult.put("error", e.getMessage());
            analysisResult.put("analysisTime", System.currentTimeMillis() - startTime);
        }
        
        return CompletableFuture.completedFuture(analysisResult);
    }
    
    @Override
    public CompletableFuture<List<UiElement>> findElementsByDescription(UiContext uiContext, String description) {
        if (uiContext == null || description == null || description.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        // 检查缓存
        String cacheKey = generateFindByDescCacheKey(uiContext, description);
        CachedAnalysisResult cachedResult = analysisCache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            @SuppressWarnings("unchecked")
            List<UiElement> cachedElements = (List<UiElement>) cachedResult.getResult().get("elements");
            return CompletableFuture.completedFuture(new ArrayList<>(cachedElements));
        }
        
        // 使用并行流高效查找
        List<UiElement> matchingElements = uiContext.getElements().parallelStream()
            .filter(element -> {
                // 优化的匹配算法：先检查是否有文本，避免空指针
                String elementText = element.getText();
                if (elementText == null) return false;
                
                // 快速检查是否包含目标描述（优化大小写不敏感匹配）
                return elementText.toLowerCase().contains(description.toLowerCase()) ||
                       // 同时检查contentDescription
                       (element.getAttributes() != null &&
                        element.getAttributes().get("contentDescription") != null &&
                        element.getAttributes().get("contentDescription").toString().toLowerCase().contains(description.toLowerCase()));
            })
            .collect(Collectors.toList());
        
        // 缓存结果
        Map<String, Object> cacheResult = new HashMap<>();
        cacheResult.put("elements", matchingElements);
        analysisCache.put(cacheKey, new CachedAnalysisResult(cacheResult));
        
        return CompletableFuture.completedFuture(matchingElements);
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> understandIntent(String instruction, Map<String, Object> context) {
        if (instruction == null || instruction.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Map.of(
                "intent", "unknown",
                "confidence", 0.0,
                "error", "Instruction is null or empty"
            ));
        }
        
        // 优化的意图理解，使用并行处理提升性能
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 简单的意图分类逻辑，实际应用中可以使用更复杂的NLP模型
                String normalizedInstruction = instruction.toLowerCase().trim();
                Map<String, Object> result = new HashMap<>();
                
                // 基于关键词识别意图类型
                if (normalizedInstruction.contains("click") || 
                    normalizedInstruction.contains("tap") || 
                    normalizedInstruction.contains("press")) {
                    result.put("intent", "click_action");
                    result.put("confidence", 0.95);
                } else if (normalizedInstruction.contains("type") || 
                           normalizedInstruction.contains("enter") || 
                           normalizedInstruction.contains("input")) {
                    result.put("intent", "text_input");
                    result.put("confidence", 0.9);
                } else if (normalizedInstruction.contains("scroll") || 
                           normalizedInstruction.contains("swipe")) {
                    result.put("intent", "scroll_action");
                    result.put("confidence", 0.85);
                } else if (normalizedInstruction.contains("back") || 
                           normalizedInstruction.contains("return")) {
                    result.put("intent", "back_action");
                    result.put("confidence", 0.92);
                } else if (normalizedInstruction.contains("find") || 
                           normalizedInstruction.contains("search")) {
                    result.put("intent", "search_action");
                    result.put("confidence", 0.88);
                } else {
                    result.put("intent", "generic_action");
                    result.put("confidence", 0.7);
                }
                
                // 添加原始指令和上下文
                result.put("instruction", instruction);
                result.put("context", context != null ? context : Collections.emptyMap());
                
                // 提取目标元素信息
                result.put("target_element", extractTargetElement(normalizedInstruction));
                
                return result;
            } catch (Exception e) {
                return Map.of(
                    "intent", "error",
                    "confidence", 0.0,
                    "error", e.getMessage(),
                    "instruction", instruction,
                    "context", context
                );
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Map<String, Object>>> generateActionPlan(Map<String, Object> intent, UiContext uiContext) {
        if (intent == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String intentType = intent.getOrDefault("intent", "unknown").toString();
            List<Map<String, Object>> plan = new ArrayList<>();
            
            // 基础步骤：分析UI
            plan.add(Map.of(
                "action", "analyze_ui",
                "description", "Analyzing UI context",
                "estimatedTime", 500,
                "priority", 1
            ));
            
            // 根据意图类型生成特定的操作步骤
            switch (intentType) {
                case "click_action":
                    plan.add(Map.of(
                        "action", "find_target_element",
                        "description", "Finding the target element to click",
                        "estimatedTime", 300,
                        "priority", 2
                    ));
                    plan.add(Map.of(
                        "action", "click_element",
                        "description", "Performing click operation",
                        "estimatedTime", 200,
                        "priority", 3
                    ));
                    plan.add(Map.of(
                        "action", "verify_click",
                        "description", "Verifying click operation result",
                        "estimatedTime", 400,
                        "priority", 4
                    ));
                    break;
                case "text_input":
                    plan.add(Map.of(
                        "action", "find_input_element",
                        "description", "Finding the input element",
                        "estimatedTime", 300,
                        "priority", 2
                    ));
                    plan.add(Map.of(
                        "action", "focus_element",
                        "description", "Focusing on the input element",
                        "estimatedTime", 100,
                        "priority", 3
                    ));
                    plan.add(Map.of(
                        "action", "input_text",
                        "description", "Entering text into the input field",
                        "estimatedTime", 800,
                        "priority", 4
                    ));
                    plan.add(Map.of(
                        "action", "verify_input",
                        "description", "Verifying text input result",
                        "estimatedTime", 300,
                        "priority", 5
                    ));
                    break;
                case "scroll_action":
                    plan.add(Map.of(
                        "action", "determine_scroll_direction",
                        "description", "Determining scroll direction and distance",
                        "estimatedTime", 200,
                        "priority", 2
                    ));
                    plan.add(Map.of(
                        "action", "perform_scroll",
                        "description", "Performing scroll operation",
                        "estimatedTime", 600,
                        "priority", 3
                    ));
                    plan.add(Map.of(
                        "action", "verify_scroll",
                        "description", "Verifying scroll result",
                        "estimatedTime", 300,
                        "priority", 4
                    ));
                    break;
                case "back_action":
                    plan.add(Map.of(
                        "action", "execute_back",
                        "description", "Executing back navigation",
                        "estimatedTime", 300,
                        "priority", 2
                    ));
                    plan.add(Map.of(
                        "action", "verify_navigation",
                        "description", "Verifying navigation result",
                        "estimatedTime", 400,
                        "priority", 3
                    ));
                    break;
                default:
                    // 通用操作
                    plan.add(Map.of(
                        "action", "execute_instruction",
                        "description", "Executing instruction based on intent",
                        "estimatedTime", 1000,
                        "priority", 2
                    ));
                    plan.add(Map.of(
                        "action", "verify_result",
                        "description", "Verifying execution result",
                        "estimatedTime", 500,
                        "priority", 3
                    ));
                    break;
            }
            
            // 添加最终验证步骤
            plan.add(Map.of(
                "action", "final_verification",
                "description", "Final verification of operation success",
                "estimatedTime", 200,
                "priority", 99
            ));
            
            return plan;
        });
    }
    
    @Override
    public void initialize(Map<String, Object> config) {
        // 初始化洞察引擎
        if (!isInitialized) {
            // 配置缓存参数
            if (config != null) {
                if (config.containsKey("cacheExpiryTimeMs")) {
                    // 更新缓存过期时间
                }
                if (config.containsKey("maxCacheSize")) {
                    // 更新最大缓存大小
                }
            }
            
            // 启动定时清理缓存任务，每5分钟执行一次
            cacheCleanupService.scheduleAtFixedRate(
                this::cleanupExpiredCache, 
                5, 5, TimeUnit.MINUTES
            );
            
            isInitialized = true;
        }
    }
    
    @Override
    public void shutdown() {
        // 关闭洞察引擎，清理资源
        if (isInitialized) {
            // 停止缓存清理服务
            cacheCleanupService.shutdown();
            try {
                if (!cacheCleanupService.awaitTermination(5, TimeUnit.SECONDS)) {
                    cacheCleanupService.shutdownNow();
                }
            } catch (InterruptedException e) {
                cacheCleanupService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            // 清理缓存
            analysisCache.clear();
            isInitialized = false;
        }
    }
    
    /**
     * 基本统计信息分析
     */
    private void basicAnalysis(UiContext uiContext, Map<String, Object> result) {
        result.put("elementCount", uiContext.getElements().size());
        result.put("timestamp", uiContext.getTimestamp());
        result.put("hasScreenshot", uiContext.getScreenshotBase64() != null);
    }
    
    /**
     * 元素类型分析
     */
    private void elementTypeAnalysis(UiContext uiContext, Map<String, Object> result) {
        Map<String, Long> typeCounts = uiContext.getElements().parallelStream()
            .collect(Collectors.groupingBy(
                element -> getSimpleClassName(getElementClassName(element)),
                Collectors.counting()
            ));
        result.put("elementTypes", typeCounts);
        
        // 计算文本元素占比
        long textElementCount = uiContext.getElements().parallelStream()
            .filter(element -> element.getText() != null && !element.getText().trim().isEmpty())
            .count();
        result.put("textElementCount", textElementCount);
        if (uiContext.getElements().size() > 0) {
            result.put("textElementRatio", (double) textElementCount / uiContext.getElements().size());
        }
    }
    
    /**
     * 交互元素分析
     */
    private void interactiveElementAnalysis(UiContext uiContext, Map<String, Object> result) {
        // 分析可点击元素
        List<UiElement> clickableElements = uiContext.getElements().parallelStream()
            .filter(this::isClickable)
            .collect(Collectors.toList());
        result.put("clickableElementCount", clickableElements.size());
        
        // 分析可编辑元素
        List<UiElement> editableElements = uiContext.getElements().parallelStream()
            .filter(this::isEditable)
            .collect(Collectors.toList());
        result.put("editableElementCount", editableElements.size());
        
        // 分析可见元素
        List<UiElement> visibleElements = uiContext.getElements().parallelStream()
            .filter(this::isVisible)
            .collect(Collectors.toList());
        result.put("visibleElementCount", visibleElements.size());
        
        // 保存重要交互元素列表
        result.put("mainInteractiveElements", 
            clickableElements.stream()
                .limit(10) // 只保留前10个最重要的交互元素
                .map(this::elementToMap)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 结构分析
     */
    private void structuralAnalysis(UiContext uiContext, Map<String, Object> result) {
        // 分析层级结构（简化版，实际可能需要更复杂的树结构分析）
        Map<Integer, Long> depthDistribution = uiContext.getElements().parallelStream()
            .mapToInt(this::getElementDepth)
            .boxed()
            .collect(Collectors.groupingBy(
                depth -> depth,
                Collectors.counting()
            ));
        result.put("depthDistribution", depthDistribution);
        
        // 分析文本密度
        int totalTextLength = uiContext.getElements().parallelStream()
            .filter(element -> element.getText() != null)
            .mapToInt(element -> element.getText().length())
            .sum();
        result.put("totalTextLength", totalTextLength);
        
        // 提取页面标题或主要文本
        String mainText = extractMainText(uiContext);
        if (mainText != null) {
            result.put("mainText", mainText);
        }
        
        // 分析布局类型（简化判断）
        String layoutType = determineLayoutType(uiContext);
        result.put("layoutType", layoutType);
    }
    
    /**
     * 提取指令中的目标元素信息
     */
    private Map<String, String> extractTargetElement(String instruction) {
        Map<String, String> targetInfo = new HashMap<>();
        // 简单的关键词提取，实际应用中可以使用更复杂的NLP技术
        
        // 检查是否包含数字，可能是第N个元素
        if (instruction.matches(".*\\d+.*")) {
            targetInfo.put("hasIndex", "true");
        }
        
        // 检查常见元素类型关键词
        String[] elementTypes = {"button", "input", "field", "text", "link", "image", "icon"};
        for (String type : elementTypes) {
            if (instruction.contains(type)) {
                targetInfo.put("elementType", type);
                break;
            }
        }
        
        return targetInfo;
    }
    
    /**
     * 获取元素的类名
     */
    private String getElementClassName(UiElement element) {
        if (element.getAttributes() != null && element.getAttributes().containsKey("className")) {
            Object classNameObj = element.getAttributes().get("className");
            if (classNameObj != null) {
                return classNameObj.toString();
            }
        }
        // 如果没有className属性，尝试使用type作为备选
        return element.getType() != null ? element.getType() : "unknown";
    }
    
    /**
     * 获取简化的类名
     */
    private String getSimpleClassName(String fullClassName) {
        if (fullClassName == null || fullClassName.isEmpty()) {
            return "unknown";
        }
        int lastDotIndex = fullClassName.lastIndexOf('.');
        return lastDotIndex > 0 ? fullClassName.substring(lastDotIndex + 1) : fullClassName;
    }
    
    /**
     * 判断元素是否可点击
     */
    private boolean isClickable(UiElement element) {
        // 直接使用接口提供的方法
        if (element.isClickable()) {
            return true;
        }
        
        Map<String, Object> attributes = element.getAttributes();
        if (attributes == null) return false;
        
        // 检查常见的可点击属性
        return attributes.containsKey("clickable") && Boolean.TRUE.equals(attributes.get("clickable")) ||
               attributes.containsKey("onClick") ||
               // 检查类名中是否包含可点击元素的特征
               getElementClassName(element).toLowerCase().contains("button") ||
               getElementClassName(element).toLowerCase().contains("link");
    }
    
    /**
     * 判断元素是否可编辑
     */
    private boolean isEditable(UiElement element) {
        // 直接使用接口提供的方法
        if (element.isEditable()) {
            return true;
        }
        
        Map<String, Object> attributes = element.getAttributes();
        if (attributes == null) return false;
        
        return attributes.containsKey("editable") && Boolean.TRUE.equals(attributes.get("editable")) ||
               attributes.containsKey("inputType") ||
               // 检查类名中是否包含可编辑元素的特征
               getElementClassName(element).toLowerCase().contains("edittext") ||
               getElementClassName(element).toLowerCase().contains("input") ||
               getElementClassName(element).toLowerCase().contains("textfield");
    }
    
    /**
     * 判断元素是否可见
     */
    private boolean isVisible(UiElement element) {
        // 直接使用接口提供的方法
        if (!element.isVisible()) {
            return false;
        }
        
        Map<String, Object> attributes = element.getAttributes();
        if (attributes == null) return true;
        
        // 检查可见性属性
        if (attributes.containsKey("visible") && !Boolean.TRUE.equals(attributes.get("visible"))) {
            return false;
        }
        if (attributes.containsKey("displayed") && !Boolean.TRUE.equals(attributes.get("displayed"))) {
            return false;
        }
        
        // 检查尺寸
        Map<String, Integer> bounds = getElementBounds(element);
        if (bounds != null) {
            return bounds.get("width") > 0 && bounds.get("height") > 0;
        }
        
        return true;
    }
    
    /**
     * 获取元素边界
     */
    private Map<String, Integer> getElementBounds(UiElement element) {
        Map<String, Object> attributes = element.getAttributes();
        if (attributes == null || !attributes.containsKey("bounds")) {
            return null;
        }
        
        try {
            // 解析边界信息，格式可能是{x,y,width,height}或类似格式
            Object bounds = attributes.get("bounds");
            if (bounds instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> boundsMap = (Map<String, Object>) bounds;
                Map<String, Integer> result = new HashMap<>();
                result.put("x", convertToInt(boundsMap.get("x")));
                result.put("y", convertToInt(boundsMap.get("y")));
                result.put("width", convertToInt(boundsMap.get("width")));
                result.put("height", convertToInt(boundsMap.get("height")));
                return result;
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        
        return null;
    }
    
    /**
     * 将对象转换为整数
     */
    private int convertToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取元素深度
     */
    private int getElementDepth(UiElement element) {
        // 简化实现，实际可能需要从父元素链计算
        Map<String, Object> attributes = element.getAttributes();
        if (attributes != null && attributes.containsKey("depth")) {
            return convertToInt(attributes.get("depth"));
        }
        return 0;
    }
    
    /**
     * 提取页面主要文本
     */
    private String extractMainText(UiContext uiContext) {
        return uiContext.getElements().stream()
            .filter(element -> element.getText() != null && element.getText().length() > 5)
            .max(Comparator.comparingInt(e -> e.getText().length()))
            .map(UiElement::getText)
            .orElse(null);
    }
    
    /**
     * 判断布局类型
     */
    private String determineLayoutType(UiContext uiContext) {
        // 简化的布局类型判断
        List<UiElement> elements = uiContext.getElements();
        if (elements.isEmpty()) return "empty";
        
        // 计算交互元素比例
        long interactiveCount = elements.stream()
            .filter(element -> isClickable(element) || isEditable(element))
            .count();
        
        double interactiveRatio = (double) interactiveCount / elements.size();
        
        // 基于交互元素比例和元素总数判断布局类型
        if (interactiveRatio > 0.3 && elements.size() > 20) {
            return "complex_interactive";
        } else if (interactiveRatio > 0.2) {
            return "form";
        } else if (elements.size() > 50) {
            return "list";
        } else {
            return "simple";
        }
    }
    
    /**
     * 将元素转换为Map表示
     */
    private Map<String, Object> elementToMap(UiElement element) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", element.getText());
        map.put("className", getSimpleClassName(getElementClassName(element)));
        
        Map<String, Object> attributes = element.getAttributes();
        if (attributes != null) {
            // 添加关键属性
            if (attributes.containsKey("resourceId")) {
                map.put("resourceId", attributes.get("resourceId"));
            }
            if (attributes.containsKey("contentDescription")) {
                map.put("contentDescription", attributes.get("contentDescription"));
            }
            
            // 添加可交互标志
            map.put("clickable", isClickable(element));
            map.put("editable", isEditable(element));
            map.put("visible", isVisible(element));
        }
        
        return map;
    }
}