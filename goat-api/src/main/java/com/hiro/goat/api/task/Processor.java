package com.hiro.goat.api.task;

import java.util.Collection;

public interface Processor {

    void process(Task task);

    void Process(Collection<Task> tasks);

}
