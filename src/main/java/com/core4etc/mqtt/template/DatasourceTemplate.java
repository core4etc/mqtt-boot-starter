package com.core4etc.mqtt.template;

import com.core4etc.mqtt.load.DatasourceLoader;
import com.core4etc.mqtt.bean.BeanFactory;

import java.sql.Connection;

public class DatasourceTemplate {

    public static Connection getConnection() {
        Connection connection = BeanFactory.get(Connection.class);
        try {
            if (connection.isClosed()) {
                BeanFactory.remove(Connection.class);
                BeanFactory.get(DatasourceLoader.class).load();
            }
            return BeanFactory.get(Connection.class);
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    public static void close() {
        try {
            BeanFactory.get(Connection.class).close();
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
        }
    }

}
