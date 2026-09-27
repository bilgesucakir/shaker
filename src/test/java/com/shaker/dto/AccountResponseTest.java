package com.shaker.dto;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.entity.user.VolumeUnit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountResponseTest {

    @Test
    void from_maps_every_field_and_role_names() {
        User user = new User();
        user.setId("id-1");
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setDisplayName("Alice");
        user.setBio("gin person");
        user.setRoles(new LinkedHashSet<>(Set.of(Role.USER, Role.ADMIN)));
        user.getPreferences().setFavoriteSpirit("gin");
        user.getPreferences().setShowCommunityTips(false);
        user.getPreferences().setPreferredVolumeUnit(VolumeUnit.ML);
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        user.setCreatedAt(created);

        AccountResponseDto response = AccountResponseDto.from(user);

        assertThat(response.id()).isEqualTo("id-1");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.displayName()).isEqualTo("Alice");
        assertThat(response.bio()).isEqualTo("gin person");
        assertThat(response.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(response.favoriteSpirit()).isEqualTo("gin");
        assertThat(response.showCommunityTips()).isFalse();
        assertThat(response.preferredVolumeUnit()).isEqualTo(VolumeUnit.ML);
        assertThat(response.createdAt()).isEqualTo(created);
    }
}
