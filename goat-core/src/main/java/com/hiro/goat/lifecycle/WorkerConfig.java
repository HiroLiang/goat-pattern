package com.hiro.goat.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WorkerConfig {

    int coreSize() default 4;

    int maxSize() default 8;

    int queueCapacity() default 100;

    long keepAliveTime() default 60;

}
