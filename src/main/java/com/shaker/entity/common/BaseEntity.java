package com.shaker.entity.common;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Common fields for every top-level MongoDB document: an id and a creation timestamp.
 * Saves repeating them (and their Lombok accessors) on every entity.
 */
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    private String id;

    private Instant createdAt = Instant.now();
}
