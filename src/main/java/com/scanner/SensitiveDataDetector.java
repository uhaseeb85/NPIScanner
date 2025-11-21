package com.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects sensitive data in log statements by analyzing variables, method calls,
 * and other patterns that may expose sensitive information.
 */
public class SensitiveDataDetector {
    
    // Pattern to identify string literals (content within double quotes)
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"([^\"]*)\"");
    
    // Pattern to extract variable names (Java identifiers)
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    
    // Pattern to extract method calls (e.g., getSSN(), card.getNumber())
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("\\.?([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    
    // Pattern to detect StringBuilder/StringBuffer append patterns (single-line)
    // Matches .append(...) calls
    private static final Pattern STRING_BUILDER_APPEND_PATTERN = Pattern.compile("\\.append\\s*\\(([^)]+)\\)");
    
    // Pattern to detect String.join patterns
    private static final Pattern STRING_JOIN_PATTERN = Pattern.compile("String\\.join\\s*\\([^,]+,\\s*(.+)\\)");
    
    // Pattern to detect StringJoiner.add patterns
    private static final Pattern STRING_JOINER_ADD_PATTERN = Pattern.compile("StringJoiner.*\\.add\\s*\\(([^)]+)\\)");
    
    /**
     * Detects sensitive data in the given log statement based on the configuration.
     *
     * @param logStatement the log statement to analyze
     * @param config the scan configuration containing keyword patterns
     * @return a list of detections found in the log statement
     */
    public List<Detection> detectSensitiveData(LogStatement logStatement, ScanConfiguration config) {
        List<Detection> detections = new ArrayList<>();
        
        if (logStatement == null || config == null) {
            return detections;
        }
        
        String statement = logStatement.getStatement();
        List<String> detectedIdentifiers = new ArrayList<>();
        
        // Extract variables from the statement
        List<String> variables = extractVariables(statement);
        
        // Check each variable against configured keywords
        for (String variable : variables) {
            for (KeywordPattern pattern : config.getPatterns()) {
                if (matchesKeyword(variable, pattern) && !detectedIdentifiers.contains(variable)) {
                    detections.add(new Detection(
                        logStatement.getFileName(),
                        logStatement.getLineNumber(),
                        pattern.getKeyword(),
                        statement
                    ));
                    detectedIdentifiers.add(variable);
                    break; // Only create one detection per variable
                }
            }
        }
        
        // Extract method calls from the statement
        List<String> methodCalls = extractMethodCalls(statement);
        
        // Check each method call against configured keywords
        for (String methodCall : methodCalls) {
            for (KeywordPattern pattern : config.getPatterns()) {
                if (matchesKeyword(methodCall, pattern) && !detectedIdentifiers.contains(methodCall)) {
                    detections.add(new Detection(
                        logStatement.getFileName(),
                        logStatement.getLineNumber(),
                        pattern.getKeyword(),
                        statement
                    ));
                    detectedIdentifiers.add(methodCall);
                    break; // Only create one detection per method call
                }
            }
        }
        
        // Extract identifiers from StringBuilder/StringBuffer append operations (single-line)
        List<String> builderAppends = extractStringBuilderAppends(statement);
        
        // Check each identifier from builder appends against configured keywords
        for (String identifier : builderAppends) {
            for (KeywordPattern pattern : config.getPatterns()) {
                if (matchesKeyword(identifier, pattern) && !detectedIdentifiers.contains(identifier)) {
                    detections.add(new Detection(
                        logStatement.getFileName(),
                        logStatement.getLineNumber(),
                        pattern.getKeyword(),
                        statement
                    ));
                    detectedIdentifiers.add(identifier);
                    break; // Only create one detection per identifier
                }
            }
        }
        
        // Check multi-line StringBuilder/StringBuffer patterns
        // Analyze relatedBuilderAppends map for tracked append operations
        for (List<String> appendOperations : logStatement.getRelatedBuilderAppends().values()) {
            for (String appendOp : appendOperations) {
                // Extract variables and method calls from each append operation
                List<String> appendVars = extractVariables(appendOp);
                List<String> appendMethods = extractMethodCalls(appendOp);
                
                List<String> allIdentifiers = new ArrayList<>();
                allIdentifiers.addAll(appendVars);
                allIdentifiers.addAll(appendMethods);
                
                // Check each identifier against configured keywords
                for (String identifier : allIdentifiers) {
                    for (KeywordPattern pattern : config.getPatterns()) {
                        if (matchesKeyword(identifier, pattern) && !detectedIdentifiers.contains(identifier)) {
                            detections.add(new Detection(
                                logStatement.getFileName(),
                                logStatement.getLineNumber(),
                                pattern.getKeyword(),
                                statement
                            ));
                            detectedIdentifiers.add(identifier);
                            break; // Only create one detection per identifier
                        }
                    }
                }
            }
        }
        
        // Extract identifiers from String.join patterns
        List<String> stringJoinIdentifiers = extractStringJoinIdentifiers(statement);
        
        // Check each identifier from String.join against configured keywords
        for (String identifier : stringJoinIdentifiers) {
            for (KeywordPattern pattern : config.getPatterns()) {
                if (matchesKeyword(identifier, pattern) && !detectedIdentifiers.contains(identifier)) {
                    detections.add(new Detection(
                        logStatement.getFileName(),
                        logStatement.getLineNumber(),
                        pattern.getKeyword(),
                        statement
                    ));
                    detectedIdentifiers.add(identifier);
                    break; // Only create one detection per identifier
                }
            }
        }
        
        // Extract identifiers from StringJoiner.add patterns
        List<String> stringJoinerIdentifiers = extractStringJoinerIdentifiers(statement);
        
        // Check each identifier from StringJoiner against configured keywords
        for (String identifier : stringJoinerIdentifiers) {
            for (KeywordPattern pattern : config.getPatterns()) {
                if (matchesKeyword(identifier, pattern) && !detectedIdentifiers.contains(identifier)) {
                    detections.add(new Detection(
                        logStatement.getFileName(),
                        logStatement.getLineNumber(),
                        pattern.getKeyword(),
                        statement
                    ));
                    detectedIdentifiers.add(identifier);
                    break; // Only create one detection per identifier
                }
            }
        }
        
        // Check for sensitive object types
        List<String> objectReferences = extractObjectReferences(statement);
        
        // Check each object reference against configured sensitive object types
        for (String objectRef : objectReferences) {
            if (isSensitiveObjectType(objectRef, config.getSensitiveObjectTypes()) && !detectedIdentifiers.contains(objectRef)) {
                // Find the matching object type pattern to use as the keyword
                for (KeywordPattern objectType : config.getSensitiveObjectTypes()) {
                    if (objectType.matches(objectRef)) {
                        detections.add(new Detection(
                            logStatement.getFileName(),
                            logStatement.getLineNumber(),
                            objectType.getKeyword(),
                            statement
                        ));
                        detectedIdentifiers.add(objectRef);
                        break; // Only create one detection per object reference
                    }
                }
            }
        }
        
        // NEW: Check for sensitive keywords within string literals
        // This detects cases like: logger.info("SSN: " + anyVariable)
        // Only check if the statement contains variables/methods being logged
        // (not just a plain string literal with no dynamic data)
        boolean hasVariablesOrMethodsOutsideLiterals = hasNonLiteralContent(statement);
        
        if (hasVariablesOrMethodsOutsideLiterals) {
            List<String> stringLiterals = extractStringLiterals(statement);
            
            for (String literal : stringLiterals) {
                for (KeywordPattern pattern : config.getPatterns()) {
                    // Check if the string literal content contains the sensitive keyword
                    if (containsKeyword(literal, pattern)) {
                        String detectionKey = "literal:" + pattern.getKeyword();
                        if (!detectedIdentifiers.contains(detectionKey)) {
                            detections.add(new Detection(
                                logStatement.getFileName(),
                                logStatement.getLineNumber(),
                                pattern.getKeyword() + " (in string literal)",
                                statement
                            ));
                            detectedIdentifiers.add(detectionKey);
                            break; // Only create one detection per keyword in literals
                        }
                    }
                }
            }
        }
        
        return detections;
    }
    
    /**
     * Checks if the given token is a string literal (content within double quotes).
     *
     * @param token the token to check
     * @return true if the token is a string literal, false otherwise
     */
    boolean isStringLiteral(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Check if the token is surrounded by double quotes
        return token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2;
    }
    
    /**
     * Extracts the positions of all string literals in the statement.
     * This is used to exclude string literal regions from variable detection.
     *
     * @param statement the statement to analyze
     * @return a list of ranges (start, end) representing string literal positions
     */
    List<int[]> getStringLiteralRanges(String statement) {
        List<int[]> ranges = new ArrayList<>();
        
        if (statement == null) {
            return ranges;
        }
        
        Matcher matcher = STRING_LITERAL_PATTERN.matcher(statement);
        while (matcher.find()) {
            ranges.add(new int[]{matcher.start(), matcher.end()});
        }
        
        return ranges;
    }
    
    /**
     * Checks if a given position in the statement is within a string literal.
     *
     * @param position the position to check
     * @param stringLiteralRanges the list of string literal ranges
     * @return true if the position is within a string literal, false otherwise
     */
    boolean isWithinStringLiteral(int position, List<int[]> stringLiteralRanges) {
        for (int[] range : stringLiteralRanges) {
            if (position >= range[0] && position < range[1]) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extracts variable names from the statement, excluding those within string literals.
     *
     * @param statement the statement to analyze
     * @return a list of variable names found in the statement
     */
    List<String> extractVariables(String statement) {
        List<String> variables = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return variables;
        }
        
        // Get string literal ranges to exclude them
        List<int[]> stringLiteralRanges = getStringLiteralRanges(statement);
        
        Matcher matcher = VARIABLE_PATTERN.matcher(statement);
        while (matcher.find()) {
            int startPos = matcher.start();
            
            // Only include variables that are not within string literals
            if (!isWithinStringLiteral(startPos, stringLiteralRanges)) {
                variables.add(matcher.group(1));
            }
        }
        
        return variables;
    }
    
    /**
     * Checks if the given identifier matches the keyword pattern.
     *
     * @param identifier the identifier to check
     * @param pattern the keyword pattern to match against
     * @return true if the identifier matches the pattern, false otherwise
     */
    boolean matchesKeyword(String identifier, KeywordPattern pattern) {
        if (identifier == null || pattern == null) {
            return false;
        }
        
        return pattern.matches(identifier);
    }
    
    /**
     * Extracts method call names from the statement, excluding those within string literals.
     * Detects patterns like getSSN(), card.getNumber(), user.getDOB().
     *
     * @param statement the statement to analyze
     * @return a list of method names found in the statement
     */
    List<String> extractMethodCalls(String statement) {
        List<String> methodCalls = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return methodCalls;
        }
        
        // Get string literal ranges to exclude them
        List<int[]> stringLiteralRanges = getStringLiteralRanges(statement);
        
        Matcher matcher = METHOD_CALL_PATTERN.matcher(statement);
        while (matcher.find()) {
            int startPos = matcher.start(1); // Position of the method name (group 1)
            
            // Only include method calls that are not within string literals
            if (!isWithinStringLiteral(startPos, stringLiteralRanges)) {
                methodCalls.add(matcher.group(1));
            }
        }
        
        return methodCalls;
    }
    
    /**
     * Extracts identifiers from StringBuilder/StringBuffer append operations (single-line).
     * Detects patterns like sb.append(ssn) or new StringBuilder().append(password).
     *
     * @param statement the statement to analyze
     * @return a list of identifiers found in append operations
     */
    List<String> extractStringBuilderAppends(String statement) {
        List<String> identifiers = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return identifiers;
        }
        
        // Only process if the statement contains StringBuilder or StringBuffer
        if (!statement.contains("StringBuilder") && !statement.contains("StringBuffer")) {
            return identifiers;
        }
        
        Matcher matcher = STRING_BUILDER_APPEND_PATTERN.matcher(statement);
        while (matcher.find()) {
            String appendContent = matcher.group(1).trim();
            
            // Extract variables and method calls from the append content
            List<String> variables = extractVariables(appendContent);
            List<String> methodCalls = extractMethodCalls(appendContent);
            
            identifiers.addAll(variables);
            identifiers.addAll(methodCalls);
        }
        
        return identifiers;
    }
    
    /**
     * Extracts identifiers from String.join patterns.
     * Detects patterns like String.join(", ", email, phone, ssn).
     *
     * @param statement the statement to analyze
     * @return a list of identifiers found in String.join operations
     */
    List<String> extractStringJoinIdentifiers(String statement) {
        List<String> identifiers = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return identifiers;
        }
        
        Matcher matcher = STRING_JOIN_PATTERN.matcher(statement);
        while (matcher.find()) {
            String joinContent = matcher.group(1).trim();
            
            // Extract variables and method calls from the join content
            List<String> variables = extractVariables(joinContent);
            List<String> methodCalls = extractMethodCalls(joinContent);
            
            identifiers.addAll(variables);
            identifiers.addAll(methodCalls);
        }
        
        return identifiers;
    }
    
    /**
     * Extracts identifiers from StringJoiner.add patterns.
     * Detects patterns like joiner.add(ssn) or new StringJoiner().add(password).
     *
     * @param statement the statement to analyze
     * @return a list of identifiers found in StringJoiner.add operations
     */
    List<String> extractStringJoinerIdentifiers(String statement) {
        List<String> identifiers = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return identifiers;
        }
        
        // Only process if the statement contains StringJoiner
        if (!statement.contains("StringJoiner")) {
            return identifiers;
        }
        
        Matcher matcher = STRING_JOINER_ADD_PATTERN.matcher(statement);
        while (matcher.find()) {
            String addContent = matcher.group(1).trim();
            
            // Extract variables and method calls from the add content
            List<String> variables = extractVariables(addContent);
            List<String> methodCalls = extractMethodCalls(addContent);
            
            identifiers.addAll(variables);
            identifiers.addAll(methodCalls);
        }
        
        return identifiers;
    }
    
    /**
     * Extracts all variable and object identifiers from the statement.
     * This includes standalone variables and objects that may be logged directly.
     *
     * @param statement the statement to analyze
     * @return a list of object references found in the statement
     */
    List<String> extractObjectReferences(String statement) {
        // For object references, we use the same logic as extractVariables
        // since object references are also identifiers
        return extractVariables(statement);
    }
    
    /**
     * Checks if the given identifier matches any sensitive object type pattern.
     *
     * @param identifier the identifier to check
     * @param objectTypes the list of sensitive object type patterns
     * @return true if the identifier matches a sensitive object type, false otherwise
     */
    boolean isSensitiveObjectType(String identifier, List<KeywordPattern> objectTypes) {
        if (identifier == null || objectTypes == null) {
            return false;
        }
        
        for (KeywordPattern objectType : objectTypes) {
            if (objectType.matches(identifier)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extracts the content of all string literals from the statement.
     * Returns the text inside the quotes without the quotes themselves.
     *
     * @param statement the statement to analyze
     * @return a list of string literal contents
     */
    List<String> extractStringLiterals(String statement) {
        List<String> literals = new ArrayList<>();
        
        if (statement == null || statement.isEmpty()) {
            return literals;
        }
        
        Matcher matcher = STRING_LITERAL_PATTERN.matcher(statement);
        while (matcher.find()) {
            // Group 1 contains the content inside the quotes
            String literalContent = matcher.group(1);
            if (literalContent != null && !literalContent.trim().isEmpty()) {
                literals.add(literalContent);
            }
        }
        
        return literals;
    }
    
    /**
     * Checks if a string literal contains a sensitive keyword.
     * This performs case-insensitive matching for plain keywords and regex matching for patterns.
     *
     * @param literal the string literal content to check
     * @param pattern the keyword pattern to match against
     * @return true if the literal contains the keyword, false otherwise
     */
    boolean containsKeyword(String literal, KeywordPattern pattern) {
        if (literal == null || pattern == null) {
            return false;
        }
        
        String keyword = pattern.getKeyword();
        
        if (pattern.isRegex()) {
            // For regex patterns, check if the pattern matches anywhere in the literal
            try {
                Pattern regexPattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
                return regexPattern.matcher(literal).find();
            } catch (Exception e) {
                // If regex is invalid, fall back to plain text matching
                return literal.toLowerCase().contains(keyword.toLowerCase());
            }
        } else {
            // For plain text keywords, do case-insensitive substring matching
            return literal.toLowerCase().contains(keyword.toLowerCase());
        }
    }
    
    /**
     * Checks if a log statement contains non-literal content (variables, method calls, etc.).
     * This helps determine if the statement is logging dynamic data or just a static string.
     * 
     * @param statement the log statement to check
     * @return true if the statement contains variables/methods outside of string literals
     */
    boolean hasNonLiteralContent(String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            return false;
        }
        
        // Remove all string literals from the statement
        String withoutLiterals = statement.replaceAll("\"[^\"]*\"", "");
        
        // Remove common logging patterns to isolate potential variables
        String cleaned = withoutLiterals
            .replaceAll("(logger|log|LOGGER|LOG)\\s*\\.\\s*(info|debug|warn|error|trace|fatal)\\s*\\(", "")
            .replaceAll("System\\s*\\.\\s*(out|err)\\s*\\.\\s*(print|println|printf)\\s*\\(", "")
            .replaceAll("[\\(\\);]", "")  // Remove parentheses and semicolons
            .replaceAll("\\s+", " ")       // Normalize whitespace
            .trim();
        
        // If there's a + operator, check what's around it
        if (cleaned.contains("+")) {
            // Split by + and check if any part has actual content (not just whitespace)
            String[] parts = cleaned.split("\\+");
            for (String part : parts) {
                String trimmed = part.trim();
                // If there's any non-empty content, it's likely a variable
                if (!trimmed.isEmpty()) {
                    return true;
                }
            }
        }
        
        // Check if there are any identifiers left (potential variables)
        // Match word characters that could be variable names
        Pattern identifierPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
        Matcher matcher = identifierPattern.matcher(cleaned);
        while (matcher.find()) {
            String identifier = matcher.group();
            // If we find any identifier, it's likely a variable
            if (!identifier.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
