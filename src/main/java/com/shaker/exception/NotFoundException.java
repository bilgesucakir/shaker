package com.shaker.exception;

/** 404 - a referenced resource does not exist. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
