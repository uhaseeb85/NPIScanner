# Sensitive Data Scanner

A powerful, configurable Java command-line utility designed to detect sensitive information leaks in log statements. It performs a deep scan of your Java codebase to identify potential exposure of PII (Personally Identifiable Information), credentials, and other sensitive data before it reaches production logs.

## 🚀 Quick Start

Get up and running in minutes!

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd NPIScanner
   ```

2. **Build the Project**
   ```bash
   mvn clean package
   ```

3. **Run the Scanner**
   
   **Option A: Using Hardcoded Configuration (Default)**
   ```bash
   java -jar target/sensitive-data-scanner-1.0.0.jar
   ```
   
   **Option B: Using Command-Line Arguments**
   ```bash
   java -jar target/sensitive-data-scanner-1.0.0.jar ./src/main/resources/config
   ```
   
   **Option C: Specify Custom Output Path**
   ```bash
   java -jar target/sensitive-data-scanner-1.0.0.jar ./src/main/resources/config ./custom-output/report.html
   ```

4. **View the Report**
   Open the generated report in your browser:
   - Default location: `output/scan-report-{timestamp}.html`
   - Custom location: As specified in command-line arguments

---

## ✨ Key Features

- **Deep Code Analysis**: Goes beyond simple grep. It understands Java syntax to detect:
  - `logger.info()`, `debug()`, `error()`, etc.
  - `System.out.println()` and `System.err.println()`
  - String concatenation (`"SSN: " + ssn`)
  - `StringBuilder` and `StringBuffer` chains
  - `String.join()` and `StringJoiner` usage
  
- **Smart Detection Engine**:
  - **Variable Analysis**: Flags variables with sensitive names (e.g., `String password`)
  - **Method Call Analysis**: Flags calls to sensitive methods (e.g., `user.getSSN()`)
  - **Object Logging**: Detects when entire sensitive objects are logged (e.g., `logger.info(request)`)
  
- **Intelligent Filtering**:
  - **Exclusion Filters**: Ignore test files, mocks, or specific paths
  - **False Positive Reduction**: Automatically ignores hardcoded string literals and method signatures
  
- **Rich Reporting**: Generates a sleek, interactive HTML report with:
  - Dark/Light mode support
  - Sortable and filterable tables
  - Export to CSV functionality
  - Responsive design

---

## 🛠️ Configuration

The scanner uses a **directory-based configuration** approach with separate XML files for different configuration aspects.

### Configuration Directory Structure

```
config/
├── scan-directories.xml    # Directories to scan
├── keywords.xml            # Sensitive keywords to detect
├── object-types.xml        # Sensitive object types
└── exclusions.xml          # Patterns to exclude
```

### 1. Scan Directories (`scan-directories.xml`)

Defines which directories to scan for Java files:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scan-directories>
    <directory>./src/main/java</directory>
    <directory>./another/source/path</directory>
</scan-directories>
```

### 2. Keywords (`keywords.xml`)

Defines sensitive keywords to detect in log statements:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<keywords>
    <!-- Exact matches for variable names -->
    <keyword type="plain">ssn</keyword>
    <keyword type="plain">password</keyword>
    <keyword type="plain">apiKey</keyword>
    <keyword type="plain">creditCard</keyword>
    <keyword type="plain">debitCard</keyword>
    <keyword type="plain">pin</keyword>
    <keyword type="plain">dateOfBirth</keyword>
    <keyword type="plain">dob</keyword>
    
    <!-- Regex patterns for broader matching -->
    <keyword type="regex">.*password.*</keyword>
    <keyword type="regex">credit[Cc]ard.*</keyword>
</keywords>
```

### 3. Sensitive Object Types (`object-types.xml`)

Defines object types that should never be logged directly:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<sensitive-object-types>
    <!-- Plain text object types -->
    <object-type type="plain">request</object-type>
    <object-type type="plain">response</object-type>
    <object-type type="plain">httpResponse</object-type>
    <object-type type="plain">apiResponse</object-type>
    
    <!-- Regex patterns for object types -->
    <object-type type="regex">.*Request</object-type>
    <object-type type="regex">.*Response</object-type>
</sensitive-object-types>
```

### 4. Exclusions (`exclusions.xml`)

Defines patterns to exclude from scan results:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<exclusions>
    <!-- Ignore test data and mock files -->
    <exclusion type="plain">test-data</exclusion>
    <exclusion type="plain">Mock</exclusion>
    <exclusion type="plain">Stub</exclusion>
    
    <!-- Ignore all files in test directories -->
    <exclusion type="regex">.*src/test/.*</exclusion>
    <exclusion type="regex">.*Test\.java</exclusion>
