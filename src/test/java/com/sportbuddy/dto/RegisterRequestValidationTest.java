package com.sportbuddy.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean-validation rules on the registration payload — in particular the
 * {@code @Email} constraint (email validation requirement).
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RegisterRequest valid() {
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
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void malformedEmail_isRejected() {
        RegisterRequest r = valid();
        r.setEmail("not-an-email");
        assertThat(validator.validate(r))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void blankEmail_isRejected() {
        RegisterRequest r = valid();
        r.setEmail("");
        assertThat(validator.validate(r))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void tooShortPassword_isRejected() {
        RegisterRequest r = valid();
        r.setPassword("123");
        assertThat(validator.validate(r))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
