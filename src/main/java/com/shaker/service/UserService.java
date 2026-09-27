package com.shaker.service;

import com.shaker.dto.UpdateAccountRequestDto;
import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.entity.user.UserPreferences;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String rawPassword, String displayName) {
        String normalizedUsername = username.trim().toLowerCase();
        String normalizedEmail = email.trim().toLowerCase();

        if (users.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username is already taken");
        }
        if (users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(StringUtils.hasText(displayName) ? displayName.trim() : normalizedUsername);
        user.setRoles(new LinkedHashSet<>(Set.of(Role.USER)));
        user.setCreatedAt(Instant.now());
        return users.save(user);
    }

    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User updateProfile(String username, UpdateAccountRequestDto request) {
        User user = getByUsername(username);
        if (request.displayName() != null && StringUtils.hasText(request.displayName())) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }

        UserPreferences preferences = user.getPreferences();
        if (request.favoriteSpirit() != null) {
            preferences.setFavoriteSpirit(
                    StringUtils.hasText(request.favoriteSpirit()) ? request.favoriteSpirit().trim().toLowerCase() : null);
        }
        if (request.showCommunityTips() != null) {
            preferences.setShowCommunityTips(request.showCommunityTips());
        }
        if (request.preferredVolumeUnit() != null) {
            preferences.setPreferredVolumeUnit(request.preferredVolumeUnit());
        }

        return users.save(user);
    }

    public List<User> findAll() {
        return users.findAll();
    }

    public User grantRole(String username, Role role) {
        User user = getByUsername(username);
        Set<Role> roles = new LinkedHashSet<>(user.getRoles());
        roles.add(role);
        user.setRoles(roles);
        return users.save(user);
    }

    public User revokeRole(String actingUsername, String targetUsername, Role role) {
        if (role == Role.USER) {
            throw new ConflictException("The USER role can't be revoked");
        }
        if (actingUsername.equals(targetUsername) && role == Role.ADMIN) {
            throw new ConflictException("You can't revoke your own admin role");
        }
        User user = getByUsername(targetUsername);
        Set<Role> roles = new LinkedHashSet<>(user.getRoles());
        roles.remove(role);
        user.setRoles(roles);
        return users.save(user);
    }
}
