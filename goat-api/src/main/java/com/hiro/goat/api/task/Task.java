package com.hiro.goat.api.task;

import com.hiro.goat.api.chain.Chainable;

public interface Task extends Runnable, Chainable {

    void execute();

    boolean isSuccess();

}
