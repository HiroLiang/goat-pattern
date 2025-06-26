package com.hiro.goat.core.postal.model;

import com.hiro.goat.api.identity.IdentityGenerator;
import com.hiro.goat.core.postal.Mailbox;
import com.hiro.goat.core.postal.PostalCenter;
import com.hiro.goat.core.utils.SnowflakeGenerator;

public class TestPostalCenter extends PostalCenter<String> {

    private final IdentityGenerator identityGenerator;

    public TestPostalCenter(String secret) {
        super(secret);
        this.identityGenerator = new SnowflakeGenerator(1L);
    }

    @Override
    protected long createPostalCode() {
        return identityGenerator.nextId();
    }

    @Override
    protected Mailbox<String> createMailBox(long postalCode) {
        return new TestMailbox(postalCode, signer);
    }

}
