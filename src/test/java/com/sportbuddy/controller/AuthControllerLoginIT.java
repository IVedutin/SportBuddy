package com.sportbuddy.controller;

import com.sportbuddy.AbstractIntegrationTest;
import com.sportbuddy.entity.Role;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST integration test for {@code POST /api/auth/login} — exercises the
 * controller, Spring Security AuthenticationManager, {@code CustomUserDetailsService}
 * and BCrypt verification against a real database.
 */
@AutoConfigureMockMvc
class AuthControllerLoginIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUser() {
        if (userRepository.findByEmail("login@example.com").isEmpty()) {
            User u = new User("Логин", "Тест", "login@example.com", "+79990007777",
                    passwordEncoder.encode("secret123"));
            u.setRole(Role.USER);
            userRepository.save(u);
        }
    }

    @Test
    void login_withCorrectCredentials_returnsOkAndRedirect() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.redirect").value("/dashboard"));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@example.com\",\"password\":\"WRONG\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
