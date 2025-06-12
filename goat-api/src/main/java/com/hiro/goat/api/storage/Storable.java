package com.hiro.goat.api.storage;

import java.util.Set;

public interface Storable<K> {

    <T> void store(K key, T obj);

    <T> T retrieve(K key, Class<T> clazz);

    void inherit(Storable<K> other);

    void exportTo(Storable<K> other);

    Set<K> keys();

    void remove(K key);

    boolean contains(K key);

}
