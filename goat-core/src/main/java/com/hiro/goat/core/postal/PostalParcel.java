package com.hiro.goat.core.postal;

import com.hiro.goat.api.signature.Signable;
import com.hiro.goat.api.signature.Signer;
import com.hiro.goat.core.parcel.ValueParcel;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class PostalParcel<T> extends ValueParcel<T> implements Signable {

    @Getter
    private final long sender;

    @Getter
    private final long recipient;

    @Getter
    private final String group;

    @Getter
    private final RecipientType recipientType;

    private String signature = "";

    protected PostalParcel(long sender, long recipient, RecipientType recipientType) {
        this.sender = sender;
        this.recipient = recipient;
        this.group = "";
        this.recipientType = recipientType;
    }

    protected PostalParcel(long sender, String group, RecipientType recipientType) {
        this.sender = sender;
        this.recipient = 0;
        this.group = group;
        this.recipientType = recipientType;
    }

    @Override
    public String signableData() {
        return sender + ":" + recipient + ":" + group + ":" + value.hashCode();
    }

    @Override
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String getSignature() {
        return this.signature;
    }

    @Override
    public void seal() {
        if (this.sealed) {
            throw new IllegalStateException("postal parcel can't be double sealed");
        }
        if (StringUtils.isBlank(this.signature)) {
            throw new IllegalStateException("signature should be signed before sealing");
        }
        super.seal();
    }

    @Override
    public T reveal() {
        if (isSealed()) {
            throw new IllegalStateException("parcel is sealed");
        }

        return super.reveal();
    }

    public PostalParcel<T> verifying(Mailbox<T> mailBox) {
        if (mailBox.verify(this)) {
            this.sealed = false;
        }
        return this;
    }

    protected PostalParcel<T> signing(PostalCenter<T> postalCenter, Signer signer) {
        if (postalCenter.isRegistered(this.sender)) {
            signer.sign(this);
            return this;
        }
        throw new IllegalArgumentException("Sender is not registered");
    }

}
