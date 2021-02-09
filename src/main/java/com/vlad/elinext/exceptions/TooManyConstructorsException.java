package com.vlad.elinext.exceptions;

public class TooManyConstructorsException extends RuntimeException {
    public TooManyConstructorsException(String message) {
        super(message);
    }
}
