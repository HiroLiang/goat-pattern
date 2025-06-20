package com.hiro.goat.core.parcel;

public class Parcels {

    public static <T> ValueParcel<T> emptyParcel() {
        return new ValueParcel<>();
    }

    public static <T> ValueParcel<T> of(T value) {
        return new ValueParcel<>(value);
    }

}
