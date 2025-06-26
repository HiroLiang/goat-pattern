package com.hiro.goat.platform.order.task;

import com.hiro.goat.core.task.AbstractTask;

public class TaskWrapper<T> extends TaskOrder<T> {

    public TaskWrapper(AbstractTask<?, T> task) {
        super(task);
    }

}
