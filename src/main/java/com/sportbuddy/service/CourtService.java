package com.sportbuddy.service;

import com.sportbuddy.dto.ParticipantDto;
import com.sportbuddy.dto.BookingDto;
import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    @Autowired
    private RankedMatchRepository rankedMatchRepository;

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportCourtRepository sportCourtRepository;

    @Autowired
    private CourtTimeSlotRepository courtTimeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository; // ДОБАВЬ ЭТО



    @Transactional
    public SportCourt createCustomCourt(SportCourt court, User owner) {
        court.setOwner(owner);
        court.setStatus("PENDING_APPROVAL");
        SportType sportType = sportTypeRepository.findById(court.getSportType().getId())
                .orElseThrow(() -> new RuntimeException("SportType not found"));
        court.setSportType(sportType);
        SportCourt savedCourt = sportCourtRepository.save(court);
        initializeSlotsForNewCourt(savedCourt);
        return savedCourt;
    }

    private void initializeSlotsForNewCourt(SportCourt court) {
        LocalDate today = LocalDate.now();
        for (int day = 0; day < 3; day++) {
            LocalDate currentDate = today.plusDays(day);
            addTimeSlotsForSingleDay(court, currentDate);
        }
        System.out.println("Initialized slots for new court: " + court.getName());
    }

    @Transactional
    public boolean lockChatCreation(Long timeSlotId, User user) {
        CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        if (timeSlot.getChatCreationLockTime() != null && timeSlot.getChatCreationLockTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        timeSlot.setChatCreationLockTime(LocalDateTime.now().plusMinutes(5));
        timeSlot.setChatCreator(user);
        courtTimeSlotRepository.save(timeSlot);
        return true;
    }

    public List<Review> getReviewsForCourt(Long courtId) {
        return reviewRepository.findByCourtId(courtId);
    }


    @Transactional
    public boolean saveChatUrl(Long timeSlotId, String chatUrl, User user) {
        CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        if (timeSlot.getChatCreator() == null || !timeSlot.getChatCreator().getId().equals(user.getId())) {
            return false;
        }
        if (timeSlot.getChatCreationLockTime() == null || timeSlot.getChatCreationLockTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        timeSlot.setChatUrl(chatUrl);
        timeSlot.setChatCreationLockTime(null);
        timeSlot.setChatCreator(null);

        courtTimeSlotRepository.save(timeSlot);

        // --- ИСПРАВЛЕНИЕ ---
        // Принудительно записываем все изменения в базу данных ПЕРЕД тем, как отдать ответ
        courtTimeSlotRepository.flush();
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        return true;
    }
    public void addReview(Long courtId, User author, int rating, String comment) {
        SportCourt court = sportCourtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        Review review = new Review();
        review.setCourt(court);
        review.setAuthor(author);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);
    }

    // Остальные методы без изменений...
    public List<SportType> getAllSportTypes() {
        return sportTypeRepository.findAll();
    }

    public List<SportCourt> getPendingCourts() {
        return sportCourtRepository.findByStatus("PENDING_APPROVAL");
    }

    // 2. Одобрить площадку
    @Transactional
    public void approveCourt(Long courtId) {
        SportCourt court = sportCourtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Площадка не найдена"));

        court.setStatus("APPROVED");
        sportCourtRepository.save(court);

        // Сразу генерируем слоты, чтобы на нее можно было записаться
        initializeSlotsForNewCourt(court);
    }

    // 3. Отклонить (удалить) площадку
    @Transactional
    public void rejectCourt(Long courtId) {
        SportCourt court = sportCourtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Площадка не найдена"));

        // Тут можно просто удалить, либо поставить статус "REJECTED"
        sportCourtRepository.delete(court);
    }

    public List<SportCourt> getCourtsBySportType(Long sportTypeId) {
        // ВАЖНО: Передаем статус "APPROVED"
        // Теперь пользователи не увидят PENDING_APPROVAL площадки
        return sportCourtRepository.findBySportTypeIdAndStatus(sportTypeId, "APPROVED");
    }
    public void createRankedMatch(String title, String locName, String address, String desc, LocalDateTime start, LocalDateTime end) {
        RankedMatch match = new RankedMatch();
        match.setTitle(title);
        match.setLocationName(locName); // Текст
        match.setAddress(address);      // Текст
        match.setDescription(desc);
        match.setStartTime(start);
        match.setEndTime(end);

        rankedMatchRepository.save(match);
    }
    @Transactional
    public boolean joinRankedMatch(Long matchId, User user) {
        RankedMatch match = rankedMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Матч не найден"));

        // Добавляем пользователя в список
        match.addParticipant(user);
        rankedMatchRepository.save(match);
        return true;
    }

    // Получить все будущие матчи (для пользователей)
    public List<RankedMatch> getUpcomingRankedMatches() {
        return rankedMatchRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
    }
    public List<CourtTimeSlot> getTimeSlotsByCourt(Long courtId) {
        LocalDateTime now = LocalDateTime.now();
        List<CourtTimeSlot> allSlots = courtTimeSlotRepository.findByCourtId(courtId);
        List<CourtTimeSlot> futureSlots = allSlots.stream()
                .filter(slot -> slot.getStartTime().isAfter(now))
                .collect(Collectors.toList());
        System.out.println("Court " + courtId + " - Total slots: " + allSlots.size() + ", Future slots: " + futureSlots.size());
        return futureSlots;
    }

    public Integer getBookingCountForTimeSlot(Long timeSlotId) {
        return bookingRepository.countByCourtTimeSlotId(timeSlotId);
    }

    public List<BookingDto> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> booking.getCourtTimeSlot().getStartTime().isAfter(now))
                .map(booking -> {
                    CourtTimeSlot timeSlot = booking.getCourtTimeSlot();
                    SportCourt court = timeSlot.getCourt();
                    SportType sportType = court.getSportType();
                    Integer participantCount = bookingRepository.countByCourtTimeSlotId(timeSlot.getId());
                    return new BookingDto(
                            booking.getId(),
                            timeSlot.getId(),
                            sportType.getName(),
                            court.getName(),
                            timeSlot.getStartTime().toLocalTime() + " - " + timeSlot.getEndTime().toLocalTime(),
                            timeSlot.getStartTime().toLocalDate().toString(),
                            participantCount,court.getId()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<BookingDto> getUserBookingHistory(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> booking.getCourtTimeSlot().getEndTime().isBefore(now))
                .map(booking -> {
                    CourtTimeSlot timeSlot = booking.getCourtTimeSlot();
                    SportCourt court = timeSlot.getCourt();
                    SportType sportType = court.getSportType();
                    Integer participantCount = bookingRepository.countByCourtTimeSlotId(timeSlot.getId());
                    return new BookingDto(
                            booking.getId(),
                            timeSlot.getId(),
                            sportType.getName(),
                            court.getName(),
                            timeSlot.getStartTime().toLocalTime() + " - " + timeSlot.getEndTime().toLocalTime(),
                            timeSlot.getStartTime().toLocalDate().toString(),
                            participantCount,court.getId()
                    );
                })
                .collect(Collectors.toList());
    }
    public SportCourt getCourtById(Long courtId) {
        return sportCourtRepository.findById(courtId).orElse(null);
    }

    public boolean cancelBooking(Long bookingId, Long userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Запись не найдена"));
            if (!booking.getUser().getId().equals(userId)) {
                return false;
            }
            bookingRepository.delete(booking);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean bookTimeSlot(User user, Long timeSlotId) {
        try {
            // 1. Сначала достаем слот, чтобы узнать его время
            CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new RuntimeException("Временной слот не найден"));

            // 2. ПРОВЕРКА: Записан ли уже пользователь на ЭТОТ ЖЕ слот?
            if (bookingRepository.existsByUserIdAndCourtTimeSlotId(user.getId(), timeSlotId)) {
                System.out.println("Пользователь уже записан в этот слот");
                return false;
            }

            // 3. НОВАЯ ПРОВЕРКА: Записан ли пользователь ВООБЩЕ КУДА-ЛИБО в это время?
            boolean hasConflict = bookingRepository.existsOverlappingBooking(
                    user.getId(),
                    timeSlot.getStartTime(),
                    timeSlot.getEndTime()
            );

            if (hasConflict) {
                System.out.println("Ошибка: У пользователя уже есть игра в это время на другой площадке!");
                return false;
            }

            // 4. Если всё чисто — создаем бронь
            Booking booking = new Booking(user, timeSlot);
            booking.setBookingTime(timeSlot.getStartTime()); // Не забываем про фикс из прошлого шага

            bookingRepository.save(booking);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ParticipantDto> getParticipantsForTimeSlot(Long timeSlotId) {
        List<Booking> bookings = bookingRepository.findByCourtTimeSlotId(timeSlotId);
        return bookings.stream()
                .map(booking -> {
                    User user = booking.getUser();
                    return new ParticipantDto(
                            user.getFirstName(),
                            user.getLastName(),
                            user.getTelegramUsername()
                    );
                })
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateTimeSlotsDaily() {
        System.out.println("=== DAILY TIME SLOTS UPDATE ===");
        LocalDate today = LocalDate.now();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
        List<CourtTimeSlot> oldSlots = courtTimeSlotRepository.findAll().stream()
                .filter(slot -> slot.getStartTime().isBefore(cutoffTime))
                .collect(Collectors.toList());
        courtTimeSlotRepository.deleteAll(oldSlots);
        System.out.println("Deleted " + oldSlots.size() + " old slots");
        List<SportCourt> allCourts = sportCourtRepository.findAll();
        LocalDate newDay = today.plusDays(2);
        for (SportCourt court : allCourts) {
            addTimeSlotsForSingleDay(court, newDay);
        }
        System.out.println("Daily update completed");
    }

    private void addTimeSlotsForSingleDay(SportCourt court, LocalDate date) {
        boolean hasSlotsForDate = courtTimeSlotRepository.findByCourtId(court.getId()).stream()
                .anyMatch(slot -> slot.getStartTime().toLocalDate().equals(date));
        if (!hasSlotsForDate) {
            for (int hour = 6; hour < 22; hour++) {
                LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, 0));
                LocalDateTime endTime = startTime.plusHours(1);
                CourtTimeSlot slot = new CourtTimeSlot(court, startTime, endTime, 0);
                courtTimeSlotRepository.save(slot);
            }
            System.out.println("Added slots for " + court.getName() + " on " + date);
        }
    }
}
