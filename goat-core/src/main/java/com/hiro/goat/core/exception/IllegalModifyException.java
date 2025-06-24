package com.hiro.goat.core.exception;

import com.hiro.goat.api.exception.GoatException;

public class IllegalModifyException extends GoatException {

    public IllegalModifyException(String message) {
        super(message);
    }

    public IllegalModifyException(String message, Throwable cause) {
        super(message, cause);
    }

}
