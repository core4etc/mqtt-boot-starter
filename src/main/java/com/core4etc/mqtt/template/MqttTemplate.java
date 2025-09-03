package com.core4etc.mqtt.template;

import com.core4etc.mqtt.bean.Bean;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A template class for simplifying MQTT operations including publishing, subscribing,
 * and managing MQTT connections. This class provides a high-level abstraction over
 * the Paho MQTT client with support for automatic reconnection and message handling.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Automatic connection management and reconnection</li>
 *   <li>Simplified publish/subscribe methods with various overloads</li>
 *   <li>Support for multiple message handlers per topic</li>
 *   <li>Thread-safe subscription management</li>
 *   <li>AutoCloseable implementation for resource cleanup</li>
 * </ul>
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * try (MqttTemplate mqttTemplate = new MqttTemplate()) {
 *     // Publish a message
 *     mqttTemplate.publish("test/topic", "Hello MQTT");
 *
 *     // Subscribe with a message handler
 *     mqttTemplate.subscribe("test/topic", message -> {
 *         System.out.println("Received: " + new String(message.getPayload()));
 *     });
 *
 *     // Check connection status
 *     if (mqttTemplate.isConnected()) {
 *         System.out.println("Connected to MQTT broker");
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see IMqttClient
 * @see MqttMessage
 * @see AutoCloseable
 */
public class MqttTemplate implements AutoCloseable {

    private final IMqttClient mqttClient;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<MqttMessage>>> subscribers;
    private final boolean autoReconnect;

    /**
     * Constructs a new MqttTemplate with auto-reconnect enabled.
     * The MQTT client is retrieved from the Bean container.
     */
    public MqttTemplate() {
        this(true);
    }

    /**
     * Constructs a new MqttTemplate with specified auto-reconnect behavior.
     *
     * @param autoReconnect true to enable automatic reconnection, false to disable
     */
    public MqttTemplate(boolean autoReconnect) {
        this.mqttClient = Bean.get(MqttClient.class);
        this.autoReconnect = autoReconnect;
        this.subscribers = new ConcurrentHashMap<>();
    }

    /**
     * Returns whether automatic reconnection is enabled.
     *
     * @return true if auto-reconnect is enabled, false otherwise
     */
    public Boolean isAutoReconnect() {
        return this.autoReconnect;
    }

    /**
     * Publishes a message to the specified topic with default QoS 1 and non-retained flag.
     *
     * @param topic the topic to publish to
     * @param message the message content as a string
     * @throws MqttException if publishing fails
     */
    public void publish(String topic, String message) throws MqttException {
        publish(topic, message.getBytes(), 1, false);
    }

    /**
     * Publishes a message to the specified topic with custom QoS and retained flag.
     *
     * @param topic the topic to publish to
     * @param message the message content as a string
     * @param qos the quality of service level (0, 1, or 2)
     * @param retained true to retain the message on the broker, false otherwise
     * @throws MqttException if publishing fails
     */
    public void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        publish(topic, message.getBytes(), qos, retained);
    }

    /**
     * Publishes a message to the specified topic with default QoS 1 and non-retained flag.
     *
     * @param topic the topic to publish to
     * @param payload the message content as byte array
     * @throws MqttException if publishing fails
     */
    public void publish(String topic, byte[] payload) throws MqttException {
        publish(topic, payload, 1, false);
    }

    /**
     * Publishes a message to the specified topic with custom QoS and retained flag.
     *
     * @param topic the topic to publish to
     * @param payload the message content as byte array
     * @param qos the quality of service level (0, 1, or 2)
     * @param retained true to retain the message on the broker, false otherwise
     * @throws MqttException if publishing fails
     */
    public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(payload);
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        mqttClient.publish(topic, mqttMessage);
    }

    /**
     * Subscribes to a topic with a traditional MQTT callback.
     *
     * @param topic the topic to subscribe to
     * @param callback the MQTT callback to handle messages and connection events
     * @throws MqttException if subscription fails
     */
    public void subscribe(String topic, MqttCallback callback) throws MqttException {
        mqttClient.setCallback(callback);
        mqttClient.subscribe(topic);
    }

    /**
     * Subscribes to a topic with a message handler function and default QoS 1.
     *
     * @param topic the topic to subscribe to
     * @param messageHandler the consumer function to handle incoming messages
     * @throws MqttException if subscription fails
     */
    public void subscribe(String topic, Consumer<MqttMessage> messageHandler) throws MqttException {
        subscribe(topic, 1, messageHandler);
    }

    /**
     * Subscribes to a topic with a message handler function and custom QoS.
     *
     * @param topic the topic to subscribe to
     * @param qos the quality of service level for the subscription
     * @param messageHandler the consumer function to handle incoming messages
     * @throws MqttException if subscription fails
     */
    public void subscribe(String topic, int qos, Consumer<MqttMessage> messageHandler) throws MqttException {
        // Add to subscribers map
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(messageHandler);

        // Subscribe to MQTT topic
        mqttClient.subscribe(topic, qos, (topicName, message) -> {
            CopyOnWriteArrayList<Consumer<MqttMessage>> handlers = subscribers.get(topicName);
            if (handlers != null) {
                handlers.forEach(handler -> handler.accept(message));
            }
        });
    }

    /**
     * Unsubscribes from a topic, removing all message handlers.
     *
     * @param topic the topic to unsubscribe from
     * @throws MqttException if unsubscription fails
     */
    public void unsubscribe(String topic) throws MqttException {
        unsubscribe(topic, null);
    }

    /**
     * Unsubscribes from a topic, optionally removing only a specific message handler.
     * If no specific handler is provided, all handlers for the topic are removed.
     *
     * @param topic the topic to unsubscribe from
     * @param specificHandler the specific handler to remove, or null to remove all handlers
     * @throws MqttException if unsubscription fails
     */
    public void unsubscribe(String topic, Consumer<MqttMessage> specificHandler) throws MqttException {
        if (specificHandler == null) {
            // Remove all handlers for this topic
            subscribers.remove(topic);
            mqttClient.unsubscribe(topic);
        } else {
            // Remove specific handler
            CopyOnWriteArrayList<Consumer<MqttMessage>> handlers = subscribers.get(topic);
            if (handlers != null) {
                handlers.remove(specificHandler);
                if (handlers.isEmpty()) {
                    subscribers.remove(topic);
                    mqttClient.unsubscribe(topic);
                }
            }
        }
    }

    /**
     * Checks if the MQTT client is currently connected to the broker.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Closes the MQTT connection and cleans up resources.
     * Disconnects from the broker, closes the client, and clears all subscriptions.
     *
     * @throws MqttException if disconnection or closing fails
     */
    @Override
    public void close() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
        subscribers.clear();
    }

    /**
     * Reconnects to the MQTT broker if not currently connected.
     *
     * @throws MqttException if reconnection fails
     */
    public void reconnect() throws MqttException {
        if (!isConnected()) {
            mqttClient.reconnect();
        }
    }

    /**
     * Resubscribes to all previously subscribed topics.
     * This is useful after reconnection to restore all subscriptions.
     */
    private void resubscribeAll() {
        subscribers.forEach((topic, handlers) -> {
            try {
                mqttClient.subscribe(topic, 1);
                System.out.println("Resubscribed to topic: " + topic);
            } catch (MqttException e) {
                System.err.println("Failed to resubscribe to topic " + topic + ": " + e.getMessage());
            }
        });
    }

    /**
     * Returns the number of active topic subscriptions.
     *
     * @return the number of subscribed topics
     */
    public int getSubscriptionCount() {
        return subscribers.size();
    }

    /**
     * Returns the MQTT client ID.
     *
     * @return the client ID string
     */
    public String getClientId() {
        return mqttClient.getClientId();
    }

}