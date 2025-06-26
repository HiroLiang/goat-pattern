package com.hiro.goat.platform.model.task;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.core.task.AbstractTask;

public class TestTask extends AbstractTask<Void, Boolean> {

    public TestTask(DependencyProvider dependencies) {
        super(dependencies);
    }

    @Override
    protected void preProcess() {
        result = false;
    }

    @Override
    protected void process() {
        result = true;
    }

    @Override
    protected void rollback() {
        result = false;
    }

}
