package com.hiro.goat.platform.model.task;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.core.task.AbstractTask;

public class TestTask extends AbstractTask<Void, Boolean> {

    public TestTask(DependencyProvider dependencies) {
        super(dependencies);
    }

    @Override
    protected void process() {

    }

    @Override
    protected void rollback() {

    }

}
