package com.hiro.goat.core.task;

import com.hiro.goat.api.chain.ChainList;
import com.hiro.goat.api.task.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask implements Task {

    protected volatile boolean success = false;

    protected abstract void process();

    @Override
    public ChainList<?> chaining() {
        return new TaskChain().chain(this);
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public void execute() {
        preProcess();
        try {
            process();
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

    protected void preProcess() {}
    protected void postProcess() {}
}
