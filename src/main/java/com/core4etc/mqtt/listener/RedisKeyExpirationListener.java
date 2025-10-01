package com.core4etc.mqtt.listener;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Redis Pub/Sub listener implementation that handles key expiration events.
 * This class listens for Redis key expiration notifications and processes them
 * accordingly. It implements the {@link RedisPubSubListener} interface with
 * String types for both keys and values.
 *
 * <p>This listener is specifically designed to handle key expiration events
 * from Redis, which are typically published through the {@code __keyevent@*__:expired}
 * channel pattern.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * RedisPubSubAdapter&lt;String, String&gt; listener = new RedisKeyExpirationListener();
 * RedisPubSubAsyncCommands&lt;String, String&gt; async = redisClient.connectPubSub().async();
 * async.getStatefulConnection().addListener(listener);
 * async.psubscribe("__keyevent@*__:expired");
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @see RedisPubSubListener
 * @since 1.0
 */
public class RedisKeyExpirationListener implements RedisPubSubListener<String, String> {

    private static final Logger log = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    /**
     * Handles messages received from subscribed channels.
     * This method is called when a message is published to a channel that this
     * listener is subscribed to. It delegates to {@link #handleExpiredKey(String)}
     * for processing the expired key.
     *
     * @param channel the channel from which the message was received
     * @param message the message content (expired key name)
     */
    @Override
    public void message(String channel, String message) {
        handleExpiredKey(message);
    }

    /**
     * Handles messages received from pattern-subscribed channels.
     * This method is called when a message is published to a channel that matches
     * a pattern this listener is subscribed to. It delegates to
     * {@link #handleExpiredKey(String)} for processing the expired key.
     *
     * @param pattern the pattern that matched the channel
     * @param channel the channel from which the message was received
     * @param message the message content (expired key name)
     */
    @Override
    public void message(String pattern, String channel, String message) {
        handleExpiredKey(message);
    }

    /**
     * Called when successfully subscribed to a channel.
     * Logs the subscription event for monitoring purposes.
     *
     * @param channel the channel that was subscribed to
     * @param count the current number of subscriptions for this connection
     */
    @Override
    public void subscribed(String channel, long count) {
        log.info("Subscribed to channel: {}", channel);
    }

    /**
     * Called when successfully subscribed to a channel pattern.
     * Logs the pattern subscription event for monitoring purposes.
     *
     * @param pattern the pattern that was subscribed to
     * @param count the current number of pattern subscriptions for this connection
     */
    @Override
    public void psubscribed(String pattern, long count) {
        log.info("Pattern subscribed: {}", pattern);
    }

    /**
     * Called when successfully unsubscribed from a channel.
     * Logs the unsubscription event for monitoring purposes.
     *
     * @param channel the channel that was unsubscribed from
     * @param count the current number of subscriptions for this connection
     */
    @Override
    public void unsubscribed(String channel, long count) {
        log.info("Unsubscribed from channel: {}", channel);
    }

    /**
     * Called when successfully unsubscribed from a channel pattern.
     * Logs the pattern unsubscription event for monitoring purposes.
     *
     * @param pattern the pattern that was unsubscribed from
     * @param count the current number of pattern subscriptions for this connection
     */
    @Override
    public void punsubscribed(String pattern, long count) {
        log.info("Pattern unsubscribed: {}", pattern);
    }

    /**
     * Handles expired key notifications.
     * This method processes Redis key expiration events. The provided message
     * contains the name of the key that has expired.
     *
     * <p><b>Note:</b> This method currently contains no implementation and should
     * be overridden or implemented to provide specific expiration handling logic.</p>
     *
     * @param message the name of the expired Redis key that needs to be processed
     */
    private void handleExpiredKey(String message) {
        // Implementation needed: Add business logic to handle expired keys
        // Example: Clean up related resources, trigger notifications, etc.
    }
}