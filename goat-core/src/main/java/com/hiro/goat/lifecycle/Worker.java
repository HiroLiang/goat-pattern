package com.hiro.goat.lifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Worker extends AbstractLifecycle {

    protected Thread workerThread;

    protected abstract void work();

    @Override
    protected void onStart() {
        if (this.workerThread == null || !this.workerThread.isAlive()) {
            this.workerThread = createWorkerThread();
            this.workerThread.start();

        } else {
            log.warn("Worker: \"{}\" was still alive from the previous stop/pause. " +
                    "Please verify the stop/pause logic.", this.workerThread.getName());
        }
    }

    @Override
    protected void onStop() {
        if (this.workerThread != null) {
            String threadName = this.workerThread.getName();

            this.workerThread.interrupt();
            this.workerThread = null;

            log.info("Worker: \"{}\" stopped.", threadName);
        }
    }

    @Override
    protected void onPause() {
        onStop();
    }

    @Override
    protected void onResume() {
        onStart();
    }

    @Override
    protected void onDestroy() {
    }

    protected Thread createWorkerThread() {
        Thread thread = new Thread(this::work);
        thread.setName("worker-" + this.getClass().getSimpleName() + "-" + this.workerThread.getId());
        thread.setDaemon(false);

        return thread;
    }
}
