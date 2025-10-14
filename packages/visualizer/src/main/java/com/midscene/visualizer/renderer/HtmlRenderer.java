package com.midscene.visualizer.renderer;

import com.midscene.visualizer.model.DebugSession;
import com.midscene.visualizer.model.ElementInfo;
import com.midscene.visualizer.model.TestExecution;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * HTML渲染器
 * 将调试会话渲染为HTML格式，便于在浏览器中查看
 */
public class HtmlRenderer implements Renderer {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public String render(DebugSession session) {
        StringBuilder html = new StringBuilder();
        
        // HTML头部
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"zh-CN\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>调试会话报告 - ").append(session.getTitle()).append("</title>")
            .append("<style>")
            .append(getCssStyles())
            .append("</style>")
            .append("</head>")
            .append("<body>");
        
        // 头部信息
        html.append("<div class=\"header\">")
            .append("<h1>调试会话报告</h1>")
            .append("<div class=\"session-info\">")
            .append("<p><strong>会话标题：</strong>").append(session.getTitle()).append("</p>")
            .append("<p><strong>会话ID：</strong>").append(session.getId()).append("</p>")
            .append("<p><strong>开始时间：</strong>").append(formatDateTime(session.getStartTime())).append("</p>")
            .append("<p><strong>结束时间：</strong>").append(session.getEndTime() != null ? formatDateTime(session.getEndTime()) : "进行中").append("</p>")
            .append("<p><strong>持续时间：</strong>").append(session.getDurationSeconds()).append("秒</p>")
            .append("<p><strong>平台：</strong>").append(session.getPlatform() != null ? session.getPlatform() : "未知").append("</p>")
            .append("<p><strong>设备：</strong>").append(session.getDeviceName() != null ? session.getDeviceName() : "未知").append("</p>")
            .append("</div>")
            .append("</div>");
        
        // 统计信息
        html.append("<div class=\"summary\">")
            .append("<h2>执行摘要</h2>")
            .append("<div class=\"stats\">")
            .append("<div class=\"stat-item\">")
            .append("<span class=\"label\">总测试数</span>")
            .append("<span class=\"value\">").append(session.getTestExecutions().size()).append("</span>")
            .append("</div>")
            .append("<div class=\"stat-item success\">")
            .append("<span class=\"label\">成功</span>")
            .append("<span class=\"value\">").append(session.getSuccessfulExecutionsCount()).append("</span>")
            .append("</div>")
            .append("<div class=\"stat-item error\">")
            .append("<span class=\"label\">失败</span>")
            .append("<span class=\"value\">").append(session.getFailedExecutionsCount()).append("</span>")
            .append("</div>")
            .append("<div class=\"stat-item\">")
            .append("<span class=\"label\">截图数</span>")
            .append("<span class=\"value\">").append(session.getScreenshots().size()).append("</span>")
            .append("</div>")
            .append("<div class=\"stat-item\">")
            .append("<span class=\"label\">元素数</span>")
            .append("<span class=\"value\">").append(session.getElements().size()).append("</span>")
            .append("</div>")
            .append("</div>")
            .append("</div>");
        
        // 测试执行列表
        renderTestExecutions(html, session.getTestExecutions());
        
        // 元素列表
        renderElements(html, session.getElements());
        
        // 截图列表
        renderScreenshots(html, session.getScreenshots());
        
        // HTML尾部
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 渲染测试执行列表
     */
    private void renderTestExecutions(StringBuilder html, List<TestExecution> executions) {
        if (executions.isEmpty()) {
            return;
        }
        
        html.append("<div class=\"section\">")
            .append("<h2>测试执行记录</h2>")
            .append("<table class=\"execution-table\">")
            .append("<thead>")
            .append("<tr>")
            .append("<th>序号</th>")
            .append("<th>操作名称</th>")
            .append("<th>目标</th>")
            .append("<th>状态</th>")
            .append("<th>执行时间</th>")
            .append("<th>开始时间</th>")
            .append("</tr>")
            .append("</thead>")
            .append("<tbody>");
        
        for (int i = 0; i < executions.size(); i++) {
            TestExecution execution = executions.get(i);
            html.append("<tr class=\"").append(execution.isSuccess() ? "success" : "error").append("\">")
                .append("<td>").append(i + 1).append("</td>")
                .append("<td>").append(execution.getActionName()).append("</td>")
                .append("<td>").append(execution.getTarget()).append("</td>")
                .append("<td>").append(execution.isSuccess() ? "成功" : "失败").append("</td>")
                .append("<td>").append(execution.getExecutionTimeMs()).append("ms</td>")
                .append("<td>").append(formatDateTime(execution.getStartTime())).append("</td>")
                .append("</tr>");
            
            // 如果有错误消息，显示错误详情
            if (!execution.isSuccess() && execution.getErrorMessage() != null) {
                html.append("<tr class=\"error-detail\">")
                    .append("<td colspan=\"6\">")
                    .append("<pre>").append(escapeHtml(execution.getErrorMessage())).append("</pre>")
                    .append("</td>")
                    .append("</tr>");
            }
        }
        
        html.append("</tbody>")
            .append("</table>")
            .append("</div>");
    }
    
