package com.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogStatementAnalyzerTest {

    private LogStatementAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new LogStatementAnalyzer();
    }

    @Test
    void testIsLogStatement_WithLoggerInfo() {
        assertTrue(analyzer.isLogStatement("logger.info(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithLoggerDebug() {
        assertTrue(analyzer.isLogStatement("logger.debug(\"debug message\");"));
    }

    @Test
    void testIsLogStatement_WithLoggerWarn() {
        assertTrue(analyzer.isLogStatement("logger.warn(\"warning\");"));
    }

    @Test
    void testIsLogStatement_WithLoggerError() {
        assertTrue(analyzer.isLogStatement("logger.error(\"error occurred\");"));
    }

    @Test
    void testIsLogStatement_WithLoggerTrace() {
        assertTrue(analyzer.isLogStatement("logger.trace(\"trace info\");"));
    }

    @Test
    void testIsLogStatement_WithLoggerFatal() {
        assertTrue(analyzer.isLogStatement("logger.fatal(\"fatal error\");"));
    }

    @Test
    void testIsLogStatement_WithUppercaseLOGGER() {
        assertTrue(analyzer.isLogStatement("LOGGER.info(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithUppercaseLOG() {
        assertTrue(analyzer.isLogStatement("LOG.debug(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithLowercaseLog() {
        assertTrue(analyzer.isLogStatement("log.error(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithWhitespace() {
        assertTrue(analyzer.isLogStatement("    logger.info(\"test\");"));
        assertTrue(analyzer.isLogStatement("logger  .  info(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithNonLogStatement() {
        assertFalse(analyzer.isLogStatement("String message = \"test\";"));
        assertFalse(analyzer.isLogStatement("int count = 0;"));
        assertFalse(analyzer.isLogStatement("public void method() {"));
    }

    @Test
    void testIsLogStatement_WithEmptyLine() {
        assertFalse(analyzer.isLogStatement(""));
        assertFalse(analyzer.isLogStatement("   "));
    }

    @Test
    void testIsLogStatement_WithNull() {
        assertFalse(analyzer.isLogStatement(null));
    }

    @Test
    void testIsLogStatement_WithLoggerButNoMethod() {
        assertFalse(analyzer.isLogStatement("Logger logger = LoggerFactory.getLogger();"));
    }

    @Test
    void testIsLogStatement_WithSystemOutPrintln() {
        assertTrue(analyzer.isLogStatement("System.out.println(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithSystemErrPrintln() {
        assertTrue(analyzer.isLogStatement("System.err.println(\"error\");"));
    }

    @Test
    void testIsLogStatement_WithSystemOutPrint() {
        assertTrue(analyzer.isLogStatement("System.out.print(\"test\");"));
    }

    @Test
    void testIsLogStatement_WithSystemErrPrint() {
        assertTrue(analyzer.isLogStatement("System.err.print(\"error\");"));
    }

    @Test
    void testIsLogStatement_WithSystemOutPrintf() {
        assertTrue(analyzer.isLogStatement("System.out.printf(\"test %s\", value);"));
    }

    @Test
    void testIsLogStatement_WithSystemErrPrintf() {
        assertTrue(analyzer.isLogStatement("System.err.printf(\"error %d\", code);"));
    }

    @Test
    void testIsLogStatement_WithSystemPrintAndWhitespace() {
        assertTrue(analyzer.isLogStatement("    System.out.println(\"test\");"));
        assertTrue(analyzer.isLogStatement("System  .  out  .  println(\"test\");"));
    }

    @Test
    void testAnalyzeFile_WithMultipleLogStatements(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("TestClass.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class TestClass {\n" +
            "    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);\n" +
            "    \n" +
            "    public void method() {\n" +
            "        logger.info(\"Starting method\");\n" +
            "        int count = 0;\n" +
            "        logger.debug(\"Count: \" + count);\n" +
            "        logger.error(\"Error occurred\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(3, statements.size());
        assertEquals("TestClass.java", statements.get(0).getFileName());
        assertTrue(statements.get(0).getStatement().contains("logger.info"));
        assertTrue(statements.get(1).getStatement().contains("logger.debug"));
        assertTrue(statements.get(2).getStatement().contains("logger.error"));
    }

    @Test
    void testAnalyzeFile_WithNoLogStatements(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("NoLogs.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class NoLogs {\n" +
            "    public void method() {\n" +
            "        String message = \"test\";\n" +
            "        int count = 0;\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertTrue(statements.isEmpty());
    }

    @Test
    void testAnalyzeFile_WithVariousLoggerPatterns(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("VariousPatterns.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class VariousPatterns {\n" +
            "    public void method() {\n" +
            "        logger.info(\"lowercase logger\");\n" +
            "        LOGGER.debug(\"uppercase LOGGER\");\n" +
            "        log.warn(\"lowercase log\");\n" +
            "        LOG.error(\"uppercase LOG\");\n" +
            "        logger.trace(\"trace level\");\n" +
            "        LOGGER.fatal(\"fatal level\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(6, statements.size());
    }

    @Test
    void testAnalyzeFile_WithSystemPrintStatements(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("SystemPrint.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class SystemPrint {\n" +
            "    public void method(String ssn, String password) {\n" +
            "        System.out.println(\"SSN: \" + ssn);\n" +
            "        System.err.println(\"Password: \" + password);\n" +
            "        System.out.print(\"User data: \" + ssn);\n" +
            "        System.err.printf(\"Error with SSN: %s\", ssn);\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(4, statements.size());
        assertTrue(statements.get(0).getStatement().contains("System.out.println"));
        assertTrue(statements.get(1).getStatement().contains("System.err.println"));
        assertTrue(statements.get(2).getStatement().contains("System.out.print"));
        assertTrue(statements.get(3).getStatement().contains("System.err.printf"));
    }

    @Test
    void testAnalyzeFile_WithMixedLoggerAndSystemPrint(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("MixedLogging.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class MixedLogging {\n" +
            "    public void method(String ssn) {\n" +
            "        logger.info(\"Using logger: \" + ssn);\n" +
            "        System.out.println(\"Using System.out: \" + ssn);\n" +
            "        log.debug(\"Debug with logger\");\n" +
            "        System.err.println(\"Error with System.err\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(4, statements.size());
    }

    @Test
    void testAnalyzeFile_WithEmptyFile(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Empty.java");
        Files.writeString(javaFile, "");

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertTrue(statements.isEmpty());
    }

    @Test
    void testAnalyzeFile_TracksCorrectLineNumbers(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("LineNumbers.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class LineNumbers {\n" +
            "    public void method() {\n" +
            "        logger.info(\"line 5\");\n" +
            "        String x = \"test\";\n" +
            "        logger.debug(\"line 7\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(2, statements.size());
        assertEquals(5, statements.get(0).getLineNumber());
        assertEquals(7, statements.get(1).getLineNumber());
    }

    // Tests for method signature filtering

    @Test
    void testIsMethodSignature_WithPublicMethod() {
        assertTrue(analyzer.isMethodSignature("public void log(String ssn) {"));
        assertTrue(analyzer.isMethodSignature("public void method(String ssn, int pin) {"));
    }

    @Test
    void testIsMethodSignature_WithPrivateMethod() {
        assertTrue(analyzer.isMethodSignature("private void log(String ssn) {"));
    }

    @Test
    void testIsMethodSignature_WithProtectedMethod() {
        assertTrue(analyzer.isMethodSignature("protected void log(String ssn) {"));
    }

    @Test
    void testIsMethodSignature_WithStaticMethod() {
        assertTrue(analyzer.isMethodSignature("public static void log(String ssn) {"));
    }

    @Test
    void testIsMethodSignature_WithReturnType() {
        assertTrue(analyzer.isMethodSignature("public String getSSN() {"));
        assertTrue(analyzer.isMethodSignature("private int getPin() {"));
    }

    @Test
    void testIsMethodSignature_WithGenericReturnType() {
        assertTrue(analyzer.isMethodSignature("public List<String> getSSNList() {"));
    }

    @Test
    void testIsMethodSignature_WithAbstractMethod() {
        assertTrue(analyzer.isMethodSignature("public abstract void log(String ssn);"));
    }

    @Test
    void testIsMethodSignature_WithInterfaceMethod() {
        assertTrue(analyzer.isMethodSignature("void log(String ssn);"));
    }

    @Test
    void testIsMethodSignature_WithWhitespace() {
        assertTrue(analyzer.isMethodSignature("    public void log(String ssn) {"));
    }

    @Test
    void testIsMethodSignature_WithLogStatement() {
        assertFalse(analyzer.isMethodSignature("logger.info(\"test\");"));
        assertFalse(analyzer.isMethodSignature("log.debug(\"message\");"));
    }

    @Test
    void testIsMethodSignature_WithVariableDeclaration() {
        assertFalse(analyzer.isMethodSignature("String ssn = \"123-45-6789\";"));
    }

    @Test
    void testIsMethodSignature_WithNull() {
        assertFalse(analyzer.isMethodSignature(null));
    }

    @Test
    void testIsMethodSignature_WithEmptyLine() {
        assertFalse(analyzer.isMethodSignature(""));
        assertFalse(analyzer.isMethodSignature("   "));
    }

    @Test
    void testAnalyzeFile_FiltersOutMethodSignatures(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("MethodSignatures.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class MethodSignatures {\n" +
            "    public void log(String ssn) {\n" +
            "        logger.info(\"Inside method\");\n" +
            "    }\n" +
            "    \n" +
            "    private String getSSN() {\n" +
            "        logger.debug(\"Getting SSN\");\n" +
            "        return \"123-45-6789\";\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should only find the logger statements, not the method signatures
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).getStatement().contains("logger.info"));
        assertTrue(statements.get(1).getStatement().contains("logger.debug"));
    }

    @Test
    void testAnalyzeFile_WithMethodParametersContainingSensitiveNames(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("SensitiveParams.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class SensitiveParams {\n" +
            "    public void processUser(String ssn, String debitCard, int pin) {\n" +
            "        logger.info(\"Processing user\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should not flag the method signature line, only the logger statement
        assertEquals(1, statements.size());
        assertEquals(5, statements.get(0).getLineNumber());
    }

    // Tests for multi-line log statement handling

    @Test
    void testExtractCompleteStatement_WithSingleLine() throws IOException {
        String line = "logger.info(\"test\");";
        BufferedReader reader = new BufferedReader(new StringReader(""));
        
        String result = analyzer.extractCompleteStatement(reader, line, 1);
        
        assertEquals("logger.info(\"test\");", result);
    }

    @Test
    void testExtractCompleteStatement_WithMultipleLines() throws IOException {
        String firstLine = "logger.info(\"User: \" +";
        String remainingLines = "    username +\n" +
            "    \" SSN: \" + ssn);";
        BufferedReader reader = new BufferedReader(new StringReader(remainingLines));
        
        String result = analyzer.extractCompleteStatement(reader, firstLine, 1);
        
        assertTrue(result.contains("logger.info"));
        assertTrue(result.contains("username"));
        assertTrue(result.contains("ssn"));
        assertTrue(result.endsWith(");"));
    }

    @Test
    void testAnalyzeFile_WithMultiLineLogStatement(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("MultiLine.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class MultiLine {\n" +
            "    public void method() {\n" +
            "        logger.info(\"User: \" +\n" +
            "            username +\n" +
            "            \" SSN: \" + ssn);\n" +
            "        logger.debug(\"Done\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(2, statements.size());
        // First statement should be the multi-line one starting at line 5
        assertEquals(5, statements.get(0).getLineNumber());
        assertTrue(statements.get(0).getStatement().contains("username"));
        assertTrue(statements.get(0).getStatement().contains("ssn"));
        // Second statement should be at line 8
        assertEquals(8, statements.get(1).getLineNumber());
        assertTrue(statements.get(1).getStatement().contains("Done"));
    }

    @Test
    void testAnalyzeFile_WithMultipleMultiLineStatements(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("MultipleMultiLine.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class MultipleMultiLine {\n" +
            "    public void method() {\n" +
            "        logger.info(\"First: \" +\n" +
            "            value1);\n" +
            "        logger.debug(\"Second: \" +\n" +
            "            value2 +\n" +
            "            \" more\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).getStatement().contains("value1"));
        assertTrue(statements.get(1).getStatement().contains("value2"));
    }

    @Test
    void testAnalyzeFile_WithVeryLongMultiLineStatement(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("VeryLong.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class VeryLong {\n" +
            "    public void method() {\n" +
            "        logger.info(\"Line1: \" +\n" +
            "            var1 +\n" +
            "            \" Line2: \" +\n" +
            "            var2 +\n" +
            "            \" Line3: \" +\n" +
            "            var3);\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(1, statements.size());
        String statement = statements.get(0).getStatement();
        assertTrue(statement.contains("var1"));
        assertTrue(statement.contains("var2"));
        assertTrue(statement.contains("var3"));
    }

    // Tests for StringBuilder/StringBuffer tracking

    @Test
    void testTrackStringBuilders_WithSimpleBuilder() {
        List<String> lines = List.of(
            "StringBuilder sb = new StringBuilder();",
            "sb.append(\"test\");",
            "sb.append(username);"
        );

        java.util.Map<String, List<String>> result = analyzer.trackStringBuilders(lines);

        assertTrue(result.containsKey("sb"));
        assertEquals(2, result.get("sb").size());
        assertTrue(result.get("sb").get(0).contains("test"));
        assertTrue(result.get("sb").get(1).contains("username"));
    }

    @Test
    void testTrackStringBuilders_WithStringBuffer() {
        List<String> lines = List.of(
            "StringBuffer buffer = new StringBuffer();",
            "buffer.append(\"SSN: \");",
            "buffer.append(ssn);"
        );

        java.util.Map<String, List<String>> result = analyzer.trackStringBuilders(lines);

        assertTrue(result.containsKey("buffer"));
        assertEquals(2, result.get("buffer").size());
    }

    @Test
    void testTrackStringBuilders_WithMultipleBuilders() {
        List<String> lines = List.of(
            "StringBuilder sb1 = new StringBuilder();",
            "StringBuilder sb2 = new StringBuilder();",
            "sb1.append(\"first\");",
            "sb2.append(\"second\");"
        );

        java.util.Map<String, List<String>> result = analyzer.trackStringBuilders(lines);

        assertTrue(result.containsKey("sb1"));
        assertTrue(result.containsKey("sb2"));
        assertEquals(1, result.get("sb1").size());
        assertEquals(1, result.get("sb2").size());
    }

    @Test
    void testTrackStringBuilders_WithChainedAppends() {
        List<String> lines = List.of(
            "StringBuilder sb = new StringBuilder();",
            "sb.append(\"User: \").append(username).append(\" SSN: \").append(ssn);"
        );

        java.util.Map<String, List<String>> result = analyzer.trackStringBuilders(lines);

        assertTrue(result.containsKey("sb"));
        // Should capture all append operations
        assertEquals(4, result.get("sb").size());
    }

    @Test
    void testFindRelatedBuilders_WithBuilderUsage() {
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        builderAppends.put("sb", List.of("username", "ssn"));
        builderAppends.put("other", List.of("data"));

        String logStatement = "logger.info(sb.toString());";

        java.util.Map<String, List<String>> result = analyzer.findRelatedBuilders(logStatement, builderAppends);

        assertTrue(result.containsKey("sb"));
        assertFalse(result.containsKey("other"));
        assertEquals(2, result.get("sb").size());
    }

    @Test
    void testFindRelatedBuilders_WithNoBuilderUsage() {
        java.util.Map<String, List<String>> builderAppends = new java.util.HashMap<>();
        builderAppends.put("sb", List.of("username", "ssn"));

        String logStatement = "logger.info(\"simple message\");";

        java.util.Map<String, List<String>> result = analyzer.findRelatedBuilders(logStatement, builderAppends);

        assertTrue(result.isEmpty());
    }

    @Test
    void testAnalyzeFile_WithMultiLineStringBuilder(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("MultiLineBuilder.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class MultiLineBuilder {\n" +
            "    public void method() {\n" +
            "        StringBuilder sb = new StringBuilder();\n" +
            "        sb.append(\"User: \");\n" +
            "        sb.append(username);\n" +
            "        sb.append(\" SSN: \");\n" +
            "        sb.append(ssn);\n" +
            "        logger.info(sb.toString());\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(1, statements.size());
        LogStatement statement = statements.get(0);
        
        // Check that the builder appends are tracked
        java.util.Map<String, List<String>> relatedBuilders = statement.getRelatedBuilderAppends();
        assertTrue(relatedBuilders.containsKey("sb"));
        assertEquals(4, relatedBuilders.get("sb").size());
        
        // Verify the append contents
        List<String> appends = relatedBuilders.get("sb");
        assertTrue(appends.stream().anyMatch(s -> s.contains("User")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("username")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("SSN")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("ssn")));
    }

    @Test
    void testAnalyzeFile_WithStringBufferAndMethodCalls(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("StringBufferMethod.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class StringBufferMethod {\n" +
            "    public void method() {\n" +
            "        StringBuffer buffer = new StringBuffer();\n" +
            "        buffer.append(\"Card: \").append(card.getNumber());\n" +
            "        buffer.append(\" PIN: \").append(pin);\n" +
            "        log.debug(buffer.toString());\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(1, statements.size());
        LogStatement statement = statements.get(0);
        
        java.util.Map<String, List<String>> relatedBuilders = statement.getRelatedBuilderAppends();
        assertTrue(relatedBuilders.containsKey("buffer"));
        
        List<String> appends = relatedBuilders.get("buffer");
        // Should have 4 appends total (2 per line with chained calls)
        assertEquals(4, appends.size());
        assertTrue(appends.stream().anyMatch(s -> s.contains("Card")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("card.getNumber")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("PIN")));
        assertTrue(appends.stream().anyMatch(s -> s.contains("pin")));
    }

    @Test
    void testAnalyzeFile_WithBuilderNotUsedInLog(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("BuilderNotUsed.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class BuilderNotUsed {\n" +
            "    public void method() {\n" +
            "        StringBuilder sb = new StringBuilder();\n" +
            "        sb.append(ssn);\n" +
            "        logger.info(\"Simple message\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        assertEquals(1, statements.size());
        LogStatement statement = statements.get(0);
        
        // Builder should not be associated with this log statement
        java.util.Map<String, List<String>> relatedBuilders = statement.getRelatedBuilderAppends();
        assertFalse(relatedBuilders.containsKey("sb"));
    }

    // Tests for file reading error handling

    @Test
    void testAnalyzeFile_WithNonExistentFile(@TempDir Path tempDir) {
        Path nonExistentFile = tempDir.resolve("DoesNotExist.java");

        // Should throw IOException for non-existent file
        assertThrows(IOException.class, () -> {
            analyzer.analyzeFile(nonExistentFile);
        });
    }

    @Test
    void testAnalyzeFile_WithMalformedContent(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Malformed.java");
        // Create a file with some malformed content but valid log statements
        String content = "package com.example;\n" +
            "\n" +
            "public class Malformed {\n" +
            "    public void method() {\n" +
            "        logger.info(\"This is valid\");\n" +
            "        // Some malformed syntax that won't affect log detection\n" +
            "        int x = ;\n" +
            "        logger.debug(\"Another valid log\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        // Should still be able to extract log statements despite malformed syntax
        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should find the valid log statements
        assertEquals(2, statements.size());
    }

    @Test
    void testAnalyzeFile_WithEmptyFileContent(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Empty.java");
        Files.writeString(javaFile, "");

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should return empty list for empty file
        assertTrue(statements.isEmpty());
    }

    @Test
    void testAnalyzeFile_WithOnlyWhitespace(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Whitespace.java");
        Files.writeString(javaFile, "   \n\n   \n");

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should return empty list for file with only whitespace
        assertTrue(statements.isEmpty());
    }

    @Test
    void testAnalyzeFile_WithSpecialCharacters(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("SpecialChars.java");
        String content = "package com.example;\n" +
            "\n" +
            "public class SpecialChars {\n" +
            "    public void method() {\n" +
            "        logger.info(\"Special chars: \\n\\t\\r\");\n" +
            "        logger.debug(\"Unicode: \\u00A9\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(javaFile, content);

        List<LogStatement> statements = analyzer.analyzeFile(javaFile);

        // Should handle special characters in log statements
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).getStatement().contains("Special chars"));
        assertTrue(statements.get(1).getStatement().contains("Unicode"));
    }
}
