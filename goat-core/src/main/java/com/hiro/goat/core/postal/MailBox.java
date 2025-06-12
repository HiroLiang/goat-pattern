package com.hiro.goat.core.postal;

import lombok.Getter;
import lombok.Setter;

public abstract class MailBox<T> {

    @Getter
    protected final String postalCode;

    @Getter
    @Setter
    protected String group = "";

    public MailBox(String postalCode) {
        this.postalCode = postalCode;
    }

    public abstract void deliver(PostalParcel<T> parcel);

}
