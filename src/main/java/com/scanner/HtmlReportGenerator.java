package com.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates interactive HTML reports for sensitive data scan results.
 * Creates a modern, self-contained HTML file with filtering, search, sorting,
 * and visualization.
 */
public class HtmlReportGenerator {

    /**
     * Generates an HTML report from the scan results and saves it to the specified
     * output path.
     *
     * @param detections the list of detections found during the scan
     * @param outputPath the path where the HTML report should be saved
     * @param stats      the scan statistics
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

            Files.write(outputFilePath, htmlContent.getBytes("UTF-8"));

        } catch (IOException e) {
            throw new ReportException("Failed to write HTML report: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the HTML content for the report.
     *
     * @param detections the list of detections to include in the report
     * @param stats      the scan statistics
     * @return the complete HTML content as a string
     */
    String buildHtmlContent(List<Detection> detections, ScanStatistics stats) {
        StringBuilder html = new StringBuilder();

        // Calculate unique files
        long uniqueFiles = detections.stream()
                .map(Detection::getFileName)
                .distinct()
                .count();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Sensitive Data Scan Report</title>\n");
        html.append(
                "    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap\" rel=\"stylesheet\">\n");
        html.append("    <style>\n");
        html.append(getCssStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Header with theme toggle
        html.append("    <header class=\"header\">\n");
        html.append("        <div class=\"header-content\">\n");
        html.append("            <h1>🔍 Sensitive Data Scan Report</h1>\n");
        html.append("            <button id=\"themeToggle\" class=\"theme-toggle\">🌙</button>\n");
        html.append("        </div>\n");
        html.append("    </header>\n");

        // Main container
        html.append("    <div class=\"container\">\n");

        // Summary cards
        html.append("        <div class=\"summary-grid\">\n");
        html.append("            <div class=\"summary-card\">\n");
        html.append("                <div class=\"card-icon\">📁</div>\n");
        html.append("                <div class=\"card-content\">\n");
        html.append("                    <div class=\"card-value\">").append(stats.getTotalFilesScanned())
                .append("</div>\n");
        html.append("                    <div class=\"card-label\">Files Scanned</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-card\">\n");
        html.append("                <div class=\"card-icon\">⚠️</div>\n");
        html.append("                <div class=\"card-content\">\n");
        html.append("                    <div class=\"card-value\">").append(stats.getTotalDetections())
                .append("</div>\n");
        html.append("                    <div class=\"card-label\">Total Detections</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-card\">\n");
        html.append("                <div class=\"card-icon\">🎯</div>\n");
        html.append("                <div class=\"card-content\">\n");
        html.append("                    <div class=\"card-value\">").append(uniqueFiles).append("</div>\n");
        html.append("                    <div class=\"card-label\">Affected Files</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"summary-card\">\n");
        html.append("                <div class=\"card-icon\">⏱️</div>\n");
        html.append("                <div class=\"card-content\">\n");
        html.append("                    <div class=\"card-value\">").append(formatDuration(stats.getScanDurationMs()))
                .append("</div>\n");
        html.append("                    <div class=\"card-label\">Scan Duration</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");

        // Detection table or empty message
        if (detections.isEmpty()) {
            html.append("        <div class=\"empty-state\">\n");
            html.append("            <div class=\"empty-icon\">✅</div>\n");
            html.append("            <h2>No Sensitive Data Detected</h2>\n");
            html.append(
                    "            <p>Great news! The scan completed successfully with no sensitive data findings.</p>\n");
            html.append("        </div>\n");
        } else {
            // Filters section
            html.append("        <div class=\"filters-section\">\n");
            html.append("            <div class=\"filters-header\">\n");
            html.append("                <h2>🔎 Filter & Search</h2>\n");
            html.append("                <div class=\"filter-actions\">\n");
            html.append(
                    "                    <button id=\"resetFilters\" class=\"btn-secondary\">Reset Filters</button>\n");
            html.append("                    <button id=\"exportCsv\" class=\"btn-primary\">Export to CSV</button>\n");
            html.append("                </div>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"filters-grid\">\n");
            html.append("                <div class=\"filter-group\">\n");
            html.append("                    <label for=\"searchBox\">Search</label>\n");
            html.append(
                    "                    <input type=\"text\" id=\"searchBox\" placeholder=\"Search across all columns...\" class=\"filter-input\">\n");
            html.append("                </div>\n");
            html.append("                <div class=\"filter-group\">\n");
            html.append("                    <label for=\"fileFilter\">File Name</label>\n");
            html.append("                    <select id=\"fileFilter\" class=\"filter-select\">\n");
            html.append("                        <option value=\"\">All Files</option>\n");

            // Add file filter options
            detections.stream().map(Detection::getFileName).distinct().sorted().forEach(file -> {
                html.append("                        <option value=\"").append(escapeHtml(file)).append("\">")
                        .append(escapeHtml(file)).append("</option>\n");
            });

            html.append("                    </select>\n");
            html.append("                </div>\n");
            html.append("                <div class=\"filter-group\">\n");
            html.append("                    <label for=\"keywordFilter\">Keyword</label>\n");
            html.append("                    <select id=\"keywordFilter\" class=\"filter-select\">\n");
            html.append("                        <option value=\"\">All Keywords</option>\n");

            // Add keyword filter options
            detections.stream().map(Detection::getMatchedKeyword).distinct().sorted().forEach(keyword -> {
                html.append("                        <option value=\"").append(escapeHtml(keyword)).append("\">")
                        .append(escapeHtml(keyword)).append("</option>\n");
            });

            html.append("                    </select>\n");
            html.append("                </div>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"results-info\">\n");
            html.append("                <span id=\"resultsCount\">Showing ").append(detections.size())
                    .append(" of ").append(detections.size()).append(" detections</span>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");

            // Detection table
            html.append(buildDetectionTable(detections));
        }

        html.append("    </div>\n");

        // JavaScript for interactivity
        if (!detections.isEmpty()) {
            html.append("    <script>\n");
            html.append("        // Theme management\n");
            html.append("        const themeToggle = document.getElementById('themeToggle');\n");
            html.append("        const savedTheme = localStorage.getItem('theme') || 'light';\n");
            html.append("        if (savedTheme === 'dark') {\n");
            html.append("            document.body.classList.add('dark-theme');\n");
            html.append("            themeToggle.textContent = '☀️';\n");
            html.append("        }\n");
            html.append("        themeToggle.addEventListener('click', () => {\n");
            html.append("            document.body.classList.toggle('dark-theme');\n");
            html.append("            const isDark = document.body.classList.contains('dark-theme');\n");
            html.append("            themeToggle.textContent = isDark ? '☀️' : '🌙';\n");
            html.append("            localStorage.setItem('theme', isDark ? 'dark' : 'light');\n");
            html.append("        });\n");
            html.append("        \n");
            html.append("        // Filtering and search\n");
            html.append("        const searchBox = document.getElementById('searchBox');\n");
            html.append("        const fileFilter = document.getElementById('fileFilter');\n");
            html.append("        const keywordFilter = document.getElementById('keywordFilter');\n");
            html.append("        const resetFilters = document.getElementById('resetFilters');\n");
            html.append("        const exportCsv = document.getElementById('exportCsv');\n");
            html.append("        const tableBody = document.getElementById('tableBody');\n");
            html.append("        const resultsCount = document.getElementById('resultsCount');\n");
            html.append("        \n");
            html.append("        searchBox.addEventListener('input', applyFilters);\n");
            html.append("        fileFilter.addEventListener('change', applyFilters);\n");
            html.append("        keywordFilter.addEventListener('change', applyFilters);\n");
            html.append("        \n");
            html.append("        resetFilters.addEventListener('click', () => {\n");
            html.append("            searchBox.value = '';\n");
            html.append("            fileFilter.value = '';\n");
            html.append("            keywordFilter.value = '';\n");
            html.append("            applyFilters();\n");
            html.append("        });\n");
            html.append("        \n");
            html.append("        function applyFilters() {\n");
            html.append("            const searchTerm = searchBox.value.toLowerCase();\n");
            html.append("            const selectedFile = fileFilter.value;\n");
            html.append("            const selectedKeyword = keywordFilter.value;\n");
            html.append("            const rows = tableBody.querySelectorAll('tr');\n");
            html.append("            let visibleCount = 0;\n");
            html.append("            \n");
            html.append("            rows.forEach(row => {\n");
            html.append("                const fileName = row.dataset.file || '';\n");
            html.append("                const keyword = row.dataset.keyword || '';\n");
            html.append("                const rowText = row.textContent.toLowerCase();\n");
            html.append("                \n");
            html.append("                const matchesSearch = !searchTerm || rowText.includes(searchTerm);\n");
            html.append("                const matchesFile = !selectedFile || fileName === selectedFile;\n");
            html.append("                const matchesKeyword = !selectedKeyword || keyword === selectedKeyword;\n");
            html.append("                \n");
            html.append("                if (matchesSearch && matchesFile && matchesKeyword) {\n");
            html.append("                    row.classList.remove('hidden');\n");
            html.append("                    visibleCount++;\n");
            html.append("                } else {\n");
            html.append("                    row.classList.add('hidden');\n");
            html.append("                }\n");
            html.append("            });\n");
            html.append("            \n");
            html.append(
                    "            resultsCount.textContent = `Showing ${visibleCount} of ${rows.length} detections`;\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        // Table sorting\n");
            html.append("        const sortableHeaders = document.querySelectorAll('.sortable');\n");
            html.append("        let currentSort = { column: null, ascending: true };\n");
            html.append("        \n");
            html.append("        sortableHeaders.forEach(header => {\n");
            html.append("            header.addEventListener('click', () => {\n");
            html.append("                const column = header.dataset.column;\n");
            html.append(
                    "                const ascending = currentSort.column === column ? !currentSort.ascending : true;\n");
            html.append("                sortTable(column, ascending);\n");
            html.append("                currentSort = { column, ascending };\n");
            html.append("                sortableHeaders.forEach(h => h.classList.remove('sorted'));\n");
            html.append("                header.classList.add('sorted');\n");
            html.append("                header.querySelector('.sort-icon').textContent = ascending ? '↑' : '↓';\n");
            html.append("            });\n");
            html.append("        });\n");
            html.append("        \n");
            html.append("        function sortTable(column, ascending) {\n");
            html.append("            const rows = Array.from(tableBody.querySelectorAll('tr'));\n");
            html.append("            rows.sort((a, b) => {\n");
            html.append("                let aVal, bVal;\n");
            html.append("                if (column === 'fileName') {\n");
            html.append("                    aVal = a.dataset.file;\n");
            html.append("                    bVal = b.dataset.file;\n");
            html.append("                } else if (column === 'lineNumber') {\n");
            html.append("                    aVal = parseInt(a.dataset.line);\n");
            html.append("                    bVal = parseInt(b.dataset.line);\n");
            html.append("                } else if (column === 'keyword') {\n");
            html.append("                    aVal = a.dataset.keyword;\n");
            html.append("                    bVal = b.dataset.keyword;\n");
            html.append("                }\n");
            html.append("                if (typeof aVal === 'number') {\n");
            html.append("                    return ascending ? aVal - bVal : bVal - aVal;\n");
            html.append("                } else {\n");
            html.append(
                    "                    return ascending ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);\n");
            html.append("                }\n");
            html.append("            });\n");
            html.append("            rows.forEach(row => tableBody.appendChild(row));\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        // CSV Export\n");
            html.append("        exportCsv.addEventListener('click', () => {\n");
            html.append("            const rows = Array.from(tableBody.querySelectorAll('tr:not(.hidden)'));\n");
            html.append("            let csv = 'File Name,Line Number,Keyword,Log Statement\\n';\n");
            html.append("            rows.forEach(row => {\n");
            html.append("                const cells = row.querySelectorAll('td');\n");
            html.append("                const rowData = Array.from(cells).map(cell => {\n");
            html.append("                    const text = cell.textContent.trim();\n");
            html.append("                    return `\"${text.replace(/\"/g, '\"\"')}\"`;\n");
            html.append("                });\n");
            html.append("                csv += rowData.join(',') + '\\n';\n");
            html.append("            });\n");
            html.append("            const blob = new Blob([csv], { type: 'text/csv' });\n");
            html.append("            const url = URL.createObjectURL(blob);\n");
            html.append("            const a = document.createElement('a');\n");
            html.append("            a.href = url;\n");
            html.append("            a.download = 'scan-report-' + new Date().toISOString().slice(0,10) + '.csv';\n");
            html.append("            a.click();\n");
            html.append("            URL.revokeObjectURL(url);\n");
            html.append("        });\n");
            html.append("    </script>\n");
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
        return "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                ":root {\n" +
                "    --bg-primary: #f8f9fa;\n" +
                "    --bg-secondary: #ffffff;\n" +
                "    --bg-tertiary: #f1f3f5;\n" +
                "    --text-primary: #212529;\n" +
                "    --text-secondary: #6c757d;\n" +
                "    --accent-primary: #667eea;\n" +
                "    --accent-secondary: #764ba2;\n" +
                "    --success: #51cf66;\n" +
                "    --danger: #ff6b6b;\n" +
                "    --border-color: #dee2e6;\n" +
                "    --shadow-md: 0 4px 12px rgba(0,0,0,0.08);\n" +
                "    --radius-md: 12px;\n" +
                "}\n" +
                "body.dark-theme {\n" +
                "    --bg-primary: #1a1b1e;\n" +
                "    --bg-secondary: #25262b;\n" +
                "    --bg-tertiary: #2c2e33;\n" +
                "    --text-primary: #e9ecef;\n" +
                "    --text-secondary: #adb5bd;\n" +
                "    --border-color: #373a40;\n" +
                "    --shadow-md: 0 4px 12px rgba(0,0,0,0.4);\n" +
                "}\n" +
                "body {\n" +
                "    font-family: 'Inter', sans-serif;\n" +
                "    background: var(--bg-primary);\n" +
                "    color: var(--text-primary);\n" +
                "    line-height: 1.6;\n" +
                "    transition: background 0.3s ease;\n" +
                "}\n" +
                ".header {\n" +
                "    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));\n" +
                "    padding: 2rem;\n" +
                "    box-shadow: var(--shadow-md);\n" +
                "    position: sticky;\n" +
                "    top: 0;\n" +
                "    z-index: 100;\n" +
                "}\n" +
                ".header-content {\n" +
                "    max-width: 1400px;\n" +
                "    margin: 0 auto;\n" +
                "    display: flex;\n" +
                "    justify-content: space-between;\n" +
                "    align-items: center;\n" +
                "}\n" +
                ".header h1 {\n" +
                "    color: white;\n" +
                "    font-size: 1.75rem;\n" +
                "    font-weight: 700;\n" +
                "}\n" +
                ".theme-toggle {\n" +
                "    background: rgba(255,255,255,0.2);\n" +
                "    border: 2px solid rgba(255,255,255,0.3);\n" +
                "    color: white;\n" +
                "    padding: 0.5rem 1rem;\n" +
                "    border-radius: 8px;\n" +
                "    cursor: pointer;\n" +
                "    font-size: 1.25rem;\n" +
                "    transition: all 0.3s ease;\n" +
                "}\n" +
                ".theme-toggle:hover {\n" +
                "    background: rgba(255,255,255,0.3);\n" +
                "    transform: scale(1.05);\n" +
                "}\n" +
                ".container {\n" +
                "    max-width: 1400px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 2rem;\n" +
                "}\n" +
                ".summary-grid {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
                "    gap: 1.5rem;\n" +
                "    margin-bottom: 2rem;\n" +
                "}\n" +
                ".summary-card {\n" +
                "    background: var(--bg-secondary);\n" +
                "    padding: 1.5rem;\n" +
                "    border-radius: var(--radius-md);\n" +
                "    box-shadow: var(--shadow-md);\n" +
                "    display: flex;\n" +
                "    align-items: center;\n" +
                "    gap: 1rem;\n" +
                "    transition: transform 0.2s ease;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".summary-card:hover {\n" +
                "    transform: translateY(-4px);\n" +
                "}\n" +
                ".card-icon {\n" +
                "    font-size: 2.5rem;\n" +
                "}\n" +
                ".card-value {\n" +
                "    font-size: 2rem;\n" +
                "    font-weight: 700;\n" +
                "    color: var(--accent-primary);\n" +
                "}\n" +
                ".card-label {\n" +
                "    font-size: 0.875rem;\n" +
                "    color: var(--text-secondary);\n" +
                "    text-transform: uppercase;\n" +
                "}\n" +
                ".filters-section {\n" +
                "    background: var(--bg-secondary);\n" +
                "    padding: 1.5rem;\n" +
                "    border-radius: var(--radius-md);\n" +
                "    box-shadow: var(--shadow-md);\n" +
                "    margin-bottom: 2rem;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".filters-header {\n" +
                "    display: flex;\n" +
                "    justify-content: space-between;\n" +
                "    align-items: center;\n" +
                "    margin-bottom: 1.5rem;\n" +
                "    flex-wrap: wrap;\n" +
                "    gap: 1rem;\n" +
                "}\n" +
                ".filter-actions {\n" +
                "    display: flex;\n" +
                "    gap: 0.75rem;\n" +
                "}\n" +
                ".btn-primary, .btn-secondary {\n" +
                "    padding: 0.625rem 1.25rem;\n" +
                "    border-radius: 8px;\n" +
                "    font-weight: 500;\n" +
                "    cursor: pointer;\n" +
                "    transition: all 0.2s ease;\n" +
                "    border: none;\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".btn-primary {\n" +
                "    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));\n" +
                "    color: white;\n" +
                "}\n" +
                ".btn-primary:hover {\n" +
                "    transform: translateY(-2px);\n" +
                "}\n" +
                ".btn-secondary {\n" +
                "    background: var(--bg-tertiary);\n" +
                "    color: var(--text-primary);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".filters-grid {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
                "    gap: 1rem;\n" +
                "    margin-bottom: 1rem;\n" +
                "}\n" +
                ".filter-group label {\n" +
                "    display: block;\n" +
                "    margin-bottom: 0.5rem;\n" +
                "    font-weight: 500;\n" +
                "    color: var(--text-secondary);\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".filter-input, .filter-select {\n" +
                "    width: 100%;\n" +
                "    padding: 0.625rem;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    border-radius: 8px;\n" +
                "    background: var(--bg-primary);\n" +
                "    color: var(--text-primary);\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".filter-input:focus, .filter-select:focus {\n" +
                "    outline: none;\n" +
                "    border-color: var(--accent-primary);\n" +
                "}\n" +
                ".results-info {\n" +
                "    padding: 0.75rem;\n" +
                "    background: var(--bg-tertiary);\n" +
                "    border-radius: 8px;\n" +
                "    text-align: center;\n" +
                "    font-weight: 500;\n" +
                "    color: var(--text-secondary);\n" +
                "}\n" +
                ".table-container {\n" +
                "    background: var(--bg-secondary);\n" +
                "    border-radius: var(--radius-md);\n" +
                "    box-shadow: var(--shadow-md);\n" +
                "    overflow: hidden;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".detections-table {\n" +
                "    width: 100%;\n" +
                "    border-collapse: collapse;\n" +
                "}\n" +
                ".detections-table thead {\n" +
                "    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));\n" +
                "}\n" +
                ".detections-table th {\n" +
                "    padding: 1rem;\n" +
                "    text-align: left;\n" +
                "    color: white;\n" +
                "    font-weight: 600;\n" +
                "    font-size: 0.875rem;\n" +
                "    text-transform: uppercase;\n" +
                "}\n" +
                ".detections-table th.sortable {\n" +
                "    cursor: pointer;\n" +
                "    user-select: none;\n" +
                "}\n" +
                ".detections-table th.sortable:hover {\n" +
                "    background: rgba(255,255,255,0.1);\n" +
                "}\n" +
                ".detections-table td {\n" +
                "    padding: 1rem;\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
                "}\n" +
                ".detections-table tbody tr {\n" +
                "    transition: background 0.2s ease;\n" +
                "}\n" +
                ".detections-table tbody tr:hover {\n" +
                "    background: var(--bg-tertiary);\n" +
                "}\n" +
                ".detections-table tbody tr.hidden {\n" +
                "    display: none;\n" +
                "}\n" +
                ".file-name {\n" +
                "    font-weight: 500;\n" +
                "    color: var(--accent-primary);\n" +
                "}\n" +
                ".line-number {\n" +
                "    text-align: center;\n" +
                "    font-weight: 600;\n" +
                "    color: var(--text-secondary);\n" +
                "    font-family: 'Courier New', monospace;\n" +
                "}\n" +
                ".keyword {\n" +
                "    color: var(--danger);\n" +
                "    font-weight: 600;\n" +
                "    padding: 0.25rem 0.5rem;\n" +
                "    background: rgba(255, 107, 107, 0.1);\n" +
                "    border-radius: 4px;\n" +
                "    display: inline-block;\n" +
                "}\n" +
                ".log-statement {\n" +
                "    font-family: 'Courier New', monospace;\n" +
                "    font-size: 0.8125rem;\n" +
                "    background: var(--bg-tertiary);\n" +
                "    padding: 0.5rem;\n" +
                "    border-radius: 4px;\n" +
                "    max-width: 600px;\n" +
                "    overflow-x: auto;\n" +
                "}\n" +
                ".empty-state {\n" +
                "    text-align: center;\n" +
                "    padding: 4rem 2rem;\n" +
                "    background: var(--bg-secondary);\n" +
                "    border-radius: var(--radius-md);\n" +
                "    box-shadow: var(--shadow-md);\n" +
                "}\n" +
                ".empty-icon {\n" +
                "    font-size: 4rem;\n" +
                "    margin-bottom: 1rem;\n" +
                "}\n" +
                ".empty-state h2 {\n" +
                "    color: var(--success);\n" +
                "    margin-bottom: 0.5rem;\n" +
                "}\n" +
                "@media (max-width: 768px) {\n" +
                "    .header-content { flex-direction: column; gap: 1rem; }\n" +
                "    .summary-grid { grid-template-columns: 1fr; }\n" +
                "    .filters-grid { grid-template-columns: 1fr; }\n" +
                "    .detections-table { font-size: 0.75rem; }\n" +
                "    .detections-table td, .detections-table th { padding: 0.5rem; }\n" +
                "}\n";
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

        table.append("        <div class=\"table-container\">\n");
        table.append("            <table id=\"detectionsTable\" class=\"detections-table\">\n");
        table.append("                <thead>\n");
        table.append("                    <tr>\n");
        table.append(
                "                        <th class=\"sortable\" data-column=\"fileName\">File Name <span class=\"sort-icon\">⇅</span></th>\n");
        table.append(
                "                        <th class=\"sortable\" data-column=\"lineNumber\">Line <span class=\"sort-icon\">⇅</span></th>\n");
        table.append(
                "                        <th class=\"sortable\" data-column=\"keyword\">Keyword <span class=\"sort-icon\">⇅</span></th>\n");
        table.append("                        <th>Log Statement</th>\n");
        table.append("                    </tr>\n");
        table.append("                </thead>\n");
        table.append("                <tbody id=\"tableBody\">\n");

        for (Detection detection : detections) {
            table.append("                    <tr data-file=\"").append(escapeHtml(detection.getFileName()))
                    .append("\" data-line=\"").append(detection.getLineNumber())
                    .append("\" data-keyword=\"").append(escapeHtml(detection.getMatchedKeyword()))
                    .append("\">\n");
            table.append("                        <td class=\"file-name\">").append(escapeHtml(detection.getFileName()))
                    .append("</td>\n");
            table.append("                        <td class=\"line-number\">").append(detection.getLineNumber())
                    .append("</td>\n");
            table.append("                        <td class=\"keyword\">")
                    .append(escapeHtml(detection.getMatchedKeyword())).append("</td>\n");
            table.append("                        <td class=\"log-statement\">")
                    .append(escapeHtml(detection.getLogStatement())).append("</td>\n");
            table.append("                    </tr>\n");
        }

        table.append("                </tbody>\n");
        table.append("            </table>\n");
        table.append("        </div>\n");

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
