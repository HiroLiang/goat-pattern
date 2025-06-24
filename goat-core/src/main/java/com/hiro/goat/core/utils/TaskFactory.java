package com.hiro.goat.core.utils;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.TaskException;
import com.hiro.goat.core.task.AbstractTask;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TaskFactory {

    private final DependencyProvider provider;

    private final Map<Class<? extends AbstractTask<?, ?>>,
            Function<DependencyProvider, ? extends AbstractTask<?, ?>>> cache = new ConcurrentHashMap<>();

    public TaskFactory(DependencyProvider provider) {
        this.provider = provider;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractTask<?, ?>> T getInstance(Class<T> taskClass) {
        try {
            Function<DependencyProvider, T> creator =
                    (Function<DependencyProvider, T>) cache.computeIfAbsent(taskClass, this::createLambda);
            return creator.apply(provider);
        } catch (Throwable e) {
            throw GoatErrors.of("Failed to instantiate task: " + taskClass.getName(), TaskException.class, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractTask<?, ?>> Function<DependencyProvider, T> createLambda(Class<T> taskClass) {
        try {
            Constructor<T> constructor = taskClass.getConstructor(DependencyProvider.class);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.unreflectConstructor(constructor);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    handle,
                    MethodType.methodType(taskClass, DependencyProvider.class)
            );

            return (Function<DependencyProvider, T>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            throw GoatErrors.of("Cannot build constructor for: " + taskClass.getName(), TaskException.class, e);
        }
    }

}
