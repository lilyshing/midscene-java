package com.midscene.shared.model.ai;

import com.midscene.shared.platform.UiElement;
import java.util.Objects;

/**
 * AI输入结果类
 * 用于封装AI输入操作的结果
 */
public class AIInputResult {
    private boolean success;
    private String message;
    private UiElement inputTarget;

    private AIInputResult() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UiElement getInputTarget() {
        return inputTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIInputResult that = (AIInputResult) o;
        return success == that.success && Objects.equals(message, that.message) && Objects.equals(inputTarget, that.inputTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, inputTarget);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final AIInputResult result = new AIInputResult();

        public Builder success(boolean success) {
            result.success = success;
            return this;
        }

        public Builder message(String message) {
            result.message = message;
            return this;
        }

        public Builder inputTarget(UiElement inputTarget) {
            result.inputTarget = inputTarget;
            return this;
        }

        public AIInputResult build() {
            return result;
        }
    }
}