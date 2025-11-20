# Implementation Plan

- [x] 1. Set up Maven project structure and dependencies





  - Create standard Maven directory structure (src/main/java, src/test/java, src/main/resources, src/test/resources)
  - Create pom.xml with project metadata, Java version (11+), and required dependencies (XML parsing, JUnit 5, Mockito)
  - Add Maven plugins (maven-assembly-plugin or maven-shade-plugin) for building executable JAR with dependencies
  - Set main class in manifest configuration
  - _Requirements: 8.1, 8.2, 8.3, 8.5_

- [x] 2. Implement data model classes




  - [x] 2.1 Create KeywordPattern class


    - Implement KeywordPattern with fields: keyword (String), isRegex (boolean), compiledPattern (Pattern)
    - Implement matches(String text) method that uses plain text or regex matching based on type
    - Write unit tests for plain text matching and regex matching
    - _Requirements: 1.3, 1.4_
  
  - [x] 2.2 Create ScanConfiguration class


    - Implement ScanConfiguration with fields: patterns (List<KeywordPattern>), sensitiveObjectTypes (List<KeywordPattern>)
    - Implement getter methods for patterns and sensitiveObjectTypes
    - Write unit tests for configuration object creation
    - _Requirements: 1.2, 1.7_
  

  - [x] 2.3 Create LogStatement class

    - Implement LogStatement with fields: fileName (String), lineNumber (int), statement (String)
    - Implement getters for all fields
    - Write unit tests for object creation and getters
    - _Requirements: 3.2_
  
  - [x] 2.4 Create Detection class


    - Implement Detection with fields: fileName (String), lineNumber (int), matchedKeyword (String), logStatement (String)
    - Implement getters for all fields
    - Write unit tests for object creation and getters
    - _Requirements: 4.1, 4.2_
  
  - [x] 2.5 Create ScanStatistics class


    - Implement ScanStatistics with fields: totalFilesScanned (int), totalDetections (int), scanDurationMs (long)
    - Implement getters and setters for all fields
    - Write unit tests for statistics tracking
    - _Requirements: 7.7_

- [x] 3. Implement custom exception hierarchy





  - Create ScannerException as base exception class extending Exception
  - Create ConfigurationException extending ScannerException
  - Create ScanException extending ScannerException
  - Create ReportException extending ScannerException
  - Write unit tests for exception creation and inheritance
  - _Requirements: 1.5_

- [x] 4. Implement XML configuration loader




  - [x] 4.1 Create ConfigurationLoader class with XML parsing


    - Implement loadConfiguration(String configFilePath) method using DOM or SAX parser
    - Parse XML structure to extract keyword elements with type attribute
    - Handle file not found errors and throw ConfigurationException
    - Write unit tests with valid XML configuration file
    - _Requirements: 1.1, 1.2, 1.5_
  
  - [x] 4.2 Add keyword pattern extraction and compilation


    - Implement parseKeywords(Document xmlDoc) method to extract keyword elements
    - Create KeywordPattern objects for each keyword based on type attribute
    - Compile regex patterns and catch PatternSyntaxException for invalid regex
    - Write unit tests for plain keywords, regex keywords, and invalid regex handling
    - _Requirements: 1.3, 1.4, 1.6_
  

  - [x] 4.3 Add sensitive object type extraction

    - Implement parseSensitiveObjectTypes(Document xmlDoc) method to extract object-type elements
    - Create KeywordPattern objects for each object type based on type attribute
    - Support both plain text and regex patterns for object types
    - Write unit tests for plain object types, regex object types
    - _Requirements: 1.7, 1.8_
  
  - [x] 4.4 Add XML validation and error handling


    - Validate XML structure and required elements
    - Handle SAXException for malformed XML with clear error messages
    - Write unit tests for invalid XML, missing elements, and malformed structure
    - _Requirements: 1.5_

- [x] 5. Implement file system scanner




  - [x] 5.1 Create JavaFileScanner class


    - Implement scanDirectory(String rootPath) method using Files.walk() for recursive traversal
    - Implement isJavaFile(Path path) method to filter .java files
    - Validate root path exists and is readable
    - Write unit tests with temporary directory structure containing .java and non-.java files
    - _Requirements: 2.1, 2.2, 2.3, 2.5_
  
  - [x] 5.2 Add error handling for file system operations


    - Handle invalid or inaccessible folder paths with clear error messages
    - Catch AccessDeniedException and log warnings while continuing with accessible files
    - Write unit tests for invalid paths, permission denied scenarios, and empty directories
    - _Requirements: 2.5_

