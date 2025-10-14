package com.midscene.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告生成器类
 * 将评估结果转换为JSON、HTML、XML、CSV等多种格式的报告
 */
public class ReportGenerator {
    private final Configuration freemarkerConfig;
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    private final CsvMapper csvMapper;
    private final String reportDirectory;
    
    /**
     * 默认构造函数
     */
    public ReportGenerator() {
        this("./reports");
    }
    
    /**
     * 构造函数
     */
    public ReportGenerator(String reportDirectory) {
        this.reportDirectory = reportDirectory;
        
        // 初始化FreeMarker配置
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setClassForTemplateLoading(getClass(), "/templates");
        
        // 初始化Jackson映射器
        jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        csvMapper = new CsvMapper();
    }
    
    /**
     * 生成报告
     */
    public String generateReport(EvaluationSummary summary, EvaluationConfig.ReportFormat format) throws IOException {
        switch (format) {
            case JSON:
                return generateJsonReport(summary);
            case HTML:
                return generateHtmlReport(summary);
            case XML:
                return generateXmlReport(summary);
            case CSV:
                return generateCsvReport(summary);
            case TEXT:
                return generateTextReport(summary);
            default:
                return generateJsonReport(summary);
        }
    }
    
    /**
     * 保存报告到文件
     */
    public File saveReport(EvaluationSummary summary, EvaluationConfig.ReportFormat format) throws IOException {
        // 确保报告目录存在
        createReportDirectory();
        
        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("evaluation_report_%s.%s", timestamp, getFileExtension(format));
        Path filePath = Paths.get(reportDirectory, fileName);
        
        // 生成报告内容
        String reportContent = generateReport(summary, format);
        
        // 写入文件
        Files.writeString(filePath, reportContent, StandardCharsets.UTF_8);
        
        return filePath.toFile();
    }
    
    /**
     * 生成JSON格式报告
     */
    public String generateJsonReport(EvaluationSummary summary) throws IOException {
        return jsonMapper.writeValueAsString(summary);
    }
    
    /**
     * 生成HTML格式报告
     */
    public String generateHtmlReport(EvaluationSummary summary) throws IOException {
        try {
            Map<String, Object> dataModel = prepareDataModel(summary);
            
            // 尝试使用模板文件
            try {
                Template template = freemarkerConfig.getTemplate("report_template.html");
                StringWriter writer = new StringWriter();
                template.process(dataModel, writer);
                return writer.toString();
            } catch (IOException | TemplateException e) {
                // 如果模板不存在或无法处理，生成简单的HTML报告
                return generateSimpleHtmlReport(summary, dataModel);
            }
        } catch (Exception e) {
            throw new IOException("生成HTML报告失败", e);
        }
    }
    
    /**
     * 生成XML格式报告
     */
    public String generateXmlReport(EvaluationSummary summary) throws IOException {
        return xmlMapper.writeValueAsString(summary);
    }
    
    /**
     * 生成CSV格式报告
     */
    public String generateCsvReport(EvaluationSummary summary) throws IOException {
        List<Map<String, Object>> csvData = summary.getResults().stream()
            .map(result -> {
                Map<String, Object> row = new HashMap<>();
                row.put("testId", result.getTestId());
                row.put("testName", result.getTestName());
                row.put("passed", result.isPassed());
                row.put("duration", result.getDuration());
                row.put("errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "");
                return row;
            })
            .collect(Collectors.toList());
        
        if (csvData.isEmpty()) {
            return "testId,testName,passed,duration,errorMessage\n";
        }
        
        CsvSchema schema = csvMapper.schemaFor(csvData.get(0).getClass()).withHeader();
        return csvMapper.writer(schema).writeValueAsString(csvData);
    }
    
    /**
     * 生成文本格式报告
     */
    public String generateTextReport(EvaluationSummary summary) {
        return summary.generateSummaryReport();
    }
    
    /**
     * 准备数据模型
     */
    private Map<String, Object> prepareDataModel(EvaluationSummary summary) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("summary", summary);
        dataModel.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dataModel.put("failedResults", summary.getFailedResults());
        dataModel.put("passedResults", summary.getPassedResults());
        dataModel.put("slowestResults", summary.getSlowestResults(5));
        return dataModel;
    }
    
    /**
     * 生成简单的HTML报告
     */
    private String generateSimpleHtmlReport(EvaluationSummary summary, Map<String, Object> dataModel) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>评估报告</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".summary { background-color: #f0f0f0; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
        html.append(".results { margin-top: 20px; }");
        html.append(".result { padding: 10px; margin-bottom: 10px; border-radius: 5px; }");
        html.append(".passed { background-color: #d4edda; color: #155724; }");
        html.append(".failed { background-color: #f8d7da; color: #721c24; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>评估报告</h1>");
        html.append("<p>生成时间: " + dataModel.get("timestamp") + "</p>");
        
        // 摘要部分
        html.append("<div class=\"summary\">");
        html.append("<h2>摘要</h2>");
        html.append("<p>总测试数: " + summary.getTotalTests() + "</p>");
        html.append("<p>通过数: " + summary.getPassedTests() + "</p>");
        html.append("<p>失败数: " + summary.getFailedTests() + "</p>");
        html.append("<p>通过率: " + String.format("%.2f", summary.getPassRate()) + "%</p>");
        html.append("<p>平均执行时间: " + summary.getAverageExecutionTime() + "ms</p>");
        html.append("</div>");
        
        // 结果表格
        html.append("<div class=\"results\">");
        html.append("<h2>测试结果详情</h2>");
        html.append("<table>");
        html.append("<tr><th>测试名称</th><th>状态</th><th>执行时间(ms)</th><th>错误信息</th></tr>");
        
        for (EvaluationResult result : summary.getResults()) {
            html.append("<tr class=\"" + (result.isPassed() ? "passed" : "failed") + "\">");
            html.append("<td>" + result.getTestName() + "</td>");
            html.append("<td>" + (result.isPassed() ? "通过" : "失败") + "</td>");
            html.append("<td>" + result.getDuration() + "</td>");
            html.append("<td>" + (result.getErrorMessage() != null ? result.getErrorMessage() : "") + "</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(EvaluationConfig.ReportFormat format) {
        switch (format) {
            case JSON:
                return "json";
            case HTML:
                return "html";
            case XML:
                return "xml";
            case CSV:
                return "csv";
            case TEXT:
                return "txt";
            default:
                return "json";
        }
    }
    
    /**
     * 创建报告目录
     */
    private void createReportDirectory() throws IOException {
        Files.createDirectories(Paths.get(reportDirectory));
    }
    
    /**
     * 生成多格式报告
     */
    public Map<EvaluationConfig.ReportFormat, File> generateMultiFormatReports(EvaluationSummary summary, 
                                                                             List<EvaluationConfig.ReportFormat> formats) 
            throws IOException {
        Map<EvaluationConfig.ReportFormat, File> reports = new HashMap<>();
        
        for (EvaluationConfig.ReportFormat format : formats) {
            File reportFile = saveReport(summary, format);
            reports.put(format, reportFile);
        }
        
        return reports;
    }
}