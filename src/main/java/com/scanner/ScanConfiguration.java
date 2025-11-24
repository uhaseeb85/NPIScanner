package com.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for the sensitive data scanner.
 * Contains patterns for sensitive keywords, sensitive object types, and
 * exclusions.
 */
public class ScanConfiguration {
    private final List<String> scanDirectories;
    private final List<KeywordPattern> patterns;
    private final List<KeywordPattern> sensitiveObjectTypes;
    private final List<KeywordPattern> exclusions;

    /**
     * Creates a ScanConfiguration with the specified patterns and sensitive object
     * types.
     *
     * @param patterns             the list of keyword patterns to search for
     * @param sensitiveObjectTypes the list of sensitive object type patterns
     */
    public ScanConfiguration(List<KeywordPattern> patterns, List<KeywordPattern> sensitiveObjectTypes) {
        this(new ArrayList<>(), patterns, sensitiveObjectTypes, null);
    }

    /**
     * Creates a ScanConfiguration with the specified patterns, sensitive object
     * types, and exclusions.
     *
     * @param patterns             the list of keyword patterns to search for
     * @param sensitiveObjectTypes the list of sensitive object type patterns
     * @param exclusions           the list of exclusion patterns
     */
    public ScanConfiguration(List<KeywordPattern> patterns, List<KeywordPattern> sensitiveObjectTypes,
            List<KeywordPattern> exclusions) {
        this(new ArrayList<>(), patterns, sensitiveObjectTypes, exclusions);
    }

    /**
     * Creates a ScanConfiguration with the specified directories, patterns,
     * sensitive object
     * types, and exclusions.
     *
     * @param scanDirectories      the list of directories to scan
     * @param patterns             the list of keyword patterns to search for
     * @param sensitiveObjectTypes the list of sensitive object type patterns
     * @param exclusions           the list of exclusion patterns
     */
    public ScanConfiguration(List<String> scanDirectories, List<KeywordPattern> patterns,
            List<KeywordPattern> sensitiveObjectTypes,
            List<KeywordPattern> exclusions) {
        this.scanDirectories = scanDirectories != null ? new ArrayList<>(scanDirectories) : new ArrayList<>();
        this.patterns = patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
        this.sensitiveObjectTypes = sensitiveObjectTypes != null ? new ArrayList<>(sensitiveObjectTypes)
                : new ArrayList<>();
        this.exclusions = exclusions != null ? new ArrayList<>(exclusions) : new ArrayList<>();
    }

    /**
     * Gets the list of directories to scan.
     *
     * @return an unmodifiable list of scan directories
     */
    public List<String> getScanDirectories() {
        return Collections.unmodifiableList(scanDirectories);
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

    /**
     * Gets the list of exclusion patterns.
     *
     * @return an unmodifiable list of exclusion patterns
     */
    public List<KeywordPattern> getExclusions() {
        return Collections.unmodifiableList(exclusions);
    }
}
