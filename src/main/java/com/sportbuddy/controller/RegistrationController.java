package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.service.SmsService; // Импортируем SMS сервис
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.sportbuddy.entity.Role.USER;

@RestController
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmsService smsService; // Подключаем сервис

    // DTO добавили поле smsCode
    public static class RegistrationRequest {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String password;
        public String smsCode; // <-- НОВОЕ ПОЛЕ
    }

    // 1. ОТПРАВКА КОДА ДЛЯ РЕГИСТРАЦИИ
    @PostMapping("/api/register/send-code")
    public ResponseEntity<Map<String, Object>> sendRegisterCode(@RequestParam String phone) {
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", "");

            // Проверка: занят ли телефон
            if (userRepository.existsByPhone(cleanPhone)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Этот номер уже зарегистрирован!"));
            }

            // Отправляем код
            smsService.sendSms(cleanPhone);
            return ResponseEntity.ok(Map.of("success", true, "message", "Код отправлен"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ошибка смс: " + e.getMessage()));
        }
    }

    // 2. ФИНАЛЬНАЯ РЕГИСТРАЦИЯ
    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationRequest request) {
        try {
            // Очищаем телефон
            String cleanPhone = request.phone.replaceAll("[^0-9]", "");

            // --- ГЛАВНАЯ ПРОВЕРКА КОДА ---
            if (!smsService.verifyCode(cleanPhone, request.smsCode)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Неверный код из СМС!"));
            }
            // -----------------------------

            if (userRepository.findByEmail(request.email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email уже занят"));
            }

            // Создаем пользователя
            User user = new User();
            user.setFirstName(request.firstName);
            user.setLastName(request.lastName);
            user.setEmail(request.email);
            user.setPassword(passwordEncoder.encode(request.password));
            user.setPhone(cleanPhone); // Сохраняем подтвержденный номер
            user.setRole(USER);
            user.setRating(1000);

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ошибка: " + e.getMessage()));
        }
    }
}
