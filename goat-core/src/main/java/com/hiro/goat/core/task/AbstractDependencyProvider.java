package com.hiro.goat.core.task;

import com.hiro.goat.api.task.DependencyProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractDependencyProvider implements DependencyProvider {

    protected final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();

    public AbstractDependencyProvider() {
        dependencies.put(Void.class, null);
    }

    @Override
    public <T> T use(Class<T> clazz) {
        return clazz.cast(dependencies.get(clazz));
    }

}
