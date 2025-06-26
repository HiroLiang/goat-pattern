package com.hiro.goat.platform.order;

import com.hiro.goat.core.task.AbstractTask;


public abstract class PlatformOrder<P, R> extends AbstractTask<P, R> {

    protected PlatformOrder() {
        super(null);
    }

}
