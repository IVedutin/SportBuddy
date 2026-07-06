package com.sportbuddy.service;

import com.sportbuddy.AbstractIntegrationTest;
import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Booking flow through {@link CourtService} against a real database:
 * successful booking, duplicate rejection and time-overlap rejection.
 */
class CourtBookingIT extends AbstractIntegrationTest {

    @Autowired private CourtService courtService;
    @Autowired private SportTypeRepository sportTypeRepository;
    @Autowired private SportCourtRepository sportCourtRepository;
    @Autowired private CourtTimeSlotRepository courtTimeSlotRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;

    private SportCourt court(SportType type, String name) {
        SportCourt c = new SportCourt();
        c.setName(name);
        c.setSportType(type);
        c.setStatus("APPROVED");
        return sportCourtRepository.save(c);
    }

    @Test
    void bookTimeSlot_success_thenDuplicateRejected_thenOverlapRejected() {
        SportType type = sportTypeRepository.save(new SportType("Теннис", "desc"));
        SportCourt court1 = court(type, "Корт 1");
        SportCourt court2 = court(type, "Корт 2");

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        CourtTimeSlot slot1 = courtTimeSlotRepository.save(new CourtTimeSlot(court1, start, end, 0));
        CourtTimeSlot slot2 = courtTimeSlotRepository.save(new CourtTimeSlot(court2, start, end, 0));

        User user = userRepository.save(
                new User("Игрок", "Первый", "player1@example.com", "+79990009999", "hashed-secret"));

        // 1. First booking succeeds and is persisted.
        assertThat(courtService.bookTimeSlot(user, slot1.getId())).isTrue();
        assertThat(bookingRepository.countByCourtTimeSlotId(slot1.getId())).isEqualTo(1);

        // 2. Booking the same slot again is rejected.
        assertThat(courtService.bookTimeSlot(user, slot1.getId())).isFalse();
        assertThat(bookingRepository.countByCourtTimeSlotId(slot1.getId())).isEqualTo(1);

        // 3. Booking another court at the SAME time is rejected (overlap).
        assertThat(courtService.bookTimeSlot(user, slot2.getId())).isFalse();
        assertThat(bookingRepository.countByCourtTimeSlotId(slot2.getId())).isEqualTo(0);
    }

    @Test
    void bookTimeSlot_nonExistingSlot_returnsFalse() {
        User user = userRepository.save(
                new User("Игрок", "Второй", "player2@example.com", "+79990008888", "hashed-secret"));

        assertThat(courtService.bookTimeSlot(user, 999_999L)).isFalse();
    }
}
