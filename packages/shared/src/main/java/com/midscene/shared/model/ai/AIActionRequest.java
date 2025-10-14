package com.midscene.shared.model.ai;

import com.midscene.shared.platform.UiContext;
import java.util.Objects;

/**
 * AI动作请求类
 * 用于封装AI动作操作的请求参数
 */
public class AIActionRequest {
    private String task;
    private UiContext uiContext;
    private String agentType;

    private AIActionRequest() {
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
        AIActionRequest that = (AIActionRequest) o;
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
        private final AIActionRequest request = new AIActionRequest();

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

        public AIActionRequest build() {
            return request;
        }
    }
}