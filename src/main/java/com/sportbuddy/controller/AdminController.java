package com.sportbuddy.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportbuddy.entity.SportCourt;
import com.sportbuddy.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CourtService courtService;

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

    // Площадки на модерации
    @GetMapping("/pending-courts")
    public ResponseEntity<List<SportCourt>> getPendingCourts() {
        return ResponseEntity.ok(courtService.getPendingCourts());
    }

    // Все площадки (для CRUD в админке)
    @GetMapping("/all-courts")
    public ResponseEntity<List<SportCourt>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    // Одобрить площадку
    @PostMapping("/approve-court/{id}")
    public ResponseEntity<String> approveCourt(@PathVariable Long id) {
        courtService.approveCourt(id);
        return ResponseEntity.ok("Площадка одобрена и доступна для записи");
    }

    // Отклонить заявку на площадку (каскадное удаление)
    @DeleteMapping("/reject-court/{id}")
    public ResponseEntity<String> rejectCourt(@PathVariable Long id) {
        try {
            courtService.deleteCourt(id);
            return ResponseEntity.ok("Площадка отклонена и удалена");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Удалить одобренную площадку (каскадное удаление слотов, бронирований, отзывов)
    @DeleteMapping("/court/{id}")
    public ResponseEntity<?> deleteCourt(@PathVariable Long id) {
        try {
            courtService.deleteCourt(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Площадка удалена"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Создать рейтинговый матч
    @PostMapping("/create-ranked-match")
    public ResponseEntity<String> createRankedMatch(@RequestBody RankedMatchRequest request) {
        courtService.createRankedMatch(
                request.title, request.locationName, request.address,
                request.description, request.startTime, request.endTime
        );
        return ResponseEntity.ok("Матч создан!");
    }
}
