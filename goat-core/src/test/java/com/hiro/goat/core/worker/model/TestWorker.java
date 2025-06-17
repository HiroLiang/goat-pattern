package com.hiro.goat.core.worker.model;

import com.hiro.goat.core.worker.AbstractWorker;
import lombok.Getter;

public class TestWorker extends AbstractWorker {

    @Getter
    private volatile long curren = 0L;

    @Override
    public void work() {
        while (running && !paused && !Thread.currentThread().isInterrupted()) {
            this.curren = System.currentTimeMillis();

            try {

                // noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
