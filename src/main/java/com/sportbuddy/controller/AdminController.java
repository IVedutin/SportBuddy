package com.sportbuddy.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportbuddy.entity.SportCourt;
// Импорт для LocalDateTime
import java.time.LocalDateTime;
import java.util.List;

import com.sportbuddy.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CourtService courtService;

    // Класс-обертка для получения данных о матче
    public static class RankedMatchRequest {
        public String title;
        public String locationName;
        public String address;
        public String description;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        public LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        public LocalDateTime endTime;
    }

    // Получить список площадок на модерации
    @GetMapping("/pending-courts")
    public ResponseEntity<List<SportCourt>> getPendingCourts() {
        return ResponseEntity.ok(courtService.getPendingCourts());
    }

    // Одобрить площадку
    @PostMapping("/approve-court/{id}")
    public ResponseEntity<String> approveCourt(@PathVariable Long id) {
        courtService.approveCourt(id);
        return ResponseEntity.ok("Площадка одобрена и доступна для записи");
    }

    // Создать рейтинговый матч
    @PostMapping("/create-ranked-match")
    public ResponseEntity<String> createRankedMatch(@RequestBody RankedMatchRequest request) {
        courtService.createRankedMatch(
                request.title,
                request.locationName,
                request.address,
                request.description,
                request.startTime,
                request.endTime
        );
        return ResponseEntity.ok("Матч создан!");
    }

    // Получить список всех ПЛОЩАДОК (опционально, если нужно)
    @GetMapping("/all-courts")
    public ResponseEntity<List<SportCourt>> getAllCourts() {
        return ResponseEntity.ok(courtService.getCourtsBySportType(1L));
    }

    // Отклонить площадку
    @DeleteMapping("/reject-court/{id}")
    public ResponseEntity<String> rejectCourt(@PathVariable Long id) {
        courtService.rejectCourt(id);
        return ResponseEntity.ok("Площадка отклонена и удалена");
    }
}
