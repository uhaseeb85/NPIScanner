# Sensitive Data Scanner

A Java-based command-line utility that scans Java codebases to detect sensitive information being logged. The tool analyzes Java source files to identify log statements that may expose sensitive data such as SSN, credit card numbers, passwords, and other configurable sensitive keywords.

## Features

- Configurable keyword detection via XML (supports both plain text and regex patterns)
- Recursive scanning of Java source directories
- Detection of multiple logging patterns:
  - Logger-based logging (logger.info, log.debug, etc.)
  - System.out.println() and System.err.println() statements
  - Variable names containing sensitive keywords
  - Method calls with sensitive names (e.g., `getSSN()`, `card.getNumber()`)
  - StringBuilder/StringBuffer patterns (single-line and multi-line)
  - String concatenation with sensitive data
  - String.join() and StringJoiner patterns
  - Direct logging of sensitive object types (request, response, etc.)
- Smart filtering to reduce false positives:
  - Ignores hardcoded string literals
  - Filters out method signatures and parameter definitions
- HTML report generation with detailed findings
- Summary statistics (files scanned, detections found, scan duration)

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

Clone the repository and build using Maven:

```bash
mvn clean package
```

This will create an executable JAR file at `target/sensitive-data-scanner.jar`.

## Running Tests

Run all unit and integration tests:

```bash
mvn test
```

## Usage

### Option 1: Hardcoded Configuration (No Command-Line Arguments)

For convenience during development or repeated scans, you can hardcode the scan directory and output filename directly in the source code.

Edit `src/main/java/com/scanner/SensitiveDataScannerApp.java` and modify these constants:

```java
// Set to true to use hardcoded values
private static final boolean USE_HARDCODED_VALUES = true;

// Hardcoded directory to scan
private static final String HARDCODED_SCAN_DIRECTORY = "./src/main/java";

// Hardcoded output HTML filename
private static final String HARDCODED_OUTPUT_FILENAME = "scan-report.html";

// Hardcoded config file path
private static final String HARDCODED_CONFIG_PATH = "./src/main/resources/sample-config.xml";
```

Then rebuild and run without arguments:

```bash
mvn clean package
java -jar target/sensitive-data-scanner.jar
```

### Option 2: Command-Line Arguments

```bash
java -jar target/sensitive-data-scanner.jar <folder-path> <config-file-path> [output-path]
```

### Arguments

- `<folder-path>` (required): Path to the root directory containing Java source files to scan
- `<config-file-path>` (required): Path to the XML configuration file defining sensitive keywords
- `[output-path]` (optional): Directory where the HTML report will be saved (defaults to current directory)

### Examples

Scan a project with default output location:

```bash
java -jar target/sensitive-data-scanner.jar ./src/main/java ./config/scan-config.xml
```

Scan with custom output directory:

```bash
java -jar target/sensitive-data-scanner.jar ./src/main/java ./config/scan-config.xml ./reports
```

Using the sample configuration:

```bash
java -jar target/sensitive-data-scanner.jar ./src/main/java ./src/main/resources/sample-config.xml
```

## Configuration

### XML Configuration Format

The scanner uses an XML configuration file to define sensitive keywords and object types to detect.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scan-configuration>
    <keywords>
        <!-- Plain text keywords (case-sensitive matching) -->
        <keyword type="plain">ssn</keyword>
        <keyword type="plain">socialSecurity</keyword>
        <keyword type="plain">password</keyword>
        <keyword type="plain">creditCard</keyword>
        <keyword type="plain">pin</keyword>
        
        <!-- Regex patterns for flexible matching -->
        <keyword type="regex">.*[Pp]assword.*</keyword>
        <keyword type="regex">credit[Cc]ard.*</keyword>
        <keyword type="regex">.*SSN.*</keyword>
    </keywords>
    
    <sensitive-object-types>
        <!-- Object types that should not be logged directly -->
        <object-type type="plain">request</object-type>
        <object-type type="plain">response</object-type>
        <object-type type="plain">httpResponse</object-type>
        <object-type type="plain">requestBody</object-type>
        
        <!-- Regex patterns for object types -->
        <object-type type="regex">.*Request</object-type>
        <object-type type="regex">.*Response</object-type>
    </sensitive-object-types>
