package com.hiro.goat.api.worker;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface DispatchWorker<T> extends Worker {

    void submit(T task) throws InterruptedException;

    void submit(Collection<T> tasks) throws InterruptedException;

    boolean offer(T task);

    boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException;

    int offer(Collection<T> taskList);

}
