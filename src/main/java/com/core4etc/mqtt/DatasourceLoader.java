package com.core4etc.mqtt;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatasourceLoader implements Loader {

    @Override
    public void load() throws Exception {
        SystemConfig.Core4etc.Database config = BeanFactory.get(SystemLoader.class).getConfig().core4etc().database();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://" + config.url() + ":" + config.port() + "/" + config.name(),
                config.username(), config.password())) {
            BeanFactory.create(connection);
        }
    }

}
