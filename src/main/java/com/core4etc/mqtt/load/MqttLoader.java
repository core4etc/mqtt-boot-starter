package com.core4etc.mqtt.load;

import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.bean.BeanFactory;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttLoader implements Loader<IMqttClient> {

    @Override
    public IMqttClient load() throws Exception {
        SystemConfig.Core4etc.Mqtt config = BeanFactory.get(SystemConfig.class).core4etc().mqtt();
        try {
            IMqttClient client = new MqttClient(config.protocol() + "://" + config.url() +
                    ":" + config.port(), MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            client.subscribe("/#");
            return client;

        } catch (MqttException e) {
//            log.warn(e.getMessage(), e);
            e.printStackTrace();
            return null;
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
