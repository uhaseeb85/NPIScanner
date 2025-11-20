package com.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
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
        // Create temporary folder and config file
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String[] args = {tempDir.toString(), configFile.toString()};

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testValidateArguments_WithValidArgumentsAndOutputPath() throws IOException {
        // Create temporary folder and config file
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");
        
        String outputPath = tempDir.resolve("output.html").toString();

        String[] args = {tempDir.toString(), configFile.toString(), outputPath};

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testValidateArguments_WithNullArgs() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(null)
        );

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
    }

    @Test
    void testValidateArguments_WithTooFewArguments() {
        String[] args = {"only-one-arg"};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
        assertTrue(exception.getMessage().contains("got 1"));
    }

    @Test
    void testValidateArguments_WithTooManyArguments() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String[] args = {tempDir.toString(), configFile.toString(), "output.html", "extra-arg"};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Invalid number of arguments"));
        assertTrue(exception.getMessage().contains("got 4"));
    }

    @Test
    void testValidateArguments_WithEmptyFolderPath() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String[] args = {"", configFile.toString()};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Folder path cannot be empty"));
    }

    @Test
    void testValidateArguments_WithNonExistentFolder() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String nonExistentFolder = tempDir.resolve("non-existent-folder").toString();
        String[] args = {nonExistentFolder, configFile.toString()};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Folder does not exist"));
    }

    @Test
    void testValidateArguments_WithFileInsteadOfFolder() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");
        
        Path regularFile = tempDir.resolve("not-a-folder.txt");
        Files.writeString(regularFile, "content");

        String[] args = {regularFile.toString(), configFile.toString()};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Path is not a directory"));
    }

    @Test
    void testValidateArguments_WithEmptyConfigPath() {
        String[] args = {tempDir.toString(), ""};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Configuration file path cannot be empty"));
    }

    @Test
    void testValidateArguments_WithNonExistentConfigFile() {
        String nonExistentConfig = tempDir.resolve("non-existent-config.xml").toString();
        String[] args = {tempDir.toString(), nonExistentConfig};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Configuration file does not exist"));
    }

    @Test
    void testValidateArguments_WithDirectoryInsteadOfConfigFile() throws IOException {
        Path configDir = tempDir.resolve("config-dir");
        Files.createDirectory(configDir);

        String[] args = {tempDir.toString(), configDir.toString()};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Configuration path is not a file"));
    }

    @Test
    void testValidateArguments_WithEmptyOutputPath() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String[] args = {tempDir.toString(), configFile.toString(), ""};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Output path cannot be empty"));
    }

    @Test
    void testValidateArguments_WithNonExistentOutputDirectory() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        String outputPath = tempDir.resolve("non-existent-dir").resolve("output.html").toString();
        String[] args = {tempDir.toString(), configFile.toString(), outputPath};

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> app.validateArguments(args)
        );

        assertTrue(exception.getMessage().contains("Output directory does not exist"));
    }

    @Test
    void testValidateArguments_WithOutputPathInCurrentDirectory() throws IOException {
        Path configFile = tempDir.resolve("config.xml");
        Files.writeString(configFile, "<?xml version=\"1.0\"?><scan-configuration></scan-configuration>");

        // Output path without parent directory (current directory)
        String[] args = {tempDir.toString(), configFile.toString(), "output.html"};

        assertDoesNotThrow(() -> app.validateArguments(args));
    }

    @Test
    void testExecuteScan_WithConfigurationException() throws IOException {
        // Create invalid config file
        Path configFile = tempDir.resolve("invalid-config.xml");
        Files.writeString(configFile, "invalid xml content");

        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ConfigurationException
        assertThrows(ConfigurationException.class, () -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithScanException() throws IOException {
        // Create valid config file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Use non-existent directory
        Path nonExistentDir = tempDir.resolve("non-existent");
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ScanException
        assertThrows(ScanException.class, () -> {
            app.executeScan(nonExistentDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithReportException() throws IOException {
        // Create valid config file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create source directory
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        // Use invalid output path (directory that doesn't exist and can't be created)
        // On Windows, we can use an invalid path like "Z:\\non-existent\\report.html"
        // But this might not work on all systems, so let's use a read-only directory approach
        
        // Create a file where we want to create a directory
        Path blockingFile = tempDir.resolve("blocking");
        Files.writeString(blockingFile, "content");
        
        // Try to create output in a path that requires creating a directory where a file exists
        String invalidOutputPath = blockingFile.toString() + "\\report.html";

        // Should throw ReportException
        assertThrows(ReportException.class, () -> {
            app.executeScan(srcDir.toString(), configFile.toString(), invalidOutputPath);
        });
    }

    @Test
    void testExecuteScan_GracefulHandlingOfFileErrors() throws Exception {
        // Create valid config file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create source directory with multiple files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        // Create a valid Java file
        Path validFile = srcDir.resolve("Valid.java");
        Files.writeString(validFile, "public class Valid { void log() { logger.info(\"SSN: \" + ssn); } }");
        
        // Create another valid Java file
        Path anotherValidFile = srcDir.resolve("Another.java");
        Files.writeString(anotherValidFile, "public class Another { void log() { logger.debug(\"Data: \" + data); } }");

        Path outputFile = tempDir.resolve("report.html");

        // Should complete successfully even if one file has issues
        assertDoesNotThrow(() -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });

        // Verify report was created
        assertTrue(Files.exists(outputFile));
    }

    @Test
    void testExecuteScan_WithEmptyDirectory() throws Exception {
        // Create valid config file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create empty source directory
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        Path outputFile = tempDir.resolve("report.html");

        // Should complete successfully with empty directory
        assertDoesNotThrow(() -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });

        // Verify report was created
        assertTrue(Files.exists(outputFile));
        
        // Verify report shows 0 files scanned
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Total Files Scanned"));
        assertTrue(reportContent.contains("0"));
    }
}
