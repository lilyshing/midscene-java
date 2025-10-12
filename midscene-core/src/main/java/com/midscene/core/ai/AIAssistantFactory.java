package com.midscene.core.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI助手工厂类，用于创建不同类型的AI助手实例
 */
public class AIAssistantFactory {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantFactory.class);
    
    /**
     * 模型类型枚举
     */
    public enum ModelType {
        DEFAULT,
        OPENAI,
        GOOGLE_GEMINI,
        QWEN
    }
    
    /**
     * 创建AI助手实例
     * @param config AI模型配置
     * @return AI助手实例
     */
    public static AIAssistant createAIAssistant(AIModelConfig config) {
        if (config == null) {
            logger.warn("No AIModelConfig provided, using DefaultAIAssistant");
            return new DefaultAIAssistant();
        }
        
        // 根据配置的模型类型或名称创建相应的助手
        ModelType modelType = determineModelType(config);
        
        switch (modelType) {
            case OPENAI:
                logger.debug("Creating OpenAIAssistant");
                return new OpenAIAssistant(config);
            case GOOGLE_GEMINI:
                logger.debug("Creating GoogleGeminiAssistant");
                return new GoogleGeminiAssistant(config);
            case QWEN:
                logger.debug("Creating QwenAssistant");
                return new QwenAssistant(config);
            case DEFAULT:
            default:
                logger.debug("Creating DefaultAIAssistant");
                return new DefaultAIAssistant();
        }
    }
    
    /**
     * 根据配置确定模型类型
     * @param config AI模型配置
     * @return 模型类型
     */
    private static ModelType determineModelType(AIModelConfig config) {
        // 优先使用明确指定的模型类型
        String modelName = config.getModelName();
        String baseUrl = config.getBaseUrl();
        String apiKey = config.getApiKey();
        
        // 根据API密钥或基础URL判断
        if (apiKey != null) {
            if (baseUrl != null) {
                if (baseUrl.contains("openai.com")) {
                    return ModelType.OPENAI;
                } else if (baseUrl.contains("generativelanguage.googleapis.com")) {
                    return ModelType.GOOGLE_GEMINI;
                } else if (baseUrl.contains("dashscope.aliyuncs.com")) {
                    return ModelType.QWEN;
                }
            }
            
            // 根据模型名称判断
            if (modelName != null) {
                modelName = modelName.toLowerCase();
                if (modelName.contains("gpt") || modelName.contains("openai")) {
                    return ModelType.OPENAI;
                } else if (modelName.contains("gemini") || modelName.contains("google")) {
                    return ModelType.GOOGLE_GEMINI;
                } else if (modelName.contains("qwen")) {
                    return ModelType.QWEN;
                }
            }
            
            // 如果有API密钥但无法判断类型，尝试根据默认设置猜测
            if (baseUrl == null && modelName == null) {
                // 默认假设是OpenAI API
                logger.info("No model type specified, defaulting to OpenAI");
                return ModelType.OPENAI;
            }
        }
        
        // 如果没有API密钥，使用默认助手
        return ModelType.DEFAULT;
    }
    
    /**
     * 创建AI助手实例
     * @param modelType 模型类型
     * @param config AI模型配置
     * @return AI助手实例
     */
    public static AIAssistant createAIAssistant(ModelType modelType, AIModelConfig config) {
        logger.debug("Creating AI assistant of type: {}", modelType);
        
        switch (modelType) {
            case OPENAI:
                return new OpenAIAssistant(config);
            case GOOGLE_GEMINI:
                return new GoogleGeminiAssistant(config);
            case QWEN:
                return new QwenAssistant(config);
            case DEFAULT:
            default:
                return new DefaultAIAssistant();
        }
    }
}