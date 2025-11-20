package com.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaFileScannerTest {

    @Test
    void testScanDirectory_withJavaFiles(@TempDir Path tempDir) throws IOException, ScanException {
        // Create test directory structure with .java files
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        
        Path javaFile1 = tempDir.resolve("Test1.java");
        Path javaFile2 = subDir.resolve("Test2.java");
        Path txtFile = tempDir.resolve("readme.txt");
        
        Files.createFile(javaFile1);
        Files.createFile(javaFile2);
        Files.createFile(txtFile);
        
        JavaFileScanner scanner = new JavaFileScanner();
        List<Path> result = scanner.scanDirectory(tempDir.toString());
        
        assertEquals(2, result.size());
        assertTrue(result.contains(javaFile1));
        assertTrue(result.contains(javaFile2));
        assertFalse(result.contains(txtFile));
    }
    
    @Test
    void testScanDirectory_withNonJavaFiles(@TempDir Path tempDir) throws IOException, ScanException {
        // Create test directory with only non-.java files
        Path txtFile = tempDir.resolve("readme.txt");
        Path xmlFile = tempDir.resolve("config.xml");
        Path mdFile = tempDir.resolve("notes.md");
        
        Files.createFile(txtFile);
        Files.createFile(xmlFile);
        Files.createFile(mdFile);
        
        JavaFileScanner scanner = new JavaFileScanner();
        List<Path> result = scanner.scanDirectory(tempDir.toString());
        
        assertEquals(0, result.size());
    }
    
    @Test
    void testScanDirectory_emptyDirectory(@TempDir Path tempDir) throws ScanException {
        JavaFileScanner scanner = new JavaFileScanner();
        List<Path> result = scanner.scanDirectory(tempDir.toString());
        
        assertEquals(0, result.size());
    }
    
    @Test
    void testScanDirectory_nestedStructure(@TempDir Path tempDir) throws IOException, ScanException {
        // Create nested directory structure
        Path level1 = tempDir.resolve("level1");
        Path level2 = level1.resolve("level2");
        Path level3 = level2.resolve("level3");
        
        Files.createDirectories(level3);
        
        Path javaFile1 = tempDir.resolve("Root.java");
        Path javaFile2 = level1.resolve("Level1.java");
        Path javaFile3 = level2.resolve("Level2.java");
        Path javaFile4 = level3.resolve("Level3.java");
        
        Files.createFile(javaFile1);
        Files.createFile(javaFile2);
        Files.createFile(javaFile3);
        Files.createFile(javaFile4);
        
        JavaFileScanner scanner = new JavaFileScanner();
        List<Path> result = scanner.scanDirectory(tempDir.toString());
        
        assertEquals(4, result.size());
        assertTrue(result.contains(javaFile1));
        assertTrue(result.contains(javaFile2));
        assertTrue(result.contains(javaFile3));
        assertTrue(result.contains(javaFile4));
    }
    
    @Test
    void testScanDirectory_invalidPath() {
        JavaFileScanner scanner = new JavaFileScanner();
        
        ScanException exception = assertThrows(ScanException.class, () -> {
            scanner.scanDirectory("/invalid/nonexistent/path");
        });
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    void testScanDirectory_pathIsFile(@TempDir Path tempDir) throws IOException {
        // Create a file instead of directory
        Path file = tempDir.resolve("test.java");
        Files.createFile(file);
        
        JavaFileScanner scanner = new JavaFileScanner();
        
        ScanException exception = assertThrows(ScanException.class, () -> {
            scanner.scanDirectory(file.toString());
        });
        
        assertTrue(exception.getMessage().contains("not a directory"));
    }
    
    @Test
    void testScanDirectory_nullPath() {
        JavaFileScanner scanner = new JavaFileScanner();
        
        ScanException exception = assertThrows(ScanException.class, () -> {
            scanner.scanDirectory(null);
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    void testScanDirectory_emptyPath() {
        JavaFileScanner scanner = new JavaFileScanner();
        
        ScanException exception = assertThrows(ScanException.class, () -> {
            scanner.scanDirectory("   ");
        });
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    void testScanDirectory_withMixedFiles(@TempDir Path tempDir) throws IOException, ScanException {
        // Create a mix of Java and non-Java files
        Path javaFile = tempDir.resolve("Test.java");
        Path classFile = tempDir.resolve("Test.class");
        Path jarFile = tempDir.resolve("lib.jar");
        Path xmlFile = tempDir.resolve("config.xml");
        
        Files.createFile(javaFile);
        Files.createFile(classFile);
        Files.createFile(jarFile);
        Files.createFile(xmlFile);
        
        JavaFileScanner scanner = new JavaFileScanner();
        List<Path> result = scanner.scanDirectory(tempDir.toString());
        
        assertEquals(1, result.size());
        assertTrue(result.contains(javaFile));
    }
}
