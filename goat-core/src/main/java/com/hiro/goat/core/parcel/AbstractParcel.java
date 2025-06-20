package com.hiro.goat.core.parcel;

import com.hiro.goat.api.parcel.Parcel;

public abstract class AbstractParcel<T> implements Parcel<T> {

    protected T value;

    protected volatile boolean sealed = false;

    @Override
    public void put(T value) {
        if (this.sealed) {
            throw new IllegalStateException("the parcel is sealed");
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
