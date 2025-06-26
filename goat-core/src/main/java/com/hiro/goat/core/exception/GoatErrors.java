package com.hiro.goat.core.exception;

import com.hiro.goat.api.exception.GoatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

/**
 * General GoatException generator
 */
@Slf4j
public class GoatErrors {

    private static final int MAX_LENGTH = 512;

    public static <E extends GoatException> E of(String message, Class<E> exceptionClass) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());
        logger.error("{}", sanitize(message));
        return getErrorInstance(message, exceptionClass);
    }

    public static <E extends GoatException> E of(String message, Class<E> exceptionClass, Throwable cause) {
        Logger logger = LoggerFactory.getLogger(getCallerClass());
        logger.error("{}", sanitize(message), cause);
        return getErrorInstance(message, exceptionClass, cause);
    }

    private static <E extends GoatException> E getErrorInstance(String message, Class<E> exceptionClass) {
        try {
            Constructor<E> constructor = exceptionClass.getConstructor(String.class);
            return constructor.newInstance(message);
        } catch (Exception e) {
            log.warn("Can't create exception instance for class: {}. Use default exception.", exceptionClass.getName(), e);
            throw new GoatException(message);
        }
    }

    private static <E extends GoatException> E getErrorInstance(String message, Class<E> exceptionClass, Throwable cause) {
        try {
            Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
            return constructor.newInstance(message, cause);
        } catch (Exception e) {
            log.warn("Can't create exception instance for class: {}. Use default exception.", exceptionClass.getName(), e);
            throw new GoatException(message, cause);
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
