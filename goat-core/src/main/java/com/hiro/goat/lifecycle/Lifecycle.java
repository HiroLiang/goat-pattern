package com.hiro.goat.lifecycle;

public interface Lifecycle {

    void start();

    void stop();

    void pause();

    void resume();

    void destroy();

    boolean isRunning();

    boolean isPaused();

}
