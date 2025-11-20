package com.scanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a log statement found in a Java source file.
 * Contains the file location, line number, and the statement text.
 */
public class LogStatement {
    private final String fileName;
    private final int lineNumber;
    private final String statement;
    private final Map<String, List<String>> relatedBuilderAppends;

    /**
     * Creates a LogStatement with the specified details.
     *
     * @param fileName the name of the file containing the log statement
     * @param lineNumber the line number where the log statement starts
     * @param statement the complete log statement text
     */
    public LogStatement(String fileName, int lineNumber, String statement) {
        this(fileName, lineNumber, statement, new HashMap<>());
    }

    /**
     * Creates a LogStatement with the specified details and related builder appends.
     *
     * @param fileName the name of the file containing the log statement
     * @param lineNumber the line number where the log statement starts
     * @param statement the complete log statement text
     * @param relatedBuilderAppends map of StringBuilder/StringBuffer variable names to their append operations
     */
    public LogStatement(String fileName, int lineNumber, String statement, Map<String, List<String>> relatedBuilderAppends) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.statement = statement;
        this.relatedBuilderAppends = relatedBuilderAppends != null ? new HashMap<>(relatedBuilderAppends) : new HashMap<>();
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getStatement() {
        return statement;
    }

    /**
     * Gets the map of related StringBuilder/StringBuffer append operations.
     *
     * @return an unmodifiable map of builder variable names to their append operations
     */
    public Map<String, List<String>> getRelatedBuilderAppends() {
        return Collections.unmodifiableMap(relatedBuilderAppends);
    }
}
