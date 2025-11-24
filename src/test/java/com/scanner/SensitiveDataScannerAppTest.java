package com.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataScannerAppTest {

    private SensitiveDataScannerApp app;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        app = new SensitiveDataScannerApp();
    }

    @Test
    void testValidateArguments_WithValidArguments() throws IOException {
        // Create config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        String[] args = { configDir.toString() };

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testValidateArguments_WithValidArgumentsAndOutputPath() throws IOException {
        // Create config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        String outputPath = tempDir.resolve("output.html").toString();

        String[] args = { configDir.toString(), outputPath };

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testValidateArguments_WithNullArgs() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(null));

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
    }

    @Test
    void testValidateArguments_WithTooFewArguments() {
        String[] args = {};

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
        assertTrue(exception.getMessage().contains("got 0"));
    }

    @Test
    void testValidateArguments_WithTooManyArguments() throws IOException {
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        String[] args = { configDir.toString(), "output.html", "extra-arg" };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
        assertTrue(exception.getMessage().contains("got 3"));
    }

    @Test
    void testValidateArguments_WithEmptyConfigPath() {
        String[] args = { "" };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Configuration directory path cannot be empty"));
    }

    @Test
    void testValidateArguments_WithNonExistentConfigDir() {
        String nonExistentConfig = tempDir.resolve("non-existent-config").toString();
        String[] args = { nonExistentConfig };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Configuration directory does not exist"));
    }

    @Test
    void testValidateArguments_WithFileInsteadOfConfigDir() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "content");

        String[] args = { configFile.toString() };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Configuration path is not a directory"));
    }

    @Test
    void testValidateArguments_WithEmptyOutputPath() throws IOException {
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        String[] args = { configDir.toString(), "" };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> app.validateArguments(args));

        assertTrue(exception.getMessage().contains("Output path cannot be empty"));
    }

    @Test
    void testValidateArguments_WithNonExistentOutputDirectory() throws IOException {
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        String outputPath = tempDir.resolve("non-existent-dir").resolve("output.html").toString();
        String[] args = { configDir.toString(), outputPath };

        // Should not throw exception anymore - directory will be created automatically
        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testValidateArguments_WithOutputPathInCurrentDirectory() throws IOException {
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        // Output path without parent directory (current directory)
        String[] args = { configDir.toString(), "output.html" };

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testExecuteScan_WithConfigurationException() throws IOException {
        // Create config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        // Create invalid config file
        Path configFile = configDir.resolve("scan-directories.xml");
        Files.writeString(configFile, "invalid xml content");

        Path outputFile = tempDir.resolve("report.html");

        // Should throw ConfigurationException
        assertThrows(ConfigurationException.class, () -> {
            app.executeScan(configDir.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithScanException() throws IOException {
        // Create valid config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        // Create valid config file but with non-existent directory
        Path nonExistentDir = tempDir.resolve("non-existent");
        Path configFile = configDir.resolve("scan-directories.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scan-directories>\n" +
                "    <directory>" + nonExistentDir.toString() + "</directory>\n" +
                "</scan-directories>";
        Files.writeString(configFile, configContent);

        Path outputFile = tempDir.resolve("report.html");

        // Should throw ScanException for non-existent directory
        assertThrows(ScanException.class, () -> {
            app.executeScan(configDir.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithReportException() throws IOException {
        // Create source directory
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        // Create valid config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        Path configFile = configDir.resolve("scan-directories.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scan-directories>\n" +
                "    <directory>" + srcDir.toString() + "</directory>\n" +
                "</scan-directories>";
        Files.writeString(configFile, configContent);

        // Create a file where we want to create a directory
        Path blockingFile = tempDir.resolve("blocking");
        Files.writeString(blockingFile, "content");

        // Try to create output in a path that requires creating a directory where a
        // file exists
        String invalidOutputPath = blockingFile.toString() + "\\report.html";

        // Should throw ReportException
        assertThrows(ReportException.class, () -> {
            app.executeScan(configDir.toString(), invalidOutputPath);
        });
    }

    @Test
    void testExecuteScan_GracefulHandlingOfFileErrors() throws Exception {
        // Create source directory with multiple files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        // Create valid config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        Path configFile = configDir.resolve("scan-directories.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scan-directories>\n" +
                "    <directory>" + srcDir.toString() + "</directory>\n" +
                "</scan-directories>";
        Files.writeString(configFile, configContent);

        // Also create keywords.xml
        Path keywordsFile = configDir.resolve("keywords.xml");
        Files.writeString(keywordsFile, "<keywords><keyword type=\"plain\">ssn</keyword></keywords>");

        // Create a valid Java file
        Path validFile = srcDir.resolve("Valid.java");
        Files.writeString(validFile, "public class Valid { void log() { logger.info(\"SSN: \" + ssn); } }");

        // Create another valid Java file
        Path anotherValidFile = srcDir.resolve("Another.java");
        Files.writeString(anotherValidFile, "public class Another { void log() { logger.debug(\"Data: \" + data); } }");

        Path outputFile = tempDir.resolve("report.html");

        // Should complete successfully even if one file has issues
        assertDoesNotThrow(() -> {
            app.executeScan(configDir.toString(), outputFile.toString());
        });

        // Verify report was created
        assertTrue(Files.exists(outputFile));
    }

    @Test
    void testExecuteScan_WithEmptyDirectory() throws Exception {
        // Create empty source directory
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        // Create valid config directory
        Path configDir = tempDir.resolve("config");
        Files.createDirectory(configDir);

        Path configFile = configDir.resolve("scan-directories.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scan-directories>\n" +
                "    <directory>" + srcDir.toString() + "</directory>\n" +
                "</scan-directories>";
        Files.writeString(configFile, configContent);

        // Also create keywords.xml
        Path keywordsFile = configDir.resolve("keywords.xml");
        Files.writeString(keywordsFile, "<keywords><keyword type=\"plain\">ssn</keyword></keywords>");

        Path outputDir = tempDir.resolve("output");
        Files.createDirectory(outputDir);
        Path outputFile = outputDir.resolve("report.html");

        // Should complete successfully with empty directory
        assertDoesNotThrow(() -> {
            app.executeScan(configDir.toString(), outputFile.toString());
        });

        // Verify report was created
        assertTrue(Files.exists(outputFile));

        // Verify report shows 0 files scanned
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Files Scanned"));
        assertTrue(reportContent.contains("0"));
    }
}
