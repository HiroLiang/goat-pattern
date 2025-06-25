package com.hiro.goat.platform.order.task;

import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.platform.graph.Graph;
import com.hiro.goat.platform.graph.GraphState;
import com.hiro.goat.platform.order.PlatformOrder;

import lombok.Getter;

public abstract class TaskOrder<R> extends PlatformOrder<AbstractTask<?, R>, R> {

    @Getter
    private final AbstractTask<?, R> task;

    @Getter
    private Class<? extends Graph> graphClass;

    @Getter
    private String graphId;

    @Getter
    private GraphState graphState;

    protected TaskOrder(AbstractTask<?, R> task) {
        super();
        this.task = task;
    }

    protected TaskOrder(AbstractTask<?, R> task, Class<? extends Graph> graphClass) {
        super();
        this.task = task;
        this.graphClass = graphClass;
    }

    protected TaskOrder(AbstractTask<?, R> task, Class<? extends Graph> graphClass, String graphId) {
        super();
        this.task = task;
        this.graphClass = graphClass;
        this.graphId = graphId;
    }


    @Override
    protected void process() {
        this.task.execute();
    }

    @Override
    protected void postProcess() {
        success = this.task.isSuccess();
        result = this.task.getResult();
    }

    @Override
    protected void rollback() {
        this.task.rollback(true).execute();
    }

    public TaskOrder<R> setGraphClass(Class<? extends Graph> graphClass) {
        this.graphClass = graphClass;
        return this;
    }

    public TaskOrder<R> setGraphId(String graphId) {
        this.graphId = graphId;
        return this;
    }

    public TaskOrder<R> setGraphState(GraphState graphState) {
        this.graphState = graphState;
        return this;
    }

}
