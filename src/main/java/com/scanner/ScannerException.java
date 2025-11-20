package com.scanner;

/**
 * Base exception class for all scanner-related exceptions.
 * Extends Exception to provide checked exception behavior.
 */
public class ScannerException extends Exception {
    
    /**
     * Constructs a new ScannerException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ScannerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ScannerException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
