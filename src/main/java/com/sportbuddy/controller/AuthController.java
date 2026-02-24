package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Временное хранилище для кодов
    private final Map<String, String> emailVerificationCodes = new ConcurrentHashMap<>();

    // DTO для запроса
    public static class EmailCodeRequest {
        public String email;
    }

    public static class EmailLoginRequest {
        public String email;
        public String code;
    }

    // Отправка кода на email
    @PostMapping("/send-email-code")
    public ResponseEntity<String> sendEmailCode(@RequestBody EmailCodeRequest request) {
        String email = request.email;

        // Проверяем, есть ли такой юзер
        if (!userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Этот email не зарегистрирован!");
        }

        // Генерируем код
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // Сохраняем код
        emailVerificationCodes.put(email, code);

        // Отправляем код на email
        String message = String.format(
                "Ваш код для входа в SportBuddy: %s\n\n" +
                        "Никому не сообщайте этот код!",
                code
        );

        emailService.sendEmail(email, "Код для входа в SportBuddy", message);

        return ResponseEntity.ok("Код отправлен на email");
    }

    // Вход по коду из email
    @PostMapping("/login-email")
    public ResponseEntity<String> loginByEmail(@RequestBody EmailLoginRequest request,
                                               HttpServletRequest httpRequest) {
        String email = request.email;
        String code = request.code;

        // Проверяем код
        String storedCode = emailVerificationCodes.get(email);

        if (storedCode != null && storedCode.equals(code)) {
            // Удаляем использованный код
            emailVerificationCodes.remove(email);

            User user = userRepository.findByEmail(email).orElseThrow();

            // ВХОД В СИСТЕМУ БЕЗ ПАРОЛЯ
            String role = user.getRoleName().startsWith("ROLE_") ? user.getRoleName() : "ROLE_" + user.getRole();

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            return ResponseEntity.ok("Успешный вход");
        } else {
            return ResponseEntity.badRequest().body("Неверный или просроченный код");
        }
    }
}