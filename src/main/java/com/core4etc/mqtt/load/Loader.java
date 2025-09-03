package com.core4etc.mqtt.load;

import java.util.function.Supplier;

/**
 * A functional interface that extends {@link Supplier} to provide loading capabilities
 * with exception handling. This interface is designed for operations that may throw
 * checked exceptions during the loading process, converting them to runtime exceptions
 * when accessed through the Supplier interface.
 *
 * <p>Implementations should define the {@link #load()} method which may throw
 * exceptions, while the {@link #get()} method provides exception handling by
 * wrapping any thrown exceptions in {@link RuntimeException}.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * // Create a loader that reads from a file
 * Loader<String> fileLoader = () -> Files.readString(Path.of("config.txt"));
 *
 * // Use with exception handling
 * String content = fileLoader.load(); // throws IOException
 *
 * // Or use with runtime exception wrapping
 * String content = fileLoader.get(); // throws RuntimeException if IOException occurs
 * }
 * </pre>
 *
 * @param <T> the type of results supplied by this loader
 * @author Mohammad Khosrojerdi m.khosrojerdi.d@gmail.com
 * @version 1.0
 * @since 1.0
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface Loader<T> extends Supplier<T> {

    /**
     * Loads and returns a result, potentially throwing a checked exception.
     * This is the primary method that implementations should define for the
     * loading operation.
     *
     * @return the loaded result
     * @throws Exception if an error occurs during the loading operation.
     *         The specific exception type depends on the implementation.
     */
    T load() throws Exception;

    /**
     * Returns the loaded result, wrapping any checked exceptions in a
     * {@link RuntimeException}. This method implements the {@link Supplier#get()}
     * contract and provides exception handling for the {@link #load()} method.
     *
     * <p>If the {@link #load()} method throws an exception, it will be caught
     * and wrapped in a RuntimeException with a descriptive message and the
     * original exception as the cause.</p>
     *
     * @return the loaded result
     * @throws RuntimeException if the {@link #load()} method throws an exception.
     *         The RuntimeException contains the original exception as its cause.
     */
    @Override
    default T get() {
        try {
            return load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource", e);
        }
    }

}