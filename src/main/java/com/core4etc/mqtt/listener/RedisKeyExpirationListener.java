package com.core4etc.mqtt.listener;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisKeyExpirationListener implements RedisPubSubListener<String, String> {

    private static final Logger log = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    @Override
    public void message(String channel, String message) {
        handleExpiredKey(message);
    }

    @Override
    public void message(String pattern, String channel, String message) {
        handleExpiredKey(message);
    }

    @Override
    public void subscribed(String channel, long count) {
        log.info("Subscribed to channel: {}", channel);
    }

    @Override
    public void psubscribed(String pattern, long count) {
        log.info("Pattern subscribed: {}", pattern);
    }

    @Override
    public void unsubscribed(String channel, long count) {
        log.info("Unsubscribed from channel: {}", channel);
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        log.info("Pattern unsubscribed: {}", pattern);
    }

    private void handleExpiredKey(String message) {
        /*SystemConfig.Core4etc.Mqtt config = BeanFactory.get(SystemLoader.class).getConfig().core4etc().mqtt();
        if (message.replaceAll(Constant.DASH, Constant.SLASH).contains(TopicPattern.Topic.DISCONNECTED_TOPIC)) {
            log.info("{}: {}", message.replaceAll(Constant.DASH, Constant.SLASH), BooleanType.TRUE.getValue());
            if (config.mqtt().lwDisconnectedPublish()) {
                MqttBroker.getInstance().publish(message.replaceAll(Constant.DASH, Constant.SLASH), BooleanType.TRUE.getValue());
            }
        }*/
    }

}