</scan-configuration>
```

### Configuration Elements

#### Keywords

Keywords define sensitive data identifiers to search for in log statements.

- `type="plain"`: Exact case-sensitive matching of variable/method names
- `type="regex"`: Regular expression pattern matching

#### Sensitive Object Types

Object types define classes or objects that should not be logged directly (e.g., HTTP requests/responses that may contain sensitive data).

- `type="plain"`: Exact case-sensitive matching of object names
- `type="regex"`: Regular expression pattern matching

## Detection Patterns

### Patterns That Will Be Detected

The scanner identifies the following patterns in log statements:

#### 1. Variable Names
```java
String ssn = "123-45-6789";
logger.info("User SSN: " + ssn);  // DETECTED: variable 'ssn'
```

#### 2. Method Calls
```java
logger.info("SSN: " + user.getSSN());  // DETECTED: method 'getSSN'
logger.error("Card: " + card.getNumber());  // DETECTED if 'Number' matches pattern
```

#### 3. String Concatenation
```java
logger.debug("Password: " + userPassword);  // DETECTED: variable 'userPassword'
```

#### 4. StringBuilder/StringBuffer (Single-line)
```java
logger.info(new StringBuilder().append("PIN: ").append(pin));  // DETECTED: variable 'pin'
```

#### 5. StringBuilder/StringBuffer (Multi-line)
```java
StringBuilder sb = new StringBuilder();
sb.append("User: ");
sb.append(username);
sb.append(" SSN: ");
sb.append(ssn);  // DETECTED: variable 'ssn'
logger.info(sb.toString());
```

#### 6. String.join() and StringJoiner
```java
logger.info(String.join(", ", email, phone));  // DETECTED: variables 'email', 'phone'
```

#### 7. Sensitive Object Logging
```java
logger.info("Request: " + request);  // DETECTED: object 'request'
logger.debug(response.getBody());  // DETECTED: object 'response'
logger.error("Response: " + httpResponse.body());  // DETECTED: object 'httpResponse'
```

#### 8. System.out and System.err Statements
```java
System.out.println("SSN: " + ssn);  // DETECTED: variable 'ssn'
System.err.println("Error with password: " + password);  // DETECTED: variable 'password'
System.out.print("User: " + username);  // DETECTED: variable 'username'
System.err.printf("PIN: %s", pin);  // DETECTED: variable 'pin'
```

### Patterns That Will Be Ignored

The scanner intelligently filters out false positives:

#### 1. String Literals
```java
logger.info("123-45-6789");  // IGNORED: hardcoded string literal
logger.info("SSN");  // IGNORED: just the word "SSN" in quotes
System.out.println("password");  // IGNORED: just the word in quotes
```

#### 2. Method Signatures
```java
public void logUser(String ssn) {  // IGNORED: method parameter definition
    // ...
}

private String getSSN() {  // IGNORED: method declaration
    return this.ssn;
}
```

#### 3. Non-logging Statements
```java
String ssn = user.getSSN();  // IGNORED: not a log statement
int result = calculateValue(password);  // IGNORED: not logging
```

## Output Report

### HTML Report Structure

The scanner generates an HTML report with the following sections:

1. Summary Section
   - Scan date and time
   - Total files scanned
   - Total detections found
   - Scan duration

2. Detections Table
   - File name
   - Line number
   - Matched keyword or pattern
   - Complete log statement

### Sample Report Output

```
Sensitive Data Scan Report
==========================

Summary
-------
Scan Date: 2025-11-20 12:34:56
Total Files Scanned: 45
Total Detections: 12
Scan Duration: 1.23 seconds

Detections
----------
File Name                    | Line | Matched Keyword | Log Statement
----------------------------|------|-----------------|----------------------------------
UserService.java            | 42   | ssn             | logger.info("SSN: " + ssn)
PaymentProcessor.java       | 156  | creditCard      | log.debug("Card: " + card.getNumber())
AuthController.java         | 89   | password        | logger.error("Failed: " + userPassword)
RequestHandler.java         | 234  | request         | logger.info("Request: " + request)
```

The report is saved with a timestamp in the filename (e.g., `scan-report-2025-11-20-123456.html`).

## Project Structure

```
sensitive-data-scanner/
├── src/
│   ├── main/
│   │   ├── java/com/scanner/
│   │   │   ├── SensitiveDataScannerApp.java      # Main entry point
│   │   │   ├── ConfigurationLoader.java          # XML config parser
│   │   │   ├── JavaFileScanner.java              # File system scanner
│   │   │   ├── LogStatementAnalyzer.java         # Log statement detector
│   │   │   ├── SensitiveDataDetector.java        # Pattern matching engine
│   │   │   ├── HtmlReportGenerator.java          # Report generator
│   │   │   ├── ScanConfiguration.java            # Config data model
│   │   │   ├── KeywordPattern.java               # Keyword data model
│   │   │   ├── LogStatement.java                 # Log statement data model
│   │   │   ├── Detection.java                    # Detection data model
│   │   │   ├── ScanStatistics.java               # Statistics data model
│   │   │   └── exceptions/
│   │   │       ├── ScannerException.java
│   │   │       ├── ConfigurationException.java
│   │   │       ├── ScanException.java
│   │   │       └── ReportException.java
│   │   └── resources/
│   │       └── sample-config.xml                 # Sample configuration
│   └── test/
│       ├── java/com/scanner/                     # Unit tests
│       └── resources/                            # Test data files
├── pom.xml
└── README.md
```

## Error Handling

The scanner handles various error scenarios gracefully:

- Invalid or missing configuration file
- Invalid regex patterns in configuration
- Inaccessible directories or files
- Malformed Java files
- Permission denied errors
- Report generation failures

All errors are reported with clear messages to help diagnose issues.

## Best Practices

1. Start with the sample configuration and customize it for your needs
2. Test your regex patterns carefully to avoid false positives
3. Run the scanner regularly as part of your code review process
4. Review the HTML report thoroughly and address all findings
5. Keep your configuration file under version control
6. Consider integrating the scanner into your CI/CD pipeline

## Limitations

- Only scans Java source files (.java extension)
- Requires valid Java syntax (malformed files may be skipped)
- Case-sensitive matching for plain text keywords
- Does not analyze compiled bytecode or JAR files
- Does not perform data flow analysis across methods

## Contributing

Contributions are welcome! Please ensure all tests pass before submitting pull requests.

## License

This project is provided as-is for security auditing purposes.

## Support

For issues or questions, please refer to the project documentation or contact your security team.
