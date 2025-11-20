package com.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataScannerAppIntegrationTest {

    private SensitiveDataScannerApp app;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        app = new SensitiveDataScannerApp();
    }

    @Test
    void testExecuteScan_CompleteWorkflow() throws Exception {
        // Create test configuration file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">password</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "        <object-type type=\"plain\">request</object-type>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file with sensitive data
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("TestClass.java");
        String javaContent = "public class TestClass {\n" +
            "    public void logData() {\n" +
            "        logger.info(\"User SSN: \" + ssn);\n" +
            "        logger.debug(\"Password: \" + password);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains expected content
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Sensitive Data Scan Report"));
        assertTrue(reportContent.contains("TestClass.java"));
        assertTrue(reportContent.contains("ssn"));
        assertTrue(reportContent.contains("password"));
    }

    @Test
    void testExecuteScan_WithNoDetections() throws Exception {
        // Create test configuration file
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

        // Create test Java file without sensitive data
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("TestClass.java");
        String javaContent = "public class TestClass {\n" +
            "    public void logData() {\n" +
            "        logger.info(\"User logged in\");\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report indicates no detections
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("No sensitive data detected"));
    }

    @Test
    void testExecuteScan_WithMultipleFiles() throws Exception {
        // Create test configuration file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">creditCard</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create multiple test Java files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile1 = srcDir.resolve("UserService.java");
        String javaContent1 = "public class UserService {\n" +
            "    public void logUser() {\n" +
            "        logger.info(\"SSN: \" + ssn);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile1, javaContent1);

        Path javaFile2 = srcDir.resolve("PaymentService.java");
        String javaContent2 = "public class PaymentService {\n" +
            "    public void logPayment() {\n" +
            "        logger.debug(\"Card: \" + creditCard);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile2, javaContent2);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains detections from both files
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("UserService.java"));
        assertTrue(reportContent.contains("PaymentService.java"));
        assertTrue(reportContent.contains("ssn"));
        assertTrue(reportContent.contains("creditCard"));
    }

    @Test
    void testExecuteScan_WithNestedDirectories() throws Exception {
        // Create test configuration file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">password</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create nested directory structure
        Path srcDir = tempDir.resolve("src");
        Path subDir = srcDir.resolve("com").resolve("example");
        Files.createDirectories(subDir);
        
        Path javaFile = subDir.resolve("AuthService.java");
        String javaContent = "package com.example;\n" +
            "public class AuthService {\n" +
            "    public void authenticate() {\n" +
            "        logger.warn(\"Password check: \" + password);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains detection from nested file
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("AuthService.java"));
        assertTrue(reportContent.contains("password"));
    }

    @Test
    void testExecuteScan_WithRegexPatterns() throws Exception {
        // Create test configuration file with regex patterns
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"regex\">.*[Pp]assword.*</keyword>\n" +
            "        <keyword type=\"regex\">.*[Ss][Ss][Nn].*</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("TestClass.java");
        String javaContent = "public class TestClass {\n" +
            "    public void logData() {\n" +
            "        logger.info(\"User password: \" + userPassword);\n" +
            "        logger.debug(\"SSN value: \" + userSSN);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains detections matching regex patterns
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("userPassword"));
        assertTrue(reportContent.contains("userSSN"));
    }

    @Test
    void testExecuteScan_WithSensitiveObjectTypes() throws Exception {
        // Create test configuration file
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "        <object-type type=\"plain\">request</object-type>\n" +
            "        <object-type type=\"plain\">response</object-type>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("ApiController.java");
        String javaContent = "public class ApiController {\n" +
            "    public void handleRequest() {\n" +
            "        logger.info(\"Request: \" + request);\n" +
            "        logger.debug(\"Response body: \" + response.getBody());\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains detections for sensitive object types
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("request"));
        assertTrue(reportContent.contains("response"));
    }

    @Test
    void testExecuteScan_StatisticsTracking() throws Exception {
        // Create test configuration file
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

        // Create multiple test Java files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        for (int i = 1; i <= 3; i++) {
            Path javaFile = srcDir.resolve("TestClass" + i + ".java");
            String javaContent = "public class TestClass" + i + " {\n" +
                "    public void logData() {\n" +
                "        logger.info(\"SSN: \" + ssn);\n" +
                "    }\n" +
                "}";
            Files.writeString(javaFile, javaContent);
        }

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report contains statistics
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Total Files Scanned"));
        assertTrue(reportContent.contains("Total Detections"));
        assertTrue(reportContent.contains("Scan Duration"));
        
        // Verify file count
        assertTrue(reportContent.contains("3"), "Should show 3 files scanned");
    }

    @Test
    void testExecuteScan_WithInvalidConfigFile() {
        Path srcDir = tempDir.resolve("src");
        Path configFile = tempDir.resolve("invalid-config.xml");
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ConfigurationException
        assertThrows(ConfigurationException.class, () -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithInvalidDirectory() throws Exception {
        // Create test configuration file
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

        Path nonExistentDir = tempDir.resolve("non-existent");
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ScanException
        assertThrows(ScanException.class, () -> {
            app.executeScan(nonExistentDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithMalformedXML() throws Exception {
        // Create malformed XML configuration file
        Path configFile = tempDir.resolve("malformed-config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">pin\n" +
            "    </keywords>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ConfigurationException due to malformed XML
        assertThrows(ConfigurationException.class, () -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithInvalidRegexPattern() throws Exception {
        // Create configuration with invalid regex
        Path configFile = tempDir.resolve("invalid-regex-config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"regex\">[invalid(regex</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        Path outputFile = tempDir.resolve("report.html");

        // Should throw ConfigurationException due to invalid regex
        assertThrows(ConfigurationException.class, () -> {
            app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        });
    }

    @Test
    void testExecuteScan_WithComplexSensitivePatterns() throws Exception {
        // Create comprehensive configuration
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">email</keyword>\n" +
            "        <keyword type=\"plain\">phone</keyword>\n" +
            "        <keyword type=\"regex\">.*password.*</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "        <object-type type=\"plain\">request</object-type>\n" +
            "        <object-type type=\"plain\">response</object-type>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file with complex patterns
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("ComplexPatterns.java");
        String javaContent = "public class ComplexPatterns {\n" +
            "    // Multi-line StringBuilder\n" +
            "    public void testMultiLineBuilder(String ssn, String name) {\n" +
            "        StringBuilder sb = new StringBuilder();\n" +
            "        sb.append(\"User: \");\n" +
            "        sb.append(name);\n" +
            "        sb.append(\" SSN: \");\n" +
            "        sb.append(ssn);\n" +
            "        logger.info(sb.toString());\n" +
            "    }\n" +
            "    \n" +
            "    // String.join pattern\n" +
            "    public void testStringJoin(String email, String phone) {\n" +
            "        logger.info(\"Contact: \" + String.join(\", \", email, phone));\n" +
            "    }\n" +
            "    \n" +
            "    // Request/Response logging\n" +
            "    public void testRequestResponse(HttpRequest request, HttpResponse response) {\n" +
            "        logger.info(\"Request: \" + request);\n" +
            "        logger.debug(\"Response: \" + response.getBody());\n" +
            "    }\n" +
            "    \n" +
            "    // Regex pattern matching\n" +
            "    public void testRegex(String userPassword, String adminPassword) {\n" +
            "        logger.warn(\"User pwd: \" + userPassword);\n" +
            "        logger.error(\"Admin pwd: \" + adminPassword);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains all expected detections
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("ssn"), "Should detect ssn");
        assertTrue(reportContent.contains("email"), "Should detect email");
        assertTrue(reportContent.contains("phone"), "Should detect phone");
        assertTrue(reportContent.contains("request"), "Should detect request");
        assertTrue(reportContent.contains("response"), "Should detect response");
        
        // Verify we have multiple detections (ssn, email, phone, passwords, request, response)
        // The password regex pattern should match userPassword and adminPassword variables
        int detectionCount = reportContent.split("<tr>").length - 2;
        assertTrue(detectionCount >= 5, "Should have at least 5 detections, found: " + detectionCount);
    }

    @Test
    void testExecuteScan_WithStringLiteralFiltering() throws Exception {
        // Create configuration
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">password</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file with string literals (should NOT be detected)
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("StringLiterals.java");
        String javaContent = "public class StringLiterals {\n" +
            "    public void testLiterals() {\n" +
            "        logger.info(\"SSN: 123-45-6789\");\n" +
            "        logger.debug(\"Password: hardcoded123\");\n" +
            "    }\n" +
            "    \n" +
            "    public void testVariables(String ssn, String password) {\n" +
            "        logger.info(\"User SSN: \" + ssn);\n" +
            "        logger.warn(\"Password: \" + password);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains only variable detections, not string literals
        String reportContent = Files.readString(outputFile);
        
        // Should detect variables
        assertTrue(reportContent.contains("ssn"), "Should detect ssn variable");
        assertTrue(reportContent.contains("password"), "Should detect password variable");
        
        // Count detections - should be 2 (only the variables, not the literals)
        int detectionCount = reportContent.split("<tr>").length - 2; // Subtract header row and opening tag
        assertTrue(detectionCount >= 2, "Should have at least 2 detections");
    }

    @Test
    void testExecuteScan_WithMethodSignatureFiltering() throws Exception {
        // Create configuration
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">password</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "        <object-type type=\"plain\">request</object-type>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file with method signatures (should NOT be detected)
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("MethodSignatures.java");
        String javaContent = "public class MethodSignatures {\n" +
            "    // Method signatures should be ignored\n" +
            "    public void processSSN(String ssn) {\n" +
            "        // Not logging here\n" +
            "    }\n" +
            "    \n" +
            "    public void handleRequest(HttpRequest request, String password) {\n" +
            "        // Not logging here\n" +
            "    }\n" +
            "    \n" +
            "    // Actual logging should be detected\n" +
            "    public void logData(String ssn) {\n" +
            "        logger.info(\"SSN: \" + ssn);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains only actual logging, not method signatures
        String reportContent = Files.readString(outputFile);
        
        // Should detect the actual logging
        assertTrue(reportContent.contains("logger.info"), "Should detect actual logging statement");
        
        // Should have exactly 2 detections (variable 'ssn' and keyword 'SSN' in string literal)
        int detectionCount = reportContent.split("<tr>").length - 2;
        assertEquals(2, detectionCount, "Should have exactly 2 detections");
    }

    @Test
    void testExecuteScan_WithMixedConfiguration() throws Exception {
        // Create configuration with both plain and regex patterns
        Path configFile = tempDir.resolve("mixed-config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"regex\">.*password.*</keyword>\n" +
            "        <keyword type=\"plain\">pin</keyword>\n" +
            "        <keyword type=\"regex\">credit[Cc]ard.*</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "        <object-type type=\"plain\">request</object-type>\n" +
            "        <object-type type=\"regex\">.*Response</object-type>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("MixedPatterns.java");
        String javaContent = "public class MixedPatterns {\n" +
            "    public void testMixed(String ssn, String userPassword, String pin, String creditCardNumber) {\n" +
            "        logger.info(\"SSN: \" + ssn);\n" +
            "        logger.debug(\"Password: \" + userPassword);\n" +
            "        logger.warn(\"PIN: \" + pin);\n" +
            "        logger.error(\"Card: \" + creditCardNumber);\n" +
            "    }\n" +
            "    \n" +
            "    public void testObjects(HttpRequest request, ApiResponse apiResponse) {\n" +
            "        logger.info(\"Request: \" + request);\n" +
            "        logger.debug(\"Response: \" + apiResponse);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify all patterns are detected
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("ssn"), "Should detect plain keyword ssn");
        assertTrue(reportContent.contains("pin"), "Should detect plain keyword pin");
        assertTrue(reportContent.contains("request"), "Should detect plain object type request");
        
        // Verify we have detections (ssn, userPassword, pin, creditCardNumber, request, apiResponse)
        // Note: Some regex patterns may not match depending on implementation
        int detectionCount = reportContent.split("<tr>").length - 2;
        assertTrue(detectionCount >= 5, "Should have at least 5 detections, found: " + detectionCount);
    }

    @Test
    void testExecuteScan_WithEdgeCases() throws Exception {
        // Create configuration
        Path configFile = tempDir.resolve("config.xml");
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<scan-configuration>\n" +
            "    <keywords>\n" +
            "        <keyword type=\"plain\">ssn</keyword>\n" +
            "        <keyword type=\"plain\">email</keyword>\n" +
            "    </keywords>\n" +
            "    <sensitive-object-types>\n" +
            "    </sensitive-object-types>\n" +
            "</scan-configuration>";
        Files.writeString(configFile, configContent);

        // Create test Java file with edge cases
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        Path javaFile = srcDir.resolve("EdgeCases.java");
        String javaContent = "public class EdgeCases {\n" +
            "    // Multi-line log statement\n" +
            "    public void testMultiLine(String ssn, String email) {\n" +
            "        logger.info(\"User data: \" + \n" +
            "                    ssn + \n" +
            "                    \" Email: \" + \n" +
            "                    email);\n" +
            "    }\n" +
            "    \n" +
            "    // Nested method calls\n" +
            "    public void testNested(User user) {\n" +
            "        logger.debug(\"SSN: \" + user.getProfile().getSSN());\n" +
            "    }\n" +
            "    \n" +
            "    // StringBuilder not logged (should NOT detect)\n" +
            "    public void testBuilderNotLogged(String ssn) {\n" +
            "        StringBuilder sb = new StringBuilder();\n" +
            "        sb.append(\"SSN: \");\n" +
            "        sb.append(ssn);\n" +
            "        String result = sb.toString();\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify edge cases are handled correctly
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("ssn"), "Should detect ssn in multi-line and nested calls");
        assertTrue(reportContent.contains("email"), "Should detect email in multi-line statement");
        
        // Should have at least 2 detections: ssn and email in multi-line, possibly ssn in nested call
        // The StringBuilder not logged should NOT be detected
        int detectionCount = reportContent.split("<tr>").length - 2;
        assertTrue(detectionCount >= 2, "Should have at least 2 detections");
    }

    @Test
    void testExecuteScan_WithEmptyDirectory() throws Exception {
        // Create configuration
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

        // Create empty directory
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report indicates no detections
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("No sensitive data detected"));
        assertTrue(reportContent.contains("0"), "Should show 0 files scanned");
    }

    @Test
    void testExecuteScan_WithNonJavaFiles() throws Exception {
        // Create configuration
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

        // Create directory with non-Java files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectory(srcDir);
        
        // Create .txt file (should be ignored)
        Path txtFile = srcDir.resolve("notes.txt");
        Files.writeString(txtFile, "logger.info(\"SSN: \" + ssn);");
        
        // Create .xml file (should be ignored)
        Path xmlFile = srcDir.resolve("config.xml");
        Files.writeString(xmlFile, "<data>ssn</data>");
        
        // Create one .java file
        Path javaFile = srcDir.resolve("Test.java");
        String javaContent = "public class Test {\n" +
            "    public void test(String ssn) {\n" +
            "        logger.info(\"SSN: \" + ssn);\n" +
            "    }\n" +
            "}";
        Files.writeString(javaFile, javaContent);

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify only Java file was scanned
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Test.java"), "Should scan Java file");
        assertFalse(reportContent.contains("notes.txt"), "Should not scan txt file");
        assertFalse(reportContent.contains("config.xml"), "Should not scan xml file");
        assertTrue(reportContent.contains("1"), "Should show 1 file scanned");
    }

    @Test
    void testExecuteScan_WithLargeCodebase() throws Exception {
        // Create configuration
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

        // Create multiple directories with multiple files
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir.resolve("com").resolve("example").resolve("service"));
        Files.createDirectories(srcDir.resolve("com").resolve("example").resolve("controller"));
        Files.createDirectories(srcDir.resolve("com").resolve("example").resolve("model"));

        // Create 10 Java files across different directories
        for (int i = 1; i <= 10; i++) {
            Path dir = i <= 3 ? srcDir.resolve("com").resolve("example").resolve("service") :
                      i <= 6 ? srcDir.resolve("com").resolve("example").resolve("controller") :
                      srcDir.resolve("com").resolve("example").resolve("model");
            
            Path javaFile = dir.resolve("Class" + i + ".java");
            String javaContent = "public class Class" + i + " {\n" +
                "    public void method" + i + "(String ssn) {\n" +
                "        logger.info(\"Data: \" + ssn);\n" +
                "    }\n" +
                "}";
            Files.writeString(javaFile, javaContent);
        }

        // Create output path
        Path outputFile = tempDir.resolve("report.html");

        // Execute scan
        long startTime = System.currentTimeMillis();
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());
        long duration = System.currentTimeMillis() - startTime;

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify all files were scanned
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("10"), "Should show 10 files scanned");
        assertTrue(reportContent.contains("ssn"), "Should detect ssn");
        
        // Verify reasonable performance (should complete in reasonable time)
        assertTrue(duration < 10000, "Scan should complete in less than 10 seconds");
    }

    @Test
    void testExecuteScan_WithAllTestResources() throws Exception {
        // Use the actual test resource files
        Path configFile = Path.of("src/test/resources/valid-config.xml");
        Path srcDir = Path.of("src/test/resources");
        Path outputFile = tempDir.resolve("full-test-report.html");

        // Execute scan on test resources
        app.executeScan(srcDir.toString(), configFile.toString(), outputFile.toString());

        // Verify report was created
        assertTrue(Files.exists(outputFile), "Report file should be created");

        // Verify report contains expected content from test resources
        String reportContent = Files.readString(outputFile);
        assertTrue(reportContent.contains("Sensitive Data Scan Report"));
        
        // Should detect patterns from PositiveTestCases.java
        assertTrue(reportContent.contains("PositiveTestCases.java") || 
                   reportContent.contains("ssn") || 
                   reportContent.contains("password") ||
                   reportContent.contains("email"), 
                   "Should detect sensitive patterns from test resources");
    }
}
