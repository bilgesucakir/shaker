package com.shaker.exception;

/** 409 - the request conflicts with existing state (e.g. username taken). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
