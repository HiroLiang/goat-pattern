package com.hiro.goat.platform.utils;

import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PlatformFactory {

    private final PlatformPostalCenter postalCenter;

    private final Map<Class<? extends Platform>, BiFunction<PlatformPostalCenter, Long, ? extends Platform>> caches =
            new ConcurrentHashMap<>();

    public PlatformFactory(PlatformPostalCenter postalCenter) {
        this.postalCenter = postalCenter;
    }

    @SuppressWarnings("unchecked")
    public <T extends Platform> T newInstance(Class<? extends Platform> platformClazz, long parentId) {
        try {
            BiFunction<PlatformPostalCenter, Long, T> creator =
                    (BiFunction<PlatformPostalCenter, Long, T>) caches.computeIfAbsent(platformClazz, this::createLambda);
            return creator.apply(postalCenter, parentId);
        } catch (Throwable e) {
            throw GoatErrors.of("Failed to instantiate platform: " + platformClazz.getName(), PlatformException.class, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Platform> BiFunction<PlatformPostalCenter, Long, T> createLambda(Class<T> platformClazz) {
        try {
            Constructor<T> constructor = platformClazz.getConstructor(PlatformPostalCenter.class, Long.class);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.unreflectConstructor(constructor);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(BiFunction.class),
                    MethodType.methodType(Object.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(platformClazz, PlatformPostalCenter.class, Long.class)
            );

            return (BiFunction<PlatformPostalCenter, Long, T>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            throw GoatErrors.of("Cannot build constructor for: " + platformClazz.getName(), PlatformException.class, e);
        }
    }

}
