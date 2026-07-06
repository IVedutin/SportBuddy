package com.sportbuddy.service;

import com.sportbuddy.entity.Role;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomUserDetailsService} — mapping a domain user to
 * Spring Security {@code UserDetails}, including the role authority.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_returnsUserDetailsWithRoleAuthority() {
        User user = new User("Иван", "Ведутин", "ivan@example.com", "+70000000000", "hashed");
        user.setRole(Role.PREMIUM);
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("ivan@example.com");

        assertThat(details.getUsername()).isEqualTo("ivan@example.com");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM"));
    }

    @Test
    void loadUserByUsername_whenUserMissing_throws() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
