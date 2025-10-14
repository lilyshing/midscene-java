package com.midscene.recorderform.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FormServiceImpl implements FormService, InitializingBean {

    @Value("${recorder.form.save-path:./output/forms}")
    private String formSavePath;

    // 使用内存缓存表单数据
    private final Map<String, Map<String, Object>> formCache = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化保存目录
        if (formSavePath != null) {
            File directory = new File(formSavePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
    }

    @Override
    public boolean saveFormData(String formId, Map<String, Object> formData) {
        try {
            // 保存到内存缓存
            formCache.put(formId, new HashMap<>(formData));
            
            // 保存到文件系统
            if (formSavePath != null) {
                String filePath = formSavePath + File.separator + formId + ".json";
                try (FileWriter writer = new FileWriter(filePath)) {
                    // 简单的JSON格式化，实际项目中应该使用Jackson或Gson
                    writer.write(formData.toString());
                }
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Map<String, Object>> loadFormData(String formId) {
        // 先从缓存中查找
        if (formCache.containsKey(formId)) {
            return Optional.of(new HashMap<>(formCache.get(formId)));
        }
        
        // 从文件系统加载
        try {
            if (formSavePath != null) {
                String filePath = formSavePath + File.separator + formId + ".json";
                if (Files.exists(Paths.get(filePath))) {
                    // 读取文件内容，实际项目中应该使用Jackson或Gson解析JSON
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    // 这里简单处理，实际应该解析JSON字符串为Map
                    Map<String, Object> formData = new HashMap<>();
                    formData.put("content", content);
                    formCache.put(formId, formData);
                    return Optional.of(formData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public String createNewForm(String title) {
        String formId = UUID.randomUUID().toString();
        Map<String, Object> newForm = new HashMap<>();
        newForm.put("id", formId);
        newForm.put("title", title);
        newForm.put("createdAt", new Date().toString());
        newForm.put("updatedAt", new Date().toString());
        
        formCache.put(formId, newForm);
        return formId;
    }

    @Override
    public boolean validateFormData(Map<String, Object> formData) {
        // 简单的验证逻辑，实际项目中应该有更复杂的验证规则
        return formData != null && !formData.isEmpty();
    }
}