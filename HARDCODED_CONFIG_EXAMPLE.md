# Using Hardcoded Configuration

## Quick Start Guide

To use hardcoded values instead of command-line arguments:

### Step 1: Edit the Configuration Constants

Open `src/main/java/com/scanner/SensitiveDataScannerApp.java` and modify these lines at the top of the class:

```java
// ========== HARDCODED CONFIGURATION ==========
// Set USE_HARDCODED_VALUES to true to use hardcoded paths instead of command-line arguments
private static final boolean USE_HARDCODED_VALUES = true;  // Change to true

// Hardcoded directory to scan (used when USE_HARDCODED_VALUES = true)
private static final String HARDCODED_SCAN_DIRECTORY = "./src/main/java";  // Your directory

// Hardcoded output HTML filename (used when USE_HARDCODED_VALUES = true)
private static final String HARDCODED_OUTPUT_FILENAME = "my-scan-report.html";  // Your filename

// Hardcoded config file path (used when USE_HARDCODED_VALUES = true)
private static final String HARDCODED_CONFIG_PATH = "./src/main/resources/sample-config.xml";
// =============================================
```

### Step 2: Rebuild the Project

```bash
mvn clean package
```

### Step 3: Run Without Arguments

```bash
java -jar target/sensitive-data-scanner.jar
```

That's it! The scanner will use your hardcoded values.

## Example Configurations

### Scan Current Project
```java
private static final boolean USE_HARDCODED_VALUES = true;
private static final String HARDCODED_SCAN_DIRECTORY = "./src/main/java";
private static final String HARDCODED_OUTPUT_FILENAME = "scan-report.html";
private static final String HARDCODED_CONFIG_PATH = "./src/main/resources/sample-config.xml";
```

### Scan Another Project
```java
private static final boolean USE_HARDCODED_VALUES = true;
private static final String HARDCODED_SCAN_DIRECTORY = "C:/projects/my-app/src";
private static final String HARDCODED_OUTPUT_FILENAME = "C:/reports/my-app-scan.html";
private static final String HARDCODED_CONFIG_PATH = "./config/custom-config.xml";
```

### Scan with Specific Output Location
```java
private static final boolean USE_HARDCODED_VALUES = true;
private static final String HARDCODED_SCAN_DIRECTORY = "./src";
private static final String HARDCODED_OUTPUT_FILENAME = "./reports/scan-2025-11-20.html";
private static final String HARDCODED_CONFIG_PATH = "./scan-config.xml";
```

## Switching Back to Command-Line Mode

To use command-line arguments again, just set:

```java
private static final boolean USE_HARDCODED_VALUES = false;
```

Then rebuild and run with arguments:

```bash
mvn clean package
java -jar target/sensitive-data-scanner.jar ./src/main/java ./config.xml ./output.html
```

## Benefits of Hardcoded Configuration

- No need to type long paths every time
- Easier for repeated scans during development
- Can be committed to version control for team consistency
- Faster iteration when testing configuration changes
- Ideal for IDE run configurations
