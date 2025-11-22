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
     * Loads the scan configuration from the specified XML file.
     *
     * @param configFilePath the path to the XML configuration file
     * @return a ScanConfiguration object containing the parsed patterns
     * @throws ConfigurationException if the file is not found, invalid, or contains
     *                                errors
     */
    public ScanConfiguration loadConfiguration(String configFilePath) throws ConfigurationException {
        if (configFilePath == null || configFilePath.trim().isEmpty()) {
            throw new ConfigurationException("Configuration file path cannot be null or empty");
        }

        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new ConfigurationException("Configuration file not found: " + configFilePath);
        }

        if (!configFile.canRead()) {
            throw new ConfigurationException("Configuration file is not readable: " + configFilePath);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);
            document.getDocumentElement().normalize();

            List<KeywordPattern> patterns = parseKeywords(document);
            List<KeywordPattern> sensitiveObjectTypes = parseSensitiveObjectTypes(document);
            List<KeywordPattern> exclusions = parseExclusions(document);

            return new ScanConfiguration(patterns, sensitiveObjectTypes, exclusions);

        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Failed to configure XML parser", e);
        } catch (SAXException e) {
            throw new ConfigurationException("Invalid XML structure in configuration file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading configuration file: " + configFilePath, e);
        }
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
