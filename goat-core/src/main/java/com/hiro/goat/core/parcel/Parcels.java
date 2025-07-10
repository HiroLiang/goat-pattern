package com.hiro.goat.core.parcel;

/**
 * Static methods of Parcel. Default using ValueParcel.
 */
public class Parcels {

    /**
     * Get a empty ValueParcel
     *
     * @param <T> Define when assigning value.
     *
     * @return ValueParcel
     */
    public static <T> ValueParcel<T> emptyParcel() {
        return new ValueParcel<>();
    }

    /**
     * Directly Gen a ValueParcel contains item
     *
     * @param value item
     * @param <T>   item type
     *
     * @return ValueParcel
     */
    public static <T> ValueParcel<T> of(T value) {
        return new ValueParcel<>(value);
    }

}
