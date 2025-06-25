package com.hiro.goat.platform.graph.order;

import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.platform.graph.Graph;

public class GraphRollback extends AbstractTask<Graph, Boolean> {

    public GraphRollback() {
        super(null);
    }

    @Override
    protected void process() {
        if (param != null) {
            param.rollback();
        }
    }

    @Override
    protected void rollback() {
    }

}
