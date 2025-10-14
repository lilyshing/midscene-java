package com.midscene.playground.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 测试控制器
 * 专门用于测试Thymeleaf模板渲染
 */
@Controller
public class TestController {

    /**
     * 访问测试页面
     */
    @GetMapping("/test-page")
    public String testPage() {
        return "test";
    }
    
    /**
     * 访问简化的首页
     */
    @GetMapping("/simple-index")
    public String simpleIndex() {
        return "index";
    }
}