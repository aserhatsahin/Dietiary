package com.dietary.common.exception;

/** The request is valid but conflicts with the current state of the data (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
