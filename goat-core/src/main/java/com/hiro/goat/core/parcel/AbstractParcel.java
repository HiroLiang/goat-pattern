package com.hiro.goat.core.parcel;

import com.hiro.goat.api.parcel.Parcel;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.IllegalModifyException;

public abstract class AbstractParcel<T> implements Parcel<T> {

    protected T value;

    protected volatile boolean sealed = false;

    @Override
    public void put(T value) {
        if (this.sealed) {
            throw GoatErrors.of("Can't put a value in the parcel which's sealed.", IllegalModifyException.class);
        }
        this.value = value;
    }

    @Override
    public T reveal() {
        this.sealed = false;
        return this.value;
    }

    @Override
    public void seal() {
        this.sealed = true;
    }

    @Override
    public boolean isSealed() {
        return this.sealed;
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }

}
