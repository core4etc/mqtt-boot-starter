package com.core4etc.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttLoader implements Loader {

    @Override
    public void load() throws Exception {
        SystemConfig.Core4etc.Mqtt config = BeanFactory.get(SystemLoader.class).getConfig().core4etc().mqtt();
        try {
            MqttClient client = new MqttClient(config.protocol() + "://" + config.url() +
                    ":" + config.port(), MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            client.subscribe("/#");

        } catch (MqttException e) {
//            log.warn(e.getMessage(), e);
        }
    }

    private MqttConnectOptions getOptions(SystemConfig.Core4etc.Mqtt config) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setUserName(config.username());
        options.setPassword(config.password().toCharArray());
        options.setMaxInflight(200);
        return options;
    }

}
