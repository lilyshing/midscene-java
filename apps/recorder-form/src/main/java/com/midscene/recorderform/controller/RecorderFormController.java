package com.midscene.recorderform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RecorderFormController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/form")
    public String form(Model model) {
        // 初始化表单数据
        model.addAttribute("formTitle", "");
        model.addAttribute("formDescription", "");
        return "form";
    }

    @PostMapping("/submit-form")
    @ResponseBody
    public Map<String, Object> submitForm(@RequestParam Map<String, String> formData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 处理表单数据
            System.out.println("表单数据: " + formData);
            
            // 返回成功响应
            response.put("success", true);
            response.put("message", "表单提交成功");
            response.put("formData", formData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "表单提交失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}