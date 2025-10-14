package com.midscene.recorderform.service;

import java.util.Map;
import java.util.Optional;

public interface FormService {
    /**
     * 保存表单数据
     * @param formId 表单ID
     * @param formData 表单数据
     * @return 保存是否成功
     */
    boolean saveFormData(String formId, Map<String, Object> formData);

    /**
     * 加载表单数据
     * @param formId 表单ID
     * @return 表单数据，如果不存在则返回Optional.empty()
     */
    Optional<Map<String, Object>> loadFormData(String formId);

    /**
     * 创建新表单
     * @param title 表单标题
     * @return 新表单ID
     */
    String createNewForm(String title);

    /**
     * 验证表单数据
     * @param formData 表单数据
     * @return 验证结果，true表示验证通过
     */
    boolean validateFormData(Map<String, Object> formData);
}