package com.core4etc.mqtt;

public record SystemConfig(Core4etc core4etc) {
    public record Core4etc(Application application, Mqtt mqtt, Database database, Redis redis, Log log) {
        public record Application(String name){};
        public record Mqtt(String url, String protocol, String port, String username, String password){}
        public record Database(String url, String port, String name, String username, String password){}
        public record Redis(String url, String port, String password, Boolean subscribe){}
        public record Log(File file) {
            public record File(String name, String path){}
        }
    }
}
