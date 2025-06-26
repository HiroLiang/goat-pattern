package com.hiro.goat.core.parcel;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValueParcel<T> extends AbstractParcel<T> {

    public ValueParcel(T value) {
        this.value = value;
    }
    
}
