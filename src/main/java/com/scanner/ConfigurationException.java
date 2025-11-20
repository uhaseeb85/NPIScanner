package com.scanner;

/**
 * Exception thrown when there are configuration-related errors,
 * such as invalid XML, missing configuration files, or invalid regex patterns.
 */
public class ConfigurationException extends ScannerException {
    
    /**
     * Constructs a new ConfigurationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
