package com.shaker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void conflict_maps_to_409_with_original_message() {
        ResponseEntity<ApiError> response =
                handler.handleConflict(new ConflictException("Username is already taken"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CONFLICT");
        assertThat(response.getBody().message()).isEqualTo("Username is already taken");
        assertThat(response.getBody().fields()).isEmpty();
    }

    @Test
    void not_found_maps_to_404() {
        ResponseEntity<ApiError> response =
                handler.handleNotFound(new NotFoundException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("User not found");
    }

    @Test
    void duplicate_key_maps_to_409_with_generic_message_not_the_driver_detail() {
        ResponseEntity<ApiError> response =
                handler.handleDuplicateKey(new DuplicateKeyException("E11000 duplicate key error ... username_1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CONFLICT");
        assertThat(response.getBody().message()).doesNotContain("E11000");
    }

    @Test
    void bad_credentials_maps_to_401() {
        ResponseEntity<ApiError> response = handler.handleAuth(new BadCredentialsException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("BAD_CREDENTIALS");
    }

    @Test
    void validation_collects_per_field_messages_with_a_fallback() {
        BindingResult binding = mock(BindingResult.class);
        when(binding.getFieldErrors()).thenReturn(List.of(
                new FieldError("signupRequest", "username", "must not be blank"),
                new FieldError("signupRequest", "email", null)));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(binding);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().fields())
                .containsEntry("username", "must not be blank")
                .containsEntry("email", "invalid");
    }
}
