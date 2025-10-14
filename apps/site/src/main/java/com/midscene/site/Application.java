package com.midscene.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Midscene 官方网站应用程序入口类
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.midscene.site",
    "com.midscene.site.controller"
})
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}