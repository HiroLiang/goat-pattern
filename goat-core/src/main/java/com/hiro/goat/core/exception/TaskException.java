package com.hiro.goat.core.exception;

import com.hiro.goat.api.exception.GoatException;

public class TaskException extends GoatException {

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

}
