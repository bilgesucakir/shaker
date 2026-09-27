package com.shaker.security;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security principal backed by our domain {@link User}. Returned by
 * {@link AppUserDetailsService} so controllers can inject it with
 * {@code @AuthenticationPrincipal} instead of the broader {@code Authentication} interface.
 */
public class AppUserPrincipal implements UserDetails {

    private final String username;
    private final String passwordHash;
    private final Set<Role> roles;

    public AppUserPrincipal(User user) {
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.roles = user.getRoles();
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }
}
