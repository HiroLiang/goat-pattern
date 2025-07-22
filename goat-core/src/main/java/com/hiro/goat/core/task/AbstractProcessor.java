package com.hiro.goat.core.task;

import com.hiro.goat.api.task.Processor;
import com.hiro.goat.api.task.Task;

/**
 * Define default process
 *
 * @param <T> Process task type
 */
public abstract class AbstractProcessor<T extends Task<?, ?>> implements Processor<T> {

    @Override
    public void process(T task) {
        task.execute();
    }

}
