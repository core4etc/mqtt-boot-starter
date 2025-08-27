package com.core4etc.mqtt.template;

import com.core4etc.mqtt.bean.BeanFactory;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MqttTemplate implements AutoCloseable {

    private final IMqttClient mqttClient;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<MqttMessage>>> subscribers;
    private final boolean autoReconnect;

    public MqttTemplate() {
        this(true);
    }

    public MqttTemplate(boolean autoReconnect) {
        this.mqttClient = BeanFactory.get(IMqttClient.class);
        this.autoReconnect = autoReconnect;
        this.subscribers = new ConcurrentHashMap<>();
        setupConnectionCallback();
    }

    /**
     * Publish a message to specified topic
     */
    public void publish(String topic, String message) throws MqttException {
        publish(topic, message.getBytes(), 1, false);
    }

    public void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        publish(topic, message.getBytes(), qos, retained);
    }

    public void publish(String topic, byte[] payload) throws MqttException {
        publish(topic, payload, 1, false);
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(payload);
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        mqttClient.publish(topic, mqttMessage);
    }

    /**
     * Subscribe to a topic with callback
     */
    public void subscribe(String topic, Consumer<MqttMessage> messageHandler) throws MqttException {
        subscribe(topic, 1, messageHandler);
    }

    public void subscribe(String topic, int qos, Consumer<MqttMessage> messageHandler) throws MqttException {
        // Add to subscribers map
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                .add(messageHandler);

        // Subscribe to MQTT topic
        mqttClient.subscribe(topic, qos, (topicName, message) -> {
            CopyOnWriteArrayList<Consumer<MqttMessage>> handlers = subscribers.get(topicName);
            if (handlers != null) {
                handlers.forEach(handler -> handler.accept(message));
            }
        });
    }

    /**
     * Unsubscribe from topic
     */
    public void unsubscribe(String topic) throws MqttException {
        unsubscribe(topic, null);
    }

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
     * Check if connected
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Disconnect and close connection
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
     * Reconnect if disconnected
     */
    public void reconnect() throws MqttException {
        if (!isConnected()) {
            mqttClient.reconnect();
        }
    }

    /**
     * Setup connection callbacks for auto-reconnect
     */
    private void setupConnectionCallback() {
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("MQTT " + (reconnect ? "reconnected" : "connected") + " to: " + serverURI);

                // Resubscribe to all topics on reconnect
                if (reconnect) {
                    resubscribeAll();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT connection lost: " + cause.getMessage());
                if (autoReconnect) {
                    try {
                        Thread.sleep(5000); // Wait before reconnect
                        reconnect();
                    } catch (Exception e) {
                        System.err.println("Auto-reconnect failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // Handled by the subscription callback
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Message delivery complete
            }
        });
    }

    /**
     * Resubscribe to all topics after reconnect
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
     * Get number of active subscriptions
     */
    public int getSubscriptionCount() {
        return subscribers.size();
    }

    /**
     * Get client ID
     */
    public String getClientId() {
        return mqttClient.getClientId();
    }

}