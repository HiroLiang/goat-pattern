package com.hiro.goat.api.task;

public interface Task<P, R> extends Runnable {

    Task<P, R> initParams(P param);

    void execute();

    boolean isSuccess();

    R getResult();

}
