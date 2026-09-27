package com.shaker.security;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    UserRepository users;

    @InjectMocks
    AppUserDetailsService service;

    @Test
    void loads_user_normalizing_the_looked_up_username() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("hash");
        user.setRoles(Set.of(Role.USER, Role.ADMIN));
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));

        AppUserPrincipal principal = service.loadUserByUsername("  Alice ");

        assertThat(principal.getUsername()).isEqualTo("alice");
        assertThat(principal.getPassword()).isEqualTo("hash");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void throws_username_not_found_when_absent() {
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
