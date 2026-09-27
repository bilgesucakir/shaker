package com.shaker.entity.user;

/**
 * Application roles. Stored on the {@link User} document without the {@code ROLE_} prefix;
 * the prefix is added when building Spring Security authorities.
 */
public enum Role {
    USER,
    ADMIN
}
