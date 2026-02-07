package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.service.SmsService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestParam String phone) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        // Проверяем, есть ли такой юзер
        if (!userRepository.existsByPhone(cleanPhone)) {
            return ResponseEntity.badRequest().body("Этот номер не зарегистрирован!");
        }

        smsService.sendSms(cleanPhone);
        return ResponseEntity.ok("Код отправлен");
    }

    @PostMapping("/login-phone")
    public ResponseEntity<String> loginByPhone(@RequestParam String phone,
                                               @RequestParam String code,
                                               HttpServletRequest request) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        if (smsService.verifyCode(cleanPhone, code)) {
            User user = userRepository.findByPhone(cleanPhone).orElseThrow();

            // ВХОД В СИСТЕМУ БЕЗ ПАРОЛЯ
            String role = user.getRoleName().startsWith("ROLE_") ? user.getRoleName() : "ROLE_" + user.getRole();

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            return ResponseEntity.ok("Успешно");
        } else {
            return ResponseEntity.badRequest().body("Неверный код");
        }
    }
}
