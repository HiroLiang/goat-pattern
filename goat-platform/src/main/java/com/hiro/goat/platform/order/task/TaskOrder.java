package com.hiro.goat.platform.order.task;

import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.platform.graph.Graph;
import com.hiro.goat.platform.graph.GraphState;
import com.hiro.goat.platform.order.Order;
import com.hiro.goat.platform.order.PlatformOrder;

import lombok.Getter;
import lombok.Setter;

public abstract class TaskOrder<R> extends PlatformOrder<AbstractTask<?, R>, R> {

    @Getter
    private final AbstractTask<?, R> task;

    @Getter
    private final Class<? extends Graph> graphClass;

    @Getter
    @Setter
    private String graphId;

    @Getter
    @Setter
    private GraphState graphState;

    protected TaskOrder(AbstractTask<?, R> task, Class<? extends Graph> graphClass) {
        super(Order.TASK);
        this.task = task;
        this.graphClass = graphClass;
    }

    protected TaskOrder(AbstractTask<?, R> task, Class<? extends Graph> graphClass, String graphId) {
        super(Order.TASK);
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

}
