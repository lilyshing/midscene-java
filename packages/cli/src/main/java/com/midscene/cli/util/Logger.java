package com.midscene.cli.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 * 提供不同级别的日志输出功能
 */
public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private boolean verbose = false;
    private boolean coloredOutput = true;
    
    /**
     * 构造函数
     */
    public Logger(boolean verbose) {
        this.verbose = verbose;
        // 检查是否支持彩色输出
        this.coloredOutput = checkColoredOutputSupport();
    }
    
    /**
     * 检查是否支持彩色输出
     */
    private boolean checkColoredOutputSupport() {
        // 在Windows上，检查是否是现代终端
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getenv("TERM_PROGRAM") != null || 
                   System.getenv("WT_SESSION") != null; // Windows Terminal
        }
        // 在Unix/Linux系统上，检查TERM环境变量
        return System.getenv("TERM") != null && 
               !System.getenv("TERM").equals("dumb");
    }
    
    /**
     * 设置是否启用详细日志
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * 获取是否启用详细日志
     */
    public boolean isVerbose() {
        return verbose;
    }
    
    /**
     * 调试日志
     */
    public void debug(String message) {
        if (verbose) {
            log("DEBUG", message, ConsoleColor.BLUE);
        }
    }
    
    /**
     * 信息日志
     */
    public void info(String message) {
        log("INFO", message, ConsoleColor.GREEN);
    }
    
    /**
     * 警告日志
     */
    public void warn(String message) {
        log("WARN", message, ConsoleColor.YELLOW);
    }
    
    /**
     * 错误日志
     */
    public void error(String message) {
        log("ERROR", message, ConsoleColor.RED);
    }
    
    /**
     * 成功日志
     */
    public void success(String message) {
        log("SUCCESS", message, ConsoleColor.GREEN);
    }
    
    /**
     * 日志输出
     */
    private void log(String level, String message, ConsoleColor color) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        if (coloredOutput) {
            logMessage = color.getAnsiCode() + logMessage + ConsoleColor.RESET.getAnsiCode();
        }
        
        System.out.println(logMessage);
    }
    
    /**
     * 控制台颜色枚举
     */
    private enum ConsoleColor {
        RESET("\033[0m"),
        BLACK("\033[30m"),
        RED("\033[31m"),
        GREEN("\033[32m"),
        YELLOW("\033[33m"),
        BLUE("\033[34m"),
        MAGENTA("\033[35m"),
        CYAN("\033[36m"),
        WHITE("\033[37m");
        
        private final String ansiCode;
        
        ConsoleColor(String ansiCode) {
            this.ansiCode = ansiCode;
        }
        
        public String getAnsiCode() {
            return ansiCode;
        }
    }
    
    /**
     * 打印分隔线
     */
    public void separator() {
        System.out.println("---------------------------------------------------------------");
    }
    
    /**
     * 打印带有标题的分隔线
     */
    public void separator(String title) {
        System.out.println("==== " + title + " ====");
    }
    
    /**
     * 打印空行
     */
    public void newline() {
        System.out.println();
    }
    
    /**
     * 打印进度信息（不换行）
     */
    public void progress(String message) {
        System.out.print("\r" + message);
        System.out.flush();
    }
    
    /**
     * 打印带有缩进的消息
     */
    public void indent(String message, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        sb.append(message);
        System.out.println(sb.toString());
    }
}