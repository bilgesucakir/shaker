package com.shaker.common;

/**
 * Small domain exceptions mapped to HTTP status codes by {@link ApiExceptionHandler}.
 */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 404 - a referenced resource does not exist. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    /** 409 - the request conflicts with existing state (e.g. username taken). */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }
}
