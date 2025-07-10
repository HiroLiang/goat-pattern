package com.hiro.goat.api.worker;

/**
 * Define a system with the lifecycle like a daemon system.
 */
public interface Lifecycle {

    /**
     * Start system
     */
    void start();

    /**
     * Stop system (usually clean up process)
     */
    void stop();

    /**
     * Pause system (temporarily pause process)
     */
    void pause();

    /**
     * Resume system from pause statement
     */
    void resume();

    /**
     * Destroy system (clean up all the mechanism object.
     */
    void destroy();

    /**
     * Check is system running
     *
     * @return true if state in [start, pause]
     */
    boolean isRunning();

    /**
     * Check is system be paused
     *
     * @return true if state in [pause]
     */
    boolean isPaused();

}
