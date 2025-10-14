package com.midscene.shared.model.ai;

import java.util.Objects;

/**
 * 数据提取结果类
 * 用于封装数据提取操作的结果
 */
public class ExtractDataResult {
    private String data;

    private ExtractDataResult() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractDataResult that = (ExtractDataResult) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final ExtractDataResult result = new ExtractDataResult();

        public Builder data(String data) {
            result.data = data;
            return this;
        }

        public ExtractDataResult build() {
            return result;
        }
    }
}