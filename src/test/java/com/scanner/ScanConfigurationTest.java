package com.scanner;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScanConfigurationTest {

    @Test
    void testConfigurationCreation_WithValidData() {
        List<KeywordPattern> patterns = Arrays.asList(
            new KeywordPattern("ssn", false),
            new KeywordPattern("password", false)
        );
        List<KeywordPattern> objectTypes = Arrays.asList(
            new KeywordPattern("request", false),
            new KeywordPattern("response", false)
        );

        ScanConfiguration config = new ScanConfiguration(patterns, objectTypes);

        assertNotNull(config.getPatterns());
        assertNotNull(config.getSensitiveObjectTypes());
        assertEquals(2, config.getPatterns().size());
        assertEquals(2, config.getSensitiveObjectTypes().size());
    }

    @Test
    void testConfigurationCreation_WithEmptyLists() {
        List<KeywordPattern> patterns = new ArrayList<>();
        List<KeywordPattern> objectTypes = new ArrayList<>();

        ScanConfiguration config = new ScanConfiguration(patterns, objectTypes);

        assertNotNull(config.getPatterns());
        assertNotNull(config.getSensitiveObjectTypes());
        assertEquals(0, config.getPatterns().size());
        assertEquals(0, config.getSensitiveObjectTypes().size());
    }

    @Test
    void testConfigurationCreation_WithNullLists() {
        ScanConfiguration config = new ScanConfiguration(null, null);

        assertNotNull(config.getPatterns());
        assertNotNull(config.getSensitiveObjectTypes());
        assertEquals(0, config.getPatterns().size());
        assertEquals(0, config.getSensitiveObjectTypes().size());
    }

    @Test
    void testGetPatterns_ReturnsUnmodifiableList() {
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));

        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());
        List<KeywordPattern> retrievedPatterns = config.getPatterns();

        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedPatterns.add(new KeywordPattern("password", false));
        });
    }

    @Test
    void testGetSensitiveObjectTypes_ReturnsUnmodifiableList() {
        List<KeywordPattern> objectTypes = new ArrayList<>();
        objectTypes.add(new KeywordPattern("request", false));

        ScanConfiguration config = new ScanConfiguration(new ArrayList<>(), objectTypes);
        List<KeywordPattern> retrievedObjectTypes = config.getSensitiveObjectTypes();

        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedObjectTypes.add(new KeywordPattern("response", false));
        });
    }

    @Test
    void testConfigurationCreation_WithMixedPatternTypes() {
        List<KeywordPattern> patterns = Arrays.asList(
            new KeywordPattern("ssn", false),
            new KeywordPattern("(?i).*password.*", true),
            new KeywordPattern("pin", false)
        );
        List<KeywordPattern> objectTypes = Arrays.asList(
            new KeywordPattern("request", false),
            new KeywordPattern(".*Response", true)
        );

        ScanConfiguration config = new ScanConfiguration(patterns, objectTypes);

        assertEquals(3, config.getPatterns().size());
        assertEquals(2, config.getSensitiveObjectTypes().size());
    }

    @Test
    void testConfigurationCreation_PatternsAreIndependent() {
        List<KeywordPattern> patterns = new ArrayList<>();
        patterns.add(new KeywordPattern("ssn", false));

        ScanConfiguration config = new ScanConfiguration(patterns, new ArrayList<>());

        // Modify original list
        patterns.add(new KeywordPattern("password", false));

        // Configuration should not be affected
        assertEquals(1, config.getPatterns().size());
    }
}
