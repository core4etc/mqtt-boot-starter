package com.core4etc.mqtt.template;

import com.core4etc.mqtt.load.DatasourceLoader;
import com.core4etc.mqtt.bean.Bean;

import java.sql.Connection;

/**
 * A template class for managing database connections through the Bean container.
 * This class provides static utility methods for obtaining and closing database
 * connections with automatic connection validation and recovery.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Automatic connection validation before returning connections</li>
 *   <li>Automatic reconnection if the connection is closed</li>
 *   <li>Integration with the Bean container for singleton connection management</li>
 *   <li>Exception handling with error logging</li>
 * </ul>
 * </p>
 *
 * <p><b>Connection Lifecycle Management:</b>
 * <ul>
 *   <li>Checks if the cached connection is closed before returning it</li>
 *   <li>Automatically recreates closed connections using DatasourceLoader</li>
 *   <li>Provides graceful connection closing</li>
 * </ul>
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * // Get a database connection
 * Connection connection = DatasourceTemplate.getConnection();
 *
 * try {
 *     // Use the connection for database operations
 *     Statement stmt = connection.createStatement();
 *     ResultSet rs = stmt.executeQuery("SELECT * FROM users");
 *
 *     // Process results...
 *
 * } finally {
 *     // Connection is managed by Bean container, no need to close here
 * }
 *
 * // Close connection explicitly (typically at application shutdown)
 * DatasourceTemplate.close();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see Connection
 * @see Bean
 * @see DatasourceLoader
 */
public class DatasourceTemplate {

    /**
     * Retrieves a database connection from the Bean container.
     * This method performs validation on the cached connection and automatically
     * recreates it if the connection is closed or invalid.
     *
     * <p><b>Connection Validation:</b>
     * <ul>
     *   <li>Checks if the connection is closed using {@link Connection#isClosed()}</li>
     *   <li>If closed, removes the old connection from Bean container and creates a new one</li>
     *   <li>Returns the validated or newly created connection</li>
     * </ul>
     * </p>
     *
     * <p><b>Error Handling:</b>
     * <ul>
     *   <li>Prints stack trace to standard error if connection validation fails</li>
     *   <li>Returns null if an exception occurs during connection retrieval</li>
     * </ul>
     * </p>
     *
     * @return a valid {@link Connection} instance, or null if connection retrieval fails
     */
    public static Connection getConnection() {
        Connection connection = Bean.get(Connection.class);
        try {
            if (connection.isClosed()) {
                Bean.remove(Connection.class);
                Bean.get(DatasourceLoader.class).load();
            }
            return Bean.get(Connection.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the database connection stored in the Bean container.
     * This method should typically be called during application shutdown to
     * ensure proper resource cleanup.
     *
     * <p><b>Note:</b> This method handles any exceptions that may occur during
     * connection closing and prints the stack trace to standard error. The
     * application will continue running even if closing fails.</p>
     *
     * <p><b>Typical Usage:</b>
     * <ul>
     *   <li>Call this method during application shutdown</li>
     *   <li>Use in conjunction with shutdown hooks</li>
     *   <li>Ensure database connections are properly released</li>
     * </ul>
     * </p>
     */
    public static void close() {
        try {
            Bean.get(Connection.class).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}