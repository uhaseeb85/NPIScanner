package com.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for the sensitive data scanner.
 * Contains patterns for sensitive keywords and sensitive object types.
 */
public class ScanConfiguration {
    private final List<KeywordPattern> patterns;
    private final List<KeywordPattern> sensitiveObjectTypes;

    /**
     * Creates a ScanConfiguration with the specified patterns and sensitive object types.
     *
     * @param patterns the list of keyword patterns to search for
     * @param sensitiveObjectTypes the list of sensitive object type patterns
     */
    public ScanConfiguration(List<KeywordPattern> patterns, List<KeywordPattern> sensitiveObjectTypes) {
        this.patterns = patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
        this.sensitiveObjectTypes = sensitiveObjectTypes != null ? new ArrayList<>(sensitiveObjectTypes) : new ArrayList<>();
    }

    /**
     * Gets the list of keyword patterns.
     *
     * @return an unmodifiable list of keyword patterns
     */
    public List<KeywordPattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    /**
     * Gets the list of sensitive object type patterns.
     *
     * @return an unmodifiable list of sensitive object type patterns
     */
    public List<KeywordPattern> getSensitiveObjectTypes() {
        return Collections.unmodifiableList(sensitiveObjectTypes);
    }
}
