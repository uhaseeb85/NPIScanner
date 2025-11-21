package com.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SensitiveDataDetector class.
 */
class SensitiveDataDetectorTest {
    
    private SensitiveDataDetector detector;
    
    @BeforeEach
    void setUp() {
        detector = new SensitiveDataDetector();
    }
    
    // Tests for task 7.1: String literal filtering
    
    @Test
    void testIsStringLiteral_WithValidStringLiteral() {
        assertTrue(detector.isStringLiteral("\"hello world\""));
        assertTrue(detector.isStringLiteral("\"123-45-6789\""));
        assertTrue(detector.isStringLiteral("\"\""));
    }
    
    @Test
    void testIsStringLiteral_WithNonStringLiteral() {
        assertFalse(detector.isStringLiteral("hello"));
        assertFalse(detector.isStringLiteral("ssn"));
        assertFalse(detector.isStringLiteral("123"));
    }
    
    @Test
    void testIsStringLiteral_WithPartialQuotes() {
        assertFalse(detector.isStringLiteral("\"hello"));
        assertFalse(detector.isStringLiteral("hello\""));
        assertFalse(detector.isStringLiteral("\""));
    }
    
    @Test
    void testIsStringLiteral_WithNullOrEmpty() {
        assertFalse(detector.isStringLiteral(null));
        assertFalse(detector.isStringLiteral(""));
    }
    
    @Test
    void testGetStringLiteralRanges_WithMultipleLiterals() {
        String statement = "logger.info(\"SSN: \" + ssn + \" Name: \" + name)";
        List<int[]> ranges = detector.getStringLiteralRanges(statement);
        
        assertEquals(2, ranges.size());
        
        // First literal: "SSN: "
        assertEquals(12, ranges.get(0)[0]);
        assertEquals(19, ranges.get(0)[1]);
        
        // Second literal: " Name: "
        assertEquals(28, ranges.get(1)[0]);
        assertEquals(37, ranges.get(1)[1]);
    }
    
    @Test
    void testGetStringLiteralRanges_WithNoLiterals() {
        String statement = "logger.info(ssn)";
        List<int[]> ranges = detector.getStringLiteralRanges(statement);
        
        assertEquals(0, ranges.size());
    }
    
    @Test
    void testGetStringLiteralRanges_WithEmptyLiteral() {
        String statement = "logger.info(\"\")";
        List<int[]> ranges = detector.getStringLiteralRanges(statement);
        
        assertEquals(1, ranges.size());
        assertEquals(12, ranges.get(0)[0]);
        assertEquals(14, ranges.get(0)[1]);
    }
    
    @Test
    void testIsWithinStringLiteral_PositionInsideLiteral() {
        List<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{12, 19}); // "SSN: "
        ranges.add(new int[]{27, 36}); // " Name: "
        
