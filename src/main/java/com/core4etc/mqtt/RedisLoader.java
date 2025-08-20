package com.core4etc.mqtt;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisLoader implements Loader {

    public void load() {
        SystemConfig config = BeanFactory.get(SystemLoader.class).getConfig();
        initializeConnection(config.core4etc().redis());
    }

    private void initializeConnection(SystemConfig.Core4etc.Redis config) {
        try (RedisClient redisClient = RedisClient.create("redis://" + (config.password().isEmpty() ? "" : ":" + config.password()) +
                "@" + config.url() + ":" + config.port())) {
            BeanFactory.create(redisClient);
            if (config.subscribe()) {
                subscribe();
            }

        }
    }

    private void subscribe() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                StatefulRedisPubSubConnection<String, String> pubSubConnection =
                        BeanFactory.get(RedisClient.class).connectPubSub();
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


}
