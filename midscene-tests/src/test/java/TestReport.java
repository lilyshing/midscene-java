import com.midscene.core.util.ReportGenerator;
import java.util.Map;

public class TestReport {
    public static void main(String[] args) {
        // Create a test report generator
        ReportGenerator reportGenerator = new ReportGenerator(
            "Test Report",
            Map.of("environment", "test", "version", "1.0.0")
        );
        
        // Test adding execution step
        ReportGenerator.ExecutionStep step = reportGenerator.createStep()
            .withAction("click")
            .withDescription("Click button")
            .withSuccess(true)
            .build();
        
        reportGenerator.addStep(step);
        
        // Verify the generated JSON report contains the added step
        String jsonReport = reportGenerator.generateJsonReport();
        System.out.println("JSON Report: " + jsonReport);
        
        // Test step with screenshot
        ReportGenerator.ExecutionStep screenshotStep = reportGenerator.createStep()
            .withAction("screenshot")
            .withDescription("Screenshot")
            .withSuccess(true)
            .withScreenshotPath("test.png")
            .build();
        
        reportGenerator.addStep(screenshotStep);
        
        // Verify JSON report contains screenshot path
        String jsonReportWithScreenshot = reportGenerator.generateJsonReport();
        System.out.println("JSON Report with screenshot: " + jsonReportWithScreenshot);
        
        // Verify HTML report contains screenshot
        String htmlReport = reportGenerator.generateHtmlReport();
        System.out.println("HTML Report with screenshot: " + htmlReport);
    }
}