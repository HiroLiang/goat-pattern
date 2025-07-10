package com.hiro.goat.api.task;

/**
 * Define basic task execute, statements methods
 *
 * @param <P> initial param
 * @param <R> execute result
 */
public interface Task<P, R> extends Runnable {

    /**
     * To provide task required parameter
     *
     * @param param parameter class
     *
     * @return self
     */
    Task<P, R> initParams(P param);

    /**
     * Execute task
     */
    void execute();

    /**
     * Check is execute success
     *
     * @return true if task success
     */
    boolean isSuccess();

    /**
     * Get task result
     *
     * @return result class
     */
    R getResult();

}
