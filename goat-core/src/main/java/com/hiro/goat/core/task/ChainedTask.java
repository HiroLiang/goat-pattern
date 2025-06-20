package com.hiro.goat.core.task;

import com.hiro.goat.api.task.Task;

import java.util.ArrayList;
import java.util.List;

public class ChainedTask extends AbstractTask {

    private final List<Task> tasks = new ArrayList<>();

    @Override
    protected void process() {
        for (Task task : tasks) {

        }
    }

    @Override
    protected void rollback() {

    }

}
