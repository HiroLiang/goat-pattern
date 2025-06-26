package com.hiro.goat.core.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchAlert {

    int alertSize() default -1;

    long alertPeriod() default 3600L;

    TimeUnit periodUnit() default TimeUnit.SECONDS;

}
