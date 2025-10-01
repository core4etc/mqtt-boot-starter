package com.core4etc.mqtt.load;

import com.core4etc.mqtt.bean.Bean;
import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.listener.RedisKeyExpirationListener;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@link Loader} implementation that provides Redis client connections.
 * This class creates a Redis client using configuration obtained from the system
 * configuration bean and optionally sets up a subscription for key expiration events.
 *
 * <p>The Redis connection URL is constructed based on the configuration parameters:</p>
 * <ul>
 *   <li>URL and port from configuration</li>
 *   <li>Password authentication if provided</li>
 *   <li>Automatic subscription to key expiration events if enabled in configuration</li>
 * </ul>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Creates Redis client with proper authentication</li>
 *   <li>Optional subscription to Redis key expiration events</li>
 *   <li>Uses a dedicated thread for pub/sub operations</li>
 *   <li>Automatic cleanup on shutdown</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * RedisLoader redisLoader = new RedisLoader();
 *
 * // Get Redis client
 * RedisClient redisClient = redisLoader.load();
 *
 * // Use the Redis client for operations
 * StatefulRedisConnection<String, String> connection = redisClient.connect();
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see Loader
 * @see RedisClient
 * @see SystemConfig
 * @see Bean
 * @see RedisKeyExpirationListener
 */
public class RedisLoader implements Loader<RedisClient> {

    /**
     * Loads and returns a Redis client configured with system settings.
     * This method retrieves Redis configuration from the system configuration bean,
     * creates a Redis client with proper authentication, and optionally sets up
     * a subscription for key expiration events if enabled in the configuration.
     *
     * <p>The connection URL is constructed in the format:
     * {@code redis://[:password@]url:port}</p>
     *
     * <p>If subscription is enabled in the configuration, this method will
     * automatically start a background thread to listen for key expiration events
     * using the {@link RedisKeyExpirationListener}.</p>
     *
     * @return a configured {@link RedisClient} instance
     * @throws RuntimeException if Redis client creation fails or if subscription
     *         setup encounters an error
     */
    public RedisClient load() throws Exception {
        SystemConfig.Core4etc.Redis config = Bean.get(SystemConfig.class).core4etc().redis();
        RedisClient redisClient;
        try {
            redisClient = RedisClient.create("redis://" + (config.password() == null ? "" : ":" + config.password()) +
                    "@" + config.url() + ":" + config.port());
        } catch (Exception e) {
            throw new Exception(e.getCause());
        }
        if (config.subscribe()) {
            subscribe();
        }
        return redisClient;
    }

    /**
     * Sets up a subscription to Redis key expiration events in a background thread.
     * This method creates a dedicated single-thread executor to handle Redis pub/sub
     * operations for monitoring key expiration events.
     *
     * <p>The subscription:
     * <ul>
     *   <li>Uses a pub/sub connection from the Redis client</li>
     *   <li>Adds a {@link RedisKeyExpirationListener} to handle expiration events</li>
     *   <li>Subscribes to the pattern {@code __keyevent@*__:expired} to receive all key expiration events</li>
     *   <li>Registers a shutdown hook to properly close the pub/sub connection</li>
     * </ul>
     * </p>
     *
     * <p><b>Note:</b> This method runs asynchronously in a separate thread and any
     * exceptions encountered during subscription setup are printed to stderr.</p>
     */
    private void subscribe() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                StatefulRedisPubSubConnection<String, String> pubSubConnection =
                        Bean.get(RedisClient.class).connectPubSub();
                RedisKeyExpirationListener listener = new RedisKeyExpirationListener();
                pubSubConnection.addListener(listener);
                // Subscribe to the key expiration event channel
                pubSubConnection.sync().psubscribe("__keyevent@*__:expired");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    pubSubConnection.close();
                }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Class<RedisClient> getType() {
        return RedisClient.class;
    }
}