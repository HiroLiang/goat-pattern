package com.hiro.goat.api.worker;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A daemon system would keep dispatching Object T
 *
 * @param <T> type to be dispatched
 */
public interface DispatchWorker<T> extends Worker {

    /**
     * Offer dispatcher task.
     *
     * @param task Object to be dispatched
     *
     * @throws InterruptedException if the dispatcher can't consume this task.
     */
    void submit(T task) throws InterruptedException;

    /**
     * Offer the dispatcher a list of tasks.
     *
     * @param tasks Objects to be dispatched
     *
     * @throws InterruptedException if the dispatcher can't consume any one of the tasks.
     */
    void submit(Collection<T> tasks) throws InterruptedException;

    /**
     * Offer dispatcher task.
     *
     * @param task Object to be dispatched
     *
     * @return true if the dispatcher consumes the task.
     */
    boolean offer(T task);

    /**
     * Offer the dispatcher a task after a period.
     *
     * @param task    Object to be dispatched
     * @param timeout wait period
     * @param unit    time unit
     *
     * @return true if all tasks be consumed
     * @throws InterruptedException any problem while waiting period.
     */
    boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Offer the dispatcher a list of tasks.
     *
     * @param taskList Objects to be dispatched
     *
     * @return Offer success if return equals collection size.
     */
    int offer(Collection<T> taskList);

    /**
     * Check is dispatcher still has tasks
     *
     * @return true if there are tasks not be consumed.
     */
    boolean hasTask();

}
