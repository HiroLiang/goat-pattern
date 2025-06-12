package com.hiro.goat.core.postal;

import com.hiro.goat.core.parcel.ValueParcel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public class PostalParcel<T> extends ValueParcel<T> {

    private final String sender;

    private final String recipient;

    private final RecipientType recipientType;

    public PostalParcel(String sender, String recipient, RecipientType recipientType) {
        this.sender = sender;
        this.recipient = recipient;
        this.recipientType = recipientType;
    }

}
