package com.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScannerException class.
 */
class ScannerExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        String message = "Test scanner exception";
        ScannerException exception = new ScannerException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test scanner exception";
        Throwable cause = new RuntimeException("Root cause");
        ScannerException exception = new ScannerException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testExtendsException() {
        ScannerException exception = new ScannerException("Test");
        assertTrue(exception instanceof Exception);
    }
}
