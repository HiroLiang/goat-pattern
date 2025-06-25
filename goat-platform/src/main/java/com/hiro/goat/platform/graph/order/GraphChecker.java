package com.hiro.goat.platform.graph.order;

import com.hiro.goat.api.task.DependencyProvider;
import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.platform.graph.GraphState;

public final class GraphChecker extends AbstractTask<GraphState, GraphState> {

    public GraphChecker(DependencyProvider dependencies) {
        super(dependencies);
    }

    @Override
    protected void process() {
        if (param != null) {
            result = param;
        }
    }

    @Override
    protected void rollback() {
    }

}
