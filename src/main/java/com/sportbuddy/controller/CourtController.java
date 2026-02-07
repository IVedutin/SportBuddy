package com.sportbuddy.controller;

import com.sportbuddy.entity.*;
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
import com.sportbuddy.entity.RankedMatch;

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

    @GetMapping("/booking-history")
    public String bookingHistory(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<BookingDto> bookingHistory = courtService.getUserBookingHistory(user.getId());

            model.addAttribute("bookings", bookingHistory);
            return "booking-history"; // Имя нового HTML-файла
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }
    @GetMapping("/api/ranked-matches")
    public ResponseEntity<List<RankedMatch>> getRankedMatches() {
        return ResponseEntity.ok(courtService.getUpcomingRankedMatches());
    }
    // НОВЫЙ МЕТОД: ОБРАБОТКА НАЖАТИЯ "СОЗДАТЬ ЧАТ"
    @PostMapping("/lock-chat-creation")
    @ResponseBody
    public ResponseEntity<String> lockChatCreation(@RequestParam Long timeSlotId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean success = courtService.lockChatCreation(timeSlotId, user);

            if (success) {
                return ResponseEntity.ok("Блокировка установлена на 5 минут.");
            } else {
                return ResponseEntity.badRequest().body("Другой пользователь уже создает чат. Попробуйте через 5 минут.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    // НОВЫЙ МЕТОД: ОБРАБОТКА СОХРАНЕНИЯ ССЫЛКИ
    @PostMapping("/save-chat-url")
    @ResponseBody
    public ResponseEntity<String> saveChatUrl(@RequestParam Long timeSlotId, @RequestParam String chatUrl, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean success = courtService.saveChatUrl(timeSlotId, chatUrl, user);

            if (success) {
                return ResponseEntity.ok("Ссылка на чат успешно сохранена!");
            } else {
                return ResponseEntity.badRequest().body("Не удалось сохранить ссылку. Возможно, время блокировки истекло или вы не тот пользователь.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }
    @GetMapping("/ranked-matches")
    public String rankedMatchesPage() {
        return "ranked-matches"; // Новый HTML файл
    }
    // --- НОВЫЕ МЕТОДЫ ---
    // GET-метод для отображения страницы с формой
    @GetMapping("/add-new")
    public String showAddCourtForm(Model model) {
        model.addAttribute("sportCourt", new SportCourt());
        model.addAttribute("sportTypes", courtService.getAllSportTypes());
        return "add-court-form"; // Имя нового HTML-файла
    }
    @PostMapping("/api/ranked-matches/join/{matchId}")
    @ResponseBody
    public ResponseEntity<String> joinMatch(@PathVariable Long matchId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean success = courtService.joinRankedMatch(matchId, user);

            if (success) {
                return ResponseEntity.ok("Вы записаны!");
            } else {
                return ResponseEntity.badRequest().body("Ошибка записи");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    // POST-метод для обработки формы
    @PostMapping("/add-new")
    public String processAddCourtForm(@ModelAttribute SportCourt sportCourt, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        courtService.createCustomCourt(sportCourt, user);

        return "redirect:/dashboard?courtAdded=true"; // Возвращаем на дашборд с сообщением
    }
    // --- КОНЕЦ НОВЫХ МЕТОДОВ ---

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
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
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
    public String getParticipants(@PathVariable Long timeSlotId, Model model, Authentication authentication) { // Добавили Authentication
        try {
            User currentUser = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new RuntimeException("Слот не найден"));

            List<ParticipantDto> participants = courtService.getParticipantsForTimeSlot(timeSlotId);

            model.addAttribute("sportType", timeSlot.getCourt().getSportType().getName());
            model.addAttribute("courtName", timeSlot.getCourt().getName());

            // Форматируем время и дату для генерации названия чата
            String timeString = timeSlot.getStartTime().toLocalTime().toString();
            String dateString = timeSlot.getStartTime().toLocalDate().toString();
            model.addAttribute("timeSlot", timeString + " - " + timeSlot.getEndTime().toLocalTime());

            // --- НОВЫЕ АТРИБУТЫ ДЛЯ JS ---
            model.addAttribute("timeSlotId", timeSlotId);
            model.addAttribute("chatUrl", timeSlot.getChatUrl());
            model.addAttribute("chatCreationLockTime", timeSlot.getChatCreationLockTime());
            model.addAttribute("currentUserId", currentUser.getId());

            if (timeSlot.getChatCreator() != null) {
                model.addAttribute("chatCreatorId", timeSlot.getChatCreator().getId());
                model.addAttribute("chatCreatorFullName", timeSlot.getChatCreator().getFirstName() + " " + timeSlot.getChatCreator().getLastName());
            } else {
                model.addAttribute("chatCreatorId", null);
                model.addAttribute("chatCreatorFullName", "");
            }
            // --- КОНЕЦ НОВЫХ АТРИБУТОВ ---

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

    @PostMapping("/add-review")
    public String addReview(@RequestParam Long courtId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        courtService.addReview(courtId, user, rating, comment);

        // После добавления отзыва возвращаем пользователя на страницу его записей
        return "redirect:/court/my-bookings";
    }
    @GetMapping("/details/{courtId}")
    public String courtDetails(@PathVariable Long courtId, Model model) {
        SportCourt court = courtService.getCourtById(courtId);
        if (court == null) {
            return "redirect:/court/booking"; // Если площадка не найдена
        }

        List<Review> reviews = courtService.getReviewsForCourt(courtId);

        model.addAttribute("court", court);
        model.addAttribute("reviews", reviews);

        return "court-details"; // Имя новой HTML страницы
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

    // Отладочные методы
    @GetMapping("/debug/all-slots")
    @ResponseBody
    public List<CourtTimeSlot> getAllSlots() {
        return courtTimeSlotRepository.findAll();
    }

    @GetMapping("/debug/slots/{courtId}")
    @ResponseBody
    public List<CourtTimeSlot> getSlotsByCourtDebug(@PathVariable Long courtId) {
        return courtService.getTimeSlotsByCourt(courtId);
    }

    private String formatTimeSlot(CourtTimeSlot timeSlot) {
        return timeSlot.getStartTime().toLocalTime() + " - " + timeSlot.getEndTime().toLocalTime();
    }
}
