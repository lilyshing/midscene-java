package com.midscene.report.controller;

import com.midscene.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Report Generator");
        return "index";
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam String format,
            @RequestBody Map<String, Object> reportData) {
        try {
            byte[] reportContent;
            MediaType mediaType;
            String fileName;

            switch (format.toLowerCase()) {
                case "pdf":
                    reportContent = reportService.generatePdfReport(reportData);
                    mediaType = MediaType.APPLICATION_PDF;
                    fileName = "report.pdf";
                    break;
                case "json":
                    reportContent = reportService.generateJsonReport(reportData);
                    mediaType = MediaType.APPLICATION_JSON;
                    fileName = "report.json";
                    break;
                case "html":
                default:
                    reportContent = reportService.generateHtmlReport(reportData);
                    mediaType = MediaType.TEXT_HTML;
                    fileName = "report.html";
                    break;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("inline", fileName);

            return new ResponseEntity<>(reportContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating report".getBytes());
        }
    }

    @GetMapping("/form")
    public String reportForm(Model model) {
        model.addAttribute("title", "Report Form");
        return "report-form";
    }
}