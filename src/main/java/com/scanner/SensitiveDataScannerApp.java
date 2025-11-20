package com.scanner;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main application entry point for the Sensitive Data Scanner.
 * Parses command-line arguments and orchestrates the scanning workflow.
 */
public class SensitiveDataScannerApp {

    // ========== HARDCODED CONFIGURATION ==========
    // Set USE_HARDCODED_VALUES to true to use hardcoded paths instead of
    // command-line arguments
    private static final boolean USE_HARDCODED_VALUES = true;

    // Hardcoded directory to scan (used when USE_HARDCODED_VALUES = true)
    private static final String HARDCODED_SCAN_DIRECTORY = "./src/main/java";

    // Hardcoded output HTML filename (used when USE_HARDCODED_VALUES = true)
    private static final String HARDCODED_OUTPUT_FILENAME = "scan-report.html";

    // Hardcoded config file path (used when USE_HARDCODED_VALUES = true)
    private static final String HARDCODED_CONFIG_PATH = "./src/main/resources/sample-config.xml";
    // =============================================

    private static final int MIN_ARGS = 2;
    private static final int MAX_ARGS = 3;

    public static void main(String[] args) {
        SensitiveDataScannerApp app = new SensitiveDataScannerApp();

        try {
            String folderPath;
            String configPath;
            String outputPath;

            if (USE_HARDCODED_VALUES) {
                // Use hardcoded values
                System.out.println("Using hardcoded configuration values...");
                folderPath = HARDCODED_SCAN_DIRECTORY;
                configPath = HARDCODED_CONFIG_PATH;
                outputPath = HARDCODED_OUTPUT_FILENAME;
                System.out.println("User SSN: " + outputPath);

                // Validate hardcoded paths
                app.validatePaths(folderPath, configPath, outputPath);
            } else {
                // Use command-line arguments
                app.validateArguments(args);
                folderPath = args[0];
                configPath = args[1];
                outputPath = args.length > 2 ? args[2] : generateDefaultOutputPath();
            }

            app.executeScan(folderPath, configPath, outputPath);

            System.out.println("Scan completed successfully!");
            System.out.println("Report generated at: " + outputPath);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            if (!USE_HARDCODED_VALUES) {
                displayUsage();
            }
            System.exit(1);
        } catch (ConfigurationException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            System.err.println("Please check your configuration file and try again.");
            System.exit(1);
        } catch (ScanException e) {
            System.err.println("Scan Error: " + e.getMessage());
            System.err.println("An error occurred while scanning the files.");
            System.exit(1);
        } catch (ReportException e) {
            System.err.println("Report Generation Error: " + e.getMessage());
            System.err.println("An error occurred while generating the report.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Validates hardcoded paths when USE_HARDCODED_VALUES is true.
     * 
     * @param folderPath Path to the folder to scan
     * @param configPath Path to the configuration file
     * @param outputPath Path for the output report
     * @throws IllegalArgumentException if paths are invalid
     */
    void validatePaths(String folderPath, String configPath, String outputPath) {
        // Validate folder path
        if (folderPath == null || folderPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder path cannot be empty");
        }

        File folder = new File(folderPath);
        if (!folder.exists()) {
            throw new IllegalArgumentException("Folder does not exist: " + folderPath);
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + folderPath);
        }

        if (!folder.canRead()) {
            throw new IllegalArgumentException("Folder is not readable: " + folderPath);
        }

        // Validate config path
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration file path cannot be empty");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Configuration file does not exist: " + configPath);
        }

        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Configuration path is not a file: " + configPath);
        }

        if (!configFile.canRead()) {
            throw new IllegalArgumentException("Configuration file is not readable: " + configPath);
        }

