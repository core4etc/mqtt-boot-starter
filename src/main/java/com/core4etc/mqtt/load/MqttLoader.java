package com.core4etc.mqtt.load;

import com.core4etc.mqtt.bean.Bean;
import com.core4etc.mqtt.config.SystemConfig;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * A {@link Loader} implementation that provides MQTT client connections.
 * This class creates and connects an MQTT client using configuration obtained
 * from the system configuration bean.
 *
 * <p>The MQTT client is configured with the following settings:
 * <ul>
 *   <li>Automatic reconnect enabled</li>
 *   <li>Clean session disabled (persistent sessions)</li>
 *   <li>Username and password authentication</li>
 *   <li>Memory persistence for message storage</li>
 *   <li>Maximum inflight messages set to 200</li>
 * </ul>
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * MqttLoader mqttLoader = new MqttLoader();
 *
 * // Get MQTT client with exception handling
 * try (IMqttClient client = mqttLoader.load()) {
 *     // Use the MQTT client
 * }
 *
 * // Or use with runtime exception wrapping
 * IMqttClient client = mqttLoader.get();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see Loader
 * @see IMqttClient
 * @see MqttClient
 * @see SystemConfig
 * @see Bean
 */
public class MqttLoader implements Loader<IMqttClient> {

    /**
     * Loads, configures, and connects an MQTT client using the system configuration.
     * This method retrieves MQTT configuration details from the system configuration bean,
     * creates a new MQTT client with a generated client ID and memory persistence,
     * and establishes a connection to the MQTT broker with predefined options.
     *
     * <p>The connection URL is constructed in the format:
     * {@code <protocol>://<url>:<port>}</p>
     *
     * <p><b>Connection Options:</b>
     * <ul>
     *   <li>Automatic reconnect: Enabled</li>
     *   <li>Clean session: Disabled (persistent session)</li>
     *   <li>Authentication: Username and password from configuration</li>
     *   <li>Max inflight messages: 200</li>
     * </ul>
     * </p>
     *
     * <p><b>Note:</b> The returned MQTT client is already connected to the broker.
     * The caller is responsible for properly disconnecting and closing the client
     * when it's no longer needed to release resources.</p>
     *
     * @return a connected {@link IMqttClient} instance configured with system settings
     * @throws Exception if any of the following occurs:
     *         <ul>
     *           <li>MQTT configuration is not available or incomplete</li>
     *           <li>Failed to create MQTT client instance</li>
     *           <li>Connection to MQTT broker fails</li>
     *           <li>Authentication fails</li>
     *           <li>Network connectivity issues</li>
     *           <li>Any other MQTT-related exception</li>
     *         </ul>
     * @see SystemConfig.Core4etc.Mqtt
     * @see MqttClient#MqttClient(String, String, org.eclipse.paho.client.mqttv3.MqttClientPersistence)
     * @see MqttOptions
     */
    @Override
    public IMqttClient load() throws Exception {
        SystemConfig.Core4etc.Mqtt config = Bean.get(SystemConfig.class).core4etc().mqtt();
        IMqttClient client = new MqttClient(config.protocol() + "://" + config.url() +
                ":" + config.port(), MqttClient.generateClientId(), new MemoryPersistence());
        client.connect(
                MqttOptions.builder()
                        .withAutomaticReconnect(true)
                        .withCleanSession(false)
                        .withUserName(config.username())
                        .withPassword(config.password())
                        .withMaxInflight(200)
                        .build()
        );
        return client;
    }

    @Override
    public Class<IMqttClient> getType() {
        return IMqttClient.class;
    }
}