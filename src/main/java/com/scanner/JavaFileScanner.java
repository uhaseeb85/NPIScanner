package com.scanner;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans directories recursively to find Java source files.
 */
public class JavaFileScanner {

    /**
     * Scans a directory recursively and returns all Java files found.
     * If access is denied to some files or directories, a warning is logged
     * and scanning continues with accessible files.
     *
     * @param rootPath the root directory path to scan
     * @return list of paths to Java files
     * @throws ScanException if the root path is invalid or inaccessible
     */
    public List<Path> scanDirectory(String rootPath) throws ScanException {
        if (rootPath == null || rootPath.trim().isEmpty()) {
            throw new ScanException("Root path cannot be null or empty");
        }
        
        Path root = Paths.get(rootPath);
        
        // Validate root path exists and is readable
        if (!Files.exists(root)) {
            throw new ScanException("Path does not exist: " + rootPath);
        }
        
        if (!Files.isDirectory(root)) {
            throw new ScanException("Path is not a directory: " + rootPath);
        }
        
        if (!Files.isReadable(root)) {
            throw new ScanException("Path is not readable: " + rootPath);
        }
        
        List<Path> javaFiles = new ArrayList<>();
        
        try (Stream<Path> pathStream = Files.walk(root)) {
            javaFiles = pathStream
                .filter(path -> {
                    try {
                        return isJavaFile(path);
                    } catch (Exception e) {
                        // Log warning for individual file access issues and continue
                        System.err.println("Warning: Cannot access file " + path + ": " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        } catch (AccessDeniedException e) {
            // Log warning and continue with files we can access
            System.err.println("Warning: Access denied to some files or directories: " + e.getFile());
            System.err.println("Continuing scan with accessible files...");
        } catch (IOException e) {
            throw new ScanException("Error scanning directory: " + rootPath + " - " + e.getMessage(), e);
        }
        
        return javaFiles;
    }
    
    /**
     * Checks if a path represents a Java source file.
     *
     * @param path the path to check
     * @return true if the path is a regular file with .java extension
     */
    private boolean isJavaFile(Path path) {
        return Files.isRegularFile(path) && 
               path.toString().endsWith(".java");
    }
}
