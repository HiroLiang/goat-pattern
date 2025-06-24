package com.hiro.goat.platform.order;

import com.hiro.goat.core.task.AbstractTask;

import lombok.Getter;

public abstract class PlatformOrder<P, R> extends AbstractTask<P, R> {

    @Getter
    protected final Order order;

    public PlatformOrder(Order order) {
        super(null);
        this.order = order;
    }

}
