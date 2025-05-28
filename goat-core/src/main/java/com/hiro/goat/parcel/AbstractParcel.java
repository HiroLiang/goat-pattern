package com.hiro.goat.parcel;

public abstract class AbstractParcel<T> implements Parcel<T> {

    protected T value;

    protected volatile boolean sealed = false;

    @Override
    public boolean isSealed() {
        return this.sealed;
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }
}
