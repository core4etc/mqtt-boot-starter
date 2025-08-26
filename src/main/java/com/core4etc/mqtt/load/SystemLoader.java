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

public class SystemLoader implements Loader<SystemConfig> {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public SystemConfig load() throws Exception {
        return findSystemConfig();
    }

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
     * Helper: put "app.security.enabled=true" → nested map structure
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
