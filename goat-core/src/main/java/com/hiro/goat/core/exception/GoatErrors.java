package com.hiro.goat.core.exception;

import com.hiro.goat.api.exception.GoatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * General GoatException generator
 */
@Slf4j
public class GoatErrors {

    private static final Map<Class<? extends GoatException>, Function<String, ? extends GoatException>>
            msgConsCache = new ConcurrentHashMap<>();

    private static final Map<Class<? extends GoatException>, BiFunction<String, Throwable, ? extends GoatException>>
            msgConsWithCauseCache = new ConcurrentHashMap<>();

    private static final int MAX_LENGTH = 512;

    @SuppressWarnings("unchecked")
    public static <E extends GoatException> E of(String message, Class<E> exceptionClass) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());
        logger.error("{}", sanitize(message));
        return (E) msgConsCache.computeIfAbsent(exceptionClass, k ->
                getFunction(exceptionClass)).apply(message);
    }

    @SuppressWarnings("unchecked")
    public static <E extends GoatException> E of(String message, Class<E> exceptionClass, Throwable cause) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());
        logger.error("{}", sanitize(message), cause);
        return (E) msgConsWithCauseCache.computeIfAbsent(exceptionClass, k ->
                getBiFunction(exceptionClass)).apply(message, cause);
    }

    @SuppressWarnings("unchecked")
    private static <E extends GoatException> Function<String, GoatException> getFunction(Class<E> exceptionClass) {
        try {
            Constructor<E> constructor = exceptionClass.getConstructor(String.class);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.unreflectConstructor(constructor);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    handle,
                    MethodType.methodType(exceptionClass, String.class)
            );
            return (Function<String, GoatException>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            log.warn("Can't create exception instance for class: {}. Use default exception.", exceptionClass.getName(), e);
            return GoatException::new;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends GoatException> BiFunction<String, Throwable, GoatException> getBiFunction(Class<E> exceptionClass) {
        try {
            Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(BiFunction.class),
                    MethodType.methodType(Object.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(exceptionClass, String.class, Throwable.class)
            );

            return (BiFunction<String, Throwable, GoatException>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            log.warn("Can't create exception instance for class: {}. Use default exception.", exceptionClass.getName(), e);
            return GoatException::new;
        }
    }

    private static String sanitize(String input) {
        if (input == null) return "null";
        String cleaned = input.replaceAll("[\\r\\n\\t]", "_");
        return cleaned.length() > MAX_LENGTH ? cleaned.substring(0, MAX_LENGTH) + "..." : cleaned;
    }

    private static Class<?> getCallerClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            try {
                Class<?> clazz = Class.forName(stackTrace[i].getClassName());
                if (!clazz.equals(GoatErrors.class)) {
                    return clazz;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return GoatErrors.class;
    }

}
