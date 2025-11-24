package com.scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * Loads and parses XML configuration files for the sensitive data scanner.
 * Extracts keyword patterns and sensitive object types from the XML structure.
 */
public class ConfigurationLoader {

    /**
     * Loads the scan configuration from a directory containing multiple XML files.
     * Expected files: scan-directories.xml, keywords.xml, object-types.xml,
     * exclusions.xml
     *
     * @param configDirPath the path to the configuration directory
     * @return a ScanConfiguration object containing the parsed patterns
     * @throws ConfigurationException if the directory is not found, invalid, or
     *                                contains errors
     */
    public ScanConfiguration loadConfiguration(String configDirPath) throws ConfigurationException {
        if (configDirPath == null || configDirPath.trim().isEmpty()) {
            throw new ConfigurationException("Configuration directory path cannot be null or empty");
        }

        File configDir = new File(configDirPath);
        if (!configDir.exists()) {
            throw new ConfigurationException("Configuration directory not found: " + configDirPath);
        }

        if (!configDir.isDirectory()) {
            throw new ConfigurationException("Configuration path is not a directory: " + configDirPath);
        }

        if (!configDir.canRead()) {
            throw new ConfigurationException("Configuration directory is not readable: " + configDirPath);
        }

        // Define expected file names
        File scanDirFile = new File(configDir, "scan-directories.xml");
        File keywordsFile = new File(configDir, "keywords.xml");
        File objectTypesFile = new File(configDir, "object-types.xml");
        File exclusionsFile = new File(configDir, "exclusions.xml");

        // Load each configuration component
        List<String> scanDirectories = new ArrayList<>();
        List<KeywordPattern> patterns = new ArrayList<>();
        List<KeywordPattern> sensitiveObjectTypes = new ArrayList<>();
        List<KeywordPattern> exclusions = new ArrayList<>();

        // Load scan directories (optional)
        if (scanDirFile.exists()) {
            scanDirectories = loadScanDirectoriesFromFile(scanDirFile);
        }

        // Load keywords (optional)
        if (keywordsFile.exists()) {
            patterns = loadKeywordsFromFile(keywordsFile);
        }

        // Load object types (optional)
        if (objectTypesFile.exists()) {
            sensitiveObjectTypes = loadObjectTypesFromFile(objectTypesFile);
        }

        // Load exclusions (optional)
        if (exclusionsFile.exists()) {
            exclusions = loadExclusionsFromFile(exclusionsFile);
        }

        return new ScanConfiguration(scanDirectories, patterns, sensitiveObjectTypes, exclusions);
    }

