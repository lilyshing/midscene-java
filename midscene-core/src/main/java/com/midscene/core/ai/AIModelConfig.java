package com.midscene.core.ai;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AI模型配置类
 */
public class AIModelConfig {
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Double temperature = 0.7;
    private Integer maxTokens = 1000;
    private Duration timeout = Duration.ofSeconds(30);
    private Map<String, Object> additionalParams = new HashMap<>();
    private boolean debugMode = false;
    private String apiVersion;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public static class Builder {
        private AIModelConfig config = new AIModelConfig();

        public Builder apiKey(String apiKey) {
            config.setApiKey(apiKey);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            config.setBaseUrl(baseUrl);
            return this;
        }

        public Builder modelName(String modelName) {
            config.setModelName(modelName);
            return this;
        }

        public Builder temperature(double temperature) {
            config.setTemperature(temperature);
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            config.setMaxTokens(maxTokens);
            return this;
        }

        public Builder timeout(Duration timeout) {
            config.setTimeout(timeout);
            return this;
        }

        public Builder debugMode(boolean debugMode) {
            config.setDebugMode(debugMode);
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            config.setApiVersion(apiVersion);
            return this;
        }

        public Builder additionalParam(String key, Object value) {
            config.getAdditionalParams().put(key, value);
            return this;
        }

        public AIModelConfig build() {
            return config;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}