- [x] 6. Implement log statement analyzer




  - [x] 6.1 Create LogStatementAnalyzer class with basic log detection


    - Implement analyzeFile(Path filePath) method to read file line by line
    - Implement isLogStatement(String line) method using regex to detect logger patterns (logger., log., LOGGER., LOG.)
    - Detect common logging methods (info, debug, warn, error, trace, fatal)
    - Write unit tests with sample Java files containing various logger patterns
    - _Requirements: 3.1, 3.2, 3.3, 2.4_
  
  - [x] 6.2 Add method signature filtering


    - Implement isMethodSignature(String line) method to detect method declarations and parameter definitions
    - Filter out lines that are method signatures or parameter definitions
    - Write unit tests for method signature detection and filtering
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [x] 6.3 Add multi-line log statement handling


    - Implement extractCompleteStatement(BufferedReader reader, String firstLine, int startLine) method
    - Handle log statements that span multiple lines by reading until statement terminator (semicolon)
    - Track line numbers correctly for multi-line statements
    - Write unit tests for single-line and multi-line log statements
    - _Requirements: 3.5_
  
  - [x] 6.4 Add StringBuilder/StringBuffer tracking across multiple lines


    - Implement trackStringBuilders(List<String> fileLines) method to identify StringBuilder/StringBuffer variable declarations
    - Track all append() operations on each builder variable throughout the file
    - When a logger statement uses a builder variable (e.g., logger.info(sb.toString())), associate all append operations with that log statement
    - Store builder append operations in LogStatement's relatedBuilderAppends map
    - Write unit tests for multi-line StringBuilder patterns with sensitive data
    - _Requirements: 4.4_
  

  - [x] 6.5 Add file reading error handling

    - Catch IOException during file reading and log errors
    - Continue processing other files when one file fails
    - Write unit tests for file read errors and malformed files
    - _Requirements: 3.4_

- [x] 7. Implement sensitive data detection engine




  - [x] 7.1 Create SensitiveDataDetector class with string literal filtering


    - Implement detectSensitiveData(LogStatement logStatement, ScanConfiguration config) method
    - Implement isStringLiteral(String token) method to identify content within double quotes
    - Create logic to exclude string literal regions from variable detection
    - Write unit tests for string literal identification and filtering
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [x] 7.2 Add variable name extraction and matching


    - Implement extractVariables(String statement) method using regex pattern \b([a-zA-Z_][a-zA-Z0-9_]*)\b
    - Filter out variables that are within string literals
    - Implement matchesKeyword(String identifier, KeywordPattern pattern) method
    - Match extracted variable names against configured keywords
    - Write unit tests for variable extraction and keyword matching with plain text and regex patterns
    - _Requirements: 4.1, 4.5, 4.6_
  
  - [x] 7.3 Add method call extraction and matching


    - Implement extractMethodCalls(String statement) method using regex for method call patterns
    - Detect patterns like getSSN(), card.getNumber(), user.getDOB()
    - Match method names against configured keywords
    - Write unit tests for method call detection including chained calls
    - _Requirements: 4.2, 4.5_
  
  - [x] 7.4 Add StringBuilder/StringBuffer pattern detection (single-line)


    - Detect StringBuilder.append() and StringBuffer.append() patterns using regex for single-line usage
    - Extract variables or method calls within append() calls
    - Match extracted identifiers against keywords
    - Write unit tests for single-line StringBuilder and StringBuffer patterns
    - _Requirements: 4.3_
  
  - [x] 7.5 Add StringBuilder/StringBuffer pattern detection (multi-line)


    - Check LogStatement's relatedBuilderAppends map for tracked append operations
    - Analyze all append() calls associated with the logged builder variable
    - Extract variables and method calls from each append operation
    - Match extracted identifiers against keywords and create detections for each match
    - Write unit tests for multi-line StringBuilder patterns where builder is constructed over multiple lines then logged
    - _Requirements: 4.4_
  
  - [x] 7.6 Add String.join and StringJoiner pattern detection


    - Detect String.join() patterns and extract variables being joined
    - Detect StringJoiner.add() patterns and extract variables
    - Match extracted identifiers against keywords
    - Write unit tests for String.join and StringJoiner patterns
    - _Requirements: 4.5_
  
  - [x] 7.7 Add concatenation pattern detection


    - Detect string concatenation patterns using + operator
    - Extract variables from concatenation expressions
    - Handle mixed string literals and variables in concatenation
    - Write unit tests for concatenation patterns with sensitive variables
    - _Requirements: 4.7, 5.4_
  
  - [x] 7.8 Add sensitive object type detection


    - Implement extractObjectReferences(String statement) method to extract all variable and object identifiers
    - Implement isSensitiveObjectType(String identifier, List<KeywordPattern> objectTypes) method
    - Detect direct logging of sensitive objects (e.g., logger.info(request), logger.info(response))
    - Detect method calls on sensitive objects (e.g., logger.info(request.getBody()), logger.info(httpResponse.body()))
    - Match object identifiers against configured sensitive object types
    - Write unit tests for request/response body logging patterns
    - _Requirements: 4.8, 4.9_

