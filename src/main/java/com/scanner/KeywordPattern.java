package com.scanner;

import java.util.regex.Pattern;

/**
 * Represents a keyword pattern that can be either plain text or a regular expression.
 * Used for matching sensitive data keywords in log statements.
 */
public class KeywordPattern {
    private final String keyword;
    private final boolean isRegex;
    private final Pattern compiledPattern;

    /**
     * Creates a KeywordPattern with the specified keyword and type.
     *
     * @param keyword the keyword string
     * @param isRegex true if the keyword should be treated as a regex pattern, false for plain text
     */
    public KeywordPattern(String keyword, boolean isRegex) {
        this.keyword = keyword;
        this.isRegex = isRegex;
        this.compiledPattern = isRegex ? Pattern.compile(keyword) : null;
    }

    /**
     * Checks if the given text matches this keyword pattern.
     * For regex patterns, uses find() to check if the pattern appears anywhere in the text.
     * For plain text, uses case-insensitive exact match.
     *
     * @param text the text to match against
     * @return true if the text matches the pattern, false otherwise
     */
    public boolean matches(String text) {
        if (text == null) {
            return false;
        }
        
        if (isRegex) {
            return compiledPattern.matcher(text).find();
        } else {
            return text.equalsIgnoreCase(keyword);
        }
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isRegex() {
        return isRegex;
    }

    public Pattern getCompiledPattern() {
        return compiledPattern;
    }
}
