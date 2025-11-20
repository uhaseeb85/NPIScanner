package com.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationException class.
 */
class ConfigurationExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        String message = "Invalid configuration file";
        ConfigurationException exception = new ConfigurationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Failed to parse XML";
        Throwable cause = new java.io.IOException("File not found");
        ConfigurationException exception = new ConfigurationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testExtendsScannerException() {
        ConfigurationException exception = new ConfigurationException("Test");
        assertTrue(exception instanceof ScannerException);
    }
    
    @Test
    void testExtendsException() {
        ConfigurationException exception = new ConfigurationException("Test");
        assertTrue(exception instanceof Exception);
    }
}
