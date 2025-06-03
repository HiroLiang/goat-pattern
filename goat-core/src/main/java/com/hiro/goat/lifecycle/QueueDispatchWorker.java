package com.hiro.goat.lifecycle;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QueueDispatchWorker<T> extends Worker {

    protected final Supplier<ExecutorService> executorFactory;

    protected final Consumer<T> taskConsumer;

    protected final BlockingDeque<T> tasks = new LinkedBlockingDeque<>();

    protected ExecutorService executor;

    /**
     * Create a new QueueDispatchWorker with the given task consumer.
     *
     * @param taskConsumer the task consumer to execute the tasks.
     */
    public QueueDispatchWorker(Consumer<T> taskConsumer) {
        this.taskConsumer = taskConsumer;
        this.executorFactory = this::createExecutor;
    }

    /**
     * Create a new QueueDispatchWorker with the given task consumer and executor factory.
     *
     * @param consumer the task consumer to execute the tasks.
     * @param factory the executor factory to create the executor.
     */
    public QueueDispatchWorker(Consumer<T> consumer, Supplier<ExecutorService> factory) {
        this.taskConsumer = consumer;
        this.executorFactory = factory;
    }

    @Override
    protected void work() {
        this.initExecutor();

        while (running && !paused) {
            try {
                T task = this.tasks.take();
                executor.submit(() -> taskConsumer.accept(task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void submit(T task) throws InterruptedException {
        this.tasks.put(task);
    }

    public void submitBatch(Collection<T> tasks) throws InterruptedException {
        for (T task : tasks) {
            this.submit(task);
        }
    }

    public boolean offer(T task) {
        return this.tasks.offer(task);
    }

    public int offerBatch(Collection<T> taskList) {
        int index = 0;
        for (T task : taskList) {
            if (!this.offer(task)) break;
            index++;
        }
        return index;
    }

    public boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException {
        return this.tasks.offer(task, timeout, unit);
    }

    protected ExecutorService createExecutor() {
        DispatchWorkerConfig config = this.getClass().getAnnotation(DispatchWorkerConfig.class);
        if (config != null) {
            return new ThreadPoolExecutor(
                    config.coreSize(),
                    config.maxSize(),
                    config.keepAliveTime(),
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(config.queueCapacity()),
                    runnable -> {
                        Thread t = new Thread(runnable);
                        t.setName("worker-" + this.getClass().getSimpleName() + "-" + t.getId());
                        return t;
                    }
            );
        } else {
            return Executors.newCachedThreadPool();
        }
    }

    private void initExecutor() {
        if (executor == null) {
            this.executor = Objects.requireNonNull(executorFactory.get(), "ExecutorService cannot be null");
        }
    }
}
