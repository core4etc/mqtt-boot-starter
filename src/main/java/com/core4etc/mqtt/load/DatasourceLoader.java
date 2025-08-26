package com.core4etc.mqtt.load;

import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.bean.BeanFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatasourceLoader implements Loader<Connection> {

    @Override
    public Connection load() throws Exception {
        SystemConfig.Core4etc.Database config = BeanFactory.get(SystemConfig.class).core4etc().database();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://" + config.url() + ":" + config.port() + "/" + config.name(),
                config.username(), config.password())) {
            return connection;
        }
    }

}
