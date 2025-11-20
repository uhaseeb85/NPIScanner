package com.scanner;

/**
 * Contains statistics about a scan operation.
 * Tracks the number of files scanned, detections found, and scan duration.
 */
public class ScanStatistics {
    private int totalFilesScanned;
    private int totalDetections;
    private long scanDurationMs;

    /**
     * Creates a ScanStatistics with default values (all zeros).
     */
    public ScanStatistics() {
        this.totalFilesScanned = 0;
        this.totalDetections = 0;
        this.scanDurationMs = 0;
    }

    /**
     * Creates a ScanStatistics with the specified values.
     *
     * @param totalFilesScanned the total number of files scanned
     * @param totalDetections the total number of detections found
     * @param scanDurationMs the scan duration in milliseconds
     */
    public ScanStatistics(int totalFilesScanned, int totalDetections, long scanDurationMs) {
        this.totalFilesScanned = totalFilesScanned;
        this.totalDetections = totalDetections;
        this.scanDurationMs = scanDurationMs;
    }

    public int getTotalFilesScanned() {
        return totalFilesScanned;
    }

    public void setTotalFilesScanned(int totalFilesScanned) {
        this.totalFilesScanned = totalFilesScanned;
    }

    public int getTotalDetections() {
        return totalDetections;
    }

    public void setTotalDetections(int totalDetections) {
        this.totalDetections = totalDetections;
    }

    public long getScanDurationMs() {
        return scanDurationMs;
    }

    public void setScanDurationMs(long scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    /**
     * Increments the total files scanned count by 1.
     */
    public void incrementFilesScanned() {
        this.totalFilesScanned++;
    }

    /**
     * Increments the total detections count by the specified amount.
     *
     * @param count the number of detections to add
     */
    public void incrementDetections(int count) {
        this.totalDetections += count;
    }
}
