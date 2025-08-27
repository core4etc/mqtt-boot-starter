package com.core4etc.mqtt.load;

import com.core4etc.mqtt.bean.BeanFactory;
import com.core4etc.mqtt.config.SystemConfig;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttLoader implements Loader<IMqttClient> {

    @Override
    public IMqttClient load() throws Exception {
        SystemConfig.Core4etc.Mqtt config = BeanFactory.get(SystemConfig.class).core4etc().mqtt();
        IMqttClient client = new MqttClient(config.protocol() + "://" + config.url() +
                ":" + config.port(), MqttClient.generateClientId(), new MemoryPersistence());
        client.connect(
                MqttOptions.builder()
                        .withAutomaticReconnect(true)
                        .withCleanSession(false)
                        .withUserName(config.username())
                        .withPassword(config.password())
                        .withMaxInflight(200)
                        .build()
        );
        client.subscribe("/#");
        return client;
    }

}