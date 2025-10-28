package com.sportbuddy.controller;

import com.sportbuddy.entity.SportType;
import com.sportbuddy.entity.SportCourt;
import com.sportbuddy.entity.CourtTimeSlot;
import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import com.sportbuddy.repository.CourtTimeSlotRepository;
import com.sportbuddy.dto.ParticipantDto;
import com.sportbuddy.dto.BookingDto;
import com.sportbuddy.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/court")
public class CourtController {

    @Autowired
    private CourtService courtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtTimeSlotRepository courtTimeSlotRepository;

    // Главная страница выбора спорта
    @GetMapping("/booking")
    public String bookingPage(Model model) {
        List<SportType> sportTypes = courtService.getAllSportTypes();
        model.addAttribute("sportTypes", sportTypes);
        return "court-booking";
    }

    // Получить площадки для выбранного вида спорта (AJAX)
    @GetMapping("/courts/{sportTypeId}")
    @ResponseBody
    public List<SportCourt> getCourtsBySportType(@PathVariable Long sportTypeId) {
        return courtService.getCourtsBySportType(sportTypeId);
    }

    // Получить временные слоты для выбранной площадки (AJAX)
    @GetMapping("/timeslots/{courtId}")
    @ResponseBody
    public List<CourtTimeSlot> getTimeSlotsByCourt(@PathVariable Long courtId) {
        return courtService.getTimeSlotsByCourt(courtId);
    }

    // Записаться на время
    @PostMapping("/book")
    @ResponseBody
    public ResponseEntity<String> bookTimeSlot(@RequestParam Long timeSlotId, Authentication authentication) {
        try {
            // Получаем текущего пользователя
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Пытаемся записать
            boolean success = courtService.bookTimeSlot(user, timeSlotId);

            if (success) {
                return ResponseEntity.ok("Успешно записаны");
            } else {
                return ResponseEntity.badRequest().body("Не удалось записаться (возможно, уже записаны)");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    // Получить количество записанных на временной слот
    @GetMapping("/booking-count/{timeSlotId}")
    @ResponseBody
    public Integer getBookingCount(@PathVariable Long timeSlotId) {
        return courtService.getBookingCountForTimeSlot(timeSlotId);
    }

    // Страница участников
    @GetMapping("/participants/{timeSlotId}")
    public String getParticipants(@PathVariable Long timeSlotId, Model model) {
        try {
            CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new RuntimeException("Слот не найден"));

            List<ParticipantDto> participants = courtService.getParticipantsForTimeSlot(timeSlotId);

            model.addAttribute("sportType", timeSlot.getCourt().getSportType().getName());
            model.addAttribute("courtName", timeSlot.getCourt().getName());
            model.addAttribute("timeSlot", formatTimeSlot(timeSlot));
            model.addAttribute("participants", participants);

            return "participants";
        } catch (Exception e) {
            return "redirect:/court/booking";
        }
    }

    // Мои записи
    @GetMapping("/my-bookings")
    public String myBookings(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<BookingDto> bookings = courtService.getUserBookings(user.getId());
            model.addAttribute("bookings", bookings);

            return "my-bookings";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    // Отменить запись
    @PostMapping("/cancel-booking")
    @ResponseBody
    public ResponseEntity<String> cancelBooking(@RequestParam Long bookingId, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean success = courtService.cancelBooking(bookingId, user.getId());

            if (success) {
                return ResponseEntity.ok("Запись отменена");
            } else {
                return ResponseEntity.badRequest().body("Не удалось отменить запись");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    private String formatTimeSlot(CourtTimeSlot timeSlot) {
        return timeSlot.getStartTime().toLocalTime() + " - " + timeSlot.getEndTime().toLocalTime();
    }
}