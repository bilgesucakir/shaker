package com.shaker.controller;

import com.shaker.dto.AccountResponseDto;
import com.shaker.entity.user.Role;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The users resource. Every operation here currently requires {@code ROLE_ADMIN} (see
 * {@code SecurityConfig}) - there's no public user directory yet - but it's modeled as a
 * normal resource rather than a separate admin API.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AccountResponseDto> getUsers() {
        return userService.findAll().stream().map(AccountResponseDto::from).toList();
    }

    @PostMapping("/{username}/roles/{role}")
    public AccountResponseDto grantRole(@PathVariable String username, @PathVariable Role role) {
        return AccountResponseDto.from(userService.grantRole(username, role));
    }

    @DeleteMapping("/{username}/roles/{role}")
    public AccountResponseDto revokeRole(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable String username, @PathVariable Role role) {
        return AccountResponseDto.from(userService.revokeRole(principal.getUsername(), username, role));
    }
}
