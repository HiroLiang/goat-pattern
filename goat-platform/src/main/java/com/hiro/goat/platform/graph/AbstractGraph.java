package com.hiro.goat.platform.graph;

import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.graph.order.GraphChecker;
import com.hiro.goat.platform.graph.order.GraphRollback;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractGraph implements Graph {

    @Getter
    private final String id = UUID.randomUUID().toString();

    private final Map<Class<? extends AbstractTask<?, ?>>, TaskGraph<?, ?>> taskGraphs = new ConcurrentHashMap<>();

    public AbstractGraph() {
        designGraph();
    }

    protected abstract void designGraph();

    @Override
    public GraphState state() {
        GraphState state = new GraphState();
        for (TaskGraph<?, ?> taskGraph : this.taskGraphs.values()) {
            state.getTaskStates().add(
                    new GraphState.TaskState(taskGraph.taskClass.getName(), taskGraph.offered, taskGraph.checked));

            if (!taskGraph.checked) state.setCompleted(false);
        }
        return state;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractTask<?, R>, R> void offer(T task) {
        if (processGraphTask(task)) return;

        TaskGraph<T, R> taskGraph = (TaskGraph<T, R>) this.taskGraphs.get(task.getClass());
        if (taskGraph == null) {
            throw GoatErrors.of("No task graph found for task: " + task.getClass().getName(), PlatformException.class);
        }

        taskGraph.verify(task);
    }

    @Override
    public void rollback() {
        for (TaskGraph<?, ?> taskGraph : this.taskGraphs.values()) {
            if (taskGraph.checked) {
                taskGraph.task.rollback(true).execute();
            }
            taskGraph.checked = true;
            taskGraph.offered = true;
        }
    }

    protected <T extends AbstractTask<?, R>, R> void addTaskGraph(Class<T> taskClass, Function<R, Boolean> taskFunction) {
        this.taskGraphs.put(taskClass, new TaskGraph<>(taskClass, taskFunction));
    }

    private boolean processGraphTask(Task<?, ?> task) {
        if (task instanceof GraphChecker) {
            GraphChecker checker = (GraphChecker) task;
            checker.initParams(this.state()).execute();
            return true;
        } else if (task instanceof GraphRollback) {
            GraphRollback rollback = (GraphRollback) task;
            rollback.initParams(this).execute();
            return true;
        }

        return false;
    }

    public static class TaskGraph<T extends AbstractTask<?, R>, R> {

        public Class<T> taskClass;

        public T task;

        public Function<R, Boolean> verifier;

        public boolean offered = false;

        public boolean checked = false;

        public TaskGraph(Class<T> taskClass, Function<R, Boolean> verifier) {
            this.taskClass = taskClass;
            this.verifier = verifier;
        }

        public void verify(T task) {
            this.task = task;
            this.offered = true;
            this.checked = verifier.apply(task.getResult());
        }

    }

}
