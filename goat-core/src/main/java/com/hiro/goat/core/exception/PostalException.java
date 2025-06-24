package com.hiro.goat.core.exception;

import com.hiro.goat.api.exception.GoatException;

public class PostalException extends GoatException {

    public PostalException(String message) {
        super(message);
    }

    public PostalException(String message, Throwable cause) {
        super(message, cause);
    }

}
