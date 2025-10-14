package com.midscene.playground.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理根路径请求并返回应用首页
 */
@Controller
public class HomeController {

    /**
     * 访问应用首页
     */
    @GetMapping({"/", "/index"})
    public String home() {
        return "index";
    }
    
    /**
     * 测试页面
     */
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}