package com.shaker.controller;

import com.shaker.dto.AccountResponseDto;
import com.shaker.dto.UpdateAccountRequestDto;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public AccountResponseDto getAccount(@AuthenticationPrincipal AppUserPrincipal principal) {
        return AccountResponseDto.from(userService.getByUsername(principal.getUsername()));
    }

    @PutMapping
    public AccountResponseDto updateAccount(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @Valid @RequestBody UpdateAccountRequestDto request) {
        return AccountResponseDto.from(userService.updateProfile(principal.getUsername(), request));
    }
}
