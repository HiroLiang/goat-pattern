package com.hiro.goat.core.task;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.TaskException;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public abstract class AbstractTask<P, R> implements Task<P, R> {

    private final BlockingQueue<R> resultQueue = new ArrayBlockingQueue<>(1);

    protected P param;

    protected R result;

    protected volatile boolean success = false;

    protected volatile boolean rollback = false;

    protected abstract void process();

    protected abstract void rollback();

    public AbstractTask(DependencyProvider dependencies) {
    }

    @Override
    public AbstractTask<P, R> initParams(P param) {
        this.param = param;
        return this;
    }

    @Override
    public R getResult() {
        return this.result;
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public void execute() {
        try {
            preProcess();
            if (rollback) {
                rollback();
            } else {
                process();
            }
            this.success = true;
        } catch (Exception e) {
            log.warn("Task {} execute error", this.getClass().getSimpleName(), e);
        }
        postProcess();
    }

    @Override
    public void run() {
        execute();
    }

    public R takeResult() {
        try {
            return this.resultQueue.take();
        } catch (InterruptedException e) {
            throw GoatErrors.of("Thread to take result of \"" + this.getClass().getSimpleName() +
                    "\"is unexcepted interrupted.", TaskException.class, e);
        }
    }

    public AbstractTask<P, R> rollback(boolean rollback) {
        this.rollback = rollback;
        return this;
    }

    protected boolean offerResult() {
        return this.resultQueue.offer(this.result);
    }

    protected void preProcess() {
    }

    protected void postProcess() {
    }

}
