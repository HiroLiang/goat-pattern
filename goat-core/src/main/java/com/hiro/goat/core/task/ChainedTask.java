package com.hiro.goat.core.task;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.api.task.Task;

import java.util.ArrayList;
import java.util.List;

public abstract class ChainedTask<P, R> extends AbstractTask<P, R> {

    protected final List<AbstractTask<?, ?>> tasks = new ArrayList<>();

    public ChainedTask(DependencyProvider dependencies) {
        super(dependencies);
    }

    protected abstract void beforeEach(Task<?, ?> previous, Task<?, ?> current);

    protected abstract void afterEach(Task<?, ?> current);

    @Override
    protected void process() {
        AbstractTask<?, ?> previous = null;
        for (AbstractTask<?, ?> task : tasks) {
            beforeEach(previous, task);

            task.rollback(false).execute();
            previous = task;

            afterEach(task);
        }
    }

    @Override
    protected void rollback() {
        AbstractTask<?, ?> previous = null;
        for (AbstractTask<?, ?> task : tasks) {
            beforeEach(previous, task);

            task.rollback(true).execute();
            previous = task;

            afterEach(task);
        }
    }

}
