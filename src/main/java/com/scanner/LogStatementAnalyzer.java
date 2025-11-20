package com.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Analyzes Java source files to identify and extract log statements.
 * Detects common logging patterns and filters out non-logging code.
 */
public class LogStatementAnalyzer {
    
    // Pattern to detect log statements: logger., log., LOGGER., LOG.
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "\\b(logger|log|LOGGER|LOG)\\s*\\."
    );
    
    // Pattern to detect common logging methods
    private static final Pattern LOG_METHOD_PATTERN = Pattern.compile(
        "\\b(logger|log|LOGGER|LOG)\\s*\\.\\s*(info|debug|warn|error|trace|fatal)\\s*\\("
    );
    
    // Pattern to detect System.out.println() and System.err.println()
    private static final Pattern SYSTEM_PRINT_PATTERN = Pattern.compile(
        "\\bSystem\\s*\\.\\s*(out|err)\\s*\\.\\s*(print|println|printf)\\s*\\("
    );
    
    // Pattern to detect method signatures and parameter definitions
    // Matches: modifiers + return type + method name + parameters
    private static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile(
        "^\\s*" +
        "(?:public|private|protected|static|final|abstract|synchronized|native|strictfp|\\s)*" +
        "\\s*" +
        "(?:void|boolean|byte|char|short|int|long|float|double|[A-Z][a-zA-Z0-9_<>\\[\\],\\s]*)" +
        "\\s+" +
        "[a-zA-Z_][a-zA-Z0-9_]*" +
        "\\s*\\([^)]*\\)" +
        "\\s*[{;]?"
    );

    /**
     * Analyzes a Java source file and extracts all log statements.
     * Handles file reading errors gracefully by logging and returning an empty list.
     *
     * @param filePath the path to the Java file to analyze
     * @return a list of LogStatement objects found in the file, or empty list if file cannot be read
     * @throws IOException if an error occurs reading the file
     */
    public List<LogStatement> analyzeFile(Path filePath) throws IOException {
        List<LogStatement> logStatements = new ArrayList<>();
        String fileName = filePath.getFileName().toString();
        
        try {
            // Read all lines first to handle multi-line statements properly
            List<String> allLines = Files.readAllLines(filePath);
            
            // Track StringBuilder/StringBuffer variables and their append operations
            java.util.Map<String, List<String>> builderAppends = trackStringBuilders(allLines);
            
            int lineIndex = 0;
            while (lineIndex < allLines.size()) {
                String line = allLines.get(lineIndex);
                int lineNumber = lineIndex + 1; // Line numbers are 1-based
                
                // Skip method signatures and parameter definitions
                if (isMethodSignature(line)) {
                    lineIndex++;
                    continue;
                }
                
                if (isLogStatement(line)) {
                    // Extract complete statement and find how many lines it spans
                    int[] result = extractCompleteStatementFromLines(allLines, lineIndex);
                    String completeStatement = allLines.get(lineIndex).trim();
                    int endLineIndex = result[0];
                    
                    // Build the complete statement from multiple lines
                    StringBuilder statement = new StringBuilder(allLines.get(lineIndex).trim());
                    for (int i = lineIndex + 1; i <= endLineIndex; i++) {
                        statement.append(" ").append(allLines.get(i).trim());
                    }
                    
                    // Check if this log statement uses any tracked builders
                    java.util.Map<String, List<String>> relatedBuilders = findRelatedBuilders(statement.toString(), builderAppends);
                    
                    logStatements.add(new LogStatement(fileName, lineNumber, statement.toString(), relatedBuilders));
                    
                    // Move past the lines we just processed
                    lineIndex = endLineIndex + 1;
                } else {
                    lineIndex++;
                }
            }
        } catch (IOException e) {
            // Log the error and re-throw to allow caller to handle
            System.err.println("Error reading file " + fileName + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle any other unexpected errors during parsing
            System.err.println("Error analyzing file " + fileName + ": " + e.getMessage());
            // Return what we've collected so far rather than failing completely
        }
        
        return logStatements;
    }

    /**
     * Checks if a line of code contains a log statement.
     * Detects patterns like logger.info(), log.debug(), LOGGER.error(), etc.
     * Also detects System.out.println() and System.err.println() statements.
     *
     * @param line the line of code to check
     * @return true if the line contains a log statement, false otherwise
     */
    boolean isLogStatement(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        // Check if line contains logging pattern with a logging method
        if (LOG_METHOD_PATTERN.matcher(line).find()) {
            return true;
        }
        
        // Check if line contains System.out.println() or System.err.println()
        return SYSTEM_PRINT_PATTERN.matcher(line).find();
    }

    /**
     * Checks if a line of code is a method signature or parameter definition.
     * This helps filter out method declarations that might contain sensitive parameter names.
     *
     * @param line the line of code to check
     * @return true if the line is a method signature, false otherwise
     */
    boolean isMethodSignature(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = line.trim();
        
        // Check for method signature pattern
        return METHOD_SIGNATURE_PATTERN.matcher(trimmed).find();
    }

    /**
     * Extracts a complete log statement from a list of lines, handling multi-line statements.
     * Finds the end line index where the statement terminates (semicolon).
     *
     * @param lines the list of all lines in the file
     * @param startIndex the index where the log statement starts
     * @return an array with one element: the end line index
     */
    int[] extractCompleteStatementFromLines(List<String> lines, int startIndex) {
        // If the first line already ends with a semicolon, it's complete
        if (lines.get(startIndex).trim().endsWith(";")) {
            return new int[]{startIndex};
        }
        
        // Find the line that ends with a semicolon
        for (int i = startIndex + 1; i < lines.size(); i++) {
            if (lines.get(i).trim().endsWith(";")) {
                return new int[]{i};
            }
        }
        
        // If no semicolon found, return the start index (incomplete statement)
        return new int[]{startIndex};
    }

    /**
     * Tracks StringBuilder and StringBuffer variables throughout the file.
     * Identifies variable declarations and collects all append() operations for each variable.
     *
     * @param fileLines all lines in the file
     * @return a map of builder variable names to their append operation contents
     */
    java.util.Map<String, List<String>> trackStringBuilders(List<String> fileLines) {
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        
        // Pattern to detect StringBuilder/StringBuffer declarations
        Pattern declarationPattern = Pattern.compile(
            "(StringBuilder|StringBuffer)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*="
        );
        
        // Pattern to detect append operations - more flexible to handle chained calls
        Pattern appendPattern = Pattern.compile(
            "\\.\\s*append\\s*\\(([^)]+)\\)"
        );
        
        for (String line : fileLines) {
            // Check for builder declarations
            java.util.regex.Matcher declMatcher = declarationPattern.matcher(line);
            while (declMatcher.find()) {
                String varName = declMatcher.group(2);
                builderAppends.putIfAbsent(varName, new ArrayList<>());
            }
            
            // Check for append operations on tracked builders
            for (String varName : builderAppends.keySet()) {
                // Check if this line contains operations on this builder variable
                if (line.contains(varName + ".append")) {
                    // Find all append operations on this line
                    java.util.regex.Matcher appendMatcher = appendPattern.matcher(line);
                    while (appendMatcher.find()) {
                        String appendContent = appendMatcher.group(1);
                        builderAppends.get(varName).add(appendContent);
                    }
                }
            }
        }
        
        return builderAppends;
    }

    /**
     * Finds which tracked builder variables are used in a log statement.
     *
     * @param logStatement the complete log statement
     * @param builderAppends map of all tracked builder variables and their appends
     * @return a map of builder variables used in this statement to their append operations
     */
    java.util.Map<String, List<String>> findRelatedBuilders(String logStatement, java.util.Map<String, List<String>> builderAppends) {
        java.util.Map<String, List<String>> relatedBuilders = new java.util.HashMap<>();
        
        // Check if any tracked builder variable is referenced in the log statement
        for (String builderVar : builderAppends.keySet()) {
            // Look for patterns like: logger.info(sb.toString()) or logger.info(sb)
            Pattern usagePattern = Pattern.compile("\\b" + Pattern.quote(builderVar) + "\\b");
            if (usagePattern.matcher(logStatement).find()) {
                relatedBuilders.put(builderVar, builderAppends.get(builderVar));
            }
        }
        
        return relatedBuilders;
    }

    /**
     * Extracts a complete log statement, handling multi-line statements.
     * Reads additional lines until a statement terminator (semicolon) is found.
     *
     * @param reader the BufferedReader to read additional lines from
     * @param firstLine the first line of the log statement
     * @param startLine the line number where the statement starts
     * @return the complete log statement as a single string
     * @throws IOException if an error occurs reading the file
     */
    String extractCompleteStatement(BufferedReader reader, String firstLine, int startLine) throws IOException {
        StringBuilder statement = new StringBuilder(firstLine.trim());
        
        // If the first line already ends with a semicolon, it's complete
        if (firstLine.trim().endsWith(";")) {
            return statement.toString();
        }
        
        // Read additional lines until we find a semicolon
        String line;
        
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            
            // Append the line to the statement
            statement.append(" ").append(trimmedLine);
            
            // Check if this line ends the statement
            if (trimmedLine.endsWith(";")) {
                break;
            }
        }
        
        return statement.toString();
    }
}
