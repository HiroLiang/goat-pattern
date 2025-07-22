package com.hiro.goat.core.task;

import com.hiro.goat.api.task.DependencyProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Define dependency provider
 * 1. Collect some dependencies for particular service usage
 */
public abstract class AbstractDependencyProvider implements DependencyProvider {

    /**
     * Dependencies holder
     */
    protected final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    public AbstractDependencyProvider() {
        dependencies.put(Void.class, Void.TYPE);
    }

    /**
     * Method to get instance if class
     *
     * @param clazz target class
     * @param <T>   dependency class
     *
     * @return target
     */
    protected abstract <T> T load(Class<T> clazz);

    @Override
    public <T> T use(Class<T> clazz) {
        return clazz.cast(dependencies.computeIfAbsent(clazz, this::load));
    }

}
