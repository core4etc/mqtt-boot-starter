package com.core4etc.mqtt.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {

    private static final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private BeanFactory() {}

    @SuppressWarnings("unchecked")
    public static <T> T create(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        // If an instance of this class exists, return it; otherwise put `obj`
        return (T) instances.computeIfAbsent(obj.getClass(), k -> obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        return (T) instances.computeIfAbsent(clazz, BeanFactory::createInstanceSafe);
    }

    public static void remove(Class<?> clazz) {
        if (!instances.containsKey(clazz)) {
            throw new IllegalArgumentException("Not created bean from this class");
        }
        instances.remove(clazz);
    }

    public static <T> Boolean exists(T obj) {
        return instances.containsKey(obj.getClass());
    }

    private static <T> T createInstanceSafe(Class<T> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();

            if (constructors.length == 0) {
                throw new RuntimeException("No constructors found for " + clazz.getName());
            }

            Constructor<?> selected = constructors[0];
            for (Constructor<?> ctor : constructors) {
                if (ctor.getParameterCount() > selected.getParameterCount()) {
                    selected = ctor;
                }
            }

            selected.setAccessible(true);
            Class<?>[] paramTypes = selected.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = get(paramTypes[i]); // recursive injection
            }

            return (T) selected.newInstance(params);

        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | SecurityException e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }
    }

}
