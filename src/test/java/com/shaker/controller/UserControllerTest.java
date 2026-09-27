package com.shaker.controller;

import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @Mock
    AppUserPrincipal principal;

    @InjectMocks
    UserController controller;

    @Test
    void getUsers_maps_to_account_dtos() {
        User bob = new User();
        bob.setUsername("bob");
        when(userService.findAll()).thenReturn(List.of(bob));

        assertThat(controller.getUsers()).extracting("username").containsExactly("bob");
    }

    @Test
    void grantRole_delegates_to_the_service() {
        User bob = new User();
        bob.setUsername("bob");
        when(userService.grantRole("bob", Role.ADMIN)).thenReturn(bob);

        assertThat(controller.grantRole("bob", Role.ADMIN).username()).isEqualTo("bob");
    }

    @Test
    void revokeRole_uses_the_actor_and_target_usernames() {
        when(principal.getUsername()).thenReturn("alice");
        User bob = new User();
        bob.setUsername("bob");
        when(userService.revokeRole("alice", "bob", Role.ADMIN)).thenReturn(bob);

        assertThat(controller.revokeRole(principal, "bob", Role.ADMIN).username()).isEqualTo("bob");
    }
}
