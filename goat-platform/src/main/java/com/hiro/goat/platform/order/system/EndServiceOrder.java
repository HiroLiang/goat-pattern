package com.hiro.goat.platform.order.system;

import com.hiro.goat.platform.Platform;

public final class EndServiceOrder extends SystemOrder<Class<? extends Platform>, Void> {

    @Override
    protected void process() {
        platform.destroy(param);
    }

    @Override
    protected void rollback() {
    }

}
