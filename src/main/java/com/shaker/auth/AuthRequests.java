package com.shaker.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record SignupRequest(
            @NotBlank
            @Size(min = 3, max = 30)
            @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                    message = "may only contain letters, digits and underscores")
            String username,

            @NotBlank @Email
            String email,

            @NotBlank @Size(min = 8, max = 100)
            String password,

            @Size(max = 60)
            String displayName) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }
}
