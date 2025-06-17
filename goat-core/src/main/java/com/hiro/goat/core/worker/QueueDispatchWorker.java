package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.DispatchWorker;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class QueueDispatchWorker<T> extends AbstractWorker implements DispatchWorker<T> {

    /**
     * Tasks to be dispatch
     */
    protected final BlockingDeque<T> tasks;

    /**
     * Customized Executor generator (If delivered form outer)
     */
    protected final Supplier<ExecutorService> executorFactory;

    /**
     * Execute task consumer for dispatched tasks
     */
    protected ExecutorService executor;

    /**
     * Processor method for dispatched tasks (If delivered from outer)
     */
    protected final Consumer<T> taskConsumer;

    /**
     * Class extends QueueDispatchWorker<T> should override method \"void processTask(T task)\"
     */
    protected QueueDispatchWorker() {
        this.taskConsumer = this::processTask;
        this.tasks = createTaskQueue();
        this.executorFactory = this::createExecutor;
    }

    /**
     * Create a new QueueDispatchWorker with the given task consumer.
     *
     * @param taskConsumer the task consumer to execute the tasks.
     */
    public QueueDispatchWorker(Consumer<T> taskConsumer) {
        this.taskConsumer = taskConsumer;
        this.tasks = createTaskQueue();
        this.executorFactory = this::createExecutor;
    }

    /**
     * Create a new QueueDispatchWorker with the given task consumer and executor factory.
     *
     * @param taskConsumer the task consumer to execute the tasks.
     * @param executorFactory the executor factory to create the executor.
     */
    public QueueDispatchWorker(Consumer<T> taskConsumer, Supplier<ExecutorService> executorFactory) {
        this.taskConsumer = taskConsumer;
        this.tasks = createTaskQueue();
        this.executorFactory = executorFactory;
    }

    @Override
    public void work() {
        initExecutor();

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

    @Override
    public void submit(T task) throws InterruptedException {
        this.tasks.put(task);
    }

    @Override
    public void submit(Collection<T> tasks) throws InterruptedException {
        for (T task : tasks) {
            this.submit(task);
        }
    }

    @Override
    public boolean offer(T task) {
        return this.tasks.offer(task);
    }

    @Override
    public boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException {
        return this.tasks.offer(task, timeout, unit);
    }

    @Override
    public int offer(Collection<T> taskList) {
        int index = 0;
        for (T task : taskList) {
            if (!this.offer(task)) break;
            index++;
        }
        return index;
    }

    protected ExecutorService createExecutor() {
        DispatchExecutor config = this.getClass().getAnnotation(DispatchExecutor.class);
        if (config != null) {
            return new ThreadPoolExecutor(
                    config.coreSize(),
                    config.maxSize(),
                    config.keepAliveTime(),
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(config.queueCapacity()),
                    runnable -> {
                        Thread t = new Thread(runnable);
                        t.setName("queue-worker-" + this.getClass().getSimpleName() + "-" + t.getId());
                        return t;
                    }
            );
        } else {
            return Executors.newCachedThreadPool();
        }
    }

    protected BlockingDeque<T> createTaskQueue() {
        DispatchDequeue config = this.getClass().getAnnotation(DispatchDequeue.class);
        if (config != null) {
            return new LinkedBlockingDeque<>(config.size());
        } else {
            return new LinkedBlockingDeque<>();
        }
    }

    protected void processTask(T task) {
        log.warn("Task Consumer of class \"{}\" should be override or be passed in constructor.", task.getClass().getSimpleName());
    }

    protected void initExecutor() {
        if (executor == null) {
            this.executor = Objects.requireNonNull(executorFactory.get(), "ExecutorService cannot be null");
        }
    }

}
