package com.midscene.site.handler;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 全局错误处理控制器
 * 负责处理网站的各种错误情况，如404、500等，并返回友好的错误页面
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * 处理所有错误请求
     * @param request HTTP请求对象
     * @param model 模型对象，用于传递数据到视图
     * @return 错误页面视图名称
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 获取错误状态码
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        
        // 获取错误信息
        String errorMessage = getErrorMessage(statusCode);
        String errorDescription = getErrorDescription(statusCode);
        
        // 设置错误页面所需的数据
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorDescription", errorDescription);
        model.addAttribute("timestamp", LocalDateTime.now().toString());
        model.addAttribute("requestUri", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        
        // 设置通用的页面属性
        model.addAttribute("appName", "Midscene");
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("currentYear", LocalDateTime.now().getYear());
        model.addAttribute("currentPage", "error");
        
        // 根据不同的状态码返回不同的错误页面
        if (statusCode == 404) {
            return "error-404";
        } else if (statusCode == 403) {
            return "error-403";
        } else if (statusCode == 500) {
            return "error-500";
        } else {
            return "error-generic";
        }
    }
    
    /**
     * 根据状态码获取错误消息
     * @param statusCode HTTP状态码
     * @return 错误消息
     */
    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "错误的请求";
            case 401:
                return "未授权";
            case 403:
                return "禁止访问";
            case 404:
                return "页面未找到";
            case 405:
                return "方法不允许";
            case 429:
                return "请求过于频繁";
            case 500:
                return "服务器内部错误";
            case 502:
                return "网关错误";
            case 503:
                return "服务不可用";
            case 504:
                return "网关超时";
            default:
                return "发生错误";
        }
    }
    
    /**
     * 根据状态码获取错误描述
     * @param statusCode HTTP状态码
     * @return 错误描述
     */
    private String getErrorDescription(int statusCode) {
        switch (statusCode) {
            case 400:
                return "您的请求格式有误，请检查后重试。";
            case 401:
                return "您需要先登录才能访问此页面。";
            case 403:
                return "抱歉，您没有权限访问此页面。";
            case 404:
                return "您访问的页面不存在或已被移除。";
            case 405:
                return "此请求方法不被允许。";
            case 429:
                return "您的请求过于频繁，请稍后再试。";
            case 500:
                return "服务器遇到了一个意外的错误，请稍后再试。";
            case 502:
                return "网关从上游服务器收到了无效的响应。";
            case 503:
                return "服务暂时不可用，请稍后再试。";
            case 504:
                return "网关超时，请稍后再试。";
            default:
                return "处理您的请求时发生了错误。";
        }
    }
    
    /**
     * 获取错误路径
     * @return 错误路径
     */
    public String getErrorPath() {
        return "/error";
    }
}