</exclusions>
```

### Hardcoded Configuration Mode

For convenience, you can enable hardcoded configuration in `SensitiveDataScannerApp.java`:

```java
private static final boolean USE_HARDCODED_VALUES = true;
private static final String HARDCODED_CONFIG_PATH = "./src/main/resources/config";
```

When enabled, the scanner will automatically use the specified configuration directory without requiring command-line arguments.

---

## 🔍 Detection Patterns

The scanner is designed to catch a wide variety of logging patterns. Here are some examples of what it detects:

### 1. Variable & Method Logging
```java
String ssn = "123-45-6789";
logger.info("User SSN: " + ssn);           // DETECTED: variable 'ssn'
logger.debug("Password: " + getPassword()); // DETECTED: method 'getPassword'
```

### 2. String Concatenation
```java
// Detects sensitive data mixed with strings
System.out.println("Card Number: " + creditCardNumber); // DETECTED
```

### 3. StringBuilder Chains
```java
// Detects sensitive data appended to builders
StringBuilder sb = new StringBuilder();
sb.append("User: ").append(username);
sb.append(" PIN: ").append(userPin);       // DETECTED: variable 'userPin'
logger.info(sb.toString());
```

### 4. Object Dumping
```java
// Detects when sensitive objects are logged directly
public void process(HttpServletRequest request) {
    logger.info("Incoming request: " + request); // DETECTED: object 'request'
}
```

### 5. String.join()
```java
// Detects sensitive data in join operations
String logMsg = String.join(", ", userId, authToken); // DETECTED: variable 'authToken'
```

---

## 🛡️ False Positive Reduction

To keep your report clean, the scanner intelligently ignores:

- **String Literals**: `logger.info("password")` (Safe: just the word "password")
- **Method Signatures**: `public void setPassword(String password)` (Safe: definition, not usage)
- **Non-Log Statements**: `String p = password;` (Safe: assignment, not logging)

---

## 📖 Usage Guide

### Command Line Arguments

| Argument | Description | Required |
|----------|-------------|----------|
| `config-directory-path` | Path to the directory containing XML configuration files | No (if hardcoded mode enabled) |
| `output-path` | Path for the output HTML report | No (defaults to `output/scan-report-{timestamp}.html`) |

### Running the Scanner

```bash
# Using hardcoded configuration (if enabled)
java -jar target/sensitive-data-scanner-1.0.0.jar

# Specify configuration directory
java -jar target/sensitive-data-scanner-1.0.0.jar ./src/main/resources/config

# Specify both configuration directory and output path
java -jar target/sensitive-data-scanner-1.0.0.jar ./src/main/resources/config ./reports/my-scan.html
```

### Running from IDE (IntelliJ / Eclipse)

1. Open the project in your IDE
2. Locate the main class: `com.scanner.SensitiveDataScannerApp`
3. Create a Run Configuration:
   - **Main Class**: `com.scanner.SensitiveDataScannerApp`
   - **Program Arguments**: (optional) `./src/main/resources/config`
   - **Working Directory**: Project root folder
4. Run the configuration

### Output

The scanner generates:
- **HTML Report**: Interactive report with all detections
  - Default location: `output/scan-report-{timestamp}.html`
  - Automatically creates the `output/` directory if it doesn't exist
- **Console Output**: Summary of scan results and statistics

---

## 📊 Report Features

The generated HTML report includes:

- **Summary Dashboard**: Quick overview of scan results
  - Total files scanned
  - Total detections found
  - Affected files count
  - Scan duration
  
- **Interactive Table**: Detailed detection list with:
  - File name and line number
  - Matched keyword
  - Full log statement context
  - Sortable columns
  - Real-time search and filtering
  
- **Export Options**: Export filtered results to CSV
- **Theme Toggle**: Switch between light and dark modes
- **Responsive Design**: Works on desktop and mobile devices

---

## 🏗️ Project Structure

```
NPIScanner/
├── src/
│   ├── main/
│   │   ├── java/com/scanner/
│   │   │   ├── SensitiveDataScannerApp.java    # Main entry point
│   │   │   ├── ConfigurationLoader.java        # Loads XML configurations
│   │   │   ├── JavaFileScanner.java            # Scans for Java files
│   │   │   ├── LogStatementAnalyzer.java       # Analyzes log statements
│   │   │   ├── SensitiveDataDetector.java      # Detects sensitive data
│   │   │   ├── HtmlReportGenerator.java        # Generates HTML reports
│   │   │   └── ...                             # Other classes
│   │   └── resources/
│   │       └── config/                          # Configuration files
│   │           ├── scan-directories.xml
│   │           ├── keywords.xml
│   │           ├── object-types.xml
│   │           └── exclusions.xml
│   └── test/
│       └── java/com/scanner/                    # Unit tests
├── output/                                      # Generated reports (auto-created)
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

---

## 📋 Requirements

- **Java**: 8 or higher (tested with Java 17)
- **Maven**: 3.6 or higher
- **Operating System**: Windows, macOS, or Linux

---

## 🧪 Testing

Run the test suite:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ConfigurationLoaderTest

# Run with coverage
mvn clean test jacoco:report
```

---

## 🤝 Contributing

Contributions are welcome! Please ensure:

1. All tests pass: `mvn clean test`
2. Code follows existing style conventions
3. New features include appropriate tests
4. Documentation is updated

---

## 📄 License

This project is provided as-is for security auditing purposes.

---

## 🔧 Troubleshooting

### Common Issues

**Issue**: "Configuration directory not found"
- **Solution**: Ensure the configuration directory path is correct and contains all required XML files

**Issue**: "No Java files found"
- **Solution**: Check that the scan directories in `scan-directories.xml` point to valid Java source directories

**Issue**: "Report not generated"
- **Solution**: Ensure the output directory is writable. The scanner will automatically create the `output/` directory if it doesn't exist

**Issue**: Tests failing with "Configuration path is not a directory"
- **Solution**: Tests expect directory-based configuration. Ensure test setup creates proper directory structures

---

## 📚 Additional Resources

- [Configuration Examples](./src/main/resources/config/) - Sample configuration files
- [Test Cases](./src/test/java/com/scanner/) - Example usage and test scenarios
- [Interactive Report Template](./interactive-report-template.html) - HTML report template

---

## 🎯 Best Practices

1. **Regular Scans**: Integrate into your CI/CD pipeline to catch issues early
2. **Custom Keywords**: Tailor keywords to your domain-specific sensitive data
3. **Review Reports**: Regularly review and act on findings
4. **Update Exclusions**: Keep exclusion patterns up-to-date to reduce false positives
5. **Version Control**: Track configuration changes in version control
