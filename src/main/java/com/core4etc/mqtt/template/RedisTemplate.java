package com.core4etc.mqtt.template;

import com.core4etc.mqtt.bean.Bean;
import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * A template class for simplifying Redis operations including data storage, retrieval,
 * and connection management. This class provides a high-level abstraction over
 * the Lettuce Redis client with support for JSON serialization and expiration.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Automatic connection management for each operation</li>
 *   <li>JSON serialization/deserialization using Gson</li>
 *   <li>Support for expiring keys with TTL (time-to-live)</li>
 *   <li>Thread-safe operations with synchronized methods</li>
 *   <li>Simplified get and put operations</li>
 * </ul>
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * RedisTemplate redisTemplate = new RedisTemplate();
 *
 * // Store an object with expiration
 * redisTemplate.put("user:123", userObject, 3600L); // Expires in 1 hour
 *
 * // Retrieve a string value
 * String value = redisTemplate.get("user:123");
 *
 * // Retrieve and deserialize an object
 * User user = redisTemplate.getDisconnected("user:123");
 *
 * // Close the Redis client (typically at application shutdown)
 * redisTemplate.close();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see RedisClient
 * @see StatefulRedisConnection
 * @see Gson
 */
public class RedisTemplate {

    /**
     * Stores a value in Redis with a specified expiration time.
     * The value is serialized to JSON format using Gson before storage.
     *
     * @param <T> the type of the value to store
     * @param key the Redis key under which to store the value
     * @param value the value to store (will be serialized to JSON)
     * @param expireTime the time-to-live in seconds for the key, or null for no expiration
     * @throws RuntimeException if Redis operation fails or serialization fails
     */
    public <T> void put(String key, T value, Long expireTime) {
        try (StatefulRedisConnection<String, String> connection = Bean.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            String serializedValue = new Gson().toJson(value);
            syncCommands.setex(key, expireTime, serializedValue);
        }
    }

    /**
     * Retrieves and deserializes a value from Redis, specifically designed for
     * Integer values. This method is synchronized to ensure thread safety.
     *
     * <p><b>Note:</b> This method is specifically typed for Integer retrieval
     * and uses unchecked casting. For generic object retrieval with proper
     * type safety, consider using a different approach.</p>
     *
     * @param <T> the expected return type (primarily Integer)
     * @param key the Redis key to retrieve
     * @return the deserialized Integer value, or null if the key doesn't exist
     * @throws NumberFormatException if the stored value cannot be parsed as an Integer
     * @throws RuntimeException if Redis operation fails
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getDisconnected(String key) {
        try (StatefulRedisConnection<String, String> connection = Bean.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            String strValue = syncCommands.get(key);
            Integer value = null;
            if (strValue != null) {
                value = Integer.valueOf(strValue);
            }
            return (T) value;
        }
    }

    /**
     * Retrieves a string value from Redis. This method is synchronized to ensure thread safety.
     * Returns the raw string value stored in Redis without any deserialization.
     *
     * @param key the Redis key to retrieve
     * @return the string value associated with the key, or null if the key doesn't exist
     * @throws RuntimeException if Redis operation fails
     */
    public synchronized String get(String key) {
        try (StatefulRedisConnection<String, String> connection = Bean.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            return syncCommands.get(key);
        }
    }

    /**
     * Shuts down the Redis client and releases all resources.
     * This method should typically be called during application shutdown.
     * After calling this method, the Redis client cannot be reused.
     *
     * @throws RuntimeException if shutdown fails
     */
    public void close() {
        Bean.get(RedisClient.class).shutdown();
    }

}