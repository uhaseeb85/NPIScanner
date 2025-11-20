package com.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class KeywordPatternTest {

    @Test
    void testPlainTextMatching_ExactMatch() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        assertTrue(pattern.matches("ssn"));
    }

    @Test
    void testPlainTextMatching_CaseInsensitive() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        assertTrue(pattern.matches("SSN"));
        assertTrue(pattern.matches("Ssn"));
        assertTrue(pattern.matches("sSn"));
    }

    @Test
    void testPlainTextMatching_NoMatch() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        assertFalse(pattern.matches("password"));
        assertFalse(pattern.matches("ssnNumber"));
        assertFalse(pattern.matches("mySSN"));
    }

    @Test
    void testPlainTextMatching_NullText() {
        KeywordPattern pattern = new KeywordPattern("ssn", false);
        assertFalse(pattern.matches(null));
    }

    @Test
    void testRegexMatching_SimplePattern() {
        KeywordPattern pattern = new KeywordPattern("(?i).*password.*", true);
        assertTrue(pattern.matches("password"));
        assertTrue(pattern.matches("mypassword"));
        assertTrue(pattern.matches("passwordField"));
        assertTrue(pattern.matches("myPasswordField"));
    }

    @Test
    void testRegexMatching_ComplexPattern() {
        KeywordPattern pattern = new KeywordPattern("credit[Cc]ard.*", true);
        assertTrue(pattern.matches("creditCard"));
        assertTrue(pattern.matches("creditcard"));
        assertTrue(pattern.matches("creditCardNumber"));
        assertTrue(pattern.matches("creditcardInfo"));
    }

    @Test
    void testRegexMatching_NoMatch() {
        KeywordPattern pattern = new KeywordPattern("(?i).*password.*", true);
        assertFalse(pattern.matches("ssn"));
        assertFalse(pattern.matches("pin"));
    }

    @Test
    void testRegexMatching_NullText() {
        KeywordPattern pattern = new KeywordPattern("(?i).*password.*", true);
        assertFalse(pattern.matches(null));
    }

    @ParameterizedTest
    @CsvSource({
        "ssn, ssn, true",
        "ssn, SSN, true",
        "ssn, password, false",
        "dateOfBirth, dateOfBirth, true",
        "dateOfBirth, DATEOFBIRTH, true"
    })
    void testPlainTextMatching_MultipleScenarios(String keyword, String text, boolean expected) {
        KeywordPattern pattern = new KeywordPattern(keyword, false);
        assertEquals(expected, pattern.matches(text));
    }

    @Test
    void testGetters() {
        KeywordPattern plainPattern = new KeywordPattern("ssn", false);
        assertEquals("ssn", plainPattern.getKeyword());
        assertFalse(plainPattern.isRegex());
        assertNull(plainPattern.getCompiledPattern());

        KeywordPattern regexPattern = new KeywordPattern("(?i).*password.*", true);
        assertEquals("(?i).*password.*", regexPattern.getKeyword());
        assertTrue(regexPattern.isRegex());
        assertNotNull(regexPattern.getCompiledPattern());
    }
}
