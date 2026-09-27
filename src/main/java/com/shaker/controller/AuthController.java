package com.shaker.controller;

import com.shaker.dto.AccountResponseDto;
import com.shaker.dto.LoginRequestDto;
import com.shaker.dto.SignupRequestDto;
import com.shaker.entity.user.User;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto signup(@Valid @RequestBody SignupRequestDto request,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        User user = userService.register(
                request.username(), request.email(), request.password(), request.displayName());
        establishSession(user.getUsername(), request.password(), httpRequest, httpResponse);
        return AccountResponseDto.from(user);
    }

    @PostMapping("/login")
    public AccountResponseDto login(@Valid @RequestBody LoginRequestDto request,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        establishSession(request.username(), request.password(), httpRequest, httpResponse);
        return AccountResponseDto.from(userService.getByUsername(request.username().trim().toLowerCase()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
    }

    /** Current session's account, or 401 if not logged in. */
    @GetMapping("/session")
    public ResponseEntity<AccountResponseDto> getCurrentSession(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(AccountResponseDto.from(userService.getByUsername(principal.getUsername())));
    }

    private void establishSession(String username, String password,
                                  HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username.trim().toLowerCase(), password));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        
        // create the session and persist the context so the JSESSIONID cookie is issued
        httpRequest.getSession(true);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}   
