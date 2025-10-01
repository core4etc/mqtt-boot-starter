package com.core4etc.mqtt.util;

import com.core4etc.mqtt.template.MqttTemplate;
import org.eclipse.paho.client.mqttv3.MqttCallback;

/**
 * An abstract base class for MQTT callback implementations that provides
 * automatic connection recovery and MQTT template integration.
 * This class implements the {@link MqttCallback} interface and handles
 * connection loss with automatic reconnection capabilities.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Automatic connection loss detection and recovery</li>
 *   <li>Integrated {@link MqttTemplate} for simplified MQTT operations</li>
 *   <li>Configurable auto-reconnect behavior with delay</li>
 *   <li>Error logging for connection issues</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <p>Extend this class and implement the required MQTT callback methods:</p>
 * <ul>
 *   <li>{@link MqttCallback#messageArrived(String, org.eclipse.paho.client.mqttv3.MqttMessage)}</li>
 *   <li>{@link MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)}</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * public class MyCallback extends Callback {
 *
 *     @Override
 *     public void messageArrived(String topic, MqttMessage message) {
 *         // Handle incoming messages
 *         System.out.println("Received: " + new String(message.getPayload()));
 *     }
 *
 *     @Override
 *     public void deliveryComplete(IMqttDeliveryToken token) {
 *         // Handle message delivery confirmation
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see MqttCallback
 * @see MqttTemplate
 */
public abstract class Callback implements MqttCallback {

    private MqttTemplate mqttTemplate = new MqttTemplate();

    /**
     * Returns the {@link MqttTemplate} instance associated with this callback.
     * The MQTT template provides simplified methods for publishing messages,
     * subscribing to topics, and other MQTT operations.
     *
     * @return the MQTT template instance used by this callback
     */
    public MqttTemplate getMqttTemplate() {
        return this.mqttTemplate;
    }

    /**
     * Handles MQTT connection loss events and attempts automatic reconnection
     * if auto-reconnect is enabled in the MQTT template.
     *
     * <p><b>Reconnection Behavior:</b></p>
     * <ul>
     *   <li>Logs the connection loss event to standard error</li>
     *   <li>Waits 5 seconds before attempting reconnection</li>
     *   <li>Attempts to reconnect using {@link MqttTemplate#reconnect()}</li>
     *   <li>Logs any reconnection failures to standard error</li>
     * </ul>
     *
     * <p><b>Note:</b> Reconnection only occurs if {@link MqttTemplate#isAutoReconnect()}
     * returns true. The method uses a fixed 5-second delay before reconnection attempts.</p>
     *
     * @param cause the throwable that caused the connection loss, providing
     *              details about what went wrong with the connection
     */
    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
        if (mqttTemplate.isAutoReconnect()) {
            try {
                Thread.sleep(5000); // Wait before reconnect
                mqttTemplate.reconnect();
            } catch (Exception e) {
                System.err.println("Auto-reconnect failed: " + e.getMessage());
            }
        }
    }

}