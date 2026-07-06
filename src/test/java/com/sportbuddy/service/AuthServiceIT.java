package com.sportbuddy.service;

import com.sportbuddy.AbstractIntegrationTest;
import com.sportbuddy.dto.RegisterRequest;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end registration through {@link AuthService} against a real database,
 * verifying BCrypt hashing and persistence.
 */
class AuthServiceIT extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest request(String email, String phone) {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("Иван");
        r.setLastName("Ведутин");
        r.setEmail(email);
        r.setPhone(phone);
        r.setPassword("secret123");
        r.setConfirmPassword("secret123");
        return r;
    }

    @Test
    void register_persistsUserWithBcryptHashedPassword() {
        Optional<String> error = authService.register(request("bcrypt@example.com", "+79990001111"));

        assertThat(error).isEmpty();
        User saved = userRepository.findByEmail("bcrypt@example.com").orElseThrow();
        // Stored password is a BCrypt hash, not the plain text.
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(saved.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
    }

    @Test
    void register_duplicateEmail_isRejected() {
        authService.register(request("dup@example.com", "+79990002222"));

        Optional<String> error = authService.register(request("dup@example.com", "+79990003333"));

        assertThat(error).isPresent();
        assertThat(error.get()).contains("email");
    }
}
