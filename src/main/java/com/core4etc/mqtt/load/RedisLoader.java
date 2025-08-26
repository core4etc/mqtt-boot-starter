package com.core4etc.mqtt.load;

import com.core4etc.mqtt.listener.RedisKeyExpirationListener;
import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.bean.BeanFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisLoader implements Loader<RedisClient> {

    public RedisClient load() {
        SystemConfig.Core4etc.Redis config = BeanFactory.get(SystemConfig.class).core4etc().redis();
        try (RedisClient redisClient = RedisClient.create("redis://" + (config.password().isEmpty() ? "" : ":" + config.password()) +
                "@" + config.url() + ":" + config.port())) {
            if (config.subscribe()) {
                subscribe();
            }
            return redisClient;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeConnection(SystemConfig.Core4etc.Redis config) {

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
