package com.hiro.goat.api.parcel;

public interface Parcel<T> {

    void put(T value);

    void seal();

    T reveal();

    boolean isSealed();

    boolean isEmpty();

}
