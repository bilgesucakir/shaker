package com.shaker.account;

import com.shaker.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public AccountResponse current(Authentication authentication) {
        return AccountResponse.from(userService.getByUsername(authentication.getName()));
    }

    @PutMapping
    public AccountResponse update(Authentication authentication,
                                  @Valid @RequestBody UpdateAccountRequest request) {
        return AccountResponse.from(
                userService.updateProfile(authentication.getName(), request.displayName(), request.bio()));
    }

    public record UpdateAccountRequest(
            @Size(max = 60) String displayName,
            @Size(max = 500) String bio) {
    }
}
