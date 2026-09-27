package com.shaker;

import com.shaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end HTTP + security + persistence test against the configured MongoDB. Runs in
 * {@code mvn verify} (Failsafe), and only when {@code MONGODB_URI} is set — point it at a
 * throwaway database (e.g. .../shaker_test), it calls {@code deleteAll} on users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MONGODB_URI", matches = ".+")
class AuthFlowIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @BeforeEach
    void clean() {
        users.deleteAll();
    }

    @Test
    void guidelines_are_public() throws Exception {
        mvc.perform(get("/api/guidelines")).andExpect(status().isOk());
    }

    @Test
    void account_requires_authentication() throws Exception {
        mvc.perform(get("/api/account")).andExpect(status().isUnauthorized());
    }

    @Test
    void signup_then_session_returns_account() throws Exception {
        mvc.perform(post("/api/auth/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tester","email":"tester@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void signup_rejects_short_password() throws Exception {
        mvc.perform(post("/api/auth/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tester","email":"tester@example.com","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void non_admin_is_forbidden_from_admin_api() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
    }
}
