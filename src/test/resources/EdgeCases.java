package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test file containing edge cases for the scanner
 */
public class EdgeCases {
    private static final Logger logger = LoggerFactory.getLogger(EdgeCases.class);
    private static final Logger log = LoggerFactory.getLogger(EdgeCases.class);
    
    // Edge Case 1: Multi-line log statement
    public void testMultiLineLogStatement(String ssn, String email) {
        logger.info("User data: " + 
                    ssn + 
                    " Email: " + 
                    email);
    }
    
    // Edge Case 2: Nested method calls
    public void testNestedMethodCalls(User user) {
        logger.debug("Data: " + user.getProfile().getSSN());
        log.info("Card: " + user.getWallet().getCard().getNumber());
    }
    
    // Edge Case 3: Complex expressions
    public void testComplexExpressions(String ssn, String mask) {
        logger.info("Masked SSN: " + (ssn != null ? ssn.substring(0, 3) + mask : "N/A"));
    }
    
    // Edge Case 4: StringBuilder used but not logged
    public void testStringBuilderNotLogged(String ssn) {
        StringBuilder sb = new StringBuilder();
        sb.append("SSN: ");
        sb.append(ssn);
        String result = sb.toString();
        // Not logged, so should not be detected
    }
    
    // Edge Case 5: Multiple builders in same method
    public void testMultipleBuilders(String ssn, String name) {
        StringBuilder sb1 = new StringBuilder();
        sb1.append("SSN: ");
        sb1.append(ssn);
        
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Name: ");
        sb2.append(name);
        
        logger.info(sb1.toString()); // Should detect ssn
        logger.debug(sb2.toString()); // Should not detect (name is not sensitive)
    }
    
    // Edge Case 6: StringJoiner
    public void testStringJoiner(String email, String phone) {
        java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
        joiner.add(email);
        joiner.add(phone);
        logger.info("Contacts: " + joiner.toString());
    }
    
    // Edge Case 7: Mixed string literals and variables
    public void testMixedContent(String ssn) {
        logger.info("SSN format is XXX-XX-XXXX, user SSN: " + ssn);
    }
    
    // Edge Case 8: Ternary operator with sensitive data
    public void testTernaryOperator(String password, boolean isValid) {
        logger.warn("Password: " + (isValid ? password : "invalid"));
    }
    
    // Edge Case 9: Array/Collection with sensitive data
    public void testArrayLogging(String[] emails, java.util.List<String> phones) {
        logger.info("Emails: " + java.util.Arrays.toString(emails));
        logger.debug("Phones: " + phones.toString());
    }
    
    // Edge Case 10: Request/Response with chained method calls
    public void testChainedMethodCalls(HttpResponse response) {
        logger.info("Status: " + response.getBody().getData().toString());
    }
    
    // Edge Case 11: Different logger variable names
    public void testDifferentLoggerNames() {
        Logger LOG = LoggerFactory.getLogger(EdgeCases.class);
        Logger LOGGER = LoggerFactory.getLogger(EdgeCases.class);
        
        String ssn = "123-45-6789";
        LOG.info("SSN: " + ssn);
        LOGGER.debug("SSN: " + ssn);
    }
    
    // Edge Case 12: Lambda expressions (should be ignored or handled carefully)
    public void testLambdaExpressions(java.util.List<String> ssns) {
        ssns.forEach(ssn -> logger.info("SSN: " + ssn));
    }
    
    // Edge Case 13: Format strings
    public void testFormatStrings(String ssn, String email) {
        logger.info(String.format("User SSN: %s, Email: %s", ssn, email));
    }
    
    // Edge Case 14: Response body with various method names
    public void testResponseBodyMethods(ResponseBody responseBody, ApiResponse apiResponse) {
        logger.info("Body: " + responseBody.getContent());
        log.debug("Response: " + apiResponse.getData());
        logger.warn("Full response: " + apiResponse);
    }
}
