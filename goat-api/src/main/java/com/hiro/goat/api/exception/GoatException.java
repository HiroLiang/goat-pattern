package com.hiro.goat.api.exception;

public class GoatException extends RuntimeException {

    public GoatException(String message) {
        super(message);
    }

    public GoatException(String message, Throwable cause) {
        super(message, cause);
    }

}
