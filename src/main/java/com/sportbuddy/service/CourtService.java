package com.sportbuddy.service;

import com.sportbuddy.dto.ParticipantDto;
import com.sportbuddy.dto.BookingDto;
import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return courtTimeSlotRepository.findByCourtId(courtId);
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
}