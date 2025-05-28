package com.hiro.goat.lifecycle;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public abstract class Worker extends AbstractLifecycle{

    private Supplier<ExecutorService> executorFactory = this::createExecutor;

    private ExecutorService executor;

    protected abstract void execute();

    @Override
    protected void onStart() {
        if (executor == null) {
            executor = executorFactory.get();
        }
        executor.submit(this::execute);
    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void onResume() {

    }

    @Override
    protected void onDestroy() {

    }

    protected ExecutorService createExecutor() {
        WorkerConfig config = this.getClass().getAnnotation(WorkerConfig.class);
        if (config != null) {
            return null;
        } else {
            return null;
        }
    }
}
