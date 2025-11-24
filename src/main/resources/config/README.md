# Configuration Directory

This directory contains the scanner configuration split into separate files for better organization and maintainability.

## Files

- **scan-directories.xml** - Directories to scan for Java files
- **keywords.xml** - Sensitive keywords to detect (plain text and regex patterns)
- **object-types.xml** - Sensitive object types that shouldn't be logged
- **exclusions.xml** - Patterns to exclude from scan results

## Usage

> **Note**: Multi-file configuration support is coming soon!

Once implemented, you'll be able to use this directory with:
```bash
java -jar sensitive-data-scanner.jar ./src/main/resources/config
```

## Current Status

The configuration files are ready, but the loader code needs to be updated to support directory-based loading. For now, continue using the single `sample-config.xml` file.

## Benefits

- **Modular**: Each concern in its own file
- **Maintainable**: Easier to update specific configurations
- **Shareable**: Can version control and share individual config files
- **Clear**: Better organization than one large XML file
