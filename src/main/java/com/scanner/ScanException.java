package com.scanner;

/**
 * Exception thrown when there are errors during the scanning process,
 * such as file system errors, file read errors, or parsing errors.
 */
public class ScanException extends ScannerException {
    
    /**
     * Constructs a new ScanException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ScanException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ScanException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
