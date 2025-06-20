package com.hiro.goat.platform.platform;

import com.hiro.goat.api.identity.IdentityGenerator;

public abstract class Platform {

    private final long ID;

    private final IdentityGenerator idGenerator;

    protected Platform(IdentityGenerator idGenerator) {
        this.ID = idGenerator.nextId();
       this.idGenerator = idGenerator;
    }

    public abstract Platform create();
}
