package com.sportbuddy.controller;

import com.sportbuddy.dto.RegisterRequest;
import com.sportbuddy.dto.ApiResponse;
import com.sportbuddy.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("=== НАЧАЛО РЕГИСТРАЦИИ ===");
        logger.info("Получен запрос на регистрацию: {}", registerRequest.getEmail());

        try {
            if (authService == null) {
                logger.error("AuthService is NULL!");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Сервис недоступен"));
            }

            Optional<String> error = authService.register(registerRequest);

            if (error.isPresent()) {
                logger.error("Ошибка регистрации: {}", error.get());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(error.get()));
            }

            logger.info("Регистрация успешна для: {}", registerRequest.getEmail());
            return ResponseEntity.ok()
                    .body(ApiResponse.success("Регистрация успешно завершена"));

        } catch (Exception e) {
            logger.error("Исключение при регистрации: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Внутренняя ошибка сервера: " + e.getMessage()));
        }
    }
}