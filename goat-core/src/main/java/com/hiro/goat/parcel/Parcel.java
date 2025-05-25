package com.hiro.goat.parcel;

public interface Parcel<T> {

    void put(T value);

    void seal();

    boolean isSealed();

    T reveal();

}
