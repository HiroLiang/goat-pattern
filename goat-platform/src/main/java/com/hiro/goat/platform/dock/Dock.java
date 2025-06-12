package com.hiro.goat.platform.dock;

import com.hiro.goat.api.parcel.Parcel;
import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.worker.QueueDispatchWorker;

import java.util.function.Consumer;

public class Dock extends QueueDispatchWorker<Parcel<Task>> {

    public Dock(Consumer<Parcel<Task>> taskConsumer) {
        super(taskConsumer);
    }

    public void deliver(Parcel<Task> parcel) {

    }

}
