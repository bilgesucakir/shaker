package com.shaker.entity.common;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Getter;
import lombok.Setter;

/**
 * Base for documents that always belong to exactly one user (diary entries, bar items,
 * bookmarks, collections) - every query on these is scoped by {@code ownerUsername}.
 *
 * <p>{@code Recipe} deliberately does NOT extend this: its owner ({@code createdBy}) can be
 * null for app-seeded classics, which would violate the "always owned" contract this base
 * class implies. That's a Liskov substitution call, not an oversight.
 */
@Getter
@Setter
public abstract class UserOwnedEntity extends BaseEntity {

    @Indexed
    private String ownerUsername;
}
