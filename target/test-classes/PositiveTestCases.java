package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test file containing positive cases - these SHOULD be detected by the scanner
 */
public class PositiveTestCases {
    private static final Logger logger = LoggerFactory.getLogger(PositiveTestCases.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(PositiveTestCases.class);
    
    // Test Case 1: Simple variable logging
    public void testSimpleVariableLogging(String ssn, String email) {
        logger.info("User SSN: " + ssn);
        logger.debug("Email address: " + email);
    }
    
    // Test Case 2: Method call logging
    public void testMethodCallLogging(User user, Card card) {
        logger.info("SSN: " + user.getSSN());
        logger.error("Card number: " + card.getNumber());
        log.warn("PIN: " + card.getPIN());
    }
    
    // Test Case 3: StringBuilder single-line
    public void testStringBuilderSingleLine(String password) {
        logger.info(new StringBuilder().append("Password: ").append(password).toString());
    }
    
    // Test Case 4: StringBuilder multi-line
    public void testStringBuilderMultiLine(String username, String ssn, String dob) {
        StringBuilder sb = new StringBuilder();
        sb.append("User: ");
        sb.append(username);
        sb.append(" SSN: ");
        sb.append(ssn);
        sb.append(" DOB: ");
        sb.append(dob);
        logger.info(sb.toString());
    }
    
    // Test Case 5: StringBuffer multi-line with method calls
    public void testStringBufferMultiLine(Card card, String pin) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Card: ").append(card.getNumber());
        buffer.append(" PIN: ").append(pin);
        log.debug(buffer.toString());
    }
    
    // Test Case 6: String.join
    public void testStringJoin(String email, String phone, String address) {
        logger.info("Contact: " + String.join(", ", email, phone, address));
    }
    
    // Test Case 7: Request/Response logging
    public void testRequestResponseLogging(HttpRequest request, HttpResponse response) {
        logger.info("Request: " + request);
        logger.debug("Response body: " + response.getBody());
        log.info("API Response: " + httpResponse.body());
    }
    
    // Test Case 8: Request body logging
    public void testRequestBodyLogging(RequestBody requestBody, ResponseBody responseBody) {
        logger.error("Full request: " + requestBody.toString());
        LOGGER.warn("Response data: " + responseBody);
    }
    
    // Test Case 9: Concatenation with sensitive data
    public void testConcatenation(String ssn, String creditCard) {
        logger.info("SSN: " + ssn + " Card: " + creditCard);
    }
    
    // Test Case 10: Regex pattern matching
    public void testRegexPatterns(String userPassword, String adminPassword) {
        logger.debug("User password: " + userPassword);
        LOGGER.info("Admin password: " + adminPassword);
    }
    
    // Test Case 11: Sensitive object with method calls
    public void testSensitiveObjectMethods(ApiResponse apiResponse, ServletRequest servletRequest) {
        logger.info("API result: " + apiResponse.toString());
        log.debug("Servlet request: " + servletRequest.getParameter("data"));
    }
    
    // Test Case 12: Date of birth
    public void testDateOfBirth(String dateOfBirth) {
        logger.warn("DOB: " + dateOfBirth);
    }
    
    // Test Case 13: Account number
    public void testAccountNumber(String accountNumber) {
        logger.info("Account: " + accountNumber);
    }
    
    // Test Case 14: Debit card
    public void testDebitCard(String debitCard) {
        log.error("Debit card: " + debitCard);
    }
}
