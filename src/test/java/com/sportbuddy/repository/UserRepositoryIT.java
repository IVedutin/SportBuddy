package com.sportbuddy.repository;

import com.sportbuddy.AbstractIntegrationTest;
import com.sportbuddy.entity.Role;
import com.sportbuddy.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link UserRepository} against a real PostgreSQL.
 */
class UserRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndLookupByEmailAndPhone() {
        User u = new User("Иван", "Ведутин", "repo-user@example.com", "+79991112233", "hashed-secret");
        u.setRole(Role.USER);
        userRepository.save(u);

        assertThat(userRepository.existsByEmail("repo-user@example.com")).isTrue();
        assertThat(userRepository.existsByPhone("+79991112233")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
        assertThat(userRepository.findByEmail("repo-user@example.com"))
                .isPresent()
                .get()
                .extracting(User::getPhone)
                .isEqualTo("+79991112233");
    }

    @Test
    void prePersistDefaultsRoleToUser() {
        User u = new User("Роль", "Дефолт", "role-default@example.com", "+79994445566", "hashed-secret");
        userRepository.save(u);

        assertThat(userRepository.findByEmail("role-default@example.com"))
                .get()
                .extracting(User::getRole)
                .isEqualTo(Role.USER);
    }
}
