package com.shaker.service;

import com.shaker.dto.UpdateAccountRequestDto;
import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.entity.user.VolumeUnit;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void register_normalizes_input_encodes_password_and_defaults_display_name() {
        when(users.existsByUsername("alice")).thenReturn(false);
        when(users.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("s3cret-pass")).thenReturn("HASHED");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("  Alice ", " Alice@Example.com ", "s3cret-pass", "   ");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        User persisted = saved.getValue();
        assertThat(persisted.getUsername()).isEqualTo("alice");
        assertThat(persisted.getEmail()).isEqualTo("alice@example.com");
        assertThat(persisted.getPasswordHash()).isEqualTo("HASHED");
        assertThat(persisted.getDisplayName()).isEqualTo("alice"); // blank -> falls back to username
        assertThat(persisted.getRoles()).containsExactly(Role.USER);
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void register_keeps_provided_display_name_trimmed() {
        when(users.existsByUsername(anyString())).thenReturn(false);
        when(users.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("h");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("bob", "bob@x.com", "password1", "  Bobby  ");

        assertThat(result.getDisplayName()).isEqualTo("Bobby");
    }

    @Test
    void register_rejects_taken_username_without_saving() {
        when(users.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("Taken", "e@x.com", "password1", null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username");
        verify(users, never()).save(any());
    }

    @Test
    void register_rejects_taken_email_without_saving() {
        when(users.existsByUsername("free")).thenReturn(false);
        when(users.existsByEmail("taken@x.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("free", "Taken@X.com", "password1", null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
        verify(users, never()).save(any());
    }

    @Test
    void getByUsername_returns_match_or_throws_not_found() {
        User alice = new User();
        alice.setUsername("alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThat(userService.getByUsername("alice")).isSameAs(alice);
        assertThatThrownBy(() -> userService.getByUsername("ghost"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_updates_only_supplied_fields_trimmed() {
        User user = new User();
        user.setUsername("alice");
        user.setDisplayName("Old");
        user.setBio("old bio");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile("alice",
                new UpdateAccountRequestDto("  New Name  ", "  new bio  ", null, null, null));

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getBio()).isEqualTo("new bio");
    }

    @Test
    void updateProfile_ignores_null_or_blank_display_name() {
        User user = new User();
        user.setUsername("alice");
        user.setDisplayName("Keep");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile("alice", new UpdateAccountRequestDto("   ", null, null, null, null));

        assertThat(user.getDisplayName()).isEqualTo("Keep");
    }

    @Test
    void updateProfile_updates_preferences_when_supplied() {
        User user = new User();
        user.setUsername("alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile("alice",
                new UpdateAccountRequestDto(null, null, " Gin ", false, VolumeUnit.ML));

        assertThat(user.getPreferences().getFavoriteSpirit()).isEqualTo("gin");
        assertThat(user.getPreferences().isShowCommunityTips()).isFalse();
        assertThat(user.getPreferences().getPreferredVolumeUnit()).isEqualTo(VolumeUnit.ML);
    }
}
