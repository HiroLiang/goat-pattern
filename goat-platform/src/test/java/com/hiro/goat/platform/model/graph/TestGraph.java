package com.hiro.goat.platform.model.graph;

import com.hiro.goat.platform.graph.AbstractGraph;
import com.hiro.goat.platform.model.task.TestTask;

public class TestGraph extends AbstractGraph {

    @Override
    protected void designGraph() {
        addTaskGraph(TestTask.class, result -> result == true);
    }

}
