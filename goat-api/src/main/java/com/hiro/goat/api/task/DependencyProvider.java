package com.hiro.goat.api.task;

/**
 * Used in any place needed dependencies
 */
public interface DependencyProvider {

    /**
     * Get the required dependency class
     *
     * @param clazz required class
     * @param <T>   required class
     *
     * @return required object
     */
    <T> T use(Class<T> clazz);

}
