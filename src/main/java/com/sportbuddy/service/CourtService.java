package com.sportbuddy.service;

import com.sportbuddy.dto.ParticipantDto;
import com.sportbuddy.dto.BookingDto;
import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
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
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportCourtRepository sportCourtRepository;

    @Autowired
    private CourtTimeSlotRepository courtTimeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // Получить все виды спорта
    public List<SportType> getAllSportTypes() {
        return sportTypeRepository.findAll();
    }

    // Получить площадки по виду спорта
    public List<SportCourt> getCourtsBySportType(Long sportTypeId) {
        return sportCourtRepository.findBySportTypeId(sportTypeId);
    }

    // Получить временные слоты для площадки
    public List<CourtTimeSlot> getTimeSlotsByCourt(Long courtId) {
        LocalDateTime now = LocalDateTime.now();
        List<CourtTimeSlot> allSlots = courtTimeSlotRepository.findByCourtId(courtId);

        // Фильтруем только будущие слоты
        List<CourtTimeSlot> futureSlots = allSlots.stream()
                .filter(slot -> slot.getStartTime().isAfter(now))
                .collect(Collectors.toList());

        System.out.println("Court " + courtId + " - Total slots: " + allSlots.size() + ", Future slots: " + futureSlots.size());

        return futureSlots;
    }

    // Получить количество записанных на временной слот
    public Integer getBookingCountForTimeSlot(Long timeSlotId) {
        return bookingRepository.countByCourtTimeSlotId(timeSlotId);
    }

    // Получить записи пользователя
    public List<BookingDto> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream()
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
                            participantCount
                    );
                })
                .collect(Collectors.toList());
    }

    // Отменить запись
    public boolean cancelBooking(Long bookingId, Long userId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Запись не найдена"));

            // Проверяем что пользователь отменяет свою запись
            if (!booking.getUser().getId().equals(userId)) {
                return false;
            }

            bookingRepository.delete(booking);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Записать пользователя на время
    public boolean bookTimeSlot(User user, Long timeSlotId) {
        try {
            // Проверяем не записан ли уже
            if (bookingRepository.existsByUserIdAndCourtTimeSlotId(user.getId(), timeSlotId)) {
                return false;
            }

            CourtTimeSlot timeSlot = courtTimeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new RuntimeException("Временной слот не найден"));

            Booking booking = new Booking(user, timeSlot);
            bookingRepository.save(booking);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить участников для временного слота
    public List<ParticipantDto> getParticipantsForTimeSlot(Long timeSlotId) {
        List<Booking> bookings = bookingRepository.findByCourtTimeSlotId(timeSlotId);

        return bookings.stream()
                .map(booking -> {
                    User user = booking.getUser();
                    return new ParticipantDto(
                            user.getFirstName(),
                            user.getLastName(),
                            user.getPhone(),
                            user.getTelegramUsername()
                    );
                })
                .collect(Collectors.toList());
    }

    // Очистка старых слотов и создание новых каждый день в 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldTimeSlots() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<CourtTimeSlot> oldSlots = courtTimeSlotRepository.findAll().stream()
                .filter(slot -> slot.getStartTime().isBefore(yesterday))
                .collect(Collectors.toList());

        courtTimeSlotRepository.deleteAll(oldSlots);

        // Создаем новые слоты на 3 дня вперед
        initializeTimeSlotsForNextDays();
    }

    private void initializeTimeSlotsForNextDays() {
        List<SportCourt> allCourts = sportCourtRepository.findAll();
        LocalDate today = LocalDate.now();

        for (SportCourt court : allCourts) {
            for (int day = 0; day < 3; day++) {
                LocalDate currentDate = today.plusDays(day);

                for (int hour = 6; hour < 22; hour++) {
                    LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.of(hour, 0));
                    LocalDateTime endTime = startTime.plusHours(1);

                    // Проверяем, не существует ли уже такой слот
                    boolean slotExists = courtTimeSlotRepository.findByCourtId(court.getId()).stream()
                            .anyMatch(slot -> slot.getStartTime().equals(startTime));

                    if (!slotExists) {
                        CourtTimeSlot slot = new CourtTimeSlot(court, startTime, endTime, 0);
                        courtTimeSlotRepository.save(slot);
                    }
                }
            }
        }
    }
    @Scheduled(cron = "0 0 0 * * ?") // Запускать каждый день в 00:00
    @Transactional
    public void updateTimeSlotsDaily() {
        System.out.println("=== DAILY TIME SLOTS UPDATE ===");

        LocalDate today = LocalDate.now();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);

        // Удаляем слоты старше вчерашнего дня
        List<CourtTimeSlot> oldSlots = courtTimeSlotRepository.findAll().stream()
                .filter(slot -> slot.getStartTime().isBefore(cutoffTime))
                .collect(Collectors.toList());

        courtTimeSlotRepository.deleteAll(oldSlots);
        System.out.println("Deleted " + oldSlots.size() + " old slots");

        // Добавляем слоты на новый день (послезавтра)
        List<SportCourt> allCourts = sportCourtRepository.findAll();
        LocalDate newDay = today.plusDays(2); // Добавляем слоты на послезавтра

        for (SportCourt court : allCourts) {
            addTimeSlotsForSingleDay(court, newDay);
        }

        System.out.println("Daily update completed");
    }

    private void addTimeSlotsForSingleDay(SportCourt court, LocalDate date) {
        // Проверяем, есть ли уже слоты на эту дату
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