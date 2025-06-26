package com.hiro.goat.platform.graph;

import com.hiro.goat.core.task.AbstractTask;

public interface Graph {

    String getId();

    GraphState state();

    <T extends AbstractTask<?, R>, R> void offer(T task);

    void rollback();

}
