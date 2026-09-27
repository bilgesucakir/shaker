package com.shaker.security;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserPrincipalTest {

    private static User userWithRoles(Set<Role> roles) {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("hash");
        user.setRoles(roles);
        return user;
    }

    @Test
    void maps_roles_to_role_prefixed_authorities() {
        AppUserPrincipal principal =
                new AppUserPrincipal(userWithRoles(new LinkedHashSet<>(Set.of(Role.USER, Role.ADMIN))));

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void exposes_username_and_password_hash() {
        AppUserPrincipal principal = new AppUserPrincipal(userWithRoles(Set.of(Role.USER)));

        assertThat(principal.getUsername()).isEqualTo("alice");
        assertThat(principal.getPassword()).isEqualTo("hash");
    }

    @Test
    void account_status_flags_default_to_active() {
        AppUserPrincipal principal = new AppUserPrincipal(userWithRoles(Set.of(Role.USER)));

        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isAdmin_reflects_whether_the_admin_role_is_present() {
        assertThat(new AppUserPrincipal(userWithRoles(Set.of(Role.USER))).isAdmin()).isFalse();
        assertThat(new AppUserPrincipal(userWithRoles(Set.of(Role.USER, Role.ADMIN))).isAdmin()).isTrue();
    }
}
