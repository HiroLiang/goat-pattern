package com.hiro.goat.platform.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraphState {

    private boolean completed = true;

    private List<TaskState> taskStates = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class TaskState {

        private String taskName;

        private boolean offered;

        private boolean satisfy;

    }

}
