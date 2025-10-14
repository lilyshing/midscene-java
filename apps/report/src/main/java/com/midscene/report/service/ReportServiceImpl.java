package com.midscene.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public byte[] generateHtmlReport(Map<String, Object> reportData) throws IOException {
        Context context = new Context();
        context.setVariables(reportData);
        String htmlContent = templateEngine.process("report-template", context);
        return htmlContent.getBytes("UTF-8");
    }

    @Override
    public byte[] generatePdfReport(Map<String, Object> reportData) throws IOException {
        // 由于PDF生成依赖暂时不可用，我们生成HTML格式并将其标记为PDF
        // 后续可以添加iText或其他PDF生成库来实现完整的PDF转换
        Context context = new Context();
        context.setVariables(reportData);
        String htmlContent = templateEngine.process("report-template", context);
        
        // 在HTML中添加PDF格式的标记
        htmlContent = "<!-- PDF Placeholder - PDF generation capability will be added later -->\n" + htmlContent;
        
        return htmlContent.getBytes("UTF-8");
    }

    @Override
    public byte[] generateJsonReport(Map<String, Object> reportData) throws IOException {
        return objectMapper.writeValueAsBytes(reportData);
    }
}