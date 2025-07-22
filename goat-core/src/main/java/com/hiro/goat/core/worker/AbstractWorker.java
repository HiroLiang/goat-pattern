package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.Worker;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.IllegalModifyException;

import lombok.extern.slf4j.Slf4j;

/**
 * Define worker's lifecycle mechanism
 */
@Slf4j
public abstract class AbstractWorker extends AbstractLifecycle implements Worker {

    /**
     * Thread to do work
     */
    protected Thread workerThread;

    @Override
    protected void onStart() {
        if (this.workerThread == null || !this.workerThread.isAlive()) {
            this.workerThread = createWorkerThread();
            this.workerThread.start();
            log.info("Worker: \"{}\" started.", this.workerThread.getName());
        } else {
            log.warn("Worker: \"{}\" was still alive from the previous stop/pause. " + "Please verify the stop/pause logic.",
                    this.workerThread.getName());

            throw GoatErrors.of("Worker: \"" + this.workerThread.getName() + "\" was already started.", IllegalModifyException.class);
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

    /**
     * Create and bind thread to work
     *
     * @return Thread
     */
    protected Thread createWorkerThread() {
        Thread thread = new Thread(this::work);
        thread.setName(this.getClass().getSimpleName() + "-worker-" + thread.getId());
        thread.setDaemon(false);

        return thread;
    }

}
