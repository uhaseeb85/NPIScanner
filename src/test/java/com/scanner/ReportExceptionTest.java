package com.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReportException class.
 */
class ReportExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        String message = "Failed to generate report";
        ReportException exception = new ReportException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error writing HTML file";
        Throwable cause = new java.io.IOException("Disk full");
        ReportException exception = new ReportException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testExtendsScannerException() {
        ReportException exception = new ReportException("Test");
        assertTrue(exception instanceof ScannerException);
    }
    
    @Test
    void testExtendsException() {
        ReportException exception = new ReportException("Test");
        assertTrue(exception instanceof Exception);
    }
}
