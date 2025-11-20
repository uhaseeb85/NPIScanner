package com.scanner;

/**
 * Represents a detection of sensitive data in a log statement.
 * Contains information about where the sensitive data was found and what keyword matched.
 */
public class Detection {
    private final String fileName;
    private final int lineNumber;
    private final String matchedKeyword;
    private final String logStatement;

    /**
     * Creates a Detection with the specified details.
     *
     * @param fileName the name of the file containing the detection
     * @param lineNumber the line number where the detection was found
     * @param matchedKeyword the keyword that was matched
     * @param logStatement the complete log statement containing the sensitive data
     */
    public Detection(String fileName, int lineNumber, String matchedKeyword, String logStatement) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.matchedKeyword = matchedKeyword;
        this.logStatement = logStatement;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMatchedKeyword() {
        return matchedKeyword;
    }

    public String getLogStatement() {
        return logStatement;
    }
}
