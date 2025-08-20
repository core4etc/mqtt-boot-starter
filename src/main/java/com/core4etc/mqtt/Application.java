package com.core4etc.mqtt;

public interface Application extends Loader {

    RedisLoader redis(Loader loader);

    DatasourceLoader datasource(Loader loader);

}
