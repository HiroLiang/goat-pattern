package com.hiro.goat.platform.postal;

import com.hiro.goat.api.identity.IdentityGenerator;
import com.hiro.goat.core.postal.PostalCenter;
import com.hiro.goat.core.utils.SnowflakeGenerator;
import com.hiro.goat.platform.order.PlatformOrder;

import lombok.Getter;

public class PlatformPostalCenter extends PostalCenter<PlatformOrder<?, ?>> {

    @Getter
    protected final IdentityGenerator idGenerator;

    protected PlatformPostalCenter(long deviceId, String secret) {
        super(secret);
        this.idGenerator = new SnowflakeGenerator(deviceId);
    }

    @Override
    public PlatformMailbox register() {
        return (PlatformMailbox) super.register();
    }

    @Override
    protected long createPostalCode() {
        return idGenerator.nextId();
    }

    @Override
    protected PlatformMailbox createMailBox(long postalCode) {
        return new PlatformMailbox(postalCode, this.signer);
    }

}
