package com.sportbuddy.service;

import com.sportbuddy.dto.RegisterRequest;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService} — registration business rules with all
 * collaborators mocked (no Spring context, no DB).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("Иван");
        r.setLastName("Ведутин");
        r.setEmail("ivan@example.com");
        r.setPhone("+79990000000");
        r.setPassword("secret123");
        r.setConfirmPassword("secret123");
        return r;
    }

    @Test
    void register_success_savesUserWithEncodedPassword() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+79990000000")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("ENCODED_HASH");

        Optional<String> error = authService.register(req);

        assertThat(error).isEmpty();
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ivan@example.com");
        // Password must be stored hashed, never in plain text.
        assertThat(saved.getValue().getPassword()).isEqualTo("ENCODED_HASH");
        assertThat(saved.getValue().getPassword()).isNotEqualTo("secret123");
    }

    @Test
    void register_passwordMismatch_returnsErrorAndDoesNotSave() {
        RegisterRequest req = validRequest();
        req.setConfirmPassword("different");

        Optional<String> error = authService.register(req);

        assertThat(error).contains("Пароли не совпадают");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_emailAlreadyExists_returnsError() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);

        Optional<String> error = authService.register(req);

        assertThat(error).contains("Пользователь с таким email уже существует");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_phoneAlreadyExists_returnsError() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone("+79990000000")).thenReturn(true);

        Optional<String> error = authService.register(req);

        assertThat(error).contains("Пользователь с таким телефоном уже существует");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_databaseError_returnsErrorMessage() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_HASH");
        when(userRepository.save(any())).thenThrow(new RuntimeException("connection refused"));

        Optional<String> error = authService.register(req);

        assertThat(error).isPresent();
        assertThat(error.get()).startsWith("Ошибка базы данных");
    }
}
