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
                "    --bg-tertiary: #e9ecef;\n" +
                "    --text-primary: #212529;\n" +
                "    --text-secondary: #6c757d;\n" +
                "    --accent-primary: #0f172a;\n" +
                "    --accent-secondary: #334155;\n" +
                "    --success: #198754;\n" +
                "    --danger: #dc3545;\n" +
                "    --border-color: #dee2e6;\n" +
                "    --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);\n" +
                "    --shadow-md: 0 4px 6px -1px rgba(0,0,0,0.1);\n" +
                "    --radius-sm: 6px;\n" +
                "    --radius-md: 8px;\n" +
                "    --contrast-bg: #000000;\n" +
                "    --contrast-text: #ffffff;\n" +
                "}\n" +
                "body.dark-theme {\n" +
                "    --bg-primary: #0f172a;\n" +
                "    --bg-secondary: #1e293b;\n" +
                "    --bg-tertiary: #334155;\n" +
                "    --text-primary: #f8fafc;\n" +
                "    --text-secondary: #94a3b8;\n" +
                "    --accent-primary: #f8fafc;\n" +
                "    --accent-secondary: #e2e8f0;\n" +
                "    --border-color: #334155;\n" +
                "    --shadow-sm: 0 1px 2px rgba(0,0,0,0.3);\n" +
                "    --shadow-md: 0 4px 6px -1px rgba(0,0,0,0.5);\n" +
                "    --contrast-bg: #ffffff;\n" +
                "    --contrast-text: #000000;\n" +
                "}\n" +
                "body {\n" +
                "    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                "    background: var(--bg-primary);\n" +
                "    color: var(--text-primary);\n" +
                "    line-height: 1.5;\n" +
                "    transition: background 0.3s ease, color 0.3s ease;\n" +
                "}\n" +
                ".header {\n" +
                "    background: var(--bg-secondary);\n" +
                "    padding: 1.5rem 2rem;\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
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
                "    color: var(--text-primary);\n" +
                "    font-size: 1.5rem;\n" +
                "    font-weight: 600;\n" +
                "    letter-spacing: -0.025em;\n" +
                "}\n" +
                ".theme-toggle {\n" +
                "    background: transparent;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    color: var(--text-primary);\n" +
                "    padding: 0.5rem;\n" +
                "    border-radius: var(--radius-sm);\n" +
                "    cursor: pointer;\n" +
                "    font-size: 1.25rem;\n" +
                "    transition: all 0.2s ease;\n" +
                "    display: flex;\n" +
                "    align-items: center;\n" +
                "    justify-content: center;\n" +
                "}\n" +
                ".theme-toggle:hover {\n" +
                "    background: var(--bg-tertiary);\n" +
                "}\n" +
                ".container {\n" +
                "    max-width: 1400px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 2rem;\n" +
                "}\n" +
                ".summary-grid {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));\n" +
                "    gap: 1.5rem;\n" +
                "    margin-bottom: 2.5rem;\n" +
                "}\n" +
                ".summary-card {\n" +
                "    background: var(--bg-secondary);\n" +
                "    padding: 1.5rem;\n" +
                "    border-radius: var(--radius-md);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    display: flex;\n" +
                "    flex-direction: column;\n" +
                "    gap: 0.5rem;\n" +
                "}\n" +
                ".card-icon {\n" +
                "    font-size: 1.5rem;\n" +
                "    margin-bottom: 0.5rem;\n" +
                "    color: var(--text-secondary);\n" +
                "}\n" +
                ".card-value {\n" +
                "    font-size: 2rem;\n" +
                "    font-weight: 700;\n" +
                "    color: var(--text-primary);\n" +
                "    line-height: 1;\n" +
                "}\n" +
                ".card-label {\n" +
                "    font-size: 0.875rem;\n" +
                "    color: var(--text-secondary);\n" +
                "    font-weight: 500;\n" +
                "}\n" +
                ".filters-section {\n" +
                "    background: var(--bg-secondary);\n" +
                "    padding: 1.5rem;\n" +
                "    border-radius: var(--radius-md);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    margin-bottom: 2rem;\n" +
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
                "    padding: 0.5rem 1rem;\n" +
                "    border-radius: var(--radius-sm);\n" +
                "    font-weight: 500;\n" +
                "    cursor: pointer;\n" +
                "    transition: all 0.2s ease;\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".btn-primary {\n" +
                "    background: var(--accent-primary);\n" +
                "    color: var(--bg-secondary);\n" +
                "    border: 1px solid var(--accent-primary);\n" +
                "}\n" +
                ".btn-primary:hover {\n" +
                "    opacity: 0.9;\n" +
                "}\n" +
                ".btn-secondary {\n" +
                "    background: transparent;\n" +
                "    color: var(--text-primary);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".btn-secondary:hover {\n" +
                "    background: var(--bg-tertiary);\n" +
                "}\n" +
                ".filters-grid {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                "    gap: 1rem;\n" +
                "    margin-bottom: 1rem;\n" +
                "}\n" +
                ".filter-group label {\n" +
                "    display: block;\n" +
                "    margin-bottom: 0.375rem;\n" +
                "    font-weight: 500;\n" +
                "    color: var(--text-secondary);\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".filter-input, .filter-select {\n" +
                "    width: 100%;\n" +
                "    padding: 0.5rem;\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    border-radius: var(--radius-sm);\n" +
                "    background: var(--bg-primary);\n" +
                "    color: var(--text-primary);\n" +
                "    font-size: 0.875rem;\n" +
                "    transition: border-color 0.2s ease;\n" +
                "}\n" +
                ".filter-input:focus, .filter-select:focus {\n" +
                "    outline: none;\n" +
                "    border-color: var(--text-secondary);\n" +
                "}\n" +

                ".table-container {\n" +
                "    background: var(--bg-secondary);\n" +
                "    border-radius: var(--radius-md);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                ".detections-table {\n" +
                "    width: 100%;\n" +
                "    border-collapse: collapse;\n" +
                "    font-size: 0.875rem;\n" +
                "}\n" +
                ".detections-table thead {\n" +
                "    background: var(--contrast-bg);\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
                "}\n" +
                ".detections-table th {\n" +
                "    padding: 0.75rem 1rem;\n" +
                "    text-align: left;\n" +
                "    color: var(--contrast-text);\n" +
                "    font-weight: 600;\n" +
                "    font-size: 0.75rem;\n" +
                "    text-transform: uppercase;\n" +
                "    letter-spacing: 0.05em;\n" +
                "}\n" +
                ".detections-table th.sortable {\n" +
                "    cursor: pointer;\n" +
                "    user-select: none;\n" +
                "}\n" +
                ".detections-table th.sortable:hover {\n" +
                "    color: var(--text-primary);\n" +
                "}\n" +
                ".detections-table td {\n" +
                "    padding: 1rem;\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
                "    color: var(--text-primary);\n" +
                "}\n" +
                ".detections-table tbody tr:last-child td {\n" +
                "    border-bottom: none;\n" +
                "}\n" +
                ".detections-table tbody tr:hover {\n" +
                "    background: var(--bg-tertiary);\n" +
                "}\n" +
                ".detections-table tbody tr.hidden {\n" +
                "    display: none;\n" +
                "}\n" +
                ".file-name {\n" +
                "    font-weight: 600;\n" +
                "    color: var(--text-primary);\n" +
                "}\n" +
                ".line-number {\n" +
                "    text-align: center;\n" +
                "    font-family: 'Courier New', monospace;\n" +
                "    color: var(--text-secondary);\n" +
                "}\n" +
                ".keyword {\n" +
                "    color: var(--danger);\n" +
                "    font-weight: 500;\n" +
                "    padding: 0.125rem 0.375rem;\n" +
                "    background: rgba(220, 53, 69, 0.1);\n" +
                "    border-radius: 4px;\n" +
                "    display: inline-block;\n" +
                "    font-size: 0.75rem;\n" +
                "}\n" +
                ".log-statement {\n" +
                "    font-family: 'Courier New', monospace;\n" +
                "    font-size: 0.8125rem;\n" +
                "    background: var(--bg-tertiary);\n" +
                "    padding: 0.5rem;\n" +
                "    border-radius: 4px;\n" +
                "    max-width: 600px;\n" +
                "    overflow-x: auto;\n" +
                "    color: var(--text-primary);\n" +
                "}\n" +
                ".empty-state {\n" +
                "    text-align: center;\n" +
                "    padding: 4rem 2rem;\n" +
                "    background: var(--bg-secondary);\n" +
                "    border-radius: var(--radius-md);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "}\n" +
                ".empty-icon {\n" +
                "    font-size: 3rem;\n" +
                "    margin-bottom: 1rem;\n" +
                "    color: var(--success);\n" +
                "}\n" +
                ".empty-state h2 {\n" +
                "    color: var(--text-primary);\n" +
                "    margin-bottom: 0.5rem;\n" +
                "    font-size: 1.25rem;\n" +
                "}\n" +
                "@media (max-width: 768px) {\n" +
                "    .header-content { flex-direction: column; gap: 1rem; align-items: flex-start; }\n" +
                "    .summary-grid { grid-template-columns: 1fr; }\n" +
                "    .filters-grid { grid-template-columns: 1fr; }\n" +
                "    .detections-table { font-size: 0.75rem; }\n" +
                "    .detections-table td, .detections-table th { padding: 0.75rem 0.5rem; }\n" +
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
