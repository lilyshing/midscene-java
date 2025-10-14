package com.midscene.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Midscene Playground Web Application 主类
 * 提供交互式测试环境的Web应用入口
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.midscene.playground",
    "com.midscene.core",
    "com.midscene.playground.service"
})
public class PlaygroundApplication {

    /**
     * 应用程序入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PlaygroundApplication.class, args);
    }

}