package com.sportbuddy.service;

import com.sportbuddy.dto.RegisterRequest;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<String> register(RegisterRequest registerRequest) {
        logger.info("=== ВНУТРИ AUTH SERVICE ===");

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            logger.warn("Пароли не совпадают");
            return Optional.of("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Email уже существует: {}", registerRequest.getEmail());
            return Optional.of("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            logger.warn("Телефон уже существует: {}", registerRequest.getPhone());
            return Optional.of("Пользователь с таким телефоном уже существует");
        }

        logger.info("Создаем нового пользователя...");

        User user = new User(
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPhone(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        try {
            userRepository.save(user);
            logger.info("Пользователь успешно сохранен в БД: {}", user.getEmail());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка при сохранении в БД: ", e);
            return Optional.of("Ошибка базы данных: " + e.getMessage());
        }
    }
}