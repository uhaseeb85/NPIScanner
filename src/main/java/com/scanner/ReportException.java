package com.scanner;

/**
 * Exception thrown when there are errors during report generation,
 * such as file writing errors or HTML generation errors.
 */
public class ReportException extends ScannerException {
    
    /**
     * Constructs a new ReportException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ReportException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ReportException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
