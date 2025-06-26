package com.hiro.goat.platform.order.system;

import com.hiro.goat.platform.Platform;

public final class CreateOrder extends SystemOrder<Class<? extends Platform>, Platform> {

    private long id = -1;

    @Override
    protected void process() {
        result = platform.create(param);
        id = result.getId();
    }

    @Override
    protected void rollback() {
        platform.destroy(id);
    }

}