- [x] 8. Implement HTML report generator


  - [x] 8.1 Create HtmlReportGenerator class with basic HTML structure


    - Implement generateReport(List<Detection> detections, String outputPath, ScanStatistics stats) method
    - Implement buildHtmlContent(List<Detection> detections, ScanStatistics stats) method
    - Create HTML structure with head, title, and body sections
    - Add CSS styling for table formatting and readability
    - Write unit tests for HTML structure generation
    - _Requirements: 7.1, 7.6_
  

  - [x] 8.2 Add summary section to HTML report
    - Implement generateTimestamp() method for scan date
    - Add summary div with scan date, total files scanned, total detections, and scan duration
    - Format statistics in readable format
    - Write unit tests for summary section generation
    - _Requirements: 7.7_

  
  - [x] 8.3 Add detection table to HTML report
    - Create table with headers: File Name, Line Number, Matched Keyword, Log Statement
    - Populate table rows with detection data
    - Escape special HTML characters in log statements
    - Write unit tests for table generation with multiple detections

    - _Requirements: 7.2, 7.3, 7.4, 7.5_
  
  - [x] 8.4 Add empty detection handling
    - Generate report with message when no detections are found
    - Display "No sensitive data detected" message in report

    - Write unit tests for empty detection list
    - _Requirements: 7.8_
  
  - [x] 8.5 Add file writing and timestamp

    - Save HTML content to file at specified output path
    - Add timestamp to output filename (e.g., scan-report-2025-11-20-143022.html)
    - Handle IOException during file writing with ReportException
    - Write unit tests for file writing and error handling
    - _Requirements: 7.9_

- [x] 9. Implement main application entry point






  - [x] 9.1 Create SensitiveDataScannerApp class with argument parsing

    - Implement main(String[] args) method
    - Implement validateArguments(String[] args) method to check for required arguments
    - Parse command-line arguments: folder path (required), config file path (required), output path (optional)
    - Display usage message if arguments are invalid
    - Write unit tests for argument validation
    - _Requirements: 8.4_
  

  - [x] 9.2 Add workflow orchestration

    - Implement executeScan(String folderPath, String configPath, String outputPath) method
    - Orchestrate the complete workflow: load config → scan files → analyze → detect → generate report
    - Track scan statistics (start time, file count, detection count)
    - Write integration tests for complete workflow
    - _Requirements: 8.1, 8.2, 8.3_
  

  - [x] 9.3 Add top-level error handling

    - Catch and handle ConfigurationException, ScanException, and ReportException
    - Display user-friendly error messages for each exception type
    - Ensure graceful termination on errors
    - Write unit tests for error handling scenarios
    - _Requirements: 1.5, 2.5_

- [x] 10. Create sample configuration file and test data





  - Create sample XML configuration file (sample-config.xml) in src/main/resources with common sensitive keywords and object types
  - Include sensitive object types: request, response, httpResponse, requestBody, responseBody, and regex patterns
  - Create test Java files in src/test/resources with positive and negative test cases
  - Include examples: logger.info with variables, string literals, method calls, StringBuilder, String.join, request/response logging
  - _Requirements: 1.1, 1.3, 1.4, 1.7_

- [x] 11. Write integration tests for end-to-end scenarios





  - Create integration test with sample Java codebase containing known sensitive patterns
  - Run complete scan and verify all expected detections are found
  - Verify HTML report is generated with correct content
  - Test with various configuration files (plain keywords, regex patterns, mixed)
  - Test error scenarios (invalid config, invalid folder, permission issues)
  - _Requirements: All requirements_

- [x] 12. Create project documentation





  - Write README.md with project description, build instructions, usage examples
  - Document XML configuration format with examples
  - Document command-line usage and arguments
  - Add examples of detected patterns and ignored patterns
  - Include sample output HTML report screenshot or description
  - _Requirements: 8.4_
