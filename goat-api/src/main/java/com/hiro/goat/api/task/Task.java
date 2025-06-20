package com.hiro.goat.api.task;

public interface Task extends Runnable {

    void execute();

    boolean isSuccess();

}
