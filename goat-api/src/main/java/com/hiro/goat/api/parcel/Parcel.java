package com.hiro.goat.api.parcel;

/**
 * @param <T> item to pack
 */
public interface Parcel<T> {

    /**
     * Provide item
     *
     * @param value item
     */
    void put(T value);

    /**
     * Sealing parcel. It should edit the statement or lock the put method.
     */
    void seal();

    /**
     * unseal and get item in the parcel.
     *
     * @return item
     */
    T reveal();

    /**
     * Return true if sealed.
     *
     * @return boolean
     */
    boolean isSealed();

    /**
     * Return true if item exist.
     *
     * @return boolean
     */
    boolean isEmpty();

}
