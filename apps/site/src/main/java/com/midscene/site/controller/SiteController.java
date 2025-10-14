package com.midscene.site.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 网站控制器，处理页面路由和内容展示
 */
@Controller
public class SiteController {
    
    @Value("${app.version}")
    private String appVersion;
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.description}")
    private String appDescription;
    
    /**
     * 首页 - 兼容 /site/ 和 /site/index 路径
     */
    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "Midscene - AI 驱动的自动化测试平台");
        model.addAttribute("pageDescription", "Midscene 是一个强大的 AI 驱动的自动化测试平台，支持 Web、移动应用和桌面应用的测试。简单、高效、可靠。");
        model.addAttribute("isHome", true);
        return "index";
    }
    
    /**
     * 快速开始页面
     */
    @GetMapping("/quickstart")
    public String quickstart(@RequestParam(value = "lang", defaultValue = "zh") String lang, Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("language", lang);
        model.addAttribute("pageTitle", "快速开始 - Midscene");
        model.addAttribute("pageDescription", "快速入门 Midscene，从安装到创建第一个测试自动化场景。");
        model.addAttribute("activeNav", "quickstart");
        
        // 添加快速开始步骤
        String[] quickStartSteps = {
            "安装 Midscene SDK",
            "配置测试环境",
            "创建第一个测试场景",
            "运行测试并查看报告"
        };
        model.addAttribute("quickStartSteps", quickStartSteps);
        return "quickstart";
    }
    
    /**
     * 功能特性页面
     */
    @GetMapping("/features")
    public String features(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "功能特性 - Midscene");
        model.addAttribute("pageDescription", "探索 Midscene 的强大功能，包括 AI 驱动的测试生成、跨平台支持、实时监控等。");
        model.addAttribute("activeNav", "features");
        
        // 添加功能特性数据
        Map<String, String> features = new HashMap<>();
        features.put("ai", "AI 驱动的测试生成和维护");
        features.put("crossPlatform", "跨平台支持（Web、移动、桌面）");
        features.put("lowCode", "低代码/无代码测试创作");
        features.put("realtime", "实时监控和分析");
        features.put("collaboration", "团队协作和版本控制");
        features.put("integration", "丰富的第三方集成");
        model.addAttribute("features", features);
        return "features";
    }
    
    /**
     * 文档首页
     */
    @GetMapping("/docs")
    public String docs(@RequestParam(value = "lang", defaultValue = "zh") String lang, Model model, @RequestParam(required = false) String section) {
        addCommonModelAttributes(model);
        model.addAttribute("language", lang);
        model.addAttribute("pageTitle", "文档中心 - Midscene");
        model.addAttribute("pageDescription", "Midscene 官方文档，包含详细的使用指南、API 参考和最佳实践。");
        model.addAttribute("activeNav", "docs");
        model.addAttribute("activeSection", section != null ? section : "getting-started");
        
        // 文档章节
        Map<String, String> docSections = new HashMap<>();
        docSections.put("getting-started", "入门指南");
        docSections.put("web-automation", "Web 自动化测试");
        docSections.put("mobile-automation", "移动应用自动化");
        docSections.put("advanced", "高级功能");
        docSections.put("api-reference", "API 参考");
        docSections.put("best-practices", "最佳实践");
        model.addAttribute("docSections", docSections);
        return "docs";
    }
    
    /**
     * 特定文档页面
     */
    @GetMapping("/docs/{section}")
    public String docSection(
            @PathVariable String section, 
            @RequestParam(value = "lang", defaultValue = "zh") String lang, 
            Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("language", lang);
        model.addAttribute("docSection", section);
        model.addAttribute("pageTitle", "文档 - " + section + " - Midscene");
        model.addAttribute("activeNav", "docs");
        model.addAttribute("activeSection", section);
        return "docs";
    }
    
    /**
     * API文档页面
     */
    @GetMapping("/api")
    public String api(@RequestParam(value = "lang", defaultValue = "zh") String lang, Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("language", lang);
        model.addAttribute("pageTitle", "API 文档 - Midscene");
        model.addAttribute("pageDescription", "Midscene API 参考文档，详细说明所有可用的 API 端点和参数。");
        model.addAttribute("activeNav", "api");
        
        // API 分类
        String[] apiCategories = {
            "认证 API",
            "测试场景 API",
            "执行 API",
            "报告 API",
            "AI 命令 API"
        };
        model.addAttribute("apiCategories", apiCategories);
        return "api";
    }
    
    /**
     * 博客页面
     */
    @GetMapping("/blog")
    public String blog(@RequestParam(value = "lang", defaultValue = "zh") String lang, Model model, @RequestParam(required = false) Integer page) {
        addCommonModelAttributes(model);
        model.addAttribute("language", lang);
        model.addAttribute("pageTitle", "博客 - Midscene");
        model.addAttribute("pageDescription", "最新的自动化测试技术文章、行业动态和 Midscene 产品更新。");
        model.addAttribute("activeNav", "blog");
        model.addAttribute("currentPage", page != null ? page : 1);
        
        // 博客分类
        String[] blogCategories = {
            "产品更新",
            "技术文章",
            "最佳实践",
            "行业动态",
            "教程"
        };
        model.addAttribute("blogCategories", blogCategories);
        return "blog";
    }
    
    /**
     * 特定博客文章
     */
    @GetMapping("/blog/{article}")
    public String blogArticle(@PathVariable String article, Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("article", article);
        model.addAttribute("pageTitle", "博客文章 - Midscene");
        model.addAttribute("activeNav", "blog");
        return "blog";
    }
    
    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "关于我们 - Midscene");
        model.addAttribute("pageDescription", "了解 Midscene 的使命、愿景和团队。我们致力于通过 AI 技术革新自动化测试领域。");
        model.addAttribute("activeNav", "about");
        
        // 团队成员数据（示例）
        Map<String, String> teamMembers = new HashMap<>();
        teamMembers.put("张三", "创始人 & CEO");
        teamMembers.put("李四", "CTO");
        teamMembers.put("王五", "产品总监");
        teamMembers.put("赵六", "研发经理");
        model.addAttribute("teamMembers", teamMembers);
        return "about";
    }
    
    /**
     * FAQ页面
     */
    @GetMapping("/faq")
    public String faq(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "常见问题 - Midscene");
        model.addAttribute("pageDescription", "Midscene 常见问题解答，帮助您快速解决使用过程中遇到的问题。");
        model.addAttribute("activeNav", "faq");
        
        // FAQ 分类
        String[] faqCategories = {
            "入门问题",
            "技术问题",
            "定价与许可",
            "部署问题",
            "集成问题"
        };
        model.addAttribute("faqCategories", faqCategories);
        return "faq";
    }
    
    /**
     * 联系我们页面
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "联系我们 - Midscene");
        model.addAttribute("pageDescription", "有任何问题？请联系我们的支持团队。我们将尽快回复您。");
        model.addAttribute("activeNav", "contact");
        return "contact";
    }
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "登录 - Midscene");
        model.addAttribute("pageDescription", "登录您的 Midscene 账户以访问高级功能和管理您的测试项目。");
        model.addAttribute("showFooter", false);
        return "login";
    }
    
    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register(Model model) {
        addCommonModelAttributes(model);
        model.addAttribute("pageTitle", "注册 - Midscene");
        model.addAttribute("pageDescription", "创建新的 Midscene 账户，开始使用 AI 驱动的自动化测试平台。");
        model.addAttribute("showFooter", false);
        return "register";
    }
    
    /**
     * 添加通用模型属性
     */
    private void addCommonModelAttributes(Model model) {
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("appName", appName);
        model.addAttribute("appDescription", appDescription);
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        
        // 当前日期
        model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("siteVersion", appVersion);
        // 联系信息
        model.addAttribute("supportEmail", "support@midscene.com");
        model.addAttribute("companyName", "Midscene 科技有限公司");
        
        // 导航链接
        Map<String, String> navLinks = new HashMap<>();
        navLinks.put("首页", "/site/");
        navLinks.put("功能特性", "/site/features");
        navLinks.put("快速开始", "/site/quickstart");
        navLinks.put("文档中心", "/site/docs");
        navLinks.put("API", "/site/api");
        navLinks.put("博客", "/site/blog");
        navLinks.put("关于我们", "/site/about");
        navLinks.put("常见问题", "/site/faq");
        model.addAttribute("navLinks", navLinks);
        
        // 社交媒体链接
        Map<String, String> socialLinks = new HashMap<>();
        socialLinks.put("GitHub", "https://github.com/midscene");
        socialLinks.put("Twitter", "https://twitter.com/midscene");
        socialLinks.put("LinkedIn", "https://linkedin.com/company/midscene");
        socialLinks.put("Discord", "https://discord.gg/midscene");
        model.addAttribute("socialLinks", socialLinks);
        
        // 默认页面属性
        if (!model.containsAttribute("activeNav")) {
            model.addAttribute("activeNav", "home");
        }
        
        if (!model.containsAttribute("showFooter")) {
            model.addAttribute("showFooter", true);
        }
        
        // 公告信息（可选）
        model.addAttribute("announcement", "我们正在进行系统维护，部分功能可能暂时不可用。");
        model.addAttribute("showAnnouncement", false);
    }
}