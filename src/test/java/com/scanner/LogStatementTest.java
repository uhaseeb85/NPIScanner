package com.scanner;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogStatementTest {

    @Test
    void testLogStatementCreation_BasicConstructor() {
        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(\"test\")");

        assertEquals("TestFile.java", logStatement.getFileName());
        assertEquals(42, logStatement.getLineNumber());
        assertEquals("logger.info(\"test\")", logStatement.getStatement());
        assertNotNull(logStatement.getRelatedBuilderAppends());
        assertTrue(logStatement.getRelatedBuilderAppends().isEmpty());
    }

    @Test
    void testLogStatementCreation_WithBuilderAppends() {
        Map<String, List<String>> builderAppends = new HashMap<>();
        builderAppends.put("sb", Arrays.asList("sb.append(ssn)", "sb.append(pin)"));

        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(sb.toString())", builderAppends);

        assertEquals("TestFile.java", logStatement.getFileName());
        assertEquals(42, logStatement.getLineNumber());
        assertEquals("logger.info(sb.toString())", logStatement.getStatement());
        assertNotNull(logStatement.getRelatedBuilderAppends());
        assertEquals(1, logStatement.getRelatedBuilderAppends().size());
        assertTrue(logStatement.getRelatedBuilderAppends().containsKey("sb"));
        assertEquals(2, logStatement.getRelatedBuilderAppends().get("sb").size());
    }

    @Test
    void testLogStatementCreation_WithNullBuilderAppends() {
        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(\"test\")", null);

        assertNotNull(logStatement.getRelatedBuilderAppends());
        assertTrue(logStatement.getRelatedBuilderAppends().isEmpty());
    }

    @Test
    void testGetters() {
        LogStatement logStatement = new LogStatement("MyClass.java", 100, "log.debug(message)");

        assertEquals("MyClass.java", logStatement.getFileName());
        assertEquals(100, logStatement.getLineNumber());
        assertEquals("log.debug(message)", logStatement.getStatement());
    }

    @Test
    void testRelatedBuilderAppends_IsUnmodifiable() {
        Map<String, List<String>> builderAppends = new HashMap<>();
        builderAppends.put("sb", Arrays.asList("sb.append(ssn)"));

        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(sb.toString())", builderAppends);
        Map<String, List<String>> retrievedAppends = logStatement.getRelatedBuilderAppends();

        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedAppends.put("newBuilder", Arrays.asList("test"));
        });
    }

    @Test
    void testLogStatementCreation_WithMultipleBuilders() {
        Map<String, List<String>> builderAppends = new HashMap<>();
        builderAppends.put("sb1", Arrays.asList("sb1.append(ssn)", "sb1.append(name)"));
        builderAppends.put("sb2", Arrays.asList("sb2.append(password)"));

        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(sb1.toString())", builderAppends);

        assertEquals(2, logStatement.getRelatedBuilderAppends().size());
        assertTrue(logStatement.getRelatedBuilderAppends().containsKey("sb1"));
        assertTrue(logStatement.getRelatedBuilderAppends().containsKey("sb2"));
    }

    @Test
    void testLogStatementCreation_BuilderAppendsAreIndependent() {
        Map<String, List<String>> builderAppends = new HashMap<>();
        builderAppends.put("sb", Arrays.asList("sb.append(ssn)"));

        LogStatement logStatement = new LogStatement("TestFile.java", 42, "logger.info(sb.toString())", builderAppends);

        // Modify original map
        builderAppends.put("newBuilder", Arrays.asList("test"));

        // LogStatement should not be affected
        assertEquals(1, logStatement.getRelatedBuilderAppends().size());
        assertFalse(logStatement.getRelatedBuilderAppends().containsKey("newBuilder"));
    }
}
