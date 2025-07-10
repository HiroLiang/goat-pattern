package com.hiro.goat.core.postal;

import com.hiro.goat.api.signature.Signable;
import com.hiro.goat.api.signature.Signer;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.IllegalModifyException;
import com.hiro.goat.core.parcel.ValueParcel;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Parcel for a postal center has same <T>
 *
 * @param <T> type in parcel (consumed Object of the postal center)
 */
@Slf4j
@ToString
public class PostalParcel<T> extends ValueParcel<T> implements Signable {

    /**
     * Sender's postal code
     */
    @Getter
    private final long sender;

    /**
     * Recipient's postal code (if RecipientType is MailBox)
     */
    @Getter
    private final long recipient;

    /**
     * Group of recipients (if RecipientType is Group)
     */
    @Getter
    private final String group;

    /**
     * Way to deliver
     */
    @Getter
    private final RecipientType recipientType;

    /**
     * Signature to check the parcel is from the same postal center
     */
    private String signature = "";

    /**
     * Constructor:
     * 1. For type All and mailbox
     *
     * @param sender        Sender's postal code
     * @param recipient     Recipient's postal code
     * @param recipientType Way to deliver
     */
    protected PostalParcel(long sender, long recipient, RecipientType recipientType) {
        this.sender = sender;
        this.recipient = recipient;
        this.group = "";
        this.recipientType = recipientType;
    }

    /**
     * Constructor:
     * 2. For type Group
     *
     * @param sender        Sender's postal code
     * @param group         Group of recipients
     * @param recipientType Way to deliver
     */
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
            throw GoatErrors.of("Postal parcel can't be double sealed.", IllegalModifyException.class);
        }
        if (StringUtils.isBlank(this.signature)) {
            throw GoatErrors.of("signature should be signed before sealing", IllegalModifyException.class);
        }
        super.seal();
    }

    @Override
    public T reveal() {
        if (isSealed()) {
            throw GoatErrors.of("Postal parcel can't be revealed after sealed.", IllegalModifyException.class);
        }

        return super.reveal();
    }

    /**
     * Verify is the mailbox can reveal this parcel
     *
     * @param mailBox Mailbox who wants to reveal this parcel
     *
     * @return unsealed parcel
     */
    public PostalParcel<T> verifying(Mailbox<T> mailBox) {
        if (mailBox.verify(this)) {
            this.sealed = false;
        }
        return this;
    }

    /**
     * Generate a signature for this parcel
     *
     * @param postalCenter Postal center
     * @param signer       signature generator
     *
     * @return signed parcel
     */
    protected PostalParcel<T> signing(PostalCenter<T> postalCenter, Signer signer) {
        if (postalCenter.isRegistered(this.sender)) {
            signer.sign(this);
            return this;
        }
        throw GoatErrors.of("Sender is not registered", IllegalModifyException.class);
    }

}
