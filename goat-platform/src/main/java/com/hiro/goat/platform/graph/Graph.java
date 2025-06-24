package com.hiro.goat.platform.graph;

import com.hiro.goat.api.task.Task;

public interface Graph {

    String getId();

    GraphState state();

    <T extends Task<?, R>, R> void offer(T task);

}
