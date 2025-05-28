package com.hiro.goat.lifecycle;

public abstract class AbstractLifecycle implements Lifecycle {

    protected volatile boolean running = false;

    protected volatile boolean paused = false;

    protected abstract void onStart();

    protected abstract void onStop();

    protected abstract void onPause();

    protected abstract void onResume();

    protected abstract void onDestroy();

    @Override
    public synchronized void start() {
        if (this.running) return;

        beforeStart();
        this.running = true;
        this.paused = false;
        onStart();
    }

    @Override
    public synchronized void stop() {
        if (!this.running) return;

        beforeStop();
        this.running = false;
        this.paused = false;
        onStop();
    }

    @Override
    public synchronized void pause() {
        if (!this.running || this.paused) return;

        beforePause();
        paused = true;
        onPause();
    }

    @Override
    public synchronized void resume() {
        if (!this.running || !this.paused) return;

        beforeResume();
        paused = false;
        onResume();
    }

    @Override
    public synchronized void destroy() {

        beforeDestroy();
        if (running) stop();
        onDestroy();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    protected void beforeStart() {}

    protected void beforeStop() {}

    protected void beforePause() {}

    protected void beforeResume() {}

    protected void beforeDestroy() {}
}
