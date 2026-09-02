package com.shaker.user;

import com.shaker.common.ApiExceptions.ConflictException;
import com.shaker.common.ApiExceptions.NotFoundException;
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

    public User updateProfile(String username, String displayName, String bio) {
        User user = getByUsername(username);
        if (displayName != null && StringUtils.hasText(displayName)) {
            user.setDisplayName(displayName.trim());
        }
        if (bio != null) {
            user.setBio(bio.trim());
        }
        return users.save(user);
    }

    public List<User> findAll() {
        return users.findAll();
    }
}
