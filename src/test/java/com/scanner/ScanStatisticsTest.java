package com.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScanStatisticsTest {

    @Test
    void testDefaultConstructor() {
        ScanStatistics stats = new ScanStatistics();

        assertEquals(0, stats.getTotalFilesScanned());
        assertEquals(0, stats.getTotalDetections());
        assertEquals(0, stats.getScanDurationMs());
    }

    @Test
    void testParameterizedConstructor() {
        ScanStatistics stats = new ScanStatistics(10, 5, 1500L);

        assertEquals(10, stats.getTotalFilesScanned());
        assertEquals(5, stats.getTotalDetections());
        assertEquals(1500L, stats.getScanDurationMs());
    }

    @Test
    void testSettersAndGetters() {
        ScanStatistics stats = new ScanStatistics();

        stats.setTotalFilesScanned(25);
        stats.setTotalDetections(12);
        stats.setScanDurationMs(3000L);

        assertEquals(25, stats.getTotalFilesScanned());
        assertEquals(12, stats.getTotalDetections());
        assertEquals(3000L, stats.getScanDurationMs());
    }

    @Test
    void testIncrementFilesScanned() {
        ScanStatistics stats = new ScanStatistics();

        stats.incrementFilesScanned();
        assertEquals(1, stats.getTotalFilesScanned());

        stats.incrementFilesScanned();
        assertEquals(2, stats.getTotalFilesScanned());

        stats.incrementFilesScanned();
        assertEquals(3, stats.getTotalFilesScanned());
    }

    @Test
    void testIncrementDetections() {
        ScanStatistics stats = new ScanStatistics();

        stats.incrementDetections(3);
        assertEquals(3, stats.getTotalDetections());

        stats.incrementDetections(5);
        assertEquals(8, stats.getTotalDetections());

        stats.incrementDetections(1);
        assertEquals(9, stats.getTotalDetections());
    }

    @Test
    void testIncrementDetections_WithZero() {
        ScanStatistics stats = new ScanStatistics();

        stats.incrementDetections(0);
        assertEquals(0, stats.getTotalDetections());
    }

    @Test
    void testStatisticsTracking_RealisticScenario() {
        ScanStatistics stats = new ScanStatistics();
        long startTime = System.currentTimeMillis();

        // Simulate scanning 5 files
        for (int i = 0; i < 5; i++) {
            stats.incrementFilesScanned();
            // Simulate finding detections in some files
            if (i % 2 == 0) {
                stats.incrementDetections(2);
            }
        }

        long endTime = System.currentTimeMillis();
        stats.setScanDurationMs(endTime - startTime);

        assertEquals(5, stats.getTotalFilesScanned());
        assertEquals(6, stats.getTotalDetections()); // 3 files with 2 detections each
        assertTrue(stats.getScanDurationMs() >= 0);
    }

    @Test
    void testStatisticsUpdate_OverwritePreviousValues() {
        ScanStatistics stats = new ScanStatistics(10, 5, 1000L);

        stats.setTotalFilesScanned(20);
        stats.setTotalDetections(15);
        stats.setScanDurationMs(2000L);

        assertEquals(20, stats.getTotalFilesScanned());
        assertEquals(15, stats.getTotalDetections());
        assertEquals(2000L, stats.getScanDurationMs());
    }

    @Test
    void testIncrementFromNonZeroBase() {
        ScanStatistics stats = new ScanStatistics(5, 10, 500L);

        stats.incrementFilesScanned();
        stats.incrementDetections(3);

        assertEquals(6, stats.getTotalFilesScanned());
        assertEquals(13, stats.getTotalDetections());
        assertEquals(500L, stats.getScanDurationMs()); // Duration unchanged
    }
}
