package com.core4etc.mqtt.load;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MqttOptions {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean automaticReconnect = true;
        private boolean cleanSession = false;
        private String userName;
        private String password;
        private int maxInflight = 200;
        private int connectionTimeout = 30;
        private int keepAliveInterval = 60;

        public Builder withAutomaticReconnect(boolean automaticReconnect) {
            this.automaticReconnect = automaticReconnect;
            return this;
        }

        public Builder withCleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withMaxInflight(int maxInflight) {
            this.maxInflight = maxInflight;
            return this;
        }

        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder withKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public MqttConnectOptions build() {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(automaticReconnect);
            options.setCleanSession(cleanSession);

            if (userName != null) {
                options.setUserName(userName);
            }

            if (password != null) {
                options.setPassword(password.toCharArray());
            }

            options.setMaxInflight(maxInflight);
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);

            return options;
        }
    }

}
