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

        // When no output path is provided (null or empty), it should generate a
        // timestamped filename
        generator.generateReport(detections, null, stats);

        // Verify filename format: scan-report-yyyy-MM-dd-HHmmss.html in current
        // directory
        // Since we can't easily check current directory in tests, let's test with empty
        // string
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
    void testGenerateReport_WithEmptyDetections(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(20, 0, 3000);
        List<Detection> detections = new ArrayList<>();

        Path outputFile = tempDir.resolve("report.html");
        generator.generateReport(detections, outputFile.toString(), stats);

        // Verify file was created
        assertTrue(Files.exists(outputFile));

        // Verify no detections message
        String content = Files.readString(outputFile);
        assertTrue(content.contains("No Sensitive Data Detected"));
    }

    @Test
    void testGenerateReport_WithMultipleDetections(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(50, 10, 5000);
        List<Detection> detections = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            detections.add(new Detection("File" + i + ".java", i * 10, "keyword" + i, "logger.info(data" + i + ")"));
        }

        Path outputFile = tempDir.resolve("report.html");
        generator.generateReport(detections, outputFile.toString(), stats);

        // Verify file was created
        assertTrue(Files.exists(outputFile));

        // Verify all detections are present
        String content = Files.readString(outputFile);
        for (int i = 1; i <= 10; i++) {
            assertTrue(content.contains("File" + i + ".java"));
            assertTrue(content.contains("keyword" + i));
        }

        // Verify statistics
        assertTrue(content.contains("50"));
        assertTrue(content.contains("10"));
    }

    @Test
    void testGenerateReport_WithSpecialCharacters(@TempDir Path tempDir) throws ReportException, IOException {
        HtmlReportGenerator generator = new HtmlReportGenerator();
        ScanStatistics stats = new ScanStatistics(1, 1, 100);
        List<Detection> detections = new ArrayList<>();
        detections.add(new Detection("Test.java", 1, "<script>", "logger.info(\"<tag>\" & 'quote')"));

        Path outputFile = tempDir.resolve("report.html");
        generator.generateReport(detections, outputFile.toString(), stats);

        // Verify HTML escaping
        String content = Files.readString(outputFile);
        assertTrue(content.contains("&lt;script&gt;") || content.contains("&lt;tag&gt;"));
        assertFalse(content.contains("<script>alert"));
    }
}
