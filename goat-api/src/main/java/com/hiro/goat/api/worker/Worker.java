package com.hiro.goat.api.worker;

/**
 * A system should keep doing work() when started.
 */
public interface Worker extends Lifecycle {

    /**
     * method to do
     */
    void work();

}
