package com.sportbuddy.controller;

import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/court-owner")
public class CourtOwnerController {

    @Autowired private UserRepository userRepository;
    @Autowired private SportCourtRepository sportCourtRepository;
    @Autowired private CourtTimeSlotRepository courtTimeSlotRepository;
    @Autowired private BookingRepository bookingRepository;

    @GetMapping("/schedule")
    public ResponseEntity<?> getSchedule(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.COURT && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Доступ только для владельцев площадок"));
        }

        List<SportCourt> courts = sportCourtRepository.findAll().stream()
                .filter(c -> c.getOwner() != null && c.getOwner().getId().equals(user.getId()))
                .collect(Collectors.toList());

        LocalDateTime from = LocalDateTime.now().minusDays(1);

        List<Map<String, Object>> result = courts.stream().map(court -> {
            Map<String, Object> courtMap = new LinkedHashMap<>();
            courtMap.put("courtId", court.getId());
            courtMap.put("courtName", court.getName());
            courtMap.put("address", court.getAddress());
            courtMap.put("sportType", court.getSportType() != null ? court.getSportType().getName() : "");
            courtMap.put("city", court.getCity() != null ? court.getCity().getName() : "");

            List<CourtTimeSlot> slots = courtTimeSlotRepository.findByCourtId(court.getId()).stream()
                    .filter(s -> s.getStartTime().isAfter(from))
                    .sorted(Comparator.comparing(CourtTimeSlot::getStartTime))
                    .collect(Collectors.toList());

            List<Map<String, Object>> slotList = slots.stream().map(slot -> {
                Map<String, Object> slotMap = new LinkedHashMap<>();
                slotMap.put("slotId", slot.getId());
                slotMap.put("startTime", slot.getStartTime());
                slotMap.put("endTime", slot.getEndTime());
                slotMap.put("maxPlayers", court.getMaxPlayers());

                List<Booking> bookings = bookingRepository.findByCourtTimeSlotId(slot.getId());
                slotMap.put("bookedCount", bookings.size());

                List<Map<String, Object>> participants = bookings.stream().map(b -> {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("firstName", b.getUser().getFirstName());
                    p.put("lastName", b.getUser().getLastName());
                    p.put("telegram", b.getUser().getTelegramUsername());
                    p.put("phone", b.getUser().getPhone());
                    return p;
                }).collect(Collectors.toList());

                slotMap.put("participants", participants);
                return slotMap;
            }).collect(Collectors.toList());

            courtMap.put("slots", slotList);
            return courtMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
