package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test file containing negative cases - these should NOT be detected by the scanner
 */
public class NegativeTestCases {
    private static final Logger logger = LoggerFactory.getLogger(NegativeTestCases.class);
    
    // Test Case 1: String literals only (no variables)
    public void testStringLiteralsOnly() {
        logger.info("SSN: 123-45-6789");
        logger.debug("Password: hardcoded123");
        logger.warn("Email: test@example.com");
    }
    
    // Test Case 2: Method signatures (not actual logging)
    public void logUserData(String ssn, String password, String email) {
        // Method signature should be ignored
    }
    
    public void processSSN(String ssn) {
        // Method signature should be ignored
    }
    
    // Test Case 3: Method parameters (not logging)
    public void handleRequest(HttpRequest request, String password) {
        // Parameters in method signature should be ignored
    }
    
    // Test Case 4: Non-logging statements with sensitive keywords
    public void testNonLoggingStatements(String ssn) {
        String userSSN = ssn;
        int ssnLength = ssn.length();
        validateSSN(ssn);
    }
    
    // Test Case 5: Comments with sensitive keywords
    public void testComments() {
        // This method processes SSN and password
        /* 
         * Handle credit card and PIN validation
         * Request and response objects are used here
         */
        logger.info("Processing complete");
    }
    
    // Test Case 6: Variable names that don't match keywords
    public void testNonSensitiveVariables(String username, String userId, int age) {
        logger.info("User: " + username);
        logger.debug("ID: " + userId);
        logger.info("Age: " + age);
    }
    
    // Test Case 7: String literals in concatenation
    public void testStringLiteralConcatenation() {
        logger.info("SSN format: " + "XXX-XX-XXXX");
        logger.debug("Password requirements: " + "8+ characters");
    }
    
    // Test Case 8: StringBuilder with non-sensitive data
    public void testStringBuilderNonSensitive(String name, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ");
        sb.append(name);
        sb.append(" Count: ");
        sb.append(count);
        logger.info(sb.toString());
    }
    
    // Test Case 9: Non-sensitive object logging
    public void testNonSensitiveObjects(User user, Config config) {
        logger.info("User: " + user.getName());
        logger.debug("Config: " + config.getSettings());
    }
    
    // Test Case 10: Empty or null logging
    public void testEmptyLogging() {
        logger.info("");
        logger.debug("Processing...");
        logger.warn("Operation completed");
    }
    
    private void validateSSN(String ssn) {
        // Helper method - not logging
    }
}
