package com.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationLoader class.
 */
class ConfigurationLoaderTest {

    private final ConfigurationLoader loader = new ConfigurationLoader();

    @Test
    void testLoadValidConfiguration() throws ConfigurationException {
        String configPath = getResourcePath("valid-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        assertNotNull(config);
        
        List<KeywordPattern> patterns = config.getPatterns();
        assertEquals(8, patterns.size());
        
        // Verify plain text keywords
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("ssn") && !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("socialSecurity") && !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("debitCard") && !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("pin") && !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("dateOfBirth") && !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("dob") && !p.isRegex()));
        
        // Verify regex keywords
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals(".*password.*") && p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("credit[Cc]ard.*") && p.isRegex()));
        
        // Verify sensitive object types
        List<KeywordPattern> objectTypes = config.getSensitiveObjectTypes();
        assertEquals(8, objectTypes.size());
        
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("request") && !p.isRegex()));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("response") && !p.isRegex()));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("httpResponse") && !p.isRegex()));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals(".*Request") && p.isRegex()));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals(".*Response") && p.isRegex()));
    }

    @Test
    void testLoadConfiguration_FileNotFound() {
        String nonExistentPath = "non-existent-config.xml";
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(nonExistentPath);
        });
        
        assertTrue(exception.getMessage().contains("Configuration file not found"));
        assertTrue(exception.getMessage().contains(nonExistentPath));
    }

    @Test
    void testLoadConfiguration_NullPath() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testLoadConfiguration_EmptyPath() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration("");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testLoadConfiguration_MalformedXML() {
        String configPath = getResourcePath("malformed-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Invalid XML structure"));
    }

    @Test
    void testLoadConfiguration_MissingTypeAttribute() {
        String configPath = getResourcePath("missing-type-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Missing 'type' attribute"));
    }

    @Test
    void testLoadConfiguration_EmptyKeyword() {
        String configPath = getResourcePath("empty-keyword-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Empty keyword found"));
    }

    @Test
    void testLoadConfiguration_InvalidRegex() {
        String configPath = getResourcePath("invalid-regex-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Invalid regex pattern"));
    }

    @Test
    void testLoadConfiguration_EmptyConfigurationElements() throws ConfigurationException, IOException {
        // Test with a valid XML that has empty keywords and object-types sections
        Path tempFile = Files.createTempFile("empty-sections-", ".xml");
        try {
            String emptyConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                               "<scan-configuration>\n" +
                               "    <keywords>\n" +
                               "    </keywords>\n" +
                               "    <sensitive-object-types>\n" +
                               "    </sensitive-object-types>\n" +
                               "</scan-configuration>";
            Files.writeString(tempFile, emptyConfig);
            
            ScanConfiguration config = loader.loadConfiguration(tempFile.toString());
            
            assertNotNull(config);
            assertEquals(0, config.getPatterns().size());
            assertEquals(0, config.getSensitiveObjectTypes().size());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseKeywords_PlainTextOnly() throws ConfigurationException {
        String configPath = getResourcePath("plain-keywords-only-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> patterns = config.getPatterns();
        assertEquals(3, patterns.size());
        
        // All should be plain text
        assertTrue(patterns.stream().allMatch(p -> !p.isRegex()));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("ssn")));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("password")));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("creditCard")));
        
        // Verify compiled patterns are null for plain text
        assertTrue(patterns.stream().allMatch(p -> p.getCompiledPattern() == null));
    }

    @Test
    void testParseKeywords_RegexOnly() throws ConfigurationException {
        String configPath = getResourcePath("regex-keywords-only-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> patterns = config.getPatterns();
        assertEquals(3, patterns.size());
        
        // All should be regex
        assertTrue(patterns.stream().allMatch(KeywordPattern::isRegex));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals(".*password.*")));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("ssn\\d+")));
        assertTrue(patterns.stream().anyMatch(p -> p.getKeyword().equals("credit[Cc]ard.*")));
        
        // Verify compiled patterns are not null for regex
        assertTrue(patterns.stream().allMatch(p -> p.getCompiledPattern() != null));
    }

    @Test
    void testParseKeywords_MixedPlainAndRegex() throws ConfigurationException {
        String configPath = getResourcePath("mixed-keywords-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> patterns = config.getPatterns();
        assertEquals(4, patterns.size());
        
        // Verify plain text keywords
        KeywordPattern ssnPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals("ssn"))
            .findFirst()
            .orElseThrow();
        assertFalse(ssnPattern.isRegex());
        assertNull(ssnPattern.getCompiledPattern());
        
        KeywordPattern pinPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals("pin"))
            .findFirst()
            .orElseThrow();
        assertFalse(pinPattern.isRegex());
        assertNull(pinPattern.getCompiledPattern());
        
        // Verify regex keywords
        KeywordPattern passwordPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals(".*password.*"))
            .findFirst()
            .orElseThrow();
        assertTrue(passwordPattern.isRegex());
        assertNotNull(passwordPattern.getCompiledPattern());
        
        KeywordPattern creditCardPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals("credit[Cc]ard.*"))
            .findFirst()
            .orElseThrow();
        assertTrue(creditCardPattern.isRegex());
        assertNotNull(creditCardPattern.getCompiledPattern());
    }

    @Test
    void testParseKeywords_RegexCompilation() throws ConfigurationException {
        String configPath = getResourcePath("regex-keywords-only-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> patterns = config.getPatterns();
        
        // Test that regex patterns actually work
        KeywordPattern passwordPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals(".*password.*"))
            .findFirst()
            .orElseThrow();
        
        // Regex is case-sensitive by default
        assertTrue(passwordPattern.matches("mypassword"));
        assertTrue(passwordPattern.matches("password123"));
        assertTrue(passwordPattern.matches("userpasswordField"));
        assertFalse(passwordPattern.matches("myPassword")); // Capital P won't match
        assertFalse(passwordPattern.matches("pin"));
        
        // Test credit card pattern
        KeywordPattern creditCardPattern = patterns.stream()
            .filter(p -> p.getKeyword().equals("credit[Cc]ard.*"))
            .findFirst()
            .orElseThrow();
        
        assertTrue(creditCardPattern.matches("creditCard"));
        assertTrue(creditCardPattern.matches("creditcard123"));
        assertTrue(creditCardPattern.matches("creditCardNumber"));
        assertFalse(creditCardPattern.matches("debitCard"));
    }

    @Test
    void testParseSensitiveObjectTypes_PlainTextOnly() throws ConfigurationException {
        String configPath = getResourcePath("plain-object-types-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> objectTypes = config.getSensitiveObjectTypes();
        assertEquals(3, objectTypes.size());
        
        // All should be plain text
        assertTrue(objectTypes.stream().allMatch(p -> !p.isRegex()));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("request")));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("response")));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals("httpResponse")));
        
        // Verify compiled patterns are null for plain text
        assertTrue(objectTypes.stream().allMatch(p -> p.getCompiledPattern() == null));
    }

    @Test
    void testParseSensitiveObjectTypes_RegexOnly() throws ConfigurationException {
        String configPath = getResourcePath("regex-object-types-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> objectTypes = config.getSensitiveObjectTypes();
        assertEquals(3, objectTypes.size());
        
        // All should be regex
        assertTrue(objectTypes.stream().allMatch(KeywordPattern::isRegex));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals(".*Request")));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals(".*Response")));
        assertTrue(objectTypes.stream().anyMatch(p -> p.getKeyword().equals(".*Body")));
        
        // Verify compiled patterns are not null for regex
        assertTrue(objectTypes.stream().allMatch(p -> p.getCompiledPattern() != null));
    }

    @Test
    void testParseSensitiveObjectTypes_MixedPlainAndRegex() throws ConfigurationException {
        String configPath = getResourcePath("mixed-object-types-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> objectTypes = config.getSensitiveObjectTypes();
        assertEquals(4, objectTypes.size());
        
        // Verify plain text object types
        KeywordPattern requestPattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals("request"))
            .findFirst()
            .orElseThrow();
        assertFalse(requestPattern.isRegex());
        assertNull(requestPattern.getCompiledPattern());
        
        KeywordPattern httpRequestPattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals("httpRequest"))
            .findFirst()
            .orElseThrow();
        assertFalse(httpRequestPattern.isRegex());
        assertNull(httpRequestPattern.getCompiledPattern());
        
        // Verify regex object types
        KeywordPattern responsePattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals(".*Response"))
            .findFirst()
            .orElseThrow();
        assertTrue(responsePattern.isRegex());
        assertNotNull(responsePattern.getCompiledPattern());
        
        KeywordPattern bodyPattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals(".*Body"))
            .findFirst()
            .orElseThrow();
        assertTrue(bodyPattern.isRegex());
        assertNotNull(bodyPattern.getCompiledPattern());
    }

    @Test
    void testParseSensitiveObjectTypes_RegexMatching() throws ConfigurationException {
        String configPath = getResourcePath("regex-object-types-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);

        List<KeywordPattern> objectTypes = config.getSensitiveObjectTypes();
        
        // Test .*Request pattern
        KeywordPattern requestPattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals(".*Request"))
            .findFirst()
            .orElseThrow();
        
        assertTrue(requestPattern.matches("httpRequest"));
        assertTrue(requestPattern.matches("apiRequest"));
        assertTrue(requestPattern.matches("myCustomRequest"));
        assertFalse(requestPattern.matches("response"));
        
        // Test .*Body pattern
        KeywordPattern bodyPattern = objectTypes.stream()
            .filter(p -> p.getKeyword().equals(".*Body"))
            .findFirst()
            .orElseThrow();
        
        assertTrue(bodyPattern.matches("requestBody"));
        assertTrue(bodyPattern.matches("responseBody"));
        assertTrue(bodyPattern.matches("httpBody"));
        assertFalse(bodyPattern.matches("request"));
    }

    @Test
    void testParseSensitiveObjectTypes_EmptyObjectType() {
        String configPath = getResourcePath("empty-object-type-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Empty object-type found"));
    }

    @Test
    void testParseSensitiveObjectTypes_MissingTypeAttribute() {
        String configPath = getResourcePath("missing-type-object-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Missing 'type' attribute for object-type"));
    }

    @Test
    void testXmlValidation_MissingSections() throws ConfigurationException {
        // Test with missing object-types section - should still work
        String configPath1 = getResourcePath("missing-object-types-section-config.xml");
        ScanConfiguration config1 = loader.loadConfiguration(configPath1);
        assertNotNull(config1);
        assertEquals(1, config1.getPatterns().size());
        assertEquals(0, config1.getSensitiveObjectTypes().size());
        
        // Test with missing keywords section - should still work
        String configPath2 = getResourcePath("missing-keywords-section-config.xml");
        ScanConfiguration config2 = loader.loadConfiguration(configPath2);
        assertNotNull(config2);
        assertEquals(0, config2.getPatterns().size());
        assertEquals(1, config2.getSensitiveObjectTypes().size());
    }

    @Test
    void testXmlValidation_InvalidRootElement() throws ConfigurationException {
        // Even with different root element, parsing should work as we look for specific tags
        String configPath = getResourcePath("invalid-root-element-config.xml");
        ScanConfiguration config = loader.loadConfiguration(configPath);
        assertNotNull(config);
        // Should still find the keyword element
        assertEquals(1, config.getPatterns().size());
    }

    @Test
    void testErrorHandling_SAXException() {
        // Already tested with malformed-config.xml
        String configPath = getResourcePath("malformed-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        assertTrue(exception.getMessage().contains("Invalid XML structure"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testErrorHandling_ClearErrorMessages() {
        // Test that error messages are clear and helpful
        
        // File not found
        ConfigurationException notFoundEx = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration("nonexistent.xml");
        });
        assertTrue(notFoundEx.getMessage().contains("not found"));
        assertTrue(notFoundEx.getMessage().contains("nonexistent.xml"));
        
        // Empty keyword
        String emptyKeywordPath = getResourcePath("empty-keyword-config.xml");
        ConfigurationException emptyKeywordEx = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(emptyKeywordPath);
        });
        assertTrue(emptyKeywordEx.getMessage().contains("Empty keyword"));
        
        // Invalid regex
        String invalidRegexPath = getResourcePath("invalid-regex-config.xml");
        ConfigurationException invalidRegexEx = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(invalidRegexPath);
        });
        assertTrue(invalidRegexEx.getMessage().contains("Invalid regex pattern"));
        assertTrue(invalidRegexEx.getMessage().contains("[invalid(regex"));
    }

    @Test
    void testErrorHandling_MalformedXMLWithDetails() {
        String configPath = getResourcePath("malformed-config.xml");
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            loader.loadConfiguration(configPath);
        });
        
        // Verify exception has cause
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getClass().getName().contains("SAX"));
    }

    /**
     * Helper method to get the absolute path of a test resource file.
     */
    private String getResourcePath(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        return file.getAbsolutePath();
    }
}
