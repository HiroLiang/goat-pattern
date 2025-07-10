package com.hiro.goat.core.parcel;

import lombok.NoArgsConstructor;

/**
 * Basic parcel with value.
 *
 * @param <T> value type
 */
@NoArgsConstructor
public class ValueParcel<T> extends AbstractParcel<T> {

    /**
     * Constructor:
     *
     * @param value item to bup in
     */
    public ValueParcel(T value) {
        this.value = value;
    }

}
