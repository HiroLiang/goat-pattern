package com.hiro.goat.core.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchExecutor {

    int coreSize() default 4;

    int maxSize() default 8;

    int queueCapacity() default 100;

    long keepAliveTime() default 60;

}
