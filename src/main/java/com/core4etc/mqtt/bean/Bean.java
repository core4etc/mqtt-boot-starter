package com.core4etc.mqtt.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton bean container that manages object instances using a concurrent map.
 * This class provides thread-safe creation, retrieval, and management of bean instances
 * with support for dependency injection through constructor parameters.
 *
 * <p>The container uses a {@link ConcurrentHashMap} to store singleton instances
 * keyed by their class types. It supports recursive dependency injection when
 * creating instances with parameterized constructors.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * // Create or get a singleton instance
 * MyService service = Bean.get(MyService.class);
 *
 * // Check if a bean exists
 * boolean exists = Bean.exists(MyService.class);
 *
 * // Remove a bean from the container
 * Bean.remove(MyService.class);
 * }
 * </pre>
 *
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Bean {

    private static final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     * This class should be used statically.
     */
    private Bean() {
    }

    /**
     * Creates or returns an existing singleton instance of the specified object.
     * If an instance of the object's class doesn't exist in the container,
     * the provided object is stored and returned. If an instance already exists,
     * the existing instance is returned.
     *
     * @param <T> the type of the object
     * @param obj the object instance to create or use as singleton
     * @return the singleton instance of the specified object's class
     * @throws IllegalArgumentException if the provided object is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        return (T) instances.computeIfAbsent(obj.getClass(), k -> obj);
    }

    /**
     * Retrieves or creates a singleton instance of the specified class.
     * If an instance doesn't exist, a new instance is created using the constructor
     * with the most parameters (supporting dependency injection). If the instance
     * already exists, the existing instance is returned.
     *
     * @param <T> the type of the class
     * @param clazz the class for which to get or create an instance
     * @return the singleton instance of the specified class
     * @throws IllegalArgumentException if the provided class is null
     * @throws RuntimeException if instance creation fails due to:
     *         <ul>
     *           <li>No constructors found</li>
     *           <li>Instantiation failure</li>
     *           <li>Illegal access</li>
     *           <li>Invocation target exception</li>
     *           <li>Security restrictions</li>
     *         </ul>
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        return (T) instances.computeIfAbsent(clazz, Bean::createInstanceSafe);
    }

    /**
     * Removes the singleton instance of the specified class from the container.
     *
     * @param clazz the class whose instance should be removed
     * @throws IllegalArgumentException if no instance exists for the specified class
     */
    public static void remove(Class<?> clazz) {
        if (!instances.containsKey(clazz)) {
            throw new IllegalArgumentException("Not created bean from this class");
        }
        instances.remove(clazz);
    }

    /**
     * Checks whether a singleton instance exists for the specified object's class.
     *
     * @param <T> the type of the object
     * @param obj the object to check for existence in the container
     * @return true if an instance exists for the object's class, false otherwise
     */
    public static <T> Boolean exists(T obj) {
        return instances.containsKey(obj.getClass());
    }

    /**
     * Safely creates a new instance of the specified class using reflection.
     * This method selects the constructor with the most parameters and recursively
     * resolves dependencies by calling {@link #get(Class)} for each parameter type.
     *
     * @param <T> the type of the class
     * @param clazz the class to instantiate
     * @return a new instance of the specified class
     * @throws RuntimeException if instance creation fails for any reason
     */
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