package com.shaker.service;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.exception.ConflictException;
import com.shaker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRoleTest {

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private static User user(String username, Role... roles) {
        User u = new User();
        u.setUsername(username);
        u.setRoles(Set.of(roles));
        return u;
    }

    @Test
    void grantRole_adds_the_role_without_dropping_existing_ones() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(user("bob", Role.USER)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.grantRole("bob", Role.ADMIN);

        assertThat(updated.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void revokeRole_removes_admin_but_keeps_user() {
        when(users.findByUsername("bob")).thenReturn(Optional.of(user("bob", Role.USER, Role.ADMIN)));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.revokeRole("alice", "bob", Role.ADMIN);

        assertThat(updated.getRoles()).containsExactly(Role.USER);
    }

    @Test
    void revokeRole_rejects_removing_the_base_user_role() {
        assertThatThrownBy(() -> userService.revokeRole("alice", "bob", Role.USER))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void revokeRole_rejects_self_demotion_from_admin() {
        assertThatThrownBy(() -> userService.revokeRole("alice", "alice", Role.ADMIN))
                .isInstanceOf(ConflictException.class);
    }
}
