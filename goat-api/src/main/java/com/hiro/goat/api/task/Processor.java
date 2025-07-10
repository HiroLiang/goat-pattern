package com.hiro.goat.api.task;

/**
 * Consume task to give task execute transaction or redirect to retry or else...
 *
 * @param <T> Consumable task class
 */
public interface Processor<T extends Task<?, ?>> {

    /**
     * Execute task
     *
     * @param task Consumable task
     */
    void process(T task);

}
