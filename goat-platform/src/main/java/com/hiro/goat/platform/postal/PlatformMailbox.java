package com.hiro.goat.platform.postal;

import com.hiro.goat.api.signature.Verifier;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.PostalException;
import com.hiro.goat.core.postal.Mailbox;
import com.hiro.goat.core.postal.PostalParcel;
import com.hiro.goat.platform.order.PlatformOrder;

import java.util.function.Consumer;

public class PlatformMailbox extends Mailbox<PlatformOrder<?, ?>> {

    private Consumer<PlatformOrder<?, ?>> taskConsumer;

    /**
     * Constructor
     *
     * @param postalCode Identity generated from the postal center
     * @param verifier   signature signer from postal center
     */
    protected PlatformMailbox(long postalCode, Verifier verifier) {
        super(postalCode, verifier);
    }

    @Override
    public void deliver(PostalParcel<PlatformOrder<?, ?>> parcel) {
        if (taskConsumer == null) {
            throw GoatErrors.of("Process setTaskConsumer() before deliver any parcel", PostalException.class);
        }

        taskConsumer.accept(parcel.verifying(this).reveal());
    }

    public PlatformMailbox setTaskConsumer(Consumer<PlatformOrder<?, ?>> taskConsumer) {
        this.taskConsumer = taskConsumer;
        return this;
    }

}
