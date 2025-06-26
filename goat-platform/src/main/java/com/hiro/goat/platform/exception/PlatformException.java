package com.hiro.goat.platform.exception;

import com.hiro.goat.api.exception.GoatException;

public class PlatformException extends GoatException {

    public PlatformException(String message) {
        super(message);
    }

    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }

}
