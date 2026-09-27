package com.shaker.dto;

import com.shaker.entity.user.User;
import com.shaker.entity.user.VolumeUnit;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Public view of an account. Never includes the password hash.
 */
public record AccountResponseDto(
        String id,
        String username,
        String email,
        String displayName,
        String bio,
        Set<String> roles,
        String favoriteSpirit,
        boolean showCommunityTips,
        VolumeUnit preferredVolumeUnit,
        Instant createdAt) {

    public static AccountResponseDto from(User user) {
        return new AccountResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getRoles().stream().map(Enum::name)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                user.getPreferences().getFavoriteSpirit(),
                user.getPreferences().isShowCommunityTips(),
                user.getPreferences().getPreferredVolumeUnit(),
                user.getCreatedAt());
    }
}
