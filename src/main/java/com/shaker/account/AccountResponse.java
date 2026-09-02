package com.shaker.account;

import com.shaker.user.User;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Public view of an account. Never includes the password hash.
 */
public record AccountResponse(
        String id,
        String username,
        String email,
        String displayName,
        String bio,
        Set<String> roles,
        Instant createdAt) {

    public static AccountResponse from(User user) {
        return new AccountResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getRoles().stream().map(Enum::name)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                user.getCreatedAt());
    }
}
