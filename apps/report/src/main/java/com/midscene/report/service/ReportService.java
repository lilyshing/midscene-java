package com.midscene.report.service;

import java.io.IOException;
import java.util.Map;

public interface ReportService {
    /**
     * 生成HTML格式的报告
     * @param reportData 报告数据
     * @return HTML报告内容的字节数组
     * @throws IOException IO异常
     */
    byte[] generateHtmlReport(Map<String, Object> reportData) throws IOException;

    /**
     * 生成PDF格式的报告
     * @param reportData 报告数据
     * @return PDF报告内容的字节数组
     * @throws IOException IO异常
     */
    byte[] generatePdfReport(Map<String, Object> reportData) throws IOException;

    /**
     * 生成JSON格式的报告
     * @param reportData 报告数据
     * @return JSON报告内容的字节数组
     * @throws IOException IO异常
     */
    byte[] generateJsonReport(Map<String, Object> reportData) throws IOException;
}