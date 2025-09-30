package com.core4etc.mqtt.load;

import com.core4etc.mqtt.config.SystemConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link Loader} implementation that loads system configuration from various file formats.
 * This class supports loading configuration from YAML (.yaml, .yml) and Properties (.properties) files.
 * It automatically searches for configuration files in the specified directory and loads the first found file.
 *
 * <p><b>Supported Formats:</b>
 * <ul>
 *   <li>YAML files (.yaml, .yml) - parsed using Jackson YAML factory</li>
 *   <li>Properties files (.properties) - converted to nested map structure and then to SystemConfig</li>
 * </ul>
 * </p>
 *
 * <p><b>Search Behavior:</b>
 * <ul>
 *   <li>Searches recursively from the root directory</li>
 *   <li>Uses system property "config.dir" to determine root directory (defaults to current directory)</li>
 *   <li>Returns the first matching configuration file found</li>
 * </ul>
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * SystemLoader systemLoader = new SystemLoader();
 *
 * // Load system configuration
 * SystemConfig config = systemLoader.load();
 *
 * // Or use with runtime exception wrapping
 * SystemConfig config = systemLoader.get();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see Loader
 * @see SystemConfig
 * @see ObjectMapper
 */
public class SystemLoader implements Loader<SystemConfig> {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Loads the system configuration from the first available configuration file found.
     * This method searches for configuration files in the directory specified by the
     * "config.dir" system property (defaulting to current directory) and loads the
     * first file found, supporting both YAML and Properties formats.
     *
     * @return a fully populated {@link SystemConfig} instance
     * @throws Exception if any of the following occurs:
     *         <ul>
     *           <li>No configuration files are found in the search directory</li>
     *           <li>Unsupported file format is encountered</li>
     *           <li>YAML parsing fails due to malformed content</li>
     *           <li>Properties file parsing fails</li>
     *           <li>IO errors during file reading</li>
     *           <li>JSON conversion errors for properties files</li>
     *         </ul>
     */
    @Override
    public SystemConfig load() throws Exception {
        return findSystemConfig();
    }

    @Override
    public Class<SystemConfig> getType() {
        return SystemConfig.class;
    }

    /**
     * Finds and loads the system configuration from the first available configuration file.
     * Searches recursively from the root directory for YAML (.yaml, .yml) and Properties (.properties) files.
     *
     * @return a configured {@link SystemConfig} instance
     * @throws IllegalStateException if no configuration files are found
     * @throws IllegalArgumentException if an unsupported file format is encountered
     * @throws Exception if file parsing or reading fails
     */
    private static SystemConfig findSystemConfig() throws Exception {
        Path rootDir = Paths.get(System.getProperty("config.dir", "."));
        List<Path> configFiles = findConfigFiles(rootDir);

        if (configFiles.isEmpty()) {
            throw new IllegalStateException("No config files found in " + rootDir.toAbsolutePath());
        }

        // For now: just load the first file
        Path file = configFiles.get(0);
        System.out.println("Loading config from: " + file);

        if (file.toString().endsWith(".yaml") || file.toString().endsWith(".yml")) {
            return yamlMapper.readValue(file.toFile(), SystemConfig.class);
        } else if (file.toString().endsWith(".properties")) {
            return loadFromProperties(file.toFile());
        } else {
            throw new IllegalArgumentException("Unsupported config format: " + file);
        }
    }

    /**
     * Recursively finds all configuration files in the specified directory and its subdirectories.
     * Searches for files with extensions: .yaml, .yml, .properties.
     *
     * @param rootDir the root directory to start searching from
     * @return a list of paths to found configuration files
     * @throws IOException if an I/O error occurs during file search
     */
    private static List<Path> findConfigFiles(Path rootDir) throws IOException {
        try (var paths = Files.find(
                rootDir,
                Integer.MAX_VALUE, // recursive
                (path, attrs) -> attrs.isRegularFile() &&
                        (path.toString().endsWith(".yaml") ||
                                path.toString().endsWith(".yml") ||
                                path.toString().endsWith(".properties"))
        )) {
            return paths.toList();
        }
    }

    /**
     * Loads system configuration from a Properties file.
     * Converts flat properties structure to a nested map structure and then to SystemConfig
     * using JSON conversion.
     *
     * @param file the Properties file to load
     * @return a {@link SystemConfig} instance populated from the properties file
     * @throws IOException if an I/O error occurs reading the file or JSON conversion fails
     */
    private static SystemConfig loadFromProperties(File file) throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        }

        // Convert flat properties → Map<String,Object>
        Map<String, Object> map = new HashMap<>();
        props.forEach((k, v) -> putDeep(map, k.toString(), v));

        // Convert map → JSON → Config
        String json = jsonMapper.writeValueAsString(map);
        return jsonMapper.readValue(json, SystemConfig.class);
    }

    /**
     * Helper method to convert flat property keys into nested map structure.
     * For example: "app.security.enabled=true" becomes a nested map structure
     * with "app" → "security" → "enabled" = "true".
     *
     * @param root the root map to populate with nested structure
     * @param key the flat property key (e.g., "app.security.enabled")
     * @param value the property value
     */
    @SuppressWarnings("unchecked")
    private static void putDeep(Map<String, Object> root, String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<>());
        }
        current.put(parts[parts.length - 1], value);
    }

}