        assertTrue(detector.isWithinStringLiteral(15, ranges));
        assertTrue(detector.isWithinStringLiteral(30, ranges));
    }
    
    @Test
    void testIsWithinStringLiteral_PositionOutsideLiteral() {
        List<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{12, 19}); // "SSN: "
        ranges.add(new int[]{27, 36}); // " Name: "
        
        assertFalse(detector.isWithinStringLiteral(5, ranges));
        assertFalse(detector.isWithinStringLiteral(22, ranges));
        assertFalse(detector.isWithinStringLiteral(40, ranges));
    }
    
    @Test
    void testIsWithinStringLiteral_AtBoundary() {
        List<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{12, 19});
        
        assertTrue(detector.isWithinStringLiteral(12, ranges)); // Start position
        assertFalse(detector.isWithinStringLiteral(19, ranges)); // End position (exclusive)
    }
    
    @Test
    void testDetectSensitiveData_WithNullInputs() {
        List<Detection> detections = detector.detectSensitiveData(null, null);
        assertNotNull(detections);
        assertEquals(0, detections.size());
    }
    
    // Tests for task 7.2: Variable name extraction and matching
    
    @Test
    void testExtractVariables_WithSimpleVariables() {
        String statement = "logger.info(ssn + name + email)";
        List<String> variables = detector.extractVariables(statement);
        
        assertTrue(variables.contains("logger"));
        assertTrue(variables.contains("info"));
        assertTrue(variables.contains("ssn"));
        assertTrue(variables.contains("name"));
        assertTrue(variables.contains("email"));
    }
    
    @Test
    void testExtractVariables_ExcludesStringLiterals() {
        String statement = "logger.info(\"ssn: \" + actualSsn)";
        List<String> variables = detector.extractVariables(statement);
        
        assertTrue(variables.contains("actualSsn"));
        assertTrue(variables.contains("logger"));
        assertTrue(variables.contains("info"));
        
        // "ssn" inside the string literal should not be extracted as a variable
        // Count how many times "ssn" appears - should only be once (actualSsn contains it)
        long ssnCount = variables.stream().filter(v -> v.equals("ssn")).count();
        assertEquals(0, ssnCount);
    }
    
    @Test
    void testExtractVariables_WithUnderscores() {
        String statement = "logger.info(user_ssn + _privateData)";
        List<String> variables = detector.extractVariables(statement);
        
        assertTrue(variables.contains("user_ssn"));
        assertTrue(variables.contains("_privateData"));
    }
    
    @Test
    void testExtractVariables_WithEmptyStatement() {
        List<String> variables = detector.extractVariables("");
        assertEquals(0, variables.size());
    }
    
    @Test
    void testExtractVariables_WithNullStatement() {
        List<String> variables = detector.extractVariables(null);
        assertNotNull(variables);
        assertEquals(0, variables.size());
    }
    
    @Test
    void testMatchesKeyword_WithPlainTextPattern() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        
        assertTrue(detector.matchesKeyword("ssn", pattern));
        assertTrue(detector.matchesKeyword("SSN", pattern)); // Case insensitive
        assertTrue(detector.matchesKeyword("Ssn", pattern));
        assertFalse(detector.matchesKeyword("socialSecurity", pattern));
    }
    
    @Test
    void testMatchesKeyword_WithRegexPattern() {
        KeywordPattern pattern = new KeywordPattern(".*[Pp]assword.*", true);
        
        assertTrue(detector.matchesKeyword("password", pattern));
        assertTrue(detector.matchesKeyword("userPassword", pattern));
        assertTrue(detector.matchesKeyword("passwordHash", pattern));
        assertTrue(detector.matchesKeyword("Password", pattern));
        assertFalse(detector.matchesKeyword("ssn", pattern));
    }
    
    @Test
    void testMatchesKeyword_WithNullInputs() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        
        assertFalse(detector.matchesKeyword(null, pattern));
        assertFalse(detector.matchesKeyword("ssn", null));
        assertFalse(detector.matchesKeyword(null, null));
    }
    
    @Test
    void testDetectSensitiveData_WithVariableMatching() {
        LogStatement logStatement = new LogStatement("Test.java", 10, "logger.info(ssn + name)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("email", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("Test.java", detections.get(0).getFileName());
        assertEquals(10, detections.get(0).getLineNumber());
        assertEquals("ssn", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithRegexPatternMatching() {
        LogStatement logStatement = new LogStatement("Test.java", 15, "logger.info(userPassword)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern(".*[Pp]assword.*", true));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals(".*[Pp]assword.*", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_DetectsStringLiterals() {
        LogStatement logStatement = new LogStatement("Test.java", 20, "logger.info(\"ssn: \" + actualData)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect "ssn" inside the string literal
        assertEquals(1, detections.size());
        assertTrue(detections.get(0).getMatchedKeyword().contains("ssn"));
    }
    
    // Tests for task 7.3: Method call extraction and matching
    
    @Test
    void testExtractMethodCalls_WithSimpleMethodCalls() {
        String statement = "logger.info(getSSN())";
        List<String> methodCalls = detector.extractMethodCalls(statement);
        
        assertTrue(methodCalls.contains("info"));
        assertTrue(methodCalls.contains("getSSN"));
    }
    
    @Test
    void testExtractMethodCalls_WithChainedMethodCalls() {
        String statement = "logger.info(card.getNumber())";
        List<String> methodCalls = detector.extractMethodCalls(statement);
        
        assertTrue(methodCalls.contains("info"));
        assertTrue(methodCalls.contains("getNumber"));
    }
    
    @Test
    void testExtractMethodCalls_WithMultipleChainedCalls() {
        String statement = "logger.info(user.getCard().getNumber())";
        List<String> methodCalls = detector.extractMethodCalls(statement);
        
        assertTrue(methodCalls.contains("info"));
        assertTrue(methodCalls.contains("getCard"));
        assertTrue(methodCalls.contains("getNumber"));
    }
    
    @Test
    void testExtractMethodCalls_ExcludesStringLiterals() {
        String statement = "logger.info(\"getSSN() method\" + user.getDOB())";
        List<String> methodCalls = detector.extractMethodCalls(statement);
        
        assertTrue(methodCalls.contains("info"));
        assertTrue(methodCalls.contains("getDOB"));
        
        // "getSSN" inside the string literal should not be extracted
        // Count occurrences - getSSN should not appear
        long getSsnCount = methodCalls.stream().filter(m -> m.equals("getSSN")).count();
        assertEquals(0, getSsnCount);
    }
    
    @Test
    void testExtractMethodCalls_WithEmptyStatement() {
        List<String> methodCalls = detector.extractMethodCalls("");
        assertEquals(0, methodCalls.size());
    }
    
    @Test
    void testExtractMethodCalls_WithNullStatement() {
        List<String> methodCalls = detector.extractMethodCalls(null);
        assertNotNull(methodCalls);
        assertEquals(0, methodCalls.size());
    }
    
    @Test
    void testDetectSensitiveData_WithMethodCallMatching() {
        LogStatement logStatement = new LogStatement("Test.java", 25, "logger.info(user.getSSN())");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("getSSN", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("Test.java", detections.get(0).getFileName());
        assertEquals(25, detections.get(0).getLineNumber());
        assertEquals("getSSN", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithChainedMethodCalls() {
        LogStatement logStatement = new LogStatement("Test.java", 30, "log.error(card.getNumber())");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("getNumber", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("getNumber", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithRegexMethodCallMatching() {
        LogStatement logStatement = new LogStatement("Test.java", 35, "logger.info(user.getPassword())");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("get[Pp]assword", true));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("get[Pp]assword", detections.get(0).getMatchedKeyword());
    }
    
    // Tests for task 7.4: StringBuilder/StringBuffer pattern detection (single-line)
    
    @Test
    void testExtractStringBuilderAppends_WithSimpleAppend() {
        String statement = "StringBuilder sb = new StringBuilder().append(ssn)";
        List<String> identifiers = detector.extractStringBuilderAppends(statement);
        
        assertTrue(identifiers.contains("ssn"));
    }
    
    @Test
    void testExtractStringBuilderAppends_WithStringBufferAppend() {
        String statement = "StringBuffer buffer = new StringBuffer().append(password)";
        List<String> identifiers = detector.extractStringBuilderAppends(statement);
        
        assertTrue(identifiers.contains("password"));
    }
    
    @Test
    void testExtractStringBuilderAppends_WithMethodCallInAppend() {
        String statement = "StringBuilder sb = new StringBuilder(); sb.append(user.getSSN())";
        List<String> identifiers = detector.extractStringBuilderAppends(statement);
        
        assertTrue(identifiers.contains("user"));
        assertTrue(identifiers.contains("getSSN"));
    }
    
    @Test
    void testExtractStringBuilderAppends_WithMultipleAppends() {
        String statement = "new StringBuilder().append(ssn).append(name)";
        List<String> identifiers = detector.extractStringBuilderAppends(statement);
        
        assertTrue(identifiers.contains("ssn"));
        assertTrue(identifiers.contains("name"));
    }
    
    @Test
    void testExtractStringBuilderAppends_WithEmptyStatement() {
        List<String> identifiers = detector.extractStringBuilderAppends("");
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testExtractStringBuilderAppends_WithNullStatement() {
        List<String> identifiers = detector.extractStringBuilderAppends(null);
        assertNotNull(identifiers);
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testDetectSensitiveData_WithStringBuilderAppend() {
        LogStatement logStatement = new LogStatement("Test.java", 40, "logger.info(new StringBuilder().append(ssn))");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("ssn", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithStringBufferAppend() {
        LogStatement logStatement = new LogStatement("Test.java", 45, "log.debug(new StringBuffer().append(card.getNumber()))");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("getNumber", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("getNumber", detections.get(0).getMatchedKeyword());
    }
    
    // Tests for task 7.5: StringBuilder/StringBuffer pattern detection (multi-line)
    
    @Test
    void testDetectSensitiveData_WithMultiLineStringBuilder() {
        // Simulate a multi-line StringBuilder pattern
        // StringBuilder sb = new StringBuilder();
        // sb.append("User: ");
        // sb.append(username);
        // sb.append(" SSN: ");
        // sb.append(ssn);
        // logger.info(sb.toString());
        
        List<String> sbAppends = new ArrayList<>();
        sbAppends.add("\"User: \"");
        sbAppends.add("username");
        sbAppends.add("\" SSN: \"");
        sbAppends.add("ssn");
        
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        builderAppends.put("sb", sbAppends);
        
        LogStatement logStatement = new LogStatement("Test.java", 50, "logger.info(sb.toString())", builderAppends);
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("ssn", detections.get(0).getMatchedKeyword());
        assertEquals(50, detections.get(0).getLineNumber());
    }
    
    @Test
    void testDetectSensitiveData_WithMultiLineStringBufferAndMethodCalls() {
        // Simulate a multi-line StringBuffer pattern with method calls
        // StringBuffer buffer = new StringBuffer();
        // buffer.append("Card: ").append(card.getNumber());
        // buffer.append(" PIN: ").append(pin);
        // log.debug(buffer.toString());
        
        List<String> bufferAppends = new ArrayList<>();
        bufferAppends.add("\"Card: \"");
        bufferAppends.add("card.getNumber()");
        bufferAppends.add("\" PIN: \"");
        bufferAppends.add("pin");
        
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        builderAppends.put("buffer", bufferAppends);
        
        LogStatement logStatement = new LogStatement("Test.java", 55, "log.debug(buffer.toString())", builderAppends);
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("getNumber", false));
        patterns.add(new KeywordPattern("pin", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(2, detections.size());
        
        // Check that both patterns were detected
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("getNumber"));
        assertTrue(matchedKeywords.contains("pin"));
    }
    
    @Test
    void testDetectSensitiveData_WithMultiLineBuilderNoSensitiveData() {
        List<String> sbAppends = new ArrayList<>();
        sbAppends.add("\"User: \"");
        sbAppends.add("username");
        
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        builderAppends.put("sb", sbAppends);
        
        LogStatement logStatement = new LogStatement("Test.java", 60, "logger.info(sb.toString())", builderAppends);
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("password", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(0, detections.size());
    }
    
    @Test
    void testDetectSensitiveData_WithEmptyBuilderAppends() {
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        
        LogStatement logStatement = new LogStatement("Test.java", 65, "logger.info(sb.toString())", builderAppends);
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(0, detections.size());
    }
    
    // Tests for task 7.6: String.join and StringJoiner pattern detection
    
    @Test
    void testExtractStringJoinIdentifiers_WithSimpleJoin() {
        String statement = "logger.info(String.join(\", \", email, phone))";
        List<String> identifiers = detector.extractStringJoinIdentifiers(statement);
        
        assertTrue(identifiers.contains("email"));
        assertTrue(identifiers.contains("phone"));
    }
    
    @Test
    void testExtractStringJoinIdentifiers_WithMethodCalls() {
        String statement = "logger.info(String.join(\", \", user.getEmail(), user.getPhone()))";
        List<String> identifiers = detector.extractStringJoinIdentifiers(statement);
        
        assertTrue(identifiers.contains("user"));
        assertTrue(identifiers.contains("getEmail"));
        assertTrue(identifiers.contains("getPhone"));
    }
    
    @Test
    void testExtractStringJoinIdentifiers_WithEmptyStatement() {
        List<String> identifiers = detector.extractStringJoinIdentifiers("");
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testExtractStringJoinIdentifiers_WithNullStatement() {
        List<String> identifiers = detector.extractStringJoinIdentifiers(null);
        assertNotNull(identifiers);
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testExtractStringJoinerIdentifiers_WithSimpleAdd() {
        String statement = "StringJoiner joiner = new StringJoiner(\", \"); joiner.add(ssn)";
        List<String> identifiers = detector.extractStringJoinerIdentifiers(statement);
        
        assertTrue(identifiers.contains("ssn"));
    }
    
    @Test
    void testExtractStringJoinerIdentifiers_WithMethodCall() {
        String statement = "new StringJoiner(\", \").add(user.getPassword())";
        List<String> identifiers = detector.extractStringJoinerIdentifiers(statement);
        
        assertTrue(identifiers.contains("user"));
        assertTrue(identifiers.contains("getPassword"));
    }
    
    @Test
    void testExtractStringJoinerIdentifiers_WithEmptyStatement() {
        List<String> identifiers = detector.extractStringJoinerIdentifiers("");
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testExtractStringJoinerIdentifiers_WithNullStatement() {
        List<String> identifiers = detector.extractStringJoinerIdentifiers(null);
        assertNotNull(identifiers);
        assertEquals(0, identifiers.size());
    }
    
    @Test
    void testDetectSensitiveData_WithStringJoin() {
        LogStatement logStatement = new LogStatement("Test.java", 70, "logger.info(String.join(\", \", email, phone, ssn))");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("email", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(2, detections.size());
        
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("ssn"));
        assertTrue(matchedKeywords.contains("email"));
    }
    
    @Test
    void testDetectSensitiveData_WithStringJoiner() {
        LogStatement logStatement = new LogStatement("Test.java", 75, "log.debug(new StringJoiner(\", \").add(password).add(username))");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("password", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("password", detections.get(0).getMatchedKeyword());
    }
    
    // Tests for task 7.7: Concatenation pattern detection
    
    @Test
    void testDetectSensitiveData_WithSimpleConcatenation() {
        LogStatement logStatement = new LogStatement("Test.java", 80, "logger.info(\"SSN: \" + ssn)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect both: variable 'ssn' and keyword in string literal "SSN: "
        assertEquals(2, detections.size());
    }
    
    @Test
    void testDetectSensitiveData_WithMultipleConcatenations() {
        LogStatement logStatement = new LogStatement("Test.java", 85, "logger.info(\"User: \" + username + \" SSN: \" + ssn + \" Email: \" + email)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("email", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect 4: variables 'ssn' and 'email', plus keywords in string literals "SSN: " and "Email: "
        assertEquals(4, detections.size());
        
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("ssn"));
        assertTrue(matchedKeywords.contains("email"));
    }
    
    @Test
    void testDetectSensitiveData_WithConcatenationAndMethodCalls() {
        LogStatement logStatement = new LogStatement("Test.java", 90, "log.error(\"Card: \" + card.getNumber() + \" PIN: \" + pin)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("getNumber", false));
        patterns.add(new KeywordPattern("pin", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect 3: method 'getNumber', variable 'pin', and keyword 'PIN' in string literal
        assertEquals(3, detections.size());
        
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("getNumber"));
        assertTrue(matchedKeywords.contains("pin"));
    }
    
    @Test
    void testDetectSensitiveData_WithMixedStringLiteralsAndVariables() {
        LogStatement logStatement = new LogStatement("Test.java", 95, "logger.info(\"Data: \" + data + \" More: \" + moreData)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("data", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect: variable 'data' and keyword 'data' in string literal "Data: "
        assertEquals(2, detections.size());
    }
    
    @Test
    void testDetectSensitiveData_ConcatenationDoesNotMatchStringLiterals() {
        LogStatement logStatement = new LogStatement("Test.java", 100, "logger.info(\"ssn\" + actualData)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect "ssn" inside the string literal
        assertEquals(1, detections.size());
    }
    
    // Tests for task 7.8: Sensitive object type detection
    
    @Test
    void testExtractObjectReferences_WithSimpleObjects() {
        String statement = "logger.info(request)";
        List<String> objectRefs = detector.extractObjectReferences(statement);
        
        assertTrue(objectRefs.contains("request"));
        assertTrue(objectRefs.contains("logger"));
        assertTrue(objectRefs.contains("info"));
    }
    
    @Test
    void testExtractObjectReferences_WithMethodCallsOnObjects() {
        String statement = "logger.info(request.getBody())";
        List<String> objectRefs = detector.extractObjectReferences(statement);
        
        assertTrue(objectRefs.contains("request"));
        assertTrue(objectRefs.contains("getBody"));
    }
    
    @Test
    void testIsSensitiveObjectType_WithPlainMatch() {
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        objectTypes.add(new KeywordPattern("response", false));
        
        assertTrue(detector.isSensitiveObjectType("request", objectTypes));
        assertTrue(detector.isSensitiveObjectType("response", objectTypes));
        assertFalse(detector.isSensitiveObjectType("user", objectTypes));
    }
    
    @Test
    void testIsSensitiveObjectType_WithRegexMatch() {
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern(".*Request", true));
        objectTypes.add(new KeywordPattern(".*Response", true));
        
        assertTrue(detector.isSensitiveObjectType("httpRequest", objectTypes));
        assertTrue(detector.isSensitiveObjectType("apiResponse", objectTypes));
        assertTrue(detector.isSensitiveObjectType("Request", objectTypes));
        assertFalse(detector.isSensitiveObjectType("user", objectTypes));
    }
    
    @Test
    void testIsSensitiveObjectType_WithNullInputs() {
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        
        assertFalse(detector.isSensitiveObjectType(null, objectTypes));
        assertFalse(detector.isSensitiveObjectType("request", null));
    }
    
    @Test
    void testDetectSensitiveData_WithDirectObjectLogging() {
        LogStatement logStatement = new LogStatement("Test.java", 105, "logger.info(request)");
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        
        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("request", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithMethodCallOnSensitiveObject() {
        LogStatement logStatement = new LogStatement("Test.java", 110, "logger.info(request.getBody())");
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        
        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("request", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithMultipleSensitiveObjects() {
        LogStatement logStatement = new LogStatement("Test.java", 115, "log.debug(\"Request: \" + request + \" Response: \" + response)");
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        objectTypes.add(new KeywordPattern("response", false));
        
        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(2, detections.size());
        
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("request"));
        assertTrue(matchedKeywords.contains("response"));
    }
    
    @Test
    void testDetectSensitiveData_WithRegexSensitiveObjectType() {
        LogStatement logStatement = new LogStatement("Test.java", 120, "logger.error(httpResponse.body())");
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern(".*Response", true));
        
        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals(".*Response", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithToStringOnSensitiveObject() {
        LogStatement logStatement = new LogStatement("Test.java", 125, "logger.info(requestBody.toString())");
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("requestBody", false));
        
        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertEquals("requestBody", detections.get(0).getMatchedKeyword());
    }
    
    @Test
    void testDetectSensitiveData_WithBothKeywordsAndObjectTypes() {
        LogStatement logStatement = new LogStatement("Test.java", 130, "logger.info(\"SSN: \" + ssn + \" Request: \" + request)");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, objectTypes);
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect 3: variable 'ssn', object 'request', and keyword 'ssn' in string literal "SSN: "
        assertEquals(3, detections.size());
        
        List<String> matchedKeywords = new ArrayList<>();
        for (Detection d : detections) {
            matchedKeywords.add(d.getMatchedKeyword());
        }
        assertTrue(matchedKeywords.contains("ssn"));
        assertTrue(matchedKeywords.contains("request"));
    }

    // Tests for string literal keyword detection

    @Test
    void testExtractStringLiterals() {
        String statement = "logger.info(\"User SSN: \" + value);";
        
        List<String> literals = detector.extractStringLiterals(statement);
        
        assertEquals(1, literals.size());
        assertEquals("User SSN: ", literals.get(0));
    }

    @Test
    void testExtractStringLiterals_MultipleStrings() {
        String statement = "logger.info(\"SSN: \" + ssn + \" Password: \" + pwd);";
        
        List<String> literals = detector.extractStringLiterals(statement);
        
        assertEquals(2, literals.size());
        assertTrue(literals.contains("SSN: "));
        assertTrue(literals.contains(" Password: "));
    }

    @Test
    void testExtractStringLiterals_EmptyString() {
        String statement = "logger.info(\"\");";
        
        List<String> literals = detector.extractStringLiterals(statement);
        
        assertTrue(literals.isEmpty());
    }

    @Test
    void testContainsKeyword_PlainText() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        
        assertTrue(detector.containsKeyword("User SSN: ", pattern));
        assertTrue(detector.containsKeyword("ssn value", pattern));
        assertTrue(detector.containsKeyword("SSN", pattern)); // Case insensitive
        assertFalse(detector.containsKeyword("User data", pattern));
    }

    @Test
    void testContainsKeyword_Regex() {
        KeywordPattern pattern = new KeywordPattern(".*[Pp]assword.*", true);
        
        assertTrue(detector.containsKeyword("User Password: ", pattern));
        assertTrue(detector.containsKeyword("password field", pattern));
        assertTrue(detector.containsKeyword("Enter Password", pattern));
        assertFalse(detector.containsKeyword("User data", pattern));
    }

    @Test
    void testDetectSensitiveData_StringLiteralWithKeyword() {
        LogStatement logStatement = new LogStatement("Test.java", 10, 
            "System.out.println(\"User SSN: \" + outputPath);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertTrue(detections.get(0).getMatchedKeyword().contains("ssn"));
        assertTrue(detections.get(0).getMatchedKeyword().contains("string literal"));
    }

    @Test
    void testDetectSensitiveData_StringLiteralWithPassword() {
        LogStatement logStatement = new LogStatement("Test.java", 15, 
            "logger.info(\"Password is: \" + value);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("password", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertTrue(detections.get(0).getMatchedKeyword().contains("password"));
    }

    @Test
    void testDetectSensitiveData_StringLiteralWithRegexPattern() {
        LogStatement logStatement = new LogStatement("Test.java", 20, 
            "log.debug(\"creditCard data: \" + data);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("credit[Cc]ard.*", true));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertEquals(1, detections.size());
        assertTrue(detections.get(0).getMatchedKeyword().toLowerCase().contains("credit"));
    }

    @Test
    void testDetectSensitiveData_BothVariableAndStringLiteral() {
        LogStatement logStatement = new LogStatement("Test.java", 25, 
            "logger.info(\"SSN: \" + ssn);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect both: the variable 'ssn' and the keyword in the string literal
        assertEquals(2, detections.size());
    }

    @Test
    void testDetectSensitiveData_StringLiteralNoMatch() {
        LogStatement logStatement = new LogStatement("Test.java", 30, 
            "logger.info(\"User data: \" + value);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        assertTrue(detections.isEmpty());
    }

    @Test
    void testDetectSensitiveData_MultipleStringLiteralsWithKeywords() {
        LogStatement logStatement = new LogStatement("Test.java", 35, 
            "logger.info(\"SSN: \" + a + \" Password: \" + b);");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("password", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should detect both keywords in string literals
        assertEquals(2, detections.size());
    }

    @Test
    void testDetectSensitiveData_StringLiteralOnly_NotDetected() {
        LogStatement logStatement = new LogStatement("Test.java", 40, 
            "logger.info(\"SSN: \");");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should NOT detect - it's just a hardcoded string with no variables
        assertTrue(detections.isEmpty());
    }

    @Test
    void testDetectSensitiveData_MultipleStringLiteralsOnly_NotDetected() {
        LogStatement logStatement = new LogStatement("Test.java", 45, 
            "logger.info(\"SSN: \" + \"Password: \");");
        
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));
        patterns.add(new KeywordPattern("password", false));
        
        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        
        List<Detection> detections = detector.detectSensitiveData(logStatement, config);
        
        // Should NOT detect - only string literals, no variables
        assertEquals(0, detections.size(), "Should not detect when only string literals are concatenated");
    }
    
    @Test
    void testHasNonLiteralContent() {
        // Should return true for statements with variables
        assertTrue(detector.hasNonLiteralContent("logger.info(\"SSN: \" + ssn);"));
        assertTrue(detector.hasNonLiteralContent("System.out.println(\"User: \" + user);"));
        
        // Should return false for statements with only string literals
        assertFalse(detector.hasNonLiteralContent("logger.info(\"SSN: \");"));
        assertFalse(detector.hasNonLiteralContent("logger.info(\"SSN: \" + \"Password: \");"));
    }
}
