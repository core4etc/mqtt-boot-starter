package com.core4etc.mqtt.template;

import com.core4etc.mqtt.bean.BeanFactory;
import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisTemplate {

    public <T> void put(String key, T value, Long expireTime) {
        try (StatefulRedisConnection<String, String> connection = BeanFactory.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            String serializedValue = new Gson().toJson(value);
            syncCommands.setex(key, expireTime, serializedValue);
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getDisconnected(String key) {
        try (StatefulRedisConnection<String, String> connection = BeanFactory.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            String strValue = syncCommands.get(key);
            Integer value = null;
            if (strValue != null) {
                value = Integer.valueOf(strValue);
            }
            return (T) value;
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public synchronized String get(String key) {
        try (StatefulRedisConnection<String, String> connection = BeanFactory.get(RedisClient.class).connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            return syncCommands.get(key);
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public void close() {
        BeanFactory.get(RedisClient.class).shutdown();
    }

}
