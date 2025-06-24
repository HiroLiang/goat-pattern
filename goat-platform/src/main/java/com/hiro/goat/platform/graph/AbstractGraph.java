package com.hiro.goat.platform.graph;

import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.order.task.GraphChecker;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractGraph implements Graph {

    @Getter
    private final String id = UUID.randomUUID().toString();

    private final Map<Class<? extends Task<?, ?>>, TaskGraph<?, ?>> taskGraphs = new ConcurrentHashMap<>();

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
            if (!taskGraph.checked) {
                state.setCompleted(false);
            }
        }
        return state;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Task<?, R>, R> void offer(T task) {
        if (GraphChecker.class.isAssignableFrom(task.getClass())) {
            GraphChecker checker = (GraphChecker) task;
            checker.initParams(this.state()).execute();
            return;
        }

        TaskGraph<T, R> taskGraph = (TaskGraph<T, R>) this.taskGraphs.get(task.getClass());
        if (taskGraph == null) {
            throw GoatErrors.of("No task graph found for task: " + task.getClass().getName(), PlatformException.class);
        }
        taskGraph.offered = true;
        taskGraph.verify(task);
    }

    protected <T extends Task<?, R>, R> void addTaskGraph(Class<T> taskClass, Function<R, Boolean> taskFunction) {
        this.taskGraphs.put(taskClass, new TaskGraph<>(taskClass, taskFunction));
    }

    public static class TaskGraph<T extends Task<?, R>, R> {

        public Class<T> taskClass;

        public Function<R, Boolean> verifier;

        public boolean offered = false;

        public boolean checked = false;

        public TaskGraph(Class<T> taskClass, Function<R, Boolean> verifier) {
            this.taskClass = taskClass;
            this.verifier = verifier;
        }

        public void verify(T task) {
            this.checked = verifier.apply(task.getResult());
        }

    }

}
