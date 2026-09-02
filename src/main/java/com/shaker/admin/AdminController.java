package com.shaker.admin;

import com.shaker.account.AccountResponse;
import com.shaker.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Requires {@code ROLE_ADMIN} (enforced in {@code SecurityConfig} for {@code /api/admin/**}).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<AccountResponse> users() {
        return userService.findAll().stream().map(AccountResponse::from).toList();
    }
}
