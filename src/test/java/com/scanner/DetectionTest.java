package com.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetectionTest {

    @Test
    void testDetectionCreation() {
        Detection detection = new Detection(
            "UserService.java",
            42,
            "ssn",
            "logger.info(\"User SSN: \" + ssn)"
        );

        assertEquals("UserService.java", detection.getFileName());
        assertEquals(42, detection.getLineNumber());
        assertEquals("ssn", detection.getMatchedKeyword());
        assertEquals("logger.info(\"User SSN: \" + ssn)", detection.getLogStatement());
    }

    @Test
    void testDetectionCreation_WithMethodCall() {
        Detection detection = new Detection(
            "CardProcessor.java",
            100,
            "getNumber",
            "log.debug(\"Card: \" + card.getNumber())"
        );

        assertEquals("CardProcessor.java", detection.getFileName());
        assertEquals(100, detection.getLineNumber());
        assertEquals("getNumber", detection.getMatchedKeyword());
        assertEquals("log.debug(\"Card: \" + card.getNumber())", detection.getLogStatement());
    }

    @Test
    void testDetectionCreation_WithRegexMatch() {
        Detection detection = new Detection(
            "AuthService.java",
            25,
            "(?i).*password.*",
            "LOGGER.error(\"Failed login with password: \" + userPassword)"
        );

        assertEquals("AuthService.java", detection.getFileName());
        assertEquals(25, detection.getLineNumber());
        assertEquals("(?i).*password.*", detection.getMatchedKeyword());
        assertEquals("LOGGER.error(\"Failed login with password: \" + userPassword)", detection.getLogStatement());
    }

    @Test
    void testGetters() {
        Detection detection = new Detection("Test.java", 1, "keyword", "statement");

        assertNotNull(detection.getFileName());
        assertNotNull(detection.getMatchedKeyword());
        assertNotNull(detection.getLogStatement());
        assertTrue(detection.getLineNumber() > 0);
    }

    @Test
    void testDetectionCreation_WithMultiLineStatement() {
        String multiLineStatement = "logger.info(\"User: \" + username +\n" +
                                   "            \" SSN: \" + ssn)";
        Detection detection = new Detection(
            "UserService.java",
            50,
            "ssn",
            multiLineStatement
        );

        assertEquals("UserService.java", detection.getFileName());
        assertEquals(50, detection.getLineNumber());
        assertEquals("ssn", detection.getMatchedKeyword());
        assertEquals(multiLineStatement, detection.getLogStatement());
    }

    @Test
    void testDetectionCreation_WithSensitiveObjectType() {
        Detection detection = new Detection(
            "ApiController.java",
            75,
            "request",
            "logger.info(\"Request: \" + request)"
        );

        assertEquals("ApiController.java", detection.getFileName());
        assertEquals(75, detection.getLineNumber());
        assertEquals("request", detection.getMatchedKeyword());
        assertEquals("logger.info(\"Request: \" + request)", detection.getLogStatement());
    }
}
