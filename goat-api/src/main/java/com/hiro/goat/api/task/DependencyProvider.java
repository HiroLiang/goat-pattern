package com.hiro.goat.api.task;

public interface DependencyProvider {

    <T> T use(Class<T> clazz);

}