    /**
     * Loads scan directories from a standalone XML file.
     */
    private List<String> loadScanDirectoriesFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return parseScanDirectories(document);
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Error loading scan directories from " + file.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads keywords from a standalone XML file.
     */
    private List<KeywordPattern> loadKeywordsFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return parseKeywords(document);
        } catch (Exception e) {
            throw new ConfigurationException("Error loading keywords from " + file.getName() + ": " + e.getMessage(),
                    e);
        }
    }

    /**
     * Loads object types from a standalone XML file.
     */
    private List<KeywordPattern> loadObjectTypesFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return parseSensitiveObjectTypes(document);
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Error loading object types from " + file.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads exclusions from a standalone XML file.
     */
    private List<KeywordPattern> loadExclusionsFromFile(File file) throws ConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return parseExclusions(document);
        } catch (Exception e) {
            throw new ConfigurationException("Error loading exclusions from " + file.getName() + ": " + e.getMessage(),
                    e);
        }
    }

    /**
     * Parses scan directory elements from the XML document.
     *
     * @param xmlDoc the parsed XML document
     * @return a list of scan directory paths
     * @throws ConfigurationException if there are errors parsing scan directories
     */
    private List<String> parseScanDirectories(Document xmlDoc) throws ConfigurationException {
        List<String> directories = new ArrayList<>();
        NodeList directoryNodes = xmlDoc.getElementsByTagName("directory");

        for (int i = 0; i < directoryNodes.getLength(); i++) {
            Element directoryElement = (Element) directoryNodes.item(i);
            String directory = directoryElement.getTextContent().trim();

            if (directory.isEmpty()) {
                throw new ConfigurationException("Empty directory found at position " + i);
            }

            directories.add(directory);
        }

        return directories;
    }

    /**
     * Parses keyword elements from the XML document.
     *
     * @param xmlDoc the parsed XML document
     * @return a list of KeywordPattern objects
     * @throws ConfigurationException if there are errors parsing keywords
     */
    private List<KeywordPattern> parseKeywords(Document xmlDoc) throws ConfigurationException {
        List<KeywordPattern> patterns = new ArrayList<>();
        NodeList keywordNodes = xmlDoc.getElementsByTagName("keyword");

        for (int i = 0; i < keywordNodes.getLength(); i++) {
            Element keywordElement = (Element) keywordNodes.item(i);
            String keyword = keywordElement.getTextContent().trim();
            String type = keywordElement.getAttribute("type");

            if (keyword.isEmpty()) {
                throw new ConfigurationException("Empty keyword found at position " + i);
            }

            if (type.isEmpty()) {
                throw new ConfigurationException("Missing 'type' attribute for keyword: " + keyword);
            }

            boolean isRegex = "regex".equalsIgnoreCase(type);

            try {
                patterns.add(new KeywordPattern(keyword, isRegex));
            } catch (PatternSyntaxException e) {
                throw new ConfigurationException("Invalid regex pattern: " + keyword + " - " + e.getMessage(), e);
            }
        }

        return patterns;
    }

    /**
     * Parses sensitive object type elements from the XML document.
     *
     * @param xmlDoc the parsed XML document
     * @return a list of KeywordPattern objects representing sensitive object types
     * @throws ConfigurationException if there are errors parsing object types
     */
    private List<KeywordPattern> parseSensitiveObjectTypes(Document xmlDoc) throws ConfigurationException {
        List<KeywordPattern> objectTypes = new ArrayList<>();
        NodeList objectTypeNodes = xmlDoc.getElementsByTagName("object-type");

        for (int i = 0; i < objectTypeNodes.getLength(); i++) {
            Element objectTypeElement = (Element) objectTypeNodes.item(i);
            String objectType = objectTypeElement.getTextContent().trim();
            String type = objectTypeElement.getAttribute("type");

            if (objectType.isEmpty()) {
                throw new ConfigurationException("Empty object-type found at position " + i);
            }

            if (type.isEmpty()) {
                throw new ConfigurationException("Missing 'type' attribute for object-type: " + objectType);
            }

            boolean isRegex = "regex".equalsIgnoreCase(type);

            try {
                objectTypes.add(new KeywordPattern(objectType, isRegex));
            } catch (PatternSyntaxException e) {
                throw new ConfigurationException(
                        "Invalid regex pattern for object-type: " + objectType + " - " + e.getMessage(), e);
            }
        }

        return objectTypes;
    }

    /**
     * Parses exclusion elements from the XML document.
     *
     * @param xmlDoc the parsed XML document
     * @return a list of KeywordPattern objects representing exclusions
     * @throws ConfigurationException if there are errors parsing exclusions
     */
    private List<KeywordPattern> parseExclusions(Document xmlDoc) throws ConfigurationException {
        List<KeywordPattern> exclusions = new ArrayList<>();
        NodeList exclusionNodes = xmlDoc.getElementsByTagName("exclusion");

        for (int i = 0; i < exclusionNodes.getLength(); i++) {
            Element exclusionElement = (Element) exclusionNodes.item(i);
            String exclusion = exclusionElement.getTextContent().trim();
            String type = exclusionElement.getAttribute("type");

            if (exclusion.isEmpty()) {
                throw new ConfigurationException("Empty exclusion found at position " + i);
            }

            if (type.isEmpty()) {
                throw new ConfigurationException("Missing 'type' attribute for exclusion: " + exclusion);
            }

            boolean isRegex = "regex".equalsIgnoreCase(type);

            try {
                exclusions.add(new KeywordPattern(exclusion, isRegex));
            } catch (PatternSyntaxException e) {
                throw new ConfigurationException(
                        "Invalid regex pattern for exclusion: " + exclusion + " - " + e.getMessage(), e);
            }
        }

        return exclusions;
    }
}
