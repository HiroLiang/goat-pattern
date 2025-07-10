package com.hiro.goat.core.postal;

import com.hiro.goat.api.signature.Verifier;

import lombok.Getter;

/**
 * Consumer for postal parcels
 * 1. Unique postal code in used postal network
 * 2. Verifier to verify for unsealing postal parcel is required
 * 3. Define consume method while using MailBox
 *
 * @param <T> parcel contain type
 */
public abstract class Mailbox<T> {

    /**
     * unique identity
     */
    @Getter
    protected final long postalCode;

    /**
     * to verify the signature in postal parcel
     */
    protected final Verifier verifier;

    /**
     * Constructor
     */
    protected Mailbox(long postalCode, Verifier verifier) {
        this.postalCode = postalCode;
        this.verifier = verifier;
    }

    /**
     * deliver parcel to this mailbox
     *
     * @param parcel postal parcel contains the same type with mailbox
     */
    public abstract void deliver(PostalParcel<T> parcel);

    /**
     * Use verifier in mailbox to verify sealed PostalParcel
     *
     * @param parcel PostalParcel
     *
     * @return true if verified
     */
    public boolean verify(PostalParcel<T> parcel) {
        return this.verifier.verify(parcel);
    }

}
