package com.core4etc.mqtt.load;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * A builder class for creating and configuring {@link MqttConnectOptions} with a fluent API.
 * This class provides a convenient way to set up MQTT connection options with sensible defaults
 * and a chainable builder pattern.
 *
 * <p><b>Default Values:</b></p>
 * <ul>
 *   <li>Automatic Reconnect: true</li>
 *   <li>Clean Session: false</li>
 *   <li>Max Inflight: 200 messages</li>
 *   <li>Connection Timeout: 30 seconds</li>
 *   <li>Keep Alive Interval: 60 seconds</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * MqttConnectOptions options = MqttOptions.builder()
 *     .withAutomaticReconnect(true)
 *     .withCleanSession(false)
 *     .withUserName("user")
 *     .withPassword("pass")
 *     .withMaxInflight(200)
 *     .withConnectionTimeout(30)
 *     .withKeepAliveInterval(60)
 *     .build();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see MqttConnectOptions
 */
public class MqttOptions {

    /**
     * Creates and returns a new Builder instance for constructing MqttConnectOptions.
     *
     * @return a new Builder instance with default configuration values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating configured instances of {@link MqttConnectOptions}.
     * Provides a fluent interface for setting various MQTT connection parameters.
     */
    public static class Builder {
        private boolean automaticReconnect = true;
        private boolean cleanSession = false;
        private String userName;
        private String password;
        private int maxInflight = 200;
        private int connectionTimeout = 30;
        private int keepAliveInterval = 60;

        /**
         * Sets the automatic reconnect option for the MQTT connection.
         *
         * @param automaticReconnect true to enable automatic reconnect, false to disable
         * @return the current Builder instance for method chaining
         */
        public Builder withAutomaticReconnect(boolean automaticReconnect) {
            this.automaticReconnect = automaticReconnect;
            return this;
        }

        /**
         * Sets the clean session option for the MQTT connection.
         *
         * @param cleanSession true to start with a clean session (no persistent state),
         *                     false to maintain session state across connections
         * @return the current Builder instance for method chaining
         */
        public Builder withCleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        /**
         * Sets the username for MQTT connection authentication.
         *
         * @param userName the username for authentication, or null if no authentication is required
         * @return the current Builder instance for method chaining
         */
        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * Sets the password for MQTT connection authentication.
         *
         * @param password the password for authentication, or null if no authentication is required
         * @return the current Builder instance for method chaining
         */
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the maximum number of inflight messages for the MQTT connection.
         * Inflight messages are messages that have been sent but not yet acknowledged.
         *
         * @param maxInflight the maximum number of inflight messages (default: 200)
         * @return the current Builder instance for method chaining
         */
        public Builder withMaxInflight(int maxInflight) {
            this.maxInflight = maxInflight;
            return this;
        }

        /**
         * Sets the connection timeout in seconds for the MQTT connection.
         * This is the maximum time allowed for the connection to complete.
         *
         * @param connectionTimeout the connection timeout in seconds (default: 30)
         * @return the current Builder instance for method chaining
         */
        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Sets the keep alive interval in seconds for the MQTT connection.
         * This defines the maximum time interval between messages sent or received.
         *
         * @param keepAliveInterval the keep alive interval in seconds (default: 60)
         * @return the current Builder instance for method chaining
         */
        public Builder withKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Builds and returns a configured {@link MqttConnectOptions} instance
         * with all the settings specified through the builder methods.
         *
         * @return a fully configured MqttConnectOptions instance
         */
        public MqttConnectOptions build() {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(automaticReconnect);
            options.setCleanSession(cleanSession);

            if (userName != null) {
                options.setUserName(userName);
            }

            if (password != null) {
                options.setPassword(password.toCharArray());
            }

            options.setMaxInflight(maxInflight);
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);

            return options;
        }
    }
}