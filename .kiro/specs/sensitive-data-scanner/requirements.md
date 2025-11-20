# Requirements Document

## Introduction

This document outlines the requirements for a Java-based utility that scans Java codebases to detect sensitive information being logged. The tool will analyze Java source files to identify log statements that may expose sensitive data such as SSN, debit card PINs, dates of birth, and other configurable sensitive keywords. The utility will be built as a Maven project and will generate an HTML report showing all detected instances with file locations, line numbers, matched keywords, and the actual log statements.

## Requirements

### Requirement 1: Configuration Management

**User Story:** As a security auditor, I want to configure sensitive keywords and patterns via an XML file, so that I can customize the scanner for different types of sensitive data without modifying code.

#### Acceptance Criteria

1. WHEN the application starts THEN the system SHALL accept an XML configuration file path as input
2. WHEN the XML configuration file is parsed THEN the system SHALL extract a list of keywords to search for
3. WHEN the XML configuration file is parsed THEN the system SHALL support both plain text keywords and regular expression patterns
4. IF a keyword is marked as a regex pattern THEN the system SHALL compile and use it as a regular expression matcher
5. IF the XML configuration file is missing or invalid THEN the system SHALL display a clear error message and terminate gracefully
6. WHEN multiple keywords are defined THEN the system SHALL check for all of them during scanning
7. WHEN the XML configuration file is parsed THEN the system SHALL extract a list of sensitive object types (e.g., request, response, body)
8. WHEN sensitive object types are configured THEN the system SHALL flag any log statement that logs these object types or calls methods on them

### Requirement 2: Java Source Code Scanning

**User Story:** As a security auditor, I want to scan a folder containing Java source files, so that I can analyze an entire codebase for sensitive data exposure.

#### Acceptance Criteria

1. WHEN the application starts THEN the system SHALL accept a folder location as input
2. WHEN a folder location is provided THEN the system SHALL recursively traverse all subdirectories
3. WHEN traversing directories THEN the system SHALL identify all files with .java extension
4. WHEN a Java file is found THEN the system SHALL read and parse its contents line by line
5. IF the folder location is invalid or inaccessible THEN the system SHALL display an error message and terminate gracefully

### Requirement 3: Log Statement Detection

**User Story:** As a security auditor, I want the scanner to focus only on logging statements, so that I can identify where sensitive data is being written to logs.

#### Acceptance Criteria

1. WHEN analyzing a line of code THEN the system SHALL check if it contains common logging patterns (LOGGER, LOG, logger, log)
2. WHEN a logging statement is identified THEN the system SHALL extract the complete statement including method calls
3. WHEN analyzing logger calls THEN the system SHALL detect patterns like logger.info(), logger.debug(), logger.error(), logger.warn(), log.info(), etc.
4. WHEN analyzing code THEN the system SHALL ignore lines that do not contain logging statements
5. WHEN a multi-line log statement is encountered THEN the system SHALL capture the complete statement across lines

### Requirement 4: Sensitive Data Pattern Matching

**User Story:** As a security auditor, I want the scanner to detect variables and method calls containing sensitive keywords, so that I can identify potential data leaks.

#### Acceptance Criteria

1. WHEN a log statement contains a variable name matching a configured keyword THEN the system SHALL flag it as a detection
2. WHEN a log statement contains a method call with a name matching a configured keyword THEN the system SHALL flag it as a detection (e.g., getSSN(), card.getNumber())
3. WHEN a log statement uses StringBuilder.append() or StringBuffer.append() with a sensitive variable THEN the system SHALL flag it as a detection
4. WHEN a StringBuilder or StringBuffer is built across multiple lines and then logged THEN the system SHALL track the builder variable and detect sensitive data in any append() calls
5. WHEN a log statement uses String.join() or StringJoiner with sensitive variables THEN the system SHALL flag it as a detection
6. WHEN a keyword is a regex pattern THEN the system SHALL apply the regex to variable names and method names in the log statement
7. WHEN analyzing log statements THEN the system SHALL check for concatenation patterns (e.g., "SSN: " + ssn)
8. WHEN a log statement contains a variable or object matching a configured sensitive object type THEN the system SHALL flag it as a detection (e.g., logger.info(request), logger.info(response.getBody()))
9. WHEN a log statement calls toString() or any method on a sensitive object type THEN the system SHALL flag it as a detection (e.g., logger.info(request.toString()), logger.info(httpResponse.body()))

### Requirement 5: String Literal Filtering

**User Story:** As a security auditor, I want the scanner to ignore hardcoded string literals in log statements, so that I only see actual variable data being logged and reduce false positives.

#### Acceptance Criteria

1. WHEN a log statement contains only string literals (e.g., logger.info("123-45-6789")) THEN the system SHALL NOT flag it as a detection
2. WHEN analyzing a log statement THEN the system SHALL distinguish between string literals and variable references
3. WHEN a sensitive keyword appears only within quoted strings THEN the system SHALL ignore it
4. WHEN a log statement mixes string literals and variables THEN the system SHALL only flag the variable portions

### Requirement 6: Method Signature Filtering

**User Story:** As a security auditor, I want the scanner to ignore method parameter definitions and method signatures, so that I only see actual logging of sensitive data and not method declarations.

#### Acceptance Criteria

1. WHEN analyzing a line containing a method parameter definition (e.g., void log(String ssn)) THEN the system SHALL NOT flag it as a detection
2. WHEN analyzing a line containing a method signature THEN the system SHALL NOT flag it as a detection
3. WHEN a line contains both a method signature and a log statement THEN the system SHALL only analyze the log statement portion

### Requirement 7: HTML Report Generation

**User Story:** As a security auditor, I want to receive an HTML report of all findings, so that I can easily review and share the results with my team.

#### Acceptance Criteria

1. WHEN the scan is complete THEN the system SHALL generate an HTML report file
2. WHEN generating the report THEN the system SHALL include the filename for each detection
3. WHEN generating the report THEN the system SHALL include the line number for each detection
4. WHEN generating the report THEN the system SHALL include the matched keyword or pattern for each detection
5. WHEN generating the report THEN the system SHALL include the actual log statement for each detection
6. WHEN generating the report THEN the system SHALL format the output in a readable table structure
7. WHEN generating the report THEN the system SHALL include a summary section showing total files scanned and total detections found
8. WHEN no detections are found THEN the system SHALL generate a report indicating that no sensitive data was detected
9. WHEN the report is generated THEN the system SHALL save it to a configurable output location with a timestamp in the filename

### Requirement 8: Maven Project Structure

**User Story:** As a developer, I want the utility to be a standard Maven project, so that I can easily build, test, and distribute it.

#### Acceptance Criteria

1. WHEN the project is created THEN the system SHALL follow standard Maven directory structure (src/main/java, src/test/java, src/main/resources)
2. WHEN the project is built THEN the system SHALL have a valid pom.xml with all necessary dependencies
3. WHEN running mvn package THEN the system SHALL produce an executable JAR file
4. WHEN the JAR is executed THEN the system SHALL accept command-line arguments for folder location and config file path
5. WHEN the project includes dependencies THEN the system SHALL use appropriate libraries for XML parsing and HTML generation