    /**
     * 渲染元素列表
     */
    private void renderElements(StringBuilder html, List<ElementInfo> elements) {
        if (elements.isEmpty()) {
            return;
        }
        
        html.append("<div class=\"section\">")
            .append("<h2>元素信息</h2>")
            .append("<div class=\"elements-grid\">");
        
        for (ElementInfo element : elements) {
            html.append("<div class=\"element-card\">")
                .append("<h3>").append(element.getTagName() != null ? element.getTagName() : "Element").append("</h3>")
                .append("<p><strong>Resource ID:</strong> " + (element.getResourceId() != null ? element.getResourceId() : "N/A") + "</p>")
                .append("<p><strong>Text:</strong> " + (element.getText() != null ? escapeHtml(element.getText()) : "N/A") + "</p>")
                .append("<p><strong>Bounds:</strong> (").append(element.getX()).append(", ")
                .append(element.getY()).append(", ")
                .append(element.getWidth()).append(", ")
                .append(element.getHeight()).append(")</p>")
                .append("<p><strong>Clickable:</strong> " + element.isClickable() + "</p>")
                .append("<p><strong>Visible:</strong> " + element.isVisible() + "</p>")
                .append("</div>");
        }
        
        html.append("</div>")
            .append("</div>");
    }
    
    /**
     * 渲染截图列表
     */
    private void renderScreenshots(StringBuilder html, List<byte[]> screenshots) {
        if (screenshots.isEmpty()) {
            return;
        }
        
        html.append("<div class=\"section\">")
            .append("<h2>截图</h2>")
            .append("<div class=\"screenshots-grid\">");
        
        for (int i = 0; i < screenshots.size(); i++) {
            byte[] screenshot = screenshots.get(i);
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            html.append("<div class=\"screenshot-item\">")
                .append("<h3>截图 ").append(i + 1).append("</h3>")
                .append("<div class=\"screenshot-container\">")
                .append("<img src=\"data:image/png;base64,").append(base64).append("\" alt=\"Screenshot ").append(i + 1).append(">"")
                .append("</div>")
                .append("</div>");
        }
        
        html.append("</div>")
            .append("</div>");
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * 获取CSS样式
     */
    private String getCssStyles() {
        return ""
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }"
            + ".header { background-color: #4285f4; color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }"
            + ".header h1 { margin: 0; }
            .session-info { margin-top: 10px; }
            .session-info p { margin: 5px 0; }
            .summary { background-color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .stats { display: flex; flex-wrap: wrap; gap: 20px; }
            .stat-item { background-color: #f8f9fa; padding: 15px; border-radius: 6px; flex: 1; min-width: 100px; text-align: center; }
            .stat-item.success { background-color: #e6f4ea; color: #0d6e3e; }
            .stat-item.error { background-color: #fce8e6; color: #b3261e; }
            .stat-item .label { display: block; font-size: 14px; opacity: 0.7; }
            .stat-item .value { display: block; font-size: 24px; font-weight: bold; }
            .section { background-color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .section h2 { margin-top: 0; color: #333; }
            .execution-table { width: 100%; border-collapse: collapse; }
            .execution-table th, .execution-table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
            .execution-table th { background-color: #f8f9fa; font-weight: 600; }
            .execution-table tr:hover { background-color: #f8f9fa; }
            .execution-table tr.success { background-color: #f1f8e9; }
            .execution-table tr.error { background-color: #ffebee; }
            .error-detail { background-color: #ffebee; }
            .error-detail pre { margin: 0; padding: 10px; background-color: #ffcdd2; border-radius: 4px; font-size: 12px; overflow-x: auto; }
            .elements-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 15px; }
            .element-card { border: 1px solid #ddd; border-radius: 6px; padding: 15px; background-color: #f8f9fa; }
            .element-card h3 { margin-top: 0; color: #4285f4; }
            .element-card p { margin: 5px 0; font-size: 14px; }
            .screenshots-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
            .screenshot-item h3 { margin-top: 0; color: #4285f4; }
            .screenshot-container { border: 1px solid #ddd; border-radius: 4px; padding: 10px; background-color: white; }
            .screenshot-container img { max-width: 100%; height: auto; display: block; }
            @media (max-width: 768px) { .stats { flex-direction: column; } .execution-table { font-size: 12px; } .elements-grid, .screenshots-grid { grid-template-columns: 1fr; } }
            ";
    }
    
    @Override
    public String getFormat() {
        return "html";
    }
    
    @Override
    public boolean supports(String format) {
        return "html".equalsIgnoreCase(format);
    }
}