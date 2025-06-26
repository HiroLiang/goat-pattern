package com.hiro.goat.core.storage;

import com.hiro.goat.api.storage.Storable;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.IllegalModifyException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Storage<K> implements Storable<K> {

    protected final Map<K, Object> stored = new ConcurrentHashMap<>();

    @Override
    public <T> void store(K key, T obj) {
        this.stored.put(key, obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T retrieve(K key, Class<T> clazz) {
        Object value = this.stored.get(key);
        if (value == null) return null;
        if (!clazz.isInstance(value)) {
            throw GoatErrors.of("Stored value is not of type " + clazz.getName() + ".", IllegalModifyException.class);
        }
        return (T) value;
    }

    @Override
    public void inherit(Storable<K> other) {
        for (K key : other.keys()) {
            this.stored.put(key, other.retrieve(key, Object.class));
        }
    }

    @Override
    public void exportTo(Storable<K> other) {
        this.stored.forEach(other::store);
    }

    @Override
    public Set<K> keys() {
        return this.stored.keySet();
    }

    @Override
    public void remove(K key) {
        this.stored.remove(key);
    }

    @Override
    public boolean contains(K key) {
        return this.stored.containsKey(key);
    }

}
