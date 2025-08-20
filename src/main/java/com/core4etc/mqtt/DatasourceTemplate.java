package com.core4etc.mqtt;

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
            return getConnection();
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
