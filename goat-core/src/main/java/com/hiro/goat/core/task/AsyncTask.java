package com.hiro.goat.core.task;

public @interface AsyncTask {

    boolean await() default false;

}
