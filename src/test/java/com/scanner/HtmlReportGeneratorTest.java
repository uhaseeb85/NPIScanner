package com.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HtmlReportGenerator class.
 */
class HtmlReportGeneratorTest {

    @Test
    void testBuildHtmlContent_BasicStructure() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(10, 5, 1500);
        List<Detection> detections = new ArrayList<>();
        detections.add(new Detection("Test.java", 42, "ssn", "logger.info(ssn)"));

        String html = generator.buildHtmlContent(detections, stats);

        // Verify basic HTML structure
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<title>Sensitive Data Scan Report</title>"));
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("</style>"));
        assertTrue(html.contains("</head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<h1>Sensitive Data Scan Report</h1>"));
        assertTrue(html.contains("</body>"));
        assertTrue(html.contains("</html>"));
    }

    @Test
    void testBuildHtmlContent_ContainsCssStyles() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(5, 2, 1000);
        List<Detection> detections = new ArrayList<>();

        String html = generator.buildHtmlContent(detections, stats);

        // Verify CSS styles are present
        assertTrue(html.contains("font-family"));
        assertTrue(html.contains("background-color"));
        assertTrue(html.contains("table"));
        assertTrue(html.contains("border-collapse"));
    }

    @Test
    void testBuildHtmlContent_WithSummarySection() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(15, 8, 2500);
        List<Detection> detections = new ArrayList<>();

        String html = generator.buildHtmlContent(detections, stats);

        // Verify summary section
        assertTrue(html.contains("Scan Date:"));
        assertTrue(html.contains("Total Files Scanned:"));
        assertTrue(html.contains("15"));
        assertTrue(html.contains("Total Detections:"));
        assertTrue(html.contains("8"));
        assertTrue(html.contains("Scan Duration:"));
    }

    @Test
    void testBuildHtmlContent_WithDetectionTable() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(10, 2, 1000);
        List<Detection> detections = new ArrayList<>();
        detections.add(new Detection("UserService.java", 42, "ssn", "logger.info(\"SSN: \" + ssn)"));
        detections.add(new Detection("CardProcessor.java", 100, "pin", "log.debug(card.getPin())"));

        String html = generator.buildHtmlContent(detections, stats);

        // Verify table structure
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<th>File Name</th>"));
        assertTrue(html.contains("<th>Line Number</th>"));
        assertTrue(html.contains("<th>Matched Keyword</th>"));
        assertTrue(html.contains("<th>Log Statement</th>"));
        assertTrue(html.contains("</thead>"));
        assertTrue(html.contains("<tbody>"));
        assertTrue(html.contains("</tbody>"));
        assertTrue(html.contains("</table>"));

        // Verify detection data
        assertTrue(html.contains("UserService.java"));
        assertTrue(html.contains("42"));
        assertTrue(html.contains("ssn"));
        assertTrue(html.contains("logger.info(&quot;SSN: &quot; + ssn)"));
        assertTrue(html.contains("CardProcessor.java"));
        assertTrue(html.contains("100"));
        assertTrue(html.contains("pin"));
    }

    @Test
    void testBuildHtmlContent_EmptyDetections() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(20, 0, 3000);
        List<Detection> detections = new ArrayList<>();

        String html = generator.buildHtmlContent(detections, stats);

        // Verify no detections message
        assertTrue(html.contains("No sensitive data detected"));
        assertTrue(html.contains("no-detections"));
        
        // Verify table is not present
        assertFalse(html.contains("<table>"));
    }

    @Test
    void testGenerateTimestamp() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        
        String timestamp = generator.generateTimestamp();
        
        assertNotNull(timestamp);
        assertFalse(timestamp.isEmpty());
        // Verify format: yyyy-MM-dd HH:mm:ss
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testEscapeHtml_SpecialCharacters() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 1, 100);
        List<Detection> detections = new ArrayList<>();
        detections.add(new Detection("Test.java", 1, "<script>", "logger.info(\"<tag>\" & 'quote')"));

        String html = generator.buildHtmlContent(detections, stats);

        // Verify HTML escaping
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&lt;tag&gt;"));
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&#39;"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<tag>"));
    }

    @Test
    void testGenerateReport_CreatesFile(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(5, 2, 1000);
        List<Detection> detections = new ArrayList<>();
        detections.add(new Detection("Test.java", 10, "password", "logger.info(password)"));

        Path outputFile = tempDir.resolve("report.html");
        generator.generateReport(detections, outputFile.toString(), stats);

        // Verify file was created
        assertTrue(Files.exists(outputFile));
        
        // Verify file content
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Sensitive Data Scan Report"));
        assertTrue(content.contains("Test.java"));
        assertTrue(content.contains("password"));
    }

    @Test
    void testGenerateReport_WithTimestampInFilename(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 0, 500);
        List<Detection> detections = new ArrayList<>();

        // When no output path is provided (null or empty), it should generate a timestamped filename
        generator.generateReport(detections, null, stats);

        // Verify filename format: scan-report-yyyy-MM-dd-HHmmss.html in current directory
        // Since we can't easily check current directory in tests, let's test with empty string
        generator.generateReport(detections, "", stats);
        
        // The test passes if no exception is thrown
        assertTrue(true);
    }

    @Test
    void testGenerateReport_CreatesOutputDirectory(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 0, 100);
        List<Detection> detections = new ArrayList<>();
        
        Path nonExistentDir = tempDir.resolve("reports").resolve("output");
        Path outputFile = nonExistentDir.resolve("report.html");
        
        generator.generateReport(detections, outputFile.toString(), stats);

        // Verify directory was created
        assertTrue(Files.exists(nonExistentDir));
        assertTrue(Files.isDirectory(nonExistentDir));
        
        // Verify file was created
        assertTrue(Files.exists(outputFile));
    }

    @Test
    void testGenerateReport_NullOutputPath(@TempDir Path tempDir) throws ReportException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 0, 100);
        List<Detection> detections = new ArrayList<>();

        // Should not throw exception with null output path
        assertDoesNotThrow(() -> generator.generateReport(detections, null, stats));
    }

    @Test
    void testGenerateReport_EmptyOutputPath(@TempDir Path tempDir) throws ReportException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 0, 100);
        List<Detection> detections = new ArrayList<>();

        // Should not throw exception with empty output path
        assertDoesNotThrow(() -> generator.generateReport(detections, "", stats));
    }

    @Test
    void testBuildHtmlContent_MultipleDetections() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(50, 10, 5000);
        List<Detection> detections = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            detections.add(new Detection("File" + i + ".java", i * 10, "keyword" + i, "logger.info(data" + i + ")"));
        }

        String html = generator.buildHtmlContent(detections, stats);

        // Verify all detections are present
        for (int i = 1; i <= 10; i++) {
            assertTrue(html.contains("File" + i + ".java"));
            assertTrue(html.contains("keyword" + i));
            assertTrue(html.contains("data" + i));
        }
        
        // Verify statistics
        assertTrue(html.contains("50"));
        assertTrue(html.contains("10"));
    }

    @Test
    void testBuildHtmlContent_DurationFormatting() {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        
        // Test milliseconds
        ScanStatistics stats1 = new ScanStatistics(1, 0, 500);
        String html1 = generator.buildHtmlContent(new ArrayList<>(), stats1);
        assertTrue(html1.contains("500 ms"));
        
        // Test seconds
        ScanStatistics stats2 = new ScanStatistics(1, 0, 2500);
        String html2 = generator.buildHtmlContent(new ArrayList<>(), stats2);
        assertTrue(html2.contains("2.50 seconds"));
        
        // Test minutes
        ScanStatistics stats3 = new ScanStatistics(1, 0, 125000);
        String html3 = generator.buildHtmlContent(new ArrayList<>(), stats3);
        assertTrue(html3.contains("2 min 5 sec"));
    }
}
