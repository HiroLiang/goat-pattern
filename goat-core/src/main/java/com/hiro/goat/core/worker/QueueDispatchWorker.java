package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.DispatchWorker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
     * Define rule to
     */
    protected final ResidualSchedule schedule;

    /**
     * Alert settings while DispatchWorker is overloaded
     */
    protected final AlertParams alertParams;

    /**
     * Customized Executor generator (If delivered form outer)
     */
    protected final Supplier<ExecutorService> executorFactory;

    /**
     * Processor method for dispatched tasks (If delivered from outer)
     */
    protected final Consumer<T> taskConsumer;

    /**
     * Execute task consumer for dispatched tasks
     */
    protected ExecutorService executor;

    /**
     * Is the dispatcher accepting tasks?
     */
    @Getter
    protected volatile boolean acceptTask = true;

    /**
     * Is the dispatcher overloaded?
     */
    @Getter
    protected volatile boolean overloaded = false;

    /**
     * Class extends QueueDispatchWorker<T> should override method \"void processTask(T task)\"
     */
    protected QueueDispatchWorker() {
        this.taskConsumer = this::processTask;
        this.tasks = defineTaskQueue();
        this.schedule = defineSchedule();
        this.executorFactory = this::defineExecutor;
        this.alertParams = initAlertParams();
    }

    /**
     * Create a new QueueDispatchWorker with the given task consumer.
     *
     * @param taskConsumer the task consumer to execute the tasks.
     */
    public QueueDispatchWorker(Consumer<T> taskConsumer) {
        this.taskConsumer = taskConsumer;
        this.tasks = defineTaskQueue();
        this.schedule = defineSchedule();
        this.executorFactory = this::defineExecutor;
        this.alertParams = initAlertParams();
    }

    /**
     * Create a new QueueDispatchWorker with the given task consumer and executor factory.
     *
     * @param taskConsumer    the task consumer to execute the tasks.
     * @param executorFactory the executor factory to create the executor.
     */
    public QueueDispatchWorker(Consumer<T> taskConsumer, Supplier<ExecutorService> executorFactory) {
        this.taskConsumer = taskConsumer;
        this.tasks = defineTaskQueue();
        this.schedule = defineSchedule();
        this.executorFactory = executorFactory;
        this.alertParams = initAlertParams();
    }

    /**
     * Worker thread dispatch tasks to executor. If overloaded, process alert() method.
     */
    @Override
    public void work() {
        initExecutor();

        while (running && !paused) {
            try {
                T task = this.tasks.take();

                if (overloaded()) {
                    this.overloaded = true;
                    alert();
                } else {
                    this.overloaded = false;
                }

                executor.submit(() -> taskConsumer.accept(task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Submit a task. Throw while the task queue is full.
     *
     * @param task Consumer accepts object.
     */
    @Override
    public void submit(T task) throws InterruptedException {
        if (this.acceptTask) {
            this.tasks.put(task);
        } else {
            throw new InterruptedException();
        }
    }

    /**
     * Submit tasks. Throw while the task queue is full.
     *
     * @param tasks Consumer accepts collection.
     */
    @Override
    public void submit(Collection<T> tasks) throws InterruptedException {
        for (T task : tasks) {
            this.submit(task);
        }
    }

    /**
     * Offer a task. Return is queue accept this task.
     *
     * @param task Consumer accepts object.
     *
     * @return is offer success
     */
    @Override
    public boolean offer(T task) {
        if (this.acceptTask) {
            return this.tasks.offer(task);
        } else {
            return false;
        }
    }

    /**
     * Delay offer task. Offer a task after a particular time.
     *
     * @param task    Consumer accepts object.
     * @param timeout wait time
     * @param unit    wait time unit
     *
     * @return is offer success
     * @throws InterruptedException if waiting been interrupted
     */
    @Override
    public boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException {
        if (this.acceptTask) {
            return this.tasks.offer(task, timeout, unit);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasTask() {
        return !this.tasks.isEmpty();
    }

    /**
     * Offer tasks. Return the last offer index.
     *
     * @param tasks Consumer accepts collection.
     *
     * @return Offer success if return equals collection size.
     */
    @Override
    public int offer(Collection<T> tasks) {
        int index = 0;
        for (T task : tasks) {
            if (!this.offer(task)) break;
            index++;
        }
        return index;
    }

    @Override
    protected void beforePause() {
        this.acceptTask = false;

        processResidualTasks();
    }

    @Override
    protected void beforeStart() {
        this.acceptTask = true;
    }

    @Override
    protected void beforeStop() {
        this.acceptTask = false;

        processResidualTasks();
    }

    @Override
    protected void beforeResume() {
        this.acceptTask = true;
    }

    @Override
    protected void beforeDestroy() {
        this.acceptTask = false;

        processResidualTasks();
    }

    @Override
    protected void onDestroy() {
        if (this.executor != null) {
            this.executor.shutdown();
            try {
                if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.executor.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                this.executor = null;
            }
        }

        tasks.clear();
    }

    protected ExecutorService defineExecutor() {
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

    protected BlockingDeque<T> defineTaskQueue() {
        DispatchDequeue config = this.getClass().getAnnotation(DispatchDequeue.class);
        if (config != null) {
            return new LinkedBlockingDeque<>(config.size());
        } else {
            return new LinkedBlockingDeque<>();
        }
    }

    protected ResidualSchedule defineSchedule() {
        DispatchDequeue config = this.getClass().getAnnotation(DispatchDequeue.class);
        if (config != null) {
            return new ResidualSchedule(config.schedule(), config.timeout(), config.timeoutUnit());
        } else {
            return new ResidualSchedule();
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

    protected AlertParams initAlertParams() {
        DispatchAlert config = this.getClass().getAnnotation(DispatchAlert.class);
        if (config != null) {
            return new AlertParams(config.alertSize(), config.alertPeriod(), config.periodUnit());
        } else {
            return new AlertParams();
        }
    }

    protected boolean overloaded() {
        return this.alertParams.alertSize > 0 && this.tasks.size() >= this.alertParams.alertSize
                && System.currentTimeMillis() - this.alertParams.lastAlertTime >= this.alertParams.periodUnit.toMillis(
                this.alertParams.alertPeriod);
    }

    protected void alert() {
        log.warn("Dispatcher: \"{}\" is overloaded.", this.getClass().getSimpleName());
        this.alertParams.lastAlertTime = System.currentTimeMillis();
    }

    protected Consumer<T> customizeRestConsumer() {
        return task ->
                log.warn("Task \"{}\" is is discarded..", task.getClass().getName());
    }

    private void processResidualTasks() {
        switch (this.schedule.residualTaskSchedule) {
            case CONSUME:
                consumeAllTasks(this.taskConsumer);
                break;
            case DISCARD:
                this.tasks.clear();
                break;
            case CUSTOMIZE:
                consumeAllTasks(this.customizeRestConsumer());
                break;
        }
    }

    private void consumeAllTasks(Consumer<T> consumer) {
        long start = System.currentTimeMillis();
        while (!this.tasks.isEmpty()) {
            T task = this.tasks.poll();
            if (task != null) {
                consumer.accept(task);
            }
            if (System.currentTimeMillis() - start > this.schedule.unit.toMillis(this.schedule.timeout)) {
                break;
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResidualSchedule {

        public ResidualTaskSchedule residualTaskSchedule = ResidualTaskSchedule.CONSUME;

        public long timeout = 60L;

        public TimeUnit unit = TimeUnit.SECONDS;

    }

    @NoArgsConstructor
    public static class AlertParams {

        public int alertSize = -1;

        public long alertPeriod = 3600L;

        public TimeUnit periodUnit = TimeUnit.SECONDS;

        public long lastAlertTime = 0L;

        public AlertParams(int alertSize, long alertPeriod, TimeUnit periodUnit) {
            this.alertSize = alertSize;
            this.alertPeriod = alertPeriod;
            this.periodUnit = periodUnit;
        }

    }

}