        // Validate output path
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be empty");
        }

        File outputFile = new File(outputPath);
        File outputDir = outputFile.getParentFile();

        if (outputDir != null && !outputDir.exists()) {
            throw new IllegalArgumentException("Output directory does not exist: " + outputDir.getAbsolutePath());
        }
    }

    /**
     * Validates command-line arguments.
     * 
     * @param args Command-line arguments
     * @throws IllegalArgumentException if arguments are invalid
     */
    void validateArguments(String[] args) {
        if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
            throw new IllegalArgumentException(
                    "Invalid number of arguments. Expected 2-3 arguments, got " +
                            (args == null ? 0 : args.length));
        }

        String folderPath = args[0];
        String configPath = args[1];

        // Validate folder path
        if (folderPath == null || folderPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder path cannot be empty");
        }

        File folder = new File(folderPath);
        if (!folder.exists()) {
            throw new IllegalArgumentException("Folder does not exist: " + folderPath);
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + folderPath);
        }

        if (!folder.canRead()) {
            throw new IllegalArgumentException("Folder is not readable: " + folderPath);
        }

        // Validate config path
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration file path cannot be empty");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Configuration file does not exist: " + configPath);
        }

        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Configuration path is not a file: " + configPath);
        }

        if (!configFile.canRead()) {
            throw new IllegalArgumentException("Configuration file is not readable: " + configPath);
        }

        // Validate output path if provided
        if (args.length > 2) {
            String outputPath = args[2];
            if (outputPath == null || outputPath.trim().isEmpty()) {
                throw new IllegalArgumentException("Output path cannot be empty");
            }

            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();

            if (outputDir != null && !outputDir.exists()) {
                throw new IllegalArgumentException("Output directory does not exist: " + outputDir.getAbsolutePath());
            }
        }
    }

    /**
     * Executes the complete scanning workflow.
     * 
     * @param folderPath Path to the folder to scan
     * @param configPath Path to the configuration file
     * @param outputPath Path for the output report
     * @throws ConfigurationException if configuration loading fails
     * @throws ScanException          if scanning fails
     * @throws ReportException        if report generation fails
     */
    void executeScan(String folderPath, String configPath, String outputPath)
            throws ConfigurationException, ScanException, ReportException {

        long startTime = System.currentTimeMillis();

        // Step 1: Load configuration
        System.out.println("Loading configuration from: " + configPath);
        ConfigurationLoader configLoader = new ConfigurationLoader();
        ScanConfiguration config = configLoader.loadConfiguration(configPath);
        System.out.println("Configuration loaded successfully.");

        // Step 2: Scan for Java files
        System.out.println("Scanning directory: " + folderPath);
        JavaFileScanner fileScanner = new JavaFileScanner();
        List<Path> javaFiles;
        try {
            javaFiles = fileScanner.scanDirectory(folderPath);
        } catch (Exception e) {
            throw new ScanException("Failed to scan directory: " + e.getMessage(), e);
        }
        System.out.println("Found " + javaFiles.size() + " Java files.");

        // Step 3: Analyze files and detect sensitive data
        System.out.println("Analyzing files for log statements...");
        LogStatementAnalyzer analyzer = new LogStatementAnalyzer();
        SensitiveDataDetector detector = new SensitiveDataDetector();

        List<Detection> allDetections = new ArrayList<>();
        int filesAnalyzed = 0;

        for (Path javaFile : javaFiles) {
            try {
                List<LogStatement> logStatements = analyzer.analyzeFile(javaFile);

                for (LogStatement logStatement : logStatements) {
                    List<Detection> detections = detector.detectSensitiveData(logStatement, config);
                    allDetections.addAll(detections);
                }

                filesAnalyzed++;
            } catch (Exception e) {
                System.err.println("Warning: Failed to analyze file " + javaFile + ": " + e.getMessage());
                // Continue with other files
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Analysis complete. Found " + allDetections.size() + " detections.");

        // Step 4: Generate statistics
        ScanStatistics stats = new ScanStatistics();
        stats.setTotalFilesScanned(filesAnalyzed);
        stats.setTotalDetections(allDetections.size());
        stats.setScanDurationMs(duration);

        // Step 5: Generate HTML report
        System.out.println("Generating HTML report...");
        HtmlReportGenerator reportGenerator = new HtmlReportGenerator();
        try {
            reportGenerator.generateReport(allDetections, outputPath, stats);
        } catch (Exception e) {
            throw new ReportException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * Displays usage information.
     */
    private static void displayUsage() {
        System.out.println();
        System.out
                .println("Usage: java -jar sensitive-data-scanner.jar <folder-path> <config-file-path> [output-path]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  folder-path      : Path to the folder containing Java source files to scan (required)");
        System.out.println("  config-file-path : Path to the XML configuration file (required)");
        System.out.println("  output-path      : Path for the output HTML report (optional)");
        System.out.println(
                "                     If not provided, a timestamped report will be generated in the current directory");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar sensitive-data-scanner.jar ./src/main/java config.xml report.html");
        System.out.println();
    }

    /**
     * Generates a default output path with timestamp.
     * 
     * @return Default output path
     */
    private static String generateDefaultOutputPath() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String timestamp = dateFormat.format(new Date());
        return "scan-report-" + timestamp + ".html";
    }
}
