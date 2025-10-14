package com.midscene.shared.model.ai;

import com.midscene.shared.platform.UiContext;
import java.util.Objects;

/**
 * AI输入请求类
 * 用于封装AI输入操作的请求参数
 */
public class AIInputRequest {
    private String task;
    private String inputText;
    private UiContext uiContext;
    private String agentType;

    private AIInputRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTask() {
        return task;
    }

    public String getInputText() {
        return inputText;
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
        AIInputRequest that = (AIInputRequest) o;
        return Objects.equals(task, that.task) &&
               Objects.equals(inputText, that.inputText) &&
               Objects.equals(uiContext, that.uiContext) &&
               Objects.equals(agentType, that.agentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, inputText, uiContext, agentType);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final AIInputRequest request = new AIInputRequest();

        public Builder task(String task) {
            request.task = task;
            return this;
        }

        public Builder inputText(String inputText) {
            request.inputText = inputText;
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

        public AIInputRequest build() {
            return request;
        }
    }
}