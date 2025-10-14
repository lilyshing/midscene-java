package com.midscene.shared.model.ai;

import com.midscene.shared.platform.UiContext;
import java.util.Objects;

/**
 * 数据提取请求类
 * 用于封装数据提取操作的请求参数
 */
public class ExtractDataRequest {
    private String task;
    private UiContext uiContext;
    private String agentType;

    private ExtractDataRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTask() {
        return task;
    }

    public UiContext getUiContext() {
        return uiContext;
    }

    public String getAgentType() {
        return agentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractDataRequest that = (ExtractDataRequest) o;
        return Objects.equals(task, that.task) && 
               Objects.equals(uiContext, that.uiContext) &&
               Objects.equals(agentType, that.agentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, uiContext, agentType);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final ExtractDataRequest request = new ExtractDataRequest();

        public Builder task(String task) {
            request.task = task;
            return this;
        }

        public Builder uiContext(UiContext uiContext) {
            request.uiContext = uiContext;
            return this;
        }

        public Builder agentType(String agentType) {
            request.agentType = agentType;
            return this;
        }

        public ExtractDataRequest build() {
            return request;
        }
    }
}