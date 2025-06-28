package com.hiro.goat.core;

import com.hiro.goat.core.worker.*;

import java.util.concurrent.TimeUnit;

@DispatchExecutor(
        coreSize = 8,
        maxSize = 32,
        queueCapacity = 128,
        keepAliveTime = 120
)
@DispatchDequeue(
        size = 100,
        schedule = ResidualTaskSchedule.CUSTOMIZE,
        timeout = 120L,
        timeoutUnit = TimeUnit.SECONDS
)
@DispatchAlert(
        alertSize = 80,
        alertPeriod = 12,
        periodUnit = TimeUnit.HOURS
)
public class TestClass extends QueueDispatchWorker<String> {

    @Override
    protected void processTask(String task) {
        System.out.println(task);
    }

}
