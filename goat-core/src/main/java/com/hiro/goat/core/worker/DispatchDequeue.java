package com.hiro.goat.core.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchDequeue {

    int size() default 1023;

    ResidualTaskSchedule schedule() default ResidualTaskSchedule.CONSUME;

    long timeout() default 60L;

    TimeUnit timeoutUnit() default TimeUnit.SECONDS;

}
