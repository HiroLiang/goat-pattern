package com.hiro.goat.core.task;

import com.hiro.goat.api.task.Task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTask implements Task {

    protected volatile boolean success = false;

    protected volatile boolean rollback = false;

    protected abstract void process();

    protected abstract void rollback();

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

    public void rollback(boolean rollback) {
        this.rollback = rollback;
        if (rollback) {
            log.debug("Task {} will be rollback", this.getClass().getSimpleName());
        } else {
            log.debug("Task {} will be process", this.getClass().getSimpleName());
        }
    }

    protected void preProcess() {
    }

    protected void postProcess() {
    }

}
