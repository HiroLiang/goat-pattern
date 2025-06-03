package com.hiro.goat.parcel;

public class Parcels {

    public static <T> ValueParcel<T> of(T value) {
        return new ValueParcel<>(value);
    }

}
