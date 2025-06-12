package com.hiro.goat.api.worker;

public interface Lifecycle {

    void start();

    void stop();

    void pause();

    void resume();

    void destroy();

    boolean isRunning();

    boolean isPaused();

}
