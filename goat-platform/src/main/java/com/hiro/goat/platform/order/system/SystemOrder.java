package com.hiro.goat.platform.order.system;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.order.PlatformOrder;

public abstract class SystemOrder<P, R> extends PlatformOrder<P, R> {

    protected Platform platform;

    protected SystemOrder() {
        super();
    }

    public void inject(Platform platform) {
        this.platform = platform;
    }

}
