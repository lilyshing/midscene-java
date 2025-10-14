package com.midscene.shared.model.ai;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI动作结果类
 * 用于封装AI动作操作的结果
 */
public class AIActionResult {
    private String intent;
    private List<Map<String, Object>> actionPlan;

    private AIActionResult() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getIntent() {
        return intent;
    }

    public List<Map<String, Object>> getActionPlan() {
        return actionPlan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIActionResult that = (AIActionResult) o;
        return Objects.equals(intent, that.intent) && 
               Objects.equals(actionPlan, that.actionPlan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intent, actionPlan);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final AIActionResult result = new AIActionResult();

        public Builder intent(String intent) {
            result.intent = intent;
            return this;
        }

        public Builder actionPlan(List<Map<String, Object>> actionPlan) {
            result.actionPlan = actionPlan;
            return this;
        }

        public AIActionResult build() {
            return result;
        }
    }
}