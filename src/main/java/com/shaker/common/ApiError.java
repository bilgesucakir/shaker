package com.shaker.common;

import java.util.Map;

/**
 * Uniform error body returned for all handled API failures.
 *
 * @param code    stable machine-readable code, e.g. {@code VALIDATION_FAILED}
 * @param message human-readable summary
 * @param fields  per-field validation messages; empty when not a field validation error
 */
public record ApiError(String code, String message, Map<String, String> fields) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Map.of());
    }
}
