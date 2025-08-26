package com.core4etc.mqtt;

import com.core4etc.mqtt.bean.BeanFactory;
import com.core4etc.mqtt.config.SystemConfig;
import com.core4etc.mqtt.load.*;
import io.lettuce.core.RedisClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.sql.Connection;
import java.util.Optional;
import java.util.function.Supplier;

public final class Application {
    private static SystemConfig systemConfig;
    private static RedisClient redisClient;
    private static Connection datasourceConnection;
    private static IMqttClient mqttClient;

    private Application() {
        // Utility class - prevent instantiation
    }

    // Builder Pattern for fluent configuration
    public static class Builder {
        private Loader<SystemConfig> systemLoader = new SystemLoader();
        private Loader<RedisClient> redisLoader;
        private Loader<Connection> datasourceLoader;
        private Loader<IMqttClient> mqttLoader;

        public Builder withSystem(Loader<SystemConfig> loader) {
            this.systemLoader = loader;
            return this;
        }

        public Builder withRedis(Loader<RedisClient> loader) {
            this.redisLoader = loader;
            return this;
        }

        public Builder withRedisIfAbsent(Loader<RedisClient> loader) {
            if (this.redisLoader == null) {
                this.redisLoader = loader;
            }
            return this;
        }

        public Builder withDatasource(Loader<Connection> loader) {
            this.datasourceLoader = loader;
            return this;
        }

        public Builder withDatasourceIfAbsent(Loader<Connection> loader) {
            if (this.datasourceLoader == null) {
                this.datasourceLoader = loader;
            }
            return this;
        }

        public Builder withMqtt(Loader<IMqttClient> loader) {
            this.mqttLoader = loader;
            return this;
        }

        public Builder withMqttIfAbsent(Loader<IMqttClient> loader) {
            if (this.mqttLoader == null) {
                this.mqttLoader = loader;
            }
            return this;
        }

        public void run() throws Exception {
            Application.initialize(this);
        }
    }

    public static Builder configure() {
        return new Builder();
    }

    private static void initialize(Builder config) throws Exception {
        // SystemConfig is mandatory
        systemConfig = config.systemLoader.load();
        BeanFactory.create(systemConfig);

        // Optional components
        initializeOptionalComponent(config.redisLoader,
                RedisLoader::new, client -> {
                    redisClient = client;
                    BeanFactory.create(client);
                });

        initializeOptionalComponent(config.datasourceLoader,
                DatasourceLoader::new, connection -> {
                    datasourceConnection = connection;
                    BeanFactory.create(connection);
                });

        initializeOptionalComponent(config.mqttLoader,
                MqttLoader::new, client -> {
                    mqttClient = client;
                    BeanFactory.create(client);
                });
    }

    private static <T> void initializeOptionalComponent(
            Loader<T> customLoader,
            Supplier<Loader<T>> defaultLoaderSupplier,
            ThrowingConsumer<T> initializer) throws Exception {

        if (customLoader != null) {
            T component = customLoader.load();
            initializer.accept(component);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    // Getters with Optional return types
    public static SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public static Optional<RedisClient> getRedisClient() {
        return Optional.ofNullable(redisClient);
    }

    public static Optional<Connection> getDatasourceConnection() {
        return Optional.ofNullable(datasourceConnection);
    }

    public static Optional<IMqttClient> getMqttClient() {
        return Optional.ofNullable(mqttClient);
    }

    // Utility method for quick startup with default configuration
    public static void run() throws Exception {
        configure().run();
    }
}