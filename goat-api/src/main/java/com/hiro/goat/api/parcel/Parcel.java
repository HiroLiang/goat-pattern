package com.hiro.goat.api.parcel;

/**
 *
 * @param <T> item to pack
 */
public interface Parcel<T> {

    void put(T value);

    void seal();

    T reveal();

    boolean isSealed();

    boolean isEmpty();

}
