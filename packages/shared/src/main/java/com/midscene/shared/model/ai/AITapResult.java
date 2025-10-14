package com.midscene.shared.model.ai;

import com.midscene.shared.platform.ElementLocator;
import java.util.Objects;

/**
 * AI点击结果类
 * 用于封装AI点击操作的结果
 */
public class AITapResult {
    private ElementLocator tapTarget;

    private AITapResult() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public ElementLocator getTapTarget() {
        return tapTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AITapResult that = (AITapResult) o;
        return Objects.equals(tapTarget, that.tapTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tapTarget);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final AITapResult result = new AITapResult();

        public Builder tapTarget(ElementLocator tapTarget) {
            result.tapTarget = tapTarget;
            return this;
        }

        public AITapResult build() {
            return result;
        }
    }
}