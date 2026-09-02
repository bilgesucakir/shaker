package com.shaker.auth;

import com.shaker.account.AccountResponse;
import com.shaker.auth.AuthRequests.LoginRequest;
import com.shaker.auth.AuthRequests.SignupRequest;
import com.shaker.user.User;
import com.shaker.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    public AccountResponse signup(@Valid @RequestBody SignupRequest request,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        User user = userService.register(
                request.username(), request.email(), request.password(), request.displayName());
        establishSession(user.getUsername(), request.password(), httpRequest, httpResponse);
        return AccountResponse.from(user);
    }

    @PostMapping("/login")
    public AccountResponse login(@Valid @RequestBody LoginRequest request,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        establishSession(request.username(), request.password(), httpRequest, httpResponse);
        return AccountResponse.from(userService.getByUsername(request.username().trim().toLowerCase()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(AccountResponse.from(userService.getByUsername(authentication.getName())));
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
