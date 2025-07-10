package com.hiro.goat.core.task;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.TaskException;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Define default task mechanism
 *
 * @param <P> initial param
 * @param <R> execute result
 */
@Slf4j
public abstract class AbstractTask<P, R> implements Task<P, R> {

    /**
     * Provide a queue for reactive requirement
     */
    private final BlockingQueue<R> resultQueue = new ArrayBlockingQueue<>(1);

    /**
     * Task input
     */
    protected P param;

    /**
     * Task output
     */
    protected R result;

    /**
     * success flag
     */
    protected volatile boolean success = false;

    /**
     * execute rollback or process flag
     */
    protected volatile boolean rollback = false;

    /**
     * positive process
     */
    protected abstract void process();

    /**
     * rollback process
     */
    protected abstract void rollback();

    /**
     * Constructor:
     *
     * @param ignore let implement class default provide DependencyProvider
     */
    public AbstractTask(DependencyProvider ignore) {
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

    /**
     * For who need this task result, can let thread block.
     * If you use Sink, ignore this method
     *
     * @return Result
     */
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

    public void offerResult() {
        if (!this.resultQueue.offer(this.result))
            log.warn("Double offer result in task: \"{}\".", this.getClass().getName());
    }

    protected void preProcess() {
    }

    protected void postProcess() {
    }

}
