package com.core4etc.mqtt.load;

import com.core4etc.mqtt.bean.Bean;
import com.core4etc.mqtt.config.SystemConfig;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * A {@link Loader} implementation that provides database connections to a PostgreSQL database.
 * This class retrieves database configuration from the system configuration and creates
 * a new database connection using JDBC DriverManager.
 *
 * <p>The connection details (URL, port, database name, username, and password) are
 * obtained from the {@link SystemConfig} bean. The connection is established using
 * the PostgreSQL JDBC driver with a connection string formatted as:
 * {@code jdbc:postgresql://<url>:<port>/<database_name>}</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * DatasourceLoader datasourceLoader = new DatasourceLoader();
 *
 * // Get connection with exception handling
 * try (Connection connection = datasourceLoader.load()) {
 *     // Use the connection
 * }
 *
 * // Or use with runtime exception wrapping
 * Connection connection = datasourceLoader.get();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see Loader
 * @see Connection
 * @see SystemConfig
 * @see Bean
 */
public class DatasourceLoader implements Loader<Connection> {

    /**
     * Loads and returns a database connection to the configured PostgreSQL database.
     * This method retrieves the database configuration from the system configuration
     * bean and establishes a connection using {@link DriverManager#getConnection(String, String, String)}.
     *
     * <p>The connection string is constructed in the format:
     * {@code jdbc:postgresql://<url>:<port>/<database_name>}</p>
     *
     * <p><b>Note:</b> The returned connection should be properly closed by the caller
     * using try-with-resources or explicit {@link Connection#close()} calls to avoid
     * resource leaks.</p>
     *
     * @return a new {@link Connection} to the configured PostgreSQL database
     * @throws Exception if any of the following occurs:
     *         <ul>
     *           <li>Database configuration is not available or incomplete</li>
     *           <li>JDBC driver is not found or cannot be loaded</li>
     *           <li>Database connection fails (invalid credentials, network issues, etc.)</li>
     *           <li>Any other SQLException during connection establishment</li>
     *         </ul>
     * @see SystemConfig.Core4etc.Database
     * @see DriverManager#getConnection(String, String, String)
     */
    @Override
    public Connection load() throws Exception {
        SystemConfig.Core4etc.Database config = Bean.get(SystemConfig.class).core4etc().database();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://" + config.url() + ":" + config.port() + "/" + config.name(),
                config.username(), config.password())) {
            return connection;
        }
    }

}