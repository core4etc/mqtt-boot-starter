package com.core4etc.mqtt.config;

/**
 * A record representing the complete system configuration for the MQTT application.
 * This configuration structure contains all necessary settings for various application
 * components including MQTT, database, Redis, logging, and general application settings.
 *
 * <p><b>Configuration Structure:</b>
 * <ul>
 *   <li>Core application settings</li>
 *   <li>MQTT broker connection parameters</li>
 *   <li>Database connection details</li>
 *   <li>Redis server configuration</li>
 *   <li>Logging configuration</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * {@code
 * SystemConfig config = new SystemConfig(
 *     new Core4etc(
 *         new Application("my-app"),
 *         new Mqtt("broker.example.com", "tcp", "1883", "user", "pass"),
 *         new Database("localhost", "5432", "mydb", "dbuser", "dbpass"),
 *         new Redis("redis-host", "6379", "redispass", true),
 *         new Log(new File("app.log", "/var/log"))
 *     )
 * );
 * }
 * </pre>
 *
 * @param core4etc the root configuration container for all core4etc application settings
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 */
public record SystemConfig(Core4etc core4etc) {

    /**
     * A record representing the core configuration container for the application.
     * This serves as the parent container for all application component configurations.
     *
     * @param application general application settings
     * @param mqtt MQTT broker connection settings
     * @param database database connection settings
     * @param redis Redis server connection settings
     * @param log logging configuration settings
     */
    public record Core4etc(Application application, Mqtt mqtt, Database database, Redis redis, Log log) {

        /**
         * A record representing general application configuration settings.
         *
         * @param name the name of the application
         */
        public record Application(String name){};

        /**
         * A record representing MQTT broker connection configuration.
         *
         * @param url the URL or hostname of the MQTT broker
         * @param protocol the protocol to use (e.g., "tcp", "ssl", "ws")
         * @param port the port number of the MQTT broker
         * @param username the username for MQTT broker authentication
         * @param password the password for MQTT broker authentication
         */
        public record Mqtt(String url, String protocol, String port, String username, String password){}

        /**
         * A record representing database connection configuration.
         *
         * @param url the URL or hostname of the database server
         * @param port the port number of the database server
         * @param name the name of the database
         * @param username the username for database authentication
         * @param password the password for database authentication
         */
        public record Database(String url, String port, String name, String username, String password){}

        /**
         * A record representing Redis server connection configuration.
         *
         * @param url the URL or hostname of the Redis server
         * @param port the port number of the Redis server
         * @param password the password for Redis authentication (empty string if no auth)
         * @param subscribe whether to subscribe to Redis key expiration events
         */
        public record Redis(String url, String port, String password, Boolean subscribe){}

        /**
         * A record representing logging configuration.
         *
         * @param file file-based logging configuration
         */
        public record Log(File file) {

            /**
             * A record representing file-based logging configuration.
             *
             * @param name the name of the log file
             * @param path the directory path where log files should be stored
             */
            public record File(String name, String path){}
        }
    }
}