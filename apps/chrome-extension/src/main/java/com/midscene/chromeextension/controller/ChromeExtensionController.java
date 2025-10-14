package com.midscene.chromeextension.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChromeExtensionController {

    @Value("${chrome.extension.id}")
    private String extensionId;

    @Value("${chrome.extension.version}")
    private String extensionVersion;

    @Value("${chrome.extension.name}")
    private String extensionName;

    /**
     * 首页 - 扩展管理界面
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("extensionId", extensionId);
        model.addAttribute("extensionVersion", extensionVersion);
        model.addAttribute("extensionName", extensionName);
        return "index";
    }

    /**
     * 扩展配置页面
     */
    @GetMapping("/config")
    public String config(Model model) {
        model.addAttribute("extensionId", extensionId);
        model.addAttribute("extensionVersion", extensionVersion);
        return "config";
    }

    /**
     * 录制界面
     */
    @GetMapping("/recorder")
    public String recorder() {
        return "recorder";
    }

    /**
     * 与浏览器扩展通信的API端点
     */
    @PostMapping("/api/bridge")
    @ResponseBody
    public Map<String, Object> bridge(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Bridge connection established");
        response.put("timestamp", System.currentTimeMillis());
        
        // 这里将在实际实现中处理与Chrome扩展的通信逻辑
        return response;
    }

    /**
     * 获取扩展信息的API
     */
    @GetMapping("/api/extension-info")
    @ResponseBody
    public Map<String, Object> getExtensionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("id", extensionId);
        info.put("version", extensionVersion);
        info.put("name", extensionName);
        return info;
    }

    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("extensionName", extensionName);
        model.addAttribute("extensionVersion", extensionVersion);
        return "about";
    }

}