package com.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScanException class.
 */
class ScanExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        String message = "Failed to scan directory";
        ScanException exception = new ScanException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error reading file";
        Throwable cause = new java.io.IOException("Permission denied");
        ScanException exception = new ScanException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testExtendsScannerException() {
        ScanException exception = new ScanException("Test");
        assertTrue(exception instanceof ScannerException);
    }
    
    @Test
    void testExtendsException() {
        ScanException exception = new ScanException("Test");
        assertTrue(exception instanceof Exception);
    }
}
