package com.hiro.goat.api.task;

public interface Processor<T extends Task<?, ?>> {

    void process(T task);

}
