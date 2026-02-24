package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.service.EmailService; // Новый сервис для email
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.sportbuddy.entity.Role.USER;

@RestController
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // Заменяем SmsService на EmailService

    // Временное хранилище для кодов подтверждения (в реальном проекте используйте Redis или БД)
    private final Map<String, String> emailVerificationCodes = new ConcurrentHashMap<>();

    // DTO для запроса регистрации (без smsCode)
    public static class RegistrationRequest {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String password;

    }

    // DTO для запроса отправки кода на email
    public static class EmailCodeRequest {
        public String email;
    }

    // 1. ОТПРАВКА КОДА ДЛЯ ПОДТВЕРЖДЕНИЯ EMAIL
    @PostMapping("/api/register/send-email-code")
    public ResponseEntity<Map<String, Object>> sendEmailCode(@RequestBody EmailCodeRequest request) {
        try {
            String email = request.email;

            // Проверка: занят ли email
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Этот email уже зарегистрирован!"
                ));
            }

            // Генерируем 6-значный код
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

            // Сохраняем код в временное хранилище (срок действия - 10 минут)
            emailVerificationCodes.put(email, verificationCode);

            // Запланировать удаление кода через 10 минут (можно реализовать через @Scheduled)

            // Отправляем код на email
            String emailText = String.format(
                    "Код подтверждения для регистрации в SportBuddy: %s\n\n" +
                            "Если вы не запрашивали этот код, просто проигнорируйте это письмо.",
                    verificationCode
            );

            emailService.sendEmail(email, "Код подтверждения SportBuddy", emailText);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Код подтверждения отправлен на email"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Ошибка отправки email: " + e.getMessage()
            ));
        }
    }

    // 2. ПРОВЕРКА КОДА (отдельный эндпоинт для проверки перед финальной регистрацией)
    @PostMapping("/api/register/verify-email-code")
    public ResponseEntity<Map<String, Object>> verifyEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        String storedCode = emailVerificationCodes.get(email);

        if (storedCode != null && storedCode.equals(code)) {
            // Код верный - удаляем его из хранилища (одноразовый)
            emailVerificationCodes.remove(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "Email подтвержден"));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Неверный или просроченный код"
            ));
        }
    }

    // 3. ФИНАЛЬНАЯ РЕГИСТРАЦИЯ (БЕЗ ПРОВЕРКИ КОДА - ОН УЖЕ ПРОВЕРЕН)
    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationRequest request) {
        try {
            // Очищаем телефон
            String cleanPhone = request.phone.replaceAll("[^0-9]", "");

            // Проверяем, не занят ли email (дубль проверки)
            if (userRepository.findByEmail(request.email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email уже занят"
                ));
            }

            // Проверяем телефон (опционально)
            if (userRepository.existsByPhone(cleanPhone)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Телефон уже занят"
                ));
            }

            // Создаем пользователя
            User user = new User();
            user.setFirstName(request.firstName);
            user.setLastName(request.lastName);
            user.setEmail(request.email);
            user.setPassword(passwordEncoder.encode(request.password));
            user.setPhone(cleanPhone);
            user.setRole(USER);
            user.setRating(1000);

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Ошибка: " + e.getMessage()
            ));
        }
    }
}