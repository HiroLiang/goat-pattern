package com.hiro.goat.core.postal.model;

import com.hiro.goat.api.signature.Verifier;
import com.hiro.goat.core.postal.Mailbox;
import com.hiro.goat.core.postal.PostalParcel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMailbox extends Mailbox<String> {

    @Getter
    @Setter
    private String word;

    TestMailbox(long postalCode, Verifier verifier) {
        super(postalCode, verifier);
    }

    @Override
    public void deliver(PostalParcel<String> parcel) {
        this.word = parcel.verifying(this).reveal();
        log.info("TestMailBox receive parcel contains: {}", this.word);
    }

}
