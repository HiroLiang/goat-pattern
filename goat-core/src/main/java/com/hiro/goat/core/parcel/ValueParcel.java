package com.hiro.goat.core.parcel;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValueParcel<T> extends AbstractParcel<T> {

    public ValueParcel(T value) {
        this.value = value;
    }

    @Override
    public void put(T value) {
        if (this.sealed) {
            throw new IllegalStateException("the parcel is sealed");
        }
        this.value = value;
    }

    @Override
    public void seal() {
        this.sealed = true;
    }

    @Override
    public T reveal() {
        this.sealed = false;
        return this.value;
    }
}
