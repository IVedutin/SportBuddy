package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.entity.Role;
import com.sportbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private UserRepository userRepository;

    // Страница с предложением купить подписку
    @GetMapping
    public String subscriptionPage() {
        return "subscription-page"; // Новый HTML файл
    }

    // "Фейковая" покупка подписки
    @PostMapping("/upgrade")
    public String upgradeToPremium(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.PREMIUM);
        userRepository.save(user);

        // Важно! Нужно заново "залогинить" пользователя, чтобы Spring Security увидел новую роль.
        // Самый простой способ - редирект на logout, а потом на страницу входа.
        // Но для простоты пока просто кидаем на дашборд. Роль применится после следующего входа.
        return "redirect:/dashboard?upgraded=true";
    }
}