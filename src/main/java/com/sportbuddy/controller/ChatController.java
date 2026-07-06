package com.sportbuddy.controller;
import lombok.extern.slf4j.Slf4j;

import com.sportbuddy.dto.BookingDto;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.service.CourtService;
import com.sportbuddy.service.GigaChatGrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    @Autowired private GigaChatGrpcService gigaChatGrpcService;
    @Autowired private UserRepository userRepository;
    @Autowired private CourtService courtService;

    @PostMapping("/message")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request, Authentication authentication) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пустое сообщение"));
        }
        try {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            String systemPrompt = buildSystemPrompt(user);
            String response = gigaChatGrpcService.sendMessage(systemPrompt, userMessage);
            if (response != null) {
                return ResponseEntity.ok(Map.of("response", response));
            }
        } catch (Exception e) {
            log.error("ChatController error: " + e.getMessage());
        }
        // Fallback если gRPC не ответил
        return ResponseEntity.ok(Map.of("response", getFallback(userMessage)));
    }

    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты спортивный ассистент приложения SportBuddy. Отвечай на русском, кратко и по делу. ");
        sb.append("Помогай с тренировками, питанием, восстановлением и советами по спорту. ");

        if (user != null) {
            sb.append("Имя пользователя: ").append(user.getFirstName()).append(". ");
            sb.append("Рейтинг: ").append(user.getRating()).append(". ");
            try {
                List<BookingDto> history = courtService.getUserBookingHistory(user.getId());
                if (!history.isEmpty()) {
                    Map<String, Long> sportCount = history.stream()
                            .collect(Collectors.groupingBy(BookingDto::getSportType, Collectors.counting()));
                    String favSport = sportCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("не определён");
                    sb.append("Статистика: ").append(history.size()).append(" игр, любимый спорт — ").append(favSport).append(". ");
                    sb.append("Спорты: ").append(sportCount.entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue() + ")")
                            .collect(Collectors.joining(", "))).append(". ");
                    sb.append("Используй эту статистику для персональных советов.");
                } else {
                    sb.append("У пользователя пока нет истории игр — предложи начать с базовых рекомендаций.");
                }
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

    private String getFallback(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("разминк")) return "Хорошая разминка: 2 мин бег на месте, суставная гимнастика, динамическая растяжка. Итого 10–15 минут.";
        if (lower.contains("питан")) return "За 1.5–2 часа до тренировки: сложные углеводы + белок. После — белок + углеводы в течение 30–60 минут.";
        if (lower.contains("выносливост")) return "Интервальный бег: 1 мин быстро / 2 мин ходьба, 3–4 раза в неделю.";
        if (lower.contains("начинающ")) return "3 дня в неделю: кардио 20 мин + приседания, отжимания, планка.";
        if (lower.contains("восстановлен")) return "После тренировки: растяжка 10 мин, белок в течение 30 мин, 7–9 часов сна.";
        return "Я спортивный ассистент SportBuddy! Спрашивайте о тренировках, питании, восстановлении или видах спорта.";
    }
}
