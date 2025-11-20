package com.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates HTML reports for sensitive data scan results.
 * Creates formatted HTML output with summary statistics and detection details.
 */
public class HtmlReportGenerator {

    /**
     * Generates an HTML report from the scan results and saves it to the specified output path.
     *
     * @param detections the list of detections found during the scan
     * @param outputPath the path where the HTML report should be saved
     * @param stats the scan statistics
     * @throws ReportException if there is an error generating or writing the report
     */
    public void generateReport(List<Detection> detections, String outputPath, ScanStatistics stats) 
            throws ReportException {
        try {
            String htmlContent = buildHtmlContent(detections, stats);
            
            Path outputFilePath;
            if (outputPath != null && !outputPath.isEmpty()) {
                outputFilePath = Paths.get(outputPath);
                
                // Create parent directories if they don't exist
                Path parentDir = outputFilePath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }
            } else {
                // Generate default filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
                String fileName = "scan-report-" + timestamp + ".html";
                outputFilePath = Paths.get(fileName);
            }
            
            Files.write(outputFilePath, htmlContent.getBytes());
            
        } catch (IOException e) {
            throw new ReportException("Failed to write HTML report: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the HTML content for the report.
     *
     * @param detections the list of detections to include in the report
     * @param stats the scan statistics
     * @return the complete HTML content as a string
     */
    String buildHtmlContent(List<Detection> detections, ScanStatistics stats) {
        StringBuilder html = new StringBuilder();
        
        // HTML header
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Sensitive Data Scan Report</title>\n");
        html.append("    <style>\n");
        html.append(getCssStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>Sensitive Data Scan Report</h1>\n");
        
        // Summary section
        html.append(buildSummarySection(stats));
        
        // Detection table or empty message
        if (detections.isEmpty()) {
            html.append("    <div class=\"no-detections\">\n");
            html.append("        <p>No sensitive data detected</p>\n");
            html.append("    </div>\n");
        } else {
            html.append(buildDetectionTable(detections));
        }
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }

    /**
     * Returns the CSS styles for the HTML report.
     *
     * @return CSS styles as a string
     */
    private String getCssStyles() {
        StringBuilder css = new StringBuilder();
        css.append("body {\n");
        css.append("    font-family: Arial, sans-serif;\n");
        css.append("    margin: 20px;\n");
        css.append("    background-color: #f5f5f5;\n");
        css.append("}\n");
        css.append("h1 {\n");
        css.append("    color: #333;\n");
        css.append("    border-bottom: 2px solid #4CAF50;\n");
        css.append("    padding-bottom: 10px;\n");
        css.append("}\n");
        css.append(".summary {\n");
        css.append("    background-color: #fff;\n");
        css.append("    padding: 15px;\n");
        css.append("    margin: 20px 0;\n");
        css.append("    border-radius: 5px;\n");
        css.append("    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
        css.append("}\n");
        css.append(".summary p {\n");
        css.append("    margin: 8px 0;\n");
        css.append("    font-size: 14px;\n");
        css.append("}\n");
        css.append(".summary strong {\n");
        css.append("    color: #555;\n");
        css.append("}\n");
        css.append("table {\n");
        css.append("    width: 100%;\n");
        css.append("    border-collapse: collapse;\n");
        css.append("    background-color: #fff;\n");
        css.append("    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
        css.append("    margin-top: 20px;\n");
        css.append("}\n");
        css.append("th {\n");
        css.append("    background-color: #4CAF50;\n");
        css.append("    color: white;\n");
        css.append("    padding: 12px;\n");
        css.append("    text-align: left;\n");
        css.append("    font-weight: bold;\n");
        css.append("}\n");
        css.append("td {\n");
        css.append("    padding: 10px 12px;\n");
        css.append("    border-bottom: 1px solid #ddd;\n");
        css.append("}\n");
        css.append("tr:hover {\n");
        css.append("    background-color: #f5f5f5;\n");
        css.append("}\n");
        css.append("tr:last-child td {\n");
        css.append("    border-bottom: none;\n");
        css.append("}\n");
        css.append(".line-number {\n");
        css.append("    text-align: center;\n");
        css.append("    font-weight: bold;\n");
        css.append("    color: #666;\n");
        css.append("}\n");
        css.append(".keyword {\n");
        css.append("    color: #d32f2f;\n");
        css.append("    font-weight: bold;\n");
        css.append("}\n");
        css.append(".log-statement {\n");
        css.append("    font-family: 'Courier New', monospace;\n");
        css.append("    font-size: 13px;\n");
        css.append("    background-color: #f9f9f9;\n");
        css.append("    padding: 5px;\n");
        css.append("    border-radius: 3px;\n");
        css.append("}\n");
        css.append(".no-detections {\n");
        css.append("    background-color: #e8f5e9;\n");
        css.append("    color: #2e7d32;\n");
        css.append("    padding: 20px;\n");
        css.append("    margin: 20px 0;\n");
        css.append("    border-radius: 5px;\n");
        css.append("    text-align: center;\n");
        css.append("    font-size: 16px;\n");
        css.append("    font-weight: bold;\n");
        css.append("}\n");
        return css.toString();
    }

    /**
     * Builds the summary section of the HTML report.
     *
     * @param stats the scan statistics
     * @return HTML string for the summary section
     */
    private String buildSummarySection(ScanStatistics stats) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("    <div class=\"summary\">\n");
        summary.append("        <p><strong>Scan Date:</strong> ").append(generateTimestamp()).append("</p>\n");
        summary.append("        <p><strong>Total Files Scanned:</strong> ")
               .append(stats.getTotalFilesScanned()).append("</p>\n");
        summary.append("        <p><strong>Total Detections:</strong> ")
               .append(stats.getTotalDetections()).append("</p>\n");
        summary.append("        <p><strong>Scan Duration:</strong> ")
               .append(formatDuration(stats.getScanDurationMs())).append("</p>\n");
        summary.append("    </div>\n");
        
        return summary.toString();
    }

    /**
     * Generates a formatted timestamp for the current date and time.
     *
     * @return formatted timestamp string
     */
    String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Formats the scan duration in a readable format.
     *
     * @param durationMs duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + " ms";
        } else if (durationMs < 60000) {
            return String.format("%.2f seconds", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return minutes + " min " + seconds + " sec";
        }
    }

    /**
     * Builds the detection table section of the HTML report.
     *
     * @param detections the list of detections
     * @return HTML string for the detection table
     */
    private String buildDetectionTable(List<Detection> detections) {
        StringBuilder table = new StringBuilder();
        
        table.append("    <table>\n");
        table.append("        <thead>\n");
        table.append("            <tr>\n");
        table.append("                <th>File Name</th>\n");
        table.append("                <th>Line Number</th>\n");
        table.append("                <th>Matched Keyword</th>\n");
        table.append("                <th>Log Statement</th>\n");
        table.append("            </tr>\n");
        table.append("        </thead>\n");
        table.append("        <tbody>\n");
        
        for (Detection detection : detections) {
            table.append("            <tr>\n");
            table.append("                <td>").append(escapeHtml(detection.getFileName())).append("</td>\n");
            table.append("                <td class=\"line-number\">").append(detection.getLineNumber()).append("</td>\n");
            table.append("                <td class=\"keyword\">").append(escapeHtml(detection.getMatchedKeyword())).append("</td>\n");
            table.append("                <td class=\"log-statement\">").append(escapeHtml(detection.getLogStatement())).append("</td>\n");
            table.append("            </tr>\n");
        }
        
        table.append("        </tbody>\n");
        table.append("    </table>\n");
        
        return table.toString();
    }

    /**
     * Escapes special HTML characters to prevent HTML injection and display issues.
     *
     * @param text the text to escape
     * @return escaped HTML string
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
