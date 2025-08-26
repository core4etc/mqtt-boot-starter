package com.core4etc.mqtt.load;

import java.util.function.Supplier;

@FunctionalInterface
public interface Loader<T> extends Supplier<T> {

    T load() throws Exception;

    @Override
    default T get() {
        try {
            return load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource", e);
        }
    }
}