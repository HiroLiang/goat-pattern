package com.hiro.goat.platform.order.system;

public final class DestroyOrder extends SystemOrder<Long, Void> {

    @Override
    protected void process() {
        platform.destroy(param);
    }

    @Override
    protected void rollback() {
    }

}
