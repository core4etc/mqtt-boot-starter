package com.core4etc.mqtt;

import com.core4etc.mqtt.bean.Bean;
import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.load.Loader;
import com.core4etc.mqtt.load.SystemLoader;
import io.lettuce.core.RedisClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.sql.Connection;
import java.util.Optional;

/**
 * The main application class for initializing and managing MQTT application components.
 * This class provides a builder pattern for configuring and initializing various
 * application components including system configuration, Redis client, database
 * connection, and MQTT client.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Builder pattern for flexible component configuration</li>
 *   <li>Automatic dependency management through Bean container</li>
 *   <li>Optional component initialization with fallback defaults</li>
 *   <li>Thread-safe singleton component access</li>
 *   <li>Simplified application bootstrap</li>
 * </ul>
 *
 * <p><b>Component Initialization Order:</b></p>
 * <ol>
 *   <li>System configuration (mandatory)</li>
 *   <li>Redis client (optional)</li>
 *   <li>Database connection (optional)</li>
 *   <li>MQTT client (optional)</li>
 * </ol>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * // Simple bootstrap
 * Application.run();
 *
 * // Custom configuration
 * Application.configure()
 *     .withSystem(new CustomSystemLoader())
 *     .withRedis(new CustomRedisLoader())
 *     .run();
 *
 * // Access components
 * SystemConfig config = Application.getSystemConfig();
 * Optional<RedisClient> redis = Application.getRedisClient();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see SystemConfig
 * @see RedisClient
 * @see Connection
 * @see IMqttClient
 * @see Bean
 */
public final class Application {
    private static SystemConfig systemConfig;
    private static RedisClient redisClient;
    private static Connection datasourceConnection;
    private static IMqttClient mqttClient;

    /**
     * Private constructor to prevent instantiation.
     * This class should be used statically.
     */
    private Application() {
    }

    /**
     * Builder class for configuring and initializing application components.
     * Provides a fluent interface for setting up system configuration and
     * optional components with custom or default loaders.
     */
    public static class Builder {
        private Loader<SystemConfig> systemLoader = new SystemLoader();
        private Loader<RedisClient> redisLoader;
        private Loader<Connection> datasourceLoader;
        private Loader<IMqttClient> mqttLoader;

        /**
         * Sets a custom system configuration loader.
         *
         * @param loader the loader to use for system configuration
         * @return the current Builder instance for method chaining
         */
        public Builder withSystem(Loader<SystemConfig> loader) {
            this.systemLoader = loader;
            return this;
        }

        /**
         * Sets a custom Redis client loader.
         *
         * @param loader the loader to use for Redis client initialization
         * @return the current Builder instance for method chaining
         */
        public Builder withRedis(Loader<RedisClient> loader) {
            this.redisLoader = loader;
            return this;
        }

        /**
         * Sets a custom Redis client loader only if no loader has been set previously.
         *
         * @param loader the loader to use for Redis client initialization if absent
         * @return the current Builder instance for method chaining
         */
        public Builder withRedisIfAbsent(Loader<RedisClient> loader) {
            if (this.redisLoader == null) {
                this.redisLoader = loader;
            }
            return this;
        }

        /**
         * Sets a custom datasource connection loader.
         *
         * @param loader the loader to use for database connection initialization
         * @return the current Builder instance for method chaining
         */
        public Builder withDatasource(Loader<Connection> loader) {
            this.datasourceLoader = loader;
            return this;
        }

        /**
         * Sets a custom datasource connection loader only if no loader has been set previously.
         *
         * @param loader the loader to use for database connection initialization if absent
         * @return the current Builder instance for method chaining
         */
        public Builder withDatasourceIfAbsent(Loader<Connection> loader) {
            if (this.datasourceLoader == null) {
                this.datasourceLoader = loader;
            }
            return this;
        }

        /**
         * Sets a custom MQTT client loader.
         *
         * @param loader the loader to use for MQTT client initialization
         * @return the current Builder instance for method chaining
         */
        public Builder withMqtt(Loader<IMqttClient> loader) {
            this.mqttLoader = loader;
            return this;
        }

        /**
         * Sets a custom MQTT client loader only if no loader has been set previously.
         *
         * @param loader the loader to use for MQTT client initialization if absent
         * @return the current Builder instance for method chaining
         */
        public Builder withMqttIfAbsent(Loader<IMqttClient> loader) {
            if (this.mqttLoader == null) {
                this.mqttLoader = loader;
            }
            return this;
        }

        /**
         * Initializes and runs the application with the configured components.
         *
         * @throws Exception if any component initialization fails
         */
        public void run() throws Exception {
            Application.initialize(this);
        }
    }

    /**
     * Creates and returns a new Builder instance for configuring the application.
     *
     * @return a new Builder instance with default configuration
     */
    public static Builder configure() {
        return new Builder();
    }

    /**
     * Initializes all application components based on the builder configuration.
     * System configuration is mandatory, while other components are optional.
     *
     * @param config the builder configuration containing component loaders
     * @throws Exception if system configuration loading fails or any component
     *         initialization encounters an error
     */
    private static void initialize(Builder config) throws Exception {
        // SystemConfig is mandatory
        systemConfig = config.systemLoader.load();
        Bean.create(systemConfig, config.systemLoader.getType());

        // Optional components
        initializeOptionalComponent(config.redisLoader);

        initializeOptionalComponent(config.datasourceLoader);

        initializeOptionalComponent(config.mqttLoader);
    }

    /**
     * Initializes an optional application component using either a custom loader
     * or a default loader if no custom loader is provided.
     *
     * @param <T> the type of component to initialize
     * @param customLoader the custom loader to use, or null to use default
     * @throws Exception if component loading fails
     */
    private static <T> void initializeOptionalComponent(Loader<T> customLoader) throws Exception {
        if (customLoader != null) {
            T component = customLoader.load();
            Bean.create(component, customLoader.getType());
        }
    }

    /**
     * Returns the system configuration loaded during application initialization.
     *
     * @return the SystemConfig instance, or null if application hasn't been initialized
     */
    public static SystemConfig getSystemConfig() {
        return systemConfig;
    }

    /**
     * Returns an Optional containing the Redis client if it was initialized.
     *
     * @return an Optional with RedisClient if available, empty Optional otherwise
     */
    public static Optional<RedisClient> getRedisClient() {
        return Optional.ofNullable(redisClient);
    }

    /**
     * Returns an Optional containing the database connection if it was initialized.
     *
     * @return an Optional with Connection if available, empty Optional otherwise
     */
    public static Optional<Connection> getDatasourceConnection() {
        return Optional.ofNullable(datasourceConnection);
    }

    /**
     * Returns an Optional containing the MQTT client if it was initialized.
     *
     * @return an Optional with IMqttClient if available, empty Optional otherwise
     */
    public static Optional<IMqttClient> getMqttClient() {
        return Optional.ofNullable(mqttClient);
    }

    /**
     * Convenience method to configure and run the application with default settings.
     *
     * @throws Exception if any component initialization fails
     */
    public static void run() throws Exception {
        configure().run();
    }
}